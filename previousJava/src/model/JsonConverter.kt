package model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate

/** Classe utilitaire pour convertir les objets du modèle en JSON */
class JsonConverter {
    private val gson: Gson =
            GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
                    .registerTypeAdapter(GroupAlim::class.java, GroupAlimAdapter())
                    .registerTypeAdapter(FoodKind::class.java, FoodKindAdapter())
                    .registerTypeAdapter(ContEnum::class.java, ContEnumAdapter())
                    .registerTypeAdapter(Espece::class.java, EspeceAdapter())
                    .registerTypeAdapter(UnitReqEnum::class.java, UnitReqEnumAdapter())
                    .create()

    /** Convertit un AlimentEv en JSON */
    fun alimentEvToJson(aliment: AlimentEv): String {
        return gson.toJson(aliment)
    }

    /** Convertit une liste d'AlimentEv en JSON */
    fun alimentEvListToJson(aliments: List<AlimentEv>): String {
        return gson.toJson(aliments)
    }

    /** Convertit un AnimalEv en JSON */
    fun animalEvToJson(animal: AnimalEv): String {
        return gson.toJson(animal)
    }

    /** Convertit une liste d'AnimalEv en JSON */
    fun animalEvListToJson(animals: List<AnimalEv>): String {
        return gson.toJson(animals)
    }

    /** Convertit une ConsultationEv en JSON */
    fun consultationEvToJson(consultation: ConsultationEv): String {
        return gson.toJson(consultation)
    }

    /** Convertit une liste de ConsultationEv en JSON */
    fun consultationEvListToJson(consultations: List<ConsultationEv>): String {
        return gson.toJson(consultations)
    }

    /** Convertit un BiblioRef en JSON */
    fun biblioRefToJson(biblio: BiblioRef): String {
        return gson.toJson(biblio)
    }

    /** Convertit une liste de BiblioRef en JSON */
    fun biblioRefListToJson(biblios: List<BiblioRef>): String {
        return gson.toJson(biblios)
    }

    /** Convertit un AlimSaver en JSON */
    fun alimSaverToJson(alimSaver: AlimSaver): String {
        return gson.toJson(alimSaver)
    }

    /** Convertit un AdjustSaveEv en JSON */
    fun adjustSaveEvToJson(adjustSave: AdjustSaveEv): String {
        return gson.toJson(adjustSave)
    }

    /** Convertit une liste d'AdjustSaveEv en JSON */
    fun adjustSaveEvListToJson(adjustSaves: List<AdjustSaveEv>): String {
        return gson.toJson(adjustSaves)
    }

    /** Convertit un AlimentRation en JSON */
    fun alimentRationToJson(alimentRation: AlimentRation): String {
        return gson.toJson(alimentRation)
    }

    /** Convertit une liste d'AlimentRation en JSON */
    fun alimentRationListToJson(alimentRations: List<AlimentRation>): String {
        return gson.toJson(alimentRations)
    }

    /** Convertit une liste de Ration en JSON */
    fun rationListToJson(rations: List<Recette>): String {
        return gson.toJson(rations)
    }
}

/** Adaptateur pour sérialiser/désérialiser les LocalDate */
class LocalDateAdapter :
        com.google.gson.JsonSerializer<LocalDate>, com.google.gson.JsonDeserializer<LocalDate> {
    override fun serialize(
            date: LocalDate,
            typeOfSrc: java.lang.reflect.Type,
            context: com.google.gson.JsonSerializationContext
    ): com.google.gson.JsonElement {
        return com.google.gson.JsonPrimitive(date.toString())
    }

    override fun deserialize(
            json: com.google.gson.JsonElement,
            typeOfT: java.lang.reflect.Type,
            context: com.google.gson.JsonDeserializationContext
    ): LocalDate {
        return LocalDate.parse(json.asString)
    }
}

// Adaptateurs pour les énumérations
class GroupAlimAdapter : JsonSerializer<GroupAlim>, JsonDeserializer<GroupAlim> {
    override fun serialize(
            src: GroupAlim,
            typeOfSrc: Type,
            context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.nameToString())
    }

    override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
    ): GroupAlim {
        return GroupAlim.StringToGroup(json.asString)
    }
}

class FoodKindAdapter : JsonSerializer<FoodKind>, JsonDeserializer<FoodKind> {
    override fun serialize(
            src: FoodKind,
            typeOfSrc: Type,
            context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.name)
    }

    override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
    ): FoodKind {
        return FoodKind.valueOf(json.asString)
    }
}

class ContEnumAdapter : JsonSerializer<ContEnum>, JsonDeserializer<ContEnum> {
    override fun serialize(
            src: ContEnum,
            typeOfSrc: Type,
            context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.name)
    }

    override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
    ): ContEnum {
        return ContEnum.valueOf(json.asString)
    }
}

class EspeceAdapter : JsonSerializer<Espece>, JsonDeserializer<Espece> {
    override fun serialize(
            src: Espece,
            typeOfSrc: Type,
            context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.nameToString())
    }

    override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
    ): Espece {
        return Espece.getEnumFromString(json.asString)
    }
}

class UnitReqEnumAdapter : JsonSerializer<UnitReqEnum>, JsonDeserializer<UnitReqEnum> {
    override fun serialize(
            src: UnitReqEnum,
            typeOfSrc: Type,
            context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.name)
    }

    override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
    ): UnitReqEnum {
        return UnitReqEnum.valueOf(json.asString)
    }
}
