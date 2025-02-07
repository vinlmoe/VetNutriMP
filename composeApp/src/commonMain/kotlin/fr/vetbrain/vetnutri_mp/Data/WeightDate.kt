package fr.vetbrain.vetnutri_mp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "Weight")
@Serializable
data class WeightDate(
        @PrimaryKey val uuid: String = Uuid.random().toString(),
        @ColumnInfo(name = "refAnimal") var refAnimal: String?, // Foreign key to AnimalEv
        var date: LocalDate?,
        var value: Float?
)
