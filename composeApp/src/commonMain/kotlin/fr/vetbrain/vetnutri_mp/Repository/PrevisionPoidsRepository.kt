package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ReglagesPrevisionPoids
import fr.vetbrain.vetnutri_mp.DataBase.WeightForecastLogDao
import fr.vetbrain.vetnutri_mp.DataBase.WeightForecastLogEntity
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Calcul de prévision du poids enregistré (traçabilité locale). */
data class CalculPrevisionJournalise(
        val uuid: String,
        val animalUuid: String,
        val createdAt: Long,
        val modelVersion: String,
        val moduleState: String,
        val inputsJson: String,
        val resultsJson: String
)

/** Réglages du module de prévision du poids et journal local de ses calculs. */
interface PrevisionPoidsRepository {
    suspend fun lireReglages(): ReglagesPrevisionPoids
    suspend fun enregistrerReglages(reglages: ReglagesPrevisionPoids)
    suspend fun journaliser(calcul: CalculPrevisionJournalise)
    suspend fun dernierCalcul(animalUuid: String): CalculPrevisionJournalise?
}

private val jsonReglages = Json { ignoreUnknownKeys = true; encodeDefaults = true }

class DatabasePrevisionPoidsRepository(
        private val dao: WeightForecastLogDao,
        private val stockage: PreferencesStorage
) : PrevisionPoidsRepository {

    override suspend fun lireReglages(): ReglagesPrevisionPoids = withContext(AppDispatchers.IO) {
        val texte = stockage.getString(CLE_REGLAGES, "")
        if (texte.isBlank()) ReglagesPrevisionPoids()
        else try {
            jsonReglages.decodeFromString<ReglagesPrevisionPoids>(texte)
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            AppLog.w("PrevisionPoidsRepository", "Réglages illisibles, module désactivé : ${e.message}")
            ReglagesPrevisionPoids()
        }
    }

    override suspend fun enregistrerReglages(reglages: ReglagesPrevisionPoids) = withContext(AppDispatchers.IO) {
        stockage.saveString(CLE_REGLAGES, jsonReglages.encodeToString(reglages))
    }

    override suspend fun journaliser(calcul: CalculPrevisionJournalise) = withContext(AppDispatchers.IO) {
        dao.insert(
                WeightForecastLogEntity(
                        calcul.uuid, calcul.animalUuid, calcul.createdAt, calcul.modelVersion,
                        calcul.moduleState, calcul.inputsJson, calcul.resultsJson
                )
        )
    }

    override suspend fun dernierCalcul(animalUuid: String): CalculPrevisionJournalise? = withContext(AppDispatchers.IO) {
        dao.getLastForAnimal(animalUuid)?.let {
            CalculPrevisionJournalise(it.uuid, it.animalUuid, it.createdAt, it.modelVersion, it.moduleState, it.inputsJson, it.resultsJson)
        }
    }

    companion object {
        const val CLE_REGLAGES = "prevision_poids_chien_v2"
    }
}

/** Implémentation en mémoire (tests). */
class InMemoryPrevisionPoidsRepository(
        private var reglages: ReglagesPrevisionPoids = ReglagesPrevisionPoids()
) : PrevisionPoidsRepository {
    val calculs = mutableListOf<CalculPrevisionJournalise>()

    override suspend fun lireReglages() = reglages
    override suspend fun enregistrerReglages(reglages: ReglagesPrevisionPoids) {
        this.reglages = reglages
    }
    override suspend fun journaliser(calcul: CalculPrevisionJournalise) {
        calculs += calcul
    }
    override suspend fun dernierCalcul(animalUuid: String) = calculs.lastOrNull { it.animalUuid == animalUuid }
}
