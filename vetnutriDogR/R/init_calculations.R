# Evaluate only mathematical AST nodes. JSON scripts never execute arbitrary R.
vn_math <- function(script, variables = list()) {
  script <- gsub('\\bNA\\b', '`NA`', script, perl = TRUE)
  script <- gsub('\\bif\\s*\\(', 'vn_if(', script, perl = TRUE)
  expression <- tryCatch(parse(text = script), error = function(e) stop("Équation non reconnue : ", script, call. = FALSE))
  if (length(expression) != 1L) stop("Une seule expression mathématique est autorisée")
  allowed <- c("+", "-", "*", "/", "^", "(", ">", "<", ">=", "<=", "==", "!=", "|", "&",
               "abs", "min", "max", "log", "exp", "sqrt", "vn_if")
  visit <- function(node) {
    if (is.numeric(node)) return(node)
    if (is.symbol(node)) {
      name <- as.character(node)
      value <- variables[[name]]
      if (is.null(value) || length(value) != 1L || !is.finite(value)) stop("Variable absente ou invalide : ", name, call. = FALSE)
      return(value)
    }
    if (!is.call(node) || !is.symbol(node[[1]]) || !as.character(node[[1]]) %in% allowed)
      stop("Opération mathématique non prise en charge", call. = FALSE)
    op <- as.character(node[[1]])
    args <- as.list(node)[-1L]
    if (op == "vn_if") {
      if (length(args) != 3L) stop("if attend trois arguments")
      return(visit(args[[if (isTRUE(as.logical(visit(args[[1]])))) 2L else 3L]]))
    }
    do.call(get(op, envir = baseenv()), lapply(args, visit))
  }
  result <- visit(expression[[1]])
  if (length(result) != 1L || !is.finite(result)) stop("Résultat mathématique non fini", call. = FALSE)
  as.numeric(result)
}

vn_init_object <- function(model, section, id) {
  objects <- model$raw[[section]]
  idx <- match(id, vapply(objects, `[[`, "", "uuid"))
  if (length(idx) != 1L || is.na(idx)) stop("Identifiant introuvable : ", section, "/", id, call. = FALSE)
  objects[[idx]]
}

#' Compute standard and adjusted energy needs using an explicit INIT reference.
#' variables uses equation symbols (e.g. AW, wG, L, wL); no inferred animal profile.
#' @export
vn_init_needs <- function(model, reference_id, weight_kg, ideal_weight_kg = NULL,
                          variables = list(), coefficient_ids = character(), adjustment = 1) {
  ref <- vn_init_object(model, "references", reference_id)
  if (ref$espece != "CHIEN") stop("Module limité au chien")
  if (isTRUE(ref$maladie)) stop("Sélectionner une référence générale ; combinaison de maladies non prise en charge")
  weight <- if (!is.null(ideal_weight_kg)) ideal_weight_kg else weight_kg
  if (length(weight) != 1L || !is.finite(weight) || weight <= 0) stop("Poids positif requis")
  if (length(adjustment) != 1L || !is.finite(adjustment) || adjustment <= 0) stop("Ajustement positif requis")
  if (!is.list(variables) || (length(variables) &&
      (is.null(names(variables)) || any(!nzchar(names(variables))) || anyDuplicated(names(variables)))))
    stop("Variables : liste nommée sans doublons requise")
  variables$BW <- weight
  # Explicit AnimalDetailViewModel.ajouterVariablesParDefaut rules (not nutrition data).
  defaults <- list(wG = 0, AW = 0, L = 0, wL = 0, BCS = 5, REI = 1, AF = 1, TE = 1, GE = 1, ME = 1)
  used_defaults <- character()
  evaluate <- function(id) {
    eq <- vn_init_object(model, "equations", id)
    symbols <- all.vars(parse(text = gsub('\\bif\\s*\\(', 'vn_if(', eq$script, perl = TRUE)))
    absent <- setdiff(intersect(symbols, names(defaults)), names(variables))
    used_defaults <<- union(used_defaults, absent)
    vn_math(eq$script, c(variables, defaults[absent]))
  }
  bee <- evaluate(ref$equationBEE)
  mw <- evaluate(ref$equationBW)
  coefs <- model$coefficients[model$coefficients$reference_id == reference_id & model$coefficients$coefficient_id %in% coefficient_ids, ]
  if (length(coefficient_ids) != nrow(coefs) || anyDuplicated(coefs$groupType)) stop("Coefficients inconnus, dupliqués ou plusieurs valeurs pour un groupe K")
  factor <- prod(coefs$coef) * adjustment
  data.frame(reference_id = reference_id, species = ref$espece, stage = ref$stadePhysio,
    weight_kg = weight, metabolic_weight = mw, standard_kcal = bee, coefficient = factor,
    need_kcal = bee * factor, equation_id = ref$equationBEE,
    default_variables = paste(used_defaults, collapse = ","),
    source_json = model$provenance$source_json)
}

