package fr.vetbrain.vetnutri_mp.Repository

/**
 * Levée lorsqu'on tente de supprimer un aliment encore référencé par au moins une ration
 * (table ALIMENTS, colonne `refAlimUnif`). Rien n'a été supprimé lorsque cette exception est levée.
 *
 * @property foodUuid identifiant de l'aliment
 * @property usageCount nombre de lignes de ration qui l'utilisent
 */
class FoodInUseException(val foodUuid: String, val usageCount: Int) :
        Exception("L'aliment $foodUuid est utilisé dans $usageCount ration(s) et ne peut pas être supprimé")
