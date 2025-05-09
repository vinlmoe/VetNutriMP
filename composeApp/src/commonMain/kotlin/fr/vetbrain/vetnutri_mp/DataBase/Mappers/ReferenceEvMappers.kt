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

/**
 * Méthodes d'extension pour la conversion entre les objets de domaine et les entités de base de
 * données concernant les références nutritionnelles
 */
object ReferenceEvMappers {

    /**
     * Convertit un objet ReferenceEv en ReferenceEvEntity pour la persistance
     * @return L'entité correspondante
     */
    fun ReferenceEv.toEntity(): ReferenceEvEntity {
        return ReferenceEvEntity(
                uuid = this.uuid,
                nom = this.nom,
                description = this.description,
                maladie = this.maladie,
                nomMaladie = this.nomMaladie,
                nomEnergie = this.nomEnergie,
                // Convertir Boolean en Int pour la persistance
                consistent = if (this.consistent) 1 else 0,
                // Convertir les énumérations en String
                espece = this.espece.name,
                stadePhysio = this.stadePhysio.name,
                // Noms des coefficients modificateurs
                nomk1 = this.nomk1,
                nomk2 = this.nomk2,
                nomk3 = this.nomk3,
                nomk4 = this.nomk4,
                nomk5 = this.nomk5,
                // Références aux équations (UUIDs)
                equationBW = this.equationBW?.uuid,
                equationBEE = this.equationBEE?.uuid,
                equationDEcom = this.equationDEcom?.uuid,
                equationDEraw = this.equationDEraw?.uuid,
                equationME = this.equationME?.uuid
        )
    }

    /**
     * Convertit un objet Nut4Ref en NutrientReferenceEntity pour la persistance
     * @param referenceId L'identifiant de la référence parente
     * @return L'entité correspondante
     */
    fun Nut4Ref.toEntity(referenceId: String): NutrientReferenceEntity {
        return NutrientReferenceEntity(
                uuid = this.uuid,
                referenceId = referenceId,
                nutrient = this.nutrient.name,
                niveauRef = this.niveauRef.name,
                quantite = this.quantite,
                unite = this.unite.name,
                uniteReq = this.uniteReq.name,
                biblioRefId = this.citation?.uuid
        )
    }

    /**
     * Convertit un objet CoefP en CoefficientEntity pour la persistance
     * @param referenceId L'identifiant de la référence parente
     * @param groupId L'identifiant du groupe (0-4 pour les groupes modk1-modk5)
     * @return L'entité correspondante
     */
    fun CoefP.toEntity(referenceId: String, groupId: Int): CoefficientEntity {
        return CoefficientEntity(
                uuid = this.uuid,
                referenceId = referenceId,
                groupUUID = groupId,
                description = this.description,
                coef = this.coef
        )
    }

    /**
     * Convertit une entité ReferenceEvEntity en objet ReferenceEv Note: Cette méthode ne récupère
     * pas les relations (Nut4Ref, CoefP, Equation), elles doivent être chargées séparément.
     * @return L'objet de domaine correspondant
     */
    fun ReferenceEvEntity.toDomain(): ReferenceEv {
        return ReferenceEv(
                        uuid = this.uuid,
                        nom = this.nom,
                        description = this.description,
                        maladie = this.maladie,
                        nomMaladie = this.nomMaladie,
                        nomEnergie = this.nomEnergie,
                        // Convertir Int en Boolean lors du chargement
                        consistent = this.consistent == 1,
                        // Convertir String en énumération
                        espece =
                                try {
                                    Espece.valueOf(this.espece)
                                } catch (e: Exception) {
                                    Espece.CHIEN
                                },
                        stadePhysio =
                                try {
                                    StadePhysio.valueOf(this.stadePhysio)
                                } catch (e: Exception) {
                                    StadePhysio.ADULTE
                                }
                )
                .apply {
                    // Définir les noms des coefficients
                    this.nomk1 = this@toDomain.nomk1
                    this.nomk2 = this@toDomain.nomk2
                    this.nomk3 = this@toDomain.nomk3
                    this.nomk4 = this@toDomain.nomk4
                    this.nomk5 = this@toDomain.nomk5
                    // Les équations et autres relations doivent être chargées séparément
                }
    }

    /**
     * Convertit une entité NutrientReferenceEntity en objet Nut4Ref
     * @param biblioRef La référence bibliographique associée (peut être null)
     * @return L'objet de domaine correspondant
     */
    fun NutrientReferenceEntity.toDomain(biblioRef: BiblioRef? = null): Nut4Ref {
        return Nut4Ref(
                uuid = this.uuid,
                nutrient =
                        try {
                            Nutrient.valueOf(this.nutrient)
                        } catch (e: Exception) {
                            throw IllegalArgumentException("Nutrient inconnu: ${this.nutrient}")
                        },
                niveauRef =
                        try {
                            Reflevel.valueOf(this.niveauRef)
                        } catch (e: Exception) {
                            Reflevel.MIN
                        },
                quantite = this.quantite,
                unite =
                        try {
                            UnitEnum.valueOf(this.unite)
                        } catch (e: Exception) {
                            UnitEnum.NO
                        },
                uniteReq =
                        try {
                            UnitReqEnum.valueOf(this.uniteReq)
                        } catch (e: Exception) {
                            UnitReqEnum.MS
                        },
                citation = biblioRef
        )
    }

    /**
     * Convertit une entité CoefficientEntity en objet CoefP
     * @return L'objet de domaine correspondant
     */
    fun CoefficientEntity.toDomain(): CoefP {
        return CoefP(
                uuid = this.uuid,
                description = this.description,
                coef = this.coef,
                groupUUID = this.groupUUID
        )
    }
}
