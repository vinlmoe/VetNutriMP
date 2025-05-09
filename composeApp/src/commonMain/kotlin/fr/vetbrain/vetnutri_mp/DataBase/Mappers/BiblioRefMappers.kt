package fr.vetbrain.vetnutri_mp.DataBase.Mappers

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefEntity

/**
 * Méthodes d'extension pour la conversion entre les objets de domaine et les entités de base de
 * données concernant les références bibliographiques
 */
object BiblioRefMappers {
    /**
     * Convertit un objet BiblioRef en BiblioRefEntity pour la persistance
     * @return L'entité correspondante
     */
    fun BiblioRef.toEntity(): BiblioRefEntity {
        // Utiliser les propriétés calculées (firstAuthor, year, completeRef) pour la conversion
        return BiblioRefEntity(
                uuid = this.uuid,
                firstAuthor = this.firstAuthor,
                year = this.year,
                completeRef = this.completeRef,
                comments = this.comments,
                bibtex = this.bibtex,
                consistent = this.consistent
        )
    }

    /**
     * Convertit une entité BiblioRefEntity en objet BiblioRef
     * @return L'objet de domaine correspondant
     */
    fun BiblioRefEntity.toDomain(): BiblioRef {
        // Extraire les informations de completeRef pour reconstituer les propriétés de BiblioRef
        val refParts = this.completeRef.split(". ", limit = 3)
        val datePart = this.year.toString()
        val journalPart = if (refParts.size > 2) refParts[2].substringBefore(", ") else ""
        val doiPart =
                if (refParts.size > 2 && refParts[2].contains("doi:"))
                        refParts[2].substringAfter("doi:").trim()
                else ""

        return BiblioRef(
                uuid = this.uuid,
                nom = "Ref-${this.firstAuthor}-${this.year}",
                authors = this.firstAuthor,
                title = if (refParts.size > 1) refParts[1] else "",
                journal = journalPart,
                date = datePart,
                doi = doiPart,
                url = "",
                comments = this.comments,
                bibtex = this.bibtex,
                consistent = this.consistent
        )
    }
}
