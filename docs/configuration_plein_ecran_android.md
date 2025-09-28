# Configuration Plein Écran Android

## Vue d'ensemble

Cette documentation décrit la configuration du mode plein écran pour l'application Android VetNutriMP, qui masque les barres de statut et de navigation pour offrir une expérience immersive.

## Implémentation

### FullscreenManager

Le `FullscreenManager` est un objet singleton qui gère la configuration plein écran de l'application :

```kotlin
object FullscreenManager {
    fun enableFullscreen(activity: Activity)
    fun showSystemBars(activity: Activity)
    fun hideSystemBars(activity: Activity)
}
```

### Configuration dans MainActivity

La configuration plein écran est automatiquement activée lors de la création de l'activité principale :

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Configuration plein écran
    FullscreenManager.enableFullscreen(this)
    
    // ... reste de l'initialisation
}
```

## Fonctionnalités

### Masquage des barres système

- **Barres de statut** : Masquées pour libérer l'espace en haut de l'écran
- **Barres de navigation** : Masquées pour libérer l'espace en bas de l'écran
- **Comportement au swipe** : Les barres apparaissent temporairement lors d'un swipe depuis les bords

### Méthodes disponibles

1. **`enableFullscreen(activity: Activity)`**
   - Configure l'application en mode plein écran
   - Masque les barres de statut et de navigation
   - Configure le comportement au swipe

2. **`showSystemBars(activity: Activity)`**
   - Affiche temporairement les barres système
   - Utile pour les interactions nécessitant l'accès aux barres

3. **`hideSystemBars(activity: Activity)`**
   - Masque les barres système
   - Utile pour revenir au mode plein écran

## Dépendances

La configuration utilise les dépendances suivantes :

- `androidx.core:core-ktx` : Pour `WindowCompat` et `WindowInsetsControllerCompat`
- `androidx.activity:activity-compose` : Pour l'intégration avec Compose

## Tests

Des tests unitaires sont disponibles dans `FullscreenManagerTest.kt` pour vérifier le bon fonctionnement des méthodes.

## Notes importantes

- La configuration est appliquée automatiquement au démarrage de l'application
- Les barres système peuvent être affichées temporairement par un swipe depuis les bords
- Cette configuration améliore l'expérience utilisateur en maximisant l'espace d'affichage
