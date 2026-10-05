# Zones where no combination yields a balanced ration, by reference, weight and K.
vn_balance_zones <- function() {
  data.frame(zone = c("EQUILIBRABLE", "SOUS_RESERVE", "SEUILS_NON_RESPECTES", "ENERGIE_DEPASSEE", "NON_EVALUABLE"),
    label = c("Équilibrable", "Sous réserve : seuls des nutriments non renseignés échouent",
              "Non équilibrable : seuils renseignés non respectés",
              "Non équilibrable : énergie déjà dépassée", "Non évaluable (cible ou composition absente)"),
    # Status palette: the meaning is also carried by hatching and the n/N label.
    colour = c("#0ca30c", "#fab219", "#d03b3b", "#ec835a", "#b5b5b0"),
    angle = c(NA, 135, NA, 45, 135),
    stringsAsFactors = FALSE)
}

#' Summarise an exploration by reference, weight and K.
#' A cell is balanceable when at least one ingredient combination is accepted.
#' @export
vn_exploration_balance <- function(exploration,
                                   accepted = c("CONFORME", "CALCULEE_AVEC_AVERTISSEMENTS", "CONFORME_ABSENTS_A_ZERO"),
                                   top = 3L) {
  s <- if (is.data.frame(exploration)) exploration else exploration$summary
  required <- c("reference_id", "reference_name", "weight_kg", "K", "status", "violated", "violated_documented")
  if (!is.data.frame(s) || !nrow(s) || !all(required %in% names(s))) stop("Résultat d'exploration requis")
  documented <- nzchar(s$violated_documented)
  category <- ifelse(s$status %in% accepted, "EQUILIBRABLE",
    ifelse(s$status == "ENERGIE_DEPASSEE", "ENERGIE_DEPASSEE",
    ifelse(s$status %in% c("SEUILS_NON_RESPECTES", "DONNEES_INCOMPLETES") & documented, "SEUILS_NON_RESPECTES",
    ifelse(s$status %in% c("SEUILS_NON_RESPECTES", "DONNEES_INCOMPLETES"), "SOUS_RESERVE", "NON_EVALUABLE"))))
  # The best category reached names the zone; without any ration within the documented
  # thresholds, the dominant failure does (ties: thresholds, energy, data).
  failures <- vn_balance_zones()$zone[-(1:2)]
  cells <- split(seq_len(nrow(s)), paste(s$reference_id, s$weight_kg, s$K, sep = "\r"), drop = TRUE)
  rows <- lapply(cells, function(i) {
    counts <- vapply(failures, function(z) sum(category[i] == z), 0L)
    balanced <- sum(category[i] == "EQUILIBRABLE")
    reserved <- sum(category[i] == "SOUS_RESERVE")
    zone <- if (balanced > 0) "EQUILIBRABLE" else if (reserved > 0) "SOUS_RESERVE" else failures[which.max(counts)]
    frequent <- function(x) {
      freq <- utils::head(sort(table(unlist(strsplit(x[nzchar(x)], ";", fixed = TRUE))), decreasing = TRUE), top)
      if (length(freq)) paste0(names(freq), " (", as.integer(freq), "/", length(i), ")", collapse = ", ") else ""
    }
    # Distance to balance: fewest documented thresholds missed by an evaluable combination.
    evaluable <- i[category[i] != "NON_EVALUABLE"]
    misses <- vapply(strsplit(s$violated_documented[evaluable], ";", fixed = TRUE), length, 0L)
    data.frame(reference_id = s$reference_id[i[1]], reference_name = s$reference_name[i[1]],
      weight_kg = s$weight_kg[i[1]], K = s$K[i[1]], combinations = length(i), balanced = balanced,
      reserved = reserved, thresholds_failed = counts[["SEUILS_NON_RESPECTES"]],
      energy_over = counts[["ENERGIE_DEPASSEE"]], not_evaluable = counts[["NON_EVALUABLE"]],
      best = switch(zone, EQUILIBRABLE = balanced, SOUS_RESERVE = reserved, 0L),
      min_failed = if (length(misses)) min(misses) else NA_integer_,
      zone = zone, limiting = frequent(s$violated_documented[i]),
      undocumented = frequent(setdiff_list(s$violated[i], s$violated_documented[i])),
      stringsAsFactors = FALSE)
  })
  out <- do.call(rbind, rows)
  rownames(out) <- NULL
  out[order(out$reference_name, out$weight_kg, out$K), ]
}

# Per scenario, violations that concern only nutrients counted as zero.
setdiff_list <- function(all, documented) {
  vapply(seq_along(all), function(j) paste(setdiff(strsplit(all[j], ";", fixed = TRUE)[[1]],
    strsplit(documented[j], ";", fixed = TRUE)[[1]]), collapse = ";"), "")
}

