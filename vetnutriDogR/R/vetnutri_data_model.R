vn_bind_rows <- function(rows, prototype) {
  rows <- Filter(function(x) !is.null(x) && nrow(x) > 0L, rows)
  if (!length(rows)) return(prototype)
  out <- do.call(rbind, rows)
  rownames(out) <- NULL
  out
}
vn_scalar <- function(x, fallback = NA_character_) if (is.null(x)) fallback else x

# Read enum metadata from the checked-out Kotlin sources, rather than maintain
# a second nutrient/unit catalogue in R. Unsupported source syntax fails closed.
vn_kotlin_dictionaries <- function(root) {
  folder <- file.path(root, "composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Enumer")
  read_enum <- function(name) {
    path <- file.path(folder, paste0(name, ".kt"))
    txt <- paste(readLines(path, warn = FALSE, encoding = "UTF-8"), collapse = "\n")
    txt <- strsplit(txt, "companion object", fixed = TRUE)[[1]][1]
    starts <- gregexpr('(?m)^[ \t]*(?!listOf\\b)[A-Za-z][A-Za-z0-9_]*\\s*\\(\\s*"', txt, perl = TRUE)[[1]]
    if (starts[1] < 0) stop("Schéma Kotlin non reconnu : ", path)
    ends <- c(starts[-1] - 1L, nchar(txt))
    lapply(seq_along(starts), function(i) {
      block <- substr(txt, starts[i], ends[i])
      strings <- regmatches(block, gregexpr('"[^"\n]*"', block, perl = TRUE))[[1]]
      strings <- substring(strings, 2L, nchar(strings) - 1L)
      unit <- regmatches(block, regexpr('UnitEnum\\.[A-Za-z0-9_]+', block))
      list(name = sub('(?s)^\\s*([A-Za-z][A-Za-z0-9_]*).*', '\\1', block, perl = TRUE),
           strings = strings, unit = sub("UnitEnum.", "", unit, fixed = TRUE), source_kotlin = path)
    })
  }
  unit_entries <- read_enum("UnitEnum")
  units <- setNames(vapply(unit_entries, function(e) e$strings[1], ""),
                    vapply(unit_entries, `[[`, "", "name"))
  classes <- c("NutrientMain", "NutrientMacro", "NutrientMin", "NutrientLipid",
               "NutrientVitam", "NutrientOther", "AAEnum", "NutrientAnalysis", "NutrientEnergy")
  nutrients <- do.call(rbind, lapply(classes, function(cls) {
    do.call(rbind, lapply(read_enum(cls), function(e) {
      label_index <- if (cls == "AAEnum") 2L else if (cls == "NutrientVitam") 4L else 3L
      unit_id <- if (length(e$unit)) e$unit else "NO"
      if (!unit_id %in% names(units)) stop("Unité Kotlin inconnue : ", unit_id)
      data.frame(nutrient_id = e$strings[label_index], name = e$strings[1],
                 unit_id = unit_id, unit = unname(units[unit_id]),
                 display_unit = if (cls == "AAEnum") "g" else e$strings[2],
                 is_ratio = cls == "NutrientAnalysis" && e$strings[2] == "",
                 enum_class = cls, source_kotlin = e$source_kotlin)
    }))
  }))
  if (anyNA(nutrients$nutrient_id) || anyDuplicated(nutrients$nutrient_id)) stop("Dictionnaire Kotlin ambigu")
  req_path <- file.path(folder, "UnitReqEnum.kt")
  lines <- readLines(req_path, warn = FALSE, encoding = "UTF-8")
  lines <- grep('^\\s*[A-Z]+\\([0-9]+, "', lines, value = TRUE)
  req <- data.frame(unit_requirement = sub('^\\s*([A-Z]+)\\(.*', '\\1', lines),
                    uniteReqId = as.integer(sub('.*\\(([0-9]+),.*', '\\1', lines)),
                    label = sub('.*"([^"]+)".*', '\\1', lines), source_kotlin = req_path)
  list(nutrients = nutrients, requirement_units = req)
}

