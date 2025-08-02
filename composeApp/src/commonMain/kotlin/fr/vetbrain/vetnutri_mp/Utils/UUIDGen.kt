package fr.vetbrain.vetnutri_mp.Utils
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun genUUID():String{
    val uuid = Uuid.random().toString()
return uuid
}