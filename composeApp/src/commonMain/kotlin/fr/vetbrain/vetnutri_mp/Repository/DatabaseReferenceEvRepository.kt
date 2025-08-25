package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.DataBase.*
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum

/** Repository pour la persistance des références évaluées avec Room Multiplatform */
class DatabaseReferenceEvRepository(
        private val referenceEvDao: ReferenceEvDao,
        private val equationDao: EquationDao,
        private val biblioRefDao: BiblioRefDao
) {

    // Méthodes principales du repository

    suspend fun getAllReferenceEv(): List<ReferenceEv> {
        return try {
            val entities = referenceEvDao.getAllReferenceEv()
            entities.map { convertEntityToReferenceEv(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getReferenceEvById(id: String): ReferenceEv? {
        return try {
            val entity = referenceEvDao.getReferenceEvById(id)
            entity?.let { convertEntityToReferenceEv(it) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveReferenceEv(referenceEv: ReferenceEv): String {
        try {
            // 1. Sauvegarder l'entité principale
            val entity = convertReferenceEvToEntity(referenceEv)
            referenceEvDao.insertReferenceEv(entity)

            // 2. Sauvegarder les relations avec les équations
            saveEquationRelations(referenceEv)

            // 3. Sauvegarder les coefficients
            saveCoefficients(referenceEv)

            // 4. Sauvegarder les nutriments
            saveNutrients(referenceEv)

            return referenceEv.uuid
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateReferenceEv(referenceEv: ReferenceEv) {
        try {
            // 1. Mettre à jour l'entité principale
            val entity = convertReferenceEvToEntity(referenceEv)
            referenceEvDao.updateReferenceEv(entity)

            // 2. Supprimer les anciennes relations
            referenceEvDao.deleteEquationsForReference(referenceEv.uuid)
            referenceEvDao.deleteCoefficientsForReference(referenceEv.uuid)
            referenceEvDao.deleteNutrientsForReference(referenceEv.uuid)

            // 3. Sauvegarder les nouvelles relations
            saveEquationRelations(referenceEv)
            saveCoefficients(referenceEv)
            saveNutrients(referenceEv)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteReferenceEv(id: String) {
        try {
            referenceEvDao.deleteReferenceEvById(id)
        } catch (e: Exception) {
            throw e
        }
    }

    // 🆕 Méthode publique pour sauvegarder un coefficient individuel
    suspend fun saveCoefficient(coefficient: ReferenceEvCoefficientEntity) {
        try {
            referenceEvDao.insertCoefficient(coefficient)
        } catch (e: Exception) {
            throw e
        }
    }

    // 🆕 Méthode spéciale pour l'import qui gère les coefficients avec leurs UUIDs originaux
    suspend fun saveReferenceEvForImport(referenceEv: ReferenceEv): String {
        try {
            // 1. Sauvegarder l'entité principale
            val entity = convertReferenceEvToEntity(referenceEv)
            referenceEvDao.insertReferenceEv(entity)

            // 2. Sauvegarder les relations avec les équations
            saveEquationRelations(referenceEv)

            // 3. 🆕 Sauvegarder les coefficients AVEC leurs UUIDs originaux
            saveCoefficientsForImport(referenceEv)

            // 4. Sauvegarder les nutriments
            saveNutrients(referenceEv)

            return referenceEv.uuid
        } catch (e: Exception) {
            throw e
        }
    }

    // 🆕 Méthode pour sauvegarder les coefficients avec leurs UUIDs originaux
    private suspend fun saveCoefficientsForImport(referenceEv: ReferenceEv) {
        val coefficients = mutableListOf<ReferenceEvCoefficientEntity>()

        // Sauvegarder les coefficients k1-k5 avec leurs UUIDs originaux
        referenceEv.getModk1().forEach { coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = coef.uuid, // ✅ UUID original préservé
                            referenceEvId = referenceEv.uuid,
                            groupType = "k1",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 0
                    )
            )
        }

        referenceEv.getModk2().forEach { coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = coef.uuid, // ✅ UUID original préservé
                            referenceEvId = referenceEv.uuid,
                            groupType = "k2",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 1
                    )
            )
        }

        referenceEv.getModk3().forEach { coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = coef.uuid, // ✅ UUID original préservé
                            referenceEvId = referenceEv.uuid,
                            groupType = "k3",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 2
                    )
            )
        }

        referenceEv.getModk4().forEach { coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = coef.uuid, // ✅ UUID original préservé
                            referenceEvId = referenceEv.uuid,
                            groupType = "k4",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 3
                    )
            )
        }

        referenceEv.getModk5().forEach { coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = coef.uuid, // ✅ UUID original préservé
                            referenceEvId = referenceEv.uuid,
                            groupType = "k5",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 4
                    )
            )
        }

        coefficients.forEach { coefficient -> referenceEvDao.insertCoefficient(coefficient) }
    }

    // Méthodes de conversion

    private fun convertReferenceEvToEntity(referenceEv: ReferenceEv): ReferenceEvEntity {
        return ReferenceEvEntity(
                uuid = referenceEv.uuid,
                nom = referenceEv.nom,
                description = referenceEv.description,
                maladie = referenceEv.maladie,
                nomMaladie = referenceEv.nomMaladie,
                nomEnergie = referenceEv.nomEnergie,
                consistent = referenceEv.consistent,
                espece = referenceEv.espece.name,
                stadePhysio = referenceEv.stadePhysio.name,
                nomk1 = referenceEv.nomk1,
                nomk2 = referenceEv.nomk2,
                nomk3 = referenceEv.nomk3,
                nomk4 = referenceEv.nomk4,
                nomk5 = referenceEv.nomk5
        )
    }

    private suspend fun convertEntityToReferenceEv(entity: ReferenceEvEntity): ReferenceEv {
        val referenceEv =
                ReferenceEv(
                        uuid = entity.uuid,
                        nom = entity.nom,
                        description = entity.description,
                        maladie = entity.maladie,
                        nomMaladie = entity.nomMaladie,
                        nomEnergie = entity.nomEnergie,
                        consistent = entity.consistent,
                        espece = Espece.valueOf(entity.espece),
                        stadePhysio = StadePhysio.valueOf(entity.stadePhysio)
                )

        // Assigner les noms des coefficients
        referenceEv.nomk1 = entity.nomk1
        referenceEv.nomk2 = entity.nomk2
        referenceEv.nomk3 = entity.nomk3
        referenceEv.nomk4 = entity.nomk4
        referenceEv.nomk5 = entity.nomk5

        // Charger les équations associées
        loadEquationsForReference(referenceEv)

        // Charger les coefficients
        loadCoefficientsForReference(referenceEv)

        // Charger les nutriments
        loadNutrientsForReference(referenceEv)

        return referenceEv
    }

    // Méthodes de compatibilité avec ReferenceEvRepository
    suspend fun getAll(): List<ReferenceEv> {
        return getAllReferenceEv()
    }

    suspend fun getById(id: String): ReferenceEv? {
        return getReferenceEvById(id)
    }

    suspend fun create(reference: ReferenceEv): Boolean {
        return try {
            saveReferenceEv(reference)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun update(reference: ReferenceEv): Boolean {
        return try {
            updateReferenceEv(reference)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun delete(id: String): Boolean {
        return try {
            deleteReferenceEv(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vide entièrement la base de données des références nutritionnelles
     * @return Le nombre de références supprimées
     */
    suspend fun clearAllReferences(): Int {
        println("DEBUG DatabaseReferenceEvRepository: clearAllReferences() démarrée")

        return try {
            // Obtenir le nombre total de références avant suppression
            val allReferences = getAllReferenceEv()
            val count = allReferences.size

            if (count > 0) {
                referenceEvDao.deleteAllEquationRelations()

                referenceEvDao.deleteAllCoefficients()

                referenceEvDao.deleteAllNutrients()

                // Supprimer toutes les références
                allReferences.forEach { reference -> deleteReferenceEv(reference.uuid) }
            }

            count
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // Méthodes de délégation pour les équations (compatibilité avec EquationViewModel)
    suspend fun updateEquationBW(referenceId: String, equation: Equation): Boolean {
        return try {
            val reference = getReferenceEvById(referenceId)
            if (reference != null) {
                reference.equationBW = equation
                updateReferenceEv(reference)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEquationBEE(referenceId: String, equation: Equation): Boolean {
        return try {
            val reference = getReferenceEvById(referenceId)
            if (reference != null) {
                reference.equationBEE = equation
                updateReferenceEv(reference)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEquationDEcom(referenceId: String, equation: Equation): Boolean {
        return try {
            val reference = getReferenceEvById(referenceId)
            if (reference != null) {
                reference.equationDEcom = equation
                updateReferenceEv(reference)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEquationDEraw(referenceId: String, equation: Equation): Boolean {
        return try {
            val reference = getReferenceEvById(referenceId)
            if (reference != null) {
                reference.equationDEraw = equation
                updateReferenceEv(reference)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEquationME(referenceId: String, equation: Equation): Boolean {
        return try {
            val reference = getReferenceEvById(referenceId)
            if (reference != null) {
                reference.equationME = equation
                updateReferenceEv(reference)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Méthodes privées pour sauvegarder les relations

    private suspend fun saveEquationRelations(referenceEv: ReferenceEv) {
        val relations = mutableListOf<ReferenceEvEquationEntity>()

        referenceEv.equationBW?.let { equation ->
            relations.add(
                    ReferenceEvEquationEntity(
                            referenceEvId = referenceEv.uuid,
                            equationId = equation.uuid,
                            equationType = "BW"
                    )
            )
        }

        referenceEv.equationBEE?.let { equation ->
            relations.add(
                    ReferenceEvEquationEntity(
                            referenceEvId = referenceEv.uuid,
                            equationId = equation.uuid,
                            equationType = "BEE"
                    )
            )
        }

        referenceEv.equationDEcom?.let { equation ->
            relations.add(
                    ReferenceEvEquationEntity(
                            referenceEvId = referenceEv.uuid,
                            equationId = equation.uuid,
                            equationType = "DEcom"
                    )
            )
        }

        referenceEv.equationDEraw?.let { equation ->
            relations.add(
                    ReferenceEvEquationEntity(
                            referenceEvId = referenceEv.uuid,
                            equationId = equation.uuid,
                            equationType = "DEraw"
                    )
            )
        }

        referenceEv.equationME?.let { equation ->
            relations.add(
                    ReferenceEvEquationEntity(
                            referenceEvId = referenceEv.uuid,
                            equationId = equation.uuid,
                            equationType = "ME"
                    )
            )
        }

        // Équations nutritionnelles additionnelles (complémentaires, etc.)
        // Nous utilisons equationType = uuid de l'équation pour permettre plusieurs lignes
        if (referenceEv.equationsNut.isNotEmpty()) {
            referenceEv.equationsNut.forEach { equation ->
                relations.add(
                        ReferenceEvEquationEntity(
                                referenceEvId = referenceEv.uuid,
                                equationId = equation.uuid,
                                equationType = equation.uuid
                        )
                )
            }
        }

        relations.forEach { relation -> referenceEvDao.insertEquationRelation(relation) }
    }

    private suspend fun saveCoefficients(referenceEv: ReferenceEv) {
        val coefficients = mutableListOf<ReferenceEvCoefficientEntity>()

        // Sauvegarder les coefficients k1-k5
        referenceEv.getModk1().forEachIndexed { index, coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = "${referenceEv.uuid}_k1_$index",
                            referenceEvId = referenceEv.uuid,
                            groupType = "k1",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 0
                    )
            )
        }

        referenceEv.getModk2().forEachIndexed { index, coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = "${referenceEv.uuid}_k2_$index",
                            referenceEvId = referenceEv.uuid,
                            groupType = "k2",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 1
                    )
            )
        }

        referenceEv.getModk3().forEachIndexed { index, coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = "${referenceEv.uuid}_k3_$index",
                            referenceEvId = referenceEv.uuid,
                            groupType = "k3",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 2
                    )
            )
        }

        referenceEv.getModk4().forEachIndexed { index, coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = "${referenceEv.uuid}_k4_$index",
                            referenceEvId = referenceEv.uuid,
                            groupType = "k4",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 3
                    )
            )
        }

        referenceEv.getModk5().forEachIndexed { index, coef ->
            coefficients.add(
                    ReferenceEvCoefficientEntity(
                            uuid = "${referenceEv.uuid}_k5_$index",
                            referenceEvId = referenceEv.uuid,
                            groupType = "k5",
                            description = coef.description ?: "Normal",
                            coef = coef.coef ?: 1.0,
                            groupUUID = coef.groupUUID ?: 4
                    )
            )
        }

        coefficients.forEach { coefficient -> referenceEvDao.insertCoefficient(coefficient) }
    }

    private suspend fun saveNutrients(referenceEv: ReferenceEv) {
        val nutrients = mutableListOf<ReferenceEvNutrientEntity>()

        // Sauvegarder les nutriments MIN
        for ((nutrient, nut4Ref) in referenceEv.getRefMapMin()) {
            nutrients.add(
                    ReferenceEvNutrientEntity(
                            uuid = "${referenceEv.uuid}_${nutrient.label}_MIN",
                            referenceEvId = referenceEv.uuid,
                            nutrientCode = nutrient.label,
                            reflevel = "MIN",
                            quantite = nut4Ref.quantite,
                            uniteId = nut4Ref.unite.getID(),
                            uniteReqId = nut4Ref.uniteReq.getID(),
                            biblioRefId = nut4Ref.biblio?.uuid
                    )
            )
        }

        // Sauvegarder les nutriments MAX
        for ((nutrient, nut4Ref) in referenceEv.getRefMapMax()) {
            nutrients.add(
                    ReferenceEvNutrientEntity(
                            uuid = "${referenceEv.uuid}_${nutrient.label}_MAX",
                            referenceEvId = referenceEv.uuid,
                            nutrientCode = nutrient.label,
                            reflevel = "MAX",
                            quantite = nut4Ref.quantite,
                            uniteId = nut4Ref.unite.getID(),
                            uniteReqId = nut4Ref.uniteReq.getID(),
                            biblioRefId = nut4Ref.biblio?.uuid
                    )
            )
        }

        // Sauvegarder les nutriments OPTIMIN
        for ((nutrient, nut4Ref) in referenceEv.getRefMapOMin()) {
            nutrients.add(
                    ReferenceEvNutrientEntity(
                            uuid = "${referenceEv.uuid}_${nutrient.label}_OPTIMIN",
                            referenceEvId = referenceEv.uuid,
                            nutrientCode = nutrient.label,
                            reflevel = "OPTIMIN",
                            quantite = nut4Ref.quantite,
                            uniteId = nut4Ref.unite.getID(),
                            uniteReqId = nut4Ref.uniteReq.getID(),
                            biblioRefId = nut4Ref.biblio?.uuid
                    )
            )
        }

        // Sauvegarder les nutriments OPTIMAX
        for ((nutrient, nut4Ref) in referenceEv.getRefMapOMax()) {
            nutrients.add(
                    ReferenceEvNutrientEntity(
                            uuid = "${referenceEv.uuid}_${nutrient.label}_OPTIMAX",
                            referenceEvId = referenceEv.uuid,
                            nutrientCode = nutrient.label,
                            reflevel = "OPTIMAX",
                            quantite = nut4Ref.quantite,
                            uniteId = nut4Ref.unite.getID(),
                            uniteReqId = nut4Ref.uniteReq.getID(),
                            biblioRefId = nut4Ref.biblio?.uuid
                    )
            )
        }

        nutrients.forEach { nutrient -> referenceEvDao.insertNutrient(nutrient) }
    }

    // Méthodes privées pour charger les relations

    private suspend fun loadEquationsForReference(referenceEv: ReferenceEv) {
        val equationRelations = referenceEvDao.getEquationsForReference(referenceEv.uuid)

        for (relation in equationRelations) {
            val equation = equationDao.getEquationById(relation.equationId)
            if (equation != null) {
                val equationObj = convertEquationEntityToEquation(equation)
                when (relation.equationType) {
                    "BW" -> referenceEv.equationBW = equationObj
                    "BEE" -> referenceEv.equationBEE = equationObj
                    "DEcom" -> referenceEv.equationDEcom = equationObj
                    "DEraw" -> referenceEv.equationDEraw = equationObj
                    "ME" -> referenceEv.equationME = equationObj
                    else -> {
                        // Toute autre valeur d'equationType est considérée comme équation
                        // nutritionnelle associée
                        if (referenceEv.equationsNut.none { it.uuid == equationObj.uuid }) {
                            referenceEv.equationsNut.add(equationObj)
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadCoefficientsForReference(referenceEv: ReferenceEv) {
        val coefficients = referenceEvDao.getCoefficientsForReference(referenceEv.uuid)

        // Grouper par type de coefficient
        val coefficientsByGroup = coefficients.groupBy { it.groupType }

        // Charger chaque groupe
        coefficientsByGroup["k1"]?.let { coefList ->
            referenceEv.getModk1().clear()
            for (coefEntity in coefList) {
                referenceEv
                        .getModk1()
                        .add(
                                CoefP(
                                        uuid = coefEntity.uuid,
                                        description = coefEntity.description,
                                        coef = coefEntity.coef,
                                        groupUUID = coefEntity.groupUUID
                                )
                        )
            }
        }

        coefficientsByGroup["k2"]?.let { coefList ->
            referenceEv.getModk2().clear()
            for (coefEntity in coefList) {
                referenceEv
                        .getModk2()
                        .add(
                                CoefP(
                                        uuid = coefEntity.uuid,
                                        description = coefEntity.description,
                                        coef = coefEntity.coef,
                                        groupUUID = coefEntity.groupUUID
                                )
                        )
            }
        }

        coefficientsByGroup["k3"]?.let { coefList ->
            referenceEv.getModk3().clear()
            for (coefEntity in coefList) {
                referenceEv
                        .getModk3()
                        .add(
                                CoefP(
                                        uuid = coefEntity.uuid,
                                        description = coefEntity.description,
                                        coef = coefEntity.coef,
                                        groupUUID = coefEntity.groupUUID
                                )
                        )
            }
        }

        coefficientsByGroup["k4"]?.let { coefList ->
            referenceEv.getModk4().clear()
            for (coefEntity in coefList) {
                referenceEv
                        .getModk4()
                        .add(
                                CoefP(
                                        uuid = coefEntity.uuid,
                                        description = coefEntity.description,
                                        coef = coefEntity.coef,
                                        groupUUID = coefEntity.groupUUID
                                )
                        )
            }
        }

        coefficientsByGroup["k5"]?.let { coefList ->
            referenceEv.getModk5().clear()
            for (coefEntity in coefList) {
                referenceEv
                        .getModk5()
                        .add(
                                CoefP(
                                        uuid = coefEntity.uuid,
                                        description = coefEntity.description,
                                        coef = coefEntity.coef,
                                        groupUUID = coefEntity.groupUUID
                                )
                        )
            }
        }
    }

    private suspend fun loadNutrientsForReference(referenceEv: ReferenceEv) {
        val nutrients = referenceEvDao.getNutrientsForReference(referenceEv.uuid)
        
        // 🔍 LOG DIAGNOSTIC : Vérification des nutriments en base
        println("🔍 DIAGNOSTIC LOAD: Chargement des nutriments pour ${referenceEv.nom} (${referenceEv.uuid})")
        println("🔍 DIAGNOSTIC LOAD: ${nutrients.size} nutriments trouvés en base")
        nutrients.forEach { nut ->
            println("  - ${nut.nutrientCode} (${nut.reflevel}): ${nut.quantite}")
        }

        // Grouper par niveau de référence
        val nutrientsByLevel = nutrients.groupBy { it.reflevel }

        // Charger chaque niveau
        nutrientsByLevel["MIN"]?.let { nutList ->
            for (nutEntity in nutList) {
                val nutrient = findNutrientByLabel(nutEntity.nutrientCode)
                if (nutrient != null) {
                    val biblio =
                            if (nutEntity.biblioRefId != null) {
                                convertBiblioRefByIdToBiblioRef(nutEntity.biblioRefId)
                            } else null

                    referenceEv.definirNutriment(
                            nutEntity.quantite,
                            nutrient,
                            Reflevel.MIN,
                            UnitReqEnum.getById(nutEntity.uniteReqId),
                            biblio ?: BiblioRef()
                    )
                }
            }
        }

        nutrientsByLevel["MAX"]?.let { nutList ->
            for (nutEntity in nutList) {
                val nutrient = findNutrientByLabel(nutEntity.nutrientCode)
                if (nutrient != null) {
                    val biblio =
                            if (nutEntity.biblioRefId != null) {
                                convertBiblioRefByIdToBiblioRef(nutEntity.biblioRefId)
                            } else null

                    referenceEv.definirNutriment(
                            nutEntity.quantite,
                            nutrient,
                            Reflevel.MAX,
                            UnitReqEnum.getById(nutEntity.uniteReqId),
                            biblio ?: BiblioRef()
                    )
                }
            }
        }

        nutrientsByLevel["OPTIMIN"]?.let { nutList ->
            for (nutEntity in nutList) {
                val nutrient = findNutrientByLabel(nutEntity.nutrientCode)
                if (nutrient != null) {
                    val biblio =
                            if (nutEntity.biblioRefId != null) {
                                convertBiblioRefByIdToBiblioRef(nutEntity.biblioRefId)
                            } else null

                    referenceEv.definirNutriment(
                            nutEntity.quantite,
                            nutrient,
                            Reflevel.OPTIMIN,
                            UnitReqEnum.getById(nutEntity.uniteReqId),
                            biblio ?: BiblioRef()
                    )
                }
            }
        }

        nutrientsByLevel["OPTIMAX"]?.let { nutList ->
            for (nutEntity in nutList) {
                val nutrient = findNutrientByLabel(nutEntity.nutrientCode)
                if (nutrient != null) {
                    val biblio =
                            if (nutEntity.biblioRefId != null) {
                                convertBiblioRefByIdToBiblioRef(nutEntity.biblioRefId)
                            } else null

                    referenceEv.definirNutriment(
                            nutEntity.quantite,
                            nutrient,
                            Reflevel.OPTIMAX,
                            UnitReqEnum.getById(nutEntity.uniteReqId),
                            biblio ?: BiblioRef()
                    )
                }
            }
        }
    }

    // Méthodes utilitaires pour les conversions

    private fun convertEquationEntityToEquation(entity: EquationEntity): Equation {
        return Equation(
                uuid = entity.uuid,
                name = entity.name,
                description = entity.description,
                equationScript = entity.equationScript,
                specie = if (entity.specie != null) Espece.valueOf(entity.specie) else null,
                kind = EquationKind.valueOf(entity.kind),
                consistent = entity.consistent,
                bib = BiblioRef() // Temporairement vide pour éviter l'erreur suspend
        )
    }

    private suspend fun convertBiblioRefEntityToBiblioRef(entity: BiblioRefEntity?): BiblioRef {
        return if (entity != null) {
            BiblioRef(
                    uuid = entity.uuid,
                    firstAuthor = entity.firstAuthor,
                    year = entity.year,
                    completeRef = entity.completeRef,
                    comments = entity.comments,
                    bibtex = entity.bibtex,
                    consistent = entity.consistent
            )
        } else {
            BiblioRef()
        }
    }

    private suspend fun convertBiblioRefByIdToBiblioRef(biblioRefId: String?): BiblioRef {
        return if (biblioRefId != null) {
            val entity = biblioRefDao.getBiblioRefById(biblioRefId)
            convertBiblioRefEntityToBiblioRef(entity)
        } else {
            BiblioRef()
        }
    }

    private fun findNutrientByLabel(label: String): Nutrient? {
        // Chercher dans tous les types de nutriments
        return try {
            // Essayer les différents types de nutriments
            fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.entries.find { it.label == label }
                    ?: fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.entries.find {
                        it.label == label
                    }
                            ?: fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.entries.find {
                        it.label == label
                    }
                            ?: fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.entries.find {
                        it.label == label
                    }
                            ?: fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.entries.find {
                        it.label == label
                    }
                            ?: fr.vetbrain.vetnutri_mp.Enumer.AAEnum.entries.find {
                        it.label == label
                    }
                            ?: fr.vetbrain.vetnutri_mp.Enumer.NutrientOther.entries.find {
                        it.label == label
                    }
                            ?: fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis.entries.find {
                        it.label == label
                    }
        } catch (e: Exception) {
            null
        }
    }
}
