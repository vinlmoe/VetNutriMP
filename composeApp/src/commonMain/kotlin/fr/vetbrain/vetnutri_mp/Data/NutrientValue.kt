package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient

/**
 * Classe qui associe un nutriment à sa valeur et son unité. Cette classe est utilisée pour stocker
 * et manipuler les valeurs nutritionnelles des aliments dans l'application.
 *
 * @property nutrient Le nutriment concerné
 * @property quantity La quantité avec son unité
 */
data class NutrientValue(val nutrient: Nutrient, val quantity: NutrientQuantity) {
        init {
                println(
                        "DEBUG NutrientValue: Création d'une nouvelle instance - nutrient=${nutrient.label}, value=${quantity.value}, unite=${quantity.unite}"
                )
        }

        /**
         * Vérifie si la valeur du nutriment est considérée comme valide (positive)
         * @return true si la valeur est positive, false sinon
         */
        fun isValid(): Boolean {
                val valid = quantity.value > 0
                println(
                        "DEBUG NutrientValue: Vérification validité de ${nutrient.label} = ${quantity.value} -> $valid"
                )
                return valid
        }

        /**
         * Convertit l'objet en représentation textuelle appropriée pour l'affichage
         * @return Une chaîne formatée pour l'affichage
         */
        fun toDisplayString(): String {
                val formattedValue = quantity.formatValue()
                println(
                        "DEBUG NutrientValue: Formatage pour affichage: ${nutrient.label} = $formattedValue ${quantity.unite}"
                )
                return "$formattedValue ${quantity.unite}"
        }

        /**
         * Convertit cette valeur nutritionnelle en entité pour la persistance
         * @param alimentUuid L'identifiant de l'aliment auquel cette valeur est associée
         * @return L'entité correspondante pour la persistance
         */
        fun toEntity(alimentUuid: String): fr.vetbrain.vetnutri_mp.DataBase.NutrientValueEntity {
                println(
                        "DEBUG NutrientValue: Conversion en entité pour aliment $alimentUuid - nutrient=${nutrient.label}, value=${quantity.value}"
                )
                return fr.vetbrain.vetnutri_mp.DataBase.NutrientValueEntity(
                        refAliment = alimentUuid,
                        nutrientLabel = nutrient.label,
                        value = quantity.value
                )
        }

        companion object {
                /**
                 * Crée un NutrientValue à partir d'une entité de base de données
                 * @param entity L'entité provenant de la base de données
                 * @return Un NutrientValue ou null si la conversion a échoué
                 */
                fun fromEntity(
                        entity: fr.vetbrain.vetnutri_mp.DataBase.NutrientValueEntity
                ): NutrientValue? {
                        println(
                                "DEBUG NutrientValue: Tentative de création depuis l'entité - label=${entity.nutrientLabel}, value=${entity.value}"
                        )

                        val nutrient =
                                fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(
                                        entity.nutrientLabel
                                )

                        return if (nutrient != null) {
                                val result =
                                        NutrientValue(
                                                nutrient = nutrient,
                                                quantity =
                                                        NutrientQuantity(
                                                                entity.value,
                                                                nutrient.unite
                                                        )
                                        )
                                println("DEBUG NutrientValue: Création réussie depuis l'entité")
                                result
                        } else {
                                println(
                                        "DEBUG NutrientValue: ÉCHEC - Nutriment non résolu pour le label ${entity.nutrientLabel}"
                                )
                                null
                        }
                }
        }
}
