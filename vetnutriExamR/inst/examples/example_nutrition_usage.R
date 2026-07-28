library(jsonlite)
library(vetnutriExamR)

x <- fromJSON("inst/examples/vetnutri_exam_example.json", simplifyVector = FALSE)

needs <- vn_compute_energy_needs(x)
print(needs[, c("animal_name", "consultation_id", "need_kcal")])

intakes <- vn_compute_ration_intakes(x)
print(intakes$ration_energy)

cmp <- vn_compare_to_references(x)
print(cmp$energy_balance[, c("consultation_id", "energy_intake_kcal", "need_kcal", "coverage_ratio")])
print(head(cmp$nutrient_comparison))
