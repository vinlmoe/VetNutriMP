package fr.vetbrain.vetnutri_mp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import fr.vetbrain.vetnutri_mp.Enumerise.TextConstant
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "Consultations", foreignKeys = [
    ForeignKey(entity = AnimalEv::class, parentColumns = ["uuid"], childColumns = ["idAnim"], onDelete = ForeignKey.CASCADE)
])
@Serializable
data class ConsultationEv(
    @PrimaryKey val uuid: String =  Uuid.random().toString(),
    val date: LocalDate?,
    val objectConsult: String?, // Renamed from 'object' to avoid keyword clash
    val observation: String?,
    val cRendu: String?,
    val weight: Float?,
    val idealWeight: Float?,
    val water: Float?,
    val bodyFat: Float?,
    val methodAnalysis: String?,
    val BCS: Int?,
    val k1Id: String?,
    val k1Value: Float?,
    val k2Id: String?,
    val k2Value: Float?,
    val k3Id: String?,
    val k3Value: Float?,
    val k4Id: String?,
    val k4Value: Float?,
    val k5Id: String?,
    val k5Value: Float?,
    val nLittle: Int?,
    val pAdult: Float?,
    val coefGes: Int?, // Using Int to store enum ordinal
    val coefLact: Int?, // Using Int to store enum ordinal
    @ColumnInfo(name = "idAnim") val idAnim: String?, // Foreign key to AnimalEv
    val MCS: Int?,
    @Ignore var suppVarp: MutableList<SupplementalvariableP>, // Transient
    @Ignore var diseaseRef: MutableList<String>, // Transient
    @Ignore var rationMutableList: MutableList<Ration>  // Transient
)