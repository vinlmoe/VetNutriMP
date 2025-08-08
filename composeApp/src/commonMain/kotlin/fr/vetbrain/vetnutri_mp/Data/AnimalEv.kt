package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.datetime.LocalDate

data class AnimalEv(
        var uuid: String = genUUID(),
        var nom: String = "",
        var dead: Boolean = false,
        var id: String? = null,
        var sexId: Int = Sex.MALE_ENTIER.id,
        var specieId: String = Espece.CHIEN.label,
        var ownerName: String = "",
        var birthdate: LocalDate? = null,
        var race: String = "",
        var summary: String = "",
        var consultations: MutableList<ConsultationEv> = mutableListOf(),
        var weightHistory: MutableList<WeightDate> = mutableListOf()
) {
    fun getSex(): Sex {
        return Sex.values().firstOrNull { it.id == sexId } ?: Sex.MALE_ENTIER
    }

    fun setSex(sex: Sex) {
        this.sexId = sex.id
    }

    fun getEspece(): Espece {
        // Chercher d'abord par label (ce qui correspond à ce qui est stocké lors de l'import)
        val especeByLabel = Espece.values().firstOrNull { it.label == specieId }
        if (especeByLabel != null) {
            return especeByLabel
        }

        // Si pas de correspondance par label, essayer par nom d'énumération (pour la compatibilité)
        val especeByName = Espece.values().firstOrNull { it.name == specieId }
        if (especeByName != null) {
            return especeByName
        }

        // Si pas de correspondance, essayer par ID
        val especeById = Espece.values().firstOrNull { it.id == specieId }
        if (especeById != null) {
            return especeById
        }

        // Valeur par défaut
        return Espece.CHIEN
    }

    fun setEspece(espece: Espece) {
        this.specieId = espece.label
    }

    /** Retourne le BEE de la consultation active si disponible */
    fun getBEE(): Double? {
        val consult = consultations.lastOrNull()
        return consult?.let { c ->
            // Approximations: utiliser coefficientAjustement comme proxy si pas de calcul dédié
            val poids = c.weight?.toDouble()
            if (poids != null) {
                fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator.calculerBesoinEnergetiqueBase(
                                poids.toFloat()
                        )
                        .also {
                            return it
                        }
            }
            null
        }
    }

    companion object {
        fun createTestAnimal(): AnimalEv {
            return AnimalEv(
                    nom = "Rex",
                    dead = false,
                    id = "TEST001",
                    sexId = Sex.MALE_ENTIER.id,
                    specieId = Espece.CHIEN.label,
                    ownerName = "Jean Dupont",
                    birthdate = LocalDate(2020, 1, 1),
                    race = "Labrador",
                    summary = "Animal de test"
            )
        }
    }
}
