package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.WeightDate
import fr.vetbrain.vetnutri_mp.DataBase.AnimalDao
import fr.vetbrain.vetnutri_mp.DataBase.FoodDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Implémentation de test du repository d'animaux qui garantit que les consultations sont
 * sauvegardées correctement pour les tests.
 */
class TestDatabaseAnimalRepository(private val animalDao: AnimalDao, private val foodDao: FoodDao) :
        AnimalRepository {

    override suspend fun saveAnimal(animal: AnimalEv) {
        withContext(AppDispatchers.Default) {
            // Insérer l'animal
            animalDao.insert(animal.toEntity())

            // Sauvegarder les consultations
            animal.consultations.forEach { consultation ->
                // S'assurer que l'ID de l'animal est correctement défini
                consultation.idAnim = animal.uuid
                // Insérer la consultation
                animalDao.insertConsultation(consultation.toEntity())
            }

            // Sauvegarder les poids
            animal.weightHistory.forEach { weight ->
                // S'assurer que la référence de l'animal est correctement définie
                weight.refAnimal = animal.uuid
                // Insérer le poids
                animalDao.insertWeight(weight.toEntity())
            }
        }
    }

    override suspend fun getAllAnimals(): List<AnimalEv> {
        return withContext(AppDispatchers.Default) {
            val animals =
                    animalDao.getAllAnimals().map { entity ->
                        val animalEv =
                                AnimalEv(
                                        uuid = entity.uuid,
                                        nom = entity.nom ?: "",
                                        dead = entity.dead ?: false,
                                        id = entity.id,
                                        sexId = entity.sexId ?: 0,
                                        specieId = entity.specieId ?: "",
                                        ownerName = entity.ownerName ?: "",
                                        birthdate =
                                                entity.birthdate?.takeIf { it.isNotEmpty() }?.let {
                                                    LocalDate.parse(it)
                                                },
                                        race = entity.race ?: "",
                                        summary = entity.summary ?: ""
                                )

                        // Charger les consultations associées
                        val consultations = animalDao.getConsultationsForAnimal(entity.uuid)
                        if (consultations.isNotEmpty()) {
                            animalEv.consultations.addAll(
                                    consultations.map { consultationEntity ->
                                        ConsultationEv(
                                                uuid = consultationEntity.uuid,
                                                idAnim = consultationEntity.idAnim,
                                                date =
                                                        consultationEntity.date
                                                                ?.takeIf { it.isNotEmpty() }
                                                                ?.let { LocalDate.parse(it) },
                                                objectConsult = consultationEntity.objectConsult
                                                                ?: "",
                                                observation = consultationEntity.observation ?: "",
                                                cRendu = consultationEntity.cRendu ?: "",
                                                weight = consultationEntity.weight,
                                                idealWeight = consultationEntity.idealWeight,
                                                water = consultationEntity.water,
                                                bodyFat = consultationEntity.bodyFat,
                                                methodAnalysis = consultationEntity.methodAnalysis
                                                                ?: "",
                                                BCS = consultationEntity.BCS,
                                                k1Id = consultationEntity.k1Id ?: "",
                                                k1Value = consultationEntity.k1Value,
                                                k2Id = consultationEntity.k2Id ?: "",
                                                k2Value = consultationEntity.k2Value,
                                                k3Id = consultationEntity.k3Id ?: "",
                                                k3Value = consultationEntity.k3Value,
                                                k4Id = consultationEntity.k4Id ?: "",
                                                k4Value = consultationEntity.k4Value,
                                                k5Id = consultationEntity.k5Id ?: "",
                                                k5Value = consultationEntity.k5Value,
                                                nLittle = consultationEntity.nLittle,
                                                pAdult = consultationEntity.pAdult,
                                                coefGes = consultationEntity.coefGes,
                                                coefLact = consultationEntity.coefLact,
                                                MCS = consultationEntity.MCS
                                        )
                                    }
                            )
                        }

                        // Charger l'historique des poids
                        val weights = animalDao.getWeightsForAnimal(entity.uuid)
                        if (weights.isNotEmpty()) {
                            animalEv.weightHistory.addAll(
                                    weights.map { weightEntity ->
                                        WeightDate(
                                                uuid = weightEntity.uuid,
                                                refAnimal = weightEntity.refAnimal,
                                                date = LocalDate.parse(weightEntity.date),
                                                value = weightEntity.value
                                        )
                                    }
                            )
                        }

                        animalEv
                    }
            animals
        }
    }

    override suspend fun deleteAnimal(animal: AnimalEv) {
        withContext(AppDispatchers.Default) {
            animalDao.delete(animal.toEntity(includeRelations = false))
        }
    }

    override suspend fun updateAnimal(animal: AnimalEv) {
        withContext(AppDispatchers.Default) {
            animalDao.update(animal.toEntity(includeRelations = false))
        }
    }

    override suspend fun getAnimalById(id: String): AnimalEv? {
        return withContext(AppDispatchers.Default) {
            val entity = animalDao.getAnimalById(id) ?: return@withContext null

            // Convertir l'entité en objet de domaine
            val animalEv =
                    AnimalEv(
                            uuid = entity.uuid,
                            nom = entity.nom ?: "",
                            dead = entity.dead ?: false,
                            id = entity.id,
                            sexId = entity.sexId ?: 0,
                            specieId = entity.specieId ?: "",
                            ownerName = entity.ownerName ?: "",
                            birthdate =
                                    entity.birthdate?.takeIf { it.isNotEmpty() }?.let {
                                        LocalDate.parse(it)
                                    },
                            race = entity.race ?: "",
                            summary = entity.summary ?: ""
                    )

            // Charger les consultations associées
            val consultations = animalDao.getConsultationsForAnimal(entity.uuid)
            if (consultations.isNotEmpty()) {
                animalEv.consultations.addAll(
                        consultations.map { consultationEntity ->
                            ConsultationEv(
                                    uuid = consultationEntity.uuid,
                                    idAnim = consultationEntity.idAnim,
                                    date =
                                            consultationEntity.date
                                                    ?.takeIf { it.isNotEmpty() }
                                                    ?.let { LocalDate.parse(it) },
                                    objectConsult = consultationEntity.objectConsult ?: "",
                                    observation = consultationEntity.observation ?: "",
                                    cRendu = consultationEntity.cRendu ?: "",
                                    weight = consultationEntity.weight,
                                    idealWeight = consultationEntity.idealWeight,
                                    water = consultationEntity.water,
                                    bodyFat = consultationEntity.bodyFat,
                                    methodAnalysis = consultationEntity.methodAnalysis ?: "",
                                    BCS = consultationEntity.BCS,
                                    k1Id = consultationEntity.k1Id ?: "",
                                    k1Value = consultationEntity.k1Value,
                                    k2Id = consultationEntity.k2Id ?: "",
                                    k2Value = consultationEntity.k2Value,
                                    k3Id = consultationEntity.k3Id ?: "",
                                    k3Value = consultationEntity.k3Value,
                                    k4Id = consultationEntity.k4Id ?: "",
                                    k4Value = consultationEntity.k4Value,
                                    k5Id = consultationEntity.k5Id ?: "",
                                    k5Value = consultationEntity.k5Value,
                                    nLittle = consultationEntity.nLittle,
                                    pAdult = consultationEntity.pAdult,
                                    coefGes = consultationEntity.coefGes,
                                    coefLact = consultationEntity.coefLact,
                                    MCS = consultationEntity.MCS
                            )
                        }
                )
            }

            // Charger l'historique des poids
            val weights = animalDao.getWeightsForAnimal(entity.uuid)
            if (weights.isNotEmpty()) {
                animalEv.weightHistory.addAll(
                        weights.map { weightEntity ->
                            WeightDate(
                                    uuid = weightEntity.uuid,
                                    refAnimal = weightEntity.refAnimal,
                                    date = LocalDate.parse(weightEntity.date),
                                    value = weightEntity.value
                            )
                        }
                )
            }

            animalEv
        }
    }

    override suspend fun importAnimals(animalsJson: List<AnimalEvJson>): AnimalImportResult {
        // Non implémenté pour les tests
        return AnimalImportResult(
                importedCount = 0,
                updatedCount = 0,
                errorCount = 0,
                totalCount = 0,
                foodsImportedCount = 0
        )
    }

    override fun getFoodRepository(): FoodRepository? {
        // Non implémenté pour les tests
        return null
    }
}
