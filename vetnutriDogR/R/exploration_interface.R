# Interface for population exploration. The manual ration application stays available.
vn_exploration_ui <- function(root = vn_find_root()) {
  roles <- vn_exploration_roles()
  defaults <- vn_exploration_targets()
  shiny::fluidPage(
    shiny::titlePanel("Exploration de rations canines"),
    shiny::p("Une combinaison = un ingrédient de chaque liste. Toutes les combinaisons sont croisées avec les poids, K et référentiels sélectionnés."),
    shiny::sidebarLayout(shiny::sidebarPanel(width = 4,
      shiny::textInput("explore_root", "Dépôt VetNutri MP", root),
      shiny::actionButton("explore_reload", "Charger / actualiser INIT"),
      shiny::selectizeInput("explore_refs", "Référentiels canins", choices = NULL, multiple = TRUE),
      shiny::h4("Grille de chiens"),
      shiny::fluidRow(shiny::column(4, shiny::numericInput("weight_from", "Poids min (kg)", 5, min = 0.01)),
        shiny::column(4, shiny::numericInput("weight_to", "Poids max (kg)", 30, min = 0.01)),
        shiny::column(4, shiny::numericInput("weight_by", "Pas (kg)", 5, min = 0.01))),
      shiny::fluidRow(shiny::column(4, shiny::numericInput("k_from", "K min", 0.8, min = 0.01)),
        shiny::column(4, shiny::numericInput("k_to", "K max", 1.2, min = 0.01)),
        shiny::column(4, shiny::numericInput("k_by", "Pas de K", 0.1, min = 0.01))),
      shiny::helpText("Besoin énergétique total = besoin standard × K. Les seuils nutritionnels conservent leur base Kotlin : besoin standard, poids ou poids métabolique."),
      shiny::textInput("explore_variables", "Variables communes aux profils (JSON : AW, wG, L, wL…)", "{}"),
      shiny::helpText("Les référentiels sont croisés avec tous les poids choisis ; aucun profil physiologique n'est déduit du poids."),
      shiny::h4("Listes d'ingrédients et cibles"),
      shiny::tagList(lapply(seq_len(nrow(roles)), function(i) {
        role <- roles$role[i]
        shiny::tags$details(open = TRUE,
          shiny::tags$summary(shiny::strong(roles$label[i])),
          shiny::selectizeInput(paste0("ingredients_", role), "Ingrédients possibles", choices = NULL, multiple = TRUE),
          if (role == "fibre") shiny::selectInput("fibre_nutrient", "Nutriment utilisé pour les fibres", choices = NULL),
          if (role != "energy") shiny::tagList(
            shiny::selectInput(paste0("target_source_", role), "Cible", c("Référentiel" = "reference", "Personnalisée" = "custom")),
            shiny::conditionalPanel(sprintf("input.target_source_%s == 'reference'", role),
              shiny::selectInput(paste0("target_level_", role), "Niveau du référentiel",
                c("Optimum minimum" = "OPTIMIN", "Minimum" = "MIN", "Optimum maximum" = "OPTIMAX", "Maximum" = "MAX"))),
            shiny::conditionalPanel(sprintf("input.target_source_%s == 'custom'", role),
              shiny::numericInput(paste0("target_value_", role), "Valeur (unité physique du nutriment)", NA, min = 0),
              shiny::selectInput(paste0("target_unit_", role), "Base de la cible personnalisée", choices = NULL)),
            shiny::numericInput(paste0("target_factor_", role), "Facteur appliqué à la cible", defaults$multiplier[i], min = 0, step = 0.1),
            if (role == "fibre") shiny::helpText("Pour CELLULOSE, Kotlin applique ×5 au seuil lors de l'ajustement. Ce facteur est modifiable ; les seuils de conformité restent ceux d'INIT.")
          ) else shiny::helpText("Quantité calculée en dernier pour compléter l'énergie déjà apportée. Un excédent énergétique est signalé, sans quantité négative.")
        )
      })),
      shiny::checkboxInput("explore_missing_zero", "Valeur absente = 0 (comme Kotlin)", FALSE),
      shiny::helpText("Décoché : une composition absente bloque l'ajustement et rend le seuil non évaluable. Coché : elle compte pour 0, comme dans VetNutri MP ; les nutriments concernés sont listés et un scénario sinon conforme reçoit CONFORME_ABSENTS_A_ZERO."),
      shiny::numericInput("scenario_limit", "Nombre maximal de scénarios autorisé", 5000, min = 1, step = 1000),
      shiny::textOutput("explore_count"),
      shiny::actionButton("explore_run", "Calculer toutes les rations", class = "btn-primary")
    ), shiny::mainPanel(width = 8,
      shiny::textOutput("explore_catalogue"),
      shiny::tabsetPanel(
        shiny::tabPanel("Cibles", shiny::tableOutput("explore_target_preview")),
        shiny::tabPanel("Résultats",
          shiny::textOutput("explore_summary"),
          shiny::helpText("Les résultats correspondent au dernier clic sur Calculer. Relancer après une modification des listes, cibles ou intervalles."),
          shiny::plotOutput("explore_heatmap"),
          shiny::selectInput("explore_status", "Filtrer les scénarios", choices = c("Tous" = "ALL")),
          shiny::numericInput("explore_page", "Page (50 scénarios)", 1, min = 1, step = 1),
          shiny::tableOutput("explore_results")),
        shiny::tabPanel("Détail d'une ration",
          shiny::selectizeInput("explore_scenario", "Scénario", choices = NULL),
          shiny::tableOutput("explore_scenario_summary"),
          shiny::tableOutput("explore_quantities"),
          shiny::tableOutput("explore_targets_result"),
          shiny::tableOutput("explore_comparison")),
        shiny::tabPanel("Exports",
          shiny::downloadButton("explore_export_summary", "Tous les scénarios (CSV)"), shiny::br(),
          shiny::downloadButton("explore_export_quantities", "Quantités des ingrédients (CSV)"), shiny::br(),
          shiny::downloadButton("explore_export_targets", "Cibles et apports (CSV)"), shiny::br(),
          shiny::downloadButton("explore_export_config", "Configuration et provenance (JSON)")),
        shiny::tabPanel("Diagnostics INIT", shiny::tableOutput("explore_diagnostics"))
      )
    ))
  )
}

