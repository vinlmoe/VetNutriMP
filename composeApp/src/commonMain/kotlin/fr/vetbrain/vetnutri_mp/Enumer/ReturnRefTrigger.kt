package fr.vetbrain.vetnutri_mp.Enumer

import kotlinx.serialization.Serializable

/**
 * Énumération représentant les types de déclenchements lors de la vérification des références de
 * nutriments Basée sur l'énumération ReturnRefTrigger du projet Java original
 */
@Serializable
enum class ReturnRefTrigger {
    NORM, // Valeur normale
    REFUP, // Valeur au-dessus de la référence
    REFDOWN, // Valeur en-dessous de la référence
    DISUP, // Valeur au-dessus de la référence pour une maladie
    DISDOWN, // Valeur en-dessous de la référence pour une maladie
    REFOPTIUP, // Valeur au-dessus de l'optimum
    REFOPTIDOWN // Valeur en-dessous de l'optimum
}
