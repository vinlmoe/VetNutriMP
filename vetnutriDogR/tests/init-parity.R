# Run from repository root: Rscript vetnutriDogR/tests/init-parity.R
# This script also runs under R CMD check when VETNUTRI_MP_ROOT is set.
module_root <- Sys.getenv("VETNUTRI_DOG_ROOT", if (dir.exists("vetnutriDogR/R"))
  normalizePath("vetnutriDogR") else if (file.exists("R/init_json_loader.R")) getwd() else "")
if (nzchar(module_root)) {
  for (file in list.files(file.path(module_root, "R"), pattern = "\\.R$", full.names = TRUE)) source(file)
} else {
  library(vetnutriDogR)
  for (name in c("vn_math", "vn_validate_init_structure", "vn_food_values", "vn_food_energy", "vn_init_object", "vn_food_for_dog"))
    assign(name, getFromNamespace(name, "vetnutriDogR"))
}
root <- Sys.getenv("VETNUTRI_MP_ROOT", if (dir.exists("composeApp")) getwd() else
  if (nzchar(module_root) && dir.exists(file.path(dirname(module_root), "composeApp"))) dirname(module_root) else "")
if (nzchar(root)) {
  model <- suppressWarnings(vn_load_init(root = root))
  raw <- jsonlite::fromJSON(model$provenance$source_json, simplifyVector = FALSE)
  stopifnot(nrow(model$foods) == sum(vapply(raw$foods, vn_food_for_dog, logical(1))),
            all(model$references$espece == "CHIEN"),
            nrow(model$equations) == length(raw$equations))
  # All original compositions and all four threshold levels, not a duplicated catalogue.
  for (kind in unique(model$foods$kind)) {
    idx <- which(model$foods$kind == kind)[1]
    f <- vn_init_object(model, "foods", model$foods$food_id[idx])
    rows <- model$food_nutrients[model$food_nutrients$food_id == f$uuid, ]
    stopifnot(identical(model$foods$name[idx], f$name), identical(model$foods$group[idx], f$group),
              identical(rows$nutrient_id, names(f$nutrients)),
              identical(rows$raw_value, as.numeric(unlist(f$nutrients))), !anyNA(rows$unit))
  }
  for (ref in Filter(function(r) r$espece == "CHIEN", raw$references)) {
    rows <- model$requirements[model$requirements$reference_id == ref$uuid, ]
    stopifnot(nrow(rows) == length(ref$nutrients))
    for (j in seq_along(ref$nutrients)) {
      n <- ref$nutrients[[j]]
      stopifnot(rows$nutrient_id[j] == n$nutrientLabel, rows$quantity[j] == n$quantity,
        rows$raw_reflevel[j] == n$reflevel, rows$raw_uniteReqId[j] == n$uniteReqId,
        rows$species[j] == ref$espece, rows$stage[j] == ref$stadePhysio,
        rows$unit[j] == model$dictionaries$nutrients$unit[match(n$nutrientLabel, model$dictionaries$nutrients$nutrient_id)])
    }
  }
  fails <- function(expr) inherits(tryCatch(force(expr), error = identity), "error")
  catref <- Filter(function(r) r$espece == "CHAT", raw$references)[[1]]
  stopifnot(fails(vn_init_needs(model, catref$uuid, 10)))
  stopifnot(vn_math("K/NA", setNames(list(4, 2), c("K", "NA"))) == 2)
  stopifnot(vn_math("if(wG > 5, 2, 1)", list(wG = 6)) == 2)
  stopifnot(fails(vn_math('system("touch /tmp/should-never-exist")')),
            fails(vn_math("1;2")), fails(vn_math("BW", list())), fails(vn_math("1/0")))
  bad <- raw; bad$foods[[2]]$uuid <- bad$foods[[1]]$uuid
  stopifnot(fails(vn_validate_init_structure(bad)))
  bad <- raw; bad$foods[[1]]$nutrients$LIPIDE <- "bad"
  stopifnot(fails(vn_validate_init_structure(bad)))
  refid <- raw$references[[1]]$uuid
  needs <- vn_init_needs(model, refid, 10)
  stopifnot(abs(needs$standard_kcal - 70 * 10^0.75) < 1e-8)
  stopifnot(vn_init_needs(model, refid, 10, 8)$weight_kg == 8)
  result <- suppressWarnings(vn_init_ration(model, refid, data.frame(food_id = raw$foods[[1]]$uuid, quantity_g = 4), needs))
  stopifnot(all(result$comparison$status[is.na(result$comparison$intake)] == "DONNEES_ABSENTES"))
  per_energy <- result$comparison$uniteReqId == 1
  stopifnot(all.equal(result$comparison$absolute_requirement[per_energy],
    result$comparison$quantity[per_energy] * needs$standard_kcal / 1000))
  # Changes and new records become visible on the next load, no code regeneration.
  tiny <- raw; tiny$foods <- raw$foods[1:2]; tiny$recipes <- list()
  tiny$foods[[2]]$uuid <- "test-new-id"; tiny$foods[[2]]$nutrients$PROTEINE <- 12.345
  tiny$foods[[1]]$nutrients$PROTEINE <- -3
  tiny$foods[[1]]$nutrients$ARGININE <- 1
  tiny$foods[[1]]$dataB <- "VF24"
  tiny$references[[1]]$nutrients[[1]]$uniteReqId <- 999
  tiny$references[[1]]$nutrients[[1]]$reflevel <- "UNKNOWN"
  tmp <- tempfile(fileext = ".json")
  jsonlite::write_json(tiny, tmp, auto_unbox = TRUE, digits = NA)
  refreshed <- suppressWarnings(vn_load_init(tmp, root = root))
  stopifnot("test-new-id" %in% refreshed$foods$food_id,
    refreshed$food_nutrients$raw_value[refreshed$food_nutrients$food_id == "test-new-id" & refreshed$food_nutrients$nutrient_id == "PROTEINE"] == 12.345)
  negative <- refreshed$food_nutrients[refreshed$food_nutrients$food_id == tiny$foods[[1]]$uuid & refreshed$food_nutrients$nutrient_id == "PROTEINE", ]
  amino <- refreshed$food_nutrients[refreshed$food_nutrients$food_id == tiny$foods[[1]]$uuid & refreshed$food_nutrients$nutrient_id == "ARGININE", ]
  stopifnot(negative$raw_value == -3, negative$value == 0, amino$raw_value == 1, is.na(amino$value),
    refreshed$requirements$raw_uniteReqId[1] == 999, refreshed$requirements$uniteReqId[1] == 0,
    refreshed$requirements$raw_reflevel[1] == "UNKNOWN", refreshed$requirements$reflevel[1] == "MIN")
  unlink(tmp)
  oracle_path <- file.path(if (nzchar(module_root)) module_root else file.path(root, "vetnutriDogR"), "build/kotlin-parity.json")
  if (file.exists(oracle_path)) {
    oracle <- jsonlite::fromJSON(oracle_path, simplifyVector = FALSE)
    stopifnot(oracle$source_md5 == model$provenance$md5)
    for (record in oracle$records) {
      f <- vn_init_object(model, "foods", record$food_id)
      ref <- vn_init_object(model, "references", record$reference_id)
      vals <- suppressWarnings(vn_food_values(model, f, ref))
      energy <- vn_food_energy(model, f, ref, vals)
      stopifnot(isTRUE(all.equal(energy$value, record$energy, tolerance = 1e-8)),
                identical(f$name, record$name), identical(f$group, record$group), identical(f$kind, record$kind))
      for (label in names(record$units)) {
        unit <- model$dictionaries$nutrients$unit[match(label, model$dictionaries$nutrients$nutrient_id)]
        stopifnot(identical(unit, record$units[[label]]))
      }
      for (label in setdiff(names(record$nutrients), "ENERGIE")) {
        expected <- record$nutrients[[label]]
        actual <- unname(vals$values[label])
        if (is.null(expected)) stopifnot(is.na(actual)) else stopifnot(isTRUE(all.equal(actual, expected, tolerance = 1e-8)))
      }
      needs <- vn_init_needs(model, ref$uuid, 10)
      stopifnot(isTRUE(all.equal(needs$standard_kcal, record$standard_kcal, tolerance = 1e-8)),
                isTRUE(all.equal(needs$metabolic_weight, record$metabolic_weight, tolerance = 1e-8)))
    }
    for (rid in unique(vapply(oracle$requirements, `[[`, "", "reference_id"))) {
      needs <- vn_init_needs(model, rid, 10)
      calculated <- vn_init_ration(model, rid, data.frame(food_id = model$foods$food_id[1], quantity_g = 100), needs)$comparison
      for (expected in Filter(function(x) x$reference_id == rid, oracle$requirements)) {
        row <- calculated[calculated$nutrient_id == expected$nutrient_id & calculated$reflevel == expected$reflevel, ]
        stopifnot(nrow(row) == 1L, row$quantity == expected$quantity, row$unit == expected$unit,
          row$uniteReqId == expected$uniteReqId, row$species == expected$species, row$stage == expected$stage,
          isTRUE(all.equal(row$absolute_requirement, expected$absolute_requirement, tolerance = 1e-8)))
      }
    }
    cat(length(oracle$records), "comparaisons aliments/besoins et", length(oracle$requirements), "seuils Kotlin/R validés\n")
  } else warning("Oracle Kotlin absent : exécuter scripts/validate-kotlin.sh pour valider la parité de calcul")
  cat("Tests INIT R réussis\n")
} else message("INIT integration tests skipped: set VETNUTRI_MP_ROOT to the source checkout")
