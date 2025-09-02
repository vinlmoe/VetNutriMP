// Exemple d'utilisation du système de correspondance DataB

import fr.vetbrain.vetnutri_mp.Utils.DataBMapping

fun main() {
    // Exemples d'utilisation de DataBMapping

    // Obtenir le nom lisible d'une valeur dataB
    val displayName1 = DataBMapping.getDisplayName("0")
    println("0 -> $displayName1") // Affiche: 0 -> CIQUAL

    val displayName2 = DataBMapping.getDisplayName("1")
    println("1 -> $displayName2") // Affiche: 1 -> FCEN

    val displayName3 = DataBMapping.getDisplayName("VF24")
    println("VF24 -> $displayName3") // Affiche: VF24 -> VetFood 2024

    // Valeur inconnue
    val unknownValue = DataBMapping.getDisplayName("99")
    println("99 -> $unknownValue") // Affiche: 99 -> 99

    // Valeur vide/null
    val emptyValue = DataBMapping.getDisplayName("")
    println("\"\" -> $emptyValue") // Affiche: "" -> Base de données inconnue

    // Vérifier si une valeur a une correspondance connue
    val hasMapping = DataBMapping.hasMapping("0")
    println("0 has mapping: $hasMapping") // Affiche: 0 has mapping: true

    val hasMappingUnknown = DataBMapping.hasMapping("99")
    println("99 has mapping: $hasMappingUnknown") // Affiche: 99 has mapping: false

    // Obtenir toutes les correspondances
    val allMappings = DataBMapping.getAllMappings()
    println("All mappings: $allMappings")
    // Affiche: All mappings: {0=CIQUAL, 1=FCEN, 2=PetFood Divers, 4=Générique, 5=Aliment Barf, VF24=VetFood 2024}

    // Recharger les correspondances (utile après modification du fichier JSON)
    DataBMapping.reloadMappings()
}
