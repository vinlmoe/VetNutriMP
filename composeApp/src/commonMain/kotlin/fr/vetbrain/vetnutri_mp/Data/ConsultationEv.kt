package fr.vetbrain.vetnutri_mp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Entity(
        tableName = "Consultations",
        foreignKeys =
                [
                        ForeignKey(
                                entity = AnimalEv::class,
                                parentColumns = ["uuid"],
                                childColumns = ["idAnim"],
                                onDelete = ForeignKey.CASCADE
                        )]
)
@Serializable
data class ConsultationEv(
        @PrimaryKey val uuid: String = Uuid.random().toString(),
        var date: LocalDate? = null,
        var objectConsult: String? = null,
        var observation: String? = null,
        var cRendu: String? = null,
        var weight: Float? = null,
        var idealWeight: Float? = null,
        var water: Float? = null,
        var bodyFat: Float? = null,
        var methodAnalysis: String? = null,
        var BCS: Int? = null,
        var k1Id: String? = null,
        var k1Value: Float? = null,
        var k2Id: String? = null,
        var k2Value: Float? = null,
        var k3Id: String? = null,
        var k3Value: Float? = null,
        var k4Id: String? = null,
        var k4Value: Float? = null,
        var k5Id: String? = null,
        var k5Value: Float? = null,
        var nLittle: Int? = null,
        var pAdult: Float? = null,
        var coefGes: Int? = null,
        var coefLact: Int? = null,
        @ColumnInfo(name = "idAnim") var idAnim: String? = null,
        var MCS: Int? = null,
        @Ignore var suppVarp: MutableList<SupplementalvariableP> = mutableListOf(),
        @Ignore var diseaseRef: MutableList<String> = mutableListOf(),
        @Ignore var rationMutableList: MutableList<Ration> = mutableListOf()
) {
    constructor() : this(uuid = Uuid.random().toString())

    fun getRationByID(uuid: String): Ration {
        return rationMutableList.last { ration: Ration -> ration.uuid == uuid }
    }
}