vn_normalize_init <- function(raw, path, dictionaries) {
  scalar_table <- function(objects, id_name) {
    if (!length(objects)) return(data.frame())
    keys <- unique(unlist(lapply(objects, function(x) names(x)[vapply(x, function(v)
      is.atomic(v) && length(v) == 1L, logical(1))])))
    out <- as.data.frame(setNames(lapply(keys, function(k) vapply(objects, function(x)
      as.character(vn_scalar(x[[k]])), "")), keys), stringsAsFactors = FALSE)
    names(out)[names(out) == "uuid"] <- id_name
    out$source_json <- path
    out$source_pointer <- paste0("/", switch(id_name, food_id = "foods", reference_id = "references",
      equation_id = "equations", biblio_id = "biblioRefs"), "/", seq_along(objects) - 1L)
    out
  }
  foods <- scalar_table(raw$foods, "food_id")
  # Keep optional fields present even for minimal/empty catalogues.
  for (key in c("food_id", "name", "group", "kind")) if (!key %in% names(foods)) foods[[key]] <- rep(NA_character_, nrow(foods))
  for (key in c("deprecated", "consistent")) foods[[key]] <- vapply(raw$foods, function(f) vn_scalar(f[[key]], FALSE), logical(1))
  foods$species <- I(lapply(raw$foods, function(f) unlist(f$species, use.names = FALSE)))
  foods$raw <- I(raw$foods)
  nutrients <- vn_bind_rows(lapply(seq_along(raw$foods), function(i) {
    f <- raw$foods[[i]]; n <- unlist(f$nutrients, use.names = TRUE)
    if (!length(n)) return(NULL)
    data.frame(food_id = f$uuid, nutrient_id = names(n), raw_value = as.numeric(n), value = as.numeric(n),
               source_json = path, source_pointer = paste0("/foods/", i - 1L, "/nutrients/", names(n)))
  }), data.frame(food_id = character(), nutrient_id = character(), raw_value = double(), value = double(), source_json = character(), source_pointer = character()))
  meta <- dictionaries$nutrients[match(nutrients$nutrient_id, dictionaries$nutrients$nutrient_id), ]
  nutrients$value <- pmax(nutrients$raw_value, 0)
  bases <- vapply(raw$foods, function(f) vn_scalar(f$dataB), "")
  masked <- meta$enum_class == "AAEnum" & bases[match(nutrients$food_id, foods$food_id)] == "VF24"
  nutrients$value[which(masked)] <- NA_real_
  nutrients$unit_id <- meta$unit_id
  nutrients$unit <- meta$unit
  nutrients$source_kotlin <- meta$source_kotlin
  energies <- vn_bind_rows(lapply(seq_along(raw$foods), function(i) {
    f <- raw$foods[[i]]; n <- unlist(f$energyPerSpecies)
    if (!length(n)) return(NULL)
    data.frame(food_id = f$uuid, species = names(n), raw_value = as.numeric(n),
               value = ifelse(n > 0, n, NA_real_), source_json = path)
  }), data.frame(food_id = character(), species = character(), raw_value = double(), value = double(), source_json = character()))
  refs <- scalar_table(raw$references, "reference_id")
  requirements <- vn_bind_rows(lapply(seq_along(raw$references), function(i) {
    r <- raw$references[[i]]
    vn_bind_rows(lapply(seq_along(r$nutrients), function(j) {
      n <- r$nutrients[[j]]
      data.frame(reference_id = r$uuid, nutrient_id = n$nutrientLabel,
        raw_reflevel = n$reflevel, reflevel = if (n$reflevel %in% c("MIN", "MAX", "OPTIMIN", "OPTIMAX")) n$reflevel else "MIN",
        quantity = n$quantity, raw_uniteReqId = n$uniteReqId,
        uniteReqId = if (n$uniteReqId %in% dictionaries$requirement_units$uniteReqId) n$uniteReqId else 0L,
        biblioRefId = vn_scalar(n$biblioRefId), species = r$espece, stage = r$stadePhysio,
        disease = vn_scalar(r$maladie, FALSE), source_json = path,
        source_pointer = paste0("/references/", i - 1L, "/nutrients/", j - 1L))
    }), data.frame())
  }), data.frame(reference_id = character(), nutrient_id = character(), raw_reflevel = character(),
      reflevel = character(), quantity = double(), raw_uniteReqId = integer(), uniteReqId = integer(),
      biblioRefId = character(), species = character(), stage = character(), disease = logical(),
      source_json = character(), source_pointer = character()))
  requirements$is_effective <- !duplicated(requirements[, c("reference_id", "nutrient_id", "reflevel")], fromLast = TRUE)
  requirements$unit <- dictionaries$nutrients$unit[match(requirements$nutrient_id, dictionaries$nutrients$nutrient_id)]
  coefficients <- vn_bind_rows(lapply(raw$references, function(r) {
    vn_bind_rows(lapply(r$coefficients, function(c) data.frame(reference_id = r$uuid,
      coefficient_id = c$uuid, groupType = c$groupType, description = c$description,
      coef = c$coef, groupUUID = c$groupUUID, source_json = path)), data.frame())
  }), data.frame())
  list(foods = foods, food_nutrients = nutrients, food_energy = energies,
       references = refs, requirements = requirements, coefficients = coefficients,
       equations = scalar_table(raw$equations, "equation_id"),
       bibliography = scalar_table(raw$biblioRefs, "biblio_id"),
       categories = unique(foods[, c("group", "kind"), drop = FALSE]),
       species = unique(unlist(lapply(raw$foods, `[[`, "species"))),
       stages = unique(vapply(raw$references, `[[`, "", "stadePhysio")),
       dictionaries = dictionaries)
}
