library(shiny)
if (!isTRUE(getOption("vetnutriDogR.source_mode"))) library(vetnutriDogR)

ui <- vn_shiny_ui()
server <- vn_shiny_server
shinyApp(ui, server)
