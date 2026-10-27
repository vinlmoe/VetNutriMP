---
name: migration-room
description: Faire évoluer le schéma Room de VetNutriMP sans perte de données — ajouter une table, une colonne ou un index, écrire la migration N→N+1, propager le champ jusqu'aux sauvegardes JSON et tester la chaîne de migrations. À utiliser dès qu'une Entity de DataBase/Entity.kt change, qu'on parle de migration, de version de schéma, de DATABASE_SCHEMA_VERSION ou de composeApp/schemas.
---

# Migration Room (VetNutriMP)

La base des utilisateurs contient leurs animaux, consultations et rations : aucune perte n'est
acceptable. `fallbackToDestructiveMigration` a été volontairement supprimé ; une migration
manquante ou fausse met la base de l'utilisateur de côté (`DatabaseGuard.kt`) et l'application
redémarre sur une base vide. Chaque changement d'entité passe donc par toutes les étapes ci-dessous.

Chemins (préfixe `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/`) :

| Fichier | Rôle |
|---------|------|
| `DataBase/AppDatabase.kt` | `DATABASE_SCHEMA_VERSION`, liste `entities`, `.addMigrations(...)`, fonctions `createMigrationXtoY()` |
| `DataBase/Entity.kt` | Entités Room |
| `DataBase/CommonDao.kt` | DAO |
| `DataBase/Mappers.kt` | Entity ↔ objets domaine (`AnimalEv`, `ConsultationEv`…) |
| `Data/ApiModels.kt` | `ApiEnvelope` / `AnimalApi`… : export JSON **et** sauvegardes automatiques |
| `composeApp/schemas/fr.vetbrain.vetnutri_mp.DataBase.AppDatabase/N.json` | Schéma exporté, versionné, vérifié par la CI |
| `composeApp/src/desktopTest/.../DataBase/MigrationChainDataPersistenceTest.kt` | Base v17 réelle migrée jusqu'à la version courante |

## Étapes

Lire d'abord la valeur actuelle : `grep -n "DATABASE_SCHEMA_VERSION =" composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/DataBase/AppDatabase.kt`.
Dans la suite, `N` = version actuelle, `N+1` = nouvelle version.

### 1. Modifier l'entité

- Nouvelle colonne : **toujours nullable ou avec valeur par défaut** (`val x: String? = null`),
  pour que les lignes existantes restent valides et que les appels de constructeur existants compilent.
- Données structurées (listes, paramètres) : une colonne `TEXT` contenant du JSON, nommée
  `xxxJson` en anglais (précédents : `herdTypesJson`, `herdParamsJson`, `configurationJson`),
  avec un objet de sérialisation dédié dans `Data/` (ex. `TroupeauJson.typesVersJson` / `typesDepuisJson`).
  `null` doit garder un sens métier clair (ex. `herdTypesJson == null` ⇒ individu, pas troupeau).
- Nouvelle table : créer l'`@Entity` **et** l'ajouter à la liste `entities` de `@Database`, puis
  son DAO et l'accesseur abstrait dans `AppDatabase`.

### 2. Écrire la migration

Ajouter la fonction à la fin de `AppDatabase.kt`, avec un KDoc d'une ou deux lignes en français
qui dit **pourquoi** (le besoin métier), pas seulement quoi :

```kotlin
/**
 * Migration N→N+1 : <besoin métier en une phrase>.
 */
fun createMigrationNtoN1(): Migration {
    return object : Migration(N, N + 1) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
            // Colonne : idempotent, toléré si déjà présente
            runStatementIgnoreIfExists(connection, "ALTER TABLE ANIMALS ADD COLUMN monChampJson TEXT")
            // Table : IF NOT EXISTS, SQL identique au schéma exporté
            connection.prepare("""
                CREATE TABLE IF NOT EXISTS `MA_TABLE` (
                    `uuid` TEXT NOT NULL,
                    `nom` TEXT NOT NULL,
                    PRIMARY KEY(`uuid`)
                )
            """.trimIndent()).use { it.step() }
        }
    }
}
```

Règles :
- Le SQL doit correspondre **exactement** au schéma que Room génère (types, `NOT NULL`, `DEFAULT`,
  clés, index). Après génération du schéma (étape 6), comparer avec `createSql` dans `N+1.json` ;
  un écart provoque `IllegalStateException: Migration didn't properly handle` au démarrage.
- Une colonne non nulle exige `NOT NULL DEFAULT <valeur>` dans l'`ALTER TABLE` **et**
  `@ColumnInfo(defaultValue = "<valeur>")` dans l'entité.
