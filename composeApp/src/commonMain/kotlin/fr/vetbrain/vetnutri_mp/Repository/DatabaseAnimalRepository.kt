package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.Data.WeightDate
import fr.vetbrain.vetnutri_mp.DataBase.AnimalDao
import fr.vetbrain.vetnutri_mp.DataBase.FoodDao
import fr.vetbrain.vetnutri_mp.DataBase.FoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueDao
import fr.vetbrain.vetnutri_mp.DataBase.SupplementalVariableEntity
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class DatabaseAnimalRepository(
        private val animalDao: AnimalDao,
        private val foodDao: FoodDao,
        private val nutrientValueDao: NutrientValueDao? = null
) : AnimalRepository {
        override suspend fun saveAnimal(animal: AnimalEv) {
                withContext(AppDispatchers.Default) {
                        // Sauvegarder l'animal
                        animalDao.insert(animal.toEntity())

                        // Sauvegarder les consultations
                        animal.consultations.forEach { consultation ->
                                // S'assurer que l'ID de l'animal est correctement défini
                                consultation.idAnim = animal.uuid
                                // Insérer la consultation
                                animalDao.insertConsultation(
                                        consultation.toEntity(includeRelations = false)
                                )
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
                        println("🔍 DEBUG DatabaseAnimalRepository: getAllAnimals() appelé")
                        val entities = animalDao.getAllAnimals()
                        println(
                                "🔍 DEBUG DatabaseAnimalRepository: ${entities.size} animaux trouvés en base"
                        )

                        entities.map { entity ->
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
                                                        entity.birthdate?.let {
                                                                LocalDate.parse(it)
                                                        },
                                                race = entity.race ?: "",
                                                summary = entity.summary ?: ""
                                        )

                                // Charger les poids pour chaque animal
                                val weightEntities = animalDao.getWeightsForAnimal(entity.uuid)
                                if (weightEntities.isNotEmpty()) {
                                        animalEv.weightHistory.addAll(
                                                weightEntities.map { weightEntity ->
                                                        WeightDate(
                                                                uuid = weightEntity.uuid,
                                                                refAnimal = weightEntity.refAnimal,
                                                                date =
                                                                        LocalDate.parse(
                                                                                weightEntity.date
                                                                        ),
                                                                value = weightEntity.value
                                                        )
                                                }
                                        )
                                        println(
                                                "🔍 DEBUG DatabaseAnimalRepository: ${weightEntities.size} poids chargés pour ${animalEv.nom}"
                                        )
                                } else {
                                        println(
                                                "🔍 DEBUG DatabaseAnimalRepository: Aucun poids pour ${animalEv.nom}"
                                        )
                                }

                                animalEv
                        }
                }
        }

        override suspend fun deleteAnimal(animal: AnimalEv) {
                withContext(AppDispatchers.Default) {
                        animalDao.delete(animal.toEntity(includeRelations = false))
                }
        }

        override suspend fun updateAnimal(animal: AnimalEv) {
                withContext(AppDispatchers.Default) {

                        // Vérifier si l'animal existe avant la mise à jour
                        val existingAnimal = animalDao.getAnimalById(animal.uuid)
                        if (existingAnimal != null) {} else {}

                        // Convertir l'animal en entité et le mettre à jour
                        val animalEntity = animal.toEntity(includeRelations = false)

                        animalDao.update(animalEntity)

                        // Supprimer tous les poids existants pour cet animal
                        animalDao.deleteWeightsForAnimal(animal.uuid)

                        // Sauvegarder les nouveaux poids
                        animal.weightHistory.forEach { weight ->
                                // S'assurer que la référence de l'animal est correctement définie
                                weight.refAnimal = animal.uuid
                                // Insérer le poids
                                animalDao.insertWeight(weight.toEntity())
                        }

                        // Vérifier que l'animal a été correctement mis à jour
                        val updatedAnimal = animalDao.getAnimalById(animal.uuid)
                        if (updatedAnimal != null) {} else {}
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
                                        birthdate = entity.birthdate?.let { LocalDate.parse(it) },
                                        race = entity.race ?: "",
                                        summary = entity.summary ?: ""
                                )

                        // Charger les consultations associées
                        val consultationEntities = animalDao.getConsultationsForAnimal(id)
                        if (consultationEntities.isNotEmpty()) {
                                animalEv.consultations.addAll(
                                        consultationEntities.map { consultEntity ->
                                                ConsultationEv(
                                                        uuid = consultEntity.uuid,
                                                        idAnim = consultEntity.idAnim,
                                                        date =
                                                                consultEntity.date?.let {
                                                                        LocalDate.parse(it)
                                                                },
                                                        objectConsult = consultEntity.objectConsult
                                                                        ?: "",
                                                        observation = consultEntity.observation
                                                                        ?: "",
                                                        cRendu = consultEntity.cRendu ?: "",
                                                        weight = consultEntity.weight,
                                                        idealWeight = consultEntity.idealWeight,
                                                        water = consultEntity.water,
                                                        bodyFat = consultEntity.bodyFat,
                                                        methodAnalysis =
                                                                consultEntity.methodAnalysis ?: "",
                                                        BCS = consultEntity.BCS,
                                                        k1Id = consultEntity.k1Id ?: "",
                                                        k1Value = consultEntity.k1Value,
                                                        k2Id = consultEntity.k2Id ?: "",
                                                        k2Value = consultEntity.k2Value,
                                                        k3Id = consultEntity.k3Id ?: "",
                                                        k3Value = consultEntity.k3Value,
                                                        k4Id = consultEntity.k4Id ?: "",
                                                        k4Value = consultEntity.k4Value,
                                                        k5Id = consultEntity.k5Id ?: "",
                                                        k5Value = consultEntity.k5Value,
                                                        nLittle = consultEntity.nLittle,
                                                        pAdult = consultEntity.pAdult,
                                                        coefGes = consultEntity.coefGes,
                                                        coefLact = consultEntity.coefLact,
                                                        MCS = consultEntity.MCS
                                                )
                                        }
                                )
                        }

                        // Charger les poids associés
                        val weightEntities = animalDao.getWeightsForAnimal(id)
                        println(
                                "🔍 DEBUG DatabaseAnimalRepository: Chargement des poids pour l'animal $id"
                        )
                        println(
                                "🔍 DEBUG DatabaseAnimalRepository: ${weightEntities.size} poids trouvés en base"
                        )

                        if (weightEntities.isNotEmpty()) {
                                animalEv.weightHistory.addAll(
                                        weightEntities.map { weightEntity ->
                                                WeightDate(
                                                        uuid = weightEntity.uuid,
                                                        refAnimal = weightEntity.refAnimal,
                                                        date = LocalDate.parse(weightEntity.date),
                                                        value = weightEntity.value
                                                )
                                        }
                                )

                                // DEBUG: Afficher les poids chargés
                                animalEv.weightHistory.forEachIndexed { index, weight ->
                                        println(
                                                "🔍 DEBUG DatabaseAnimalRepository: Poids $index chargé - Date: ${weight.date}, Valeur: ${weight.value}kg"
                                        )
                                }
                        } else {
                                println(
                                        "🔍 DEBUG DatabaseAnimalRepository: Aucun poids trouvé pour l'animal $id"
                                )
                        }

                        animalEv
                }
        }

        /**
         * Convertit un identifiant d'espèce en nom d'énumération.
         *
         * @param especeId L'identifiant d'espèce à convertir (peut être une chaîne ou un nombre)
         * @return Le nom de l'énumération Espece correspondante ou l'identifiant original si non
         * reconnu
         */
        private fun convertSpecieId(especeId: String): String {
                // Journal détaillé pour l'import JSON

                // Si l'entrée est vide ou null, conserver la valeur vide
                if (especeId.isBlank()) {
                        return ""
                }

                // Vérifier directement si l'especeId correspond à l'id d'une des espèces de
                // l'énumération
                val especeDirectMatch = Espece.entries.find { it.id == especeId }
                if (especeDirectMatch != null) {
                        return especeDirectMatch.label
                }

                // Essayer de convertir en entier si c'est un nombre
                val especeNumId = especeId.toIntOrNull()

                return if (especeNumId != null) {
                        // Vérifier si l'ID numérique correspond à l'ID d'une espèce
                        val especeByNumId = Espece.entries.find { it.id == especeNumId.toString() }
                        if (especeByNumId != null) {
                                especeByNumId.label
                        } else {
                                // Si c'est un nombre, utiliser getEnumFromInt qui utilise le champ
                                // catégorie
                                try {
                                        val espece = Espece.getEnumFromInt(especeNumId)
                                        espece.label
                                } catch (e: Exception) {
                                        // Essayer les autres stratégies
                                        try {
                                                val especeByName =
                                                        Espece.valueOf(especeId.uppercase())
                                                especeByName.label
                                        } catch (e2: Exception) {
                                                // Essayer par label
                                                val especeByLabel = Espece.getByLabel(especeId)
                                                if (especeByLabel != null) {
                                                        especeByLabel.label
                                                } else {
                                                        // En cas d'échec, conserver l'ID original
                                                        especeId
                                                }
                                        }
                                }
                        }
                } else {
                        // Si c'est une chaîne, essayer d'abord par label
                        val especeByLabel = Espece.getByLabel(especeId)
                        if (especeByLabel != null) {
                                return especeByLabel.label
                        }

                        // Essayer par nom d'énumération
                        try {
                                val especeEnum = Espece.valueOf(especeId.uppercase())
                                especeEnum.label
                        } catch (e: Exception) {
                                // En cas d'échec, conserver la valeur originale
                                especeId
                        }
                }
        }

        override suspend fun importAnimals(animalsJson: List<AnimalEvJson>): AnimalImportResult {
                return withContext(AppDispatchers.Default) {
                        val availableFoodUUIDs = mutableSetOf<String>()
                        // Map pour stocker les noms des aliments par UUID
                        val foodNamesMap = mutableMapOf<String, String>()

                        // Récupérer les aliments existants dans la base de données
                        foodDao.getAllFoods().forEach { food ->
                                availableFoodUUIDs.add(food.uuid)
                                // Stocker les noms des aliments déjà présents dans la base
                                food.name?.let { name -> foodNamesMap[food.uuid] = name }
                        }

                        // Première passe : extraire tous les aliments uniques des rations
                        // et stocker leurs noms dans la map
                        val alimToImport = mutableMapOf<String, AlimentEvJson>()

                        // Parcourir tous les animaux pour collecter les noms des aliments
                        // même s'ils ne sont pas importés directement
                        animalsJson.forEach { animalJson ->
                                // Format 1: consultations directement dans l'objet animal
                                val consultations =
                                        animalJson.consultations ?: // Format 2: consultations dans
                                                // list.consultations
                                                animalJson.list?.consultations
                                                        ?: // Aucune consultation
                                                emptyList()

                                consultations.forEach { consultJson ->
                                        consultJson.rationList.values.forEach { rationJson ->
                                                rationJson.alimentList.forEach { alimentJson ->
                                                        // Stocker tous les noms d'aliments dans la
                                                        // map, même si l'aliment
                                                        // existe déjà dans la base de données
                                                        if (alimentJson.UUIDunif != null &&
                                                                        alimentJson.alime != null
                                                        ) {
                                                                val nomAliment =
                                                                        alimentJson.alime.nom
                                                                                ?: "Sans nom"
                                                                foodNamesMap[alimentJson.UUIDunif] =
                                                                        nomAliment

                                                                // Si l'aliment n'existe pas dans la
                                                                // base, l'ajouter à la liste à
                                                                // importer
                                                                if (!availableFoodUUIDs.contains(
                                                                                alimentJson.UUIDunif
                                                                        )
                                                                ) {
                                                                        alimToImport[
                                                                                alimentJson
                                                                                        .UUIDunif] =
                                                                                alimentJson.alime
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }

                        // Deuxième passe : importer les aliments extraits
                        var importedFoodsCount = 0

                        for ((uuid, aliment) in alimToImport) {
                                try {
                                        // Créer un FoodEntity à partir de l'AlimentEvJson
                                        val foodEntity =
                                                FoodEntity(
                                                        uuid = uuid,
                                                        groupAlim = 0, // Par défaut
                                                        typeAlim = 0, // Par défaut
                                                        ingredients = aliment.ingredients ?: "",
                                                        price = aliment.prix ?: 0.0,
                                                        categPrice = aliment.categoriePrix ?: "",
                                                        brand = aliment.marque ?: "",
                                                        gamme = aliment.gamme ?: "",
                                                        unitPres = 0, // Par défaut
                                                        quantityPres = aliment.quantInt ?: 0.0,
                                                        version = 1, // Par défaut
                                                        date = "", // Par défaut
                                                        nameDef = aliment.nom ?: "",
                                                        consistent = 1, // Par défaut
                                                        deprecated =
                                                                if (aliment.deprecated == true) 1
                                                                else 0,
                                                        DataB = aliment.DataB ?: "",
                                                        RefRation = "",
                                                        RefAlimUnif = "",
                                                        cont = "NO", // Valeur par défaut
                                                        name = aliment.nom ?: "",
                                                        quantite = 0.0,
                                                        especesJson = "[]",
                                                        indicationsJson = "[]"
                                                )

                                        // Insérer l'aliment dans la base de données
                                        foodDao.insert(foodEntity)
                                        availableFoodUUIDs.add(uuid)
                                        importedFoodsCount++
                                } catch (e: Exception) {
                                        e.printStackTrace()
                                }
                        }

                        var importedCount = 0
                        var updatedCount = 0
                        var errorCount = 0
                        var rationsWithAliments = 0
                        var totalAlimentsInRations = 0

                        // Troisième passe : importer les animaux avec leurs relations
                        for (animalJson in animalsJson) {
                                try {
                                        // Créer un nouvel animal à partir des données JSON
                                        val animal =
                                                AnimalEv(
                                                        uuid = animalJson.UUID,
                                                        nom = animalJson.nom,
                                                        dead = animalJson.dead,
                                                        id = animalJson.id,
                                                        sexId = animalJson.sex,
                                                        specieId =
                                                                convertSpecieId(animalJson.espece),
                                                        ownerName = animalJson.nomProprio,
                                                        birthdate = animalJson.dateNaiss,
                                                        race = animalJson.race,
                                                        summary = animalJson.resume
                                                )

                                        // Ajouter les poids à l'historique
                                        animal.weightHistory =
                                                animalJson
                                                        .listWeight
                                                        .map { weightJson ->
                                                                WeightDate(
                                                                        uuid = weightJson.UUID,
                                                                        refAnimal = animalJson.UUID,
                                                                        date = weightJson.date,
                                                                        value = weightJson.value
                                                                )
                                                        }
                                                        .toMutableList()

                                        // Ajouter les consultations - gérer les deux formats
                                        // possibles
                                        val consultations =
                                                when {
                                                        // Format 1: consultations directement dans
                                                        // l'objet animal
                                                        animalJson.consultations != null ->
                                                                animalJson.consultations
                                                        // Format 2: consultations dans
                                                        // list.consultations
                                                        animalJson.list != null ->
                                                                animalJson.list.consultations
                                                        // Aucune consultation
                                                        else -> emptyList()
                                                }

                                        animal.consultations =
                                                consultations
                                                        .map { consultJson ->
                                                                val consultation =
                                                                        ConsultationEv(
                                                                                uuid =
                                                                                        consultJson
                                                                                                .UUID,
                                                                                idAnim =
                                                                                        animalJson
                                                                                                .UUID,
                                                                                date =
                                                                                        consultJson
                                                                                                .date,
                                                                                objectConsult =
                                                                                        consultJson
                                                                                                .objet
                                                                                                ?: "",
                                                                                observation =
                                                                                        consultJson
                                                                                                .observation
                                                                                                ?: "",
                                                                                cRendu =
                                                                                        consultJson
                                                                                                .CRendu
                                                                                                ?: "",
                                                                                weight =
                                                                                        consultJson
                                                                                                .Poids,
                                                                                idealWeight =
                                                                                        consultJson
                                                                                                .PoidsIdeal,
                                                                                water =
                                                                                        consultJson
                                                                                                .Boisson,
                                                                                bodyFat =
                                                                                        consultJson
                                                                                                .TauxMG,
                                                                                BCS =
                                                                                        consultJson
                                                                                                .bcs
                                                                                                ?.toIntOrNull(),
                                                                                MCS =
                                                                                        consultJson
                                                                                                .MCS,
                                                                                k1Value =
                                                                                        consultJson
                                                                                                .k1value,
                                                                                k2Value =
                                                                                        consultJson
                                                                                                .k2value,
                                                                                k3Value =
                                                                                        consultJson
                                                                                                .k3value,
                                                                                k4Value =
                                                                                        consultJson
                                                                                                .k4value,
                                                                                k5Value =
                                                                                        consultJson
                                                                                                .k5value
                                                                        )

                                                                // Ajouter les rations à la
                                                                // consultation
                                                                consultation.rations =
                                                                        consultJson
                                                                                .rationList
                                                                                .values
                                                                                .map { rationJson ->
                                                                                        val ration =
                                                                                                Ration(
                                                                                                        uuid =
                                                                                                                rationJson
                                                                                                                        .UUID,
                                                                                                        idConsult =
                                                                                                                consultJson
                                                                                                                        .UUID,
                                                                                                        name =
                                                                                                                rationJson
                                                                                                                        .Nom,
                                                                                                        actual =
                                                                                                                rationJson
                                                                                                                        .actual
                                                                                                )

                                                                                        // Ajouter
                                                                                        // les
                                                                                        // aliments
                                                                                        // à la
                                                                                        // ration
                                                                                        ration.alimentMutableList =
                                                                                                rationJson
                                                                                                        .alimentList
                                                                                                        .map {
                                                                                                                alimentJson
                                                                                                                ->
                                                                                                                // Créer l'AlimentRation avec les données du JSON
                                                                                                                AlimentRation(
                                                                                                                        uuid =
                                                                                                                                alimentJson
                                                                                                                                        .UUID,
                                                                                                                        uuidUnif =
                                                                                                                                alimentJson
                                                                                                                                        .UUIDunif,
                                                                                                                        refRation =
                                                                                                                                rationJson
                                                                                                                                        .UUID,
                                                                                                                        quantite =
                                                                                                                                alimentJson
                                                                                                                                        .quantite,
                                                                                                                        proportion =
                                                                                                                                alimentJson
                                                                                                                                        .prop,
                                                                                                                        weight =
                                                                                                                                alimentJson
                                                                                                                                        .weight,
                                                                                                                        category =
                                                                                                                                alimentJson
                                                                                                                                        .categ,
                                                                                                                        densiteEnergetique =
                                                                                                                                alimentJson
                                                                                                                                        .density,
                                                                                                                        refAlimUnif =
                                                                                                                                alimentJson
                                                                                                                                        .UUIDunif
                                                                                                                )
                                                                                                        }
                                                                                                        .toMutableList()

                                                                                        ration
                                                                                }
                                                                                .toMutableList()

                                                                // Ajouter les variables
                                                                // supplémentaires à la consultation
                                                                consultation.suppVarp =
                                                                        consultJson
                                                                                .svp
                                                                                .map { svpJson ->
                                                                                        SupplementalvariableP(
                                                                                                variable =
                                                                                                        try {
                                                                                                                VariableKind
                                                                                                                        .valueOf(
                                                                                                                                svpJson.variable
                                                                                                                        )
                                                                                                        } catch (
                                                                                                                e:
                                                                                                                        Exception) {
                                                                                                                null
                                                                                                        },
                                                                                                varue =
                                                                                                        svpJson.value
                                                                                        )
                                                                                }
                                                                                .toMutableList()

                                                                consultation
                                                        }
                                                        .toMutableList()

                                        // Vérifier si l'animal existe déjà
                                        val existingAnimal = animalDao.getAnimalById(animal.uuid)
                                        if (existingAnimal != null) {
                                                // L'animal existe déjà, faire une mise à jour
                                                animalDao.update(
                                                        animal.toEntity(includeRelations = false)
                                                )

                                                // Supprimer les anciennes relations pour éviter les
                                                // doublons
                                                animalDao.deleteWeightsForAnimal(animal.uuid)

                                                // Récupérer les consultations existantes
                                                val existingConsultations =
                                                        animalDao.getConsultationsForAnimal(
                                                                animal.uuid
                                                        )

                                                // Supprimer les consultations existantes et leurs
                                                // relations
                                                existingConsultations.forEach { consultation ->
                                                        animalDao
                                                                .deleteSupplementalVariablesForConsultation(
                                                                        consultation.uuid
                                                                )
                                                        animalDao.deleteRationsForConsultation(
                                                                consultation.uuid
                                                        )
                                                        animalDao.deleteConsultation(consultation)
                                                }
                                        } else {
                                                // L'animal n'existe pas, l'insérer
                                                animalDao.insert(
                                                        animal.toEntity(includeRelations = false)
                                                )
                                        }

                                        // Insérer les poids
                                        animal.weightHistory.forEach { weight ->
                                                weight.refAnimal = animal.uuid
                                                animalDao.insertWeight(weight.toEntity())
                                        }

                                        // Insérer les consultations avec leurs relations
                                        animal.consultations.forEach { consultation ->
                                                consultation.idAnim = animal.uuid
                                                animalDao.insertConsultation(
                                                        consultation.toEntity(
                                                                includeRelations = false
                                                        )
                                                )

                                                // Insérer les variables supplémentaires
                                                consultation.suppVarp.forEach { suppVar ->
                                                        suppVar.variable?.let { variable ->
                                                                animalDao
                                                                        .insertSupplementalVariable(
                                                                                SupplementalVariableEntity(
                                                                                        idConsult =
                                                                                                consultation
                                                                                                        .uuid,
                                                                                        variableKind =
                                                                                                variable.uuid,
                                                                                        value =
                                                                                                suppVar.varue
                                                                                                        ?: 0.0
                                                                                )
                                                                        )
                                                        }
                                                }

                                                // Insérer les rations avec leurs aliments
                                                consultation.rations.forEach { ration ->
                                                        ration.idConsult = consultation.uuid
                                                        animalDao.insertRation(
                                                                ration.toEntity(
                                                                        includeRelations = false
                                                                )
                                                        )

                                                        // Filtrer les aliments pour n'insérer que
                                                        // ceux qui ont une référence à un aliment
                                                        // existant
                                                        val validAliments =
                                                                ration.alimentMutableList.filter {
                                                                        aliment ->
                                                                        val refAlimUnif =
                                                                                aliment.refAlimUnif

                                                                        // Si pas de référence,
                                                                        // ignorer cet aliment
                                                                        if (refAlimUnif == null) {
                                                                                return@filter false
                                                                        }

                                                                        // Vérifier si l'aliment
                                                                        // existe déjà dans la table
                                                                        // FOOD
                                                                        val existingFood =
                                                                                foodDao.getFoodById(
                                                                                        refAlimUnif
                                                                                )

                                                                        if (existingFood != null) {
                                                                                // L'aliment existe
                                                                                // déjà, on peut
                                                                                // l'utiliser
                                                                                return@filter true
                                                                        } else {
                                                                                // L'aliment
                                                                                // n'existe pas,
                                                                                // essayons de le
                                                                                // créer à partir
                                                                                // des données
                                                                                // disponibles
                                                                                try {
                                                                                        // Récupérer
                                                                                        // le nom
                                                                                        // depuis la
                                                                                        // map ou
                                                                                        // utiliser
                                                                                        // un nom
                                                                                        // par
                                                                                        // défaut
                                                                                        val nomAliment =
                                                                                                foodNamesMap[
                                                                                                        refAlimUnif]
                                                                                                        ?: "Aliment importé ${refAlimUnif}"

                                                                                        val foodEntity =
                                                                                                FoodEntity(
                                                                                                        uuid =
                                                                                                                refAlimUnif,
                                                                                                        groupAlim =
                                                                                                                0,
                                                                                                        typeAlim =
                                                                                                                0,
                                                                                                        ingredients =
                                                                                                                "",
                                                                                                        price =
                                                                                                                0.0,
                                                                                                        categPrice =
                                                                                                                "",
                                                                                                        brand =
                                                                                                                "",
                                                                                                        gamme =
                                                                                                                "",
                                                                                                        unitPres =
                                                                                                                0,
                                                                                                        quantityPres =
                                                                                                                0.0,
                                                                                                        version =
                                                                                                                1,
                                                                                                        date =
                                                                                                                "",
                                                                                                        nameDef =
                                                                                                                nomAliment,
                                                                                                        consistent =
                                                                                                                1,
                                                                                                        deprecated =
                                                                                                                0,
                                                                                                        DataB =
                                                                                                                "",
                                                                                                        RefRation =
                                                                                                                "",
                                                                                                        RefAlimUnif =
                                                                                                                "",
                                                                                                        cont =
                                                                                                                "NO",
                                                                                                        name =
                                                                                                                nomAliment,
                                                                                                        quantite =
                                                                                                                0.0,
                                                                                                        especesJson =
                                                                                                                "[]",
                                                                                                        indicationsJson =
                                                                                                                "[]"
                                                                                                )

                                                                                        // Insérer
                                                                                        // l'aliment
                                                                                        // dans la
                                                                                        // table
                                                                                        // FOOD
                                                                                        foodDao.insert(
                                                                                                foodEntity
                                                                                        )
                                                                                        availableFoodUUIDs
                                                                                                .add(
                                                                                                        refAlimUnif
                                                                                                )
                                                                                        return@filter true
                                                                                } catch (
                                                                                        e:
                                                                                                Exception) {
                                                                                        return@filter false
                                                                                }
                                                                        }
                                                                }

                                                        // Insérer uniquement les aliments valides
                                                        validAliments.forEach { aliment ->
                                                                aliment.refRation = ration.uuid
                                                                try {
                                                                        // Insérer l'AlimentRation
                                                                        animalDao
                                                                                .insertAlimentRation(
                                                                                        aliment.toEntity()
                                                                                )
                                                                } catch (e: Exception) {}
                                                        }

                                                        // Charger les données AlimentEv pour chaque
                                                        // AlimentRation
                                                        ration.alimentMutableList.forEach {
                                                                alimentRation ->
                                                                val foodEntity =
                                                                        foodDao.getFoodById(
                                                                                alimentRation
                                                                                        .refAlimUnif
                                                                                        ?: ""
                                                                        )
                                                                if (foodEntity != null) {
                                                                        // Créer un AlimentEv à
                                                                        // partir de FoodEntity
                                                                        val alimentEv =
                                                                                AlimentEv(
                                                                                        uuid =
                                                                                                foodEntity
                                                                                                        .uuid,
                                                                                        nom =
                                                                                                foodEntity
                                                                                                        .nameDef,
                                                                                        group =
                                                                                                null,
                                                                                        typeAliment =
                                                                                                null,
                                                                                        ingredients =
                                                                                                foodEntity
                                                                                                        .ingredients,
                                                                                        price =
                                                                                                foodEntity
                                                                                                        .price,
                                                                                        categPrice =
                                                                                                foodEntity
                                                                                                        .categPrice,
                                                                                        brand =
                                                                                                foodEntity
                                                                                                        .brand,
                                                                                        gamme =
                                                                                                foodEntity
                                                                                                        .gamme,
                                                                                        consistent =
                                                                                                foodEntity
                                                                                                        .consistent !=
                                                                                                        0,
                                                                                        cont =
                                                                                                fr.vetbrain
                                                                                                        .vetnutri_mp
                                                                                                        .Enumer
                                                                                                        .ContEnum
                                                                                                        .byId(
                                                                                                                foodEntity
                                                                                                                        .consistent
                                                                                                        ),
                                                                                        quantInt =
                                                                                                foodEntity
                                                                                                        .quantityPres,
                                                                                        deprecated =
                                                                                                foodEntity
                                                                                                        .deprecated >
                                                                                                        0,
                                                                                        dataB =
                                                                                                foodEntity
                                                                                                        .DataB,
                                                                                        rationUUID =
                                                                                                alimentRation
                                                                                                        .uuid
                                                                                )
                                                                        alimentRation.aliment =
                                                                                alimentEv
                                                                } else {}
                                                        }

                                                        rationsWithAliments++
                                                        totalAlimentsInRations +=
                                                                consultation.rations.size
                                                }
                                        }

                                        importedCount++
                                        println("Animal importé: ${animal.nom} (ID=${animal.uuid})")
                                } catch (e: Exception) {
                                        // Ignorer les erreurs d'importation pour un animal
                                        // spécifique
                                        e.printStackTrace() // Ajouter la trace de la pile pour le
                                        // débogage
                                }
                        }

                        val totalCount = getAllAnimals().size

                        return@withContext AnimalImportResult(
                                importedCount = importedCount,
                                updatedCount =
                                        0, // Nous ne suivons pas actuellement les mises à jour
                                errorCount = animalsJson.size - importedCount,
                                totalCount = totalCount,
                                foodsImportedCount = importedFoodsCount
                        )
                }
        }

        /**
         * Récupère le repository des aliments
         *
         * @return Le repository des aliments ou null s'il n'est pas disponible
         */
        override fun getFoodRepository(): FoodRepository? {
                // Créer une instance de DatabaseFoodRepository avec le FoodDao et NutrientValueDao
                return DatabaseFoodRepository(foodDao, nutrientValueDao)
        }
}
