as_num <- function(x, default = NA_real_) {
  v <- suppressWarnings(as.numeric(x))
  if (length(v) == 0 || is.na(v)) default else v
}

to_named_numeric <- function(x) {
  if (is.null(x)) return(numeric(0))

  if (is.numeric(x) && !is.null(names(x))) {
    return(x)
  }

  if (is.list(x) && length(x) > 0 && !is.null(names(x))) {
    out <- vapply(x, function(v) {
      if (is.list(v) && !is.null(v$value)) {
        as_num(v$value, default = 0)
      } else {
        as_num(v, default = 0)
      }
    }, numeric(1))
    return(out)
  }

  numeric(0)
}

is_min_level <- function(reflevel) {
  toupper(reflevel %||% "") %in% c("MIN", "OPTIMIN", "RECOMMENDED")
}

is_max_level <- function(reflevel) {
  toupper(reflevel %||% "") %in% c("MAX", "OPTIMAX")
}

eval_script <- function(script, vars = list()) {
  if (is.null(script) || !nzchar(script)) return(NA_real_)
  env <- new.env(parent = baseenv())
  env$abs <- abs
  env$min <- min
  env$max <- max
  env$log <- log
  env$exp <- exp
  env$sqrt <- sqrt
  for (k in names(vars)) {
    env[[k]] <- as_num(vars[[k]], default = NA_real_)
  }
  expr <- tryCatch(parse(text = script)[[1]], error = function(e) NULL)
  if (is.null(expr)) return(NA_real_)
  out <- tryCatch(eval(expr, envir = env), error = function(e) NA_real_)
  as_num(out, default = NA_real_)
}

find_energy_density_equation <- function(reference, equations) {
  if (!is.null(reference$equationDEcom) && !is.null(equations[[reference$equationDEcom]])) {
    return(equations[[reference$equationDEcom]])
  }
  if (!is.null(reference$equationDEraw) && !is.null(equations[[reference$equationDEraw]])) {
    return(equations[[reference$equationDEraw]])
  }
  for (eq in equations) {
    if (!is.null(eq$kind) && eq$kind == "ENERGYDENSITY") {
      return(eq)
    }
  }
  NULL
}

extract_context <- function(x) {
  foods <- list()
  if (!is.null(x$foods) && is.list(x$foods)) {
    for (f in x$foods) {
      if (!is.null(f$uuid)) foods[[f$uuid]] <- f
    }
  }

  equations <- list()
  if (!is.null(x$equations) && is.list(x$equations)) {
    for (e in x$equations) {
      if (!is.null(e$uuid)) equations[[e$uuid]] <- e
    }
  }

  references <- list()
  if (!is.null(x$references) && is.list(x$references)) {
    for (r in x$references) {
      if (!is.null(r$uuid)) references[[r$uuid]] <- r
    }
  }

  list(foods = foods, equations = equations, references = references)
}

extract_consultation_rows <- function(x) {
  rows <- list()
  idx <- 1L

  if (!is.null(x$animals) && is.list(x$animals)) {
    for (a in x$animals) {
      animal_id <- a$uuid %||% ""
      animal_name <- a$name %||% a$nom %||% ""
      exam_student_id <- a$examStudentId %||% ""
      exam_exercise_id <- a$examExerciseId %||% ""
      consults <- a$consultations %||% list()
      if (!is.list(consults)) next
      for (c in consults) {
        rows[[idx]] <- list(
          animal_id = animal_id,
          animal_name = animal_name,
          exam_student_id = exam_student_id,
          exam_exercise_id = exam_exercise_id,
          consultation_id = c$uuid %||% c$UUID %||% "",
          consultation_date = c$date %||% "",
          weight_kg = as_num(c$weightKg %||% c$Poids, default = NA_real_),
          reference_id = c$referenceGeneraleId %||% c$RefString %||% "",
          k1 = as_num(c$k1Value %||% c$k1value, default = 1),
          k2 = as_num(c$k2Value %||% c$k2value, default = 1),
          k3 = as_num(c$k3Value %||% c$k3value, default = 1),
          k4 = as_num(c$k4Value %||% c$k4value, default = 1),
          k5 = as_num(c$k5Value %||% c$k5value, default = 1),
          adjust_coef = as_num(c$coefficientAjustement %||% c$ky, default = 1),
          consultation = c
        )
        idx <- idx + 1L
      }
    }
  } else if (is.list(x) && length(x) > 0) {
    # Compatibilite ancien format (liste d'animaux)
    for (a in x) {
      animal_id <- a$uuid %||% a$UUID %||% ""
      animal_name <- a$name %||% a$nom %||% ""
      exam_student_id <- a$examStudentId %||% ""
      exam_exercise_id <- a$examExerciseId %||% ""
      consults <- a$consultations %||% (a$list$consultList %||% a$list$consultations %||% list())
      if (!is.list(consults)) next
      for (c in consults) {
        rows[[idx]] <- list(
          animal_id = animal_id,
          animal_name = animal_name,
          exam_student_id = exam_student_id,
          exam_exercise_id = exam_exercise_id,
          consultation_id = c$uuid %||% c$UUID %||% "",
          consultation_date = c$date %||% "",
          weight_kg = as_num(c$weightKg %||% c$Poids, default = NA_real_),
          reference_id = c$referenceGeneraleId %||% c$RefString %||% "",
          k1 = as_num(c$k1Value %||% c$k1value, default = 1),
          k2 = as_num(c$k2Value %||% c$k2value, default = 1),
          k3 = as_num(c$k3Value %||% c$k3value, default = 1),
          k4 = as_num(c$k4Value %||% c$k4value, default = 1),
          k5 = as_num(c$k5Value %||% c$k5value, default = 1),
          adjust_coef = as_num(c$coefficientAjustement %||% c$ky, default = 1),
          consultation = c
        )
        idx <- idx + 1L
      }
    }
  }

  rows
}

