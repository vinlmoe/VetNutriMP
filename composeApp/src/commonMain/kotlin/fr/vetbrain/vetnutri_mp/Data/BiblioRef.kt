package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import kotlinx.serialization.Serializable
import kotlin.uuid.*

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "Biblio")
@Serializable
data class BiblioRef(
    @PrimaryKey val uuid: String = Uuid.random().toString(),
    @ColumnInfo(name = "fAuthor") var firstAuthor: String?,
    var year: String?,
    @ColumnInfo(name = "fullRef") var completeRef: String?,
    var comments: String?,
    var consistent: Int?
) 