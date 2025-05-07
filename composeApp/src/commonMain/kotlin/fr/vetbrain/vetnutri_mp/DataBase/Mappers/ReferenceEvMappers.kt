package fr.vetbrain.vetnutri_mp.DataBase.Mappers

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.CoefP
import fr.vetbrain.vetnutri_mp.Data.Nut4Ref
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.DataBase.CoefficientEntity
import fr.vetbrain.vetnutri_mp.DataBase.NutrientReferenceEntity
import fr.vetbrain.vetnutri_mp.DataBase.ReferenceEvEntity
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlin.reflect.jvm.isAccessible

/** Convertit une entité ReferenceEvEntity en objet du domaine ReferenceEv */
fun ReferenceEvEntity.toDomain(): ReferenceEv {
    println("DEBUG ReferenceEvMappers: Conversion de ReferenceEvEntity en ReferenceEv")
    println("DEBUG ReferenceEvMappers: UUID: $uuid, Nom: $nom")

    val reference =
            ReferenceEv(
                    uuid = uuid,
                    nom = nom,
                    description = description,
                    maladie = maladie,
                    nomMaladie = nomMaladie,
                    nomEnergie = nomEnergie,
                    consistent = consistent,
                    espece =
                            try {
                                Espece.valueOf(espece)
                            } catch (e: Exception) {
                                Espece.CHIEN
                            },
                    stadePhysio =
                            try {
                                StadePhysio.valueOf(stadePhysio)
                            } catch (e: Exception) {
                                StadePhysio.ADULTE
                            }
            )

    // Ajouter les noms des coefficients
    reference.nomk1 = nomk1
    reference.nomk2 = nomk2
    reference.nomk3 = nomk3
    reference.nomk4 = nomk4
    reference.nomk5 = nomk5

    return reference
}

/** Convertit un objet du domaine ReferenceEv en entité ReferenceEvEntity */
fun ReferenceEv.toEntity(): ReferenceEvEntity {
    println("DEBUG ReferenceEvMappers: Conversion de ReferenceEv en ReferenceEvEntity")
    println("DEBUG ReferenceEvMappers: UUID: $uuid, Nom: $nom")

    return ReferenceEvEntity(
            uuid = uuid,
            nom = nom,
            description = description,
            maladie = maladie,
            nomMaladie = nomMaladie,
            nomEnergie = nomEnergie,
            consistent = consistent,
            espece = espece.name,
            stadePhysio = stadePhysio.name,
            nomk1 = nomk1,
            nomk2 = nomk2,
            nomk3 = nomk3,
            nomk4 = nomk4,
            nomk5 = nomk5,
            equationBW = equationBW?.uuid,
            equationBEE = equationBEE?.uuid,
            equationDEcom = equationDEcom?.uuid,
            equationDEraw = equationDEraw?.uuid,
            equationME = equationME?.uuid
    )
}

/** Convertit un CoefP en CoefficientEntity */
fun CoefP.toEntity(referenceId: String): CoefficientEntity {
    return CoefficientEntity(
            uuid = uuid ?: java.util.UUID.randomUUID().toString(),
            referenceId = referenceId,
            groupUUID = groupUUID,
            description = description,
            coef = coef
    )
}

/** Convertit un CoefficientEntity en CoefP */
fun CoefficientEntity.toDomain(): CoefP {
    return CoefP(uuid = uuid, description = description, coef = coef, groupUUID = groupUUID)
}

/** Convertit une entité NutrientReferenceEntity en Nut4Ref pour le domaine */
fun NutrientReferenceEntity.toDomain(biblio: BiblioRef? = null): Nut4Ref {
    return Nut4Ref(
            nutrient =
                    try {
                        Nutrient.valueOf(nutrient)
                    } catch (e: Exception) {
                        Nutrient.ENERGIE
                    },
            niveauRelatif =
                    try {
                        Reflevel.valueOf(niveauRef)
                    } catch (e: Exception) {
                        Reflevel.MIN
                    },
            quantite = quantite,
            unite =
                    try {
                        UnitEnum.valueOf(unite)
                    } catch (e: Exception) {
                        UnitEnum.KCAL
                    },
            uniteReq =
                    try {
                        UnitReqEnum.valueOf(uniteReq)
                    } catch (e: Exception) {
                        UnitReqEnum.KCAL
                    },
            biblio = biblio ?: BiblioRef()
    )
}

/** Convertit un Nut4Ref en NutrientReferenceEntity */
fun Nut4Ref.toEntity(referenceId: String): NutrientReferenceEntity {
    return NutrientReferenceEntity(
            uuid = java.util.UUID.randomUUID().toString(),
            referenceId = referenceId,
            nutrient = nutrient.name,
            niveauRef = niveauRelatif.name,
            quantite = quantite,
            unite = unite.name,
            uniteReq = uniteReq.name,
            biblioRefId = biblio.uuid.takeIf { it.isNotBlank() }
    )
}

/**
 * Récupère la liste des coefficients d'un groupe spécifique depuis une ReferenceEv en utilisant la
 * réflexion
 */
fun ReferenceEv.getCoefficientsForGroup(groupId: Int): List<CoefP> {
    val field =
            when (groupId) {
                0 -> "modk1"
                1 -> "modk2"
                2 -> "modk3"
                3 -> "modk4"
                4 -> "modk5"
                else -> return emptyList()
            }

    try {
        val property = ReferenceEv::class.java.getDeclaredField(field)
        property.isAccessible = true
        @Suppress("UNCHECKED_CAST") return (property.get(this) as? ArrayList<CoefP>) ?: emptyList()
    } catch (e: Exception) {
        println(
                "DEBUG ReferenceEvMappers: Erreur lors de la récupération des coefficients: ${e.message}"
        )
        return emptyList()
    }
}

/**
 * Met à jour les coefficients d'un groupe spécifique dans une ReferenceEv en utilisant la réflexion
 */
fun ReferenceEv.updateCoefficientsForGroup(groupId: Int, coefficients: List<CoefP>): Boolean {
    val field =
            when (groupId) {
                0 -> "modk1"
                1 -> "modk2"
                2 -> "modk3"
                3 -> "modk4"
                4 -> "modk5"
                else -> return false
            }

    try {
        val property = ReferenceEv::class.java.getDeclaredField(field)
        property.isAccessible = true

        // Créer une nouvelle ArrayList avec les coefficients
        val newList = ArrayList<CoefP>(coefficients)

        // Mettre à jour la propriété
        property.set(this, newList)
        return true
    } catch (e: Exception) {
        println(
                "DEBUG ReferenceEvMappers: Erreur lors de la mise à jour des coefficients: ${e.message}"
        )
        return false
    }
}
