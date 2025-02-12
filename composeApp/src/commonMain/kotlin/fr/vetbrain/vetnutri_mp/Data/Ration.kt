package fr.vetbrain.vetnutri_mp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Entity(
        tableName = "Ration",
        foreignKeys =
                [
                        ForeignKey(
                                entity = ConsultationEv::class,
                                parentColumns = ["uuid"],
                                childColumns = ["idConsult"],
                                onDelete = ForeignKey.CASCADE
                        )]
)
@Serializable
data class Ration(
        @PrimaryKey val uuid: String = Uuid.random().toString(),
        @ColumnInfo(name = "idConsult") var idConsult: String? = null,
        var name: String? = null,
        var coef: Float? = null,
        var actual: Boolean? = null,
        var number: Int? = null,
        var espece: String? = null,
        var recette: Boolean? = null,
        var description: String? = null,
        @Ignore var alimentMutableList: MutableList<AlimentRation> = mutableListOf()
) {
    constructor() : this(uuid = Uuid.random().toString())

    fun getAlimentByUUID(uuiDalim: String): AlimentRation {
        return alimentMutableList.last { al -> al.uuid == uuiDalim }
    }
}
