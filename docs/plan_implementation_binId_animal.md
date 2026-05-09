# Plan d'implémentation : Ajout du champ `binId` dans la base de données Animal

## Objectif
Stocker le `binId` de jsonbin.io directement dans la base de données de l'animal pour permettre la mise à jour automatique du bin lors des partages successifs.

## Structure actuelle

### Classes concernées
1. **`AnimalEv`** (`Data/AnimalEv.kt`) : Classe métier
2. **`AnimalEntity`** (`DataBase/Entity.kt`) : Entité Room
3. **`AnimalEvJson`** (`Data/JsonStructures.kt`) : Structure JSON pour sérialisation
4. **Mappers** (`DataBase/Mappers.kt`) : Conversion Entity ↔ Ev
5. **`AppDatabase`** (`DataBase/AppDatabase.kt`) : Version actuelle = 24

---

## Plan d'implémentation

### Étape 1 : Mise à jour de la classe métier `AnimalEv`

**Fichier** : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Data/AnimalEv.kt`

**Action** :
- Ajouter le champ `var jsonbinId: String? = null` dans la classe `AnimalEv`

**Code** :
```kotlin
data class AnimalEv(
        var uuid: String = genUUID(),
        var nom: String = "",
        var dead: Boolean = false,
        var id: String? = null,
        var sexId: Int = Sex.MALE_ENTIER.id,
        var specieId: String = Espece.CHIEN.label,
        var ownerName: String = "",
        var birthdate: LocalDate? = null,
        var race: String = "",
        var summary: String = "",
        var jsonbinId: String? = null, // ⬅️ NOUVEAU : ID du bin jsonbin.io pour le partage en ligne
        var consultations: MutableList<ConsultationEv> = mutableListOf(),
        var weightHistory: MutableList<WeightDate> = mutableListOf()
)
```

---

### Étape 2 : Mise à jour de l'entité Room `AnimalEntity`

**Fichier** : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/DataBase/Entity.kt`

**Action** :
- Ajouter le champ `val jsonbinId: String?` dans `AnimalEntity`

**Code** :
```kotlin
@Entity(tableName = "ANIMALS")
data class AnimalEntity(
        @PrimaryKey val uuid: String,
        val nom: String?,
        val dead: Boolean = false,
        val id: String?,
        val sexId: Int = 0,
        val specieId: String?,
        val ownerName: String?,
        val birthdate: String?,
        val race: String?,
        val summary: String?,
        val jsonbinId: String? = null // ⬅️ NOUVEAU : ID du bin jsonbin.io
)
```

---

### Étape 3 : Mise à jour de la structure JSON `AnimalEvJson`

**Fichier** : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Data/JsonStructures.kt`

**Action** :
- Ajouter le champ `val jsonbinId: String? = null` dans `AnimalEvJson`

**Code** :
```kotlin
@Serializable
data class AnimalEvJson(
        val UUID: String,
        val version: String = "22.1",
        val nom: String = "",
        val dead: Boolean = false,
        val id: String? = null,
        val sex: Int = 0,
        val espece: String = "1",
        val nomProprio: String = "",
        @Serializable(with = LocalDateSerializer::class)
        val dateNaiss: LocalDate = LocalDate(2023, 1, 1),
        val race: String = "",
        val resume: String = "",
        val jsonbinId: String? = null, // ⬅️ NOUVEAU : ID du bin jsonbin.io
        val listWeight: List<WeightDateJson> = listOf(),
        val list: ListConsultEvJson? = null,
        val consultations: List<ConsultationEvJson>? = null
)
```

---

### Étape 4 : Mise à jour des mappers

**Fichier** : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/DataBase/Mappers.kt`

**Action 4.1** : Mettre à jour `AnimalEv.toEntity()`
- Ajouter `jsonbinId = this.jsonbinId` dans la construction de `AnimalEntity`

**Code** :
```kotlin
fun AnimalEv.toEntity(includeRelations: Boolean = true): AnimalEntity {
        val entity = AnimalEntity(
                uuid = this.uuid,
                nom = this.nom,
                dead = this.dead,
                id = this.id ?: "",
                sexId = this.sexId,
                specieId = this.specieId,
                ownerName = this.ownerName,
                birthdate = this.birthdate?.toString() ?: "",
                race = this.race,
                summary = this.summary,
                jsonbinId = this.jsonbinId // ⬅️ NOUVEAU
        )
        // ... reste du code
}
```

