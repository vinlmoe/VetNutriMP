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
    # Before selection, the run block lists what is missing instead of failing later.
    stopifnot(any(grepl("au moins un référentiel", run_problems())), any(grepl("^Listes vides", run_problems())))
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
    stopifnot(length(run_problems()) == 0L, scenario_count() == 8L)
    session$setInputs(explore_run = 1)
    out <- exploration()
    stopifnot(nrow(out$summary) == 8L, nrow(out$quantities) == 48L)
    stopifnot(page_count() == 1L, grepl("8 scénarios", output$explore_page_info), is.null(output$explore_stale$html) ||
      !grepl("Paramètres modifiés", output$explore_stale$html))
    session$setInputs(explore_scenario = out$summary$scenario_id[1], explore_scenario_next = 1)
    session$setInputs(weight_by = 2.5)
    stopifnot(grepl("Paramètres modifiés", output$explore_stale$html))
    session$setInputs(weight_by = 5)
    stopifnot(!grepl("Paramètres modifiés", paste(output$explore_stale$html)))
    # Over the limit or invalid JSON: listed before the run, and the run reports it instead of crashing.
    session$setInputs(scenario_limit = 4, explore_variables = "{AW")
    stopifnot(any(grepl("dépassent la limite", run_problems())), any(grepl("JSON invalide", run_problems())))
    session$setInputs(scenario_limit = 20, explore_variables = "{}")
    stopifnot(nzchar(output$explore_target_preview), nzchar(output$explore_results))
    session$setInputs(explore_scenario = out$summary$scenario_id[1])
    stopifnot(is.finite(detail()$energy_kcal), nzchar(output$explore_quantities))
    # Controls can change without mutating the previously calculated result.
    session$setInputs(k_to = 2)
    stopifnot(identical(exploration()$configuration$k_values, c(0.8, 1.2)))
    stopifnot(isFALSE(exploration()$configuration$missing_as_zero),
      identical(exploration()$configuration$ignore_levels, "OPTIMAX"),
      isTRUE(exploration()$configuration$rounding), all(unlist(exploration()$configuration$min_dose_g) == 5))
    # The checkbox reaches the engine and the detail tab on the next run.
    session$setInputs(k_to = 1.2, explore_missing_zero = TRUE, min_dose_calcium = 12, explore_run = 2)
    out <- exploration()
    stopifnot(unlist(out$configuration$min_dose_g)[["calcium"]] == 12, unlist(out$configuration$min_dose_g)[["energy"]] == 5)
    stopifnot(isTRUE(out$configuration$missing_as_zero), "zero_filled" %in% names(out$summary))
    session$setInputs(explore_scenario = out$summary$scenario_id[1])
    stopifnot(isTRUE(detail()$missing_as_zero), nzchar(output$explore_summary), nzchar(output$explore_comparison),
      all(c("Attendu", "Observé", "Écart (observé − attendu)") %in% names(comparison_display())))
    stopifnot(nzchar(output$explore_uncovered), is.null(out$configuration$nutrients))
    session$setInputs(explore_curve_reference = ref, explore_curve_combination = out$summary$combination_id[1],
      explore_curve_weight = "5", explore_curve_k = "0.8", explore_curve_nutrients = "CAP")
    curves <- curve_data()
    stopifnot(nrow(curves) == 24L, all(c("weight_kg", "K", "series", "quantity_g") %in% names(curves)))
    cap_by_k <- unique(curves[curves$weight_kg == 5, c("scenario_id", "weight_kg", "K")])
    cap_by_k <- nutrient_curve_data(cap_by_k)
    stopifnot(nrow(cap_by_k) == 2L, all(cap_by_k$nutrient_id == "CAP"), all(is.finite(cap_by_k$intake)))
    # Nutrient selection: only the retained nutrients are evaluated; "Tout décocher" empties it.
    session$setInputs(explore_all_nutrients = FALSE, explore_nutrients = c("PROTEINE", "CAL"), explore_run = 3)
    stopifnot(identical(exploration()$configuration$nutrients, c("PROTEINE", "CAL")), length(nutrient_choices()) > 20,
      all(unlist(strsplit(exploration()$summary$violated, ";")) %in% c("PROTEINE MIN", "PROTEINE OPTIMIN", "CAL MIN", "CAL OPTIMIN", "CAL MAX", "PROTEINE MAX")))
    session$setInputs(explore_nutrients = NULL, explore_run = 4)
    stopifnot(identical(exploration()$configuration$nutrients, character()))
    session$setInputs(explore_adjust_cap = TRUE, cap_increment_g = 2, explore_run = 5)
    stopifnot(isTRUE(exploration()$configuration$adjust_cap), exploration()$configuration$cap_increment_g == 2,
      "cap_adjustment_g" %in% names(exploration()$summary))
    session$setInputs(explore_balance_all = TRUE)
    stopifnot(nrow(balance()) == 4L, !is.null(output$explore_balance_maps), nzchar(output$explore_balance_table))
    # Click on the weight × K map: the cell opens in the editor, edits are re-evaluated, saved and exported.
    stopifnot(is.null(vn_balance_cell_at(balance(), 100, 0.8)))
    session$setInputs(explore_balance_click_1 = list(x = 10.4, y = 1.15))
    stopifnot(editor$cell$weight_kg == 10, editor$cell$K == 1.2, nrow(editor$items) == 6L)
    session$elapse(500)
    before <- edited_evaluation()$summary
    stopifnot(before$weight_kg == 10, before$K == 1.2, is.finite(before$energy_kcal))
    v <- editor$version
    do.call(session$setInputs, setNames(list(editor$items$quantity_g[1] + 50), paste0("edit_q_", v, "_1")))
    session$elapse(500)
    after <- edited_evaluation()$summary
    stopifnot(after$energy_kcal > before$energy_kcal, after$quantity_total_g > before$quantity_total_g)
    session$setInputs(edit_add_food = candidates[[2]]$uuid, edit_add_qty = 12, edit_add = 1)
    stopifnot(nrow(editor$items) == 7L, editor$items$quantity_g[7] == 12, editor$items$role[7] == "Ajout")
    session$setInputs(edit_label = "Essai 10 kg", edit_save = 1)
    saved <- edited_rations()
    stopifnot(length(saved) == 1L, names(saved) == "R001", saved$R001$label == "Essai 10 kg",
      sum(saved$R001$items$quantity_g) == after$quantity_total_g + 12, nzchar(output$edited_rations_table))
    tables <- vn_edited_rations_tables(saved)
    stopifnot(nrow(tables$summary) == 1L, nrow(tables$quantities) == sum(saved$R001$items$quantity_g > 0), identical(exploration()$configuration$nutrients, character()))
    payload <- jsonlite::fromJSON(jsonlite::toJSON(vn_edited_rations_json(saved), auto_unbox = TRUE, dataframe = "rows", digits = NA))
    stopifnot(payload$rations$ration_id == "R001", payload$rations$weight_kg == 10)
    # Saving again from the same editor updates the ration instead of adding one.
    session$setInputs(edit_save = 2)
    stopifnot(length(edited_rations()) == 1L)
    session$setInputs(edited_ration_pick = "R001", edited_ration_open = 1)
    stopifnot(editor$ration_id == "R001", identical(editor$items, saved$R001$items))
    session$setInputs(edited_ration_delete = 1)
    stopifnot(length(edited_rations()) == 0L)
    session$setInputs(scenario_limit = 4, explore_run = 6)
    stopifnot(grepl("Calcul impossible", tryCatch({ exploration(); "" }, error = conditionMessage)))
  })
  cat("QMD exploration : listes, grille, cibles, calcul, détail et conservation des résultats validés\n")
} else message("Exploration Shiny integration skipped: source checkout and shiny required")
