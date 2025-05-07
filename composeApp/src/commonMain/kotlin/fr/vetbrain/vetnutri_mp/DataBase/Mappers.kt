package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import kotlinx.datetime.LocalDate

object Mappers {
        // Animal Mappers avec relations
        fun AnimalEv.toEntity(includeRelations: Boolean = true): AnimalEntity {
                val entity =
                        AnimalEntity(
                                uuid = this.uuid,
                                nom = this.nom,
                                dead = this.dead,
                                id = this.id ?: "",
                                sexId = this.sexId,
                                specieId = this.specieId,
                                ownerName = this.ownerName,
                                birthdate = this.birthdate?.toString() ?: "",
                                race = this.race,
                                summary = this.summary
                        )

                if (includeRelations) {
                        // Convertir les consultations
                        this.consultations.forEach { consultation ->
                                consultation.idAnim = this.uuid
                        }
                        // Convertir l'historique des poids
                        this.weightHistory.forEach { weight -> weight.refAnimal = this.uuid }
                }

                return entity
        }

        fun AnimalEntity.toData(
                consultations: List<ConsultationEntity> = emptyList(),
                weights: List<WeightEntity> = emptyList()
        ): AnimalEv {
                return AnimalEv(
                        uuid = this.uuid,
                        nom = this.nom ?: "",
                        dead = this.dead ?: false,
                        id = this.id?.takeIf { it.isNotEmpty() },
                        sexId = this.sexId ?: 0,
                        specieId = this.specieId ?: "",
                        ownerName = this.ownerName ?: "",
                        birthdate =
                                this.birthdate?.takeIf { it.isNotEmpty() }?.let {
                                        LocalDate.parse(it)
                                },
                        race = this.race ?: "",
                        summary = this.summary ?: "",
                        consultations = consultations.map { it.toData() }.toMutableList(),
                        weightHistory = weights.map { it.toData() }.toMutableList()
                )
        }

        // Consultation Mappers avec relations
        fun ConsultationEv.toEntity(includeRelations: Boolean = true): ConsultationEntity {
                return ConsultationEntity(
                                uuid = this.uuid,
                                idAnim = this.idAnim,
                                date = this.date?.toString() ?: "",
                                objectConsult = this.objectConsult ?: "",
                                observation = this.observation ?: "",
                                cRendu = this.cRendu ?: "",
                                weight = this.weight ?: 0f,
                                idealWeight = this.idealWeight ?: 0f,
                                water = this.water ?: 0f,
                                bodyFat = this.bodyFat ?: 0f,
                                methodAnalysis = this.methodAnalysis ?: "",
                                BCS = this.BCS ?: 0,
                                k1Id = this.k1Id ?: "",
                                k1Value = this.k1Value ?: 0f,
                                k2Id = this.k2Id ?: "",
                                k2Value = this.k2Value ?: 0f,
                                k3Id = this.k3Id ?: "",
                                k3Value = this.k3Value ?: 0f,
                                k4Id = this.k4Id ?: "",
                                k4Value = this.k4Value ?: 0f,
                                k5Id = this.k5Id ?: "",
                                k5Value = this.k5Value ?: 0f,
                                nLittle = this.nLittle ?: 0,
                                pAdult = this.pAdult ?: 0f,
                                coefGes = this.coefGes ?: 0,
                                coefLact = this.coefLact ?: 0,
                                MCS = this.MCS ?: 0
                        )
                        .apply {
                                if (includeRelations) {
                                        this@toEntity.rations.forEach { ration ->
                                                ration.idConsult = this@toEntity.uuid
                                        }
                                        this@toEntity.suppVarp.forEach { suppVar ->
                                                suppVar.variable?.let { variable ->
                                                        SupplementalVariableEntity(
                                                                idConsult = this@toEntity.uuid,
                                                                variableKind = variable.uuid,
                                                                value = suppVar.varue ?: 0f
                                                        )
                                                }
                                        }
                                }
                        }
        }

