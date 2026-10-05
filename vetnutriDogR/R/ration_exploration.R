# The six slots are adjustment roles, not a second food/nutrition database.
vn_exploration_roles <- function() {
  data.frame(role = c("protein", "fibre", "calcium", "omega6", "sodium", "energy"),
    label = c("Protéines", "Fibres", "Calcium", "Oméga-6", "Sel / sodium", "Énergie restante"),
    nutrient_id = c("PROTEINE", "CELLULOSE", "CAL", "O6", "NA", "ENERGIE"))
}

#' Make an inclusive sequence for a weight or K grid.
#' @export
vn_exploration_interval <- function(from, to, by) {
  x <- c(from, to, by)
  if (length(x) != 3L || any(!is.finite(x)) || from <= 0 || to < from || by <= 0)
    stop("Intervalle invalide : bornes positives, maximum ≥ minimum, pas positif")
  if ((to - from) / by > 200000) stop("Intervalle trop fin")
  out <- seq(from, to, by = by)
  if (tail(out, 1L) < to - 1e-10 * max(1, to)) out <- c(out, to)
  unique(out)
}

#' Default adjustment targets: OPTIMIN and the explicit Kotlin cellulose factor.
#' @export
vn_exploration_targets <- function() {
  roles <- vn_exploration_roles()
  data.frame(role = roles$role, nutrient_id = roles$nutrient_id,
    source = c(rep("reference", 5), "energy"), reflevel = c(rep("OPTIMIN", 5), NA_character_),
    value = NA_real_, uniteReqId = c(rep(NA_integer_, 5), 6L), multiplier = c(1, 5, 1, 1, 1, 1))
}

vn_exploration_error <- function(code, message) {
  stop(structure(list(message = message, call = NULL, code = code),
                 class = c("vn_exploration_error", "error", "condition")))
}

vn_absolute_target <- function(value, unit, needs) {
  factor <- switch(as.character(unit), `0` = needs$weight_kg,
    `1` = needs$standard_kcal / 1000, `2` = needs$metabolic_weight,
    `4` = needs$standard_kcal * 4.184 / 1000, `6` = 1, NA_real_)
  value * factor
}

vn_resolve_exploration_targets <- function(model, reference_id, targets, needs) {
  out <- targets
  out$absolute_target <- NA_real_
  out$reference_value <- NA_real_
  out$source_pointer <- NA_character_
  out$source_json <- model$provenance$source_json
  out$unit <- model$dictionaries$nutrients$unit[match(out$nutrient_id, model$dictionaries$nutrients$nutrient_id)]
  for (i in seq_len(nrow(out))) {
    if (out$source[i] == "energy") {
      out$absolute_target[i] <- needs$need_kcal
      out$reference_value[i] <- needs$need_kcal
      next
    }
    if (out$source[i] == "reference") {
      rows <- model$requirements[model$requirements$reference_id == reference_id &
        model$requirements$nutrient_id == out$nutrient_id[i] &
        model$requirements$reflevel == out$reflevel[i] & model$requirements$is_effective, ]
      if (nrow(rows) != 1L) vn_exploration_error("CIBLE_ABSENTE",
        paste("Seuil absent :", out$nutrient_id[i], out$reflevel[i], "—", reference_id))
      out$value[i] <- rows$quantity
      out$uniteReqId[i] <- rows$uniteReqId
      out$source_pointer[i] <- rows$source_pointer
    }
    out$reference_value[i] <- out$value[i]
    out$absolute_target[i] <- vn_absolute_target(out$value[i], out$uniteReqId[i], needs) * out$multiplier[i]
    if (!is.finite(out$absolute_target[i]) || out$absolute_target[i] < 0)
      vn_exploration_error("CIBLE_INVALIDE", paste("Cible non calculable :", out$nutrient_id[i]))
  }
  out$unit_requirement <- model$dictionaries$requirement_units$label[match(out$uniteReqId, model$dictionaries$requirement_units$uniteReqId)]
  out
}

# Container rule of arrondirQuantiteSelonRegles: half a unit (presentation ≠ NO,
# presentationQuantity > 0; INIT "YES" is mapped to CAN as in ApiModels).
vn_container_step <- function(food) {
  cont <- food$presentation; size <- food$presentationQuantity
  if (is.null(cont) || !nzchar(cont) || identical(cont, "NO") || is.null(size) || !is.finite(size) || size <= 0) NA_real_ else size / 2
}