vn_food_values <- function(model, food, ref) {
  dict <- model$dictionaries$nutrients
  direct <- unlist(food$nutrients)
  if (!length(direct)) direct <- setNames(numeric(), character())
  unknown <- setdiff(names(direct), dict$nutrient_id)
  if (length(unknown)) stop("Nutriments non interprétés : ", paste(unknown, collapse = ", "))
  direct <- pmax(direct, 0)
  if (identical(food$dataB, "VF24")) direct <- direct[!names(direct) %in% dict$nutrient_id[dict$enum_class == "AAEnum"]]
  vars <- as.list(setNames(rep(0, nrow(dict)), dict$nutrient_id))
  vars[names(direct)] <- as.list(direct)
  derived <- numeric(); derivation <- character()
  for (id in unlist(ref$equationsNut)) {
    eq <- vn_init_object(model, "equations", id)
    target <- eq$nutrient
    if (eq$kind != "COMPLEMENTARY_NUTRIENT" || is.null(target) ||
        !isTRUE(eq$specie %in% c(ref$espece, "CH")) || target %in% names(direct)) next
    # Kotlin replaces failed complementary evaluations with zero.
    value <- tryCatch(vn_math(eq$script, vars), error = function(e) {
      warning(sprintf("%s / %s : %s ; valeur Kotlin 0", food$uuid, id, conditionMessage(e)), call. = FALSE); 0
    })
    old <- if (target %in% names(derived)) derived[[target]] else 0
    derived[target] <- if (isTRUE(eq$ratio)) value else old + value
    derivation[target] <- paste(c(derivation[target][!is.na(derivation[target])], id), collapse = ";")
  }
  derived <- pmax(derived, 0)
  list(values = c(direct, derived), raw = unlist(food$nutrients), derivation = derivation)
}

vn_food_energy <- function(model, food, ref, values) {
  energy <- unlist(food$energyPerSpecies)
  energy <- energy[energy > 0]
  if (ref$espece %in% names(energy)) return(list(value = unname(energy[ref$espece]), source = paste0("energyPerSpecies/", ref$espece)))
  generic <- food$nutrients$ENERGIE
  if (!length(energy) && !is.null(generic) && generic > 0) return(list(value = generic, source = "nutrients/ENERGIE"))
  commercial <- food$kind %in% c("COMPLET", "COMPLEMENTAIRE")
  id <- if (commercial) ref$equationDEcom else ref$equationDEraw
  if (is.null(id) || !nzchar(id)) {
    candidates <- Filter(function(e) identical(e$nutrient, "ENERGIE") && identical(e$ratio, commercial), model$raw$equations)
    if (!length(candidates)) stop("Équation énergétique absente pour ", food$uuid)
    id <- candidates[[1]]$uuid
  }
  eq <- vn_init_object(model, "equations", id)
  vars <- as.list(setNames(rep(0, nrow(model$dictionaries$nutrients)), model$dictionaries$nutrients$nutrient_id))
  direct <- intersect(names(values$values), names(food$nutrients))
  vars[direct] <- as.list(values$values[direct])
  if ("ENA" %in% names(values$values) && values$values[["ENA"]] > 0) vars$ENA <- values$values[["ENA"]]
  if (vars$ENA <= 0) for (eid in unlist(ref$equationsNut)) {
    e <- vn_init_object(model, "equations", eid)
    if (identical(e$nutrient, "ENA") && e$kind == "COMPLEMENTARY_NUTRIENT") {
      vars$ENA <- vn_math(e$script, vars)
    }
  }
  value <- tryCatch(vn_math(eq$script, vars), error = function(e) {
    warning(sprintf("Énergie %s : %s ; valeur Kotlin 0", food$uuid, conditionMessage(e)), call. = FALSE); 0
  })
  list(value = max(0, value), source = id)
}

