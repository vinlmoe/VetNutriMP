library(vetnutriExamR)

result <- vn_grade_from_json(
  submissions_json = "inst/examples/submissions.json",
  answer_key_json = "inst/examples/answer_key.json",
  scale_to = 20,
  output_csv = "notes.csv"
)

print(result$scores)
