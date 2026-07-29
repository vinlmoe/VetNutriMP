# Dossier Data

Ce dossier contient les modèles de données utilisés dans l'application VetNutri_MP. Ces classes représentent les entités métier et encapsulent les données manipulées au sein de l'application.

## Modèles principaux

### AnimalEv

Représentation d'un animal dans l'application.

```kotlin
data class AnimalEv(
    val id: String? = null,
    val nom: String,
    val espece: EspeceAnimal,
    val races: MutableList<String>,
    val dateNaissance: Long,
    val sexe: SexeAnimal,
    val poids: MutableList<WeightEv>,
    val neutre: Boolean,
    val stade: StadePhysiologique,
    val bcs: Float,
    val mcs: Float,
    val activite: NiveauActivite,
    val pathologies: MutableList<String>,
    val traitements: MutableList<String>,
    val allergies: MutableList<String>,
    val consultations: MutableList<ConsultationEv>,
    val preferences: MutableList<String>,
    val avatar: String? = null
)
```

### AlimentEv

Représentation d'un aliment dans l'application.

```kotlin
data class AlimentEv(
    val id: String? = null,
    val nom: String,
    val brand: String,
    val typeAliment: TypeAliment,
    val especeCompatibles: List<EspeceAnimal>,
    val stades: List<StadePhysiologique>,
    val indications: List<String>,
    val ingredients: String,
    val groupe: GroupeAlimentaire,
    val valeurs: Map<String, Float>,
    val calMet: Float = 0f,
    val calAl: Float = 0f
)
```

### ConsultationEv

Représentation d'une consultation vétérinaire.

```kotlin
data class ConsultationEv(
    val id: String? = null,
    val date: Long,
    val poids: Float,
    val bcs: Float,
    val mcs: Float,
    val stade: StadePhysiologique,
    val activite: NiveauActivite,
    val pathologies: MutableList<String>,
    val traitements: MutableList<String>,
    val allergies: MutableList<String>,
    val commentaires: String,
    val rations: MutableList<RationEv>
)
```

### RationEv

Représentation d'une ration alimentaire.

```kotlin
data class RationEv(
    val id: String? = null,
    val titre: String,
    val besoinEnergetique: Float,
    val mode: ModeRation,
    val aliments: MutableList<AlimentRationEv>,
    val commentaires: String
)
```

### AlimentRationEv

Représentation d'un aliment dans une ration avec sa quantité.

```kotlin
data class AlimentRationEv(
    val id: String? = null,
    val aliment: AlimentEv,
    val quantite: Float
)
```

### WeightEv

Représentation d'un relevé de poids pour un animal.

```kotlin
data class WeightEv(
    val id: String? = null,
    val date: Long,
    val poids: Float
)
```

### NutrientValueEv

Représentation d'une valeur nutritionnelle spécifique.

```kotlin
data class NutrientValueEv(
    val name: String,
    val value: Float
)
```

## Relations entre modèles

Les modèles sont organisés dans une structure hiérarchique:

- Un `AnimalEv` contient une liste de `ConsultationEv`
- Un `AnimalEv` contient une liste de `WeightEv`
- Une `ConsultationEv` contient une liste de `RationEv`
- Une `RationEv` contient une liste de `AlimentRationEv`
- Un `AlimentRationEv` référence un `AlimentEv`
- Un `AlimentEv` contient une map de valeurs nutritionnelles (`String` -> `Float`)

## Validations et règles métier

Ces modèles de données incluent des validations internes pour garantir l'intégrité des données:

- Vérifications des plages (ex: BCS entre 1 et 9)
- Validations de cohérence (ex: date de naissance antérieure à aujourd'hui)
- Gestion des associations (ex: certains aliments uniquement compatibles avec certaines espèces)

## Calculs et fonctionnalités

Les modèles incluent des méthodes pour effectuer des calculs métier:

- Calcul du besoin énergétique d'un animal
- Calcul de l'apport nutritionnel d'une ration
- Évaluation de l'adéquation d'une ration aux besoins de l'animal

## Sérialisation et désérialisation

Des fonctions d'extension sont définies dans `JsonMappers.kt` pour convertir les modèles en JSON et vice-versa, permettant:

- La persistance en base de données
- Le partage des données via des API
- L'import/export de données

## Relations avec d'autres modules

- **Repository**: Les repositories manipulent ces modèles de données
- **ViewModel**: Les ViewModels exposent ces modèles aux vues
- **View**: Les vues affichent ces modèles
- **DataBase**: Les entités de base de données sont mappées vers ces modèles
- **Utils**: Des utilitaires pour manipuler ces modèles

## Exemple d'utilisation

```kotlin
// Création d'un animal
val animal = AnimalEv(
    nom = "Rex",
    espece = EspeceAnimal.CHIEN,
    races = mutableListOf("Labrador"),
    dateNaissance = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L),
    sexe = SexeAnimal.MALE,
    poids = mutableListOf(),
    neutre = true,
    stade = StadePhysiologique.ADULTE,
    bcs = 5f,
    mcs = 3f,
    activite = NiveauActivite.NORMAL,
    pathologies = mutableListOf(),
    traitements = mutableListOf(),
    allergies = mutableListOf(),
    consultations = mutableListOf(),
    preferences = mutableListOf()
)

// Ajout d'une mesure de poids
animal.poids.add(WeightEv(
    date = System.currentTimeMillis(),
    poids = 25f
))

// Calcul du besoin énergétique
val besoinEnergetique = animal.calculerBesoinEnergetique()
``` 