#' Evaluate a ration from live INIT foods, in grams, with an explicit reference.
#' Missing composition remains NA in comparisons; it is never labelled adequate,
#' unless missing_as_zero is TRUE (Kotlin `?: 0.0`), which is then reported.
#' @export
vn_init_ration <- function(model, reference_id, items, needs, missing_as_zero = FALSE, ignore_levels = character(),
                           nutrients = NULL) {
  if (!isTRUE(missing_as_zero) && !isFALSE(missing_as_zero)) stop("missing_as_zero doit valoir TRUE ou FALSE")
  ref <- vn_init_object(model, "references", reference_id)
  if (ref$espece != "CHIEN") stop("Module limité au chien")
  if (nrow(needs) != 1L || needs$reference_id != reference_id) stop("Besoins incompatibles avec le référentiel")
  if (!is.data.frame(items) || !all(c("food_id", "quantity_g") %in% names(items)) || !nrow(items)) stop("Ration vide ou colonnes food_id / quantity_g absentes")
  if (!is.numeric(items$quantity_g) || any(!is.finite(items$quantity_g) | items$quantity_g < 0)) stop("Quantités invalides")
  items <- items[items$quantity_g > 0, , drop = FALSE]
  if (!nrow(items)) stop("Ration sans quantité positive")
  dict <- model$dictionaries$nutrients
  required <- unique(c(model$requirements$nutrient_id[model$requirements$reference_id == reference_id], "DM", "ENERGIE"))
  diagnostics <- list()
  details <- list(); energies <- numeric(nrow(items)); matrix_values <- list()
  for (i in seq_len(nrow(items))) {
    food <- vn_init_object(model, "foods", items$food_id[i])
    if (!vn_food_for_dog(food)) stop("Aliment hors périmètre chien : ", food$uuid)
    collect_warning <- function(w) {
      diagnostics[[length(diagnostics) + 1L]] <<- data.frame(item_index = i,
        food_id = food$uuid, message = conditionMessage(w), source_json = model$provenance$source_json)
      invokeRestart("muffleWarning")
    }
    values <- withCallingHandlers(vn_food_values(model, food, ref), warning = collect_warning)
    energy <- withCallingHandlers(vn_food_energy(model, food, ref, values), warning = collect_warning)
    energies[i] <- energy$value * items$quantity_g[i] / 100
    values$values["ENERGIE"] <- energy$value
    ids <- union(required, names(values$values))
    effective <- unname(values$values[ids])
    item_ratios <- dict$is_ratio[match(ids, dict$nutrient_id)]
    matrix_values[[i]] <- values$values * items$quantity_g[i] / 100
    details[[i]] <- data.frame(item_index = i, food_id = food$uuid, reference_id = reference_id,
      nutrient_id = ids, quantity_g = items$quantity_g[i], raw_value = unname(values$raw[ids]),
      value_per_100g = ifelse(item_ratios, NA_real_, effective),
      ratio_value = ifelse(item_ratios, effective, NA_real_),
      intake = ifelse(item_ratios, effective, effective * items$quantity_g[i] / 100),
      unit = dict$unit[match(ids, dict$nutrient_id)],
      equation_id = ifelse(ids == "ENERGIE", energy$source, unname(values$derivation[ids])),
      source_json = model$provenance$source_json)
  }
  detail <- do.call(rbind, details)
  ids <- unique(detail$nutrient_id)
  zero_filled <- character()
  totals <- setNames(vapply(ids, function(id) {
    # Strict absence propagation by default (Kotlin may display an incomplete sum).
    v <- vapply(matrix_values, function(x) if (id %in% names(x)) x[[id]] else NA_real_, 0.0)
    if (missing_as_zero && anyNA(v)) {
      zero_filled <<- c(zero_filled, id)
      v[is.na(v)] <- 0
    }
    sum(v)
  }, 0.0), ids)
  evaluation <- vn_compare_totals(model, reference_id, totals, needs, ignore_levels, nutrients)
  evaluation$comparison$zero_filled <- evaluation$comparison$nutrient_id %in% zero_filled
  totals <- evaluation$totals
  cmp <- evaluation$comparison
  list(needs = needs, item_intakes = detail, nutrient_totals = totals,
       energy_kcal = sum(energies), energy_coverage = sum(energies) / needs$need_kcal,
       comparison = cmp, missing_as_zero = missing_as_zero,
       zero_filled = vn_zero_filled_requirements(model, zero_filled, cmp),
       diagnostics = vn_bind_rows(diagnostics, data.frame(item_index = integer(), food_id = character(), message = character(), source_json = character())),
       source_json = model$provenance$source_json)
}

