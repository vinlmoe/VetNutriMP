# Audit multiplateforme — 25 septembre 2026

> État initial avant corrections. Voir [les corrections et leur validation](CORRECTIONS_AUDIT_MULTIPLATEFORME.md) pour le travail réalisé ensuite.

Révision examinée : `4569fab`. Audit du code partagé, des implémentations Android/iOS/Desktop, des points d'entrée et des chemins accessibles depuis l'interface. Desktop désigne macOS, Windows et Linux, qui utilisent la même cible JVM.

Les constats ci-dessous sont établis par lecture des chemins de code. Leurs symptômes n'ont pas été reproduits sur appareils mobiles ou sur Windows/Linux. Aucun correctif applicatif n'a été effectué. Cet audit ciblé ne constitue pas une validation exhaustive des calculs nutritionnels, de la sécurité ou de tous les écrans.

P1 : données, sauvegardes ou fonction importante compromise. P2 : fonction défaillante dans un scénario particulier.

## 1. P1 — iOS : une sauvegarde peut être annoncée réussie alors que l'écriture a échoué

**Sources :** `composeApp/src/iosMain/kotlin/fr/vetbrain/vetnutri_mp/ExcelPlatform/PlatformFile.ios.kt:64`, `Utils/JsonStreamUtils.ios.kt:8` dans le même répertoire de plateforme ; `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Service/BackupService.kt:114`.

`writeText` ignore le booléen de `NSString.writeToFile` et ne récupère pas l'erreur native. `encodeEnvelopeToFile` retourne donc `Result.success` même lorsque Foundation signale un échec sans exception Kotlin. `createBackup` poursuit alors la rotation et produit des métadonnées de succès.

**Déclencheur :** stockage plein ou destination non inscriptible. La nouvelle sauvegarde peut manquer sans que l'appelant le sache. `readText` transforme également une erreur en chaîne vide, et `copyTo` ignore l'échec natif de copie.

**Correction :** vérifier chaque résultat Foundation, propager l'erreur native et valider l'écriture avant toute rotation. Ajouter des tests iOS sur destination invalide, fichier absent et échec de copie.

## 2. P1 — Desktop : les préférences peuvent s'écraser entre écrans

**Sources :** `composeApp/src/desktopMain/kotlin/fr/vetbrain/vetnutri_mp/Utils/PreferencesStorage.desktop.kt:13`, `:28`, `:65` ; `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/AppContainer.kt:90` ; `Localization/LocalizationManager.kt:54` dans le code commun.

Chaque instance charge une copie du fichier au constructeur puis réécrit toutes ses propriétés lors d'une modification. Plusieurs instances coexistent réellement : préférences générales, langue, liste d'animaux, etc.

**Scénario :** A et B chargent le fichier ; A enregistre une nouvelle langue ; B enregistre ensuite une autre préférence avec son ancienne copie. La langue enregistrée par A disparaît ou reprend sa valeur antérieure. Aucun accès concurrent n'est nécessaire pour déclencher cette perte.

**Correction :** stockage partagé avec synchronisation des lectures/modifications et écriture atomique ; ou relecture et fusion sous verrou avant chaque écriture. Tester deux instances modifiant des clés différentes, puis une nouvelle lecture du fichier.

## 3. P1 — Toutes plateformes : « restaurer » fusionne au lieu de remplacer

**Sources :** `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Service/BackupService.kt:284`, `Repository/ExportImportRepository.kt:574`, `View/BackupRestoreView.kt:203`.

L'interface annonce explicitement le remplacement de toutes les données actuelles. L'implémentation appelle l'import générique, qui insère/met à jour les objets présents dans la sauvegarde, sans supprimer les objets absents.

**Scénario :** sauvegarder avec l'animal A, créer B, puis restaurer la sauvegarde. B reste présent malgré la promesse de retour à l'état sauvegardé. Le test existant restaure dans un dépôt vide et ne couvre pas ce cas.

**Correction :** distinguer clairement fusion et restauration. Pour une restauration, valider intégralement la sauvegarde puis remplacer les données de manière transactionnelle, avec sauvegarde préalable. Tester une base non vide contenant des objets supplémentaires et modifiés.

## 4. P1 — Toutes plateformes : une restauration partielle peut afficher un succès

**Sources :** `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Repository/ExportImportRepository.kt:697`, `:705`, `:1039`, `:1185` ; `Service/BackupService.kt:286` ; `ViewModel/BackupRestoreViewModel.kt:116`.

L'import intercepte plusieurs erreurs, poursuit le traitement et retourne uniquement les compteurs d'objets importés. Le résultat n'expose pas de compteur global d'échecs. La restauration ne fournit pas non plus de listener à l'import : les messages d'erreur optionnels sont donc perdus. Le service renvoie un succès et l'écran affiche « Restauration terminée avec succès! ».

**Déclencheur :** un dépôt échoue lors de la sauvegarde d'un animal, d'un aliment ou d'une référence. Une partie des modifications peut déjà avoir été appliquée.

**Correction :** résultat d'import incluant erreurs et objets rejetés ; transaction globale pour une restauration complète, ou statut explicite de restauration partielle. Tester une erreur injectée après plusieurs insertions.

## 5. P1 — Android et iOS : des boutons d'import JSON débouchent sur des stubs

**Sources :** `composeApp/src/androidMain/kotlin/fr/vetbrain/vetnutri_mp/FileImport.kt:14`, `:44` ; `composeApp/src/iosMain/kotlin/fr/vetbrain/vetnutri_mp/FileImport.kt:26`, `:47` ; `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/ViewModel/ImportViewModel.kt:177`, `ViewModel/SettingsViewModel.kt:259`, `View/SettingsView.kt:2217`.

