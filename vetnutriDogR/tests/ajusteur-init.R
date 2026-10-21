# Depuis la racine du dépôt : Rscript vetnutriDogR/tests/ajusteur-init.R
# Vérifie la copie intégrée sans faire dépendre le rendu Quarto du JSON INIT.
source_json <- "composeApp/src/commonMain/resources/data/vetnutri_export_init.json"
document <- "vetnutriDogR/inst/quarto/ajusteur_ration_simple.qmd"
init <- jsonlite::fromJSON(source_json, simplifyVector = FALSE)
lines <- readLines(document, warn = FALSE, encoding = "UTF-8")
first <- which(lines == "catalogue_ingredients <- data.frame(")
last <- which(grepl("^choisir_ingredient <- function", lines)) - 1L
stopifnot(length(first) == 1L, length(last) == 1L, last > first)
env <- new.env(parent = baseenv())
eval(parse(text = lines[first:last]), envir = env)
copie <- env$catalogue_ingredients
mapping <- c(proteines_g_100g = "PROTEINE", fibres_g_100g = "CELLULOSE",
             lipides_g_100g = "LIPIDE", omega6_g_100g = "O6", ena_g_100g = "ENA")
ids <- vapply(init$foods, function(f) f$uuid, character(1))
stopifnot(nrow(copie) == 19L, !anyDuplicated(copie$source_uuid))
for (i in seq_len(nrow(copie))) {
    index <- which(ids == copie$source_uuid[i])
    stopifnot(length(index) == 1L)
    original <- init$foods[[index]]
    stopifnot(identical(original$name, copie$source_nom[i]))
    stopifnot("49d2bc6b-73a9-4cfa-bebd-559fd9e17819" %in% unlist(original$biblioRefIds))
    for (column in names(mapping)) {
        expected <- original$nutrients[[mapping[[column]]]]
        if (is.null(expected) || !identical(as.numeric(expected), copie[[column]][i])) {
            stop(sprintf("Écart INIT : %s / %s", original$name, column))
        }
    }
    expected_energy <- sum(c(3.5, 3.5, 8.5) * unlist(original$nutrients[c("PROTEINE", "ENA", "LIPIDE")]))
    stopifnot(abs(copie$energie_kcal_100g[i] - expected_energy) < 1e-10)
}
cat("OK : 19 UUID et noms, 95 valeurs nutritionnelles, 19 densités Atwater vérifiés contre INIT.\n")
