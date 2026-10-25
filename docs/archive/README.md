# Documents archivés

Documents conservés pour l'historique mais **qui ne décrivent plus l'état du code**.
L'analyse qualité du 25 octobre 2026 (`docs/qualite/`) a constaté que plusieurs
optimisations annoncées ici (`OptimizedNutrientResolver`, `OptimizedResourceReader`,
`CacheFactory`, scripts de test mémoire, maps O(1) dans `UnitEnum`, options JVM `-Xmx16g`)
n'ont jamais été implémentées, ou l'ont été sous une autre forme. Ne pas s'y fier
pour comprendre le projet actuel : voir `CLAUDE.md`, `CONTRIBUTING.md` et `docs/qualite/`.

| Fichier | Statut |
|---|---|
| `ANALYSE_OPTIMISATION_MEMOIRE.md` | Plan jamais exécuté (contient du SQL PostgreSQL inapplicable à SQLite) |
| `ANALYSE_OPTIMISATION_STATIQUE.md` | Partiellement appliqué |
| `OPTIMISATION_MEMOIRE.md` | Valeurs JVM différentes de `gradle.properties` |
| `PERFORMANCE_IMPROVEMENTS.md` | Partiellement appliqué (clés de listes, println) |
| `RESUME_OPTIMISATIONS_PRIORITAIRES.md` | Fichiers annoncés inexistants |
| `README_MODIFICATIONS_UNITENUM.md` | Note de modification ponctuelle |
| `CHANGELOG_PREFERENCES.md` | Fusionné dans `CHANGELOG.md` |
