# Dossier DataBase

Ce dossier contient les classes et interfaces liées à la gestion de la base de données de l'application VetNutri_MP. Il définit comment les données sont stockées, accédées et manipulées de manière persistante.

## Architecture de la base de données

L'application utilise SQLite comme système de gestion de base de données, avec SQLDelight comme couche d'abstraction pour une utilisation multiplateforme.

## Composants principaux

### Database.sq

Fichier principal de définition de la base de données SQLDelight qui définit les tables, les requêtes et les opérations.

### AppDatabase

Classe qui encapsule l'instance de la base de données SQLDelight et fournit des méthodes d'accès aux différents DAOs.

### DAOs (Data Access Objects)

Interfaces qui définissent les opérations de base de données pour chaque entité :

- **AnimalDao** : Opérations pour les animaux
- **ConsultationDao** : Opérations pour les consultations
- **FoodDao** : Opérations pour les aliments
- **RationDao** : Opérations pour les rations
- **WeightDao** : Opérations pour les mesures de poids

### Entités

Classes qui représentent les tables de la base de données :

- **AnimalEntity** : Représentation en base de données d'un animal
- **ConsultationEntity** : Représentation en base de données d'une consultation
- **FoodEntity** : Représentation en base de données d'un aliment
- **RationEntity** : Représentation en base de données d'une ration
- **WeightEntity** : Représentation en base de données d'une mesure de poids
- **NutrientValueEntity** : Représentation en base de données d'une valeur nutritionnelle

### Gestionnaires de migration

Classes qui gèrent les migrations de la base de données lors des mises à jour de l'application.

## Relations entre les tables

- **Animal** a plusieurs **Consultations** (one-to-many)
- **Animal** a plusieurs **Weights** (one-to-many)
- **Consultation** a plusieurs **Rations** (one-to-many)
- **Ration** a plusieurs **AlimentRation** (one-to-many)
- **AlimentRation** référence un **Aliment** (many-to-one)
- **Aliment** a plusieurs **NutrientValue** (one-to-many)

## Conversion entre entités et modèles de domaine

Des fonctions de mappage sont définies pour convertir entre les entités de base de données et les modèles de domaine utilisés dans l'application :

- `AnimalEntity.toModel()` : Convertit une entité Animal en modèle AnimalEv
- `AnimalEv.toEntity()` : Convertit un modèle AnimalEv en entité Animal
- (et ainsi de suite pour les autres entités)

## Relations avec d'autres modules

- **Repository** : Les repositories utilisent les DAOs pour accéder aux données
- **Data** : Les entités sont mappées vers et depuis les modèles de données
- **Utils** : Des utilitaires pour les opérations de base de données

## Exemple d'utilisation

```kotlin
// Initialisation de la base de données
val databaseDriver = createDatabaseDriver()
val appDatabase = AppDatabase(databaseDriver)

// Accès à un DAO
val animalDao = appDatabase.animalDao

// Utilisation du DAO
val animals = animalDao.getAllAnimals()
val animal = animalDao.getAnimalById(animalId)
animalDao.insertAnimal(animalEntity)
animalDao.updateAnimal(animalEntity)
animalDao.deleteAnimal(animalId)
```

## Transactions

La base de données supporte les transactions pour garantir l'intégrité des données lors d'opérations complexes :

```kotlin
appDatabase.transaction {
    // Insérer un animal
    val animalId = animalDao.insertAnimal(animalEntity)
    
    // Insérer une consultation pour cet animal
    val consultationEntity = ConsultationEntity(refAnimal = animalId, ...)
    val consultationId = consultationDao.insertConsultation(consultationEntity)
    
    // Insérer une ration pour cette consultation
    val rationEntity = RationEntity(refConsultation = consultationId, ...)
    rationDao.insertRation(rationEntity)
}
``` 