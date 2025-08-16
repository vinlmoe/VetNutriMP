package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import kotlin.math.pow
import kotlinx.serialization.Serializable

/**
 * Classe pour effectuer les calculs sur les rations Basée sur la classe RationCalculator du projet
 * Java original
 */
@Serializable
class RationCalculator {

    private var poids: Double = 0.0
    private var poidsOptimal: Double = 0.0
    private var bee: Double = 0.0
    private var poidsMetabolique: Double = 0.0
    private var poidsMetaboliqueOptimal: Double = 0.0
    private var espece: Espece = Espece.CHIEN
    private var equationBEE: Equation? = null
    private var equationPM: Equation? = null

    /** Constructeur par défaut */
    constructor() {
        // Initialisation par défaut
    }

    /**
     * Constructeur avec paramètres
     *
     * @param poids Le poids actuel de l'animal
     * @param poidsOptimal Le poids optimal de l'animal
     * @param espece L'espèce de l'animal
     */
    constructor(poids: Double, poidsOptimal: Double, espece: Espece) {
        this.poids = poids
        this.poidsOptimal = poidsOptimal
        this.espece = espece
    }

    /**
     * Calcule le besoin énergétique et le poids métabolique
     *
     * @param svp Les variables supplémentaires
     */
    fun calculer(svp: List<SupplementalvariableP>) {
        // Calcul du poids métabolique
        if (equationPM != null) {
            // TODO: Implémenter l'évaluation des équations personnalisées
            // Pour l'instant, utiliser le calcul par défaut
            poidsMetabolique = poids.pow(0.75)
            poidsMetaboliqueOptimal = poidsOptimal.pow(0.75)
        } else {
            // Calcul par défaut si pas d'équation
            poidsMetabolique = poids.pow(0.75)
            poidsMetaboliqueOptimal = poidsOptimal.pow(0.75)
        }

        // Calcul du besoin énergétique
        if (equationBEE != null) {
            // TODO: Implémenter l'évaluation des équations personnalisées
            // Pour l'instant, utiliser le calcul par défaut
            bee = 70.0 * poidsMetabolique
        } else {
            // Calcul par défaut si pas d'équation
            bee = 70.0 * poidsMetabolique
        }
    }

    /**
     * Définit l'équation pour le calcul du besoin énergétique
     *
     * @param equation L'équation à utiliser
     */
    fun setEquationBEE(equation: Equation?) {
        if (equation != null && equation.kind == EquationKind.ENERGYNEED) {
            this.equationBEE = equation
        }
    }

    /**
     * Définit l'équation pour le calcul du poids métabolique
     *
     * @param equation L'équation à utiliser
     */
    fun setEquationPM(equation: Equation?) {
        if (equation != null && equation.kind == EquationKind.MW) {
            this.equationPM = equation
        }
    }

    // Getters et setters

    fun getPoids(): Double = poids

    fun setPoids(poids: Double) {
        this.poids = poids
    }

    fun getOptiPoids(): Double = poidsOptimal

    fun setOptiPoids(poidsOptimal: Double) {
        this.poidsOptimal = poidsOptimal
    }

    fun getBEE(): Double = bee

    fun getPM(): Double = poidsMetabolique

    fun getPMOpti(): Double = poidsMetaboliqueOptimal

    fun getEspece(): Espece = espece

    fun setEspece(espece: Espece) {
        this.espece = espece
    }

    /**
     * Clone ce calculateur de ration
     *
     * @return Un nouveau calculateur avec les mêmes valeurs
     */
    fun clone(): RationCalculator {
        val clone = RationCalculator()
        clone.poids = this.poids
        clone.poidsOptimal = this.poidsOptimal
        clone.bee = this.bee
        clone.poidsMetabolique = this.poidsMetabolique
        clone.poidsMetaboliqueOptimal = this.poidsMetaboliqueOptimal
        clone.espece = this.espece
        clone.equationBEE = this.equationBEE
        clone.equationPM = this.equationPM
        return clone
    }
}