# Step used by VetNutri MP for a quantity: container half-unit, else 1 g / 5 g / 25 g.
vn_quantity_step <- function(q, container_step = NA_real_) {
  if (!is.na(container_step)) container_step else if (q < 20) 1 else if (q < 200) 5 else 25
}

# One minimum dose per ingredient role: a single value applies to all six roles.
vn_role_min_doses <- function(min_dose_g) {
  roles <- vn_exploration_roles()$role
  if (!is.numeric(min_dose_g) || anyNA(min_dose_g) || any(!is.finite(min_dose_g) | min_dose_g < 0))
    stop("Dose minimale invalide : valeurs positives ou nulles")
  if (length(min_dose_g) == 1L && is.null(names(min_dose_g))) return(setNames(rep(min_dose_g, length(roles)), roles))
  if (is.null(names(min_dose_g)) || !setequal(names(min_dose_g), roles) || anyDuplicated(names(min_dose_g)))
    stop("Dose minimale : une valeur, ou une valeur nommée par rôle (", paste(roles, collapse = ", "), ")")
  min_dose_g[roles]
}

vn_min_dose <- function(container_step, min_dose_g) {
  if (min_dose_g <= 0) 0 else if (!is.na(container_step)) ceiling(min_dose_g / container_step - 1e-9) * container_step else min_dose_g
}

#' Round a quantity as VetNutri MP (arrondirQuantiteSelonRegles), with a minimum dose.
#' A used ingredient weighs at least min_dose_g (for containers, the first multiple of
#' the half-unit reaching it); below half that minimum it is not used.
#' @export
vn_round_quantity <- function(q, container_step = NA_real_, min_dose_g = 5) {
  if (length(q) != 1L || !is.finite(q)) stop("Quantité invalide")
  if (q <= 0) return(0)
  minimum <- vn_min_dose(container_step, min_dose_g)
  if (q < minimum) return(if (q >= minimum / 2) minimum else 0)
  step <- vn_quantity_step(q, container_step)
  max(0, round(q / step) * step) # R and Kotlin round ties to even.
}

# Build the composition once for each selected food/reference, not once per dog.
vn_exploration_profiles <- function(model, reference_id, food_ids) {
  ref <- vn_init_object(model, "references", reference_id)
  ids <- model$dictionaries$nutrients$nutrient_id
  profiles <- lapply(food_ids, function(id) {
    messages <- character()
    tryCatch(withCallingHandlers({
      food <- vn_init_object(model, "foods", id)
      if (!vn_food_for_dog(food)) stop("Aliment hors périmètre chien")
      values <- vn_food_values(model, food, ref)
      energy <- vn_food_energy(model, food, ref, values)
      values$values["ENERGIE"] <- energy$value
      list(values = setNames(unname(values$values[ids]), ids) / 100,
           container_step = vn_container_step(food), messages = messages, error = NULL)
    }, warning = function(w) {
      messages <<- c(messages, conditionMessage(w))
      invokeRestart("muffleWarning")
    }), error = function(e) list(values = NULL, messages = messages, error = conditionMessage(e)))
  })
  setNames(profiles, food_ids)
}

