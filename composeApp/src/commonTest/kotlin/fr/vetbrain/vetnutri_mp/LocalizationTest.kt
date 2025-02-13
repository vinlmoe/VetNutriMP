package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Localization.ResourceReader
import kotlin.test.*

class LocalizationTest {
    private lateinit var mockResourceReader: ResourceReader

    @BeforeTest
    fun setup() {
        mockResourceReader = TestResourceReader()
        LocalizationManager.setResourceReader(mockResourceReader)
        LocalizationManager.initialize()
    }

    @Test
    fun `test toutes les clés générales sont présentes`() {
        // Arrange
        val generalKeys =
                listOf(
                        LocalizationKeys.General.WELCOME,
                        LocalizationKeys.General.APP_NAME,
                        LocalizationKeys.General.LOADING,
                        LocalizationKeys.General.SAVE,
                        LocalizationKeys.General.CANCEL,
                        LocalizationKeys.General.DELETE,
                        LocalizationKeys.General.EDIT,
                        LocalizationKeys.General.ADD,
                        LocalizationKeys.General.REMOVE,
                        LocalizationKeys.General.SEARCH,
                        LocalizationKeys.General.CALCULATE,
                        LocalizationKeys.General.VALIDATE,
                        LocalizationKeys.General.EXPORT,
                        LocalizationKeys.General.IMPORT
                )

        // Act & Assert
        generalKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    @Test
    fun `test toutes les clés animal sont présentes`() {
        val animalKeys =
                listOf(
                        LocalizationKeys.Animal.NAME,
                        LocalizationKeys.Animal.SPECIES,
                        LocalizationKeys.Animal.SEX,
                        LocalizationKeys.Animal.BREED,
                        LocalizationKeys.Animal.ID,
                        LocalizationKeys.Animal.OWNER,
                        LocalizationKeys.Animal.DESCRIPTION,
                        LocalizationKeys.Animal.WEIGHT,
                        LocalizationKeys.Animal.AGE,
                        LocalizationKeys.Animal.BIRTH_DATE,
                        LocalizationKeys.Animal.SUMMARY,
                        LocalizationKeys.Animal.STERILIZED,
                        LocalizationKeys.Animal.DEAD
                )

        animalKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    @Test
    fun `test toutes les clés espèces sont présentes`() {
        val speciesKeys =
                listOf(
                        LocalizationKeys.Species.DOG,
                        LocalizationKeys.Species.CAT,
                        LocalizationKeys.Species.HORSE,
                        LocalizationKeys.Species.FERRET,
                        LocalizationKeys.Species.WILD_CANINE,
                        LocalizationKeys.Species.WILD_FELINE,
                        LocalizationKeys.Species.FOLIVORE
                )

        speciesKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    @Test
    fun `test toutes les clés nutrition sont présentes`() {
        val nutritionKeys =
                listOf(
                        LocalizationKeys.Nutrition.PROTEINS,
                        LocalizationKeys.Nutrition.LIPIDS,
                        LocalizationKeys.Nutrition.CARBOHYDRATES,
                        LocalizationKeys.Nutrition.FIBER,
                        LocalizationKeys.Nutrition.MOISTURE,
                        LocalizationKeys.Nutrition.ASH,
                        LocalizationKeys.Nutrition.ENERGY
                )

        nutritionKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    @Test
    fun `test toutes les clés vitamines sont présentes`() {
        val vitaminKeys =
                listOf(
                        LocalizationKeys.Vitamins.VITAMIN_A,
                        LocalizationKeys.Vitamins.VITAMIN_B1,
                        LocalizationKeys.Vitamins.VITAMIN_B2,
                        LocalizationKeys.Vitamins.VITAMIN_B3,
                        LocalizationKeys.Vitamins.VITAMIN_B5,
                        LocalizationKeys.Vitamins.VITAMIN_B6,
                        LocalizationKeys.Vitamins.VITAMIN_B8,
                        LocalizationKeys.Vitamins.VITAMIN_B9,
                        LocalizationKeys.Vitamins.VITAMIN_B12,
                        LocalizationKeys.Vitamins.VITAMIN_C,
                        LocalizationKeys.Vitamins.VITAMIN_D,
                        LocalizationKeys.Vitamins.VITAMIN_E,
                        LocalizationKeys.Vitamins.VITAMIN_K
                )

        vitaminKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    @Test
    fun `test toutes les clés minéraux sont présentes`() {
        val mineralKeys =
                listOf(
                        LocalizationKeys.Minerals.CALCIUM,
                        LocalizationKeys.Minerals.PHOSPHORUS,
                        LocalizationKeys.Minerals.MAGNESIUM,
                        LocalizationKeys.Minerals.SODIUM,
                        LocalizationKeys.Minerals.POTASSIUM,
                        LocalizationKeys.Minerals.CHLORINE,
                        LocalizationKeys.Minerals.IRON,
                        LocalizationKeys.Minerals.ZINC,
                        LocalizationKeys.Minerals.COPPER,
                        LocalizationKeys.Minerals.MANGANESE,
                        LocalizationKeys.Minerals.IODINE,
                        LocalizationKeys.Minerals.SELENIUM
                )

        mineralKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    @Test
    fun `test toutes les clés unités sont présentes`() {
        val unitKeys =
                listOf(
                        LocalizationKeys.Units.KILOGRAM,
                        LocalizationKeys.Units.GRAM,
                        LocalizationKeys.Units.MILLIGRAM,
                        LocalizationKeys.Units.MICROGRAM,
                        LocalizationKeys.Units.LITER,
                        LocalizationKeys.Units.MILLILITER,
                        LocalizationKeys.Units.PERCENTAGE,
                        LocalizationKeys.Units.INTERNATIONAL_UNIT,
                        LocalizationKeys.Units.MCAL,
                        LocalizationKeys.Units.KCAL
                )

        unitKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    @Test
    fun `test toutes les clés consultation sont présentes`() {
        val consultationKeys =
                listOf(
                        LocalizationKeys.Consultation.DATE,
                        LocalizationKeys.Consultation.OBJECTIVE,
                        LocalizationKeys.Consultation.OBSERVATION,
                        LocalizationKeys.Consultation.REPORT,
                        LocalizationKeys.Consultation.IDEAL_WEIGHT,
                        LocalizationKeys.Consultation.BODY_FAT,
                        LocalizationKeys.Consultation.BCS,
                        LocalizationKeys.Consultation.MCS,
                        LocalizationKeys.Consultation.WATER
                )

        consultationKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    @Test
    fun `test toutes les clés ration sont présentes`() {
        val rationKeys =
                listOf(
                        LocalizationKeys.Ration.NAME,
                        LocalizationKeys.Ration.COEFFICIENT,
                        LocalizationKeys.Ration.ACTUAL,
                        LocalizationKeys.Ration.NUMBER,
                        LocalizationKeys.Ration.RECIPE,
                        LocalizationKeys.Ration.DESCRIPTION
                )

        rationKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    @Test
    fun `test toutes les clés erreur sont présentes`() {
        val errorKeys =
                listOf(
                        LocalizationKeys.Error.REQUIRED_FIELD,
                        LocalizationKeys.Error.INVALID_VALUE,
                        LocalizationKeys.Error.DATABASE_ERROR,
                        LocalizationKeys.Error.NETWORK_ERROR,
                        LocalizationKeys.Error.UNKNOWN_ERROR
                )

        errorKeys.forEach { key ->
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    private class TestResourceReader : ResourceReader() {
        override fun readResource(name: String): String =
                """
            {
                "translations": {
                    "welcome": "Bienvenue",
                    "app_name": "VetNutri MP",
                    "loading": "Chargement...",
                    "save": "Sauvegarder",
                    "cancel": "Annuler",
                    "delete": "Supprimer",
                    "edit": "Éditer",
                    "add": "Ajouter",
                    "remove": "Retirer",
                    "search": "Rechercher",
                    "calculate": "Calculer",
                    "validate": "Valider",
                    "export": "Exporter",
                    "import": "Importer",
                    "animal.name": "Nom",
                    "animal.species": "Espèce",
                    "animal.sex": "Sexe",
                    "animal.breed": "Race",
                    "animal.id": "ID",
                    "animal.owner": "Propriétaire",
                    "animal.description": "Description",
                    "animal.weight": "Poids",
                    "animal.age": "Âge",
                    "animal.birthDate": "Date de naissance",
                    "animal.summary": "Résumé",
                    "animal.sterilized": "Stérilisé",
                    "animal.dead": "Décédé",
                    "species.dog": "Chien",
                    "species.cat": "Chat",
                    "species.horse": "Cheval",
                    "species.ferret": "Furet",
                    "species.wildCanine": "Canidé sauvage",
                    "species.wildFeline": "Félin sauvage",
                    "species.folivore": "Folivore",
                    "nutrition.proteins": "Protéines",
                    "nutrition.lipids": "Lipides",
                    "nutrition.carbohydrates": "Glucides",
                    "nutrition.fiber": "Fibres",
                    "nutrition.moisture": "Humidité",
                    "nutrition.ash": "Cendres",
                    "nutrition.energy": "Énergie",
                    "vitamins.a": "Vitamine A",
                    "vitamins.b1": "Thiamine (B1)",
                    "vitamins.b2": "Riboflavine (B2)",
                    "vitamins.b3": "Niacine (B3)",
                    "vitamins.b5": "Acide pantothénique (B5)",
                    "vitamins.b6": "Pyridoxine (B6)",
                    "vitamins.b8": "Biotine (B8)",
                    "vitamins.b9": "Acide Folique (B9)",
                    "vitamins.b12": "Cobalamine (B12)",
                    "vitamins.c": "Vitamine C",
                    "vitamins.d": "Vitamine D",
                    "vitamins.e": "Vitamine E",
                    "vitamins.k": "Vitamine K",
                    "minerals.calcium": "Calcium",
                    "minerals.phosphorus": "Phosphore",
                    "minerals.magnesium": "Magnésium",
                    "minerals.sodium": "Sodium",
                    "minerals.potassium": "Potassium",
                    "minerals.chlorine": "Chlore",
                    "minerals.iron": "Fer",
                    "minerals.zinc": "Zinc",
                    "minerals.copper": "Cuivre",
                    "minerals.manganese": "Manganèse",
                    "minerals.iodine": "Iode",
                    "minerals.selenium": "Sélénium",
                    "units.kilogram": "kg",
                    "units.gram": "g",
                    "units.milligram": "mg",
                    "units.microgram": "µg",
                    "units.liter": "L",
                    "units.milliliter": "mL",
                    "units.percentage": "%",
                    "units.internationalUnit": "UI",
                    "units.mcal": "Mcal",
                    "units.kcal": "kcal",
                    "consultation.date": "Date",
                    "consultation.objective": "Objectif",
                    "consultation.observation": "Observation",
                    "consultation.report": "Compte-rendu",
                    "consultation.idealWeight": "Poids idéal",
                    "consultation.bodyFat": "Masse grasse",
                    "consultation.bcs": "NEC",
                    "consultation.mcs": "MCS",
                    "consultation.water": "Eau",
                    "ration.name": "Nom",
                    "ration.coefficient": "Coefficient",
                    "ration.actual": "Actuelle",
                    "ration.number": "Numéro",
                    "ration.recipe": "Recette",
                    "ration.description": "Description",
                    "error.requiredField": "Champ requis",
                    "error.invalidValue": "Valeur invalide",
                    "error.database": "Erreur de base de données",
                    "error.network": "Erreur réseau",
                    "error.unknown": "Erreur inconnue"
                }
            }
        """.trimIndent()
    }
}
