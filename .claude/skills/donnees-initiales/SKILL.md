---
name: donnees-initiales
description: Ajouter ou mettre à jour des données livrées avec VetNutriMP (aliments du commerce, ingrédients, référentiels, équations) dans vetnutri_export_init.json — trois copies identiques (commonMain, Android, iOS), format d'un aliment, uuid, labels de nutriments, version qui déclenche la mise à jour chez les utilisateurs. À utiliser quand on ajoute une gamme d'aliments, corrige une composition, ou touche à un fichier vetnutri_export_init.json.
---

# Données initiales (VetNutriMP)

## Les fichiers

Le même fichier (~20 Mo, ~11 500 aliments) existe en **trois copies qui doivent rester identiques
octet pour octet** :

- `composeApp/src/commonMain/resources/data/vetnutri_export_init.json` (desktop, tests, vetnutriDogR)
- `composeApp/src/androidMain/assets/data/vetnutri_export_init.json`
- `iosApp/iosApp/Resources/vetnutri_export_init.json`

Format = `ApiEnvelope` (`Data/ApiModels.kt`) : `version`, `generatedAtEpochMs`, `foods`, `references`,
`equations`, `biblioRefs`, `recipes`, `conseils`, `consultationKeywords`.
Ne pas confondre avec `food.json` (racine) ni `androidMain/assets/data/vetfood.json` (anciens formats).

## Quand les utilisateurs reçoivent les changements

L'import est déclenché au démarrage (`View/StartupScreen.kt`) et depuis les réglages
(`SettingsViewModel`) **seulement si `version` du fichier est supérieure à la version stockée**
(`DatabaseVersionManager.compareVersions`, comparaison numérique `3.3.18` < `3.3.19`) ou si la base n'a
pas d'aliments. À version égale, rien n'est réimporté : un ajout sans changement de version n'atteint
que les nouvelles installations.

⇒ Monter `version` dans les trois copies quand les données doivent partir chez les utilisateurs
existants. Elle suit `versionName` de `composeApp/build.gradle.kts` (actuellement identiques) : à faire
avec la montée de version de l'application, ou demander à l'utilisateur. Mettre aussi à jour
`generatedAtEpochMs` (`date +%s000`).

```bash
for f in composeApp/src/commonMain/resources/data/vetnutri_export_init.json \
         composeApp/src/androidMain/assets/data/vetnutri_export_init.json \
         iosApp/iosApp/Resources/vetnutri_export_init.json; do
  sed -i '2s/"version": "[^"]*"/"version": "3.3.19"/; 3s/"generatedAtEpochMs": [0-9]*/"generatedAtEpochMs": '"$(date +%s000)"'/' "$f"
done
head -3 composeApp/src/commonMain/resources/data/vetnutri_export_init.json
```

## Format d'un aliment

```json
{
  "uuid": "LN-EI-DH0019",
  "name": "Chien Junior",
  "group": "ALL",
  "kind": "COMPLET",
  "brand": "La Normandise",
  "price": 0.0, "categoryPrice": "", "ingredients": "", "gamme": "",
  "presentation": "CAN",
  "presentationQuantity": 0.0,
  "deprecated": false,
  "dataB": "VF2026",
  "consistent": false,
  "lastUpdateDate": "2026-10-27",
  "rationId": "",
  "biblioRefIds": [],
  "species": ["CHIEN"],
  "indications": ["PHYS", "PED", "CROISSANCE"],
  "nutrients": { "HUMIDITE": 82.4, "PROTEINE": 9.7, "LIPIDE": 4.5, "CAL": 0.24, "ENERGIE": 75.3 },
  "energyPerSpecies": { "CHIEN": 75.3 }
}
```