**Action 4.2** : Mettre à jour `AnimalEntity.toData()`
- Ajouter `jsonbinId = this.jsonbinId` dans la construction de `AnimalEv`

**Code** :
```kotlin
fun AnimalEntity.toData(
        consultations: List<ConsultationEntity> = emptyList(),
        weights: List<WeightEntity> = emptyList()
): AnimalEv {
        return AnimalEv(
                uuid = this.uuid,
                nom = this.nom ?: "",
                dead = this.dead ?: false,
                id = this.id?.takeIf { it.isNotEmpty() },
                sexId = this.sexId ?: 0,
                specieId = this.specieId ?: "",
                ownerName = this.ownerName ?: "",
                birthdate = this.birthdate?.takeIf { it.isNotEmpty() }?.let { /* ... */ },
                race = this.race ?: "",
                summary = this.summary ?: "",
                jsonbinId = this.jsonbinId, // ⬅️ NOUVEAU
                consultations = consultations.map { it.toData() }.toMutableList(),
                weightHistory = weights.map { it.toData() }.toMutableList()
        )
}
```

---

### Étape 5 : Création de la migration de base de données

**Fichier** : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/DataBase/AppDatabase.kt`

**Action 5.1** : Incrémenter la version de la base de données
- Changer `version = 24` → `version = 25`

**Action 5.2** : Créer la fonction de migration `createMigration24to25()`

**Code** :
```kotlin
/** Migration 24 → 25 : Ajout du champ jsonbinId à la table ANIMALS */
fun createMigration24to25(): Migration {
    return object : Migration(24, 25) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
            try {
                // Ajouter la colonne jsonbinId (nullable TEXT)
                connection.prepare(
                    "ALTER TABLE ANIMALS ADD COLUMN jsonbinId TEXT"
                ).use { statement -> 
                    statement.step() 
                }
                println("✅ Migration 24→25 : Colonne jsonbinId ajoutée à la table ANIMALS")
            } catch (e: Exception) {
                println("❌ Erreur lors de la migration 24→25: ${e.message}")
                throw e
            }
        }
    }
}
```

**Action 5.3** : Ajouter la migration dans `getRoomDatabase()`
- Ajouter `.addMigrations(createMigration24to25())` dans la liste des migrations

**Code** :
```kotlin
.addMigrations(
        // ... migrations existantes
        createMigration23to24(),
        // Migration 24→25 : Ajout du champ jsonbinId pour jsonbin.io
        createMigration24to25() // ⬅️ NOUVEAU
)
```

---

### Étape 6 : Mise à jour des mappers JSON

**Fichier** : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Data/JsonMappers.kt` (ou fichier équivalent)

**Action** : Vérifier et mettre à jour les fonctions de conversion JSON ↔ AnimalEv
- S'assurer que `jsonbinId` est inclus dans les conversions JSON

**Rechercher** :
- Fonctions qui convertissent `AnimalEv` ↔ `AnimalEvJson`
- Ajouter `jsonbinId` dans ces conversions

---

### Étape 7 : Mise à jour du code de partage

**Fichier** : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/View/AnimalDetailView.kt`

**Action 7.1** : Modifier `partagerAnimalEnLigne()` pour utiliser `animal.jsonbinId`

**Remplacement** :
```kotlin
// AVANT (lignes 289-294)
val preferencesStorage = createPreferencesStorage()
val binIdKey = "jsonbin_animal_${animal.uuid}"
val existingBinId = withContext(AppDispatchers.IO) {
        preferencesStorage.getString(binIdKey, "")
}

// APRÈS
val existingBinId = animal.jsonbinId
```

**Action 7.2** : Sauvegarder le `binId` dans l'animal après un upload réussi

**Remplacement** :
```kotlin
// AVANT (lignes 315-319)
withContext(AppDispatchers.IO) {
        preferencesStorage.saveString(binIdKey, shareLink.binId)
        println("✅ [AnimalDetailView] BinId sauvegardé pour ${animal.uuid}: ${shareLink.binId}")
}

