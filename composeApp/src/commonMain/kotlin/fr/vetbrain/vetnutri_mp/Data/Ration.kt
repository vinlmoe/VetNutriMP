package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Ration(
        var uuid: String = Uuid.random().toString(),
        var idConsult: String = "",
        var name: String = "",
        var coef: Float = 1.0f,
        var actual: Boolean = false,
        var number: Int = 1,
        var espece: String? = null,
        var recette: Boolean = false,
        var description: String = "",
        var alimentMutableList: MutableList<AlimentRation> = mutableListOf()
) {
        fun getAlimentByUUID(uuiDalim: String): AlimentRation {
                return alimentMutableList.last { al -> al.uuid == uuiDalim }
        }

        fun getEspece(): Espece {
                return Espece.getByLabel(espece ?: "") ?: Espece.CHIEN
        }

        fun setEspece(especeEnum: Espece) {
                this.espece = especeEnum.label
        }
}
