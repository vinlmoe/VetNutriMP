#' Importer une liste de JSON VetNutri
#'
#' `sources` peut contenir des chemins locaux et/ou des URLs.
#'
#' @param sources Vecteur de chemins/URLs.
#' @param simplify Passe a `jsonlite::fromJSON`.
#' @param api_key Cle API JSONBin optionnelle (defaut: variable d'env `JSONBIN_API_KEY`).
#' @return Liste avec `items` (reussites) et `errors` (echecs).
#' @export
vn_import_json_list <- function(
  sources,
  simplify = FALSE,
  api_key = Sys.getenv("JSONBIN_API_KEY", unset = "")
) {
  if (length(sources) == 0) {
    stop("`sources` est vide.", call. = FALSE)
  }

  if (is.null(names(sources))) {
    names(sources) <- paste0("source_", seq_along(sources))
  } else {
    blank <- !nzchar(names(sources))
    names(sources)[blank] <- paste0("source_", which(blank))
  }

  items <- list()
  errors <- list()
  ok_i <- 1L
  err_i <- 1L

  for (i in seq_along(sources)) {
    src <- sources[[i]]
    src_id <- names(sources)[i]
    is_url <- grepl("^https?://", src)

    obj <- tryCatch(
      {
        if (is_url) {
          if (grepl("api\\.jsonbin\\.io", src) && nzchar(api_key) && requireNamespace("httr2", quietly = TRUE)) {
            txt <- httr2::request(src) |>
              httr2::req_headers("X-Master-Key" = api_key) |>
              httr2::req_perform() |>
              httr2::resp_body_string()
            jsonlite::fromJSON(txt, simplifyVector = simplify)
          } else {
            jsonlite::fromJSON(src, simplifyVector = simplify)
          }
        } else {
          vn_read_json(src, simplify = simplify)
        }
      },
      error = function(e) e
    )

    if (inherits(obj, "error")) {
      errors[[err_i]] <- data.frame(
        source_id = src_id,
        source = src,
        error = conditionMessage(obj),
        stringsAsFactors = FALSE
      )
      err_i <- err_i + 1L
    } else {
      items[[ok_i]] <- list(
        source_id = src_id,
        source = src,
        data = obj
      )
      ok_i <- ok_i + 1L
    }
  }

  list(
    items = items,
    errors = if (length(errors) > 0) do.call(rbind, errors) else data.frame()
  )
}

extract_animals_meta <- function(x) {
  if (is.null(x$animals) || !is.list(x$animals) || length(x$animals) == 0) {
    return(data.frame())
  }

  rows <- lapply(x$animals, function(a) {
    data.frame(
      animal_id = a$uuid %||% "",
      animal_name = a$name %||% a$nom %||% "",
      exam_student_id = a$examStudentId %||% "",
      exam_student_number = a$examStudentNumber %||% "",
      exam_exercise_id = a$examExerciseId %||% "",
      stringsAsFactors = FALSE
    )
  })

  do.call(rbind, rows)
}

safe_ratio <- function(num, den, default = NA_real_) {
  ifelse(is.na(den) | den == 0, default, num / den)
}

