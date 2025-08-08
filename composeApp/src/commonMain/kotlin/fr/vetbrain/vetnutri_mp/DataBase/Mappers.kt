package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
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
                                MCS = this.MCS ?: 0,
                                // Nouveaux champs pour les références nutritionnelles
                                referenceGeneraleId = this.referenceGeneraleId,
                                referencesMaladiesJson = this.referencesMaladies.joinToString(","),
                                coefficientAjustement = this.coefficientAjustement
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
                        idAnim = this.idAnim,
                        date =
                                if (this.date?.isNotBlank() == true) LocalDate.parse(this.date)
                                else null,
                        objectConsult = this.objectConsult ?: "",
                        observation = this.observation ?: "",
                        cRendu = this.cRendu ?: "",
                        weight = if (this.weight != 0f) this.weight else null,
                        idealWeight = if (this.idealWeight != 0f) this.idealWeight else null,
                        water = if (this.water != 0f) this.water else null,
                        bodyFat = if (this.bodyFat != 0f) this.bodyFat else null,
                        methodAnalysis = this.methodAnalysis ?: "",
                        BCS = if (this.BCS != 0) this.BCS else null,
                        k1Id = if (this.k1Id?.isNotBlank() == true) this.k1Id else null,
                        k1Value = if (this.k1Value != 0f) this.k1Value else null,
                        k2Id = if (this.k2Id?.isNotBlank() == true) this.k2Id else null,
                        k2Value = if (this.k2Value != 0f) this.k2Value else null,
                        k3Id = if (this.k3Id?.isNotBlank() == true) this.k3Id else null,
                        k3Value = if (this.k3Value != 0f) this.k3Value else null,
                        k4Id = if (this.k4Id?.isNotBlank() == true) this.k4Id else null,
                        k4Value = if (this.k4Value != 0f) this.k4Value else null,
                        k5Id = if (this.k5Id?.isNotBlank() == true) this.k5Id else null,
                        k5Value = if (this.k5Value != 0f) this.k5Value else null,
                        nLittle = if (this.nLittle != 0) this.nLittle else null,
                        pAdult = if (this.pAdult != 0f) this.pAdult else null,
                        coefGes = if (this.coefGes != 0) this.coefGes else null,
                        coefLact = if (this.coefLact != 0) this.coefLact else null,
                        MCS = if (this.MCS != 0) this.MCS else null,
                        suppVarp = suppVars.map { it.toData() }.toMutableList(),
                        rations = rations.map { it.toData() }.toMutableList(),
                        // Nouveaux champs pour les références nutritionnelles
                        referenceGeneraleId = this.referenceGeneraleId,
                        referencesMaladies =
                                if (!this.referencesMaladiesJson.isNullOrBlank()) {
                                        this.referencesMaladiesJson.split(",").toMutableList()
                                } else {
                                        mutableListOf()
                                },
                        coefficientAjustement = this.coefficientAjustement
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

                if (!this.especesJson.isNullOrEmpty()) {
                        // Extraire la liste des espèces du JSON
                        val especesStringList = this.especesJson.split(",")

                        // Essayer de convertir chaque espèce en énumération
                        especesStringList.forEach { especeStr ->
                                try {
                                        // Utiliser getFromString qui gère désormais le nettoyage et
                                        // la conversion
                                        val espece = Espece.getFromString(especeStr)
                                        if (espece != null) {

                                                especesList.add(
                                                        espece.name
                                                ) // Utiliser le nom de l'énumération
                                        } else {

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
                        especesList.addAll(
                                especes
                                        .map {
                                                try {
                                                        // Utiliser getFromString qui gère désormais
                                                        // le
                                                        // nettoyage et la conversion
                                                        val espece = Espece.getFromString(it.espece)
                                                        if (espece != null) {
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
                                                        if (cleaned.isNotEmpty()) cleaned else null
                                                }
                                        }
                                        .filterNotNull()
                        )
                }

                val indicatList = mutableListOf<AlimIndic>()

                // Ajouter des logs de débogage pour comprendre le contenu de indicationsJson

                if (!this.indicationsJson.isNullOrEmpty()) {
                        val indicationsStringList = this.indicationsJson.split(",")

                        indicationsStringList.forEach { indic ->
                                try {
                                        // Utiliser getFromString qui gère désormais le nettoyage et
                                        // la conversion
                                        val alimIndic = AlimIndic.getFromString(indic)
                                        if (alimIndic != AlimIndic.AUTRE ||
                                                        indic.trim().uppercase() == "AUTRE"
                                        ) {
                                                indicatList.add(alimIndic)
                                        }
                                } catch (e: Exception) {}
                        }
                } else {
                        indicatList.addAll(
                                indications.mapNotNull { entity ->
                                        try {
                                                val indication =
                                                        AlimIndic.entries.getOrNull(
                                                                entity.indication
                                                        )
                                                if (indication != null) {}
                                                indication
                                        } catch (e: Exception) {
                                                null
                                        }
                                }
                        )
                }

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
                return mapNotNull { (nutrient, nutrientQuantity) ->
                        // Ne créer des entités que pour les valeurs strictement positives
                        if (nutrientQuantity.value > 0) {
                                NutrientValueEntity(
                                        refAliment = alimentUuid,
                                        nutrientLabel = nutrient.label,
                                        value = nutrientQuantity.value
                                )
                        } else {
                                null
                        }
                }
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
                val result = mutableMapOf<Nutrient, fr.vetbrain.vetnutri_mp.Data.NutrientQuantity>()
                forEach { entity ->
                        val nutrient = NutrientResolver.AllNutrientResolver(entity.nutrientLabel)
                        if (nutrient != null) {
                                result[nutrient] =
                                        fr.vetbrain.vetnutri_mp.Data.NutrientQuantity(
                                                entity.value,
                                                entity.nutrientLabel
                                        )
                        }
                }
                return result
        }
        // FIN ZONE PROTÉGÉE

        /** Extensions pour la conversion entre Equation et EquationEntity */
        fun Equation.toEntity(): EquationEntity {

                val bibRef = if (this.bib.uuid.isNotEmpty()) this.bib.uuid else null

                return EquationEntity(
                        uuid = this.uuid,
                        name = this.name,
                        description = this.description,
                        equationScript = this.equationScript,
                        specie = this.specie?.name,
                        kind = this.kind.name,
                        consistent = this.consistent,
                        bibRef = bibRef,
                        variables = "",
                        nutrient = this.nutrient?.label,
                        ratio = this.ratio
                )
        }

        fun EquationEntity.toDomain(biblioRef: BiblioRef? = null): Equation {

                // Résoudre le nutriment à partir de son label
                val nutrient = this.nutrient?.let { NutrientResolver.AllNutrientResolver(it) }

                return Equation(
                        uuid = this.uuid,
                        name = this.name ?: "",
                        description = this.description ?: "",
                        equationScript = this.equationScript ?: "",
                        kind = EquationKind.valueOf(this.kind ?: EquationKind.ENERGYNEED.name),
                        specie = this.specie?.let { Espece.valueOf(it) },
                        nutrient = nutrient,
                        bib = biblioRef ?: BiblioRef(),
                        consistent = this.consistent,
                        ratio = this.ratio,
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

        // Ajouter les mappers manquants
        fun SupplementalVariableEntity.toData(): SupplementalvariableP {
                return SupplementalvariableP(
                        variable = VariableKind.getById(this.variableKind),
                        varue = this.value
                )
        }
}
