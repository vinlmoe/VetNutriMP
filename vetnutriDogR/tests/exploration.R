# Deterministic algorithm checks use synthetic matrices, never catalogue defaults.
module_root <- Sys.getenv("VETNUTRI_DOG_ROOT", if (dir.exists("vetnutriDogR/R")) normalizePath("vetnutriDogR") else if (file.exists("R/ration_exploration.R")) getwd() else "")
if (nzchar(module_root)) {
  for (file in list.files(file.path(module_root, "R"), pattern = "\\.R$", full.names = TRUE)) source(file)
} else {
  library(vetnutriDogR)
  for (name in c("vn_exploration_roles", "vn_adjust_combination", "vn_resolve_exploration_targets"))
    assign(name, getFromNamespace(name, "vetnutriDogR"))
}
failed <- function(expr) inherits(tryCatch(force(expr), error = identity), "error")
roles <- vn_exploration_roles()
matrix <- diag(6)
rownames(matrix) <- roles$nutrient_id
colnames(matrix) <- roles$role
matrix["ENERGIE", ] <- c(2, 1, 0, 1, 0, 4)
profiles <- setNames(lapply(seq_len(6), function(i) list(values = matrix[, i], messages = character(), error = NULL)), roles$role)
selection <- setNames(roles$role, roles$role)
targets <- vn_exploration_targets()
targets$absolute_target <- c(2, 3, 1, 1, 1, 10)
fit <- vn_adjust_combination(profiles, selection, targets)
stopifnot(isTRUE(all.equal(unname(fit$quantities), c(2, 3, 1, 1, 1, 0.5))), fit$totals["ENERGIE"] == 10)
targets$absolute_target[6] <- 5
fit <- vn_adjust_combination(profiles, selection, targets)
stopifnot(fit$quantities["energy"] == 0, fit$totals["ENERGIE"] == 8)
profiles$protein$values["CAL"] <- NA_real_
stopifnot(failed(vn_adjust_combination(profiles, selection, targets)))
stopifnot(identical(vn_exploration_interval(5, 12, 5), c(5, 10, 12)),
  failed(vn_exploration_interval(0, 10, 1)), failed(vn_exploration_interval(10, 5, 1)), failed(vn_exploration_interval(5, 10, 0)))
root <- Sys.getenv("VETNUTRI_MP_ROOT", if (dir.exists("composeApp")) getwd() else if (nzchar(module_root) && dir.exists(file.path(dirname(module_root), "composeApp"))) dirname(module_root) else "")
if (nzchar(root)) {
  m <- suppressWarnings(vn_load_init(root = root))
  ref <- m$references$reference_id[m$references$nom == "Adulte >25kg"]
  stopifnot(length(ref) == 1L)
  labels <- setdiff(roles$nutrient_id, "ENERGIE")
  eligible <- Filter(function(f) f$uuid %in% m$foods$food_id &&
    all(labels %in% names(f$nutrients)) && all(unlist(f$nutrients[labels]) > 0), m$raw$foods)
  stopifnot(length(eligible) >= 2L)
  lists <- setNames(rep(list(eligible[[1]]$uuid), 6), roles$role)
  lists$protein <- vapply(eligible[1:2], `[[`, "", "uuid")
  out <- vn_explore_rations(m, ref, lists, c(5, 10), c(0.8, 1.2))
  stopifnot(nrow(out$summary) == 8L, nrow(out$combinations) == 2L,
    nrow(out$quantities) == 48L, !anyDuplicated(out$summary$scenario_id), all(out$quantities$quantity_g >= 0),
    all(out$summary$need_kcal == out$summary$standard_kcal * out$summary$K))
  for (i in seq_len(nrow(out$summary))) {
    s <- out$summary[i, ]
    q <- out$quantities[out$quantities$scenario_id == s$scenario_id, ]
    n <- vn_init_needs(m, s$reference_id, s$weight_kg, adjustment = s$K)
    manual <- vn_init_ration(m, s$reference_id, q[, c("food_id", "quantity_g")], n)
    stopifnot(isTRUE(all.equal(s$energy_kcal, manual$energy_kcal, tolerance = 1e-10)),
      s$insufficient == sum(manual$comparison$status == "INSUFFISANT"),
      s$excess == sum(manual$comparison$status == "EXCES"),
      s$missing == sum(manual$comparison$status == "DONNEES_ABSENTES"))
  }
  # Distinct K values must not rescale thresholds expressed per standard BEE.
  a <- out$targets[out$targets$nutrient_id == "PROTEINE", ]
  stopifnot(length(unique(a$absolute_target[out$summary$weight_kg == 5])) == 1L)
  n <- vn_init_needs(m, ref, 10)
  t <- vn_resolve_exploration_targets(m, ref, vn_exploration_targets(), n)
  fibre <- t[t$role == "fibre", ]
  stopifnot(fibre$absolute_target == fibre$reference_value * n$standard_kcal / 1000 * 5)
  custom <- vn_exploration_targets()
  custom$source[custom$role == "fibre"] <- "custom"
  custom$value[custom$role == "fibre"] <- 2 # explicit test override, not production data
  custom$uniteReqId[custom$role == "fibre"] <- 6
  custom$multiplier[custom$role == "fibre"] <- 1
  changed <- vn_resolve_exploration_targets(m, ref, custom, n)
  stopifnot(changed$absolute_target[changed$role == "fibre"] == 2)
  # Missing reference thresholds produce rows for every requested scenario.
  rer <- m$references$reference_id[m$references$nom == "Calcul sur RER"]
  absent <- vn_explore_rations(m, rer, lists, 10, 1)
  stopifnot(nrow(absent$summary) == 2L, all(absent$summary$status == "CIBLE_ABSENTE"))
  stopifnot(failed(vn_explore_rations(m, ref, lists, 1:10, seq(0.5, 1.5, 0.1), max_scenarios = 10)))
  empty <- lists; empty$protein <- character()
  stopifnot(failed(vn_explore_rations(m, ref, empty, 10, 1)))
  cat("Exploration : 8 scénarios exhaustifs, contrôles de cibles, erreurs et comparaison au moteur de ration validés\n")
} else message("Live INIT exploration tests skipped: set VETNUTRI_MP_ROOT")
