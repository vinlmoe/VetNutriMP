package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
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
     * Calcule le besoin énergétique et le poids métabolique.
     *
     * Le poids métabolique passe par [CalculMetabolique] (même moteur que l'analyse de
     * consultation) : l'équation BW fournie via [setEquationPM] est utilisée si elle existe,
     * sinon BW^0,75 ([CalculMetabolique.EXPOSANT_PAR_DEFAUT]). Le besoin énergétique suit la
     * même logique avec l'équation BEE ([setEquationBEE]) ; sans équation, 70 × PM (valeur
     * historique de ce calculateur).
     *
     * @param svp Les variables supplémentaires
     */
    fun calculer(svp: List<SupplementalvariableP>) {
        // Calcul du poids métabolique (équation BW du référentiel, sinon BW^0,75)
        poidsMetabolique = CalculMetabolique.poidsMetaboliqueOuDefaut(poids, equationPM, svp)
        poidsMetaboliqueOptimal = CalculMetabolique.poidsMetaboliqueOuDefaut(poidsOptimal, equationPM, svp)

        // Calcul du besoin énergétique (équation BEE si disponible, sinon 70 × PM)
        val beeEquation = CalculMetabolique.besoinEnergetiqueStandard(poids, equationBEE, svp)
        bee = if (beeEquation != null && beeEquation.isFinite() && beeEquation > 0.0) beeEquation
              else 70.0 * poidsMetabolique
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
