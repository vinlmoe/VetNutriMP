# Système de Sections HTML Réutilisables

Ce système permet de créer et gérer des sections HTML réutilisables qui peuvent être intégrées dans les exports PDF de VetNutri MP.

## Architecture

### Modèles de données

- **HtmlSection**: Représente une section HTML avec titre, contenu et métadonnées
- **RichTextContent**: Contenu de texte enrichi avec blocs (paragraphes, titres, listes, tableaux)
- **TextBlock**: Différents types de blocs de contenu (Paragraph, Heading, ListBlock, TableBlock)
- **SectionCategory**: Catégories pour organiser les sections

### Composants principaux

1. **RichTextEditor**: Éditeur de texte enrichi pour créer du contenu
2. **HtmlSectionParser**: Parseur pour convertir le contenu en HTML propre
3. **HtmlSectionRepository**: Gestionnaire de persistance des sections
4. **HtmlSectionViewModel**: ViewModel pour la logique métier
5. **HtmlSectionManagerView**: Interface utilisateur pour gérer les sections

## Utilisation

### Créer une nouvelle section

```kotlin
val section = HtmlSection(
    id = "unique_id",
    title = "Ma section",
    content = RichTextContent(
        blocks = listOf(
            TextBlock.Paragraph(
                id = "para1",
                text = "Contenu de la section",
                formatting = TextFormatting()
            )
        )
    ),
    category = SectionCategory.GENERAL
)
```

### Intégrer dans l'export PDF

```kotlin
val exportData = ExportData(
    // ... autres données
    htmlSections = listOf(section1, section2)
)

// Générer le HTML complet
val html = HtmlDocumentBuilder.buildHtml(DocumentType.RATION_ANALYSIS, exportData)
```

### Utiliser l'éditeur

```kotlin
RichTextEditor(
    initialContent = RichTextContent(),
    onContentChange = { content ->
        // Traiter les changements
    }
)
```

## Fonctionnalités

### Éditeur de texte enrichi
- ✅ Paragraphes avec formatage (gras, italique, souligné)
- ✅ Titres (H1, H2, H3)
- ✅ Listes (numérotées ou à puces)
- ✅ Tableaux
- ✅ Gestion des couleurs et tailles de police

### Gestion des sections
- ✅ Création, modification, suppression
- ✅ Organisation par catégories
- ✅ Système de tags
- ✅ Modèles réutilisables
- ✅ Recherche et filtrage
- ✅ Duplication de sections

### Intégration PDF
- ✅ Génération HTML propre
- ✅ CSS intégré pour le style
- ✅ Support multiplateforme
- ✅ Intégration transparente avec le système existant

## Structure des fichiers

```
Export/
├── HtmlSectionModels.kt      # Modèles de données
├── HtmlSectionParser.kt      # Parseur HTML
├── HtmlDocumentBuilder.kt    # Intégration avec l'export
├── ExportModels.kt           # Modèles d'export mis à jour

Components/
└── RichTextEditor.kt         # Éditeur de texte enrichi

DataBase/
├── Entity.kt                 # Entités de base de données
└── CommonDao.kt              # DAO pour les sections

Repository/
└── HtmlSectionRepository.kt  # Repository des sections

ViewModel/
└── HtmlSectionViewModel.kt   # ViewModel de gestion

View/
└── HtmlSectionManagerView.kt # Interface utilisateur

Example/
└── HtmlSectionExample.kt     # Exemples d'utilisation
```

## Exemple complet

Voir le fichier `HtmlSectionExample.kt` pour un exemple complet d'utilisation avec des données d'exemple.

## Perspectives d'évolution

- Support avancé de formatage (alignement, indentation)
- Drag & drop pour réorganiser les blocs
- Import/export de sections
- Partage de sections entre utilisateurs
- Historique des modifications
- Collaboration en temps réel
