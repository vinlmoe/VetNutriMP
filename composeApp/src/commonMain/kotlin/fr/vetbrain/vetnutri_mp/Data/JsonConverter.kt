package fr.vetbrain.vetnutri_mp.Data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import fr.vetbrain.vetnutri_mp.Utils.isDebugBuild

/** Classe utilitaire pour convertir les objets du modèle en JSON */
class JsonConverter {
    private val json = Json {
        prettyPrint = isDebugBuild()
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        serializersModule =
                SerializersModule {
                    // Les sérialiseurs contextuels seront ajoutés ici une fois que les énumérations
                    // seront disponibles
                }
    }

    /** Convertit un AlimentEv en JSON */
    fun alimentEvToJson(aliment: AlimentEv): String {
        return json.encodeToString(aliment.toJson())
    }

    /** Convertit une liste d'AlimentEv en JSON */
    fun alimentEvListToJson(aliments: List<AlimentEv>): String {
        return json.encodeToString(aliments.map { it.toJson() })
    }

    /** Convertit un JSON en AlimentEv */
    fun jsonToAlimentEv(jsonString: String): AlimentEv {
        val alimentEvJson = json.decodeFromString<AlimentEvJson>(jsonString)
        return alimentEvJson.toData()
    }

    /** Convertit un JSON en liste d'AlimentEv */
    fun jsonToAlimentEvList(jsonString: String): List<AlimentEv> {
        val alimentEvJsonList = json.decodeFromString<List<AlimentEvJson>>(jsonString)
        return alimentEvJsonList.map { it.toData() }
    }

    /** Convertit un AnimalEv en JSON */
    fun animalEvToJson(animal: AnimalEv): String {
        return json.encodeToString(animal.toJson())
    }

    /** Convertit une liste d'AnimalEv en JSON */
    fun animalEvListToJson(animals: List<AnimalEv>): String {
        return json.encodeToString(animals.map { it.toJson() })
    }

    /** Convertit un JSON en AnimalEv */
    fun jsonToAnimalEv(jsonString: String): AnimalEv {
        val animalEvJson = json.decodeFromString<AnimalEvJson>(jsonString)
        return animalEvJson.toData()
    }

    /** Convertit un JSON en liste d'AnimalEv */
    fun jsonToAnimalEvList(jsonString: String): List<AnimalEv> {
        val animalEvJsonList = json.decodeFromString<List<AnimalEvJson>>(jsonString)
        return animalEvJsonList.map { it.toData() }
    }

    /** Convertit une ConsultationEv en JSON */
    fun consultationEvToJson(consultation: ConsultationEv): String {
        return json.encodeToString(consultation.toJson())
    }

    /** Convertit une liste de ConsultationEv en JSON */
    fun consultationEvListToJson(consultations: List<ConsultationEv>): String {
        return json.encodeToString(consultations.map { it.toJson() })
    }

    /** Convertit un JSON en ConsultationEv */
    fun jsonToConsultationEv(jsonString: String): ConsultationEv {
        val consultationEvJson = json.decodeFromString<ConsultationEvJson>(jsonString)
        return consultationEvJson.toData()
    }

    /** Convertit un JSON en liste de ConsultationEv */
    fun jsonToConsultationEvList(jsonString: String): List<ConsultationEv> {
        val consultationEvJsonList = json.decodeFromString<List<ConsultationEvJson>>(jsonString)
        return consultationEvJsonList.map { it.toData() }
    }

    /** Convertit une liste de Ration en JSON */
    fun rationListToJson(rations: List<Ration>): String {
        return json.encodeToString(rations.map { it.toJson() })
    }

    /** Convertit un JSON en liste de Ration */
    fun jsonToRationList(jsonString: String): List<Ration> {
        val rationJsonList = json.decodeFromString<List<RationJson>>(jsonString)
        return rationJsonList.map { it.toData() }
    }

    companion object {
        // Instance singleton pour faciliter l'utilisation
        val instance = JsonConverter()
    }
}
