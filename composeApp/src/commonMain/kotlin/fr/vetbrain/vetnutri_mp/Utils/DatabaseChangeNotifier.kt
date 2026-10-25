package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Notificateur central des changements de base de données Permet aux ViewModels de s'informer des
 * mises à jour
 */
object DatabaseChangeNotifier {

    // Événements de changement
    enum class ChangeType {
        FOOD_ADDED,
        FOOD_UPDATED,
        FOOD_DELETED,
        FOOD_IMPORTED,
        ANIMAL_ADDED,
        ANIMAL_UPDATED,
        ANIMAL_DELETED,
        ANIMAL_IMPORTED,
        REFERENCE_ADDED,
        REFERENCE_UPDATED,
        REFERENCE_DELETED,
        REFERENCE_IMPORTED,
        CONSULTATION_UPDATED,
        CONSULTATION_DELETED,
        /** Écriture d'une autre donnée sauvegardée (équation, biblio, recette, conseil, mot-clé) */
        DATA_WRITTEN,
        DATABASE_RESET,
        DATABASE_VERSION_UPDATED,
        DATABASE_MIGRATION_FAILED,
        DATABASE_CORRUPTION_DETECTED
    }

    data class DatabaseChangeEvent(
            val type: ChangeType,
            val timestamp: Long = instantNow().toEpochMilliseconds(),
            val details: String? = null,
            val count: Int? = null
    )

    private val _changeEvents = MutableStateFlow<DatabaseChangeEvent?>(null)
    val changeEvents: StateFlow<DatabaseChangeEvent?> = _changeEvents.asStateFlow()

    private val _lastChangeTimestamp = MutableStateFlow(0L)
    val lastChangeTimestamp: StateFlow<Long> = _lastChangeTimestamp.asStateFlow()

    /**
     * Compteur monotone incrémenté à chaque notification : permet de savoir si la base a changé
     * depuis un instant donné (sauvegarde automatique) sans dépendre de l'horloge.
     */
    private val _changeVersion = MutableStateFlow(0L)
    val changeVersion: StateFlow<Long> = _changeVersion.asStateFlow()

    /** Notifie d'un changement dans la base de données */
    fun notifyChange(type: ChangeType, details: String? = null, count: Int? = null) {
        val event = DatabaseChangeEvent(type, instantNow().toEpochMilliseconds(), details, count)
        _changeEvents.value = event
        _lastChangeTimestamp.value = event.timestamp
        _changeVersion.update { it + 1 }
    }

    /** Notifie l'écriture d'une donnée sauvegardée sans type dédié (équation, biblio, recette, conseil…) */
    fun notifyDataWritten(details: String) {
        notifyChange(ChangeType.DATA_WRITTEN, details)
    }

    /** Notifie d'un import d'aliments */
    fun notifyFoodImport(count: Int, source: String) {
        notifyChange(ChangeType.FOOD_IMPORTED, "Import depuis $source", count)
    }

    /** Notifie d'un import d'animaux */
    fun notifyAnimalImport(count: Int, source: String) {
        notifyChange(ChangeType.ANIMAL_IMPORTED, "Import depuis $source", count)
    }

    /** Notifie d'un import de références */
    fun notifyReferenceImport(count: Int, source: String) {
        notifyChange(ChangeType.REFERENCE_IMPORTED, "Import depuis $source", count)
    }

    /** Notifie d'une mise à jour de version */
    fun notifyVersionUpdate(oldVersion: String, newVersion: String) {
        notifyChange(
                ChangeType.DATABASE_VERSION_UPDATED,
                "Mise à jour de $oldVersion vers $newVersion"
        )
    }

    /** Notifie d'une réinitialisation de la base */
    fun notifyDatabaseReset() {
        notifyChange(ChangeType.DATABASE_RESET, "Base de données réinitialisée")
    }

    /** Efface le dernier événement (utile pour éviter les notifications multiples) */
    fun clearLastEvent() {
        _changeEvents.value = null
    }

    /** Vérifie si des changements ont eu lieu depuis un timestamp donné */
    fun hasChangesSince(timestamp: Long): Boolean {
        return _lastChangeTimestamp.value > timestamp
    }
}
