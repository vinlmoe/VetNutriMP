package fr.vetbrain.vetnutri_mp.Data



import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "Ration", foreignKeys = [
    ForeignKey(entity = ConsultationEv::class, parentColumns = ["uuid"], childColumns = ["idConsult"], onDelete = ForeignKey.CASCADE)
])
@Serializable
data class Ration(
    @PrimaryKey val uuid: String =  Uuid.random().toString(),
    @ColumnInfo(name = "idConsult") val idConsult: String?, // Foreign key to ConsultationEv
    var name: String?,
    var coef: Float?,
    var actual: Boolean?,
    var number: Int?,
    var espece: String?, // Using String to store enum uuid
    var recette: Boolean?, // Assuming 'recette' column exists
    var description: String?,
    @Ignore var alimentMutableList: MutableList<AlimentRation>  // Transient, loaded separately
){

    fun getAlimentByUUID(uuiDalim: String): AlimentRation {
return alimentMutableList.last {
        al->al.uuid==uuiDalim
}
    }
}
