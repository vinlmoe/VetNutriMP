# Guide Import/Export Excel Aliments

## Vue d'ensemble

Le système d'import/export Excel permet de gérer les aliments (`AlimentEv`) de manière efficace via des fichiers CSV compatibles Excel. Ce système prend en compte tous les paramètres de la classe `AlimentEv` et permet de définir l'unité pour chaque nutriment individuellement.

## Architecture du système

### Composants principaux

1. **`AlimentExcelRow`** - Structure de données pour l'import/export
2. **`AlimentExcelService`** - Service de conversion CSV
3. **`AlimentImportExportView`** - Interface utilisateur
4. **`AlimentImportExportViewModel`** - Logique métier

## Format CSV

### Structure générale

- **Séparateur**: Point-virgule (`;`)
- **Encodage**: UTF-8
- **Première ligne**: En-têtes des colonnes
- **Listes**: Séparées par des virgules (`,`)
- **Valeurs vides**: Cellules vides ou absence de valeur

### Colonnes principales

| Colonne | Type | Description | Obligatoire |
|---------|------|-------------|-------------|
| UUID | String | Identifiant unique | Non (généré automatiquement) |
| Nom | String | Nom de l'aliment | Oui |
| Marque | String | Marque du produit | Non |
| Gamme | String | Gamme du produit | Non |
| Ingrédients | String | Liste des ingrédients | Non |
| Groupe Alimentaire | String | Groupe (ex: "croquettes", "viandes") | Non |
| Type Aliment | String | Type (complet, complémentaire, ménager, barf) | Non |
| Conditionnement | String | Type de conditionnement | Non |
| Prix | Double | Prix unitaire | Non |
| Catégorie Prix | String | Catégorie de prix | Non |
| Quantité Interne | Double | Quantité interne | Non |
| Consistant | Boolean | Aliment consistant | Non |
| Obsolète | Boolean | Aliment obsolète | Non |
| Espèces | String | Espèces séparées par virgules | Non |
| Indications | String | Indications séparées par virgules | Non |
| UUID Ration | String | UUID de la ration associée | Non |

### Colonnes de nutriments

Pour chaque nutriment, deux colonnes sont créées :
- `{NUTRIMENT} (valeur)`: Valeur numérique
- `{NUTRIMENT} (unité)`: Unité de mesure

#### Nutriments principaux (15 nutriments)
- HUMIDITE, PROTEINE, LIPIDE, GLUCIDE, ENA, FIBRE, CELLULOSE
- CENDRE, ENERGIE, SUCRE, AMIDON, FIBRESOL, FIBRETOT, NDF, ADF

#### Vitamines (16 nutriments)
- VITA, VITC, VITD, VITE, VITK
- VITB1, VITB2, VITB3, VITB5, VITB6, VITB8, VITB9, VITB12
- CHOLINE, RETINOL, BETACAR

#### Minéraux (6 nutriments)
- FE (Fer), CU (Cuivre), ZN (Zinc), MN (Manganèse)
- I (Iode), SE (Sélénium)

#### Macroéléments (6 nutriments)
- CAL (Calcium), PHOS (Phosphore), MG (Magnésium)
- NA (Sodium), K (Potassium), CHL (Chlore)

#### Lipides (21 nutriments)
- AGSATURE (Acides gras saturés), AGMONO (mono-insaturés), AGPOLY (poly-insaturés)
- AG40 à AG226 (différents acides gras : C4:0, C6:0, C8:0, etc.)
- CHOLES (Cholestérol), O3 (Omega 3), O6 (Omega 6), EPADHA (EPA et DHA)

#### Acides aminés (22 nutriments)
- ALANINE, ARGININE, ASPARAGINE, ASPARATE, CYSTEINE
- GLUTAMATE, GLUTAMINE, GLYCINE, HISTIDINE, ISOLEUCINE
- LEUCINE, LYSINE, METHIONINE, PHENYLALANINE, PROLINE
- PYRROLYSINE, SELENOCYSTEINE, SERINE, THREONINE, TRYPTOPHANE
- TYROSINE, VALINE

#### Autres nutriments (12 nutriments)
- TAURINE, CARNITINE, FOS, MOS
- SACC (Saccharose), FRUCT (Fructose), LACTO (Lactose), MALT (Maltose)
- AcOx (Acide Oxalique), GAL (Galactose), GLUCOSE, DEXTROSE

**Total : 98 nutriments × 2 colonnes = 196 colonnes de nutriments**

> **Note**: Les nutriments d'énergie (9 éléments) et d'analyse (10 éléments) ont été exclus car ils sont généralement calculés automatiquement ou ne sont pas directement saisis dans les aliments de base.

## Utilisation

### Export

1. Accéder à la section "Import/Export Excel Aliments"
2. Cliquer sur "Exporter vers CSV"
3. Confirmer l'export
4. Le fichier CSV est généré avec tous les aliments

### Import

1. Préparer un fichier CSV selon le format décrit
2. Accéder à la section "Import/Export Excel Aliments"
3. Cliquer sur "Importer depuis CSV"
4. Sélectionner le fichier CSV
5. Vérifier les résultats d'import

### Modèle d'exemple

1. Cliquer sur "Télécharger modèle"
2. Utiliser le fichier généré comme base pour créer vos données

## Gestion des unités

### Unités par défaut par nutriment