// APRÈS
withContext(AppDispatchers.IO) {
        // Mettre à jour l'animal avec le nouveau binId
        val updatedAnimal = animal.copy(jsonbinId = shareLink.binId)
        settingsViewModel.animalRepository.updateAnimal(updatedAnimal)
        println("✅ [AnimalDetailView] BinId sauvegardé dans la BDD pour ${animal.uuid}: ${shareLink.binId}")
}
```

---

### Étape 8 : Vérification des autres usages de `AnimalEv`

**Fichiers à vérifier** :
- `Repository/AnimalRepository.kt` : Vérifier que les méthodes CRUD gèrent le nouveau champ
- `ViewModel/AnimalDetailViewModel.kt` : Vérifier la sauvegarde
- `ViewModel/AnimalListViewModel.kt` : Vérifier la création/modification
- Tous les endroits où `AnimalEv` est créé/modifié

**Action** : S'assurer que `jsonbinId` est préservé lors des opérations CRUD

---

### Étape 9 : Nettoyage du code de préférences (optionnel)

**Fichier** : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/View/AnimalDetailView.kt`

**Action** : Supprimer le code qui utilisait `PreferencesStorage` pour stocker le `binId`
- Supprimer les lignes 290-294 (création et lecture depuis préférences)
- Supprimer les lignes 316-319 (sauvegarde dans préférences)

---

## Ordre d'implémentation recommandé

1. ✅ **Étape 1** : Ajouter `jsonbinId` dans `AnimalEv`
2. ✅ **Étape 2** : Ajouter `jsonbinId` dans `AnimalEntity`
3. ✅ **Étape 3** : Ajouter `jsonbinId` dans `AnimalEvJson`
4. ✅ **Étape 4** : Mettre à jour les mappers Entity ↔ Ev
5. ✅ **Étape 5** : Créer la migration 24→25
6. ✅ **Étape 6** : Mettre à jour les mappers JSON (si nécessaire)
7. ✅ **Étape 7** : Modifier le code de partage pour utiliser `animal.jsonbinId`
8. ✅ **Étape 8** : Vérifier les autres usages
9. ✅ **Étape 9** : Nettoyer le code de préférences

---

## Tests à effectuer

1. **Test de migration** :
   - Vérifier que la migration 24→25 s'exécute sans erreur
   - Vérifier que les animaux existants conservent leurs données

2. **Test de création** :
   - Créer un nouvel animal
   - Partager l'animal → vérifier que `jsonbinId` est sauvegardé
   - Relire l'animal depuis la BDD → vérifier que `jsonbinId` est présent

3. **Test de mise à jour** :
   - Partager un animal (création du bin)
   - Modifier l'animal
   - Repartager l'animal → vérifier que le bin est mis à jour (pas créé)

4. **Test d'export/import** :
   - Exporter un animal avec `jsonbinId`
   - Vérifier que `jsonbinId` est dans le JSON exporté
   - Importer le JSON → vérifier que `jsonbinId` est restauré

---

## Risques et considérations

1. **Migration de données** : Les animaux existants n'auront pas de `jsonbinId` (c'est normal, `null` par défaut)

2. **Compatibilité JSON** : S'assurer que `jsonbinId` est optionnel dans `AnimalEvJson` pour la compatibilité avec les anciens exports

3. **Suppression d'animal** : Le `binId` sera supprimé avec l'animal (comportement souhaité)

4. **Partage multiple** : Si plusieurs utilisateurs partagent le même animal exporté, chaque instance aura son propre `binId` (comportement attendu)

---

## Avantages de cette approche

✅ **Cohérence** : Le `binId` est lié directement à l'animal dans la BDD  
✅ **Persistance** : Le `binId` est sauvegardé avec l'animal  
✅ **Export/Import** : Le `binId` sera inclus dans les exports JSON  
✅ **Simplicité** : Plus besoin de gérer un système de préférences séparé  
✅ **Intégrité** : Le `binId` est supprimé automatiquement si l'animal est supprimé

---

## Notes supplémentaires

- Le champ `jsonbinId` est **nullable** pour permettre la compatibilité avec les animaux existants
- La migration utilise `ALTER TABLE ADD COLUMN` qui est sûre en SQLite
- Aucun impact sur les requêtes existantes car le champ est nullable

















