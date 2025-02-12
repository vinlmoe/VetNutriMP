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
        const val NAME = "name"
        const val SPECIES = "species"
        const val SEX = "sex"
        const val BREED = "breed"
        const val ID = "id"
        const val OWNER = "owner"
        const val DESCRIPTION = "description"
        const val WEIGHT = "weight"
        const val AGE = "age"
        const val BIRTH_DATE = "birthDate"
    }

    // Clés pour les espèces
    object Species {
        const val DOG = "dog"
        const val CAT = "cat"
        const val HORSE = "horse"
        const val FERRET = "ferret"
        const val WILD_CANINE = "wildCanine"
        const val WILD_FELINE = "wildFeline"
        const val FOLIVORE = "folivore"
    }

    // Clés pour les analyses nutritionnelles
    object Nutrition {
        const val PROTEINS = "proteins"
        const val LIPIDS = "lipids"
        const val CARBOHYDRATES = "carbohydrates"
        const val FIBER = "fiber"
        const val MOISTURE = "moisture"
        const val ASH = "ash"
    }

    // Clés pour les vitamines
    object Vitamins {
        const val VITAMIN_A = "vitaminA"
        const val VITAMIN_B1 = "vitaminB1"
        const val VITAMIN_B2 = "vitaminB2"
        const val VITAMIN_B3 = "vitaminB3"
        const val VITAMIN_B5 = "vitaminB5"
        const val VITAMIN_B6 = "vitaminB6"
        const val VITAMIN_B8 = "vitaminB8"
        const val VITAMIN_B9 = "vitaminB9"
        const val VITAMIN_B12 = "vitaminB12"
        const val VITAMIN_C = "vitaminC"
        const val VITAMIN_D = "vitaminD"
        const val VITAMIN_E = "vitaminE"
        const val VITAMIN_K = "vitaminK"
    }

    // Clés pour les minéraux
    object Minerals {
        const val CALCIUM = "calcium"
        const val PHOSPHORUS = "phosphorus"
        const val MAGNESIUM = "magnesium"
        const val SODIUM = "sodium"
        const val POTASSIUM = "potassium"
        const val CHLORINE = "chlorine"
        const val IRON = "iron"
        const val ZINC = "zinc"
        const val COPPER = "copper"
        const val MANGANESE = "manganese"
        const val IODINE = "iodine"
        const val SELENIUM = "selenium"
    }

    // Clés pour les unités
    object Units {
        const val KILOGRAM = "kilogram"
        const val GRAM = "gram"
        const val MILLIGRAM = "milligram"
        const val MICROGRAM = "microgram"
        const val LITER = "liter"
        const val MILLILITER = "milliliter"
        const val PERCENTAGE = "percentage"
        const val INTERNATIONAL_UNIT = "internationalUnit"
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
