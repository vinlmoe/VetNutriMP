#' Lire un JSON VetNutri
#'
#' @param path Chemin vers le fichier JSON.
#' @param simplify Si TRUE, simplifie les vecteurs lors de l'import.
#' @return Objet R (liste) issu du JSON.
#' @export
vn_read_json <- function(path, simplify = FALSE) {
  if (!file.exists(path)) {
    stop("Fichier introuvable: ", path, call. = FALSE)
  }
  jsonlite::fromJSON(path, simplifyVector = simplify)
}

#' Telecharger un JSON depuis une URL
#'
#' @param url URL source.
#' @param destfile Fichier de destination local.
#' @param overwrite Ecraser si le fichier existe deja.
#' @return Chemin du fichier telecharge.
#' @export
vn_download_json <- function(url, destfile, overwrite = FALSE) {
  if (file.exists(destfile) && !overwrite) {
    stop("Le fichier existe deja: ", destfile, call. = FALSE)
  }

  utils::download.file(url = url, destfile = destfile, mode = "wb", quiet = TRUE)

  if (!file.exists(destfile)) {
    stop("Echec du telechargement vers: ", destfile, call. = FALSE)
  }

  destfile
}

#' Charger un bareme de correction
#'
#' Format attendu:
#' - liste nommee: question -> reponse
#' - ou data.frame avec colonnes `question`, `answer`, `points` (optionnel), `tolerance` (optionnel)
#'
#' @param path Chemin du JSON du bareme.
#' @return data.frame normalise avec colonnes question, answer, points, tolerance.
#' @export
vn_load_answer_key <- function(path) {
  key_raw <- vn_read_json(path, simplify = TRUE)

  if (is.data.frame(key_raw)) {
    key <- key_raw
  } else if (is.list(key_raw) && !is.null(names(key_raw))) {
    key <- data.frame(
      question = names(key_raw),
      answer = unlist(key_raw, use.names = FALSE),
      stringsAsFactors = FALSE
    )
  } else {
    stop("Format de bareme non supporte.", call. = FALSE)
  }

  if (!"question" %in% names(key) || !"answer" %in% names(key)) {
    stop("Le bareme doit contenir les colonnes 'question' et 'answer'.", call. = FALSE)
  }

  if (!"points" %in% names(key)) {
    key$points <- 1
  }
  if (!"tolerance" %in% names(key)) {
    key$tolerance <- NA_real_
  }

  key[, c("question", "answer", "points", "tolerance")]
}
