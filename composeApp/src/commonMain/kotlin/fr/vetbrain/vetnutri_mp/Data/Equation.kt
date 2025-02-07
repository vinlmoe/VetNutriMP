package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import fr.vetbrain.vetnutri_mp.Enumerise.VariableKind
import kotlinx.serialization.Serializable

import kotlin.uuid.*

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "Equation")
@Serializable
data class Equation(
    @PrimaryKey val uuid: String = Uuid.random().toString(),
    var script: String?,
    @ColumnInfo(name = "refBiblio") var refBiblio: String?,
    var name: String?,
    var description: String?,
    @ColumnInfo(name = "speciesRef") var specie: String?,
    var kind: Int?,
    var consistent: Boolean?,
    var nutrient: Int?,
    @Ignore var bib: BiblioRef? = null,
    @Ignore var varMutableList: MutableList<VariableKind>
) 