Règles :
- **`uuid`** : stable et unique. Pour un produit industriel, préfixe de marque + référence fabricant
  (`LN-EI-…`, `RC-23-…`). Le même uuid = mise à jour de l'aliment existant ; un nouvel uuid = nouvel
  aliment (l'ancien reste dans les rations des utilisateurs). Pour retirer un produit : `"deprecated": true`,
  ne jamais le supprimer (des rations y font référence).
- **`nutrients`** : valeurs **pour 100 g brut**, labels canoniques des enums Kotlin (`HUMIDITE`,
  `PROTEINE`, `LIPIDE`, `CELLULOSE`, `CENDRE`, `ENA`, `CAL`, `PHOS`, `NA`, `K`, `CHL`, `MG`, `CU`, `ZN`,
  `FE`, `MN`, `I`, `SE`, `VITA`, `VITD`, `VITE`, `VITB1`…, `O3`, `O6`, `EPADHA`, acides aminés…). Unités
  = celles de l'enum : g pour les macro-éléments et minéraux majeurs, **mg** pour Cu/Zn/Fe/Mn,
  **µg** pour I et Se, UI pour les vitamines A/D/E. Convertir les étiquettes fabricant (souvent en
  mg/kg ou % de MS) ; ne pas mettre `0` pour une valeur inconnue : omettre la clé.
  Un label inconnu devient un nutriment personnalisé : utiliser le skill `ajouter-nutriment` si un vrai
  nouveau nutriment est nécessaire.
- **`ENERGIE`** et `energyPerSpecies` : kcal/100 g ; une entrée par espèce de `species`.
- Champs énumérés : `species` = noms de `Enumer/Espece.kt` (`CHIEN`, `CHAT`…) ; `kind` ∈ `COMPLET`,
  `COMPLEMENTAIRE`, `MEN` (ingrédient ménager), `BARF`, `ALL` ; `presentation` ∈ `CAN`, `SACHET`,
  `BOUCH`, `DOSETTE`, `GEL`, `ML`, `PRESSION`, `COMP`, `NO` ; `indications` = noms des
  entrées de `Enumer/AlimIndic.kt` (`PHYS`, `PED`, `SEN`, `CROISSANCE`…). En cas de doute, copier les valeurs d'un aliment voisin du même type.
- **`lastUpdateDate`** : `aaaa-mm-jj` (ISO). L'option d'import « seulement si plus récent » ne sait pas
  comparer les dates `jj/mm/aaaa` (`DatabaseFoodRepository.parseDateToInstantOrNull`).
- `dataB` : source des données (`VF2026` pour la base actuelle).

## Ajouter ou mettre à jour des aliments

Ne **jamais** recharger puis réécrire tout le fichier avec `json.dump` (des milliers de lignes de diff).
Utiliser le script fourni, qui modifie les trois copies et ne touche qu'aux aliments concernés :

1. Écrire les aliments dans un fichier JSON (liste) dans le scratchpad.
2. Ajouter :
   ```bash
   python3 .claude/skills/donnees-initiales/scripts/ajouter_aliments.py /chemin/nouveaux.json
   ```
   Mettre à jour des aliments existants (même uuid) : ajouter `--remplacer`.
3. Contrôler :
   ```bash
   python3 .claude/skills/donnees-initiales/scripts/verifier_init.py
   git diff --stat
   ```
   `verifier_init.py` vérifie les trois copies identiques, l'unicité des uuid, que chaque label de
   nutriment existe dans les enums Kotlin, et les valeurs numériques positives.
4. Monter `version` si les données doivent atteindre les installations existantes (voir plus haut).
5. Si R est disponible : `Rscript vetnutriDogR/tests/init-parity.R` (le module R lit ce fichier) ;
   sinon le signaler.

## Référentiels, équations, références bibliographiques

Même fichier, sections `references`, `equations`, `biblioRefs`. Ces objets sont liés par uuid
(un référentiel cite des équations et des références) : modifier un objet en gardant son uuid, vérifier
que tout uuid cité existe dans sa section. Préférer créer/modifier le référentiel dans l'application puis
copier l'objet exporté plutôt que d'écrire des équations à la main ; les équations sont évaluées par
`Utils/EquationEvaluator.kt` (variables `BW`, `MW`, `BEE`… : s'inspirer des équations existantes).
Toute modification de règle de calcul ⇒ `bash vetnutriDogR/scripts/validate-kotlin.sh`.

## Liste de contrôle

- [ ] Trois copies identiques (le script s'en charge)
- [ ] uuid stables et uniques, retraits par `deprecated`
- [ ] Valeurs pour 100 g, unités des enums, labels canoniques, pas de 0 pour « inconnu »
- [ ] `lastUpdateDate` ISO
- [ ] `verifier_init.py` sans erreur
- [ ] `version` montée si diffusion aux utilisateurs existants (ou question posée)