# Deficit/density adjustment, as in ajusterAlimentsPourNutriment.
# Order: protein, fibre, calcium, omega6, sodium, then the remaining energy.
# missing_as_zero reproduces Kotlin `valMap[n]?.value ?: 0.0`; the substitutions are returned.
# rounding rounds each new quantity as Kotlin does after every step, so later steps use it.
vn_adjust_combination <- function(profiles, selection, targets, tolerance = 1e-8, missing_as_zero = FALSE,
                                  rounding = FALSE, min_dose_g = 0) {
  quantities <- setNames(rep(0, length(selection)), names(selection))
  matrix <- do.call(cbind, lapply(selection, function(id) {
    profile <- profiles[[id]]
    if (!is.null(profile$error)) vn_exploration_error("COMPOSITION_INVALIDE", paste(id, profile$error))
    profile$values
  }))
  colnames(matrix) <- names(selection)
  absent <- is.na(matrix)
  if (missing_as_zero) matrix[absent] <- 0
  for (i in seq_along(selection)) {
    nutrient <- targets$nutrient_id[i]
    density <- matrix[nutrient, i]
    active <- quantities > 0
    known <- matrix[nutrient, active]
    if (anyNA(known)) vn_exploration_error("COMPOSITION_ABSENTE",
      paste("Composition absente pour", nutrient, "dans", paste(selection[active][is.na(known)], collapse = ", ")))
    intake <- sum(known * quantities[active])
    deficit <- targets$absolute_target[i] - intake
    if (deficit > tolerance * max(1, targets$absolute_target[i])) {
      if (is.na(density)) vn_exploration_error("COMPOSITION_ABSENTE", paste(selection[i], ":", nutrient, "absent"))
      if (density <= 0) vn_exploration_error("INGREDIENT_INADAPTE", paste(selection[i], ": densité nulle pour", nutrient))
      quantities[i] <- quantities[i] + deficit / density
      if (rounding) quantities[i] <- vn_round_quantity(quantities[i], vn_profile_step(profiles[[selection[i]]]),
        if (length(min_dose_g) > 1L) min_dose_g[[i]] else min_dose_g)
    }
  }
  # Energy accepted around the need: half a rounding step of the energy ingredient.
  energy_density <- matrix["ENERGIE", length(selection)]
  energy_step <- if (!rounding) 0 else {
    q <- quantities[length(selection)]
    cs <- vn_profile_step(profiles[[selection[length(selection)]]])
    # At or below the minimum dose, rounding moved up to half of that dose.
    minimum <- vn_min_dose(cs, if (length(min_dose_g) > 1L) min_dose_g[[length(selection)]] else min_dose_g)
    max(vn_quantity_step(q, cs), if (q <= minimum) minimum else 0)
  }
  energy_tolerance <- if (is.finite(energy_density)) 0.5 * energy_step * energy_density else 0
  active <- quantities > 0
  totals <- if (any(active)) rowSums(sweep(matrix[, active, drop = FALSE], 2, quantities[active], `*`)) else
    setNames(rep(0, nrow(matrix)), rownames(matrix))
  used <- quantities > 0
  zero_filled <- if (missing_as_zero && any(used))
    rownames(matrix)[rowSums(absent[, used, drop = FALSE]) > 0] else character()
  list(quantities = quantities, totals = totals, zero_filled = zero_filled, energy_tolerance_kcal = energy_tolerance,
       messages = unique(unlist(lapply(unique(selection), function(id) profiles[[id]]$messages))))
}

# Optional terminal Ca/P correction. It runs after energy and only increases
# the ingredient selected for the calcium role; the other five doses are kept.
vn_adjust_cap_terminal <- function(model, reference_id, profiles, selection, fit, needs,
                                   ignore_levels, nutrients, increment_g, missing_as_zero,
                                   max_iterations = 10000L) {
  if (!is.finite(increment_g) || increment_g <= 0) stop("Incrément Ca/P invalide")
  compare <- function(totals) vn_compare_totals(model, reference_id, totals, needs, ignore_levels, nutrients)$comparison
  lower_cap <- function(cmp) cmp[cmp$nutrient_id == "CAP" & cmp$reflevel %in% c("MIN", "OPTIMIN"), , drop = FALSE]
  cap <- lower_cap(compare(fit$totals))
  if (!nrow(cap)) {
    fit$messages <- unique(c(fit$messages, "Ajustement terminal Ca/P non appliqué : aucun seuil inférieur Ca/P actif."))
    fit$cap_adjustment_g <- 0
    return(fit)
  }
  if (all(cap$status != "INSUFFISANT")) {
    fit$cap_adjustment_g <- 0
    return(fit)
  }
  profile <- profiles[[selection[["calcium"]]]]
  if (is.null(profile) || !is.null(profile$error) || is.na(profile$values["CAL"]) || profile$values["CAL"] <= 0) {
    fit$messages <- unique(c(fit$messages, "Ajustement terminal Ca/P impossible : l'ingrédient calcium n'apporte pas de calcium exploitable."))
    fit$cap_adjustment_g <- 0
    return(fit)
  }
  delta <- profile$values
  if (missing_as_zero) delta[is.na(delta)] <- 0
  added <- 0
  for (i in seq_len(max_iterations)) {
    fit$quantities[["calcium"]] <- fit$quantities[["calcium"]] + increment_g
    fit$totals <- fit$totals + delta * increment_g
    added <- added + increment_g
    if (missing_as_zero) fit$zero_filled <- unique(c(fit$zero_filled, names(profile$values)[is.na(profile$values)]))
    cap <- lower_cap(compare(fit$totals))
    if (nrow(cap) && all(cap$status != "INSUFFISANT")) {
      fit$messages <- unique(c(fit$messages, paste0("Ajustement terminal Ca/P : +", format(added, trim = TRUE), " g de l'ingrédient calcium.")))
      fit$cap_adjustment_g <- added
      return(fit)
    }
  }
  fit$messages <- unique(c(fit$messages, paste0("Ajustement terminal Ca/P arrêté après ", max_iterations, " incréments sans atteindre le seuil.")))
  fit$cap_adjustment_g <- added
  fit
}

