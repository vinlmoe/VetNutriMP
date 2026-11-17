# INIT is read on every load; no bundled copy or hidden session cache.
vn_find_root <- function(root = Sys.getenv("VETNUTRI_MP_ROOT", "")) {
  if (!nzchar(root)) root <- Sys.getenv("VETNUTRI_MP_ROOT", "")
  if (nzchar(root)) {
    root <- normalizePath(root, mustWork = TRUE)
    if (!dir.exists(file.path(root, "composeApp/src/commonMain"))) stop("Racine VetNutri MP invalide")
    return(root)
  }
  root <- normalizePath(getwd())
  repeat {
    if (dir.exists(file.path(root, "composeApp/src/commonMain/resources/data"))) return(root)
    parent <- dirname(root)
    if (parent == root) stop("Indiquer root ou VETNUTRI_MP_ROOT (dépôt VetNutri MP)")
    root <- parent
  }
}

#' Locate the four platform INIT files (never build products or dated backups).
#' @export
vn_locate_init <- function(root = Sys.getenv("VETNUTRI_MP_ROOT", "")) {
  root <- vn_find_root(root)
  paths <- file.path(root, c(
    "composeApp/src/commonMain/resources/data/vetnutri_export_init.json",
    "composeApp/src/androidMain/assets/data/vetnutri_export_init.json",
    "iosApp/iosApp/vetnutri_export_init.json",
    "iosApp/iosApp/Resources/vetnutri_export_init.json"))
  data.frame(platform = c("common", "android", "ios", "ios_resources"),
             source_json = paths, exists = file.exists(paths),
             md5 = unname(tools::md5sum(paths)), stringsAsFactors = FALSE)
}

#' Load and normalize the live VetNutri INIT catalogue.
#' @export
vn_load_init <- function(path = NULL, root = Sys.getenv("VETNUTRI_MP_ROOT", ""), strict = FALSE) {
  root <- vn_find_root(root)
  copies <- vn_locate_init(root)
  if (is.null(path)) {
    path <- copies$source_json[1L]
    if (length(unique(na.omit(copies$md5))) > 1L)
      warning("Copies INIT divergentes : la source commonMain est utilisée", call. = FALSE)
  }
  path <- normalizePath(path, mustWork = TRUE)
  raw <- jsonlite::fromJSON(path, simplifyVector = FALSE)
  vn_validate_init_structure(raw)
  dictionaries <- vn_kotlin_dictionaries(root)
  model <- vn_normalize_init(raw, path, dictionaries)
  model$raw <- raw
  model$root <- root
  model$platform_files <- copies
  model$provenance <- data.frame(source_json = path, md5 = unname(tools::md5sum(path)),
                                 version = raw$version, generatedAtEpochMs = raw$generatedAtEpochMs)
  model$diagnostics <- vn_validate_init(model)
  # Scope requested by the module: dogs. Raw envelope remains intact for audit.
  keep <- vapply(raw$foods, vn_food_for_dog, logical(1))
  model$foods <- model$foods[keep, , drop = FALSE]
  model$food_nutrients <- model$food_nutrients[model$food_nutrients$food_id %in% model$foods$food_id, , drop = FALSE]
  model$food_energy <- model$food_energy[model$food_energy$food_id %in% model$foods$food_id & model$food_energy$species == "CHIEN", , drop = FALSE]
  model$references <- model$references[model$references$espece == "CHIEN", , drop = FALSE]
  model$requirements <- model$requirements[model$requirements$reference_id %in% model$references$reference_id, , drop = FALSE]
  model$coefficients <- model$coefficients[model$coefficients$reference_id %in% model$references$reference_id, , drop = FALSE]
  model$categories <- unique(model$foods[, c("group", "kind"), drop = FALSE])
  model$species <- "CHIEN"
  model$stages <- unique(model$references$stadePhysio)
  class(model) <- "vetnutri_init"
  if (nrow(model$diagnostics)) {
    msg <- sprintf("INIT : %d diagnostics ; consulter $diagnostics", nrow(model$diagnostics))
    if (strict) stop(msg, call. = FALSE) else warning(msg, call. = FALSE)
  }
  model
}

# FoodSearchComponent.matchesEspece: no restriction, ALL, or explicit dog.
vn_food_for_dog <- function(food) {
  species <- toupper(trimws(unlist(food$species)))
  !length(species) || any(species %in% c("CHIEN", "DOG", "0", "CH", "ALL", "2"))
}
