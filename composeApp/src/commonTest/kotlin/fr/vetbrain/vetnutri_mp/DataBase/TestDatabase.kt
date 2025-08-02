package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.NutrientQuantity
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TestAnimalDao : AnimalDao {
    private val animals = mutableMapOf<String, AnimalEntity>()
    private val weights = mutableMapOf<String, List<WeightEntity>>()
    private val consultations = mutableMapOf<String, List<ConsultationEntity>>()
    private val rations = mutableMapOf<String, List<RationEntity>>()
    private val aliments = mutableMapOf<String, List<AlimentRationEntity>>()
    private val supplementalVariables = mutableMapOf<String, List<SupplementalVariableEntity>>()

    override suspend fun insert(animal: AnimalEntity) {
        animals[animal.uuid] = animal
    }

    override suspend fun update(animal: AnimalEntity) {
        animals[animal.uuid] = animal
    }

    override suspend fun delete(animal: AnimalEntity) {
        animals.remove(animal.uuid)
        weights.remove(animal.uuid)
        consultations.remove(animal.uuid)
    }

    override suspend fun getAllAnimals(): List<AnimalEntity> {
        return animals.values.toList()
    }

    override suspend fun getAnimalById(id: String): AnimalEntity? {
        return animals[id]
    }

    override suspend fun insertWeight(weight: WeightEntity) {
        val currentWeights = weights[weight.refAnimal] ?: emptyList()
        weights[weight.refAnimal] = currentWeights + weight
    }

    override suspend fun insertConsultation(consultation: ConsultationEntity) {
        val currentConsultations = consultations[consultation.idAnim] ?: emptyList()
        consultations[consultation.idAnim] = currentConsultations + consultation
    }

    override suspend fun insertRation(ration: RationEntity) {
        val currentRations = rations[ration.idConsult] ?: emptyList()
        rations[ration.idConsult] = currentRations + ration
    }

    override suspend fun insertAlimentRation(aliment: AlimentRationEntity) {
        val currentAliments = aliments[aliment.refRation] ?: emptyList()

        // Vérifier si l'aliment existe déjà pour préserver ses propriétés importantes
        val existingIndex = currentAliments.indexOfFirst { it.uuid == aliment.uuid }
        if (existingIndex != -1) {
            // Mise à jour de l'aliment existant en préservant la quantité
            val existingAliment = currentAliments[existingIndex]
            val updatedAliment = aliment.copy(quantity = existingAliment.quantity)
            aliments[aliment.refRation] =
                    currentAliments.toMutableList().apply {
                        removeAt(existingIndex)
                        add(updatedAliment)
                    }
        } else {
            // Insertion d'un nouvel aliment
            aliments[aliment.refRation] = currentAliments + aliment
        }

    }

    override suspend fun insertSupplementalVariable(
            supplementalVariable: SupplementalVariableEntity
    ) {
        val currentVariables = supplementalVariables[supplementalVariable.idConsult] ?: emptyList()
        supplementalVariables[supplementalVariable.idConsult] =
                currentVariables + supplementalVariable
    }

    override suspend fun getWeightsForAnimal(animalId: String): List<WeightEntity> {
        return weights[animalId] ?: emptyList()
    }

    override suspend fun deleteWeightsForAnimal(animalId: String) {
        weights.remove(animalId)
    }

    override suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEntity> {
        return consultations[animalId] ?: emptyList()
    }

    override suspend fun deleteConsultation(consultation: ConsultationEntity) {
        val currentConsultations = consultations[consultation.idAnim] ?: return
        consultations[consultation.idAnim] =
                currentConsultations.filter { it.uuid != consultation.uuid }
        supplementalVariables.remove(consultation.uuid)
        rations.remove(consultation.uuid)
    }

    override suspend fun deleteSupplementalVariablesForConsultation(consultationId: String) {
        supplementalVariables.remove(consultationId)
    }

    override suspend fun deleteRationsForConsultation(consultationId: String) {
        rations.remove(consultationId)
    }

    // Ajout des méthodes nécessaires pour les tests
    suspend fun getRationsForConsultation(consultationId: String): List<RationEntity> {
        return rations[consultationId] ?: emptyList()
    }

    suspend fun getAlimentRationsForRation(rationId: String): List<AlimentRationEntity> {
        return aliments[rationId] ?: emptyList()
    }
}

