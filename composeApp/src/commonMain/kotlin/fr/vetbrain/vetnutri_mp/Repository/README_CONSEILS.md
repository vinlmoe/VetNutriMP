# Gestion des Conseils Personnalisés en HTML

## Vue d'ensemble

Le système de gestion des conseils personnalisés permet de créer, stocker et réutiliser des conseils vétérinaires sous forme de contenu HTML structuré. Les conseils sont persistés dans la base de données Room multiplatform et sont indépendants des consultations.

## Fonctionnalités principales

### ✅ Persistance des conseils
- Stockage en base de données Room multiplatform
- Support des contenus HTML riches avec formatage
- Métadonnées complètes (catégories, tags, priorités, etc.)

### ✅ Gestion des templates
- Création de templates réutilisables
- Génération de conseils personnalisés à partir de templates
- Support des placeholders pour la personnalisation

### ✅ Recherche et filtrage
- Recherche par mot-clé dans le titre et le contenu
- Filtrage par catégorie, espèce, groupe d'âge
- Tri par popularité, date d'utilisation, priorité

### ✅ Statistiques d'utilisation
- Comptage des utilisations
- Historique des derniers conseils utilisés
- Conseils populaires

## Structure des données

### Catégories de conseils disponibles

```kotlin
enum class SectionCategory {
    CONSEIL_NUTRITIONNEL,    // Conseils alimentaires
    CONSEIL_HYGIENE,         // Soins et hygiène
    CONSEIL_COMPORTEMENT,    // Comportement animal
    CONSEIL_SANTE,           // Santé générale
    CONSEIL_REPRODUCTION,    // Reproduction
    CONSEIL_ALIMENTATION,    // Alimentation spécifique
    CONSEIL_ACTIVITE,        // Activité physique
    CONSEIL_PREVENTION       // Prévention
}
```

### Propriétés d'un conseil

```kotlin
data class HtmlSection(
    val id: String,                          // Identifiant unique
    val title: String,                       // Titre du conseil
    val content: RichTextContent,            // Contenu HTML structuré
    val category: SectionCategory,           // Catégorie du conseil
    val tags: List<String>,                  // Tags pour la recherche
    val priority: Int,                       // Priorité d'affichage (0-10)
    val isActive: Boolean,                   // Actif/inactif
    val targetSpecies: List<String>,         // Espèces cibles
    val targetAgeGroups: List<String>,       // Groupes d'âge cibles
    val usageCount: Int,                     // Nombre d'utilisations
    val lastUsed: Instant?,                  // Dernière utilisation
    val isTemplate: Boolean,                 // Template ou conseil
    val createdAt: Instant,                  // Date de création
    val updatedAt: Instant                   // Date de modification
)
```

## Utilisation

### Initialisation

```kotlin
// Dans votre ViewModel ou Service
val database = // ... votre instance AppDatabase
val conseilRepository = ConseilRepository(database.htmlSectionDao())
```

### Créer un conseil

```kotlin
val contenu = RichTextContent(
    blocks = listOf(
        TextBlock.Heading("h1", 2, "Titre du conseil"),
        TextBlock.Paragraph("p1", "Contenu du conseil..."),
        TextBlock.ListBlock("list1", listOf("Point 1", "Point 2"))
    )
)

val conseil = HtmlSection(
    id = "conseil_001",
    title = "Mon conseil",
    content = contenu,
    category = SectionCategory.CONSEIL_NUTRITIONNEL,
    targetSpecies = listOf("chien"),
    priority = 5
)

conseilRepository.sauvegarderConseil(conseil)
```

### Rechercher des conseils

```kotlin
// Recherche par mot-clé
val resultats = conseilRepository.rechercherConseils("nutrition")

// Conseils par espèce
val conseilsChiens = conseilRepository.getConseilsParEspece("chien")

// Conseils par catégorie
val conseilsSante = conseilRepository.getConseilsByCategory(SectionCategory.CONSEIL_SANTE)

// Conseils populaires
val populaires = conseilRepository.getConseilsPopulaires(limit = 10)
```

### Gestion des templates

```kotlin
// Créer un template
val template = HtmlSection(
    id = "template_001",
    title = "Template conseil",
    content = contenu,
    isTemplate = true,
    category = SectionCategory.CONSEIL_HYGIENE
)

// Utiliser un template
val nouveauConseil = conseilRepository.creerConseilDepuisTemplate(
    templateId = "template_001",
    titre = "Conseil personnalisé",
    modifications = mapOf("{{ESPECE}}" to "chat")
)
```

### Marquer un conseil comme utilisé

```kotlin
conseilRepository.incrementerUsage("conseil_001")
```

## Migration de base de données

La migration 23→24 ajoute automatiquement les tables nécessaires :

- `HTML_SECTIONS` : Stockage des conseils et templates
- `HTML_SECTION_LIBRARIES` : Bibliothèques de conseils

Les index sont créés pour optimiser les performances de recherche.

## Avantages

1. **Indépendance** : Les conseils ne sont pas liés aux consultations
2. **Réutilisabilité** : Templates et conseils réutilisables
3. **Flexibilité** : Contenu HTML riche et structuré
4. **Performance** : Index optimisés pour les recherches
5. **Extensibilité** : Facile d'ajouter de nouvelles catégories
6. **Statistiques** : Suivi de l'utilisation des conseils

## Exemple complet

Voir `ConseilRepositoryExample.kt` pour des exemples d'utilisation détaillés.
