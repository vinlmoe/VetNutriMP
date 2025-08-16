package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlinx.serialization.Serializable

/**
 * Classe représentant une variable supplémentaire pour les calculs Basée sur la classe
 * SupplementalvariableP du projet Java original
 */
@Serializable
data class SupplementalvariableP(val variable: VariableKind? = null, val varue: Double? = null)
