package fr.vetbrain.vetnutri_mp.Enumerise

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class ConditionEnum( override val label: String,  val unite: String) :Labelable{
    MORE(">", ""),
    LESS("<", ""),
    INCLUDE("INCLUDE", ""),
    EXCLUDE("EXCLUDE", "");

  
}
