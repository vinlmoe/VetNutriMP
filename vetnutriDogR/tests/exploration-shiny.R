# Run from the source checkout; exercises the server used by the QMD.
module_root <- Sys.getenv("VETNUTRI_DOG_ROOT", if (dir.exists("vetnutriDogR/R")) normalizePath("vetnutriDogR") else if (file.exists("R/ration_exploration.R")) getwd() else "")
root <- Sys.getenv("VETNUTRI_MP_ROOT", if (dir.exists("composeApp")) getwd() else if (nzchar(module_root) && dir.exists(file.path(dirname(module_root), "composeApp"))) dirname(module_root) else "")
if (nzchar(module_root) && nzchar(root) && requireNamespace("shiny", quietly = TRUE)) {
  for (file in list.files(file.path(module_root, "R"), pattern = "\\.R$", full.names = TRUE)) source(file)
  shiny::testServer(vn_exploration_server, {
    session$setInputs(explore_root = root, explore_reload = 1,
      weight_from = 5, weight_to = 10, weight_by = 5,
      k_from = 0.8, k_to = 1.2, k_by = 0.4,
      explore_variables = "{}", scenario_limit = 20, fibre_nutrient = "CELLULOSE",
      explore_status = "ALL", explore_page = 1)
    m <- model()
    ref <- m$references$reference_id[m$references$nom == "Adulte >25kg"]
    labels <- setdiff(vn_exploration_roles()$nutrient_id, "ENERGIE")
    candidates <- Filter(function(f) f$uuid %in% m$foods$food_id && all(labels %in% names(f$nutrients)) &&
      all(unlist(f$nutrients[labels]) > 0), m$raw$foods)
    inputs <- list(explore_refs = ref)
    for (role in vn_exploration_roles()$role) {
      inputs[[paste0("ingredients_", role)]] <- candidates[[1]]$uuid
      if (role != "energy") {
        inputs[[paste0("target_source_", role)]] <- "reference"
        inputs[[paste0("target_level_", role)]] <- "OPTIMIN"
        inputs[[paste0("target_factor_", role)]] <- if (role == "fibre") 5 else 1
        inputs[[paste0("target_unit_", role)]] <- "1"
      }
    }
    inputs$ingredients_protein <- vapply(candidates[1:2], `[[`, "", "uuid")
    do.call(session$setInputs, inputs)
    session$setInputs(explore_run = 1)
    out <- exploration()
    stopifnot(nrow(out$summary) == 8L, nrow(out$quantities) == 48L)
    stopifnot(nzchar(output$explore_target_preview), nzchar(output$explore_results))
    session$setInputs(explore_scenario = out$summary$scenario_id[1])
    stopifnot(is.finite(detail()$energy_kcal), nzchar(output$explore_quantities))
    # Controls can change without mutating the previously calculated result.
    session$setInputs(k_to = 2)
    stopifnot(identical(exploration()$configuration$k_values, c(0.8, 1.2)))
    stopifnot(isFALSE(exploration()$configuration$missing_as_zero))
    # The checkbox reaches the engine and the detail tab on the next run.
    session$setInputs(k_to = 1.2, explore_missing_zero = TRUE, explore_run = 2)
    out <- exploration()
    stopifnot(isTRUE(out$configuration$missing_as_zero), "zero_filled" %in% names(out$summary))
    session$setInputs(explore_scenario = out$summary$scenario_id[1])
    stopifnot(isTRUE(detail()$missing_as_zero), nzchar(output$explore_summary), nzchar(output$explore_comparison))
  })
  cat("QMD exploration : listes, grille, cibles, calcul, détail et conservation des résultats validés\n")
} else message("Exploration Shiny integration skipped: source checkout and shiny required")
