package fr.vetbrain.vetnutri_mp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
