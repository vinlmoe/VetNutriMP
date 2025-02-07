package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import kotlin.uuid.*
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "DataRef")
@Serializable
data class ReferenceEv(
        @PrimaryKey val uuid: String = Uuid.random().toString(),
        var name: String?,
        var description: String?,
        var disease: Boolean?,
        var BWeqRef: String?,
        var SERName: String?,
        var SERRef: String?,
        var DEcomRef: String?,
        var DErawRef: String?,
        var k1Name: String?,
        var k1Ref: String?,
        var k2Name: String?,
        var k2Ref: String?,
        var k3Name: String?,
        var k3Ref: String?,
        var k4Name: String?,
        var k4Ref: String?,
        var k5Name: String?,
        var k5Ref: String?,
        var specie: String?,
        var consistent: Int?,
        @Ignore var BWEqu: Equation? = null,
        @Ignore var BEEqu: Equation? = null,
        @Ignore var DEcomEqu: Equation? = null,
        @Ignore var DErawEqu: Equation? = null,
        @Ignore var modk1: MutableList<CoefP>,
        @Ignore var modk2: MutableList<CoefP>,
        @Ignore var modk3: MutableList<CoefP>,
        @Ignore var modk4: MutableList<CoefP>,
        @Ignore var modk5: MutableList<CoefP>,
        @Ignore var nutEqu: MutableList<Equation>
)