#' Module de notation nutrition pour un JSON VetNutri
#'
#' Note calculee a partir de:
#' - adequation energetique (proximite de 100%)
#' - respect des minimums
#' - absence de depassement des maximums
#'
#' @param x Objet JSON VetNutri.
#' @param scale_to Note finale cible.
#' @param energy_tolerance_pct Tolerance energie (ecart absolu en % pour score nul).
#' @param weights Poids nommes: `energy`, `minimum`, `maximum`.
#' @return data.frame de notes par animal/copie.
#' @export
vn_grade_nutrition_case <- function(
  x,
  scale_to = 20,
  energy_tolerance_pct = 30,
  weights = c(energy = 0.4, minimum = 0.4, maximum = 0.2)
) {
  required_weights <- c("energy", "minimum", "maximum")
  if (!all(required_weights %in% names(weights))) {
    stop("`weights` doit contenir: energy, minimum, maximum.", call. = FALSE)
  }
  if (sum(weights) <= 0) {
    stop("La somme des poids doit etre > 0.", call. = FALSE)
  }

  cmp <- vn_compare_to_references(x)
  meta <- extract_animals_meta(x)

  energy <- cmp$energy_balance
  if (nrow(energy) > 0) {
    energy$energy_dev_pct <- abs(energy$coverage_ratio - 1) * 100
    energy$energy_score_unit <- pmax(0, 1 - energy$energy_dev_pct / energy_tolerance_pct)
  }

  nutr <- cmp$nutrient_comparison

  animal_ids <- unique(c(
    if (nrow(energy) > 0) energy$animal_id else character(0),
    if (nrow(nutr) > 0) nutr$animal_id else character(0),
    if (nrow(meta) > 0) meta$animal_id else character(0)
  ))

  if (length(animal_ids) == 0) return(data.frame())

  rows <- lapply(animal_ids, function(aid) {
    e <- if (nrow(energy) > 0) energy[energy$animal_id == aid, , drop = FALSE] else data.frame()
    n <- if (nrow(nutr) > 0) nutr[nutr$animal_id == aid, , drop = FALSE] else data.frame()

    # Energie
    energy_score <- if (nrow(e) > 0) mean(e$energy_score_unit, na.rm = TRUE) else NA_real_
    energy_score <- ifelse(is.na(energy_score), 1, energy_score)
    mean_energy_coverage_pct <- if (nrow(e) > 0) mean(e$coverage_ratio * 100, na.rm = TRUE) else NA_real_

    # Minimums
    n_min <- n[n$is_min_requirement %in% TRUE, , drop = FALSE]
    min_total <- nrow(n_min)
    min_below <- if (min_total > 0) sum(n_min$is_below_minimum %in% TRUE, na.rm = TRUE) else 0
    min_score <- if (min_total > 0) 1 - (min_below / min_total) else 1

    # Maximums
    n_max <- n[n$is_max_requirement %in% TRUE, , drop = FALSE]
    max_total <- nrow(n_max)
    max_above <- if (max_total > 0) sum(n_max$is_above_maximum %in% TRUE, na.rm = TRUE) else 0
    max_score <- if (max_total > 0) 1 - (max_above / max_total) else 1

    weighted_unit <- (
      weights["energy"] * energy_score +
        weights["minimum"] * min_score +
        weights["maximum"] * max_score
      ) / sum(weights)

    grade <- weighted_unit * scale_to

    data.frame(
      animal_id = aid,
      energy_score_unit = energy_score,
      minimum_score_unit = min_score,
      maximum_score_unit = max_score,
      min_total = min_total,
      min_below = min_below,
      max_total = max_total,
      max_above = max_above,
      mean_energy_coverage_pct = mean_energy_coverage_pct,
      grade = grade,
      stringsAsFactors = FALSE
    )
  })

  out <- do.call(rbind, rows)

  if (nrow(meta) > 0) {
    out <- merge(meta, out, by = "animal_id", all.y = TRUE)
  }

  out
}

#' Noter une liste de JSON VetNutri
#'
#' Importe chaque JSON, applique `vn_grade_nutrition_case`, et consolide.
#'
#' @param sources Vecteur de chemins/URLs.
#' @param scale_to Note finale cible.
#' @param energy_tolerance_pct Tolerance energie.
#' @param weights Poids du module de notation.
#' @param api_key Cle API JSONBin optionnelle.
#' @param output_csv Optionnel, chemin CSV de sortie.
#' @return Liste avec `scores`, `imports_errors`.
#' @export
vn_grade_nutrition_batch <- function(
  sources,
  scale_to = 20,
  energy_tolerance_pct = 30,
  weights = c(energy = 0.4, minimum = 0.4, maximum = 0.2),
  api_key = Sys.getenv("JSONBIN_API_KEY", unset = ""),
  output_csv = NULL
) {
  imported <- vn_import_json_list(sources, simplify = FALSE, api_key = api_key)
  if (length(imported$items) == 0) {
    stop("Aucun JSON exploitable n'a pu etre importe.", call. = FALSE)
  }

  all_scores <- list()
  idx <- 1L
  for (it in imported$items) {
    sc <- vn_grade_nutrition_case(
      it$data,
      scale_to = scale_to,
      energy_tolerance_pct = energy_tolerance_pct,
      weights = weights
    )
    if (nrow(sc) == 0) next
    sc$source_id <- it$source_id
    sc$source <- it$source
    all_scores[[idx]] <- sc
    idx <- idx + 1L
  }

  scores <- if (length(all_scores) > 0) do.call(rbind, all_scores) else data.frame()

  if (!is.null(output_csv) && nrow(scores) > 0) {
    utils::write.csv(scores, output_csv, row.names = FALSE)
  }

  list(
    scores = scores,
    imports_errors = imported$errors
  )
}
