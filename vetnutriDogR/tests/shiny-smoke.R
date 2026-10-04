# Runs with the module sources or its installed package and a live checkout.
root <- Sys.getenv("VETNUTRI_MP_ROOT", if (dir.exists("composeApp")) getwd() else "")
module_root <- Sys.getenv("VETNUTRI_DOG_ROOT", if (dir.exists("vetnutriDogR/R"))
  normalizePath("vetnutriDogR") else if (file.exists("R/init_json_loader.R")) getwd() else "")
if (!nzchar(root) && nzchar(module_root) && dir.exists(file.path(dirname(module_root), "composeApp")))
  root <- dirname(module_root)
if (nzchar(root) && requireNamespace("shiny", quietly = TRUE) &&
    (nzchar(module_root) || requireNamespace("vetnutriDogR", quietly = TRUE))) {
  Sys.setenv(VETNUTRI_MP_ROOT = root)
  app <- new.env()
  old_options <- options(vetnutriDogR.source_mode = nzchar(module_root))
  if (nzchar(module_root)) {
    for (file in list.files(file.path(module_root, "R"), pattern = "\\.R$", full.names = TRUE)) source(file)
    app_file <- file.path(module_root, "inst/shiny/app.R")
  } else app_file <- system.file("shiny/app.R", package = "vetnutriDogR")
  sys.source(app_file, envir = app)
  shiny::testServer(app$server, {
    session$setInputs(root = root, reload = 1, weight = 10,
      ideal = 0, adjustment = 1, variables = "{}")
    m <- model()
    ref <- m$references$reference_id[m$references$nom == "Calcul sur RER"][1]
    session$setInputs(reference = ref, food = m$foods$food_id[1], quantity = 4)
    session$setInputs(add = 1)
    stopifnot(nrow(ration()) == 1L, result()$energy_kcal > 0,
              all(m$references$espece == "CHIEN"), is.data.frame(result()$diagnostics))
    session$setInputs(clear = 1)
    stopifnot(nrow(ration()) == 0L)
  })
  options(old_options)
  stopifnot(!"vetnutriExamR" %in% loadedNamespaces())
  cat("Shiny canine autonome: OK\n")
} else message("Shiny integration test skipped: install package/shiny and set VETNUTRI_MP_ROOT")
