coerce_num <- function(x) {
  suppressWarnings(as.numeric(x))
}

`%||%` <- function(a, b) {
  if (is.null(a)) b else a
}

is_equal_answer <- function(student_value, expected_value, tolerance = NA_real_) {
  s_num <- coerce_num(student_value)
  e_num <- coerce_num(expected_value)

  if (!is.na(s_num) && !is.na(e_num)) {
    tol <- if (is.na(tolerance)) 0 else tolerance
    return(abs(s_num - e_num) <= tol)
  }

  identical(as.character(student_value), as.character(expected_value))
}

extract_single_submission <- function(x, submission_id = "submission_1") {
  if (is.data.frame(x) && all(c("question", "answer") %in% names(x))) {
    x$submission_id <- submission_id
    return(x[, c("submission_id", "question", "answer")])
  }

  if (is.list(x) && !is.null(x$answers) && is.list(x$answers) && !is.null(names(x$answers))) {
    return(data.frame(
      submission_id = submission_id,
      question = names(x$answers),
      answer = unlist(x$answers, use.names = FALSE),
      stringsAsFactors = FALSE
    ))
  }

  if (is.list(x) && !is.null(x$nutrientRequirements) && is.data.frame(x$nutrientRequirements)) {
    nr <- x$nutrientRequirements

    if (!"nutrient" %in% names(nr) || !"quantity" %in% names(nr)) {
      stop("nutrientRequirements doit contenir 'nutrient' et 'quantity'.", call. = FALSE)
    }

    reference_level <- if ("referenceLevel" %in% names(nr)) nr$referenceLevel else ""
    unit_req <- if ("unitRequirement" %in% names(nr)) nr$unitRequirement else ""

    question <- paste(nr$nutrient, reference_level, unit_req, sep = "::")

    return(data.frame(
      submission_id = submission_id,
      question = question,
      answer = nr$quantity,
      stringsAsFactors = FALSE
    ))
  }

  if (is.list(x) && !is.null(x$nutrientRequirements) && is.list(x$nutrientRequirements)) {
    nr <- x$nutrientRequirements
    nutrient <- vapply(nr, function(it) it$nutrient %||% "", character(1))
    quantity <- vapply(nr, function(it) it$quantity %||% NA_real_, numeric(1))
    reference_level <- vapply(nr, function(it) it$referenceLevel %||% "", character(1))
    unit_req <- vapply(nr, function(it) it$unitRequirement %||% "", character(1))

    question <- paste(nutrient, reference_level, unit_req, sep = "::")

    return(data.frame(
      submission_id = submission_id,
      question = question,
      answer = quantity,
      stringsAsFactors = FALSE
    ))
  }

  if (is.list(x) && !is.null(names(x))) {
    return(data.frame(
      submission_id = submission_id,
      question = names(x),
      answer = unlist(x, use.names = FALSE),
      stringsAsFactors = FALSE
    ))
  }

  stop("Impossible d'extraire les reponses depuis ce JSON.", call. = FALSE)
}

#' Extraire des copies depuis un JSON
#'
#' Formats supportes:
#' - une copie unique avec `answers`
#' - plusieurs copies dans `submissions` (liste)
#' - export VetNutri avec `nutrientRequirements`
#'
#' @param x Objet JSON deja charge (liste R).
#' @return data.frame avec colonnes submission_id, question, answer.
#' @export
vn_extract_submissions <- function(x) {
  if (is.list(x) && !is.null(x$submissions) && is.list(x$submissions)) {
    out <- lapply(seq_along(x$submissions), function(i) {
      item <- x$submissions[[i]]
      sid <- if (!is.null(item$student_id)) item$student_id else paste0("submission_", i)
      extract_single_submission(item, submission_id = sid)
    })
    return(do.call(rbind, out))
  }

  if (is.list(x) && !is.null(x$submissions) && is.data.frame(x$submissions)) {
    out <- lapply(seq_len(nrow(x$submissions)), function(i) {
      item <- as.list(x$submissions[i, , drop = FALSE])
      sid <- if (!is.null(item$student_id) && nzchar(item$student_id)) item$student_id else paste0("submission_", i)
      extract_single_submission(item, submission_id = sid)
    })
    return(do.call(rbind, out))
  }

  extract_single_submission(x)
}

#' Corriger une ou plusieurs copies
#'
#' @param submissions data.frame issu de `vn_extract_submissions`.
#' @param answer_key data.frame issu de `vn_load_answer_key`.
#' @param scale_to Note finale cible (ex: 20).
#' @return Liste contenant `details` et `scores`.
#' @export
vn_grade <- function(submissions, answer_key, scale_to = 20) {
  required_sub <- c("submission_id", "question", "answer")
  if (!all(required_sub %in% names(submissions))) {
    stop("submissions doit contenir: submission_id, question, answer", call. = FALSE)
  }

  required_key <- c("question", "answer", "points", "tolerance")
  if (!all(required_key %in% names(answer_key))) {
    stop("answer_key doit contenir: question, answer, points, tolerance", call. = FALSE)
  }

  merged <- merge(
    submissions,
    answer_key,
    by = "question",
    all.x = TRUE,
    suffixes = c("_student", "_expected")
  )

  if (nrow(merged) == 0) {
    stop("Aucune question corrigeable n'a ete trouvee.", call. = FALSE)
  }

  merged$points <- ifelse(is.na(merged$points), 0, merged$points)

  merged$is_correct <- mapply(
    is_equal_answer,
    student_value = merged$answer_student,
    expected_value = merged$answer_expected,
    tolerance = merged$tolerance
  )

  merged$earned_points <- ifelse(merged$is_correct, merged$points, 0)

  total_points <- sum(answer_key$points)
  if (total_points <= 0) {
    stop("Le bareme doit avoir un total de points > 0.", call. = FALSE)
  }

  split_scores <- split(merged, merged$submission_id)
  scores <- lapply(names(split_scores), function(sid) {
    df <- split_scores[[sid]]
    raw <- sum(df$earned_points)
    scaled <- raw / total_points * scale_to
    data.frame(
      submission_id = sid,
      raw_points = raw,
      total_points = total_points,
      grade = scaled,
      stringsAsFactors = FALSE
    )
  })

  list(
    details = merged[order(merged$submission_id, merged$question), ],
    scores = do.call(rbind, scores)
  )
}

#' Pipeline complet: JSON -> extraction -> correction
#'
#' @param submissions_json JSON des copies.
#' @param answer_key_json JSON du bareme.
#' @param scale_to Note cible (defaut 20).
#' @param output_csv Optionnel, chemin CSV des notes.
#' @return Liste `details` et `scores`.
#' @export
vn_grade_from_json <- function(submissions_json, answer_key_json, scale_to = 20, output_csv = NULL) {
  submissions_raw <- vn_read_json(submissions_json, simplify = FALSE)
  answer_key <- vn_load_answer_key(answer_key_json)
  submissions <- vn_extract_submissions(submissions_raw)

  result <- vn_grade(
    submissions = submissions,
    answer_key = answer_key,
    scale_to = scale_to
  )

  if (!is.null(output_csv)) {
    utils::write.csv(result$scores, output_csv, row.names = FALSE)
  }

  result
}
