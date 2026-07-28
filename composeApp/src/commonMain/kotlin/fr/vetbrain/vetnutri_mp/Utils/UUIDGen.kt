package fr.vetbrain.vetnutri_mp.Utils
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock

@OptIn(ExperimentalUuidApi::class)
fun genUUID():String{
    val uuid = Uuid.random().toString()
return uuid
}

/**
 * Génère un UUID unique avec timestamp pour éviter les conflits d'unicité
 * @return Un UUID unique basé sur le timestamp actuel et un nombre aléatoire
 */
fun genUniqueUUID(): String {
    val timestamp = Clock.System.now().toEpochMilliseconds()
    val random = kotlin.random.Random.nextInt()
    return "$timestamp-$random"
}