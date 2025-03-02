package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.Data.WeightDate
import fr.vetbrain.vetnutri_mp.DataBase.AnimalDao
import fr.vetbrain.vetnutri_mp.DataBase.FoodDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.DataBase.SupplementalVariableEntity
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class DatabaseAnimalRepository(private val animalDao: AnimalDao, private val foodDao: FoodDao) :
        AnimalRepository {
        override suspend fun saveAnimal(animal: AnimalEv) {
                withContext(AppDispatchers.Default) { animalDao.insert(animal.toEntity()) }
        }

        override suspend fun getAllAnimals(): List<AnimalEv> {
                return withContext(AppDispatchers.Default) {
                        animalDao.getAllAnimals().map { entity ->
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
                        animalDao.update(animal.toEntity(includeRelations = false))
                }
        }

        override suspend fun getAnimalById(id: String): AnimalEv? {
                return withContext(AppDispatchers.Default) {
                        val entity = animalDao.getAnimalById(id) ?: return@withContext null

                        // Convertir l'entité en objet de domaine
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
                }
        }

        override suspend fun importAnimals(animalsJson: List<AnimalEvJson>): Int {
                return withContext(AppDispatchers.Default) {
                        // Vérifier si les aliments existent dans la base de données
                        val allFoods = foodDao.getAllFoods()
                        println("Nombre d'aliments dans la base de données: ${allFoods.size}")
                        if (allFoods.isNotEmpty()) {
                                println(
                                        "Premier aliment: ${allFoods.first().name}, ID: ${allFoods.first().uuid}"
                                )
                        }

                        // Créer un ensemble des UUIDs des aliments disponibles pour référence
                        // rapide
                        val availableFoodUUIDs = allFoods.map { it.uuid }.toSet()
                        println("UUIDs des aliments disponibles: $availableFoodUUIDs")

                        var importedCount = 0

                        for (animalJson in animalsJson) {
                                try {
                                        // Convertir manuellement AnimalEvJson en AnimalEv
                                        val animal =
                                                AnimalEv(
                                                        uuid = animalJson.UUID,
                                                        nom = animalJson.nom,
                                                        dead = animalJson.dead,
                                                        id = animalJson.id,
                                                        sexId = animalJson.sex,
                                                        specieId = animalJson.espece,
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
                                                                                                                // Créer
                                                                                                                // l'AlimentRation
                                                                                                                // avec les données
                                                                                                                // du JSON
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
                                                                                                                        quantity =
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
                                                                                                                        density =
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
                                                                        val hasValidReference =
                                                                                aliment.refAlimUnif !=
                                                                                        null &&
                                                                                        availableFoodUUIDs
                                                                                                .contains(
                                                                                                        aliment.refAlimUnif
                                                                                                )
                                                                        if (!hasValidReference) {
                                                                                println(
                                                                                        "Ignoré: Aliment avec ID=${aliment.uuid} référence un aliment inconnu: ${aliment.refAlimUnif}"
                                                                                )
                                                                        }
                                                                        hasValidReference
                                                                }

                                                        // Insérer uniquement les aliments valides
                                                        validAliments.forEach { aliment ->
                                                                aliment.refRation = ration.uuid
                                                                try {
                                                                        animalDao
                                                                                .insertAlimentRation(
                                                                                        aliment.toEntity()
                                                                                )
                                                                        println(
                                                                                "Inséré: AlimentRation avec ID=${aliment.uuid}, référençant l'aliment ${aliment.refAlimUnif}"
                                                                        )
                                                                } catch (e: Exception) {
                                                                        println(
                                                                                "Erreur lors de l'insertion de l'aliment avec ID=${aliment.uuid}: ${e.message}"
                                                                        )
                                                                }
                                                        }

                                                        // Charger les données AlimentEv pour chaque
                                                        // AlimentRation
                                                        ration.alimentMutableList.forEach {
                                                                alimentRation ->
                                                                println(
                                                                        "Recherche de l'aliment avec ID: ${alimentRation.refAlimUnif}"
                                                                )
                                                                val alimentEvEntity =
                                                                        foodDao.getFoodById(
                                                                                alimentRation
                                                                                        .refAlimUnif
                                                                                        ?: ""
                                                                        )
                                                                if (alimentEvEntity != null) {
                                                                        println(
                                                                                "Aliment trouvé: ${alimentEvEntity.nameDef}"
                                                                        )
                                                                        // Créer un AlimentEv à
                                                                        // partir de FoodEntity
                                                                        val alimentEv =
                                                                                AlimentEv(
                                                                                        uuid =
                                                                                                alimentEvEntity
                                                                                                        .uuid,
                                                                                        nom =
                                                                                                alimentEvEntity
                                                                                                        .nameDef,
                                                                                        group =
                                                                                                null,
                                                                                        typeAliment =
                                                                                                null,
                                                                                        ingredients =
                                                                                                alimentEvEntity
                                                                                                        .ingredients,
                                                                                        price =
                                                                                                alimentEvEntity
                                                                                                        .price,
                                                                                        categPrice =
                                                                                                alimentEvEntity
                                                                                                        .categPrice,
                                                                                        brand =
                                                                                                alimentEvEntity
                                                                                                        .brand,
                                                                                        gamme =
                                                                                                alimentEvEntity
                                                                                                        .gamme,
                                                                                        consistent =
                                                                                                alimentEvEntity
                                                                                                        .consistent !=
                                                                                                        0,
                                                                                        cont =
                                                                                                alimentEvEntity
                                                                                                        .consistent,
                                                                                        quantInt =
                                                                                                alimentEvEntity
                                                                                                        .quantityPres,
                                                                                        deprecated =
                                                                                                alimentEvEntity
                                                                                                        .deprecated,
                                                                                        dataB =
                                                                                                alimentEvEntity
                                                                                                        .DataB,
                                                                                        rationUUID =
                                                                                                alimentRation
                                                                                                        .uuid
                                                                                )
                                                                        alimentRation.aliment =
                                                                                alimentEv
                                                                } else {
                                                                        println(
                                                                                "Aliment non trouvé pour ID: ${alimentRation.refAlimUnif}"
                                                                        )
                                                                }
                                                        }
                                                }
                                        }

                                        importedCount++
                                } catch (e: Exception) {
                                        // Ignorer les erreurs d'importation pour un animal
                                        // spécifique
                                        println(
                                                "Erreur lors de l'importation de l'animal: ${e.message}"
                                        )
                                        e.printStackTrace() // Ajouter la trace de la pile pour le
                                        // débogage
                                }
                        }

                        importedCount
                }
        }

        /**
         * Récupère le repository des aliments
         *
         * @return Le repository des aliments ou null s'il n'est pas disponible
         */
        override fun getFoodRepository(): FoodRepository? {
                // Nous ne pouvons pas accéder directement au nutrientValueDao
                // Retournons null pour indiquer que le repository n'est pas disponible
                return null
        }
}
