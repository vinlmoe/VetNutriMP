package fr.vetbrain.vetnutri_mp.Data.PoidsChien

/**
 * Constantes du modèle de poids du chien adulte « chien-v2 » (déjà calé : ne pas les modifier).
 * Unités : kg, jours, kcal EM/j.
 */
object ConstantesChienV2 {
    const val VERSION = "chien-v2"

    /** kcal/kg de masse grasse. */
    const val RHO_F = 9400.0
    /** kcal/kg de masse maigre. */
    const val RHO_L = 1800.0
    /** kcal/kg^0,75/j (masse grasse). */
    const val GAMMA_F = 6.0
    /** kcal/kg^0,75/j (masse maigre). */
    const val GAMMA_L = 44.0
    /** Adaptation métabolique. */
    const val ETA = 0.75
    /** Constante de temps de l'adaptation, jours. */
    const val TAU = 112.0
    /** Exposant allométrique de partition. */
    const val C_ALLO = 0.134818124
    /** Fraction de graisse au poids idéal. */
    const val F_IDEAL = 0.337391595
    /** Besoin d'entretien de population, kcal EM/kg^0,75/j (chiens de propriétaires). */
    const val M_PROP = 97.49440146
    /** Écart-type entre chiens du log du besoin. */
    const val SD_LOG_M = 0.1248956609
    /** kcal/g de protéines. */
    const val EM_P = 3.5
    /** kcal/g de lipides. */
    const val EM_L = 8.5
    /** Thermogenèse alimentaire des protéines, lipides et glucides. */
    const val BETA_P = 0.25
    const val BETA_L = 0.03
    const val BETA_G = 0.08
    /** Écart-type d'une pesée, en fraction du poids initial. */
    const val BRUIT = 0.01
    /** Protéines et lipides par défaut, g/100 kcal EM. */
    const val P_DEF = 10.4
    const val LP_DEF = 3.5

    // Paramètres numériques de la mise en œuvre (non calés)
    /** Pas d'intégration Runge-Kutta, jours. */
    const val PAS_JOURS = 2
    /** Horizon maximal de simulation, jours. */
    const val HORIZON_MAX_JOURS = 364
    /** Grille du log du besoin : [U_MIN, U_MAX] en NOMBRE_U valeurs. */
    const val U_MIN = -0.8
    const val U_MAX = 0.8
    const val NOMBRE_U = 321
    /** Une pesée faite moins de ce délai après un changement de ration ou le début du suivi est ignorée. */
    const val DELAI_PESEE_JOURS = 14
    /** Poids normalisé minimal d'une valeur de grille utilisée pour la ration proposée. */
    const val POIDS_GRILLE_MIN = 1e-6
    /** Bornes de recherche de la ration proposée, kcal EM/j. */
    const val RATION_MIN = 40.0
    const val RATION_MAX = 4000.0
    /** Délai de l'objectif d'entretien, jours. */
    const val DELAI_ENTRETIEN_JOURS = 84
    /** Niveaux de déficit proposés, en fraction du besoin initial. */
    val NIVEAUX_DEFICIT = listOf(0.40, 0.50, 0.60, 0.70)
}