Les actions communes « importer des animaux » et « importer API » appellent des implémentations qui affichent seulement « pas encore implémenté ». Le commentaire Android évoque une implémentation dans MainActivity, mais celle-ci n'enregistre que les sélecteurs CSV via `ExcelFileOperationsBridge`.

**Impact :** les fichiers JSON exportés ne peuvent pas être réimportés depuis ces actions sur mobile, contrairement au Desktop. Cela ne concerne pas nécessairement les autres chemins d'import, tels que CSV ou QR.

**Correction :** brancher les sélecteurs de documents natifs sur les ViewModels existants ; tester annulation, document invalide et aller-retour Desktop/mobile.

## 6. P1 — iOS : l'export JSON manipule UIKit depuis un thread de travail

**Sources :** `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/View/AnimalDetailView.kt:529`, `View/SettingsView.kt:2170` ; `composeApp/src/iosMain/kotlin/fr/vetbrain/vetnutri_mp/Utils/MainDispatcher.kt:9`, `FileImport.kt:69`.

Les appelants exécutent `exportApiEnvelopeToFile` dans `AppDispatchers.IO`, qui correspond à `Dispatchers.Default` sur iOS. L'implémentation construit et présente directement le `UIActivityViewController`, sans transfert vers le thread principal.

**Impact probable :** avertissements Main Thread Checker, présentation défaillante ou crash selon le contexte. De plus, la fonction retourne `true` même si aucun contrôleur racine n'a été trouvé.

**Correction :** conserver sérialisation/écriture sur le thread de travail, puis présenter le partage sur Main depuis le contrôleur visible. Distinguer fichier préparé, présentation et partage terminé.

La contrainte de thread est confirmée par la [documentation UIKit d'Apple](https://developer.apple.com/documentation/UIKit?language=objc).

## 7. P2 — iOS : un rapport PDF long peut devenir une page blanche

**Sources :** `composeApp/src/iosMain/kotlin/fr/vetbrain/vetnutri_mp/Export/PdfExporter.ios.kt:222`, `:253`, `:282`.

Pour un HTML contenant du SVG et de taille inférieure ou égale à 1 500 000 caractères, l'export essaie la génération directe. Le renderer refuse les documents de plus de cinq pages. Le fallback `genererPdfAlternative` crée alors une page PDF sans dessiner le moindre contenu, puis cette donnée non nulle est partagée comme un rapport normal.

**Scénario :** rapport avec graphique SVG dépassant cinq pages. Le même fallback intervient si le renderer échoue pour une autre raison.

**Correction :** supprimer ce faux succès ; basculer vers une impression fonctionnelle ou retourner une erreur explicite. Vérifier visuellement un rapport de six pages avec graphiques.

## 8. P2 — Android 9 : l'export JSON utilise un stockage nécessitant une permission absente

**Sources :** `composeApp/src/androidMain/kotlin/fr/vetbrain/vetnutri_mp/ExportImport.android.kt:18`, `:45` ; `composeApp/src/androidMain/AndroidManifest.xml:4`.

Sur API 28, les deux exports écrivent dans `MediaStore.Files` sur le stockage externe. Le manifeste ne déclare pas `WRITE_EXTERNAL_STORAGE` et MainActivity ne la demande pas. L'exception est avalée et devient simplement `false`. Android 9 fait pourtant partie des plateformes prises en charge.

**Correction :** utiliser un sélecteur de destination de type CreateDocument pour les JSON, ou gérer spécifiquement la permission sur API 28. Tester un appareil/émulateur API 28 avec installation neuve.

Les permissions du stockage partagé sur Android 9 sont décrites dans la [documentation Android](https://developer.android.com/training/data-storage).

## 9. P2 — Toutes plateformes : les métadonnées de sauvegarde manquantes sont remplacées par des chiffres fictifs

**Source :** `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Service/BackupService.kt:228`.

Si le JSON de sauvegarde existe sans son fichier de métadonnées, la liste enregistre arbitrairement 0 animal, 8 846 aliments, 24 équations, 1 conseil et 1 recette. Ces chiffres sont ensuite persistés et présentés comme les métadonnées de cette sauvegarde.

**Déclencheur :** copie d'une sauvegarde seule ou échec de l'écriture de ses métadonnées. Un utilisateur peut choisir une sauvegarde sur la base d'un contenu erroné.

**Correction :** lire les compteurs dans le contenu réel ou afficher « inconnu » sans fabriquer de nombres. Tester une sauvegarde sans sidecar de métadonnées.

## Validation

- `./gradlew :composeApp:desktopTest --offline` : succès ; 643 tests, 0 échec, 0 erreur, 0 test ignoré, répartis dans 58 suites.
- `./gradlew :composeApp:testDebugUnitTest --offline` : bloqué avant compilation par des dépendances AAR absentes du cache (notamment QR Kit, KoalaPlot et WebView). Les tests Android n'ont donc pas été exécutés ; cet échec de résolution ne prouve pas un défaut du code applicatif.
- Pas d'exécution sur simulateur/appareil iOS, Android, Windows ou Linux. Les tests JVM ne valident pas les API natives mobiles.
- Aucun fichier applicatif modifié ; seul ce rapport est ajouté.

Priorité recommandée : fiabilité sauvegarde/restauration (1, 3, 4), persistance Desktop (2), puis imports et exports mobiles (5 à 8), et exactitude des métadonnées (9).
