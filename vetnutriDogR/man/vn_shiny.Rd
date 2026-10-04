\name{vn_shiny_ui}
\alias{vn_shiny_ui}
\alias{vn_shiny_server}
\title{Manual canine ration interface}
\description{Provides the manual ration application with diagnostics and CSV export.
The Quarto exploration interface uses the same underlying calculation engine.}
\usage{
vn_shiny_ui(root = vn_find_root())
vn_shiny_server(input, output, session)
}
\arguments{
\item{root}{Path to the VetNutri MP source checkout. Defaults to automatic discovery.}
\item{input}{Shiny session inputs.}
\item{output}{Shiny session outputs.}
\item{session}{Shiny session.}
}
\value{The UI function returns HTML tags. The server function registers reactive
calculations and output handlers in the current session.}