# Sequential red ramp for "seuils renseignés non respectés": light = 1 missed threshold, dark = 5 or more.
vn_distance_ramp <- function() grDevices::colorRampPalette(c("#f4a6a0", "#d03b3b", "#7a1717"))(5)

vn_balance_fill <- function(b) {
  zones <- vn_balance_zones()
  fill <- zones$colour[match(b$zone, zones$zone)]
  red <- b$zone == "SEUILS_NON_RESPECTES" & !is.na(b$min_failed)
  fill[red] <- vn_distance_ramp()[pmin(pmax(b$min_failed[red], 1L), 5L)]
  fill
}

# n/N for reachable zones; the distance to balance for the others.
vn_balance_label <- function(b) {
  ifelse(b$zone %in% c("EQUILIBRABLE", "SOUS_RESERVE"), paste0(b$best, "/", b$combinations),
    ifelse(is.na(b$min_failed), "-", as.character(b$min_failed)))
}

# Tile edges halfway between grid values, so uneven steps still tile the plane.
vn_tile_edges <- function(x) {
  x <- sort(unique(x))
  if (length(x) == 1L) return(c(x - 0.5 * max(abs(x) * 0.1, 0.05), x + 0.5 * max(abs(x) * 0.1, 0.05)))
  mid <- (x[-1] + x[-length(x)]) / 2
  c(x[1] - (mid[1] - x[1]), mid, x[length(x)] + (x[length(x)] - mid[length(mid)]))
}

#' Draw the weight × K balance map, one panel per reference.
#' With `legend = FALSE`, the last panel's coordinates stay active so that a
#' click on a single-reference map can be mapped back to its cell.
#' @export
vn_plot_balance_map <- function(balance, labels = NULL, legend = TRUE) {
  if (!is.data.frame(balance) || !nrow(balance)) stop("Résultat de vn_exploration_balance requis")
  zones <- vn_balance_zones()
  refs <- unique(balance[, c("reference_id", "reference_name")])
  cols <- min(2L, nrow(refs))
  old <- graphics::par(no.readonly = TRUE)
  if (legend) on.exit(graphics::par(old))
  panels <- matrix(seq_len(cols * ceiling(nrow(refs) / cols)), ncol = cols, byrow = TRUE)
  if (legend) graphics::layout(rbind(panels, rep(length(panels) + 1L, cols)),
                               heights = c(rep(1, nrow(panels)), graphics::lcm(4.4)))
  else graphics::layout(panels)
  graphics::par(mar = c(4.2, 4.2, 2.6, 1), mgp = c(2.6, 0.7, 0), las = 1, col.axis = "#555550", fg = "#8a8a85")
  for (r in seq_len(nrow(refs))) {
    b <- balance[balance$reference_id == refs$reference_id[r], ]
    w <- sort(unique(b$weight_kg)); k <- sort(unique(b$K))
    we <- vn_tile_edges(w); ke <- vn_tile_edges(k)
    graphics::plot(NA, xlim = range(we), ylim = range(ke), xaxs = "i", yaxs = "i", axes = FALSE,
      xlab = "Poids (kg)", ylab = "K (besoin = BEE standard × K)")
    graphics::title(refs$reference_name[r], adj = 0, font.main = 1, cex.main = 1, col.main = "#1f1f1e")
    show_text <- if (is.null(labels)) length(w) * length(k) <= 300 else labels
    text_cex <- max(0.55, min(0.9, 11 / max(length(w), length(k))))
    fills <- vn_balance_fill(b)
    for (i in seq_len(nrow(b))) {
      x <- match(b$weight_kg[i], w); y <- match(b$K[i], k)
      z <- match(b$zone[i], zones$zone)
      # 1px surface gap between tiles.
      graphics::rect(we[x], ke[y], we[x + 1], ke[y + 1], col = fills[i], border = "white", lwd = 1)
      if (!is.na(zones$angle[z]))
        graphics::rect(we[x], ke[y], we[x + 1], ke[y + 1], density = 12, angle = zones$angle[z], col = "#ffffff99", border = NA)
      if (show_text)
        graphics::text(b$weight_kg[i], b$K[i], vn_balance_label(b[i, ]), cex = text_cex,
          col = if (zones$zone[z] %in% c("NON_EVALUABLE", "SOUS_RESERVE") ||
            (zones$zone[z] == "SEUILS_NON_RESPECTES" && isTRUE(b$min_failed[i] <= 1))) "#1f1f1e" else "white", font = 2)
    }
    at_w <- if (length(w) > 12) pretty(w) else w
    at_k <- if (length(k) > 12) pretty(k) else k
    graphics::axis(1, at = at_w[at_w >= min(we) & at_w <= max(we)], lwd = 0, lwd.ticks = 1, tcl = -0.3)
    graphics::axis(2, at = at_k[at_k >= min(ke) & at_k <= max(ke)], lwd = 0, lwd.ticks = 1, tcl = -0.3)
  }
  if (legend) {
    for (extra in seq_len(cols * ceiling(nrow(refs) / cols) - nrow(refs))) graphics::plot.new()
    vn_plot_balance_legend()
  }
  invisible(balance)
}

