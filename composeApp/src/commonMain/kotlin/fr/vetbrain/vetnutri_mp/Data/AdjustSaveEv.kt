package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import kotlinx.serialization.Serializable
import kotlin.uuid.*
import fr.vetbrain.vetnutri_mp.Enumer.Espece

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "Method")
@Serializable
data class AdjustSaveEv(
    @PrimaryKey val uuid: String = Uuid.random().toString(),
    var name: String?,
    var species: String?,
    var description: String?,
    @Ignore var MutableList: MutableList<TargetDefinitionEv>,
    @Ignore var esp: Espece? = null
)