extract_rations_from_consult <- function(consultation) {
  r <- consultation$rations %||% consultation$rationList %||% list()
  if (!is.list(r)) return(list())
  r
}

extract_items_from_ration <- function(ration) {
  items <- ration$items %||% ration$alimentList %||% ration$alimentMutableList %||% list()
  if (!is.list(items)) return(list())
  items
}

get_food_from_item <- function(item, foods_lookup) {
  if (!is.null(item$alime)) return(item$alime)
  if (!is.null(item$aliment)) return(item$aliment)
  fid <- item$foodId %||% ""
  if (nzchar(fid) && !is.null(foods_lookup[[fid]])) return(foods_lookup[[fid]])
  NULL
}

#' Calculer le besoin energetique des consultations
#'
#' Calcule le besoin energetique via la reference associee (`equationBEE`) et les
#' coefficients de consultation (`k1..k5`, `coefficientAjustement`).
#'
#' @param x Objet JSON VetNutri deja charge.
#' @return data.frame des besoins energetiques par consultation.
#' @export
vn_compute_energy_needs <- function(x) {
  ctx <- extract_context(x)
  consult_rows <- extract_consultation_rows(x)
  out <- list()
  j <- 1L

  for (row in consult_rows) {
    ref <- ctx$references[[row$reference_id]]
    eq <- NULL
    if (!is.null(ref) && !is.null(ref$equationBEE)) {
      eq <- ctx$equations[[ref$equationBEE]]
    }
    if (is.null(eq)) {
      for (cand in ctx$equations) {
        if (!is.null(cand$kind) && cand$kind == "ENERGYNEED") {
          eq <- cand
          break
        }
      }
    }

    base_need <- NA_real_
    if (!is.null(eq)) {
      base_need <- eval_script(eq$script %||% "", list(BW = row$weight_kg))
    }

    coef_total <- row$k1 * row$k2 * row$k3 * row$k4 * row$k5 * row$adjust_coef
    need_kcal <- if (!is.na(base_need)) base_need * coef_total else NA_real_

    out[[j]] <- data.frame(
      animal_id = row$animal_id,
      animal_name = row$animal_name,
      exam_student_id = row$exam_student_id,
      exam_exercise_id = row$exam_exercise_id,
      consultation_id = row$consultation_id,
      date = row$consultation_date,
      weight_kg = row$weight_kg,
      reference_id = row$reference_id,
      equation_id = if (!is.null(eq)) eq$uuid %||% "" else "",
      equation_name = if (!is.null(eq)) eq$name %||% "" else "",
      base_need_kcal = base_need,
      coef_total = coef_total,
      need_kcal = need_kcal,
      stringsAsFactors = FALSE
    )
    j <- j + 1L
  }

  if (length(out) == 0) return(data.frame())
  do.call(rbind, out)
}

