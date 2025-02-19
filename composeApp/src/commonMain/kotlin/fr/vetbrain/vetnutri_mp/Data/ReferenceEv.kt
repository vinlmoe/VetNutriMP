import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import fr.vetbrain.vetnutri_mp.Data.CoefP


@OptIn(ExperimentalUuidApi::class)
data class ReferenceEv(
        val uuid: String = Uuid.random().toString(),
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
        var BWEqu: Equation? = null,
        var BEEqu: Equation? = null,
        var DEcomEqu: Equation? = null,
        var DErawEqu: Equation? = null,
        var modk1: MutableList<CoefP>,
        var modk2: MutableList<CoefP>,
        var modk3: MutableList<CoefP>,
        var modk4: MutableList<CoefP>,
        var modk5: MutableList<CoefP>,
        var nutEqu: MutableList<Equation>
)
