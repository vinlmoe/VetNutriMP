package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.runBlocking

actual fun <T> runBlockingOnPlatform(block: suspend () -> T): T = runBlocking { block() }