#' Calculer les apports nutritionnels par ration
#'
#' Pour chaque ration, calcule l'apport de chaque nutriment a partir des aliments.
#' Les nutriments sont interpretes "pour 100g" (parametre `nutrient_basis`).
#'
#' @param x Objet JSON VetNutri deja charge.
#' @param nutrient_basis Base de normalisation des nutriments (defaut 100).
#' @param energy_keys Cles nutritionnelles d'energie directe.
#' @return Liste avec `item_intakes`, `ration_nutrients`, `ration_energy`.
#' @export
vn_compute_ration_intakes <- function(
  x,
  nutrient_basis = 100,
  energy_keys = c("ENERGIE", "EM", "KCAL", "ME")
) {
  ctx <- extract_context(x)
  consult_rows <- extract_consultation_rows(x)

  item_out <- list()
  io <- 1L

  for (row in consult_rows) {
    ref <- ctx$references[[row$reference_id]]
    energy_eq <- if (!is.null(ref)) find_energy_density_equation(ref, ctx$equations) else NULL
    rations <- extract_rations_from_consult(row$consultation)

    for (ration in rations) {
      ration_id <- ration$uuid %||% ration$UUID %||% ""
      ration_name <- ration$name %||% ration$Nom %||% ""
      ration_is_current <- as.logical(ration$isCurrent %||% ration$actual %||% FALSE)
      if (!isTRUE(ration_is_current) && nzchar(ration_name)) {
        lname <- tolower(ration_name)
        if (grepl("actuelle|current", lname)) {
          ration_is_current <- TRUE
        }
      }
      ration_type <- ifelse(isTRUE(ration_is_current), "ACTUELLE", "PROPOSEE")
      items <- extract_items_from_ration(ration)

      for (it in items) {
        qty <- as_num(it$quantity %||% it$quantite, default = 0)
        food <- get_food_from_item(it, ctx$foods)
        food_id <- it$foodId %||% (food$uuid %||% food$UUID %||% "")
        food_name <- if (!is.null(food)) (food$name %||% food$nom %||% "") else ""
        nutr <- numeric(0)
        if (!is.null(food)) {
          nutr <- to_named_numeric(food$nutrients %||% food$valMap)
        }

        # energie priorite: cle directe, sinon equation de densite
        energy_density <- NA_real_
        energy_source <- ""
        for (k in energy_keys) {
          if (!is.na(nutr[k])) {
            energy_density <- nutr[k]
            energy_source <- paste0("nutrient:", k)
            break
          }
        }
        if (is.na(energy_density) && !is.null(energy_eq)) {
          energy_density <- eval_script(energy_eq$script %||% "", as.list(nutr))
          if (!is.na(energy_density)) {
            energy_source <- paste0("equation:", energy_eq$uuid %||% "")
          }
        }
        if (is.na(energy_density)) {
          energy_density <- as_num(ration$EnerTot, default = NA_real_)
          if (!is.na(energy_density)) {
            energy_source <- "ration:enerTot"
          }
        }

        if (length(nutr) > 0) {
          item_uuid <- it$uuid %||% it$UUID %||% paste0(ration_id, "::", food_id, "::", io)
          for (k in names(nutr)) {
            intake <- qty * nutr[[k]] / nutrient_basis
            item_out[[io]] <- data.frame(
              animal_id = row$animal_id,
              animal_name = row$animal_name,
              exam_student_id = row$exam_student_id,
              exam_exercise_id = row$exam_exercise_id,
              consultation_id = row$consultation_id,
              date = row$consultation_date,
              ration_id = ration_id,
              ration_name = ration_name,
              ration_is_current = ration_is_current,
              ration_type = ration_type,
              item_uuid = item_uuid,
              food_id = food_id,
              food_name = food_name,
              quantity_g = qty,
              nutrient = k,
              nutrient_value_basis = nutr[[k]],
              intake = intake,
              energy_density = energy_density,
              energy_source = energy_source,
              energy_intake_kcal = if (!is.na(energy_density)) qty * energy_density / nutrient_basis else NA_real_,
              stringsAsFactors = FALSE
            )
            io <- io + 1L
          }
        }
      }
    }
  }

  if (length(item_out) == 0) {
    return(list(
      item_intakes = data.frame(),
      ration_nutrients = data.frame(),
      ration_energy = data.frame()
    ))
  }

  item_df <- do.call(rbind, item_out)

  ration_nutrients <- stats::aggregate(
    intake ~ animal_id + animal_name + exam_student_id + exam_exercise_id + consultation_id + date + ration_id + ration_name + ration_is_current + ration_type + nutrient,
    data = item_df,
    FUN = sum
  )

  energy_items <- item_df[!duplicated(item_df[, c("consultation_id", "ration_id", "item_uuid")]), ]
  ration_energy <- stats::aggregate(
    energy_intake_kcal ~ animal_id + animal_name + exam_student_id + exam_exercise_id + consultation_id + date + ration_id + ration_name + ration_is_current + ration_type,
    data = energy_items,
    FUN = function(v) sum(v, na.rm = TRUE)
  )

  list(
    item_intakes = item_df,
    ration_nutrients = ration_nutrients,
    ration_energy = ration_energy
  )
}

