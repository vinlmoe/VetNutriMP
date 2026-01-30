package fr.vetbrain.vetnutri_mp.Utils

import kotlin.native.Platform

actual fun isDebugBuild(): Boolean = Platform.isDebugBinary