class TestConsultationDao : ConsultationDao {
    private val consultations = mutableMapOf<String, ConsultationEntity>()
    private val supplementalVariables = mutableMapOf<String, List<SupplementalVariableEntity>>()
    private val rations = mutableMapOf<String, List<RationEntity>>()
    private val aliments = mutableMapOf<String, List<AlimentRationEntity>>()

    override suspend fun insert(consultation: ConsultationEntity) {
        consultations[consultation.uuid] = consultation
    }

    override suspend fun update(consultation: ConsultationEntity) {
        consultations[consultation.uuid] = consultation
    }

    override suspend fun delete(consultation: ConsultationEntity) {
        consultations.remove(consultation.uuid)
        supplementalVariables.remove(consultation.uuid)
        rations.remove(consultation.uuid)
    }

    override suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEntity> {
        return consultations.values.filter { it.idAnim == animalId }
    }

    override suspend fun getConsultationById(id: String): ConsultationEntity? {
        return consultations[id]
    }

    override suspend fun getSupplementalVariablesForConsultation(
            consultationId: String
    ): List<SupplementalVariableEntity> {
        return supplementalVariables[consultationId] ?: emptyList()
    }

    override suspend fun getRationsForConsultation(consultationId: String): List<RationEntity> {
        return rations[consultationId] ?: emptyList()
    }

    override suspend fun getAlimentsForRation(rationId: String): List<AlimentRationEntity> {
        return aliments[rationId] ?: emptyList()
    }

    override suspend fun insertSupplementalVariable(
            supplementalVariable: SupplementalVariableEntity
    ) {
        val currentVariables = supplementalVariables[supplementalVariable.idConsult] ?: emptyList()
        supplementalVariables[supplementalVariable.idConsult] =
                currentVariables + supplementalVariable
    }

    override suspend fun insertRation(ration: RationEntity) {
        val currentRations = rations[ration.idConsult] ?: emptyList()
        rations[ration.idConsult] = currentRations + ration
    }

    override suspend fun insertAlimentRation(aliment: AlimentRationEntity) {
        val currentAliments = aliments[aliment.refRation] ?: emptyList()

        // Vérifier si l'aliment existe déjà pour préserver ses propriétés importantes
        val existingIndex = currentAliments.indexOfFirst { it.uuid == aliment.uuid }
        if (existingIndex != -1) {
            // Mise à jour de l'aliment existant en préservant la quantité
            val existingAliment = currentAliments[existingIndex]
            val updatedAliment = aliment.copy(quantity = existingAliment.quantity)
            aliments[aliment.refRation] =
                    currentAliments.toMutableList().apply {
                        removeAt(existingIndex)
                        add(updatedAliment)
                    }
        } else {
            // Insertion d'un nouvel aliment
            aliments[aliment.refRation] = currentAliments + aliment
        }

    }

    override suspend fun deleteRationsForConsultation(consultationId: String) {
        rations.remove(consultationId)
    }

    override suspend fun deleteSupplementalVariablesForConsultation(consultationId: String) {
        supplementalVariables.remove(consultationId)
    }
}

class TestFoodDao : FoodDao {
    private val foods = mutableMapOf<String, FoodEntity>()
    private val indications = mutableMapOf<String, List<IndicationAlimentEntity>>()

    override suspend fun insert(food: FoodEntity) {
        foods[food.uuid] = food
    }

    override suspend fun update(food: FoodEntity) {
        foods[food.uuid] = food
    }

    override suspend fun delete(food: FoodEntity) {
        foods.remove(food.uuid)
    }

    override suspend fun findAll(): List<FoodEntity> {
        return foods.values.toList()
    }

    override suspend fun getFoodById(id: String): FoodEntity? {
        return foods[id]
    }

    override suspend fun insertFood(food: FoodEntity) {
        insert(food)
    }

    override suspend fun updateFood(food: FoodEntity) {
        update(food)
    }

    override suspend fun deleteFood(uuid: String) {
        foods.remove(uuid)
    }

    override suspend fun deleteAllFoods() {
        foods.clear()
    }

    override suspend fun getFood(uuid: String): FoodEntity? {
        return getFoodById(uuid)
    }

    override suspend fun getAllFoods(): List<FoodEntity> {
        return findAll()
    }

    override suspend fun insertIndications(indications: List<IndicationAlimentEntity>) {
        indications.forEach { indication ->
            val currentIndications = this.indications[indication.refAliment] ?: emptyList()
            this.indications[indication.refAliment] = currentIndications + indication
        }
    }

