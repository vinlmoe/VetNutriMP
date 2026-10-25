# Guide utilisateur (version desktop)

- `guide_utilisateur.tex` : source LaTeX ; `guide_utilisateur.pdf` : version compilée.
- `captures/` : captures d'écran de l'application desktop (base de démonstration, données fictives).

## Compiler

```bash
cd docs/guide_utilisateur
latexmk -pdf guide_utilisateur.tex   # puis latexmk -c pour nettoyer
```

## Refaire les captures

Les captures sont produites par le pilote de test
`composeApp/src/desktopTest/kotlin/fr/vetbrain/vetnutri_mp/Guide/GuideScreenshotDriver.kt`
(Compose UI test, rendu hors écran en 1400 × 900). Il lance la vraie application, exécute un
script de pas (`click`, `type`, `shot`, `dump`… — liste en tête du fichier) et enregistre les PNG.
Il est inactif dans les tests ordinaires : il ne tourne que si `VETNUTRI_GUIDE_SCRIPT` est défini.

Toujours pointer la base et le dossier personnel vers des dossiers jetables, pour ne pas toucher
à la base ni aux préférences réelles :

```bash
VETNUTRI_GUIDE_SCRIPT=/chemin/script.txt \
VETNUTRI_GUIDE_HOME=/tmp/guide/home \
VETNUTRI_TEST_DATABASE_DIR=/tmp/guide/db \
VETNUTRI_GUIDE_OUT=/tmp/guide/out \
./gradlew :composeApp:desktopTest --tests '*GuideScreenshotDriver*' --rerun
```

La base persiste entre deux exécutions : un premier script installe les données et accepte les
CGU, les suivants commencent par `scrollto Continuer` / `click Continuer`. `dump <nom>` écrit les
libellés visibles (et leur position) pour trouver quoi cliquer.
