# Guide JSON d'import pour des conseils predefinis

Ce document explique comment construire un fichier JSON importable dans VetNutri contenant des conseils predefinis.

## Principe

Le fichier doit suivre le format `ApiEnvelope` de l'application, meme si vous ne voulez importer que des conseils.

Concretement :
- le JSON racine est un objet
- les conseils sont dans la cle `conseils`
- les autres collections doivent etre presentes, en general vides

## Structure minimale du fichier

```json
{
  "version": "1.0.0",
  "generatedAtEpochMs": 1760000000000,
  "animals": [],
  "foods": [],
  "rations": [],
  "recipes": [],
  "equations": [],
  "biblioRefs": [],
  "references": [],
  "conseils": [],
  "consultationKeywords": []
}
```

## Structure d'un conseil

Chaque entree du tableau `conseils` suit ce modele :

```json
{
  "id": "conseil_transition_7j",
  "title": "Transition alimentaire 7 jours",
  "content": {
    "blocks": []
  },
  "category": "CONSEIL_NUTRITIONNEL",
  "tags": ["transition", "alimentation"],
  "priority": 0,
  "isActive": true,
  "targetSpecies": [],
  "targetAgeGroups": [],
  "usageCount": 0,
  "isTemplate": false,
  "createdAt": 1760000000000,
  "updatedAt": 1760000000000
}
```

## Champs utiles

- `id` : identifiant unique stable. Eviter les espaces.
- `title` : titre affiche dans l'application.
- `content.blocks` : contenu du conseil.
- `category` : categorie exacte attendue par l'application.
- `tags` : mots-cles pour la recherche.
- `priority` : priorite d'affichage.
- `isActive` : `true` pour rendre le conseil disponible.
- `targetSpecies` : filtrage par espece. Le plus sur est `[]` pour toutes les especes.
- `targetAgeGroups` : filtrage par age. Le plus sur est `[]` si vous ne filtrez pas.
- `usageCount` : compteur d'utilisation, generalement `0` a l'import.
- `isTemplate` : laisser `false` pour un conseil standard.
- `createdAt` / `updatedAt` : timestamp Unix en millisecondes.

## Categories valides

Utiliser exactement une de ces valeurs :

- `CONSEILS`
- `CONSEIL_NUTRITIONNEL`
- `CONSEIL_HYGIENE`
- `CONSEIL_COMPORTEMENT`
- `CONSEIL_SANTE`
- `CONSEIL_REPRODUCTION`
- `CONSEIL_ALIMENTATION`
- `CONSEIL_ACTIVITE`
- `CONSEIL_PREVENTION`

## Format des blocs `content.blocks`

Le contenu est une liste de blocs. Chaque bloc doit contenir un champ `type` exact.

Types supportes :
- `fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Paragraph`
- `fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Heading`
- `fr.vetbrain.vetnutri_mp.Data.TextBlockApi.ListBlock`
- `fr.vetbrain.vetnutri_mp.Data.TextBlockApi.TableBlock`
- `fr.vetbrain.vetnutri_mp.Data.TextBlockApi.RawHtml`

### Exemple de paragraphe

```json
{
  "type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Paragraph",
  "id": "p1",
  "text": "Introduire progressivement le nouvel aliment sur 7 jours.",
  "formatting": {
    "isBold": false,
    "isItalic": false,
    "isUnderline": false,
    "isStrikethrough": false,
    "alignment": "LEFT"
  }
}
```

### Exemple de titre

```json
{
  "type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Heading",
  "id": "h1",
  "level": 1,
  "text": "Transition alimentaire"
}
```

### Exemple de liste

```json
{
  "type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.ListBlock",
  "id": "l1",
  "items": [
    "Jours 1 a 2 : 75% ancien / 25% nouveau",
    "Jours 3 a 4 : 50% / 50%",
    "Jours 5 a 6 : 25% ancien / 75% nouveau",
    "Jour 7 : 100% nouveau"
  ],
  "isOrdered": false
}
```

## Exemple complet pret a importer

```json
{
  "version": "1.0.0",
  "generatedAtEpochMs": 1760000000000,
  "animals": [],
  "foods": [],
  "rations": [],
  "recipes": [],
  "equations": [],
  "biblioRefs": [],
  "references": [],
  "conseils": [
    {
      "id": "conseil_transition_7j",
      "title": "Transition alimentaire 7 jours",
      "content": {
        "blocks": [
          {
            "type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Heading",
            "id": "h1",
            "level": 1,
            "text": "Transition alimentaire"
          },
          {
            "type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Paragraph",
            "id": "p1",
            "text": "Introduire progressivement le nouvel aliment pour limiter les troubles digestifs.",
            "formatting": {
              "isBold": false,
              "isItalic": false,
              "isUnderline": false,
              "isStrikethrough": false,
              "alignment": "LEFT"
            }
          },
          {
            "type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.ListBlock",
            "id": "l1",
            "items": [
              "Jours 1 a 2 : 75% ancien / 25% nouveau",
              "Jours 3 a 4 : 50% / 50%",
              "Jours 5 a 6 : 25% ancien / 75% nouveau",
              "Jour 7 : 100% nouveau"
            ],
            "isOrdered": false
          }
        ]
      },
      "category": "CONSEIL_NUTRITIONNEL",
      "tags": ["transition", "digestif"],
      "priority": 1,
      "isActive": true,
      "targetSpecies": [],
      "targetAgeGroups": [],
      "usageCount": 0,
      "isTemplate": false,
      "createdAt": 1760000000000,
      "updatedAt": 1760000000000
    },
    {
      "id": "conseil_surveillance_digestive",
      "title": "Surveillance digestive",
      "content": {
        "blocks": [
          {
            "type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Paragraph",
            "id": "p2",
            "text": "En cas de vomissements, diarrhee ou refus alimentaire, recontacter le veterinaire.",
            "formatting": {
              "isBold": false,
              "isItalic": false,
              "isUnderline": false,
              "isStrikethrough": false,
              "alignment": "LEFT"
            }
          }
        ]
      },
      "category": "CONSEIL_SANTE",
      "tags": ["surveillance"],
      "priority": 0,
      "isActive": true,
      "targetSpecies": [],
      "targetAgeGroups": [],
      "usageCount": 0,
      "isTemplate": false,
      "createdAt": 1760000000000,
      "updatedAt": 1760000000000
    }
  ],
  "consultationKeywords": []
}
```

## Recommandations pratiques

- Garder `animals`, `foods`, `rations`, `recipes`, `equations`, `biblioRefs`, `references` et `consultationKeywords` a `[]` si vous importez seulement des conseils.
- Ne pas changer l'orthographe des valeurs `category` et `type`.
- Utiliser des `id` uniques pour eviter d'ecraser un conseil existant.
- Preferer `targetSpecies: []` et `targetAgeGroups: []` tant que vous n'avez pas valide les codes de filtrage exacts utilises dans votre base.
- Le JSON standard n'accepte pas les commentaires.

## Reference technique

Le format est aligne sur ces structures du projet :
- `ApiEnvelope`
- `ConseilApi`
- `RichTextContentApi`
- `TextBlockApi`

