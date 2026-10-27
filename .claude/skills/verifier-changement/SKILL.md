---
name: verifier-changement
description: Vérifier une modification de VetNutriMP avant commit ou push — quelle commande Gradle lancer selon les fichiers touchés (commonTest, desktopTest, Android, iOS), test ciblé, schémas Room, lancement de l'application sur une base neuve, ce que la CI exigera. À utiliser avant de committer, quand on demande « lance les tests », « est-ce que ça compile », ou pour reproduire un échec de CI.
---

# Vérifier un changement (VetNutriMP)

## Ce que fait la CI (`.github/workflows/ci.yml`)

| Job | Commande | Bloquant pour |
|-----|----------|---------------|
| Tests JVM | `./gradlew :composeApp:desktopTest` (exécute `commonTest` **et** `desktopTest`) | tout |
| Schémas Room | `git status --porcelain composeApp/schemas` doit être vide après les tests | changement d'entité |
| Android | `./gradlew :composeApp:testDebugUnitTest` puis `assembleDebug` | tout |
| iOS (manuel) | `./gradlew :composeApp:compileKotlinIosSimulatorArm64` | code iOS / `expect`/`actual` |

Déclenchée sur `main`, `master`, `revetBackUp`, `claude/**` et les PR ; ignorée si seuls des `.md`
ou `docs/` changent.

## Choisir la vérification

| Fichiers modifiés | Minimum avant commit |
|-------------------|----------------------|
| `Data/`, `Utils/`, `Enumer/` (moteurs purs) | test ciblé puis `desktopTest` |
| `DataBase/`, `Repository/` | `desktopTest` + schémas (skill `migration-room` si entité) |
| `ViewModel/`, `View/`, `Navigation/` | `compileKotlinDesktop` + `desktopTest` ; lancer l'app si le comportement visible change |
| `androidMain/` | `testDebugUnitTest` (SDK Android requis) |
| `iosMain/`, `expect`/`actual` | compiler toutes les cibles disponibles ; sinon le signaler |
| JSON INIT | `python3 .claude/skills/donnees-initiales/scripts/verifier_init.py` |
| `strings_*.json` | contrôle des clés (skill `traduire-interface`) |
| Calcul nutritionnel | + parité R (skill `parite-vetnutridogr`) |

## Commandes

```bash
# Compilation seule (rapide)
./gradlew :composeApp:compileKotlinDesktop

# Un test, une classe, un paquet
./gradlew :composeApp:desktopTest --tests "*TroupeauTest*"
./gradlew :composeApp:desktopTest --tests "fr.vetbrain.vetnutri_mp.Data.*"

# Toute la suite JVM (ce que la CI lance en premier)
./gradlew :composeApp:desktopTest

# Schémas Room à jour ?
git status --porcelain composeApp/schemas

# Lancer l'application desktop sur une base temporaire (la vraie base n'est pas touchée)
./scripts/run_fresh_desktop.sh        # ou : make run-fresh
```

Rapports : `composeApp/build/reports/tests/desktopTest/index.html`, résultats XML dans
`composeApp/build/test-results/desktopTest/`. Pour lire un échec :
`grep -l "<failure" composeApp/build/test-results/desktopTest/*.xml`.

## Pièges connus

- Les tests desktop qui décodent des images (Skia, `AppLogoDecoderTest`) demandent `libgl1`,
  `libfontconfig1`, `libfreetype6` sur Linux ; leur absence n'est pas une régression du code.
- `AppSecretsTest` exige `JSONBIN_CREATE_KEY` / `JSONBIN_READ_KEY` non vides. Les clés sont injectées
  **à la compilation** (`local.properties` : `jsonbin.create.key`, sinon variables d'environnement). Pour
  les tests, une valeur factice suffit (`JSONBIN_CREATE_KEY=dev JSONBIN_READ_KEY=dev ./gradlew …`, comme
  la CI) ; ne jamais construire un paquet distribué avec ces valeurs, ni écrire de vraie clé dans un
  fichier suivi par git.
- Même `desktopTest` a besoin de télécharger le plugin Android (AGP) depuis Google Maven : l'erreur
  « Plugin [id: 'com.android.application', version: '8.7.3'] was not found » signale un accès réseau
  manquant (environnement cloud restreint), pas un défaut du code. Dans ce cas, ne pas annoncer les
  tests comme passés : le dire et laisser la CI trancher.
- Les cibles Android exigent un SDK (`ANDROID_HOME` ou `local.properties`) ; iOS exige macOS.
  Si une cible ne peut pas être construite ici, le dire dans le compte rendu au lieu de la déclarer vérifiée.
- Un test qui dépend de la langue de l'interface est fragile : comparer à `translate(clé)`.
- Ne jamais désactiver, ignorer ou affaiblir un test pour obtenir du vert.

## Avant de pousser

1. Relire le diff (`git diff`) : pas de fichier de log, de base `.db`, de `build/`, de JSON de test égaré
   à la racine (le dépôt en contient déjà beaucoup, ne pas en ajouter).
2. Vérifications ci-dessus vertes, ou limites explicitement signalées.
3. Message de commit en français, qui dit ce qui change pour l'utilisateur (style de l'historique :
   « Troupeau : état du troupeau dans la consultation, analyse du troupeau entier »).