#' Legend of the balance map (zones, hatching and tile labels).
#' @export
vn_plot_balance_legend <- function() {
  zones <- vn_balance_zones()
  graphics::par(mar = c(0, 0, 0, 0))
  graphics::plot.new()
  zones$label[zones$zone == "SEUILS_NON_RESPECTES"] <- paste(zones$label[zones$zone == "SEUILS_NON_RESPECTES"],
    "(clair = 1 seuil, foncé = 5 et plus)")
  graphics::legend("center", legend = zones$label, fill = zones$colour, border = "white", bty = "n",
    ncol = 1, density = NA, cex = 0.9, text.col = "#1f1f1e",
    title = "Libellé : n/N combinaisons qui y parviennent ; sinon nombre minimal de seuils renseignés non respectés", title.col = "#555550")
  graphics::legend("center", legend = zones$label, fill = "#ffffff99", border = NA, bty = "n", ncol = 1,
    density = ifelse(is.na(zones$angle), 0, 12), angle = ifelse(is.na(zones$angle), 0, zones$angle), cex = 0.9,
    text.col = NA, title = " ", title.col = NA)
  invisible(zones)
}

#' List the nutrients not covered across an exploration.
#' One row per nutrient and threshold level missed by at least one evaluated scenario:
#' scenarios concerned, those explained only by absent values counted as zero, and
#' weight × K cells where every evaluated combination misses it (never covered).
#' @export
vn_uncovered_nutrients <- function(exploration, model = NULL) {
  s <- if (is.data.frame(exploration)) exploration else exploration$summary
  if (is.null(model) && !is.data.frame(exploration)) model <- exploration$model
  required <- c("reference_id", "weight_kg", "K", "violated", "violated_documented")
  if (!is.data.frame(s) || !all(required %in% names(s))) stop("Résultat d'exploration requis")
  # Scenarios that reached the threshold comparison (errors leave insufficient as NA).
  evaluated <- if ("insufficient" %in% names(s)) s[!is.na(s$insufficient), , drop = FALSE] else s
  empty <- data.frame(nutrient_id = character(), reflevel = character(), direction = character(),
    scenarios = integer(), share = numeric(), absent_data_only = integer(), cells_never_covered = integer(),
    cells = integer(), name = character(), unit = character())
  if (!nrow(evaluated)) return(empty)
  split_items <- function(x) strsplit(x, ";", fixed = TRUE)
  all_items <- split_items(evaluated$violated)
  documented <- split_items(evaluated$violated_documented)
  items <- sort(unique(unlist(all_items)))
  if (!length(items)) return(empty)
  cell <- paste(evaluated$reference_id, evaluated$weight_kg, evaluated$K, sep = "\r")
  rows <- lapply(items, function(item) {
    hit <- vapply(all_items, function(v) item %in% v, TRUE)
    doc <- vapply(documented, function(v) item %in% v, TRUE)
    per_cell <- tapply(hit, cell, all)
    parts <- strsplit(item, " ", fixed = TRUE)[[1]]
    data.frame(nutrient_id = parts[1], reflevel = parts[2],
      direction = if (parts[2] %in% c("MIN", "OPTIMIN")) "Insuffisant" else "Excès",
      scenarios = sum(hit), share = sum(hit) / length(hit), absent_data_only = sum(hit & !doc),
      cells_never_covered = sum(per_cell), cells = length(per_cell), stringsAsFactors = FALSE)
  })
  out <- do.call(rbind, rows)
  dict <- if (!is.null(model)) model$dictionaries$nutrients else NULL
  out$name <- if (is.null(dict)) out$nutrient_id else dict$name[match(out$nutrient_id, dict$nutrient_id)]
  out$unit <- if (is.null(dict)) NA_character_ else dict$unit[match(out$nutrient_id, dict$nutrient_id)]
  out <- out[order(-out$cells_never_covered, -out$scenarios, out$nutrient_id), ]
  rownames(out) <- NULL
  out
}

# Nutrients with at least one effective threshold in a general canine reference, labelled for the UI.
vn_evaluable_nutrients <- function(model) {
  refs <- model$references$reference_id[model$references$maladie == "FALSE"]
  ids <- unique(model$requirements$nutrient_id[model$requirements$reference_id %in% refs & model$requirements$is_effective])
  dict <- model$dictionaries$nutrients
  ids <- ids[order(match(ids, dict$nutrient_id))]
  setNames(ids, paste0(dict$name[match(ids, dict$nutrient_id)], " (", ids, ")"))
}
