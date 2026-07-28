# Dossier Components

Ce dossier contient les composants d'interface utilisateur réutilisables de l'application VetNutri_MP. Ces composants sont des éléments Compose qui peuvent être utilisés dans différentes vues pour maintenir une cohérence visuelle et fonctionnelle dans toute l'application.

## Principes de conception

Les composants suivent plusieurs principes de conception :
- **Réutilisabilité** : Conçus pour être utilisés dans plusieurs contextes
- **Paramétrisables** : Adaptables via des paramètres
- **Cohérence** : Respect des thèmes et styles de l'application
- **Accessibilité** : Prise en compte des besoins d'accessibilité
- **Performances** : Optimisés pour éviter les recompositions inutiles

## Organisation des fichiers

- **BadgeComponents.kt** : Composants liés aux badges et étiquettes
- **Cards.kt** : Cartes génériques et composants de sélection
- **FormFields.kt** : Champs de formulaire et listes déroulantes
- **NutrientComponents.kt** : Composants liés à l'affichage des nutriments
- **NutrientUtils.kt** : Fonctions utilitaires pour le calcul des valeurs nutritionnelles
- **Section.kt** : Composant de section avec titre et contenu
- **TopBar.kt** : Barre de navigation supérieure personnalisée
- **AppDatePicker.kt** : Sélecteur de date personnalisé
- **AppTextField.kt** : Champ de texte personnalisé avec validation
- **ConfirmDialog.kt** : Boîte de dialogue de confirmation
- **CodeView.kt** : Composant pour afficher du code avec coloration syntaxique

## Composants principaux

### BadgeComponents.kt

```kotlin
@Composable
fun Badge(
    text: String,
    subText: String? = null,
    id: Any? = null,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    // Implémentation...
}
```

### FormFields.kt

```kotlin
@Composable
fun TextFieldNut(value: Labelable?, label: String) {
    // Implémentation...
}

@Composable
fun ComboBox(
    items: List<Labelable>,
    init: Labelable?,
    modifier: Modifier = Modifier,
    label: String = "",
    onItemSelected: (String) -> Unit
) {
    // Implémentation...
}

@Composable
fun <T> GenericDropdown(
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    items: List<T?>,
    getDisplayText: (T?) -> String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    // Implémentation...
}
```

### Cards.kt

```kotlin
@Composable
fun <T> MultiSelectionCard(
    titre: String,
    elementsDisponibles: List<T>,
    elementsSelectionnes: MutableList<T>,
    onSelectionChange: (List<T>) -> Unit,
    getLabel: (T) -> String,
    getIdentifiant: (T) -> String,
    couleurArrierePlan: Color,
    modifier: Modifier = Modifier
) {
    // Implémentation...
}
```

### NutrientComponents.kt

```kotlin
@Composable
fun NutrientSection(
    titre: String,
    nutriments: List<Nutrient>,
    valeursNutriments: SnapshotStateMap<Nutrient, String>,
    erreursNutriments: SnapshotStateMap<Nutrient, Boolean>,
    couleurArrierePlan: Color,
    modifier: Modifier = Modifier
) {
    // Implémentation...
}

@Composable
fun AnalyseNutritionnelleCard(
    nutriments: List<Nutrient>,
    valeursTotales: Map<Nutrient, Float>,
    diviseur: Float,
    typeDiviseur: String,
    couleurFond: Color = MaterialTheme.colors.surface,
    onModeDivisionChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Implémentation...
}

@Composable
fun AnalyseNutritionnelleCompacte(
    nutriments: List<Nutrient>,
    valeursTotales: Map<Nutrient, Float>,
    diviseur: Float,
    typeDiviseur: String,
    couleurFond: Color = MaterialTheme.colors.surface,
    onModeDivisionChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Implémentation...
}
```

### NutrientUtils.kt

```kotlin
fun calculerValeursNutritionnelles(
    alimentsRation: List<AlimentRation>,
    nutriments: List<Nutrient>
): Map<Nutrient, Float> {
    // Implémentation...
}
```

### Section.kt

```kotlin
@Composable
fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Implémentation...
}
```

### TopBar.kt

```kotlin
@Composable
fun VetNutriTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // Implémentation...
}
```

## Utilisation des composants

Pour utiliser les composants dans une vue, il suffit de les importer depuis le package Components :

```kotlin
import fr.vetbrain.vetnutri_mp.Components.Badge
import fr.vetbrain.vetnutri_mp.Components.GenericDropdown
import fr.vetbrain.vetnutri_mp.Components.Section

@Composable
fun AlimentDetailView(aliment: AlimentEv) {
    Column(modifier = Modifier.padding(16.dp)) {
        Section(title = "Informations générales") {
            Text("Nom: ${aliment.nom}")
            Text("Marque: ${aliment.brand}")
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(
                    text = aliment.typeAliment.name,
                    backgroundColor = MaterialTheme.colors.primary
                )
                Badge(
                    text = aliment.groupe.name,
                    backgroundColor = MaterialTheme.colors.secondary
                )
            }
        }
    }
}
```

## Relations avec d'autres modules

- **View** : Les vues utilisent ces composants pour construire l'interface
- **Theme** : Les composants respectent les thèmes et styles définis
- **Data** : Les composants affichent et manipulent les données des modèles
- **Utils** : Les composants utilisent des utilitaires pour des fonctionnalités spécifiques 