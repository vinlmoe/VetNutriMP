package fr.vetbrain.vetnutri_mp.Data

enum class Sexe : Labelable {
    MALE {
        override val label = "Mâle"
    },
    FEMELLE {
        override val label = "Femelle"
    }
}
