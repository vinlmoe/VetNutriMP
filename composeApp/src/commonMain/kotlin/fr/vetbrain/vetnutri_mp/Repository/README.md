# Dossier Repository

Ce dossier contient les repositories de l'application VetNutri_MP. Les repositories servent de couche d'abstraction entre les sources de données (base de données, API, etc.) et le reste de l'application.

## Rôle des Repositories

Les repositories sont responsables de :
- Fournir une API simple et cohérente pour accéder aux données
- Abstraire les détails d'implémentation des sources de données
- Gérer le cache et la persistance des données
- Coordonner les opérations CRUD (Create, Read, Update, Delete)
- Gérer les transformations entre les modèles de domaine et les entités de la base de données

## Repositories principaux

### AnimalRepository

Gère l'accès aux données des animaux.

**Fonctionnalités :**
- Récupérer la liste des animaux
- Récupérer un animal par son identifiant
- Ajouter, modifier et supprimer des animaux
- Gérer les consultations et l'historique du poids
- Importation et exportation des données d'animaux

### FoodRepository

Gère l'accès aux données des aliments.

**Fonctionnalités :**
- Récupérer la liste des aliments
- Récupérer un aliment par son identifiant
- Ajouter, modifier et supprimer des aliments
- Filtrer les aliments par espèce, type, etc.
- Importation et exportation des données d'aliments

### ConsultationRepository

Gère l'accès aux données des consultations.

**Fonctionnalités :**
- Récupérer les consultations d'un animal
- Ajouter, modifier et supprimer des consultations
- Gérer les rations associées aux consultations

### RationRepository

Gère l'accès aux données des rations alimentaires.

**Fonctionnalités :**
- Récupérer les rations d'une consultation
- Ajouter, modifier et supprimer des rations
- Gérer les aliments dans une ration

## Implémentations

### InMemoryAnimalRepository

Implémentation en mémoire du repository d'animaux (utilisée pour le développement et les tests).

### SqliteAnimalRepository

Implémentation utilisant SQLite pour persister les données d'animaux.

### InMemoryFoodRepository

Implémentation en mémoire du repository d'aliments.

### SqliteFoodRepository

Implémentation utilisant SQLite pour persister les données d'aliments.

## Relations avec d'autres modules

- **ViewModel** : Les ViewModels utilisent les repositories pour accéder aux données
- **DataBase** : Les repositories utilisent les DAOs pour interagir avec la base de données
- **Data** : Les repositories manipulent les modèles de données
- **Utils** : Les repositories utilisent les utilitaires pour des opérations spécifiques

## Exemple d'utilisation

```kotlin
// Dans un ViewModel
class AnimalListViewModel(private val animalRepository: AnimalRepository) {
    // Flux observable des animaux
    private val _animals = MutableStateFlow<List<AnimalEv>>(emptyList())
    val animals: StateFlow<List<AnimalEv>> = _animals.asStateFlow()

    // Charger la liste des animaux
    fun loadAnimals() {
        viewModelScope.launch {
            try {
                val animalList = animalRepository.getAllAnimals()
                _animals.value = animalList
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }

    // Ajouter un nouvel animal
    fun addAnimal(animal: AnimalEv) {
        viewModelScope.launch {
            try {
                animalRepository.insertAnimal(animal)
                loadAnimals() // Recharger la liste
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }
} 