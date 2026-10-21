# Depuis la racine : Rscript vetnutriDogR/tests/ajusteur-exploration.R
# Compare l'exploration accélérée au calcul individuel de référence.
document <- "vetnutriDogR/inst/quarto/ajusteur_ration_simple.qmd"
lines <- readLines(document, warn = FALSE, encoding = "UTF-8")
fin_yaml <- which(lines == "---")[2]
params <- yaml::yaml.load(paste(lines[2:(fin_yaml - 1)], collapse = "\n"))$params
params$choix <- params$choix$value
params$variation <- params$variation$value
chunk <- function(label) {
    debut <- which(lines == paste0("#| label: ", label)) + 1L
    fin <- which(seq_along(lines) > debut & lines == "```")[1] - 1L
    lines[debut:fin]
}
eval(parse(text = chunk("calculs")))
exploration <- chunk("choix-possibles")
# Ne pas exécuter la présentation HTML.
fin <- which(grepl("^associations <- explorer_aliments", exploration)) - 1L
eval(parse(text = exploration[seq_len(fin)]))
debut <- which(grepl("^regrouper_rations <- function", exploration))
fin <- which(grepl("^rations <- regrouper_rations", exploration)) - 1L
eval(parse(text = exploration[debut:fin]))

verifier <- function(poids, k) {
    x <- explorer_aliments(poids, k)
    stopifnot(nrow(x) == 216L, all(x$glucide_g == 0 | x$glucide_g >= 5))
    for (i in seq_len(nrow(x))) {
        ids <- unlist(x[i, c("proteine_id", "fibre_id", "huile_id", "glucide_id")])
        ref <- ajuster_ration(poids, k, params$fibres_cible_g_mcal,
            catalogue_ingredients[ids, ], params$proteines_cible_g_mcal,
            params$omega6_cible_g_mcal)
        doses <- unname(unlist(x[i, c("proteine_g", "fibre_g", "huile_g", "glucide_g")]))
        manque <- ref$apports[1:3] < ref$besoins[1:3] - 1e-8
        ecart <- 100 * (ref$apports[4] / ref$besoins[4] - 1)
        statut <- if (ref$alerte_glucide) "Non équilibrable : énergie déjà dépassée" else if (ecart > 5) "Énergie dépassée après arrondi" else if (ecart < -5) "Énergie insuffisante" else "Compatible en énergie"
        stopifnot(
            identical(doses, ref$composition$quantite_g_j),
            abs(x$apport[i] - ref$apports[4]) < 1e-9,
            x$compatible[i] == (abs(ecart) <= 5 + 1e-8),
            x$nb_manques[i] == sum(manque), x$statut[i] == statut
        )
    }
    uniques <- regrouper_rations(x)
    stopifnot(sum(uniques$associations) == nrow(x))
    # Les regroupements ne changent pas les aliments réellement utilisés.
    for (role in c("proteine", "fibre", "huile", "glucide")) {
        signature <- function(y) {
            y <- y[y$compatible & y[[paste0(role, "_g")]] > 0, ]
            sort(unique(paste(y[[paste0(role, "_id")]], y[[paste0(role, "_g")]])))
        }
        stopifnot(identical(signature(x), signature(uniques)))
    }
    nrow(x)
}
profils <- expand.grid(poids = c(5, 9, 10, 17, 20, 25), k = seq(0.5, 1.5, by = 0.1))
total <- sum(vapply(seq_len(nrow(profils)), function(i) verifier(profils$poids[i], profils$k[i]), integer(1)))
stopifnot(!any(explorer_aliments(5, 0.01)$compatible)) # Cas sans solution.
stopifnot(nrow(regrouper_rations(explorer_aliments(5, 0.5))) < 216L)
stopifnot(identical(vapply(c(0, 4.9, 9.5, 10, 99, 100, 112.5), arrondir_quantite, numeric(1)),
                    c(0, 5, 10, 10, 100, 100, 100)))
cat(sprintf("OK : %d associations comparées au calcul individuel ; arrondis, statuts et regroupements vérifiés.\n", total))
