\name{vn_load_init}
\alias{vn_load_init}
\alias{vn_locate_init}
\alias{vn_validate_init}
\alias{vn_init_needs}
\alias{vn_init_ration}
\title{Live VetNutri INIT catalogue and canine ration calculations}
\description{Reads INIT directly from a VetNutri MP checkout, preserving source
identifiers, raw values, and provenance. The exposed catalogue and calculations
are restricted to dogs. Nutrient metadata is read from Kotlin enum sources.}
\usage{
vn_locate_init(root = Sys.getenv("VETNUTRI_MP_ROOT", ""))
vn_load_init(path = NULL, root = Sys.getenv("VETNUTRI_MP_ROOT", ""), strict = FALSE)
vn_validate_init(model)
vn_init_needs(model, reference_id, weight_kg, ideal_weight_kg = NULL,
              variables = list(), coefficient_ids = character(), adjustment = 1)
vn_init_ration(model, reference_id, items, needs)
}
\arguments{
\item{root}{VetNutri MP checkout. If empty, search the current directory and its parents.}
\item{path}{Explicit INIT file; defaults to the commonMain resource.}
\item{strict}{Stop on any diagnostic, including missing direct composition values.}
\item{model}{Object returned by \code{vn_load_init}.}
\item{reference_id}{Original UUID of a canine general reference.}
\item{weight_kg}{Current body weight in kilograms.}
\item{ideal_weight_kg}{Optional ideal body weight, taking priority when supplied.}
\item{variables}{Named list of equation symbols, e.g. AW, L, wG, wL.}
\item{coefficient_ids}{Original coefficient UUIDs, at most one per K group.}
\item{adjustment}{Positive adjustment factor; defaults to 1 as in Kotlin.}
\item{items}{Data frame containing food_id and quantity_g, in grams per day.}
\item{needs}{One-row result from \code{vn_init_needs} for the same reference.}
}
\value{Locator and validator return data frames. Loader returns a vetnutri_init
list with normalized tables, raw envelope, provenance and diagnostics. Needs
returns a data frame. Ration returns item intakes, totals and threshold comparisons.}
\details{No nutrition values are bundled in this package. An updated JSON is read
on the next load. Raw contains the complete envelope for auditing; normalized
food/reference tables expose dogs only. Unrestricted foods are retained following
FoodSearchComponent. Missing nutrient observations propagate to NA in comparisons.
Disease-reference combinations and evolutive plans are not implemented.
See docs/INIT_REFERENCE.md in the source checkout for interpretation and validation.}