#' Comparer apports des rations aux references de consultation
#'
#' Compare les apports nutritionnels agreges avec les references associees a la
#' consultation (`references[].nutrients`).
#'
#' @param x Objet JSON VetNutri deja charge.
#' @param nutrient_basis Base des nutriments (defaut 100).
#' @return Liste avec `energy_balance` et `nutrient_comparison`.
#' @export
vn_compare_to_references <- function(x, nutrient_basis = 100) {
  ctx <- extract_context(x)
  needs <- vn_compute_energy_needs(x)
  intakes <- vn_compute_ration_intakes(x, nutrient_basis = nutrient_basis)

  energy_balance <- merge(
    intakes$ration_energy,
    needs[, c("consultation_id", "need_kcal", "reference_id", "exam_student_id", "exam_exercise_id")],
    by = "consultation_id",
    all.x = TRUE,
    suffixes = c("", "_need")
  )
  if ("exam_student_id_need" %in% names(energy_balance)) {
    energy_balance$exam_student_id <- ifelse(
      nzchar(energy_balance$exam_student_id),
      energy_balance$exam_student_id,
      energy_balance$exam_student_id_need
    )
    energy_balance$exam_student_id_need <- NULL
  }
  if ("exam_exercise_id_need" %in% names(energy_balance)) {
    energy_balance$exam_exercise_id <- ifelse(
      nzchar(energy_balance$exam_exercise_id),
      energy_balance$exam_exercise_id,
      energy_balance$exam_exercise_id_need
    )
    energy_balance$exam_exercise_id_need <- NULL
  }
  energy_balance$coverage_ratio <- energy_balance$energy_intake_kcal / energy_balance$need_kcal
  energy_balance$gap_kcal <- energy_balance$energy_intake_kcal - energy_balance$need_kcal

  # table de references nutriments
  ref_rows <- list()
  ri <- 1L
  for (ref in ctx$references) {
    if (is.null(ref$nutrients) || !is.list(ref$nutrients)) next
    for (n in ref$nutrients) {
      ref_rows[[ri]] <- data.frame(
        reference_id = ref$uuid %||% "",
        nutrient = n$nutrientLabel %||% n$nutrient %||% "",
        reflevel = n$reflevel %||% n$referenceLevel %||% "",
        reference_value = as_num(n$quantity, default = NA_real_),
        stringsAsFactors = FALSE
      )
      ri <- ri + 1L
    }
  }
  ref_df <- if (length(ref_rows) > 0) do.call(rbind, ref_rows) else data.frame()

  ration_nutrients <- intakes$ration_nutrients
  if (nrow(ration_nutrients) == 0 || nrow(ref_df) == 0) {
    nutrient_comparison <- data.frame()
  } else {
    consult_ref <- needs[, c("consultation_id", "reference_id")]
    ration_with_ref <- merge(ration_nutrients, consult_ref, by = "consultation_id", all.x = TRUE)
    nutrient_comparison <- merge(
      ration_with_ref,
      ref_df,
      by = c("reference_id", "nutrient"),
      all.x = TRUE
    )
    nutrient_comparison$coverage_ratio <- nutrient_comparison$intake / nutrient_comparison$reference_value
    nutrient_comparison$gap <- nutrient_comparison$intake - nutrient_comparison$reference_value

    nutrient_comparison$gap_pct <- 100 * nutrient_comparison$gap / nutrient_comparison$reference_value

    nutrient_comparison$is_min_requirement <- vapply(
      nutrient_comparison$reflevel,
      is_min_level,
      logical(1)
    )
    nutrient_comparison$is_max_requirement <- vapply(
      nutrient_comparison$reflevel,
      is_max_level,
      logical(1)
    )

    nutrient_comparison$minimum_coverage_pct <- ifelse(
      nutrient_comparison$is_min_requirement,
      nutrient_comparison$coverage_ratio * 100,
      NA_real_
    )
    nutrient_comparison$minimum_gap_pct <- ifelse(
      nutrient_comparison$is_min_requirement,
      nutrient_comparison$minimum_coverage_pct - 100,
      NA_real_
    )
    nutrient_comparison$is_below_minimum <- ifelse(
      nutrient_comparison$is_min_requirement,
      nutrient_comparison$intake < nutrient_comparison$reference_value,
      FALSE
    )

    nutrient_comparison$maximum_excess_pct <- ifelse(
      nutrient_comparison$is_max_requirement,
      pmax(0, nutrient_comparison$gap_pct),
      NA_real_
    )
    nutrient_comparison$is_above_maximum <- ifelse(
      nutrient_comparison$is_max_requirement,
      nutrient_comparison$intake > nutrient_comparison$reference_value,
      FALSE
    )

    nutrient_comparison$signal <- ifelse(
      nutrient_comparison$is_below_minimum,
      "BELOW_MINIMUM",
      ifelse(nutrient_comparison$is_above_maximum, "ABOVE_MAXIMUM", "OK")
    )
  }

  list(
    energy_balance = energy_balance,
    nutrient_comparison = nutrient_comparison
  )
}
