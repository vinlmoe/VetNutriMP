\name{vn_explore_rations}
\alias{vn_explore_rations}
\alias{vn_exploration_interval}
\alias{vn_exploration_targets}
\alias{vn_exploration_ui}
\alias{vn_exploration_server}
\alias{vn_round_quantity}
\title{Explore canine rations over ingredient combinations, weights and K values}
\description{Selects one ingredient from each of six lists and evaluates the full
Cartesian product with the requested reference, weight and global K grids.
Quantities are adjusted sequentially from nutrient deficits, with energy last.}
\usage{
vn_exploration_interval(from, to, by)
vn_exploration_targets()
vn_explore_rations(model, reference_ids, ingredient_lists, weights, k_values,
                  targets = vn_exploration_targets(), variables = list(),
                  max_scenarios = 5000, progress = NULL, missing_as_zero = FALSE,
                  ignore_levels = "OPTIMAX", rounding = TRUE, min_dose_g = 5)
vn_round_quantity(q, container_step = NA_real_, min_dose_g = 5)
vn_exploration_ui(root = vn_find_root())
vn_exploration_server(input, output, session)
}
\arguments{
\item{from, to, by}{Positive interval bounds and step. The maximum is included.}
\item{model}{Live canine catalogue returned by \code{vn_load_init}.}
\item{reference_ids}{UUIDs of canine general references.}
\item{ingredient_lists}{Named list of food UUID vectors for protein, fibre,
calcium, omega6, sodium and energy. Each list must be nonempty.}
\item{weights}{Positive body weights in kg.}
\item{k_values}{Positive global energy multipliers; total need equals standard BEE times K.}
\item{targets}{Six-row data frame from \code{vn_exploration_targets}, optionally
modified. Fields: role, nutrient_id, source (reference/custom/energy), reflevel,
value, uniteReqId and multiplier. Custom values require an explicit unit.}
\item{variables}{Named list of additional equation variables common to all profiles.}
\item{max_scenarios}{Explicit limit checked before expanding the combinations.
Exceeding it raises an error; no scenarios are sampled or silently dropped.}
\item{progress}{Optional callback taking completed and total counts.}
\item{rounding}{Round each quantity after its step as VetNutri MP
(arrondirQuantiteSelonRegles): half a container unit, else 1, 5 or 25 g. Energy
is then accepted within half a step of the energy ingredient. FALSE keeps
continuous grams and ignores the minimum dose.}
\item{min_dose_g}{Minimum weight of a used ingredient: one value for every role,
or a vector named by role (protein, fibre, calcium, omega6, sodium, energy). For a
container it is the first multiple of the step reaching it. Smaller quantities
become 0 below half of it.}
\item{q, container_step}{Quantity in grams and optional container half-unit.}
\item{ignore_levels}{Threshold levels left out of the conformity evaluation. The
default ignores OPTIMAX, so only MAX limits intakes; \code{character()} evaluates
every level.}
\item{missing_as_zero}{FALSE (default): an absent composition blocks the
adjustment and leaves thresholds unevaluated. TRUE: absent values count as zero,
as in Kotlin; summary columns zero_filled and zero_filled_nutrients report the
substitutions and an otherwise compliant scenario is CONFORME_ABSENTS_A_ZERO.}
\item{root}{VetNutri MP source checkout.}
\item{input, output, session}{Shiny session objects.}
}
\value{Interval returns a numeric vector; targets returns a configuration data
frame; exploration returns summary, quantities, targets, combinations and
configuration, all with original identifiers and provenance. UI returns HTML;
server registers reactive handlers.}
\details{Default nutrient targets use OPTIMIN. Cellulose uses the explicit
Kotlin adjustment multiplier of five, which can be changed. No missing target
is inferred; missing composition is only replaced by zero when
\code{missing_as_zero = TRUE}. The energy step completes energy from the previous
steps without permitting negative quantities. All thresholds are evaluated
again on the resulting ration. Later ingredients can increase an earlier
nutrient target. This sequential exploration is not a simultaneous constraint
solver or an implementation of all Kotlin ration-adjustment modes.}
