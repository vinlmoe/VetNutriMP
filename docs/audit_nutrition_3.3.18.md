# Audit des données nutritionnelles — 3.3.18

Audit du 28 septembre 2026 sur les **12 aliments ajoutés ou modifiés pendant cette mise à jour** : deux GlycoAdvanced, Metabolic + Mobility chat, cinq GI Biome félins et quatre OSALIM. Comparaison au catalogue antérieur et aux sources transmises. Il ne s’agit pas d’une validation de l’ensemble du catalogue ni d’une évaluation de l’adéquation d’une ration.

## Résultat

- **348 valeurs Hill’s contrôlées individuellement** contre les six tableaux utilisateur : aucune erreur de transcription ou de conversion détectée.
- Conversions GlycoAdvanced et OSALIM vérifiées, notamment UI/kg, mg/kg, µg/100 g et énergie par dose de 5 g.
- Valeurs numériques finies, positives ou nulles ; identifiants des 12 aliments uniques ; références sources présentes.
- Énergie manquante de Metabolic + Mobility complétée à **346,1 kcal/100 g**, soit 3461 kcal/kg. La [fiche officielle Hill’s Italie](https://www.hillspet.it/cat-food/prescription-diet-metabolic-jd-weight-management-dry) concorde avec le profil transmis (38 % de protéines, 13,7 % de lipides, 0,87 % de calcium, 0,68 % de phosphore et 0,732 % d’EPA+DHA).
- **Trois points restent à confirmer** : lipides/oméga-3 de Physio+, unité du magnésium de Senior+, vitamine A de GlycoAdvanced humide. Les valeurs explicitement sourcées sont conservées ; aucune correction par analogie n’a été appliquée.

## Méthode et unités

Les nutriments sont stockés pour 100 g d’aliment. Les acides aminés de `AAEnum` sont stockés en g/100 g de protéines, conformément aux calculs de l’application. La taurine reste en g/100 g d’aliment.

| Unité source | Conversion vers le JSON |
|---|---|
| % | g/100 g, valeur identique |
| UI/kg | division par 10 |
| ppm ou mg/kg, champ en mg | division par 10 |
| ppm ou mg/kg, champ en µg | multiplication par 100 |
| mg/kg, champ en g | division par 10 000 |
| Acide aminé en % de l’aliment | valeur × 100 / protéines (%) |
| kcal pour 5 g | multiplication par 20 |

Les comparateurs ont été choisis par espèce, usage et forme humide/sèche. Les compléments minéraux vitaminés sont comparés entre eux. Les écarts supérieurs à un facteur 5 servent de signal de revue, sans constituer une preuve d’erreur. Pour GlycoAdvanced, la vitamine A a aussi été ramenée à la matière sèche. Les valeurs des autres produits du catalogue ne sont pas présumées exemptes d’erreurs historiques.

## Comparaisons avec des produits proches

Valeurs pour 100 g d’aliment, sauf mention contraire.

| Produit contrôlé | Comparateur(s) du catalogue | Constat |
|---|---|---|
| GlycoAdvanced sec | RC Diabetic `RC-23-041`, Satiety `RC-23-043` | Vitamine A 2720 UI contre 2750–2850 ; Mg 0,06 g contre 0,069–0,100. Ordres de grandeur proches. |
| GlycoAdvanced humide | RC Diabetic humide `RC-23-042`, Satiety humide `RC-23-044` | Mg 0,02 g contre environ 0,012 ; cuivre 0,44 mg contre 0,30–0,37 ; zinc 2,4 mg contre 1,7–3,0. Vitamine A atypique : voir réserve ci-dessous. |
| Metabolic + Mobility chat | Metabolic Feline `H20-42`, Metabolic + Urinary Stress sec `M140` | Protéines 38 % contre 37–38,3 %, lipides 13,7 % contre 12,7–13,4 %, Ca/P 1,28 contre environ 1,10–1,30. Carnitine 53,19 mg proche des 52,6 mg de `M140`. |
| GI Biome Stress croquettes | GI Biome croquettes `H20-24` | Protéines 36 % contre 34,3 %, lipides 16,9 % contre 17,2 %, énergie identique 378,6 kcal. Aucun écart supérieur à ×5 sur les champs communs positifs. |
| GI Biome croquettes | GI Biome Stress croquettes | Même contrôle réciproque. EPA+DHA 0,348 contre 0,343 g. |
| GI Biome Stress mijoté | GI Biome mijoté `H20-113` | Protéines 7,7 % contre 7,5 %, lipides 3,9 % contre 3,8 %, énergie 82,6 contre 82,2 kcal. Amidon 3,1 % contre 0,6 % : différence de formulation explicitement présente dans les tableaux. |
| GI Biome mijoté | GI Biome Stress mijoté | Même contrôle réciproque ; pas de normalisation de l’amidon par analogie. |
| GI Biome sachet | Les deux mijotés GI Biome | Produit moins humide (76 % contre 80 %), énergie plus élevée (96,2 contre 82,2–82,6 kcal). B1 0,6 mg contre 5,4–5,6 et bêta-carotène 5 µg contre 108–179 : différences confirmées par les tableaux utilisateur. |
| OSALIM Physio+ | OSALIM Digest+, Vit’i5 Orange `cdd7ce64-4b19-431d-be1c-e4c786de99b5`, Vit’i5 Canine Ca/P=3 `C2` | Ca/P 2,98, comparable à environ 3 ; fer 82,5 mg, B5 56,8 mg et B12 150 µg du même ordre que les compléments proches. Incohérence des lipides détaillée ci-dessous. |
| OSALIM Digest+ | Physio+, Skin+ | Ca/P 2,96. B9 100 000 µg et B12 500 µg, soit environ 3,33 fois les autres OSALIM : valeurs spécifiques confirmées par la plaquette et le tableau officiel. |
| OSALIM Skin+ | Physio+, Digest+ | Zinc 300 mg et cuivre 30 mg, supérieurs à Physio+/Digest+ (150 et 12 mg). EPA+DHA 0,10 g contre 0,60 g pour Physio+ : valeurs explicitement documentées. |
| OSALIM Senior+ | Physio+, Vit’i5 Rénal `c2e9cb8e-a28f-4ae7-9d28-96a3d5287abc`, Vit’i5 Bleu `5bc0564e-785d-47aa-9a6b-edb8626768b9` | Calcium 11,3 % contre 10–12,5 % pour les deux Vit’i5 ; phosphore explicitement nul, cohérent avec le positionnement de la formule. Magnésium non validable. |

### Écarts de sources à confirmer

1. **OSALIM Physio+ : oméga-3 supérieurs aux lipides totaux.** Le [tableau officiel Osalia](https://irp.cdn-website.com/3b090fda/dms3rep/multi/opt/Plan%2Bde%2Btravail%2B1OASLIM-PHYSIO--CHIEN-poso-2601-1920w.jpg) indique simultanément 7500 mg/kg d’oméga-3 (0,75 %), 6000 mg/kg d’EPA+DHA (0,60 %) et 0,30 % de matières grasses. Cette relation est incohérente pour une même base analytique. La conversion n’est pas en cause. Valeurs conservées et anomalie ajoutée à la référence liée au produit ; analyse corrigée du fabricant nécessaire. L’énergie de 143,2 kcal/100 g reste celle de la plaquette, sans recalcul à partir de ce profil.
2. **OSALIM Senior+ : magnésium.** Le PDF et le [tableau officiel](https://irp.cdn-website.com/3b090fda/dms3rep/multi/opt/DOSAGE-OSALIM-Dosage-SENIOR--1920w.jpg) indiquent « 0,12 mg » dans la colonne par kg ou %, contre 6,15 mg pour 5 g. Ces mentions sont incompatibles ; le champ `MG` reste absent. Les écarts des doses de bentonite, sodium et calcium sont également consignés dans la référence source. Les valeurs par % de sodium/calcium restent inchangées.
3. **GlycoAdvanced humide : vitamine A élevée.** Le message utilisateur donne 27 200 UI/kg, soit 2720 UI/100 g et **17 000 UI/100 g de matière sèche** à 84 % d’humidité. Diabetic humide et Satiety humide du catalogue sont à environ 2288 et 3125 UI/100 g de matière sèche : écart de ×5,4 à ×7,4. La valeur utilisateur reste prioritaire mais mérite confirmation. La [page Royal Canin](https://www.royalcanin.com/fr/cats/products/vet-products/glycoadvanced-8071) ne fournit pas l’analyse totale nécessaire pour trancher ; une liste d’additifs ne permet pas de remplacer une teneur totale.

### Différences historiques chez les comparateurs

La biotine de `H20-42` est enregistrée à 0,054 µg/100 g, très loin des 44 µg/100 g du nouveau Metabolic + Mobility. La nouvelle valeur est bien la conversion de 0,44 ppm ; l’ancienne fiche peut contenir une confusion mg/µg. De même, `H20-42` contient 538,2 mg/100 g de carnitine, contre 52,6 pour `M140` et 53,19 pour le nouvel aliment. Cela motive une revue ultérieure du comparateur, pas une modification de la valeur nouvelle correctement sourcée.

Les valeurs historiques de choline des quatre OSALIM sont conservées sans nouvelle confirmation : les documents mentionnent de la **bétaïne HCl**, qui ne doit pas être assimilée à une teneur en choline. Aucun nouveau champ choline n’a été calculé à partir de la bétaïne.

## Contrôles internes

- Somme humidité + protéines + lipides + ENA + cellulose brute + cendres : 100 % pour GlycoAdvanced et OSALIM ; 99,5 à 100,6 % pour les six Hill’s. Les faibles écarts des tableaux arrondis sont conservés, sans imposer une somme artificielle à 100 %.
- EPA+DHA concorde avec la somme EPA et DHA à 0,0011 g/100 g près, ce qui couvre les arrondis des tableaux Biome.
- Fibres totales supérieures ou égales aux fibres solubles et à la cellulose brute pour les fiches qui renseignent ces champs.
- Les acides aminés reconvertis en g/100 g d’aliment restituent les tableaux transmis. Glutamine + glutamate ne sont pas attribués arbitrairement au glutamate seul.
- Les informations sans champ approprié restent conservées dans les références sources.

## Harmonisation des versions

Version applicative **3.3.18**, numéro de build **318** : Gradle Android, paquet Desktop, constante affichée dans l’interface, deux Info.plist iOS, configurations du projet Xcode et script de packaging macOS. README et changelog actualisés en préservant l’historique des versions.

Les quatre fichiers `vetnutri_export_init.json` sont identiques en version **3.3.18**, avec 11 466 aliments. La copie `iosApp/iosApp/Resources` avait 10 916 aliments ; tous ses identifiants figuraient déjà dans le catalogue courant. Elle a été synchronisée avec celui-ci, plutôt que de changer uniquement son étiquette de version. Les exports explicitement archivés ne sont pas réécrits.

## Validation technique

- Compilation `:composeApp:compileKotlinDesktop --offline` réussie ; avertissements Kotlin/Gradle existants.
- Lecture des quatre JSON, identité octet par octet et version 3.3.18 vérifiées.
- Versions et builds des six configurations Xcode, deux Info.plist, Gradle et constante d’interface contrôlés.
- Projet Xcode validé par `plutil`, syntaxe du script macOS validée par `bash -n`, contrôle `git diff --check` réussi.
- Pas de compilation native iOS/Android ni de publication de binaires dans le cadre de cet audit.
