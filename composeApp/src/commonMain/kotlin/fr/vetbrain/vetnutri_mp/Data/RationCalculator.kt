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

    private var poids: Float = 0f
    private var poidsOptimal: Float = 0f
    private var bee: Float = 0f
    private var poidsMetabolique: Float = 0f
    private var poidsMetaboliqueOptimal: Float = 0f
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
    constructor(poids: Float, poidsOptimal: Float, espece: Espece) {
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
            poidsMetabolique = equationPM!!.calculerValeurAnimal(poids, svp).toFloat()
            poidsMetaboliqueOptimal = equationPM!!.calculerValeurAnimal(poidsOptimal, svp).toFloat()
        } else {
            // Calcul par défaut si pas d'équation
            poidsMetabolique = poids.toDouble().pow(0.75).toFloat()
            poidsMetaboliqueOptimal = poidsOptimal.toDouble().pow(0.75).toFloat()
        }

        // Calcul du besoin énergétique
        if (equationBEE != null) {
            bee = equationBEE!!.calculerValeurAnimal(poids, svp).toFloat()
        } else {
            // Calcul par défaut si pas d'équation
            bee = 70f * poidsMetabolique
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

    fun getPoids(): Float = poids

    fun setPoids(poids: Float) {
        this.poids = poids
    }

    fun getOptiPoids(): Float = poidsOptimal

    fun setOptiPoids(poidsOptimal: Float) {
        this.poidsOptimal = poidsOptimal
    }

    fun getBEE(): Float = bee

    fun getPM(): Float = poidsMetabolique

    fun getPMOpti(): Float = poidsMetaboliqueOptimal

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
