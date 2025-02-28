package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

/**
 * Classe utilitaire pour convertir les objets du modèle en JSON
 */
public class JsonConverter {
    private final Gson gson;

    public JsonConverter() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(GroupAlim.class, new GroupAlimAdapter())
                .registerTypeAdapter(FoodKind.class, new FoodKindAdapter())
                .registerTypeAdapter(ContEnum.class, new ContEnumAdapter())
                .registerTypeAdapter(Espece.class, new EspeceAdapter())
                .registerTypeAdapter(UnitReqEnum.class, new UnitReqEnumAdapter())
                .create();
    }

    /**
     * Convertit un AlimentEv en JSON
     */
    public String alimentEvToJson(AlimentEv aliment) {
        return gson.toJson(aliment);
    }

    /**
     * Convertit une liste d'AlimentEv en JSON
     */
    public String alimentEvListToJson(List<AlimentEv> aliments) {
        return gson.toJson(aliments);
    }

    /**
     * Convertit un AnimalEv en JSON
     */
    public String animalEvToJson(AnimalEv animal) {
        return gson.toJson(animal);
    }

    /**
     * Convertit une liste d'AnimalEv en JSON
     */
    public String animalEvListToJson(List<AnimalEv> animals) {
        return gson.toJson(animals);
    }

    /**
     * Convertit une ConsultationEv en JSON
     */
    public String consultationEvToJson(ConsultationEv consultation) {
        return gson.toJson(consultation);
    }

    /**
     * Convertit une liste de ConsultationEv en JSON
     */
    public String consultationEvListToJson(List<ConsultationEv> consultations) {
        return gson.toJson(consultations);
    }

    /**
     * Convertit un BiblioRef en JSON
     */
    public String biblioRefToJson(BiblioRef biblio) {
        return gson.toJson(biblio);
    }

    /**
     * Convertit une liste de BiblioRef en JSON
     */
    public String biblioRefListToJson(List<BiblioRef> biblios) {
        return gson.toJson(biblios);
    }

    /**
     * Convertit un AlimSaver en JSON
     */
    public String alimSaverToJson(AlimSaver alimSaver) {
        return gson.toJson(alimSaver);
    }

    /**
     * Convertit un AdjustSaveEv en JSON
     */
    public String adjustSaveEvToJson(AdjustSaveEv adjustSave) {
        return gson.toJson(adjustSave);
    }

    /**
     * Convertit une liste d'AdjustSaveEv en JSON
     */
    public String adjustSaveEvListToJson(List<AdjustSaveEv> adjustSaves) {
        return gson.toJson(adjustSaves);
    }

    /**
     * Convertit une liste de Recette en JSON
     */
    public String rationListToJson(List<Recette> rations) {
        return gson.toJson(rations);
    }
}

/**
 * Adaptateur pour sérialiser/désérialiser les LocalDate
 */
class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    @Override
    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(date.toString());
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return LocalDate.parse(json.getAsString());
    }
}

/**
 * Adaptateurs pour les énumérations
 */
class GroupAlimAdapter implements JsonSerializer<GroupAlim>, JsonDeserializer<GroupAlim> {
    @Override
    public JsonElement serialize(GroupAlim src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.nameToString());
    }

    @Override
    public GroupAlim deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return GroupAlim.StringToGroup(json.getAsString());
    }
}

class FoodKindAdapter implements JsonSerializer<FoodKind>, JsonDeserializer<FoodKind> {
    @Override
    public JsonElement serialize(FoodKind src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }

    @Override
    public FoodKind deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return FoodKind.valueOf(json.getAsString());
    }
}

class ContEnumAdapter implements JsonSerializer<ContEnum>, JsonDeserializer<ContEnum> {
    @Override
    public JsonElement serialize(ContEnum src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }

    @Override
    public ContEnum deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return ContEnum.valueOf(json.getAsString());
    }
}

class EspeceAdapter implements JsonSerializer<Espece>, JsonDeserializer<Espece> {
    @Override
    public JsonElement serialize(Espece src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.nameToString());
    }

    @Override
    public Espece deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return Espece.getEnumFromString(json.getAsString());
    }
}

class UnitReqEnumAdapter implements JsonSerializer<UnitReqEnum>, JsonDeserializer<UnitReqEnum> {
    @Override
    public JsonElement serialize(UnitReqEnum src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }

    @Override
    public UnitReqEnum deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return UnitReqEnum.valueOf(json.getAsString());
    }
}