    override suspend fun deleteIndicationsForAliment(alimentUuid: String) {
        indications.remove(alimentUuid)
    }

    override suspend fun getIndicationsForAliment(
            alimentUuid: String
    ): List<IndicationAlimentEntity> {
        return indications[alimentUuid] ?: emptyList()
    }
}

class TestNutrientValueDao : NutrientValueDao {
    private val nutrientValues = mutableMapOf<String, List<NutrientValueEntity>>()

    override suspend fun getNutrientValues(alimentUuid: String): List<NutrientValueEntity> {
        return nutrientValues[alimentUuid] ?: emptyList()
    }

    override suspend fun insertNutrientValues(values: List<NutrientValueEntity>) {
        values.forEach { value ->
            val currentValues = nutrientValues[value.refAliment] ?: emptyList()
            nutrientValues[value.refAliment] = currentValues + value
        }
    }

    override suspend fun deleteNutrientValues(values: List<NutrientValueEntity>) {
        values.forEach { value ->
            val currentValues =
                    nutrientValues[value.refAliment]?.filter {
                        it.nutrientLabel != value.nutrientLabel
                    }
                            ?: emptyList()
            if (currentValues.isEmpty()) {
                nutrientValues.remove(value.refAliment)
            } else {
                nutrientValues[value.refAliment] = currentValues
            }
        }
    }

    override suspend fun deleteAllNutrientValuesForAliment(alimentUuid: String) {
        nutrientValues.remove(alimentUuid)
    }
}

class TestFoodRepository(
        private val foodDao: TestFoodDao,
        private val nutrientValueDao: TestNutrientValueDao
) : FoodRepository {
    private val _foodsFlow = MutableStateFlow<List<AlimentEv>>(emptyList())
    val foodsFlow: StateFlow<List<AlimentEv>> = _foodsFlow.asStateFlow()

    override suspend fun insert(food: AlimentEv) {
        val foodEntity = TestMappers.toFoodEntity(food)
        foodDao.insert(foodEntity)

        // Insérer les valeurs nutritionnelles
        if (food.valMap.isNotEmpty()) {
            val nutrientValues =
                    food.valMap.map { (nutrient, quantity) ->
                        NutrientValueEntity(
                                refAliment = food.uuid,
                                nutrientLabel = nutrient.label,
                                value = quantity.value
                        )
                    }
            nutrientValueDao.insertNutrientValues(nutrientValues)
        }
    }

    override suspend fun update(food: AlimentEv) {
        val foodEntity = TestMappers.toFoodEntity(food)
        foodDao.update(foodEntity)

        // Mettre à jour les valeurs nutritionnelles
        nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)
        if (food.valMap.isNotEmpty()) {
            val nutrientValues =
                    food.valMap.map { (nutrient, quantity) ->
                        NutrientValueEntity(
                                refAliment = food.uuid,
                                nutrientLabel = nutrient.label,
                                value = quantity.value
                        )
                    }
            nutrientValueDao.insertNutrientValues(nutrientValues)
        }
    }

    override suspend fun delete(food: AlimentEv) {
        val foodEntity = TestMappers.toFoodEntity(food)
        foodDao.delete(foodEntity)
        nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)
    }

    override suspend fun getAllFoods(): List<AlimentEv> {
        return foodDao.getAllFoods().map { foodEntity ->
            val nutrientValues = nutrientValueDao.getNutrientValues(foodEntity.uuid)
            TestMappers.toAlimentEv(foodEntity, nutrientValues = nutrientValues)
        }
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return foodDao.getFoodById(id)?.let { foodEntity ->
            val nutrientValues = nutrientValueDao.getNutrientValues(foodEntity.uuid)
            TestMappers.toAlimentEv(foodEntity, nutrientValues = nutrientValues)
        }
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        return flow { emit(getAllFoods()) }
    }

    override suspend fun importFoods(foods: List<AlimentEvJson>): FoodImportResult {
        var count = 0
        // Implémentation simplifiée pour les tests
        return FoodImportResult(
                importedCount = count,
                updatedCount = 0,
                deletedCount = 0,
                errorCount = 0,
                totalCount = count,
                nonResolvedNutrientsCount = 0
        )
    }

    override suspend fun insertFood(food: AlimentEv) {
        insert(food)
    }

    override suspend fun getFood(uuid: String): AlimentEv? {
        return getFoodById(uuid)
    }

    override suspend fun deleteFood(uuid: String) {
        foodDao.deleteFood(uuid)
        nutrientValueDao.deleteAllNutrientValuesForAliment(uuid)
    }

    override suspend fun updateFood(food: AlimentEv) {
        update(food)
    }

    /**
     * Supprime tous les aliments de la base de données.
     * @return Le nombre d'aliments supprimés
     */
    override suspend fun clearAllFoods(): Int {
        val allFoods = foodDao.getAllFoods()
        val count = allFoods.size

        // Supprimer les valeurs nutritionnelles
        allFoods.forEach { food -> nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid) }

        // Supprimer tous les aliments
        foodDao.deleteAllFoods()

        return count
    }
}

