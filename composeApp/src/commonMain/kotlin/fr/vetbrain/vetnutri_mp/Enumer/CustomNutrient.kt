package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable
import fr.vetbrain.vetnutri_mp.Utils.SynchronizedLock

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

/**
 * Registre global des nutriments personnalisés. Écrit depuis le dispatcher IO (préchargement,
 * import) et lu depuis Main : tous les accès sont protégés par [lock].
 */
object CustomNutrientRegistry {
    private val lock = SynchronizedLock()
    private val nutrientsByLabel = linkedMapOf<String, CustomNutrient>()
    private var versionCourante: Int = 0

    /**
     * Compteur incrémenté à chaque modification du registre. Permet aux caches dérivés
     * (ex. `NutrientResolver.labelToNutrient`) de se reconstruire quand le registre change.
     */
    val version: Int
        get() = lock.withLock { versionCourante }

    fun register(nutrient: CustomNutrient): CustomNutrient {
        lock.withLock {
            val precedent = nutrientsByLabel.put(nutrient.label, nutrient)
            if (precedent != nutrient) versionCourante++
        }
        return nutrient
    }

    fun registerFromRaw(name: String, unit: String = "g"): CustomNutrient {
        val nutrient = CustomNutrient.fromRaw(name, unit)
        return register(nutrient)
    }

    fun getByLabel(label: String): CustomNutrient? =
        lock.withLock { nutrientsByLabel[label.uppercase().trim()] }

    fun all(): List<CustomNutrient> = lock.withLock { nutrientsByLabel.values.toList() }

    fun removeByLabel(label: String): Boolean {
        return lock.withLock {
            val supprime = nutrientsByLabel.remove(label.uppercase().trim()) != null
            if (supprime) versionCourante++
            supprime
        }
    }
}
