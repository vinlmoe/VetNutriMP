package fr.vetbrain.vetnutri_mp.Utils
import platform.Foundation.*

actual fun TimeStamp(): Long = NSDate().timeIntervalSince1970.toLong() * 1000