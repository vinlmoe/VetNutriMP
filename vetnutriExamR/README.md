# vetnutriExamR

Package R pour corriger des examens VetNutri a partir de fichiers JSON.

## Installation locale

```r
install.packages("jsonlite")
# Depuis la racine du projet VetNutriMP:
# devtools::install("vetnutriExamR")
```

## Formats JSON supportes

1. **Copie unique**

```json
{
  "answers": {
    "Q1": "A",
    "Q2": 12.5
  }
}
```

2. **Copies multiples**

```json
{
  "submissions": [
    {"student_id": "etu_01", "answers": {"Q1": "A", "Q2": 10}},
    {"student_id": "etu_02", "answers": {"Q1": "B", "Q2": 12.5}}
  ]
}
```

3. **Export VetNutri (nutrientRequirements)**

Les questions sont construites automatiquement avec:
`nutrient::referenceLevel::unitRequirement`

## Bareme JSON

- Format simple (liste nommee):

```json
{
  "Q1": "A",
  "Q2": 12.5
}
```

- Format detaille (points, tolerance):

```json
[
  {"question": "Q1", "answer": "A", "points": 2},
  {"question": "Q2", "answer": 12.5, "points": 3, "tolerance": 0.1}
]
```

## Utilisation

```r
library(vetnutriExamR)

res <- vn_grade_from_json(
  submissions_json = "inst/examples/submissions.json",
  answer_key_json = "inst/examples/answer_key.json",
  scale_to = 20,
  output_csv = "notes.csv"
)

print(res$scores)
```

## Calculs nutritionnels VetNutri (besoins + apports + references)

```r
library(jsonlite)
library(vetnutriExamR)

x <- fromJSON("inst/examples/vetnutri_exam_example.json", simplifyVector = FALSE)

# 1) Besoin energetique par consultation
needs <- vn_compute_energy_needs(x)
print(needs[, c("animal_name", "consultation_id", "need_kcal")])

# 2) Apports par ration (somme des nutriments et energie)
intakes <- vn_compute_ration_intakes(x)
print(intakes$ration_energy)

# 3) Comparaison aux references (energie + nutriments)
cmp <- vn_compare_to_references(x)
print(cmp$energy_balance)
print(head(cmp$nutrient_comparison))
```

Compatibilite:
- nouveau schema VetNutri: `animals -> consultations -> rations -> items` + `foods`, `equations`, `references`
- ancien schema: `consultations/rationList/alimentList` (support de base conserve)

## Import d'une liste de JSON + module de notation

```r
library(vetnutriExamR)

# chemins locaux et/ou URLs
sources <- c(
  copie_01 = "inst/examples/vetnutri_exam_example.json",
  copie_02 = "inst/examples/vetnutri_exam_example.json"
)

# 1) Import batch (diagnostic des erreurs d'import)
imp <- vn_import_json_list(sources)
print(length(imp$items))
print(imp$errors)

# 2) Notation nutrition par copie/animal
batch <- vn_grade_nutrition_batch(
  sources = sources,
  scale_to = 20,
  energy_tolerance_pct = 30,
  weights = c(energy = 0.4, minimum = 0.4, maximum = 0.2),
  api_key = Sys.getenv("JSONBIN_API_KEY", unset = ""),
  output_csv = "notes_nutrition.csv"
)

print(batch$scores)
print(batch$imports_errors)
```

Colonnes utiles de `batch$scores`:
- `grade`: note finale / 20
- `minimum_score_unit`, `maximum_score_unit`: respect min/max
- `min_below`, `max_above`: nombre d'ecarts
- `exam_student_id`, `exam_student_number`, `exam_exercise_id` (si presents dans le JSON)
