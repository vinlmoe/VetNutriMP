package fr.vetbrain.vetnutri_mp.Localization

object LocalizationKeys {
    // Clés générales de l'application
    object General {
        const val WELCOME = "welcome"
        const val APP_NAME = "app_name"
        const val LOADING = "loading"
        const val SAVE = "save"
        const val CANCEL = "cancel"
        const val DELETE = "delete"
        const val EDIT = "edit"
        const val ADD = "add"
        const val REMOVE = "remove"
        const val SEARCH = "search"
        const val CALCULATE = "calculate"
        const val VALIDATE = "validate"
        const val EXPORT = "export"
        const val IMPORT = "import"
    }

    // Clés pour les animaux
    object Animal {
        const val NAME = "animal.name"
        const val SPECIES = "animal.species"
        const val SEX = "animal.sex"
        const val BREED = "animal.breed"
        const val ID = "animal.id"
        const val OWNER = "animal.owner"
        const val DESCRIPTION = "animal.description"
        const val WEIGHT = "animal.weight"
        const val AGE = "animal.age"
        const val BIRTH_DATE = "animal.birthDate"
        const val SUMMARY = "animal.summary"
        const val STERILIZED = "animal.sterilized"
        const val DEAD = "animal.dead"
    }

    // Clés pour les espèces
    object Species {
        const val DOG = "species.dog"
        const val CAT = "species.cat"
        const val HORSE = "species.horse"
        const val FERRET = "species.ferret"
        const val WILD_CANINE = "species.wildCanine"
        const val WILD_FELINE = "species.wildFeline"
        const val FOLIVORE = "species.folivore"
    }

    // Clés pour les analyses nutritionnelles
    object Nutrition {
        const val PROTEINS = "nutrition.proteins"
        const val LIPIDS = "nutrition.lipids"
        const val CARBOHYDRATES = "nutrition.carbohydrates"
        const val FIBER = "nutrition.fiber"
        const val MOISTURE = "nutrition.moisture"
        const val ASH = "nutrition.ash"
        const val ENERGY = "nutrition.energy"
    }

    // Clés pour les vitamines
    object Vitamins {
        const val VITAMIN_A = "vitamins.a"
        const val VITAMIN_B1 = "vitamins.b1"
        const val VITAMIN_B2 = "vitamins.b2"
        const val VITAMIN_B3 = "vitamins.b3"
        const val VITAMIN_B5 = "vitamins.b5"
        const val VITAMIN_B6 = "vitamins.b6"
        const val VITAMIN_B8 = "vitamins.b8"
        const val VITAMIN_B9 = "vitamins.b9"
        const val VITAMIN_B12 = "vitamins.b12"
        const val VITAMIN_C = "vitamins.c"
        const val VITAMIN_D = "vitamins.d"
        const val VITAMIN_E = "vitamins.e"
        const val VITAMIN_K = "vitamins.k"
    }

    // Clés pour les minéraux
    object Minerals {
        const val CALCIUM = "minerals.calcium"
        const val PHOSPHORUS = "minerals.phosphorus"
        const val MAGNESIUM = "minerals.magnesium"
        const val SODIUM = "minerals.sodium"
        const val POTASSIUM = "minerals.potassium"
        const val CHLORINE = "minerals.chlorine"
        const val IRON = "minerals.iron"
        const val ZINC = "minerals.zinc"
        const val COPPER = "minerals.copper"
        const val MANGANESE = "minerals.manganese"
        const val IODINE = "minerals.iodine"
        const val SELENIUM = "minerals.selenium"
    }

    // Clés pour les unités
    object Units {
        const val KILOGRAM = "units.kilogram"
        const val GRAM = "units.gram"
        const val MILLIGRAM = "units.milligram"
        const val MICROGRAM = "units.microgram"
        const val LITER = "units.liter"
        const val MILLILITER = "units.milliliter"
        const val PERCENTAGE = "units.percentage"
        const val INTERNATIONAL_UNIT = "units.internationalUnit"
        const val MCAL = "units.mcal"
        const val KCAL = "units.kcal"
    }

    // Clés pour les consultations
    object Consultation {
        const val DATE = "consultation.date"
        const val OBJECTIVE = "consultation.objective"
        const val OBSERVATION = "consultation.observation"
        const val REPORT = "consultation.report"
        const val IDEAL_WEIGHT = "consultation.idealWeight"
        const val BODY_FAT = "consultation.bodyFat"
        const val BCS = "consultation.bcs"
        const val MCS = "consultation.mcs"
        const val WATER = "consultation.water"
    }

    // Clés pour les rations
    object Ration {
        const val NAME = "ration.name"
        const val COEFFICIENT = "ration.coefficient"
        const val ACTUAL = "ration.actual"
        const val NUMBER = "ration.number"
        const val RECIPE = "ration.recipe"
        const val DESCRIPTION = "ration.description"
    }

    // Clés pour les erreurs
    object Error {
        const val REQUIRED_FIELD = "error.requiredField"
        const val INVALID_VALUE = "error.invalidValue"
        const val DATABASE_ERROR = "error.database"
        const val NETWORK_ERROR = "error.network"
        const val UNKNOWN_ERROR = "error.unknown"
    }

    enum class AnimalKeys {
        ID,
        NAME,
        OWNER,
        BREED,
        BIRTHDATE,
        SUMMARY
    }
}