| Nutriment | Unité par défaut | Description |
|-----------|------------------|-------------|
| PROTEINE | g | Grammes |
| LIPIDE | g | Grammes |
| GLUCIDE | g | Grammes |
| ENERGIE | kcal | Kilocalories |
| VITA | UI | Unités Internationales |
| VITC | mg | Milligrammes |
| VITD | UI | Unités Internationales |
| VITE | UI | Unités Internationales |
| VITK | mg | Milligrammes |
| VITB1-12 | mg | Milligrammes |
| CHOLINE | mg | Milligrammes |
| RETINOL | µg | Microgrammes |
| BETACAR | µg | Microgrammes |

### Personnalisation des unités

Vous pouvez définir une unité spécifique pour chaque nutriment en remplissant la colonne `{NUTRIMENT} (unité)`. Si elle est vide, l'unité par défaut sera utilisée.

## Gestion des erreurs

### Erreurs d'import courantes

1. **Nombre de colonnes incorrect**:
   - Vérifier que toutes les colonnes sont présentes
   - Utiliser le modèle d'exemple comme référence

2. **Valeurs invalides**:
   - Prix et quantités doivent être numériques
   - Booléens doivent être "true" ou "false"
   - UUID doit être une chaîne valide

3. **Références manquantes**:
   - Groupes alimentaires, types, indications doivent correspondre aux valeurs enum existantes
   - Si une valeur n'existe pas, elle sera ignorée ou remplacée par une valeur par défaut

4. **Format CSV incorrect**:
   - Utiliser le point-virgule comme séparateur
   - Échapper les guillemets doubles avec des guillemets doubles
   - Utiliser des guillemets pour les valeurs contenant des points-virgules

### Gestion des erreurs

- Les erreurs sont affichées avec le numéro de ligne concerné
- L'import continue même en cas d'erreur sur certaines lignes
- Un rapport détaillé des erreurs est fourni

## Exemples

### Aliment simple

```csv
UUID;Nom;Marque;Gamme;Ingrédients;Groupe Alimentaire;Type Aliment;Conditionnement;Prix;Catégorie Prix;Quantité Interne;Consistant;Obsolète;Espèces;Indications;UUID Ration;HUMIDITE (valeur);HUMIDITE (unité);PROTEINE (valeur);PROTEINE (unité);LIPIDE (valeur);LIPIDE (unité);ENERGIE (valeur);ENERGIE (unité);VITA (valeur);VITA (unité)
uuid-123;Croquettes Premium Chat;Royal Canin;Sterilised;Viande de poulet, riz, maïs;croquettes;complet;sachet;45.99;Premium;2.0;true;false;Chat;Stérilisé;ration-456;8.5;g;30.0;g;12.0;g;350.0;kcal;15000.0;UI
```

### Aliment avec ingrédients complexes

```csv
UUID;Nom;Marque;Gamme;Ingrédients;Groupe Alimentaire;Type Aliment;Conditionnement;Prix;Catégorie Prix;Quantité Interne;Consistant;Obsolète;Espèces;Indications;UUID Ration
uuid-789;"Croquettes ""Premium""";Royal Canin;Sterilised;"Viande de poulet (25%), riz (20%), maïs (15%), gluten de blé, huile de poisson";croquettes;complet;sachet;52.50;Premium;3.0;true;false;Chat, Chien;Stérilisé, Croissance;ration-789
```

### Aliment avec nutriments complets (aperçu)

```csv
UUID;Nom;...;CAL (valeur);CAL (unité);FE (valeur);FE (unité);TAURINE (valeur);TAURINE (unité);O3 (valeur);O3 (unité);...
uuid-1000;Aliment Complet Premium;...;12.5;g;150.0;mg;2.1;g;1.2;g;...
```

## Bonnes pratiques

### Préparation des données

1. **Utiliser le modèle**: Commencez toujours par télécharger et modifier le modèle d'exemple
2. **Validation**: Vérifiez vos données avant l'import
3. **Sauvegarde**: Exportez vos données existantes avant un import massif
4. **Tests**: Testez l'import avec quelques lignes avant un import complet

### Formatage

1. **Noms d'aliments**: Utilisez des noms descriptifs et uniques
2. **Listes**: Séparez les éléments par des virgules sans espaces
3. **Valeurs numériques**: Utilisez le point comme séparateur décimal
4. **Texte avec caractères spéciaux**: Entourez de guillemets doubles

### Maintenance

1. **Mises à jour régulières**: Exportez régulièrement vos données
2. **Archivage**: Gardez des copies des fichiers importants
3. **Documentation**: Commentez vos modifications importantes

## Extensions futures

### Améliorations possibles

1. **Support Excel natif**: Intégration directe avec les fichiers .xlsx
2. **Validation avancée**: Contrôles de cohérence des données
3. **Import partiel**: Possibilité d'importer seulement certains champs
4. **Templates personnalisés**: Création de templates spécifiques par type d'aliment
5. **Interface drag & drop**: Glisser-déposer de fichiers
6. **Synchronisation cloud**: Sauvegarde automatique dans le cloud

### Intégrations

1. **APIs externes**: Connexion avec des bases de données nutritionnelles
2. **Formats multiples**: Support JSON, XML en plus du CSV
3. **Workflows automatisés**: Intégration avec des outils d'ETL
4. **Reporting**: Génération de rapports d'import/export

---

*Ce guide est destiné à être mis à jour selon les évolutions du système.*
