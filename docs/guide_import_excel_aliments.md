# Guide d'Import Excel pour les Aliments

Ce guide détaille le format requis pour importer des aliments dans VetNutri MP via un fichier CSV compatible Excel.

## Résumé Exécutif pour LLM

**Format**: CSV avec séparateur point-virgule (`;`), encodage UTF-8

**Enums à utiliser**:
- **Type Aliment**: Labels en minuscules (`complete`, `complementary`, `household`, `barf`)
- **Conditionnement**: Labels en minuscules (`can`, `sachet`, `gel`, `no`, `pressure`, `ml`, `tablet`, `spoon`, `dosette`)
- **Indications**: Noms d'enum en MAJUSCULES séparés par virgules (`NEUT, SEN, URO`)
- **Groupe Alimentaire**: Labels (`supplements`, `offal`, `creams`, etc.)

**Séparation des valeurs multiples**: Utiliser des virgules (`,`), espaces optionnels

**IMPORTANT - Acides aminés**: Les acides aminés sont exprimés **pour 100g de protéine** (et non pour 100g d'aliment). Tous les autres nutriments sont exprimés pour 100g d'aliment.

**Exemple de ligne**:
```
UUID;Nom;Marque;Gamme;Ingrédients;Groupe Alimentaire;Type Aliment;Conditionnement;Prix;Catégorie Prix;Quantité Interne;Consistant;Obsolète;Données Base;Espèces;Indications;UUID Ration;PROTEINE (%);LIPIDE (%);ENERGIE (kcal/100g)
550e8400-e29b-41d4-a716-446655440000;Croquettes Premium Chat;Royal Canin;Sterilised;Viande de poulet, riz;supplements;complete;can;45.99;Premium;350.0;true;false;6;Chat;NEUT, SEN;;30.0;12.0;350.0
```

**Note**: Dans cet exemple, le conditionnement est `can` (boîte) et la Quantité Interne est `350.0`, ce qui signifie que chaque boîte contient 350g d'aliment.

## Format du Fichier

- **Format**: CSV (Comma-Separated Values)
- **Séparateur**: Point-virgule (`;`)
- **Encodage**: UTF-8
- **Première ligne**: En-têtes des colonnes (obligatoire)
- **Lignes suivantes**: Données des aliments (une ligne par aliment)

## Structure des Colonnes

### Colonnes Obligatoires de Base

| Colonne | Type | Description | Exemple |
|---------|------|-------------|---------|
| UUID | String | Identifiant unique (généré automatiquement si vide) | `550e8400-e29b-41d4-a716-446655440000` |
| Nom | String | Nom de l'aliment | `Croquettes Premium Chat` |
| Marque | String | Marque du produit | `Royal Canin` |
| Gamme | String | Gamme du produit | `Sterilised` |
| Ingrédients | String | Liste des ingrédients | `Viande de poulet, riz, maïs...` |

### Colonnes de Classification

| Colonne | Type | Description | Valeurs Possibles |
|---------|------|-------------|-------------------|
| **Groupe Alimentaire** | String | Catégorie de l'aliment | Voir section "GroupAlim" ci-dessous |
| **Type Aliment** | String | Type d'aliment | Voir section "FoodKind" ci-dessous |
| **Conditionnement** | String | Format de conditionnement | Voir section "ContEnum" ci-dessous |

### Colonnes Numériques et Booléennes

| Colonne | Type | Description | Format |
|---------|------|-------------|--------|
| Prix | Double | Prix du produit | `45.99` |
| Catégorie Prix | String | Catégorie de prix | `i`, `Premium`, etc. |
| Quantité Interne | Double | Quantité du conditionnement (en grammes ou unité selon le type) | `350.0` |
| Consistant | Boolean | Aliment consistant | `true` ou `false` |
| Obsolète | Boolean | Aliment obsolète | `true` ou `false` |
| Données Base | String | Source des données | `6`, `7`, etc. |

**Note sur la Quantité Interne**: Cette valeur représente la quantité contenue dans le conditionnement. Par exemple:
- Si le **Conditionnement** est `can` (boîte), la Quantité Interne représente le poids en grammes par boîte (ex: `350.0` pour une boîte de 350g)
- Si le **Conditionnement** est `sachet`, la Quantité Interne représente le poids en grammes par sachet
- Si le **Conditionnement** est `ml`, la Quantité Interne représente le volume en millilitres
- Si le **Conditionnement** est `tablet` (comprimé), la Quantité Interne représente le nombre de comprimés ou le poids par comprimé
- Si le **Conditionnement** est `spoon` (cuillère), la Quantité Interne représente la quantité par cuillère
- Si le **Conditionnement** est `dosette`, la Quantité Interne représente la quantité par dosette

### Colonnes Multiples (Séparées par Virgule)

| Colonne | Type | Description | Format |
|---------|------|-------------|--------|
| **Espèces** | String | Espèces ciblées (séparées par virgule) | `Chat, Chien` |
| **Indications** | String | Indications nutritionnelles (séparées par virgule) | Voir section "AlimIndic" ci-dessous |

### Colonnes de Nutriments

Les colonnes de nutriments suivent le format: `NOM_NUTRIMENT (unité)`

Exemples:
- `PROTEINE (%)`
- `LIPIDE (%)`
- `ENERGIE (kcal/100g)`
- `VITA (UI/kg)`

Voir la liste complète des nutriments dans la section dédiée.

---

## Enums et Valeurs Possibles

### 1. Type Aliment (FoodKind)

**Colonne**: `Type Aliment`

**Valeurs acceptées** (utiliser le **label** en minuscules):

| Label | Description |
|-------|-------------|
| `complete` | Aliment complet |
| `complementary` | Aliment complémentaire |
| `household` | Aliment ménager |
| `barf` | Aliment BARF (cru) |

**Exemple**: `complete`

**Note**: Le système accepte aussi les synonymes (complet, complémentaire, ménager, cru, etc.) mais il est recommandé d'utiliser les labels ci-dessus.

---

### 2. Conditionnement (ContEnum)

**Colonne**: `Conditionnement`

**Valeurs acceptées** (utiliser le **label** en minuscules):

| Label | Description |
|-------|-------------|
| `no` | Aucun conditionnement spécifique |
| `can` | Boîte de conserve |
| `sachet` | Sachet |
| `gel` | Gel |
| `pressure` | Pression |
| `ml` | Millilitres |
| `tablet` | Comprimé |
| `spoon` | Cuillère |
| `dosette` | Dosette |

**Exemple**: `can`

**Note**: La quantité du conditionnement doit être spécifiée dans la colonne **Quantité Interne**. Par exemple, si le conditionnement est `can` (boîte), la Quantité Interne indiquera le poids en grammes par boîte (ex: `350.0` pour une boîte de 350g).

---

### 3. Indications (AlimIndic)

**Colonne**: `Indications`

**Format**: Valeurs séparées par des **virgules** (`,`)

**IMPORTANT**: Utiliser le **nom de l'enum** (en majuscules) et non le label.

**Valeurs acceptées** (utiliser le **nom de l'enum**):

| Nom Enum | Label (affichage) |
|----------|------------------|
| `PED` | Pédiatrique |
| `NEUT` | Stérilisé |
| `PHYS` | Physiologique |
| `SEN` | Sénior |
| `CALM` | Stress félin |
| `OBES` | Obésité |
| `GESTATION` | Gestation |
| `SONDE` | Sonde |
| `LACT` | Lactation |
| `CROISSANCE` | Croissance |
| `DENT` | Hygiène Buccodentaire |
| `DIAB` | Diabète |
| `INSHEP` | Insuffisance Hépatique |
| `HYPO` | Hypoallergénique |
| `ART` | Soutien Articulaire |
| `MRC` | Soutien de la fonction rénale |
| `CONV` | Convalescence |
| `MBAUF` | MBAUF |
| `URO` | Urolithiase |
| `DERM` | Affections cutanées |
| `GI` | Affections gastro-intestinales |
| `CAR` | Affections cardiaques |
| `END` | Affections endocriniennes |
| `IPE` | Insuffisance pancréatique |
| `DISTRU` | Dissolution struvites |
| `REDSTRU` | Réduction struvites |
| `REDURA` | Réduction urates |
| `REDOXA` | Réduction oxalates |
| `REDCYST` | Réduction cystines |
| `ACT` | Sport |
| `AUTRE` | Autre |

**Exemple avec plusieurs indications**: `NEUT, SEN, URO`

**Note**: Le système accepte aussi les labels (ex: "Stérilisé", "Sénior") mais il est **fortement recommandé** d'utiliser les noms d'enum (PED, NEUT, etc.) pour garantir la compatibilité.

---

### 4. Groupe Alimentaire (GroupAlim)

**Colonne**: `Groupe Alimentaire`

**Valeurs acceptées** (utiliser le **label**):

| Label | Description |
|-------|-------------|
| `offal` | Abats |
| `culinaryAids` | Aides culinaires |
| `seaweed` | Algues |
| `freshHerbs` | Aromates frais |
| `driedHerbs` | Aromates séchés |
| `otherCereals` | Autres céréales |
| `otherFishProducts` | Autres produits de poisson |
| `babyFood` | Aliments pour bébé |
| `animalFats` | Matières grasses animales |
| `dairyFats` | Matières grasses laitières |
| `otherFats` | Autres matières grasses |
| `rusks` | Biscotte |
| `savoryBiscuits` | Biscuits salés |
| `sweetBiscuits` | Biscuits sucrés |
| `readyBroth` | Bouillon prêt à l'emploi |
| `breakfastCereals` | Céréales petit-déjeuner |
| `delicatessen` | Charcuterie |
| `supplements` | Compléments |
| `creams` | Crèmes |
| `shellfish` | Crustacés |
| `dairyDesserts` | Desserts lactés |
| `otherDesserts` | Autres desserts |
| `water` | Eaux |
| `spices` | Épices |
| `cannedFruits` | Fruits en conserve |
| `freshFruits` | Fruits frais |
| `nuts` | Fruits à coque |
| `juices` | Jus |
| ... (liste complète disponible dans le code) |

**Exemple**: `supplements`

**Note**: Cette colonne est optionnelle. Si vide, le groupe sera `null`.

---

## Séparation des Valeurs Multiples

### Espèces

**Format**: Valeurs séparées par des **virgules** (`,`)

**Exemple**: `Chat, Chien`

**Valeurs possibles**: `Chat`, `Chien`, `Lapin`, `Furet`, etc.

### Indications

**Format**: Noms d'enum séparés par des **virgules** (`,`)

**Exemple**: `NEUT, SEN, URO`

**Important**: 
- Utiliser les **noms d'enum** (PED, NEUT, etc.) et non les labels
- Séparer par des virgules
- Espaces optionnels autour des virgules (seront automatiquement supprimés)

---

## Liste Complète des Nutriments

Les colonnes de nutriments suivent le format: `NOM_NUTRIMENT (unité)`

**IMPORTANT**: Les acides aminés sont exprimés **pour 100g de protéine** (et non pour 100g d'aliment). Tous les autres nutriments sont exprimés pour 100g d'aliment.

### Détection Automatique des Unités

Le système détecte automatiquement l'unité depuis l'en-tête de colonne (entre parenthèses) et effectue les conversions nécessaires :

- **Si l'unité est `/kg` ou `g/kg`**: La valeur est automatiquement convertie en `/100g` (division par 10)
  - Exemple: `LIPIDE (g/kg)` avec valeur `100` → converti en `10 g/100g`
  
- **Si l'unité est `%`**: Aucune conversion (déjà en pourcentage)
  - Exemple: `LIPIDE (%)` avec valeur `10` → reste `10%`
  
- **Si l'unité est `/100g` ou `g/100g`**: Aucune conversion (déjà la bonne unité)
  - Exemple: `LIPIDE (g/100g)` avec valeur `10` → reste `10 g/100g`
  
- **Si aucune unité n'est spécifiée** (pas de parenthèses): Le système suppose `/100g` (pas de conversion)
  - Exemple: `LIPIDE` avec valeur `10` → traité comme `10 g/100g`

**Note**: Les conversions automatiques permettent d'importer des fichiers avec différentes unités sans modification manuelle.

### Nutriments Principaux

- `HUMIDITE (%)`
- `PROTEINE (%)`
- `LIPIDE (%)`
- `GLUCIDE (%)`
- `ENA (%)`
- `FIBRE (%)`
- `CELLULOSE (%)`
- `CENDRE (%)`
- `ENERGIE (kcal/100g)`
- `SUCRE (%)`
- `AMIDON (%)`
- `FIBRESOL (%)`
- `FIBRETOT (%)`
- `NDF (%)`
- `ADF (%)`

### Vitamines

- `VITA (UI/kg)`
- `VITC (mg/kg)`
- `VITD (UI/kg)`
- `VITE (UI/kg)`
- `VITK (mg/kg)`
- `VITB1 (mg/kg)`
- `VITB2 (mg/kg)`
- `VITB3 (mg/kg)`
- `VITB5 (mg/kg)`
- `VITB6 (mg/kg)`
- `VITB8 (mg/kg)`
- `VITB9 (mg/kg)`
- `VITB12 (mg/kg)`
- `CHOLINE (mg/kg)`
- `RETINOL (UI/kg)`
- `BETACAR (mg/kg)`

### Minéraux

- `FE (mg/kg)`
- `CU (mg/kg)`
- `ZN (mg/kg)`
- `MN (mg/kg)`
- `I (mg/kg)`
- `SE (mg/kg)`

### Macroéléments

- `CAL (g/100g)`
- `PHOS (g/100g)`
- `MG (g/100g)`
- `NA (g/100g)`
- `K (g/100g)`
- `CHL (g/100g)`

### Lipides

- `AGSATURE (g/100g)`
- `AGMONO (g/100g)`
- `AGPOLY (g/100g)`
- `AG40 (g/100g)`
- `AG60 (g/100g)`
- `AG80 (g/100g)`
- `AG100 (g/100g)`
- `AG120 (g/100g)`
- `AG140 (g/100g)`
- `AG160 (g/100g)`
- `AG180 (g/100g)`
- `AG181 (g/100g)`
- `AG182 (g/100g)`
- `AG183 (g/100g)`
- `AG204 (g/100g)`
- `AG205 (g/100g)`
- `AG226 (g/100g)`
- `CHOLES (g/100g)`
- `O3 (g/100g)`
- `O6 (g/100g)`
- `EPADHA (g/100g)`

### Acides Aminés

**IMPORTANT**: Les acides aminés sont exprimés **pour 100g de protéine** (et non pour 100g d'aliment).

- `ALANINE (g/100g protéine)`
- `ARGININE (g/100g protéine)`
- `ASPARAGINE (g/100g protéine)`
- `ASPARATE (g/100g protéine)`
- `CYSTEINE (g/100g protéine)`
- `GLUTAMATE (g/100g protéine)`
- `GLUTAMINE (g/100g protéine)`
- `GLYCINE (g/100g protéine)`
- `HISTIDINE (g/100g protéine)`
- `ISOLEUCINE (g/100g protéine)`
- `LEUCINE (g/100g protéine)`
- `LYSINE (g/100g protéine)`
- `METHIONINE (g/100g protéine)`
- `PHENYLALANINE (g/100g protéine)`
- `PROLINE (g/100g protéine)`
- `PYRROLYSINE (g/100g protéine)`
- `SELENOCYSTEINE (g/100g protéine)`
- `SERINE (g/100g protéine)`
- `THREONINE (g/100g protéine)`
- `TRYPTOPHANE (g/100g protéine)`
- `TYROSINE (g/100g protéine)`
- `VALINE (g/100g protéine)`

**Note**: Si vous avez les valeurs en g/100g d'aliment, vous devez les convertir en g/100g de protéine en divisant par le pourcentage de protéine et en multipliant par 100.

### Autres Nutriments

- `TAURINE (g/100g)`
- `CARNITINE (g/100g)`
- `FOS (g/100g)`
- `MOS (g/100g)`
- `SACC (g/100g)`
- `FRUCT (g/100g)`
- `LACTO (g/100g)`
- `MALT (g/100g)`
- `AcOx (g/100g)`
- `GAL (g/100g)`
- `GLUCOSE (g/100g)`
- `DEXTROSE (g/100g)`

**Note**: Les valeurs de nutriments sont optionnelles. Laisser la cellule vide si la valeur n'est pas disponible.

---

## Exemple de Fichier CSV

```csv
UUID;Nom;Marque;Gamme;Ingrédients;Groupe Alimentaire;Type Aliment;Conditionnement;Prix;Catégorie Prix;Quantité Interne;Consistant;Obsolète;Données Base;Espèces;Indications;UUID Ration;PROTEINE (%);LIPIDE (%);ENERGIE (kcal/100g);VITA (UI/kg)
550e8400-e29b-41d4-a716-446655440000;Croquettes Premium Chat;Royal Canin;Sterilised;Viande de poulet, riz, maïs, gluten de blé;supplements;complete;can;45.99;Premium;350.0;true;false;6;Chat;NEUT, SEN;550e8400-e29b-41d4-a716-446655440001;30.0;12.0;350.0;15000.0
```

**Note**: Dans cet exemple, le conditionnement est `can` (boîte) et la Quantité Interne est `350.0`, ce qui signifie que chaque boîte contient 350g d'aliment.

---

## Règles Importantes

### 1. Format des Enums

- **Type Aliment**: Utiliser le **label** en minuscules (`complete`, `complementary`, `household`, `barf`)
- **Conditionnement**: Utiliser le **label** en minuscules (`can`, `sachet`, `gel`, etc.)
- **Indications**: Utiliser le **nom de l'enum** en majuscules (`NEUT`, `SEN`, `URO`, etc.) séparés par des virgules
- **Groupe Alimentaire**: Utiliser le **label** (`supplements`, `offal`, etc.)

### 2. Séparation des Valeurs Multiples

- **Espèces**: Séparer par des virgules avec espaces optionnels (`Chat, Chien` ou `Chat,Chien`)
- **Indications**: Séparer par des virgules avec espaces optionnels (`NEUT, SEN, URO` ou `NEUT,SEN,URO`)

### 3. Valeurs Numériques et Séparateurs Décimaux

- Les colonnes peuvent être vides sauf indication contraire
- Les valeurs numériques peuvent utiliser **soit le point (`.`) soit la virgule (`,`)** comme séparateur décimal
  - Format anglo-saxon: `10.5` (recommandé)
  - Format européen: `10,5` (également accepté)
- Les valeurs booléennes doivent être `true` ou `false` (en minuscules)

### 4. Unités des Nutriments

- Les unités peuvent être spécifiées dans l'en-tête entre parenthèses: `NOM_NUTRIMENT (unité)`
- **Unités supportées**:
  - `%` : Pourcentage (pas de conversion)
  - `g/100g` ou `/100g` : Grammes pour 100g (pas de conversion)
  - `g/kg` ou `/kg` : Grammes pour kg (converti automatiquement en g/100g en divisant par 10)
  - `mg/100g`, `UI/kg`, etc. : Autres unités (pas de conversion automatique)
- **Si aucune unité n'est spécifiée**: Le système suppose `g/100g` (ou `%` selon le nutriment)

### 4. Gestion des Erreurs

- Si une valeur d'enum n'est pas reconnue, le système utilisera une valeur par défaut
- Les erreurs seront reportées dans le résultat d'import
- Les lignes avec des erreurs critiques seront ignorées

---

## Checklist de Préparation

Avant d'importer votre fichier, vérifiez:

- [ ] Le fichier est au format CSV avec séparateur point-virgule (`;`)
- [ ] L'encodage est UTF-8
- [ ] La première ligne contient les en-têtes de colonnes
- [ ] Les valeurs de **Type Aliment** utilisent les labels en minuscules (`complete`, `complementary`, etc.)
- [ ] Les valeurs de **Conditionnement** utilisent les labels en minuscules (`can`, `sachet`, etc.)
- [ ] Les valeurs de **Indications** utilisent les noms d'enum en majuscules (`NEUT`, `SEN`, etc.) séparés par des virgules
- [ ] Les valeurs multiples (Espèces, Indications) sont séparées par des virgules
- [ ] Les valeurs numériques utilisent le point (`.`) comme séparateur décimal
- [ ] Les valeurs booléennes sont `true` ou `false` en minuscules

---

## Support

En cas de problème lors de l'import, vérifiez:
1. Le format du fichier (CSV avec point-virgule)
2. L'encodage (UTF-8)
3. Les valeurs des enums (respecter les formats indiqués)
4. Les messages d'erreur dans le résultat d'import

