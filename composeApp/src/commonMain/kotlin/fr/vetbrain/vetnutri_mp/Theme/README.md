# Dossier Theme

Ce dossier contient les éléments de style et de thème de l'application VetNutri_MP. Ces fichiers définissent l'apparence visuelle de l'application, garantissant une cohérence esthétique sur toutes les plateformes.

## Principes de conception

Le système de thème de VetNutri_MP est conçu selon les principes suivants :
- **Cohérence** : Garantir une expérience visuelle uniforme dans toute l'application
- **Accessibilité** : Assurer une lisibilité optimale pour tous les utilisateurs
- **Adaptabilité** : S'adapter aux différents modes (clair/sombre) et plateformes
- **Extensibilité** : Permettre des modifications faciles sans impacter le code métier

## Fichiers principaux

### Colors.kt

Définit les palettes de couleurs utilisées dans l'application.

```kotlin
object VetNutriColors {
    val Primary = Color(0xFF3F51B5)
    val PrimaryVariant = Color(0xFF303F9F)
    val Secondary = Color(0xFFFF9800)
    val SecondaryVariant = Color(0xFFF57C00)
    val Background = Color(0xFFFAFAFA)
    val Surface = Color.White
    val Error = Color(0xFFB00020)
    val OnPrimary = Color.White
    val OnSecondary = Color.Black
    val OnBackground = Color.Black
    val OnSurface = Color.Black
    val OnError = Color.White
    
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFEB3B)
    val Info = Color(0xFF2196F3)
}
```

### Typography.kt

Définit les styles de texte utilisés dans l'application.

```kotlin
val VetNutriTypography = Typography(
    h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 96.sp,
        letterSpacing = (-1.5).sp
    ),
    h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 60.sp,
        letterSpacing = (-0.5).sp
    ),
    // Autres styles de texte...
)
```

### AppSizes.kt

Définit les dimensions et espacements standard utilisés dans l'application.

```kotlin
object AppSizes {
    val paddingSmall = 4.dp
    val paddingMedium = 8.dp
    val paddingLarge = 16.dp
    val paddingXLarge = 24.dp
    
    val elevationSmall = 2.dp
    val elevationMedium = 4.dp
    val elevationLarge = 8.dp
    
    val cornerRadiusSmall = 4.dp
    val cornerRadiusMedium = 8.dp
    val cornerRadiusLarge = 16.dp
    
    val iconSizeSmall = 16.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 32.dp
}
```

### AppIcons.kt

Définit les icônes personnalisées utilisées dans l'application.

```kotlin
object AppIcons {
    val Dog = Icons.Default.Pets
    val Cat = Icons.Default.Pets // Remplacer par icônes spécifiques
    val Horse = Icons.Default.Pets // Remplacer par icônes spécifiques
    
    val Dry = Icons.Default.Grain
    val Wet = Icons.Default.Opacity
    val Treats = Icons.Default.Star
    
    val Add = Icons.Default.Add
    val Delete = Icons.Default.Delete
    val Edit = Icons.Default.Edit
    val Search = Icons.Default.Search
}
```

### VetNutriTheme.kt

Définit le thème principal de l'application, intégrant les couleurs, typographies et autres styles.

```kotlin
@Composable
fun VetNutriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColors(
            primary = VetNutriColors.Primary,
            primaryVariant = VetNutriColors.PrimaryVariant,
            secondary = VetNutriColors.Secondary,
            // Autres couleurs...
        )
    } else {
        lightColors(
            primary = VetNutriColors.Primary,
            primaryVariant = VetNutriColors.PrimaryVariant,
            secondary = VetNutriColors.Secondary,
            // Autres couleurs...
        )
    }

    MaterialTheme(
        colors = colors,
        typography = VetNutriTypography,
        shapes = Shapes(
            small = RoundedCornerShape(AppSizes.cornerRadiusSmall),
            medium = RoundedCornerShape(AppSizes.cornerRadiusMedium),
            large = RoundedCornerShape(AppSizes.cornerRadiusLarge)
        ),
        content = content
    )
}
```

## Utilisation

Le thème est appliqué au niveau le plus élevé de l'application :

```kotlin
@Composable
fun App() {
    VetNutriTheme {
        // Contenu de l'application
    }
}
```

Les composants peuvent ensuite utiliser les éléments du thème :

```kotlin
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary
        ),
        modifier = Modifier.padding(AppSizes.paddingMedium)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.button
        )
    }
}
```

## Relations avec d'autres modules

- **View** : Les vues utilisent le thème pour leur apparence
- **Components** : Les composants réutilisables appliquent le thème pour maintenir la cohérence
- **Localization** : Certains éléments visuels peuvent varier selon la langue ou la région 