# Manual-ration interface; the QMD exploration shares the calculation engine.
vn_shiny_ui <- function(root = vn_find_root()) shiny::fluidPage(
  shiny::titlePanel("VetNutri — Analyse d’une ration canine"),
  shiny::sidebarLayout(shiny::sidebarPanel(
    shiny::textInput("root", "Dépôt VetNutri MP", root),
    shiny::actionButton("reload", "Charger / actualiser le catalogue"),
    shiny::selectInput("reference", "Référentiel", choices = NULL),
    shiny::numericInput("weight", "Poids (kg)", 10, min = 0.01),
    shiny::numericInput("ideal", "Poids idéal (kg, 0 = non renseigné)", 0, min = 0),
    shiny::uiOutput("coefficients"),
    shiny::numericInput("adjustment", "Coefficient d’ajustement", 1, min = 0.01),
    shiny::textInput("variables", "Variables supplémentaires (objet JSON, ex. {\"AW\": 20})", "{}"),
    shiny::selectizeInput("food", "Aliment", choices = NULL),
    shiny::numericInput("quantity", "Quantité (g/jour)", 100, min = 0),
    shiny::actionButton("add", "Ajouter à la ration"),
    shiny::actionButton("clear", "Vider la ration")
  ), shiny::mainPanel(
    shiny::textOutput("source"), shiny::tableOutput("ration"),
    shiny::verbatimTextOutput("needs"), shiny::plotOutput("energy"),
    shiny::tabsetPanel(
      shiny::tabPanel("Évaluation", shiny::tableOutput("comparison")),
      shiny::tabPanel("Traçabilité", shiny::downloadButton("export", "Exporter les apports"), shiny::tableOutput("details")),
      shiny::tabPanel("Diagnostics du catalogue", shiny::tableOutput("diagnostics")),
      shiny::tabPanel("Diagnostics de calcul", shiny::tableOutput("calculation_diagnostics"))
    )
  ))
)

vn_shiny_server <- function(input, output, session) {
  model <- shiny::eventReactive(input$reload, {
    vn_load_init(root = input$root)
  }, ignoreNULL = FALSE)
  ration <- shiny::reactiveVal(data.frame(food_id = character(), quantity_g = double()))
  shiny::observeEvent(model(), {
    m <- model()
    refs <- m$references[m$references$maladie == "FALSE", ]
    shiny::updateSelectInput(session, "reference", choices = setNames(refs$reference_id,
      paste(refs$nom, refs$espece, refs$stadePhysio, sep = " — ")))
    shiny::updateSelectizeInput(session, "food", choices = setNames(m$foods$food_id,
      paste(m$foods$name, m$foods$food_id, sep = " — ")), server = TRUE)
  })
  output$coefficients <- shiny::renderUI({
    shiny::req(input$reference)
    coefs <- model()$coefficients
    coefs <- coefs[coefs$reference_id == input$reference, ]
    shiny::tagList(lapply(unique(coefs$groupType), function(group) {
      rows <- coefs[coefs$groupType == group, ]
      shiny::selectInput(paste0("coef_", group), group, choices = c("Non renseigné (×1)" = "",
        setNames(rows$coefficient_id, paste(rows$description, rows$coef, sep = " : "))))
    }))
  })
  shiny::observeEvent(input$add, {
    shiny::req(input$food, is.finite(input$quantity), input$quantity > 0)
    ration(rbind(ration(), data.frame(food_id = input$food, quantity_g = input$quantity)))
  })
  shiny::observeEvent(input$clear, ration(ration()[FALSE, ]))
  needs <- shiny::reactive({
    shiny::req(input$reference)
    vars <- jsonlite::fromJSON(input$variables, simplifyVector = FALSE)
    shiny::validate(shiny::need(is.list(vars), "Les variables doivent être un objet JSON"))
    ids <- unlist(lapply(paste0("coef_k", 1:5), function(k) input[[k]]))
    vn_init_needs(model(), input$reference, input$weight,
      if (input$ideal > 0) input$ideal else NULL, variables = vars,
      coefficient_ids = ids[nzchar(ids)], adjustment = input$adjustment)
  })
  result <- shiny::reactive({
    shiny::req(nrow(ration()) > 0)
    vn_init_ration(model(), input$reference, ration(), needs())
  })
  output$source <- shiny::renderText(paste("INIT", model()$provenance$version, "—", nrow(model()$foods), "aliments"))
  output$ration <- shiny::renderTable({
    x <- ration(); x$name <- model()$foods$name[match(x$food_id, model()$foods$food_id)]; x
  })
  output$needs <- shiny::renderPrint(needs()[, c("standard_kcal", "need_kcal", "default_variables")])
  output$energy <- shiny::renderPlot({
    r <- result()
    barplot(c("Besoin" = r$needs$need_kcal, "Apport" = r$energy_kcal),
      ylab = "kcal/jour", col = c("#497b94", "#69a77f"))
  })
  output$comparison <- shiny::renderTable(result()$comparison[, c("nutrient_id", "reflevel", "unit", "intake", "absolute_requirement", "status")])
  output$details <- shiny::renderTable(head(result()$item_intakes, 100))
  output$calculation_diagnostics <- shiny::renderTable(result()$diagnostics)
  output$diagnostics <- shiny::renderTable(model()$diagnostics)
  output$export <- shiny::downloadHandler(filename = function() "vetnutri-apports.csv",
    content = function(file) write.csv(result()$item_intakes, file, row.names = FALSE))
}
