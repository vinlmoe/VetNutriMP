package fr.vetbrain.vetnutri_mp.Utils

import kotlin.native.Platform

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
actual fun isDebugBuild(): Boolean = Platform.isDebugBinary