- Changer une contrainte (clé étrangère, type, `NOT NULL` sur colonne existante) : SQLite ne sait pas
  le faire par `ALTER` ; recréer la table (`ALTER TABLE x RENAME TO x_OLD` → `CREATE TABLE x (…)` →
  `INSERT INTO x SELECT … FROM x_OLD` → `DROP TABLE x_OLD` → recréer les index). Voir `createMigration32to33()`.
- Remplissage de données dérivées (backfill) : dans la même migration, en SQL pur, sans passer par
  les DAO ni le code Kotlin métier (il évoluera, la migration ne doit pas).
- Ne jamais modifier une migration déjà publiée : en ajouter une nouvelle.

### 3. Enregistrer la migration et monter la version

Dans `AppDatabase.kt` :
- `const val DATABASE_SCHEMA_VERSION = N + 1`
- en fin de `.addMigrations(...)`, ajouter une ligne de commentaire puis l'appel, comme les précédents :
  ```kotlin
  // Migration N→N+1 : <résumé>
  createMigrationNtoN1()
  ```
  (penser à la virgule après l'appel précédent).

### 4. Propager le champ

Un champ ajouté seulement à l'entité est perdu à la première sauvegarde/restauration JSON.
- `DataBase/Mappers.kt` : `toEntity()` **et** `toModel()` (ou équivalents) de l'objet domaine.
- Objet domaine (`AnimalEv`, `ConsultationEv`…) : propriété avec valeur par défaut.
- `Data/ApiModels.kt` : champ de l'`XxxApi` **avec valeur par défaut** (`= null`) — les anciennes
  sauvegardes JSON doivent rester lisibles —, et les deux fonctions de conversion Api ↔ domaine
  (précédent : `herdTypes` dans `AnimalApi`).
- Nouvelle table qui doit survivre à une restauration : l'ajouter à `ApiEnvelope` (liste avec
  `= emptyList()`), à `ExportImportRepository.exportAllEnvelope()` et à l'import correspondant.

Vérifier avec `grep -rn "<ancienChampVoisin>" composeApp/src/commonMain` qu'aucun point de copie
champ par champ (ex. `copy(...)` explicite, duplication d'animal/consultation) n'oublie le nouveau champ.

### 5. Tester la chaîne

Ajouter un test dans `MigrationChainDataPersistenceTest.kt`, sur le modèle de
`getRoomDatabase_migratingFromV17_storesHerdTypesAndParameters` :
1. `seedV17Database()` ;
2. ouvrir via `getRoomDatabase(Room.databaseBuilder<AppDatabase>(name = dbPath), dbPath)` ;
3. vérifier que les données existantes sont intactes et que le nouveau champ vaut sa valeur par défaut ;
4. écrire puis relire une valeur non triviale dans le nouveau champ / la nouvelle table ;
5. `assertTrue(db.checkIntegrity())` puis `db.close()`.

Commentaire de tête du test : `// La migration N->N+1 <ce qu'elle apporte>`.
Si un objet JSON est introduit, ajouter aussi un test aller-retour du format dans `commonTest`.

### 6. Générer le schéma et valider

```bash
./gradlew :composeApp:desktopTest --tests "*MigrationChainDataPersistenceTest*"
./gradlew :composeApp:desktopTest
git status --porcelain composeApp/schemas
```

- Le build génère `composeApp/schemas/.../N+1.json` : il **doit** être commité (`git add composeApp/schemas/`).
  La CI échoue si le dossier diverge des entités.
- Ne jamais éditer à la main un `.json` de schéma ni modifier un schéma de version déjà publiée.
- Comparer le `createSql` de la table touchée dans `N+1.json` avec le SQL de la migration (étape 2).

### 7. Documenter

- Si la fonctionnalité a sa section dans `CLAUDE.md` (Points d'attention), y citer la colonne/table
  et sa signification (précédent : « colonne `ANIMALS.herdTypesJson`, null = individu »).
- Mettre à jour la ligne « Room/SQLite ← … migrations, schéma vN » de `CLAUDE.md`.

## Liste de contrôle finale

- [ ] Champ nullable / avec défaut dans l'entité, l'objet domaine et l'objet Api
- [ ] `createMigrationNtoN1()` avec KDoc, idempotente, SQL identique au schéma exporté
- [ ] `DATABASE_SCHEMA_VERSION` incrémenté, migration ajoutée à `.addMigrations`
- [ ] Mappers Entity ↔ domaine et Api ↔ domaine mis à jour
- [ ] Test ajouté dans `MigrationChainDataPersistenceTest`, `desktopTest` vert
- [ ] `composeApp/schemas/.../N+1.json` généré et commité
- [ ] `CLAUDE.md` mis à jour
