package fr.vetbrain.vetnutri_mp.Data

import java.util.UUID
import kotlinx.serialization.Serializable

/** Classe représentant une référence bibliographique */
@Serializable
data class BiblioRef(
        val uuid: String = UUID.randomUUID().toString(),
        val firstAuthor: String = "",
        val year: Int = 1800,
        val completeRef: String = "",
        val comments: String = "",
        val bibtex: String = "",
        val consistent: Int = 1 // 1 = cohérent, 0 = incohérent
) {
    /** Constructeur secondaire qui prend un UUID spécifique */
    constructor(
            uuid: String
    ) : this(
            uuid = uuid,
            firstAuthor = "",
            year = 1800,
            completeRef = "",
            comments = "",
            bibtex = "",
            consistent = 0
    )

    /**
     * Vérifie si cette référence est cohérente
     *
     * @return true si la référence est cohérente, false sinon
     */
    fun isConsistent(): Boolean {
        return consistent > 0
    }

    override fun toString(): String {
        return "$firstAuthor, $year"
    }

    companion object {
        val EMPTY = BiblioRef()
    }
}
