---
name: ajouter-repository
description: Ajouter un Repository (et son DAO) à VetNutriMP — interface + implémentation Room + implémentation en mémoire pour les tests, withContext(AppDispatchers.IO), conversion Entity ↔ domaine, déclaration dans AppContainer et rememberAppContainer. À utiliser quand un ViewModel a besoin d'un nouvel accès aux données ou qu'on crée une nouvelle table.
---

# Ajouter un Repository (VetNutriMP)

Préfixe : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/`.
Modèle à copier : `Repository/MultiRationExplorationRepository.kt` (interface, implémentation Room et
implémentation en mémoire dans un seul fichier, ~70 lignes).

Si une table ou une colonne est créée, suivre **d'abord** le skill `migration-room`.

## Étapes

### 1. DAO — `DataBase/CommonDao.kt`

```kotlin
@Dao
interface MonDao {
    @Query("SELECT * FROM MA_TABLE ORDER BY updatedAt DESC") suspend fun getAll(): List<MonEntity>
    @Query("SELECT * FROM MA_TABLE WHERE uuid = :uuid") suspend fun getById(uuid: String): MonEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: MonEntity)
    @Query("DELETE FROM MA_TABLE WHERE uuid = :uuid") suspend fun deleteById(uuid: String)
}
```
Puis l'accesseur dans `DataBase/AppDatabase.kt` : `abstract fun monDao(): MonDao`.
Toutes les méthodes DAO sont `suspend`. Modèle : `MultiRationExplorationDao` en fin de `CommonDao.kt`.

### 2. Repository — `Repository/MonRepository.kt`

```kotlin
/** <Ce que contient ce repository, en français.> */
interface MonRepository {
    suspend fun getAllXxx(): List<Xxx>
    suspend fun saveXxx(xxx: Xxx)
    suspend fun deleteXxx(uuid: String)
}

class DatabaseMonRepository(private val dao: MonDao) : MonRepository {
    override suspend fun getAllXxx(): List<Xxx> =
            withContext(AppDispatchers.IO) { dao.getAll().map { it.toDomain() } }
    // …
}

/** Implémentation en mémoire (tests). */
class InMemoryMonRepository : MonRepository { … }
```

Règles :
- **Chaque** `suspend fun` publique de l'implémentation Room enveloppe son corps dans
  `withContext(AppDispatchers.IO)` (jamais `Dispatchers.IO` directement : `AppDispatchers` est
  substituable en test).
- Méthodes publiques en **anglais** (`getAllXxx`, `saveXxx`, `deleteXxx`) ; fonctions privées
  d'import/parsing en français.
- Le repository renvoie des objets domaine (`Data/`), jamais des `Entity`. Conversion : fonction privée
  `toDomain()` dans le fichier pour un objet simple, ou `DataBase/Mappers.kt` pour un objet `…Ev` agrégé
  (plusieurs tables).
- Écritures multi-tables : une méthode `@Transaction` dans le DAO (exemples dans `CommonDao.kt`) pour
  ne jamais laisser un état partiel.
- Implémentation en mémoire : même comportement observable (tri, remplacement par uuid) ; elle sert aux
  tests `commonTest` des ViewModels et moteurs.

### 3. Injection — `AppContainer.kt`

Deux endroits, sinon la compilation échoue ou la dépendance n'est pas créée :
1. le champ dans `data class AppContainer` (`val monRepository: MonRepository`, type **interface**) ;
2. dans `rememberAppContainer(appDatabase)` : `val monRepository = remember { DatabaseMonRepository(appDatabase.monDao()) }`
   puis `monRepository = monRepository` dans le `return AppContainer(...)`.

Puis dans `App.kt` : `val monRepository = appContainer.monRepository` et injection dans le ViewModel
(skill `ajouter-ecran`).

### 4. Sauvegarde et export

Les données utilisateur doivent survivre à une sauvegarde/restauration JSON : si la table contient des
données saisies par l'utilisateur, l'ajouter à `ApiEnvelope` (`Data/ApiModels.kt`, liste avec
`= emptyList()`) et à `Repository/ExportImportRepository.kt` (export **et** import). Données dérivées
ou recalculables : ne rien stocker (cas des résultats d'exploration multiration).

### 5. Tests

- Moteur / ViewModel avec l'implémentation en mémoire : `composeApp/src/commonTest/...`.
- Requêtes Room réelles : `composeApp/src/desktopTest/.../DataBase/` ou `.../Repository/`
  (base SQLite temporaire, cf. `AppDatabasePersistenceTest`).

```bash
./gradlew :composeApp:desktopTest
```

## Liste de contrôle

- [ ] DAO `suspend` + accesseur dans `AppDatabase`
- [ ] Interface + implémentation Room (`withContext(AppDispatchers.IO)` partout) + en mémoire
- [ ] Objets domaine en sortie, conversion centralisée
- [ ] `AppContainer` : champ **et** `rememberAppContainer`
- [ ] Sauvegarde JSON couverte si données utilisateur
- [ ] Tests verts
