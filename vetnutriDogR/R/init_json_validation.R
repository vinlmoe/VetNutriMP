vn_validate_init_structure <- function(raw) {
  fail <- function(p) stop("Structure INIT invalide : ", p, call. = FALSE)
  scalar <- function(x, type, p) {
    ok <- length(x) == 1L && !is.na(x) && switch(type,
      text = is.character(x), number = is.numeric(x) && is.finite(x), boolean = is.logical(x))
    if (!ok) fail(p)
  }
  if (!is.list(raw) || is.null(names(raw))) fail("objet racine attendu")
  scalar(raw$version, "text", "version")
  scalar(raw$generatedAtEpochMs, "number", "generatedAtEpochMs")
  for (section in c("animals", "foods", "equations", "references", "biblioRefs")) {
    if (!is.list(raw[[section]]) || !is.null(names(raw[[section]]))) fail(paste0(section, " : tableau attendu"))
    ids <- character()
    for (i in seq_along(raw[[section]])) {
      obj <- raw[[section]][[i]]; p <- paste0(section, "/", i - 1L)
      if (!is.list(obj)) fail(p)
      scalar(obj$uuid, "text", paste0(p, "/uuid"))
      if (!nzchar(obj$uuid) || obj$uuid %in% ids) fail(paste0(p, " : identifiant vide ou dupliqué"))
      ids <- c(ids, obj$uuid)
      if (section == "foods") {
        for (key in c("nutrients", "energyPerSpecies")) {
          map <- obj[[key]]
          if (!is.null(map) && (!is.list(map) || (length(map) && is.null(names(map))))) fail(paste(p, key))
          if (anyDuplicated(names(map))) fail(paste(p, key, "clés dupliquées"))
          for (v in map) scalar(v, "number", paste(p, key))
        }
        for (key in c("name", "group", "kind")) if (!is.null(obj[[key]])) scalar(obj[[key]], "text", paste(p, key))
        for (key in c("deprecated", "consistent")) if (!is.null(obj[[key]])) scalar(obj[[key]], "boolean", paste(p, key))
      }
      if (section == "equations") for (key in c("name", "kind", "script")) scalar(obj[[key]], "text", paste(p, key))
      if (section == "references") {
        for (key in c("nutrients", "coefficients", "equationsNut")) {
          if (!is.null(obj[[key]]) && (!is.list(obj[[key]]) || !is.null(names(obj[[key]])))) fail(paste(p, key, "tableau attendu"))
        }
        for (coef in obj$coefficients) {
          for (key in c("uuid", "groupType", "description")) scalar(coef[[key]], "text", paste(p, "coefficients", key))
          for (key in c("coef", "groupUUID")) scalar(coef[[key]], "number", paste(p, "coefficients", key))
        }
        for (key in c("nom", "espece", "stadePhysio")) scalar(obj[[key]], "text", paste(p, key))
        for (n in obj$nutrients) {
          for (key in c("nutrientLabel", "reflevel")) scalar(n[[key]], "text", paste(p, key))
          for (key in c("quantity", "uniteReqId")) scalar(n[[key]], "number", paste(p, key))
        }
      }
    }
  }
  invisible(TRUE)
}

#' Inspect unresolved links and unsupported or absent data without fabricating it.
#' @export
vn_validate_init <- function(model) {
  issues <- list()
  add <- function(code, id, field, detail) {
    issues[[length(issues) + 1L]] <<- data.frame(code = code, object_id = id,
      field = field, detail = detail, source_json = model$provenance$source_json)
  }
  dict <- model$dictionaries$nutrients$nutrient_id
  for (id in setdiff(unique(c(model$food_nutrients$nutrient_id, model$requirements$nutrient_id)), dict))
    add("unknown_nutrient", id, "nutrient_id", "Unité/interprétation absente du dictionnaire Kotlin")
  if ("is_effective" %in% names(model$requirements)) {
    for (i in which(!model$requirements$is_effective))
      add("superseded_requirement", model$requirements$reference_id[i], model$requirements$nutrient_id[i],
          paste("Le dernier seuil du même niveau remplace celui-ci :", model$requirements$source_pointer[i]))
  }
  eqids <- vapply(model$raw$equations, `[[`, "", "uuid")
  bibids <- vapply(model$raw$biblioRefs, `[[`, "", "uuid")
  foodids <- vapply(model$raw$foods, `[[`, "", "uuid")
  for (r in model$raw$references) {
    for (field in c("equationBW", "equationBEE", "equationDEcom", "equationDEraw", "equationME", "equationsNut")) {
      for (id in unlist(r[[field]])) if (nzchar(id) && !id %in% eqids) add("unresolved_equation", r$uuid, field, id)
    }
    for (n in r$nutrients) {
      if (!is.null(n$biblioRefId) && !n$biblioRefId %in% bibids) add("unresolved_bibliography", r$uuid, n$nutrientLabel, n$biblioRefId)
      if (!n$reflevel %in% c("MIN", "MAX", "OPTIMIN", "OPTIMAX")) add("default_reflevel", r$uuid, "reflevel", n$reflevel)
      if (!n$uniteReqId %in% model$dictionaries$requirement_units$uniteReqId) add("default_unit", r$uuid, "uniteReqId", as.character(n$uniteReqId))
    }
  }
  for (f in model$raw$foods) {
    for (id in unlist(f$biblioRefIds)) if (!id %in% bibids) add("unresolved_bibliography", f$uuid, "biblioRefIds", id)
  }
  for (r in model$raw$recipes) for (item in r$aliments)
    if (!item$foodId %in% foodids) add("unresolved_food", r$uuid, "aliments.foodId", item$foodId)
  # Absence is reported in aggregate to keep an 11,000-food catalogue readable.
  for (nutrient in c("HUMIDITE", "DM", "ENERGIE")) {
    present <- vapply(model$raw$foods, function(f) nutrient %in% names(f$nutrients), logical(1))
    missing <- foodids[!present]
    if (length(missing)) add("missing_direct_value", "foods", nutrient,
      sprintf("%d aliments sans valeur brute ; calcul possible uniquement via une règle explicite", length(missing)))
  }
  vn_bind_rows(issues, data.frame(code = character(), object_id = character(), field = character(), detail = character(), source_json = character()))
}
