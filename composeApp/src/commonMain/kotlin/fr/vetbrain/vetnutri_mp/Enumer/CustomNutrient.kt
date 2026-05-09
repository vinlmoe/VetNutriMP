package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/**
 * Nutriment personnalisable créé par l'utilisateur et réutilisable.
 * L'identité fonctionnelle est portée par le label normalisé.
 */
data class CustomNutrient(
    private val displayName: String,
    override val label: String,
    override val unite: String,
    override val ue: UnitEnum = UnitEnum.BUg,
    override val coef: Int = 0,
    private val category: MainNutrientEnum = MainNutrientEnum.OTHER
) : Nutrient, Labelable {

    override fun getMNE(): MainNutrientEnum = category

    fun nameToString(): String = displayName

    companion object {
        fun fromRaw(
            name: String,
            unit: String = "g",
            category: MainNutrientEnum = MainNutrientEnum.OTHER
        ): CustomNutrient {
            val cleanedName = name.trim().ifBlank { "Nutriment personnalisé" }
            val normalizedLabel = normalizeLabel(cleanedName)
            val resolvedUnit = normalizeUnit(unit)
            val resolvedUnitEnum = UnitEnum.fromDisplayName(resolvedUnit)
            return CustomNutrient(
                displayName = cleanedName,
                label = normalizedLabel,
                unite = resolvedUnit,
                ue = if (resolvedUnitEnum == UnitEnum.NO && resolvedUnit.isNotBlank()) UnitEnum.BUg else resolvedUnitEnum,
                category = category
            )
        }

        fun fromLabel(label: String): CustomNutrient {
            val cleaned = label.trim()
            val normalizedLabel = normalizeLabel(cleaned)
            return CustomNutrient(
                displayName = cleaned.ifBlank { normalizedLabel },
                label = normalizedLabel,
                unite = "g",
                ue = UnitEnum.BUg,
                category = MainNutrientEnum.OTHER
            )
        }

        private fun normalizeLabel(raw: String): String {
            return raw
                .trim()
                .uppercase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace(Regex("[^A-Z0-9_]"), "")
                .ifBlank { "CUSTOM_NUTRIENT" }
        }

        private fun normalizeUnit(raw: String): String {
            val trimmed = raw.trim()
            return when {
                trimmed.equals("mg", ignoreCase = true) -> "mg"
                trimmed.equals("µg", ignoreCase = true) || trimmed.equals("ug", ignoreCase = true) -> "µg"
                trimmed.equals("kcal", ignoreCase = true) -> "kcal"
                trimmed.isBlank() -> "g"
                else -> trimmed
            }
        }
    }
}

object CustomNutrientRegistry {
    private val nutrientsByLabel = linkedMapOf<String, CustomNutrient>()

    fun register(nutrient: CustomNutrient): CustomNutrient {
        nutrientsByLabel[nutrient.label] = nutrient
        return nutrient
    }

    fun registerFromRaw(name: String, unit: String = "g"): CustomNutrient {
        val nutrient = CustomNutrient.fromRaw(name, unit)
        return register(nutrient)
    }

    fun getByLabel(label: String): CustomNutrient? = nutrientsByLabel[label.uppercase().trim()]

    fun all(): List<CustomNutrient> = nutrientsByLabel.values.toList()
}
