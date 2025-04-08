package fr.vetbrain.vetnutri_mp.Data

/**
 * Classe pour gérer une liste de références de nutriments Basée sur la classe listNutrientRef du
 * projet Java original
 */
class ListNutrientRef {

    private val references: MutableList<NutrientRef> = mutableListOf()
    private val mapNutrientRef = mutableMapOf<String, NutrientRefP>()

    /** Constructeur par défaut */
    constructor()

    /**
     * Constructeur avec une liste initiale de références
     *
     * @param initialReferences Liste initiale de références de nutriments
     */
    constructor(initialReferences: List<NutrientRef>) {
        references.addAll(initialReferences)
    }

    /**
     * Ajoute des références à la liste existante
     *
     * @param newReferences Liste de références à ajouter
     */
    fun ajouterReferences(newReferences: List<NutrientRef>) {
        references.addAll(newReferences)
    }

    /**
     * Récupère la liste des références
     *
     * @return Liste des références de nutriments
     */
    fun getReferences(): List<NutrientRef> {
        return references.toList()
    }

    /**
     * Clone cette liste de références
     *
     * @return Une nouvelle instance de ListNutrientRef avec les mêmes références
     */
    fun clone(): ListNutrientRef {
        return ListNutrientRef(references)
    }
}
