\name{vn_exploration_balance}
\alias{vn_exploration_balance}
\alias{vn_plot_balance_map}
\title{Weight by K map of the zones where home-made rations cannot be balanced}
\description{Groups the scenarios of \code{vn_explore_rations} by reference,
weight and K. A cell is balanceable when at least one ingredient combination
meets every evaluated threshold. Otherwise it is balanceable with reservations
when the only failures concern nutrients without data (counted as zero), or is
named after its dominant failure: documented thresholds missed, energy already
exceeded, or not evaluable.}
\usage{
vn_exploration_balance(exploration,
  accepted = c("CONFORME", "CALCULEE_AVEC_AVERTISSEMENTS", "CONFORME_ABSENTS_A_ZERO"),
  top = 3L)
vn_plot_balance_map(balance, labels = NULL)
}
\arguments{
\item{exploration}{Result of \code{vn_explore_rations}, or its summary table.}
\item{accepted}{Scenario statuses counted as a balanced ration.}
\item{top}{Number of most frequent limiting thresholds reported per cell.}
\item{balance}{Result of \code{vn_exploration_balance}.}
\item{labels}{Draw the cell labels; by default only when the grid has at most 300 cells.}
}
\value{One row per reference, weight and K with counts per category, the zone,
\code{best} (combinations reaching the zone), \code{min_failed} (fewest
documented thresholds missed by an evaluable combination), \code{limiting}
(documented thresholds most often missed) and \code{undocumented} (failures on
nutrients counted as zero). The plot draws one panel per reference.}
\details{The adjustment is sequential: a cell that is not balanceable means that
no tested combination works with this method, not that no ration exists.}
