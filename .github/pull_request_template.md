## Objectif

<!-- Que fait cette PR et pourquoi. Lier l'issue le cas échéant. -->

## Changements

<!-- Liste des modifications notables, par couche (View / ViewModel / Repository / DataBase). -->

-

## Vérifications

- [ ] `./gradlew :composeApp:desktopTest` passe en local
- [ ] Les opérations de base tournent dans `withContext(AppDispatchers.IO)`
- [ ] Aucun Composable n'accède directement à un Repository
- [ ] Si une entité Room a changé : migration ajoutée **et** schéma régénéré dans `composeApp/schemas/`
- [ ] Si un écran a été ajouté : `Screen.kt`, `AppNavHost.kt` (et `AppNavModels`/`App.kt` si ViewModel)

## Plateformes testées

- [ ] Desktop
- [ ] Android
- [ ] iOS <!-- poser le label `ci:ios` pour déclencher la compilation iOS en CI -->