object TestMappers {
    fun toFoodEntity(food: AlimentEv): FoodEntity {
        return FoodEntity(
                uuid = food.uuid,
                groupAlim = food.group?.ordinal ?: 0,
                typeAlim = food.typeAliment?.ordinal ?: 0,
                ingredients = food.ingredients ?: "",
                price = food.price ?: 0.0,
                categPrice = food.categPrice ?: "",
                brand = food.brand ?: "",
                gamme = food.gamme ?: "",
                cont = food.cont?.name ?: "NO",
                unitPres = 0,
                quantityPres = food.quantInt ?: 0f,
                version = 1,
                date = "",
                nameDef = food.nom ?: "",
                consistent = if (food.consistent) 1 else 0,
                deprecated = if (food.deprecated) 1 else 0,
                DataB = food.dataB ?: "",
                RefRation = food.rationUUID,
                RefAlimUnif = "",
                especesJson =
                        if (food.especes.isNotEmpty()) Json.encodeToString(food.especes) else null,
                indicationsJson =
                        if (food.indicat.isNotEmpty())
                                Json.encodeToString(food.indicat.map { it.name })
                        else null,
                name = food.nom,
                quantite = food.quantInt ?: 0f
        )
    }

    fun toAlimentEv(
            foodEntity: FoodEntity,
            nutrientValues: List<NutrientValueEntity> = emptyList()
    ): AlimentEv {
        // Convertir les espèces
        val especesList = mutableListOf<String>()
        if (!foodEntity.especesJson.isNullOrEmpty()) {
            try {
                especesList.addAll(Json.decodeFromString<List<String>>(foodEntity.especesJson))
            } catch (e: Exception) {
                // En cas d'erreur de décodage, laisser la liste vide
            }
        }

        // Convertir les indications
        val indicatList = mutableListOf<AlimIndic>()
        if (!foodEntity.indicationsJson.isNullOrEmpty()) {
            try {
                val indicationNames =
                        Json.decodeFromString<List<String>>(foodEntity.indicationsJson)
                indicatList.addAll(
                        indicationNames.mapNotNull { name ->
                            try {
                                AlimIndic.valueOf(name)
                            } catch (e: Exception) {
                                null
                            }
                        }
                )
            } catch (e: Exception) {
                // En cas d'erreur de décodage, laisser la liste vide
            }
        }

        // Convertir les valeurs nutritionnelles
        val nutrientMap = mutableMapOf<Nutrient, NutrientQuantity>()
        nutrientValues.forEach { nutrientValue ->
            val nutrient = NutrientResolver.AllNutrientResolver(nutrientValue.nutrientLabel)
            if (nutrient != null) {
                nutrientMap[nutrient] = NutrientQuantity(nutrientValue.value, nutrient.label)
            }
        }

        return AlimentEv(
                uuid = foodEntity.uuid,
                nom = foodEntity.nameDef,
                group = GroupAlim.values().getOrNull(foodEntity.groupAlim),
                typeAliment = FoodKind.values().getOrNull(foodEntity.typeAlim),
                ingredients = foodEntity.ingredients,
                price = foodEntity.price,
                categPrice = foodEntity.categPrice,
                brand = foodEntity.brand,
                gamme = foodEntity.gamme,
                consistent = foodEntity.consistent == 1,
                cont = ContEnum.getByName(foodEntity.cont),
                quantInt = foodEntity.quantityPres,
                deprecated = foodEntity.deprecated == 1,
                dataB = foodEntity.DataB,
                especes = especesList.toMutableList(),
                indicat = indicatList.toMutableList(),
                valMap = nutrientMap.toMutableMap(),
                rationUUID = foodEntity.RefRation
        )
    }
}