vn_exploration_server <- function(input, output, session) {
  roles <- vn_exploration_roles()
  model <- shiny::eventReactive(input$explore_reload, {
    vn_load_init(root = input$explore_root)
  }, ignoreNULL = FALSE)
  shiny::observeEvent(model(), {
    m <- model()
    refs <- m$references[m$references$maladie == "FALSE", ]
    shiny::updateSelectizeInput(session, "explore_refs",
      choices = setNames(refs$reference_id, paste(refs$nom, refs$stadePhysio, sep = " — ")), server = TRUE)
    choices <- setNames(m$foods$food_id, paste(m$foods$name, m$foods$food_id, sep = " — "))
    units <- m$dictionaries$requirement_units
    units <- units[units$uniteReqId %in% c(0, 1, 2, 4, 6), ]
    for (role in roles$role) {
      shiny::updateSelectizeInput(session, paste0("ingredients_", role), choices = choices, server = TRUE)
      if (role != "energy") shiny::updateSelectInput(session, paste0("target_unit_", role), choices = setNames(units$uniteReqId, units$label), selected = "1")
    }
    fibre <- m$dictionaries$nutrients
    fibre <- fibre[fibre$nutrient_id %in% c("CELLULOSE", "FIBRETOT", "FIBRESOL", "NDF", "ADF"), ]
    shiny::updateSelectInput(session, "fibre_nutrient", choices = setNames(fibre$nutrient_id, fibre$name), selected = "CELLULOSE")
  })
  shiny::observeEvent(input$fibre_nutrient, {
    shiny::updateNumericInput(session, "target_factor_fibre", value = if (input$fibre_nutrient == "CELLULOSE") 5 else 1)
  }, ignoreInit = TRUE)
  weights <- shiny::reactive(vn_exploration_interval(input$weight_from, input$weight_to, input$weight_by))
  ks <- shiny::reactive(vn_exploration_interval(input$k_from, input$k_to, input$k_by))
  lists <- shiny::reactive(setNames(lapply(roles$role, function(role) input[[paste0("ingredients_", role)]]), roles$role))
  targets <- shiny::reactive({
    t <- vn_exploration_targets()
    shiny::req(input$fibre_nutrient)
    t$nutrient_id[t$role == "fibre"] <- input$fibre_nutrient
    for (i in which(t$role != "energy")) {
      role <- t$role[i]
      shiny::req(input[[paste0("target_source_", role)]], input[[paste0("target_level_", role)]])
      t$source[i] <- input[[paste0("target_source_", role)]]
      t$reflevel[i] <- input[[paste0("target_level_", role)]]
      t$multiplier[i] <- input[[paste0("target_factor_", role)]]
      if (t$source[i] == "custom") {
        t$value[i] <- input[[paste0("target_value_", role)]]
        t$uniteReqId[i] <- as.integer(input[[paste0("target_unit_", role)]])
      }
    }
    t
  })
  output$explore_catalogue <- shiny::renderText(paste("INIT", model()$provenance$version, "—", nrow(model()$foods), "aliments compatibles chien"))
  output$explore_diagnostics <- shiny::renderTable(model()$diagnostics)
  output$explore_count <- shiny::renderText({
    n <- prod(lengths(lists())) * length(weights()) * length(ks()) * length(input$explore_refs)
    paste(format(n, scientific = FALSE), "scénarios = combinaisons × poids × K × référentiels")
  })
  output$explore_target_preview <- shiny::renderTable({
    shiny::req(input$explore_refs)
    m <- model(); t <- targets()
    rows <- list()
    for (ref in input$explore_refs) for (i in seq_len(nrow(t))) {
      value <- t$value[i]; unit <- t$uniteReqId[i]; note <- ""
      if (t$source[i] == "reference") {
        r <- m$requirements[m$requirements$reference_id == ref & m$requirements$nutrient_id == t$nutrient_id[i] &
          m$requirements$reflevel == t$reflevel[i] & m$requirements$is_effective, ]
        if (nrow(r) == 1L) { value <- r$quantity; unit <- r$uniteReqId } else note <- "Seuil absent : choisir un autre niveau ou saisir une cible"
      }
      if (t$source[i] == "energy") note <- "Besoin standard × K, énergie restante après les autres apports"
      rows[[length(rows) + 1L]] <- data.frame(
        reference = m$references$nom[match(ref, m$references$reference_id)],
        nutriment = t$nutrient_id[i], source = t$source[i], niveau = t$reflevel[i], valeur = value,
        unite = m$dictionaries$nutrients$unit[match(t$nutrient_id[i], m$dictionaries$nutrients$nutrient_id)],
        base = vn_scalar(m$dictionaries$requirement_units$label[match(unit, m$dictionaries$requirement_units$uniteReqId)]),
        facteur = if (t$source[i] == "energy") NA_real_ else t$multiplier[i], note = note)
    }
    do.call(rbind, rows)
  })
  exploration <- shiny::eventReactive(input$explore_run, {
    m <- model()
    vars <- jsonlite::fromJSON(input$explore_variables, simplifyVector = FALSE)
    shiny::validate(shiny::need(is.list(vars), "Variables : objet JSON requis"))
    shiny::withProgress(message = "Exploration de toutes les combinaisons", value = 0, {
      out <- vn_explore_rations(m, input$explore_refs, lists(), weights(), ks(), targets(), vars,
        max_scenarios = input$scenario_limit, missing_as_zero = isTRUE(input$explore_missing_zero),
        progress = function(i, total) shiny::setProgress(value = i / total, detail = paste(i, "/", total)))
    })
    out$model <- m # Snapshot: later catalogue reloads must not change existing results.
    out
  })
  shiny::observeEvent(exploration(), {
    out <- exploration()
    s <- out$summary
    shiny::updateSelectInput(session, "explore_status", choices = c("Tous" = "ALL", setNames(unique(s$status), unique(s$status))))
    shiny::updateSelectizeInput(session, "explore_scenario", choices = setNames(s$scenario_id,
      paste(s$scenario_id, s$reference_name, s$weight_kg, "kg — K", s$K, s$status)), server = TRUE)
    shiny::updateNumericInput(session, "explore_page", value = 1)
  })
  output$explore_summary <- shiny::renderText({
    s <- exploration()$summary
    text <- paste(nrow(s), "scénarios ;", sum(s$status == "CONFORME"), "entièrement conformes aux seuils connus, sans données manquantes ni avertissement.")
    if (isTRUE(exploration()$configuration$missing_as_zero))
      text <- paste(text, sum(s$status == "CONFORME_ABSENTS_A_ZERO"), "conformes en comptant les valeurs absentes à 0.")
    text
  })
  output$explore_results <- shiny::renderTable({
    s <- exploration()$summary
    if (!is.null(input$explore_status) && input$explore_status != "ALL") s <- s[s$status == input$explore_status, ]
    shiny::req(input$explore_page >= 1)
    page <- as.integer(input$explore_page)
    s <- s[seq_len(nrow(s)) > (page - 1) * 50 & seq_len(nrow(s)) <= page * 50, ]
    s[, c("scenario_id", "combination_id", "reference_name", "weight_kg", "K", "need_kcal", "energy_kcal", "status", "insufficient", "excess", "missing", "zero_filled", "message")]
  }, digits = 3)
  output$explore_heatmap <- shiny::renderPlot({
    s <- exploration()$summary
    zero <- isTRUE(exploration()$configuration$missing_as_zero)
    ok <- s$status == "CONFORME" | (zero & s$status == "CONFORME_ABSENTS_A_ZERO")
    counts <- stats::aggregate(list(conformes = as.numeric(ok), total = rep(1, nrow(s))),
                               s[, c("weight_kg", "K")], sum)
    rate <- counts$conformes / counts$total
    palette <- grDevices::colorRampPalette(c("#d95f59", "#f5d787", "#4e9b72"))(101)
    graphics::plot(counts$weight_kg, counts$K, pch = 15, cex = 3, col = palette[1 + round(100 * rate)],
      xlab = "Poids (kg)", ylab = "K global", main = "Part de scénarios entièrement conformes",
      sub = paste0("Toutes les combinaisons et tous les référentiels sélectionnés",
        if (zero) " ; valeurs absentes comptées à 0" else ""))
    graphics::text(counts$weight_kg, counts$K, labels = paste0(round(100 * rate), "%"), pos = 3, cex = 0.8)
    graphics::legend("topright", legend = c("0 %", "50 %", "100 %"), col = palette[c(1, 51, 101)], pch = 15, bty = "n")
  })
  selected <- shiny::reactive({
    out <- exploration(); shiny::req(input$explore_scenario)
    s <- out$summary[out$summary$scenario_id == input$explore_scenario, , drop = FALSE]
    shiny::req(nrow(s) == 1L)
    s
  })
  output$explore_scenario_summary <- shiny::renderTable(selected()[, c("scenario_id", "reference_id", "reference_name", "stage", "weight_kg", "K", "status", "zero_filled_nutrients", "message", "warnings")])
  output$explore_quantities <- shiny::renderTable({
    out <- exploration(); s <- selected(); q <- out$quantities
    shiny::req(nrow(q) > 0)
    q <- q[q$scenario_id == s$scenario_id, ]
    q$food_name <- out$model$foods$name[match(q$food_id, out$model$foods$food_id)]
    q[, c("role", "food_id", "food_name", "quantity_g")]
  }, digits = 4)
  output$explore_targets_result <- shiny::renderTable({
    t <- exploration()$targets; shiny::req(nrow(t) > 0)
    t[t$scenario_id == selected()$scenario_id, c("nutrient_id", "unit", "source", "reflevel", "multiplier", "absolute_target", "final_intake", "gap")]
  }, digits = 4)
  detail <- shiny::reactive({
    out <- exploration(); s <- selected(); q <- out$quantities
    shiny::req(nrow(q) > 0)
    q <- q[q$scenario_id == s$scenario_id & q$quantity_g > 0, ]
    shiny::req(nrow(q) > 0)
    n <- vn_init_needs(out$model, s$reference_id, s$weight_kg,
      variables = out$configuration$variables, adjustment = s$K)
    vn_init_ration(out$model, s$reference_id, q[, c("food_id", "quantity_g")], n,
      missing_as_zero = isTRUE(out$configuration$missing_as_zero))
  })
  output$explore_comparison <- shiny::renderTable(detail()$comparison[, c("nutrient_id", "reflevel", "unit", "intake", "absolute_requirement", "status", "zero_filled")], digits = 4)
  output$explore_export_summary <- shiny::downloadHandler(filename = function() "scenarios-canins.csv",
    content = function(file) utils::write.csv(exploration()$summary, file, row.names = FALSE))
  output$explore_export_quantities <- shiny::downloadHandler(filename = function() "quantites-rations.csv",
    content = function(file) utils::write.csv(exploration()$quantities, file, row.names = FALSE))
  output$explore_export_targets <- shiny::downloadHandler(filename = function() "cibles-apports.csv",
    content = function(file) utils::write.csv(exploration()$targets, file, row.names = FALSE))
  output$explore_export_config <- shiny::downloadHandler(filename = function() "configuration-exploration.json",
    content = function(file) jsonlite::write_json(exploration()$configuration, file, auto_unbox = TRUE, pretty = TRUE, digits = NA))
}