vn_profile_step <- function(profile) if (is.null(profile$container_step)) NA_real_ else profile$container_step

#' Explore every ingredient combination across weight/K grids and canine references.
#' @export
vn_explore_rations <- function(model, reference_ids, ingredient_lists, weights, k_values,
                               targets = vn_exploration_targets(), variables = list(),
                               max_scenarios = 5000, progress = NULL, missing_as_zero = FALSE,
                               ignore_levels = "OPTIMAX", rounding = TRUE, min_dose_g = 5, nutrients = NULL,
                               adjust_cap = FALSE, cap_increment_g = 1) {
  if (!isTRUE(missing_as_zero) && !isFALSE(missing_as_zero)) stop("missing_as_zero doit valoir TRUE ou FALSE")
  if (!isTRUE(rounding) && !isFALSE(rounding)) stop("rounding doit valoir TRUE ou FALSE")
  if (!isTRUE(adjust_cap) && !isFALSE(adjust_cap)) stop("adjust_cap doit valoir TRUE ou FALSE")
  if (length(cap_increment_g) != 1L || !is.finite(cap_increment_g) || cap_increment_g <= 0) stop("Incrément Ca/P invalide")
  min_dose_g <- vn_role_min_doses(min_dose_g)
  if (!is.null(nutrients)) {
    nutrients <- unique(as.character(nutrients))
    if (anyNA(nutrients) || any(!nutrients %in% model$dictionaries$nutrients$nutrient_id))
      stop("Nutriments évalués inconnus : ", paste(setdiff(nutrients, model$dictionaries$nutrients$nutrient_id), collapse = ", "))
  }
  if (!rounding) min_dose_g[] <- 0
  if (!is.character(ignore_levels) || any(!ignore_levels %in% c("OPTIMIN", "OPTIMAX", "MAX")))
    stop("ignore_levels : OPTIMIN, OPTIMAX ou MAX uniquement")
  roles <- vn_exploration_roles()$role
  if (!is.list(ingredient_lists) || !setequal(names(ingredient_lists), roles) || anyDuplicated(names(ingredient_lists)))
    stop("Fournir une liste d'ingrédients pour chacun des six ajustements")
  ingredient_lists <- lapply(ingredient_lists[roles], function(x) unique(as.character(x)))
  if (any(lengths(ingredient_lists) == 0L)) stop("Chaque liste d'ingrédients doit contenir au moins un aliment")
  selected_ids <- unique(unlist(ingredient_lists, use.names = FALSE))
  if (anyNA(selected_ids) || any(!selected_ids %in% model$foods$food_id)) stop("Identifiant d'aliment inconnu ou non canin")
  reference_ids <- unique(as.character(reference_ids))
  if (!length(reference_ids) || anyNA(reference_ids) || any(!reference_ids %in% model$references$reference_id)) stop("Sélectionner au moins un référentiel canin")
  for (id in reference_ids) if (isTRUE(vn_init_object(model, "references", id)$maladie)) stop("Références générales uniquement")
  for (x in list(weights, k_values)) if (!is.numeric(x) || !length(x) || any(!is.finite(x) | x <= 0)) stop("Poids et K doivent être positifs et finis")
  weights <- unique(weights); k_values <- unique(k_values)
  required <- c("role", "nutrient_id", "source", "reflevel", "value", "uniteReqId", "multiplier")
  if (!is.data.frame(targets) || !all(required %in% names(targets)) || nrow(targets) != 6L ||
      !setequal(targets$role, roles) || anyDuplicated(targets$role)) stop("Table des six cibles invalide")
  targets <- targets[match(roles, targets$role), , drop = FALSE]
  dict <- model$dictionaries$nutrients
  if (anyNA(targets$nutrient_id) || any(!targets$nutrient_id %in% dict$nutrient_id) ||
      any(dict$is_ratio[match(targets$nutrient_id, dict$nutrient_id)])) stop("Les ajustements nécessitent des nutriments additifs, pas des ratios")
  if (anyNA(targets$source) || any(!targets$source %in% c("reference", "custom", "energy")) ||
      !identical(which(targets$source == "energy"), 6L) || targets$nutrient_id[6] != "ENERGIE") stop("L'énergie restante doit être le dernier ajustement")
  if (any(!is.finite(targets$multiplier) | targets$multiplier < 0)) stop("Multiplicateurs invalides")
  for (i in which(targets$source == "custom")) if (!is.finite(targets$value[i]) || targets$value[i] < 0 ||
      !targets$uniteReqId[i] %in% c(0, 1, 2, 4, 6)) stop("Cible personnalisée ou unité invalide")
  count <- prod(lengths(ingredient_lists)) * length(weights) * length(k_values) * length(reference_ids)
  if (length(max_scenarios) != 1L || !is.finite(max_scenarios) || max_scenarios < 1) stop("Limite de scénarios invalide")
  if (!is.finite(count) || count > max_scenarios) stop(sprintf("%s scénarios demandés, limite %s. Réduire la grille ou augmenter explicitement la limite.", format(count, scientific = FALSE), max_scenarios))
  combinations <- expand.grid(ingredient_lists, stringsAsFactors = FALSE, KEEP.OUT.ATTRS = FALSE)
  combinations$combination_id <- sprintf("C%06d", seq_len(nrow(combinations)))
  grid <- expand.grid(reference_id = reference_ids, weight_kg = weights, K = k_values,
                      stringsAsFactors = FALSE, KEEP.OUT.ATTRS = FALSE)
  summaries <- quantities <- resolved_targets <- vector("list", count)
  profiles <- setNames(lapply(reference_ids, function(id) vn_exploration_profiles(model, id, selected_ids)), reference_ids)
  index <- 0L
  for (g in seq_len(nrow(grid))) {
    reference_id <- grid$reference_id[g]
    # K is the global multiplier of BEE. No K profile is silently inferred.
    context <- tryCatch({
      needs <- vn_init_needs(model, reference_id, grid$weight_kg[g], variables = variables, adjustment = grid$K[g])
      list(needs = needs, targets = vn_resolve_exploration_targets(model, reference_id, targets, needs))
    }, error = identity)
    for (c in seq_len(nrow(combinations))) {
      index <- index + 1L
      sid <- sprintf("S%07d", index)
      selection <- unlist(combinations[c, roles], use.names = TRUE)
      row <- data.frame(scenario_id = sid, combination_id = combinations$combination_id[c],
        reference_id = reference_id,
        reference_name = model$references$nom[match(reference_id, model$references$reference_id)],
        stage = model$references$stadePhysio[match(reference_id, model$references$reference_id)],
        weight_kg = grid$weight_kg[g], K = grid$K[g],
        standard_kcal = NA_real_, need_kcal = NA_real_, energy_kcal = NA_real_, energy_gap_kcal = NA_real_,
        quantity_total_g = NA_real_, energy_tolerance_kcal = NA_real_, insufficient = NA_integer_, excess = NA_integer_, missing = NA_integer_,
        zero_filled = NA_integer_, zero_filled_nutrients = "", cap_adjustment_g = NA_real_, violated = "", violated_documented = "",
        not_covered = "", in_excess = "", composition = "", status = "", message = "", warnings = "", source_json = model$provenance$source_json)
      for (role in roles) row[[paste0("food_", role)]] <- selection[[role]]
      for (role in roles) row[[paste0("quantity_", role, "_g")]] <- NA_real_
      attempt <- tryCatch({
        if (inherits(context, "error")) stop(context)
        n <- context$needs
        row$standard_kcal <- n$standard_kcal; row$need_kcal <- n$need_kcal
        fit <- vn_adjust_combination(profiles[[reference_id]], selection, context$targets,
          missing_as_zero = missing_as_zero, rounding = rounding, min_dose_g = min_dose_g)
        if (adjust_cap) fit <- vn_adjust_cap_terminal(model, reference_id, profiles[[reference_id]], selection, fit, n,
          ignore_levels, nutrients, cap_increment_g, missing_as_zero)
        evaluated <- vn_compare_totals(model, reference_id, fit$totals, n, ignore_levels, nutrients)
        cmp <- evaluated$comparison
        row$energy_kcal <- unname(fit$totals["ENERGIE"])
        row$energy_gap_kcal <- row$energy_kcal - row$need_kcal
        row$quantity_total_g <- sum(fit$quantities)
        row$insufficient <- sum(cmp$status == "INSUFFISANT")
        row$excess <- sum(cmp$status == "EXCES")
        row$missing <- sum(cmp$status == "DONNEES_ABSENTES")
        failed <- cmp$status %in% c("INSUFFISANT", "EXCES")
        row$violated <- paste(unique(paste(cmp$nutrient_id[failed], cmp$reflevel[failed])), collapse = ";")
        row$not_covered <- paste(unique(cmp$nutrient_id[cmp$status == "INSUFFISANT"]), collapse = ",")
        row$in_excess <- paste(unique(cmp$nutrient_id[cmp$status == "EXCES"]), collapse = ",")
        for (role in roles) row[[paste0("quantity_", role, "_g")]] <- fit$quantities[[role]]
        used <- fit$quantities > 0
        row$composition <- paste(sprintf("%s : %s g", model$foods$name[match(selection[used], model$foods$food_id)],
          as.character(round(fit$quantities[used], 1))), collapse = " + ")
        filled <- vn_zero_filled_requirements(model, fit$zero_filled, cmp)
        # Violations not explained by an absent value counted as zero.
        documented <- failed & !cmp$nutrient_id %in% filled
        row$violated_documented <- paste(unique(paste(cmp$nutrient_id[documented], cmp$reflevel[documented])), collapse = ";")
        row$zero_filled <- length(filled)
        row$zero_filled_nutrients <- paste(filled, collapse = ",")
        row$cap_adjustment_g <- if (is.null(fit$cap_adjustment_g)) 0 else fit$cap_adjustment_g
        row$warnings <- paste(fit$messages, collapse = " | ")
        row$energy_tolerance_kcal <- fit$energy_tolerance_kcal
        energy_over <- row$energy_gap_kcal > max(1e-8 * max(1, row$need_kcal), fit$energy_tolerance_kcal)
        row$status <- if (energy_over) "ENERGIE_DEPASSEE" else if (row$missing > 0) "DONNEES_INCOMPLETES" else
          if (row$insufficient + row$excess > 0) "SEUILS_NON_RESPECTES" else if (nzchar(row$warnings)) "CALCULEE_AVEC_AVERTISSEMENTS" else
          if (row$zero_filled > 0) "CONFORME_ABSENTS_A_ZERO" else "CONFORME"
        if (energy_over) row$message <- if (fit$quantities[[length(fit$quantities)]] > 0)
          "Énergie au-dessus du besoin au-delà de la tolérance d'arrondi." else
          "Les ingrédients des autres ajustements dépassent déjà le besoin énergétique ; quantité énergétique nulle."
        quantities[[index]] <- data.frame(scenario_id = sid, role = roles, food_id = unname(selection),
          food_name = model$foods$name[match(unname(selection), model$foods$food_id)],
          quantity_g = unname(fit$quantities), source_json = model$provenance$source_json)
        target_rows <- context$targets
        target_rows$scenario_id <- sid
        target_rows$reference_id <- reference_id
        target_rows$final_intake <- unname(evaluated$totals[target_rows$nutrient_id])
        target_rows$gap <- target_rows$final_intake - target_rows$absolute_target
        resolved_targets[[index]] <- target_rows
        NULL
      }, error = identity)
      if (inherits(attempt, "error")) {
        row$status <- if (inherits(attempt, "vn_exploration_error")) attempt$code else "ERREUR_CALCUL"
        row$message <- conditionMessage(attempt)
      }
      summaries[[index]] <- row
      if (!is.null(progress) && (index %% max(1, floor(count / 100)) == 0 || index == count)) progress(index, count)
    }
  }
  list(summary = vn_bind_rows(summaries, data.frame()),
       quantities = vn_bind_rows(quantities, data.frame()),
       targets = vn_bind_rows(resolved_targets, data.frame()), combinations = combinations,
       configuration = list(reference_ids = reference_ids, ingredient_lists = ingredient_lists,
         weights = weights, k_values = k_values, targets = targets, variables = variables,
         missing_as_zero = missing_as_zero, ignore_levels = ignore_levels,
         rounding = rounding, min_dose_g = as.list(min_dose_g), nutrients = nutrients,
         adjust_cap = adjust_cap, cap_increment_g = cap_increment_g,
         method = "sequential_deficit_energy_last", provenance = model$provenance))
}
