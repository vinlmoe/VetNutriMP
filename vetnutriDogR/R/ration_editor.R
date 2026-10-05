# Manual editing of a ration taken from an exploration cell (reference × weight × K).

#' Evaluate an edited ration for one reference, weight and K.
#' `items` holds food_id and quantity_g (role and food_name are kept when present).
#' The status follows the exploration rules; an energy gap beyond
#' `energy_tolerance_pct` of the need is reported in either direction.
#' @export
vn_evaluate_edited_ration <- function(model, reference_id, weight_kg, K, items, variables = list(),
                                      missing_as_zero = FALSE, ignore_levels = character(), nutrients = NULL,
                                      energy_tolerance_pct = 2) {
  if (length(energy_tolerance_pct) != 1L || !is.finite(energy_tolerance_pct) || energy_tolerance_pct < 0)
    stop("Tolérance énergétique invalide")
  needs <- vn_init_needs(model, reference_id, weight_kg, variables = variables, adjustment = K)
  result <- vn_init_ration(model, reference_id, items[, c("food_id", "quantity_g")], needs,
    missing_as_zero = missing_as_zero, ignore_levels = ignore_levels, nutrients = nutrients)
  cmp <- result$comparison
  failed <- cmp$status %in% c("INSUFFISANT", "EXCES")
  filled <- result$zero_filled
  documented <- failed & !cmp$nutrient_id %in% filled
  gap <- result$energy_kcal - needs$need_kcal
  tolerance <- energy_tolerance_pct / 100 * needs$need_kcal
  missing <- sum(cmp$status == "DONNEES_ABSENTES")
  status <- if (gap > tolerance) "ENERGIE_DEPASSEE" else if (gap < -tolerance) "ENERGIE_INSUFFISANTE" else
    if (missing > 0) "DONNEES_INCOMPLETES" else if (any(failed)) "SEUILS_NON_RESPECTES" else
    if (length(filled)) "CONFORME_ABSENTS_A_ZERO" else "CONFORME"
  used <- items[items$quantity_g > 0, , drop = FALSE]
  names_used <- if ("food_name" %in% names(used)) used$food_name else model$foods$name[match(used$food_id, model$foods$food_id)]
  summary <- data.frame(reference_id = reference_id,
    reference_name = model$references$nom[match(reference_id, model$references$reference_id)],
    weight_kg = weight_kg, K = K, need_kcal = needs$need_kcal, energy_kcal = result$energy_kcal,
    energy_gap_kcal = gap, energy_gap_percent = 100 * gap / needs$need_kcal,
    quantity_total_g = sum(used$quantity_g), insufficient = sum(cmp$status == "INSUFFISANT"),
    excess = sum(cmp$status == "EXCES"), missing = missing,
    not_covered = paste(unique(cmp$nutrient_id[cmp$status == "INSUFFISANT"]), collapse = ","),
    in_excess = paste(unique(cmp$nutrient_id[cmp$status == "EXCES"]), collapse = ","),
    violated_documented = paste(unique(paste(cmp$nutrient_id[documented], cmp$reflevel[documented])), collapse = ";"),
    zero_filled_nutrients = paste(filled, collapse = ","),
    composition = paste(sprintf("%s : %s g", names_used, as.character(round(used$quantity_g, 1))), collapse = " + "),
    status = status, stringsAsFactors = FALSE)
  list(summary = summary, comparison = cmp, result = result)
}

#' Flatten saved edited rations into export tables.
#' Each ration is a list with id, label, source_scenario_id, items and evaluation.
#' @export
vn_edited_rations_tables <- function(rations) {
  if (!length(rations)) return(list(summary = data.frame(), quantities = data.frame(), comparison = data.frame()))
  meta <- function(r) data.frame(ration_id = r$id, label = r$label, source_scenario_id = r$source_scenario_id,
    stringsAsFactors = FALSE)
  list(
    summary = do.call(rbind, lapply(rations, function(r) cbind(meta(r), r$evaluation$summary))),
    quantities = do.call(rbind, lapply(rations, function(r) {
      items <- r$items[r$items$quantity_g > 0, c("role", "food_id", "food_name", "quantity_g"), drop = FALSE]
      cbind(meta(r)[rep(1L, nrow(items)), , drop = FALSE], items, row.names = NULL)
    })),
    comparison = do.call(rbind, lapply(rations, function(r) {
      cmp <- r$evaluation$comparison
      cbind(meta(r)[rep(1L, nrow(cmp)), , drop = FALSE], cmp, row.names = NULL)
    })))
}

#' Exportable JSON payload of saved edited rations (meta, configuration, quantities).
#' @export
vn_edited_rations_json <- function(rations) {
  list(version = 1L, rations = lapply(unname(rations), function(r) list(
    ration_id = r$id, label = r$label, source_scenario_id = r$source_scenario_id,
    reference_id = r$evaluation$summary$reference_id, reference_name = r$evaluation$summary$reference_name,
    weight_kg = r$evaluation$summary$weight_kg, K = r$evaluation$summary$K,
    status = r$evaluation$summary$status, need_kcal = r$evaluation$summary$need_kcal,
    energy_kcal = r$evaluation$summary$energy_kcal, configuration = r$configuration,
    items = r$items[r$items$quantity_g > 0, c("role", "food_id", "food_name", "quantity_g"), drop = FALSE])))
}

# Cell (reference, weight, K) under a click on a single-reference balance map.
vn_balance_cell_at <- function(balance, x, y) {
  if (is.null(x) || is.null(y) || !nrow(balance)) return(NULL)
  w <- sort(unique(balance$weight_kg)); k <- sort(unique(balance$K))
  i <- findInterval(x, vn_tile_edges(w), rightmost.closed = TRUE)
  j <- findInterval(y, vn_tile_edges(k), rightmost.closed = TRUE)
  if (i < 1L || i > length(w) || j < 1L || j > length(k)) return(NULL)
  cell <- balance[balance$weight_kg == w[i] & balance$K == k[j], , drop = FALSE]
  if (nrow(cell) == 1L) cell else NULL
}
