# Corrections de l'audit multiplateforme

## Comportement retenu

La restauration reste une **fusion non destructive** : les objets correspondants sont mis à jour, et les objets absents de la sauvegarde sont conservés. Le texte de confirmation décrit désormais ce comportement. Aucun effacement global de base ni changement de schéma n'a été ajouté.

Avant la fusion, le JSON est décodé et une sauvegarde de l'état courant doit réussir. Un échec pendant l'import est signalé comme une restauration incomplète, avec le nom de la sauvegarde préalable. Il n'y a pas de rollback transactionnel global : des éléments peuvent déjà avoir été importés lorsque l'erreur est affichée. La sauvegarde préalable préserve les données exportables au format JSON existant ; elle ne constitue pas une image binaire complète de SQLite.

## Modifications

- **Sauvegardes** : écriture dans un fichier temporaire puis déplacement vérifié ; rotation uniquement après publication du fichier ; sérialisation des sauvegardes/restaurations d'une même instance ; prévention des collisions de noms ; annulation des coroutines propagée. Un export global ne remplace plus une erreur de lecture d'un dépôt par une section vide.
- **Métadonnées** : reconstruction des compteurs à partir du JSON lorsque le fichier de métadonnées manque.
- **Imports partiels** : compteurs d'erreurs dans le résultat d'import, contrôle par les parcours restauration/API/QR/import initial/examen ; correction du compteur d'animaux, auparavant incrémenté une seule fois par liste.
- **Préférences Desktop** : relecture sous verrou partagé avant chaque opération et remplacement du fichier par une écriture temporaire ; format Properties existant conservé.
- **Fichiers iOS** : les erreurs de lecture, écriture et copie natives ne sont plus ignorées. Les déplacements/suppressions/créations de répertoires sont vérifiés dans les services des trois plateformes.
- **JSON mobile** : sélecteurs natifs Android et iOS, délégation aux traitements communs existants pour animaux, aliments, API et besoins nutritionnels ; lecture en arrière-plan. Annulation du sélecteur sans import.
- **Export Android** : choix de destination via CreateDocument, y compris sous Android 9, sans permission globale de stockage. Écriture du JSON API en streaming conservée.
- **Export iOS** : fichier préparé en arrière-plan, feuille de partage présentée depuis Main, résultat attendu via son callback ; ancrage iPad et nom de fichier conservés.
- **PDF iOS** : suppression du fallback qui fabriquait une page blanche ; retour vers le parcours d'impression existant si la génération directe échoue ou dépasse cinq pages.

Les fonctions communes d'ouverture/export JSON deviennent suspendues pour attendre les sélecteurs sans bloquer le thread d'interface. Les appels existants ont été vérifiés et les implémentations Desktop conservent leurs dialogues Swing.

## Tests ajoutés

- Préférences : deux instances, écritures concurrentes, lecture fraîche, suppression/effacement sans résurrection, erreur disque.
- Import : plusieurs animaux comptés correctement, erreur partielle sans listener, propagation d'annulation.
- Sauvegarde : fusion sur une base non vide, sauvegarde préalable, JSON invalide, échec d'import, échec de sauvegarde préalable, métadonnées manquantes, déplacement de fichier inexistant.
- iOS : aller-retour UTF-8, lecture absente, destination invalide, copie depuis une source absente, conservation de la destination en cas d’échec, respect du paramètre de remplacement.

## Validation

Commande de vérification :

```sh
./gradlew :composeApp:desktopTest :composeApp:testDebugUnitTest :composeApp:compileTestKotlinIosSimulatorArm64 --offline --console=plain
```

- Desktop : 656 tests, aucune erreur ni échec.
- Android : 603 tests unitaires JVM, aucune erreur ni échec.
- iOS Simulator ARM64 : compilation de l’application Kotlin et des tests ; 6 tests spécifiques aux fichiers iOS ajoutés. Aucun test iOS exécuté : `xcrun simctl` est indisponible dans cet environnement.
- 13 nouveaux tests exécutables sur JVM ajoutés (dont 3 partagés avec Android), plus les 6 tests iOS.
- Traductions JSON valides ; `git diff --check` sans erreur.

Les avertissements de configuration préexistants (AGP/compileSdk, options Kotlin dépréciées, bibliothèque de console Jansi) ne sont pas traités dans ce correctif.

La validation des dialogues natifs et du rendu PDF reste distincte des tests unitaires. Contrôles manuels recommandés : import/export JSON sur Android 9 et Android récent ; annulation et reprise du sélecteur ; import depuis Fichiers/iCloud sur iOS ; partage sur iPhone et iPad ; rapport SVG de plus de cinq pages.
