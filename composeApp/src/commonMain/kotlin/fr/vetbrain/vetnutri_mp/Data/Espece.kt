package fr.vetbrain.vetnutri_mp.Data

enum class Espece : Labelable {
    CHIEN {
        override val label = "Chien"
    },
    CHAT {
        override val label = "Chat"
    }
}