# Additive nutrients evaluated against a threshold whose total used a zero for an absent value.
vn_zero_filled_requirements <- function(model, zero_filled, comparison) {
  dict <- model$dictionaries$nutrients
  additive <- dict$nutrient_id[!dict$is_ratio]
  sort(intersect(intersect(zero_filled, additive), unique(comparison$nutrient_id)))
}

# Shared by manual rations and the exploration grid; no second set of thresholds.
# ignore_levels removes threshold levels from the evaluation (e.g. OPTIMAX: only MAX limits);
# nutrients, when not NULL, keeps only those nutrients' thresholds (character() evaluates none).
vn_compare_totals <- function(model, reference_id, totals, needs, ignore_levels = character(), nutrients = NULL) {
  if (!is.null(nutrients) && (!is.character(nutrients) || anyNA(nutrients))) stop("Nutriments évalués invalides")
  if (!is.character(ignore_levels) || any(!ignore_levels %in% c("MIN", "OPTIMIN", "OPTIMAX", "MAX")))
    stop("Niveaux de seuil ignorés invalides")
  ref <- vn_init_object(model, "references", reference_id)
  dict <- model$dictionaries$nutrients
  # Ratios are evaluated from ration totals, never summed per ingredient.
  for (id in dict$nutrient_id[dict$is_ratio]) {
    totals[id] <- NA_real_
    for (eid in unlist(ref$equationsNut)) {
      eq <- vn_init_object(model, "equations", eid)
      if (identical(eq$nutrient, id) && eq$kind == "COMPLEMENTARY_NUTRIENT")
        totals[id] <- tryCatch(vn_math(eq$script, as.list(totals)), error = function(e) NA_real_)
    }
  }
  cmp <- model$requirements[model$requirements$reference_id == reference_id & model$requirements$is_effective &
    !model$requirements$reflevel %in% ignore_levels &
    (if (is.null(nutrients)) TRUE else model$requirements$nutrient_id %in% nutrients), , drop = FALSE]
  cmp$intake <- unname(totals[cmp$nutrient_id])
  ratio <- dict$is_ratio[match(cmp$nutrient_id, dict$nutrient_id)]
  ratio[is.na(ratio)] <- FALSE
  multiplier <- vapply(cmp$uniteReqId, function(u) switch(as.character(u),
    `0` = needs$weight_kg, `1` = needs$standard_kcal / 1000, `2` = needs$metabolic_weight,
    `4` = needs$standard_kcal * 4.184 / 1000, `5` = NA_real_, `6` = 1, NA_real_), 0.0)
  cmp$absolute_requirement <- cmp$quantity * ifelse(ratio, 1, multiplier)
  cmp$status <- ifelse(is.na(cmp$intake) | is.na(cmp$absolute_requirement), "DONNEES_ABSENTES",
    ifelse(cmp$reflevel %in% c("MIN", "OPTIMIN") & cmp$intake < cmp$absolute_requirement, "INSUFFISANT",
      ifelse(cmp$reflevel %in% c("MAX", "OPTIMAX") & cmp$intake > cmp$absolute_requirement, "EXCES", "CONFORME")))
  list(totals = totals, comparison = cmp)
}
