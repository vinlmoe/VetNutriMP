package fr.vetbrain.vetnutri_mp.Data
data class AdjustSaveEv(
    val uuid: String,
    val name: String,
    val esp: Espece?, // Assuming Espece is another data class/enum and can be null
    val description: String,
    val all: List<TargetDefinitionEv> // Assuming TargetDefinitionEv is another data class
)
