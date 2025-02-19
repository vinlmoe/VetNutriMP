import fr.vetbrain.vetnutri_mp.Enumer.Espece
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class AdjustSaveEv(
        val uuid: String = Uuid.random().toString(),
        var name: String?,
        var species: String?,
        var description: String?,
        var MutableList: MutableList<TargetDefinitionEv>,
        var esp: Espece? = null
)
