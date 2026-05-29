# Guide d'implémentation — Synchronisation multi-appareils (Supabase)

Ce document décrit pas à pas comment reproduire la fonctionnalité de synchronisation cloud
entre appareils dans VetNutriMP. Il s'adresse à un agent IA ou à un développeur qui part
d'une branche sans les fichiers concernés.

---

## Vue d'ensemble

```
Appareil A ──push──▶ Supabase Storage (bucket vetnutri-sync)
                              │
Appareil B ◀──pull───────────┘
```

- **Auth** : Supabase GoTrue (e-mail + mot de passe).
- **Stockage** : un bucket privé par utilisateur (`{userId}/manifest.json` + `{userId}/latest.json`).
- **Format** : export JSON existant (`ExportImportRepository.exportAllEnvelope()`) encapsulé dans une `SyncEnvelope`.
- **Conflits** : détectés localement par comparaison de timestamps ; l'utilisateur choisit.

---

## Étape 0 — Créer le projet Supabase

1. Créer un projet sur [supabase.com](https://supabase.com).
2. Dans **Storage** → créer un bucket `vetnutri-sync` en mode **privé**.
3. Ajouter la policy RLS suivante sur le bucket :

```sql
-- SELECT / INSERT / UPDATE / DELETE
(auth.uid()::text) = (storage.foldername(name))[1]
```

   Cela signifie : chaque utilisateur ne voit que son propre dossier `{userId}/`.

4. Récupérer depuis **Settings → API** :
   - `Project URL` → `SUPABASE_URL`
   - `anon public key` → `SUPABASE_ANON_KEY`

---

## Étape 1 — Dépendances Gradle

### `gradle/libs.versions.toml`

```toml
[versions]
supabase = "3.1.4"

[libraries]
supabase-auth    = { module = "io.github.jan-tennert.supabase:auth-kt",    version.ref = "supabase" }
supabase-storage = { module = "io.github.jan-tennert.supabase:storage-kt", version.ref = "supabase" }
```

### `composeApp/build.gradle.kts`

En haut du fichier, lire `local.properties` **et** les variables d'environnement :

```kotlin
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}
val supabaseUrl: String = localProps.getProperty("SUPABASE_URL")
    ?: System.getenv("SUPABASE_URL") ?: ""
val supabaseAnonKey: String = localProps.getProperty("SUPABASE_ANON_KEY")
    ?: System.getenv("SUPABASE_ANON_KEY") ?: ""
```

Dans le bloc `commonMain.dependencies` :

```kotlin
implementation(libs.supabase.auth)
implementation(libs.supabase.storage)
```

Ajouter la tâche `generateSecrets` qui génère `AppSecretsGenerated.kt` dans `build/` :

```kotlin
val generateSecrets by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/secrets/kotlin")
    outputs.dir(outputDir)
    inputs.property("supabaseUrl", supabaseUrl)
    inputs.property("supabaseKey", supabaseAnonKey)
    doLast {
        outputDir.get().asFile.mkdirs()
        File(outputDir.get().asFile, "AppSecretsGenerated.kt").writeText("""
            package fr.vetbrain.vetnutri_mp.Utils
            internal const val SUPABASE_URL_VALUE: String      = "$supabaseUrl"
            internal const val SUPABASE_ANON_KEY_VALUE: String = "$supabaseAnonKey"
        """.trimIndent())
    }
}
kotlin.sourceSets.commonMain {
    kotlin.srcDir(generateSecrets.map { it.outputs.files })
}
```

---

## Étape 2 — Secrets locaux

Ajouter dans `local.properties` (à la racine, **ignoré par git**) :

```properties
SUPABASE_URL=https://xxxxxxxxxxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
```

---

## Étape 3 — `AppSecrets.kt`

Fichier : `Utils/AppSecrets.kt`

```kotlin
package fr.vetbrain.vetnutri_mp.Utils

object AppSecrets {
    val supabaseUrl: String?      = SUPABASE_URL_VALUE.ifBlank { null }
    val supabaseAnonKey: String?  = SUPABASE_ANON_KEY_VALUE.ifBlank { null }
}
```

Les constantes `SUPABASE_URL_VALUE` / `SUPABASE_ANON_KEY_VALUE` sont injectées par `generateSecrets`.

---

## Étape 4 — Modèles domaine

Fichier : `Data/SyncModels.kt`

| Classe | Rôle |
|--------|------|
| `AuthState` | Sealed class : `Unauthenticated`, `Loading`, `Authenticated(userId, email)`, `Error(msg)` |
| `SyncConfig` | Données persistées localement (deviceId, deviceName, lastPushMs, lastPullMs) |
| `SyncManifest` | Fichier léger stocké dans le cloud (`manifest.json`) — pas de payload |
| `SyncEnvelope` | Payload complet (`latest.json`) — contient `data: ApiEnvelope` |
| `SyncResult` | Résultats : `PushSuccess`, `PullSuccess`, `ConflictDetected`, `SchemaIncompatible`, `AlreadyUpToDate`, `Error` |

`ApiEnvelope` est le type retourné par `ExportImportRepository.exportAllEnvelope()` — il existe déjà dans le projet.

---

## Étape 5 — `SyncSessionManager.kt`

Fichier : `Service/SyncSessionManager.kt`

Implémente `io.github.jan.supabase.auth.SessionManager` pour persister le JWT de Supabase
dans `PreferencesStorage` (survive aux redémarrages) :

```kotlin
class SyncSessionManager(private val prefs: PreferencesStorage) : SessionManager {
    private val json = Json { ignoreUnknownKeys = true }
    private val KEY = "sync_supabase_session"

    override suspend fun saveSession(session: UserSession) {
        prefs.saveString(KEY, json.encodeToString(session))
    }
    override suspend fun loadSession(): UserSession? {
        val raw = prefs.getString(KEY)
        return if (raw.isBlank()) null
               else try { json.decodeFromString(raw) } catch (_: Exception) { null }
    }
    override suspend fun deleteSession() { prefs.remove(KEY) }
}
```

---

## Étape 6 — `AuthService.kt`

Fichier : `Service/AuthService.kt`

Wraps `supabase.auth` avec des méthodes `suspend` simples :

- `signIn(email, password): Result<Unit>`
- `signUp(email, password): Result<Unit>`
- `signOut()`
- `restoreSession(): AuthState`
- `currentAuthState(): AuthState`
- `getCurrentUserId(): String?`

---

## Étape 7 — `SyncService.kt`

Fichier : `Service/SyncService.kt`

Constantes importantes :

```kotlin
const val DB_SCHEMA_VERSION = 36   // à aligner avec la version Room courante
const val BUCKET = "vetnutri-sync"
```

Clés `PreferencesStorage` :

```
sync_device_id      // UUID généré au premier lancement
sync_device_name    // nom lisible choisi par l'utilisateur
sync_last_push_ms   // timestamp epoch du dernier push
sync_last_pull_ms   // timestamp epoch du dernier pull
```

### `push()`

1. Récupérer `userId` depuis la session Supabase courante.
2. Appeler `withContext(AppDispatchers.IO) { exportImport.exportAllEnvelope() }`.
3. Emballer dans `SyncEnvelope`.
4. Uploader `manifest.json` (léger, pour la détection de conflit) puis `latest.json` (payload).
5. Mettre à jour `sync_last_push_ms`.

### `pull(forceOverwrite: Boolean = false)`

1. Télécharger `manifest.json` → vérifier `dbSchemaVersion` et `pushedAtMs`.
2. Si `pushedAtMs <= lastPullMs` → retourner `AlreadyUpToDate`.
3. Si conflit possible (push local plus récent que dernier pull, autre appareil) → retourner `ConflictDetected`.
4. Télécharger `latest.json`, décoder la `SyncEnvelope`, appeler `importAll()`.
5. Mettre à jour `sync_last_pull_ms`.

---

## Étape 8 — ViewModels

### `AuthViewModel.kt`

```
AuthViewModel(authService: AuthService) : ViewModel
  state: StateFlow<AuthState>
  signIn(email, password)
  signUp(email, password)
  signOut()
```

- `init` : restaure la session via `authService.restoreSession()`.

### `SyncViewModel.kt`

```
SyncViewModel(syncService: SyncService) : ViewModel
  syncResult:      StateFlow<SyncResult?>
  syncConfig:      StateFlow<SyncConfig?>
  remoteManifest:  StateFlow<SyncManifest?>
  isLoading:       StateFlow<Boolean>
  loadConfig()
  saveDeviceName(name)
  push()
  pull(forceOverwrite)
  checkRemoteManifest()
  clearResult()
```

---

## Étape 9 — `SyncView.kt`

Fichier : `View/SyncView.kt`

Comportement :

- Si `AuthState.Unauthenticated` → afficher formulaire e-mail / mot de passe avec boutons
  **Connexion** et **Créer un compte**.
- Si `AuthState.Authenticated` → afficher le nom de l'appareil (éditable), les timestamps du
  dernier push/pull, les boutons **Envoyer vers le cloud** et **Recevoir depuis le cloud**.
- Gérer les `SyncResult` :
  - `ConflictDetected` → `AlertDialog` demandant confirmation d'écrasement.
  - `SchemaIncompatible` → `AlertDialog` informatif (migration requise).
  - `PushSuccess` / `PullSuccess` → message de succès.
  - `Error` → message d'erreur.

---

## Étape 10 — Câblage navigation

### `Screen.kt`

```kotlin
object Sync : Screen()
```

### `AppNavHost.kt`

```kotlin
Screen.Sync -> {
    SyncView(
        authViewModel = authViewModel,
        syncViewModel = syncViewModel,
        onBack = { nav.goBack() }
    )
}
```

Les ViewModels `authViewModel` et `syncViewModel` sont passés depuis `AppNavHost` ou
instanciés dans `App.kt` puis transmis.

### `App.kt`

```kotlin
val authViewModel = remember { AuthViewModel(appContainer.authService) }
val syncViewModel = remember { SyncViewModel(appContainer.syncService) }
```

### `AppContainer.kt`

```kotlin
data class AppContainer(
    // ... dépendances existantes ...
    val authService: AuthService,
    val syncService: SyncService
)

fun rememberAppContainer(appDatabase: AppDatabase): AppContainer {
    val supabaseClient = remember {
        createSupabaseClient(
            supabaseUrl  = AppSecrets.supabaseUrl  ?: "https://placeholder.supabase.co",
            supabaseKey  = AppSecrets.supabaseAnonKey ?: "placeholder"
        ) {
            install(Auth) { sessionManager = SyncSessionManager(preferencesStorage) }
            install(Storage)
        }
    }
    val authService = remember { AuthService(supabaseClient) }
    val syncService = remember {
        SyncService(
            supabase      = supabaseClient,
            exportImport  = exportImportRepository,   // repo existant
            prefs         = preferencesStorage
        )
    }
    // ...
}
```

---

## Étape 11 — Point d'entrée UI (Réglages)

Dans `View/SettingsSections/AdministrationSettings.kt`, ajouter un bouton :

```kotlin
Button(onClick = { nav.navigateTo(Screen.Sync) }) {
    Text("Synchronisation entre appareils")
}
```

---

## Checklist finale

- [ ] Projet Supabase créé, bucket `vetnutri-sync` en mode privé
- [ ] Policy RLS ajoutée sur le bucket
- [ ] `SUPABASE_URL` et `SUPABASE_ANON_KEY` dans `local.properties`
- [ ] `DB_SCHEMA_VERSION` dans `SyncService` aligné avec la version de migration Room courante
- [ ] Les 7 nouveaux fichiers créés et le câblage effectué
- [ ] Build Desktop : `./gradlew :composeApp:run`
- [ ] Test du flux complet : inscription → push appareil A → pull appareil B