        fun ConsultationEntity.toData(
                rations: List<RationEntity> = emptyList(),
                suppVars: List<SupplementalVariableEntity> = emptyList()
        ): ConsultationEv {
                return ConsultationEv(
                        uuid = this.uuid,
                        idAnim = this.idAnim ?: "",
                        date = this.date?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) },
                        objectConsult = this.objectConsult ?: "",
                        observation = this.observation ?: "",
                        cRendu = this.cRendu ?: "",
                        weight = this.weight ?: 0f,
                        idealWeight = this.idealWeight ?: 0f,
                        water = this.water ?: 0f,
                        bodyFat = this.bodyFat ?: 0f,
                        methodAnalysis = this.methodAnalysis ?: "",
                        BCS = this.BCS ?: 0,
                        k1Id = this.k1Id ?: "",
                        k1Value = this.k1Value ?: 0f,
                        k2Id = this.k2Id ?: "",
                        k2Value = this.k2Value ?: 0f,
                        k3Id = this.k3Id ?: "",
                        k3Value = this.k3Value ?: 0f,
                        k4Id = this.k4Id ?: "",
                        k4Value = this.k4Value ?: 0f,
                        k5Id = this.k5Id ?: "",
                        k5Value = this.k5Value ?: 0f,
                        nLittle = this.nLittle ?: 0,
                        pAdult = this.pAdult ?: 0f,
                        coefGes = this.coefGes ?: 0,
                        coefLact = this.coefLact ?: 0,
                        MCS = this.MCS ?: 0,
                        suppVarp =
                                suppVars
                                        .map { entity ->
                                                SupplementalvariableP(
                                                        variable =
                                                                VariableKind.getById(
                                                                        entity.variableKind
                                                                ),
                                                        varue = entity.value ?: 0f
                                                )
                                        }
                                        .toMutableList(),
                        rations = rations.map { it.toData() }.toMutableList()
                )
        }

        // Ration Mappers avec relations
        fun Ration.toEntity(includeRelations: Boolean = true): RationEntity {
                return RationEntity(
                                uuid = this.uuid,
                                idConsult = this.idConsult,
                                name = this.name ?: "",
                                coef = this.coef ?: 1.0f,
                                actual = this.actual ?: false,
                                number = this.number ?: 1,
                                espece = this.espece ?: "",
                                recette = this.recette ?: false,
                                description = this.description ?: ""
                        )
                        .apply {
                                if (includeRelations) {
                                        this@toEntity.alimentMutableList.forEach { aliment ->
                                                aliment.refRation = this@toEntity.uuid
                                        }
                                }
                        }
        }

        fun RationEntity.toData(aliments: List<AlimentRationEntity> = emptyList()): Ration {
                return Ration(
                        uuid = this.uuid,
                        idConsult = this.idConsult ?: "",
                        name = this.name ?: "",
                        coef = this.coef ?: 1.0f,
                        actual = this.actual ?: false,
                        number = this.number ?: 1,
                        espece = this.espece ?: "",
                        recette = this.recette ?: false,
                        description = this.description ?: "",
                        alimentMutableList = aliments.map { it.toData() }.toMutableList()
                )
        }

        // AlimentRation Mappers
        fun AlimentRation.toEntity(): AlimentRationEntity {
                // Fournir des valeurs par défaut pour gérer les nullables
                val safeRefAlimUnif = this.refAlimUnif ?: ""
                val safeRefRation = this.refRation ?: ""
                val safeQuantity = this.quantite ?: 0f
                val safeRefTarget = this.refTarget ?: 0

                return AlimentRationEntity(
                        uuid = this.uuid,
                        refAlimUnif = safeRefAlimUnif,
                        refRation = safeRefRation,
                        quantity = safeQuantity,
                        refTarget = safeRefTarget
                )
        }

        fun AlimentRationEntity.toData(): AlimentRation {
                return AlimentRation(
                        uuid = this.uuid,
                        uuidUnif = this.refAlimUnif ?: "",
                        quantite = this.quantity ?: 0f,
                        proportion = 0f,
                        aliment = null,
                        refAlimUnif = this.refAlimUnif ?: "",
                        refRation = this.refRation ?: "",
                        refTarget = this.refTarget ?: 0
                )
        }

        // DÉBUT ZONE PROTÉGÉE - NE PAS MODIFIER SANS AUTORISATION EXPRESSE
        // Description: Méthode critique pour la conversion d'AlimentEv en FoodEntity.
        // Cette méthode est utilisée dans le processus d'import pour convertir les données en
        // entités de base de données.
        fun AlimentEv.toFoodEntity(): FoodEntity {
                // Assurer que les valeurs ne sont pas nulles
                val safeRationUUID = this.rationUUID ?: ""
                val safeQuantInt = this.quantInt ?: 0f

                return FoodEntity(
                        uuid = this.uuid,
                        groupAlim = this.group?.ordinal ?: 0,
                        typeAlim = this.typeAliment?.ordinal ?: 0,
                        ingredients = this.ingredients ?: "",
                        price = this.price ?: 0.0,
                        categPrice = this.categPrice ?: "",
                        brand = this.brand ?: "",
                        gamme = this.gamme ?: "",
                        cont = this.cont?.name ?: "NO",
                        unitPres = 0, // À adapter selon vos besoins
                        quantityPres = this.quantInt ?: 0f,
                        version = 1, // À adapter selon vos besoins
                        date = "", // À adapter selon vos besoins
                        nameDef = this.nom ?: "",
                        consistent = if (this.consistent) 1 else 0,
                        deprecated = if (this.deprecated) 1 else 0,
                        DataB = this.dataB ?: "",
                        RefRation = safeRationUUID,
                        name = this.nom,
                        quantite = safeQuantInt,
                        especesJson =
                                if (this.especes.isNotEmpty()) this.especes.joinToString(",")
                                else null,
                        indicationsJson =
                                if (this.indicat.isNotEmpty())
                                        this.indicat.joinToString(",") { it.name }
                                else null
                )
        }
        // FIN ZONE PROTÉGÉE

        // DÉBUT ZONE PROTÉGÉE - NE PAS MODIFIER SANS AUTORISATION EXPRESSE
        // Description: Méthode critique pour la conversion de FoodEntity en AlimentEv.
        // Cette méthode est utilisée pour convertir les entités de base de données en objets
        // métier.
        // Fonction pour convertir les entités en AlimentEv
        fun FoodEntity.toAlimentEv(
                especes: List<EspeceAlimentEntity> = emptyList(),
                indications: List<IndicationAlimentEntity> = emptyList(),
                nutrientValues: List<NutrientValueEntity> = emptyList()
        ): AlimentEv {
                val especesList = mutableListOf<String>()

                // Ajouter des logs de débogage pour comprendre le contenu de especesJson
                println(
                        "DEBUG Mappers: Extraction des espèces pour l'aliment ${this.uuid} (${this.nameDef})"
                )
                println("DEBUG Mappers: especesJson = ${this.especesJson}")

                if (!this.especesJson.isNullOrEmpty()) {
                        // Extraire la liste des espèces du JSON
                        val especesStringList = this.especesJson.split(",")
                        println("DEBUG Mappers: especesStringList = $especesStringList")

                        // Essayer de convertir chaque espèce en énumération
                        especesStringList.forEach { especeStr ->
                                try {
                                        // Utiliser getFromString qui gère désormais le nettoyage et
                                        // la conversion
                                        val espece = Espece.getFromString(especeStr)
                                        if (espece != null) {
                                                println(
                                                        "DEBUG Mappers: Conversion réussie de l'espèce: $especeStr -> ${espece.name}"
                                                )
                                                especesList.add(
                                                        espece.name
                                                ) // Utiliser le nom de l'énumération
                                        } else {
                                                println(
                                                        "DEBUG Mappers: Espèce non reconnue, conservation du texte original: $especeStr"
                                                )
                                                // Si l'espèce n'est pas vide, l'ajouter à la liste
                                                val cleanedEspece =
                                                        especeStr
                                                                .replace("[", "")
                                                                .replace("]", "")
                                                                .replace("\"", "")
                                                                .trim()
                                                if (cleanedEspece.isNotEmpty()) {
                                                        especesList.add(cleanedEspece)
                                                }
                                        }
                                } catch (e: Exception) {
                                        println(
                                                "DEBUG Mappers: Erreur lors de la conversion de l'espèce $especeStr: ${e.message}"
                                        )
                                        // Nettoyer quand même en cas d'erreur
                                        val cleanedEspece =
                                                especeStr
                                                        .replace("[", "")
                                                        .replace("]", "")
                                                        .replace("\"", "")
                                                        .trim()
                                        if (cleanedEspece.isNotEmpty()) {
                                                especesList.add(cleanedEspece)
                                        }
                                }
                        }
                } else {
                        // Si pas de JSON, utiliser les entités directement
                        println(
                                "DEBUG Mappers: Pas de especesJson, utilisation des entités directes: ${especes.size} espèces"
                        )
                        especesList.addAll(
                                especes
                                        .map {
                                                try {
                                                        // Utiliser getFromString qui gère désormais
                                                        // le
                                                        // nettoyage et la conversion
                                                        val espece = Espece.getFromString(it.espece)
                                                        if (espece != null) {
                                                                println(
                                                                        "DEBUG Mappers: Espèce trouvée dans les entités: ${espece.name}"
                                                                )
                                                                espece.name
                                                        } else {
                                                                // Nettoyer quand même avant
                                                                // d'ajouter
                                                                val cleaned =
                                                                        it.espece
                                                                                .replace("[", "")
                                                                                .replace("]", "")
                                                                                .replace("\"", "")
                                                                                .trim()
                                                                println(
                                                                        "DEBUG Mappers: Espèce non reconnue dans les entités: $cleaned"
                                                                )
                                                                if (cleaned.isNotEmpty()) cleaned
                                                                else null
                                                        }
                                                } catch (e: Exception) {
                                                        // Nettoyer quand même en cas d'erreur
                                                        val cleaned =
                                                                it.espece
                                                                        .replace("[", "")
                                                                        .replace("]", "")
                                                                        .replace("\"", "")
                                                                        .trim()
                                                        println(
                                                                "DEBUG Mappers: Erreur lors de la conversion de l'espèce ${it.espece}: ${e.message}"
                                                        )
                                                        if (cleaned.isNotEmpty()) cleaned else null
                                                }
                                        }
                                        .filterNotNull()
                        )
                }

                println(
                        "DEBUG Mappers: Espèces extraites pour l'aliment ${this.uuid}: $especesList"
                )

                val indicatList = mutableListOf<AlimIndic>()

                // Ajouter des logs de débogage pour comprendre le contenu de indicationsJson
                println("DEBUG Mappers: Extraction des indications pour l'aliment ${this.uuid}")
                println("DEBUG Mappers: indicationsJson = ${this.indicationsJson}")

                if (!this.indicationsJson.isNullOrEmpty()) {
                        val indicationsStringList = this.indicationsJson.split(",")
                        println("DEBUG Mappers: indicationsStringList = $indicationsStringList")

                        indicationsStringList.forEach { indic ->
                                try {
                                        // Utiliser getFromString qui gère désormais le nettoyage et
                                        // la conversion
                                        val alimIndic = AlimIndic.getFromString(indic)
                                        if (alimIndic != AlimIndic.AUTRE ||
                                                        indic.trim().uppercase() == "AUTRE"
                                        ) {
                                                indicatList.add(alimIndic)
                                                println(
                                                        "DEBUG Mappers: Indication reconnue: $indic -> ${alimIndic.name}"
                                                )
                                        }
                                } catch (e: Exception) {
                                        println(
                                                "DEBUG Mappers: Erreur lors du traitement de l'indication $indic: ${e.message}"
                                        )
                                }
                        }
                } else {
                        println(
                                "DEBUG Mappers: Pas de indicationsJson, utilisation des entités directes: ${indications.size} indications"
                        )
                        indicatList.addAll(
                                indications.mapNotNull { entity ->
                                        try {
                                                val indication =
                                                        AlimIndic.entries.getOrNull(
                                                                entity.indication
                                                        )
                                                if (indication != null) {
                                                        println(
                                                                "DEBUG Mappers: Indication trouvée dans les entités: ${indication.name}"
                                                        )
                                                }
                                                indication
                                        } catch (e: Exception) {
                                                println(
                                                        "DEBUG Mappers: Erreur lors de la conversion de l'indication ${entity.indication}: ${e.message}"
                                                )
                                                null
                                        }
                                }
                        )
                }

                println(
                        "DEBUG Mappers: Indications extraites pour l'aliment ${this.uuid}: ${indicatList.map { it.name }}"
                )

                return AlimentEv(
                        uuid = this.uuid,
                        nom = this.nameDef ?: "",
                        typeAliment =
                                try {
                                        this.typeAlim.let { FoodKind.entries.getOrNull(it) }
                                } catch (e: Exception) {
                                        null
                                },
                        group =
                                try {
                                        this.groupAlim.let { GroupAlim.entries.getOrNull(it) }
                                } catch (e: Exception) {
                                        null
                                },
                        consistent = this.consistent == 1,
                        deprecated = this.deprecated == 1,
                        price = this.price,
                        categPrice = this.categPrice ?: "",
                        ingredients = this.ingredients ?: "",
                        brand = this.brand ?: "",
                        gamme = this.gamme ?: "",
                        quantInt = this.quantityPres ?: 0f,
                        dataB = this.DataB ?: "",
                        especes = especesList.toMutableList(),
                        indicat = indicatList.toMutableList(),
                        valMap = nutrientValues.toNutrientValueMap(),
                        cont = fr.vetbrain.vetnutri_mp.Enumer.ContEnum.getByName(this.cont ?: "NO"),
                        rationUUID = this.RefRation ?: ""
                )
        }
        // FIN ZONE PROTÉGÉE

        // Fonction pour convertir les entités en AlimentEvLight (version légère sans valeurs
        // nutritionnelles)
        fun FoodEntity.toAlimentEvLight(): AlimentEvLight {
                val especesList = mutableListOf<String>()

                // Extraire les espèces du JSON
                if (!this.especesJson.isNullOrEmpty()) {
                        val especesStringList = this.especesJson.split(",")

                        especesStringList.forEach { especeStr ->
                                try {
                                        val espece = Espece.getFromString(especeStr)
                                        if (espece != null) {
                                                especesList.add(espece.name)
                                        } else {
                                                val cleanedEspece =
                                                        especeStr
                                                                .replace("[", "")
                                                                .replace("]", "")
                                                                .replace("\"", "")
                                                                .trim()
                                                if (cleanedEspece.isNotEmpty()) {
                                                        especesList.add(cleanedEspece)
                                                }
                                        }
                                } catch (e: Exception) {
                                        val cleanedEspece =
                                                especeStr
                                                        .replace("[", "")
                                                        .replace("]", "")
                                                        .replace("\"", "")
                                                        .trim()
                                        if (cleanedEspece.isNotEmpty()) {
                                                especesList.add(cleanedEspece)
                                        }
                                }
                        }
                }

                val indicatList = mutableListOf<AlimIndic>()

                // Extraire les indications du JSON
                if (!this.indicationsJson.isNullOrEmpty()) {
                        val indicationsStringList = this.indicationsJson.split(",")

                        indicationsStringList.forEach { indic ->
                                try {
                                        val alimIndic = AlimIndic.getFromString(indic)
                                        if (alimIndic != AlimIndic.AUTRE ||
                                                        indic.trim().uppercase() == "AUTRE"
                                        ) {
                                                indicatList.add(alimIndic)
                                        }
                                } catch (e: Exception) {
                                        // Ignorer les indications non reconnues
                                }
                        }
                }

                return AlimentEvLight(
                        uuid = this.uuid,
                        nom = this.nameDef ?: "",
                        typeAliment =
                                try {
                                        this.typeAlim.let { FoodKind.entries.getOrNull(it) }
                                } catch (e: Exception) {
                                        null
                                },
                        group =
                                try {
                                        this.groupAlim.let { GroupAlim.entries.getOrNull(it) }
                                } catch (e: Exception) {
                                        null
                                },
                        brand = this.brand ?: "",
                        gamme = this.gamme ?: "",
                        especes = especesList,
                        indicat = indicatList,
                        deprecated = this.deprecated == 1
                )
        }

        // Weight Mappers
        fun WeightDate.toEntity(): WeightEntity {
                return WeightEntity(
                        uuid = this.uuid,
                        refAnimal = this.refAnimal,
                        date = this.date.toString(),
                        value = this.value
                )
        }

        fun WeightEntity.toData(): WeightDate {
                return WeightDate(
                        uuid = this.uuid,
                        refAnimal = this.refAnimal,
                        date = LocalDate.parse(this.date),
                        value = this.value
                )
        }

        /**
         * Convertit une map de nutriments et valeurs en liste d'entités de valeurs de nutriments.
         * Ne crée des entités que pour les nutriments avec des valeurs > 0.
         */
        // DÉBUT ZONE PROTÉGÉE - NE PAS MODIFIER SANS AUTORISATION EXPRESSE
        // Description: Méthode critique pour la conversion de Map<Nutrient, NutrientQuantity> en
        // List<NutrientValueEntity>.
        // Cette méthode est utilisée dans le processus d'import pour convertir les valeurs
        // nutritionnelles en entités de base de données.
        fun Map<Nutrient, fr.vetbrain.vetnutri_mp.Data.NutrientQuantity>.toNutrientValueEntities(
                alimentUuid: String
        ): List<NutrientValueEntity> {
                println(
                        "DEBUG Mappers: Conversion de ${this.size} nutriments en entités pour l'aliment $alimentUuid"
                )
                val result = mutableListOf<NutrientValueEntity>()

                if (this.isEmpty()) {
                        println(
                                "DEBUG Mappers: ATTENTION - Map de nutriments vide pour l'aliment $alimentUuid"
                        )
                        return emptyList()
                }

                this.forEach { (nutrient, nutrientQuantity) ->
                        try {
                                println(
                                        "DEBUG Mappers: Traitement du nutriment ${nutrient.label} = ${nutrientQuantity.value}"
                                )

                                // Vérification du label du nutriment
                                if (nutrient.label.isBlank()) {
                                        println(
                                                "DEBUG Mappers: ERREUR - Le label du nutriment est vide"
                                        )
                                        return@forEach
                                }

                                // Ne créer des entités que pour les valeurs strictement positives
                                if (nutrientQuantity.value > 0) {
                                        val entity =
                                                NutrientValueEntity(
                                                        refAliment = alimentUuid,
                                                        nutrientLabel = nutrient.label,
                                                        value = nutrientQuantity.value
                                                )
                                        result.add(entity)
                                        println(
                                                "DEBUG Mappers: Entité créée pour ${nutrient.label} = ${nutrientQuantity.value}"
                                        )
                                } else {
                                        println(
                                                "DEBUG Mappers: Valeur <= 0 pour ${nutrient.label} (${nutrientQuantity.value}), ignorée"
                                        )
                                }
                        } catch (e: Exception) {
                                println(
                                        "DEBUG Mappers: ERREUR lors de la conversion du nutriment ${nutrient.label}: ${e.message}"
                                )
                                e.printStackTrace()
                        }
                }

                println(
                        "DEBUG Mappers: ${result.size}/${this.size} nutriments convertis en entités"
                )
                return result
        }
        // FIN ZONE PROTÉGÉE

        /**
         * Convertit une liste d'entités de valeurs de nutriments en map de nutriments et valeurs.
         */
        // DÉBUT ZONE PROTÉGÉE - NE PAS MODIFIER SANS AUTORISATION EXPRESSE
        // Description: Méthode critique pour la conversion de List<NutrientValueEntity> en
        // Map<Nutrient, NutrientQuantity>.
        // Cette méthode est utilisée pour convertir les entités de base de données en objets
        // métier.
        fun List<NutrientValueEntity>.toNutrientValueMap():
                MutableMap<Nutrient, fr.vetbrain.vetnutri_mp.Data.NutrientQuantity> {
                println(
                        "DEBUG Mappers: Conversion de ${this.size} entités de nutriments en objets métier"
                )
                val result = mutableMapOf<Nutrient, fr.vetbrain.vetnutri_mp.Data.NutrientQuantity>()

                if (this.isEmpty()) {
                        println("DEBUG Mappers: ATTENTION - Liste d'entités de nutriments vide")
                        return result
                }

                var successCount = 0
                var errorCount = 0

                this.forEach { entity ->
                        try {
                                println(
                                        "DEBUG Mappers: Traitement de l'entité - label=${entity.nutrientLabel}, value=${entity.value}"
                                )

                                // Vérification du label du nutriment
                                if (entity.nutrientLabel.isBlank()) {
                                        println(
                                                "DEBUG Mappers: ERREUR - Le label du nutriment est vide"
                                        )
                                        errorCount++
                                        return@forEach
                                }

                                val nutrient =
                                        NutrientResolver.AllNutrientResolver(entity.nutrientLabel)
                                if (nutrient != null) {
                                        result[nutrient] =
                                                fr.vetbrain.vetnutri_mp.Data.NutrientQuantity(
                                                        entity.value,
                                                        entity.nutrientLabel
                                                )
                                        println(
                                                "DEBUG Mappers: Nutriment résolu: ${entity.nutrientLabel} -> ${nutrient.label}"
                                        )
                                        successCount++
                                } else {
                                        println(
                                                "DEBUG Mappers: ERREUR - Impossible de résoudre le nutriment pour le label '${entity.nutrientLabel}'"
                                        )
                                        errorCount++
                                }
                        } catch (e: Exception) {
                                println(
                                        "DEBUG Mappers: ERREUR lors de la conversion de l'entité ${entity.nutrientLabel}: ${e.message}"
                                )
                                e.printStackTrace()
                                errorCount++
                        }
                }

                println(
                        "DEBUG Mappers: Résultat de la conversion: $successCount réussies, $errorCount échecs"
                )
                return result
        }
        // FIN ZONE PROTÉGÉE

        /** Extensions pour la conversion entre Equation et EquationEntity */
        fun Equation.toEntity(): EquationEntity {
                println(
                        "DEBUG Mappers: Conversion de Equation en EquationEntity - UUID: ${this.uuid}"
                )
                println("DEBUG Mappers: Nom: ${this.name}")
                println("DEBUG Mappers: Description: ${this.description}")
                println("DEBUG Mappers: Script: ${this.equationScript}")
                println("DEBUG Mappers: Type: ${this.kind.name}")
                println("DEBUG Mappers: Espèce: ${this.specie?.name}")
                println("DEBUG Mappers: Bib UUID: ${this.bib.uuid}")
                println("DEBUG Mappers: Bib firstAuthor: ${this.bib.firstAuthor}")

                val bibRef = if (this.bib.uuid.isNotEmpty()) this.bib.uuid else null
                println("DEBUG Mappers: bibRef final: $bibRef")

                return EquationEntity(
                        uuid = this.uuid,
                        name = this.name,
                        description = this.description,
                        equationScript = this.equationScript,
                        kind = this.kind.name,
                        specie = this.specie?.name,
                        bibRef = bibRef,
                        variables = "",
                        consistent = this.consistent
                )
        }

        fun EquationEntity.toDomain(biblioRef: BiblioRef? = null): Equation {
                println(
                        "DEBUG Mappers: Conversion de EquationEntity en Equation - UUID: ${this.uuid}"
                )
                println("DEBUG Mappers: Nom: ${this.name}")
                println("DEBUG Mappers: Description: ${this.description}")
                println("DEBUG Mappers: Script: ${this.equationScript}")
                println("DEBUG Mappers: Kind: ${this.kind}")
                println("DEBUG Mappers: Specie: ${this.specie}")
                println("DEBUG Mappers: BibRef ID: ${this.bibRef}")
                println("DEBUG Mappers: BiblioRef fourni: ${biblioRef?.uuid}")

                return Equation(
                        uuid = this.uuid,
                        name = this.name ?: "",
                        description = this.description ?: "",
                        equationScript = this.equationScript ?: "",
                        kind = EquationKind.valueOf(this.kind ?: EquationKind.ENERGYNEED.name),
                        specie = this.specie?.let { Espece.valueOf(it) },
                        bib = biblioRef ?: BiblioRef(),
                        consistent = this.consistent,
                        variables = mutableListOf()
                )
        }

        // BiblioRef Mappers
        fun BiblioRef.toEntity(): BiblioRefEntity {
                return BiblioRefEntity(
                        uuid = this.uuid,
                        firstAuthor = this.firstAuthor,
                        year = this.year,
                        completeRef = this.completeRef,
                        comments = this.comments,
                        bibtex = this.bibtex,
                        consistent = this.consistent
                )
        }

        fun BiblioRefEntity.toDomain(): BiblioRef {
                return BiblioRef(
                        uuid = this.uuid,
                        firstAuthor = this.firstAuthor ?: "",
                        year = this.year ?: 0,
                        completeRef = this.completeRef ?: "",
                        comments = this.comments ?: "",
                        bibtex = this.bibtex ?: "",
                        consistent = this.consistent
                )
        }

        /** Convertit un objet ReferenceEv en ReferenceEvEntity */
        fun ReferenceEv.toEntity(): ReferenceEvEntity {
                return ReferenceEvEntity(
                        uuid = this.uuid,
                        nom = this.nom,
                        description = this.description,
                        maladie = this.maladie,
                        nomMaladie = this.nomMaladie,
                        nomEnergie = this.nomEnergie,
                        consistent = if (this.consistent) 1 else 0,
                        espece = this.espece.name,
                        stadePhysio = this.stadePhysio.name,
                        nomk1 = this.nomk1,
                        nomk2 = this.nomk2,
                        nomk3 = this.nomk3,
                        nomk4 = this.nomk4,
                        nomk5 = this.nomk5,
                        equationBW = this.equationBW?.uuid,
                        equationBEE = this.equationBEE?.uuid,
                        equationDEcom = this.equationDEcom?.uuid,
                        equationDEraw = this.equationDEraw?.uuid,
                        equationME = this.equationME?.uuid
                )
        }

        /**
         * Convertit un objet ReferenceEvEntity en ReferenceEv Nécessite des appels supplémentaires
         * pour remplir les listes de coefficients et de valeurs nutritionnelles
         */
        suspend fun ReferenceEvEntity.toDomain(
                equationRepository: EquationRepository? = null,
                coefficientDao: CoefficientDao? = null,
                nutrientReferenceDao: NutrientReferenceDao? = null
        ): ReferenceEv {
                // Conversion des équations référencées
                val bwEquation =
                        if (equationBW != null && equationRepository != null) {
                                equationRepository.getEquationById(equationBW)
                        } else null

                val beeEquation =
                        if (equationBEE != null && equationRepository != null) {
                                equationRepository.getEquationById(equationBEE)
                        } else null

                val decomEquation =
                        if (equationDEcom != null && equationRepository != null) {
                                equationRepository.getEquationById(equationDEcom)
                        } else null

                val derawEquation =
                        if (equationDEraw != null && equationRepository != null) {
                                equationRepository.getEquationById(equationDEraw)
                        } else null

                val meEquation =
                        if (equationME != null && equationRepository != null) {
                                equationRepository.getEquationById(equationME)
                        } else null

                val referenceEv =
                        ReferenceEv(
                                uuid = uuid,
                                nom = nom,
                                description = description,
                                maladie = maladie,
                                nomMaladie = nomMaladie,
                                nomEnergie = nomEnergie,
                                consistent = consistent == 1,
                                espece = Espece.valueOf(espece),
                                stadePhysio = StadePhysio.valueOf(stadePhysio),
                                nomk1 = nomk1,
                                nomk2 = nomk2,
                                nomk3 = nomk3,
                                nomk4 = nomk4,
                                nomk5 = nomk5,
                                equationBW = bwEquation,
                                equationBEE = beeEquation,
                                equationDEcom = decomEquation,
                                equationDEraw = derawEquation,
                                equationME = meEquation
                        )

                // Charger les coefficients si le DAO est fourni
                if (coefficientDao != null) {
                        val allCoefficients = coefficientDao.getCoefficientsByReferenceId(uuid)

                        // Grouper par groupe de coefficient
                        val groupedCoefficients = allCoefficients.groupBy { it.groupUUID }

                        // Mapper et ajouter les coefficients par groupe
                        for (groupId in 0..4) {
                                val groupCoefficients = groupedCoefficients[groupId] ?: emptyList()
                                val coefPList = groupCoefficients.map { it.toDomain() }

                                when (groupId) {
                                        0 -> referenceEv.modk1.addAll(coefPList)
                                        1 -> referenceEv.modk2.addAll(coefPList)
                                        2 -> referenceEv.modk3.addAll(coefPList)
                                        3 -> referenceEv.modk4.addAll(coefPList)
                                        4 -> referenceEv.modk5.addAll(coefPList)
                                }
                        }
                }

                // Charger les valeurs nutritionnelles si le DAO est fourni
                if (nutrientReferenceDao != null) {
                        val nutritionValues = nutrientReferenceDao.getNutrientsByReferenceId(uuid)
                        val mappedValues = nutritionValues.map { it.toDomain() }
                        referenceEv.nut4RefList.addAll(mappedValues)
                }

                return referenceEv
        }

        /** Convertit un objet CoefP en CoefficientEntity */
        fun CoefP.toEntity(referenceId: String, groupId: Int): CoefficientEntity {
                return CoefficientEntity(
                        uuid = this.uuid,
                        referenceId = referenceId,
                        groupUUID = groupId,
                        description = this.description,
                        coef = this.coef
                )
        }

        /** Convertit un objet CoefficientEntity en CoefP */
        fun CoefficientEntity.toDomain(): CoefP {
                return CoefP(uuid = uuid, description = description, coef = coef)
        }

        /** Convertit un objet Nut4Ref en NutrientReferenceEntity */
        fun Nut4Ref.toEntity(referenceId: String): NutrientReferenceEntity {
                return NutrientReferenceEntity(
                        uuid = this.uuid,
                        referenceId = referenceId,
                        nutrient = this.nutrient.name,
                        niveauRef = this.niveauRef.name,
                        quantite = this.quantite,
                        unite = this.unite.name,
                        uniteReq = this.uniteReq.name,
                        biblioRefId = this.citation?.uuid
                )
        }

        /** Convertit un objet NutrientReferenceEntity en Nut4Ref */
        suspend fun NutrientReferenceEntity.toDomain(biblioRefDao: BiblioRefDao? = null): Nut4Ref {
                // Récupérer la référence bibliographique si disponible
                val biblioRef =
                        if (biblioRefId != null && biblioRefDao != null) {
                                biblioRefDao.getBiblioRefById(biblioRefId)?.toDomain()
                        } else null

                return Nut4Ref(
                        uuid = uuid,
                        nutrient = Nutrient.valueOf(nutrient),
                        niveauRef = Reflevel.valueOf(niveauRef),
                        quantite = quantite,
                        unite = UnitEnum.valueOf(unite),
                        uniteReq = UnitReqEnum.valueOf(uniteReq),
                        citation = biblioRef
                )
        }

        /** Récupère tous les coefficients pour un groupe spécifique d'une référence */
        suspend fun getCoefficientsForGroup(
                coefficientDao: CoefficientDao,
                referenceId: String,
                groupId: Int
        ): List<CoefP> {
                val coefficients = coefficientDao.getCoefficientsByGroup(referenceId, groupId)
                return coefficients.map { it.toDomain() }
        }

        /** Met à jour les coefficients pour un groupe spécifique d'une référence */
        suspend fun updateCoefficientsForGroup(
                coefficientDao: CoefficientDao,
                referenceId: String,
                groupId: Int,
                coefficients: List<CoefP>
        ) {
                // Supprimer les anciens coefficients
                coefficientDao.deleteCoefficientsForGroup(referenceId, groupId)

                // Convertir et insérer les nouveaux coefficients
                val entities = coefficients.map { it.toEntity(referenceId, groupId) }
                coefficientDao.insertCoefficients(entities)
        }
}
