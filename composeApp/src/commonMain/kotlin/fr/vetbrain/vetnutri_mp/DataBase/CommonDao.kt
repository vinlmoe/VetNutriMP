package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AnimalDao {
        @Insert suspend fun insert(animal: AnimalEntity)

        @Update suspend fun update(animal: AnimalEntity)

        @Delete suspend fun delete(animal: AnimalEntity)

        @Query("SELECT * FROM animals") suspend fun getAllAnimals(): List<AnimalEntity>

        @Query("SELECT * FROM animals WHERE uuid = :id")
        suspend fun getAnimalById(id: String): AnimalEntity?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertWeight(weight: WeightEntity)

        @Insert suspend fun insertConsultation(consultation: ConsultationEntity)

        @Insert suspend fun insertRation(ration: RationEntity)

        @Insert suspend fun insertAlimentRation(aliment: AlimentRationEntity)

        @Insert
        suspend fun insertSupplementalVariable(supplementalVariable: SupplementalVariableEntity)

        @Query("SELECT * FROM WEIGHT WHERE refAnimal = :animalId")
        suspend fun getWeightsForAnimal(animalId: String): List<WeightEntity>

        @Query("DELETE FROM WEIGHT WHERE refAnimal = :animalId")
        suspend fun deleteWeightsForAnimal(animalId: String)

        @Query("SELECT * FROM CONSULTATIONS WHERE idAnim = :animalId")
        suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEntity>

        @Delete suspend fun deleteConsultation(consultation: ConsultationEntity)

        @Query("DELETE FROM SUPPLEMENTAL_VARIABLES WHERE idConsult = :consultationId")
        suspend fun deleteSupplementalVariablesForConsultation(consultationId: String)

        @Query("DELETE FROM RATIONS WHERE idConsult = :consultationId")
        suspend fun deleteRationsForConsultation(consultationId: String)
}

@Dao
interface ConsultationDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(consultation: ConsultationEntity)

        @Update suspend fun update(consultation: ConsultationEntity)

        @Delete suspend fun delete(consultation: ConsultationEntity)

        @Query("SELECT * FROM CONSULTATIONS WHERE idAnim = :animalId")
        suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEntity>

        @Query("SELECT * FROM CONSULTATIONS WHERE uuid = :id")
        suspend fun getConsultationById(id: String): ConsultationEntity?

        @Query("SELECT * FROM SUPPLEMENTAL_VARIABLES WHERE idConsult = :consultationId")
        suspend fun getSupplementalVariablesForConsultation(
                consultationId: String
        ): List<SupplementalVariableEntity>

        @Query("SELECT * FROM RATIONS WHERE idConsult = :consultationId")
        suspend fun getRationsForConsultation(consultationId: String): List<RationEntity>

        @Query("SELECT * FROM ALIMENTS WHERE refRation = :rationId")
        suspend fun getAlimentsForRation(rationId: String): List<AlimentRationEntity>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertSupplementalVariable(supplementalVariable: SupplementalVariableEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertRation(ration: RationEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAlimentRation(aliment: AlimentRationEntity)

        @Query("DELETE FROM RATIONS WHERE idConsult = :consultationId")
        suspend fun deleteRationsForConsultation(consultationId: String)

        @Query("DELETE FROM SUPPLEMENTAL_VARIABLES WHERE idConsult = :consultationId")
        suspend fun deleteSupplementalVariablesForConsultation(consultationId: String)
}

@Dao
interface RecipeDao {
        @Insert suspend fun insertRecipe(recipe: RecetteEntity)
        @Update suspend fun updateRecipe(recipe: RecetteEntity)
        @Delete suspend fun deleteRecipe(recipe: RecetteEntity)

        @Insert suspend fun insertAlimentRecette(aliment: AlimentRecetteEntity)
        @Update suspend fun updateAlimentRecette(aliment: AlimentRecetteEntity)
        @Delete suspend fun deleteAlimentRecette(aliment: AlimentRecetteEntity)

        @Query("SELECT * FROM RECETTES") suspend fun getAllRecipes(): List<RecetteEntity>

        @Query("SELECT * FROM RECETTES WHERE uuid = :id")
        suspend fun getRecipeById(id: String): RecetteEntity?

        @Query("SELECT * FROM ALIMENTS_RECETTES WHERE refRecipe = :recipeId")
        suspend fun getAlimentsForRecipe(recipeId: String): List<AlimentRecetteEntity>

        @Query("DELETE FROM ALIMENTS_RECETTES WHERE refRecipe = :recipeId")
        suspend fun deleteAlimentsForRecipe(recipeId: String)
}

@Dao
interface FoodDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(food: FoodEntity)

        @Update suspend fun update(food: FoodEntity)

        @Delete suspend fun delete(food: FoodEntity)

        @Query("SELECT * FROM FOOD") suspend fun findAll(): List<FoodEntity>

        @Query("SELECT * FROM FOOD WHERE uuid = :id")
        suspend fun getFoodById(id: String): FoodEntity?

        @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertFood(food: FoodEntity)

        @Update suspend fun updateFood(food: FoodEntity)

        @Query("DELETE FROM FOOD WHERE uuid = :uuid") suspend fun deleteFood(uuid: String)

        @Query("SELECT * FROM FOOD WHERE uuid = :uuid")
        suspend fun getFood(uuid: String): FoodEntity?

        @Query("SELECT * FROM FOOD") suspend fun getAllFoods(): List<FoodEntity>

        // Requêtes optimisées avec pagination
        @Query("SELECT * FROM FOOD LIMIT :limit OFFSET :offset")
        suspend fun getFoodsPaginated(limit: Int, offset: Int): List<FoodEntity>

        @Query("SELECT COUNT(*) FROM FOOD") suspend fun getFoodsCount(): Int

        /**
         * ⚠️ MÉTHODE DANGEREUSE - Supprime TOUS les aliments de la base Cette méthode ne doit
         * JAMAIS être appelée automatiquement Utiliser uniquement avec une confirmation explicite
         * de l'utilisateur
         */
        @Query("DELETE FROM FOOD") suspend fun deleteAllFoods()

        // Optimisations pour import en lot
        @Query("SELECT uuid FROM FOOD") suspend fun getAllFoodIds(): List<String>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertFoods(foods: List<FoodEntity>)

        @Update suspend fun updateFoods(foods: List<FoodEntity>)

        @Query("SELECT * FROM FOOD WHERE uuid IN (:uuids)")
        suspend fun getFoodsByIds(uuids: List<String>): List<FoodEntity>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertIndications(indications: List<IndicationAlimentEntity>)

        @Query("DELETE FROM INDICATIONS_ALIMENTS WHERE refAliment = :alimentUuid")
        suspend fun deleteIndicationsForAliment(alimentUuid: String)

        @Query("SELECT * FROM INDICATIONS_ALIMENTS WHERE refAliment = :alimentUuid")
        suspend fun getIndicationsForAliment(alimentUuid: String): List<IndicationAlimentEntity>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertEspeces(especes: List<EspeceAlimentEntity>)

        @Query("SELECT * FROM ESPECES_ALIMENTS WHERE refAliment = :alimentUuid")
        suspend fun getEspecesForAliment(alimentUuid: String): List<EspeceAlimentEntity>

        @Query("DELETE FROM ESPECES_ALIMENTS WHERE refAliment = :alimentUuid")
        suspend fun deleteEspecesForAliment(alimentUuid: String)

        // Requêtes optimisées pour la recherche et le filtrage
        @Query(
                "SELECT * FROM FOOD WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' LIMIT :limit"
        )
        suspend fun searchFoodsByNameOrBrand(query: String, limit: Int = 100): List<FoodEntity>

        @Query("SELECT * FROM FOOD WHERE groupAlim = :group LIMIT :limit")
        suspend fun getFoodsByGroup(group: Int, limit: Int = 100): List<FoodEntity>

        @Query("SELECT * FROM FOOD WHERE typeAlim = :type LIMIT :limit")
        suspend fun getFoodsByType(type: Int, limit: Int = 100): List<FoodEntity>

        @Query("SELECT * FROM FOOD WHERE deprecated = 0 LIMIT :limit")
        suspend fun getActiveFoods(limit: Int = 100): List<FoodEntity>
}

@Dao
interface NutrientValueDao {
        @Query("SELECT * FROM NUTRIENT_VALUES WHERE refAliment = :alimentUuid")
        suspend fun getNutrientValues(alimentUuid: String): List<NutrientValueEntity>

        @Query("SELECT * FROM NUTRIENT_VALUES WHERE refAliment IN (:alimentUuids)")
        suspend fun getNutrientValuesForAliments(alimentUuids: List<String>): List<NutrientValueEntity>

        @Query("SELECT * FROM NUTRIENT_VALUES WHERE refAliment IN (:alimentUuids) AND nutrientLabel = :nutrientLabel")
        suspend fun getNutrientValueForAliments(alimentUuids: List<String>, nutrientLabel: String): List<NutrientValueEntity>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertNutrientValues(values: List<NutrientValueEntity>)

        @Delete suspend fun deleteNutrientValues(values: List<NutrientValueEntity>)

        @Query("DELETE FROM NUTRIENT_VALUES WHERE refAliment = :alimentUuid")
        suspend fun deleteAllNutrientValuesForAliment(alimentUuid: String)

        @Query("DELETE FROM NUTRIENT_VALUES WHERE refAliment IN (:alimentUuids)")
        suspend fun deleteAllForAliments(alimentUuids: List<String>)
}

/** DAO pour accéder aux références bibliographiques dans la base de données */
@Dao
interface BiblioRefDao {
        @Query("SELECT * FROM BIBLIO_REFS") suspend fun getAllBiblioRefs(): List<BiblioRefEntity>

        @Query("SELECT * FROM BIBLIO_REFS WHERE uuid = :uuid")
        suspend fun getBiblioRefById(uuid: String): BiblioRefEntity?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertBiblioRef(biblioRef: BiblioRefEntity)

        @Update suspend fun updateBiblioRef(biblioRef: BiblioRefEntity)

        @Delete suspend fun deleteBiblioRef(biblioRef: BiblioRefEntity)

        @Query("DELETE FROM BIBLIO_REFS") suspend fun deleteAllBiblioRefs()

        @Query(
                "SELECT * FROM BIBLIO_REFS WHERE firstAuthor LIKE '%' || :query || '%' OR completeRef LIKE '%' || :query || '%' OR comments LIKE '%' || :query || '%'"
        )
        suspend fun searchBiblioRefs(query: String): List<BiblioRefEntity>
}

/** DAO pour accéder aux équations dans la base de données */
@Dao
interface EquationDao {
        @Query("SELECT * FROM EQUATIONS") suspend fun getAllEquations(): List<EquationEntity>

        @Query("SELECT * FROM EQUATIONS WHERE uuid = :uuid")
        suspend fun getEquationById(uuid: String): EquationEntity?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertEquation(equation: EquationEntity)

        @Update suspend fun updateEquation(equation: EquationEntity)

        @Delete suspend fun deleteEquation(equation: EquationEntity)

        @Query("DELETE FROM EQUATIONS WHERE uuid = :uuid") suspend fun deleteEquation(uuid: String)

        @Query("DELETE FROM EQUATIONS") suspend fun deleteAllEquations()

        @Query("SELECT * FROM EQUATIONS WHERE kind = :kind")
        suspend fun getEquationsByKind(kind: String): List<EquationEntity>

        @Query("SELECT * FROM EQUATIONS WHERE specie = :specie")
        suspend fun getEquationsBySpecie(specie: String): List<EquationEntity>
}

/** DAO pour accéder aux références évaluées dans la base de données */
@Dao
interface ReferenceEvDao {
        @Query("SELECT * FROM REFERENCE_EV")
        suspend fun getAllReferenceEv(): List<ReferenceEvEntity>

        @Query("SELECT * FROM REFERENCE_EV WHERE uuid = :uuid")
        suspend fun getReferenceEvById(uuid: String): ReferenceEvEntity?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertReferenceEv(referenceEv: ReferenceEvEntity)

        @Update suspend fun updateReferenceEv(referenceEv: ReferenceEvEntity)

        @Delete suspend fun deleteReferenceEv(referenceEv: ReferenceEvEntity)

        @Query("DELETE FROM REFERENCE_EV WHERE uuid = :uuid")
        suspend fun deleteReferenceEvById(uuid: String)

        @Query("DELETE FROM REFERENCE_EV") suspend fun deleteAllReferenceEv()

        @Query("SELECT * FROM REFERENCE_EV WHERE espece = :espece")
        suspend fun getReferenceEvByEspece(espece: String): List<ReferenceEvEntity>

        // Relations avec les équations
        @Query("SELECT * FROM REFERENCE_EV_EQUATIONS WHERE referenceEvId = :referenceEvId")
        suspend fun getEquationsForReference(referenceEvId: String): List<ReferenceEvEquationEntity>

        @Query(
                "SELECT * FROM REFERENCE_EV_EQUATIONS WHERE referenceEvId = :referenceEvId AND equationType = :equationType"
        )
        suspend fun getEquationForReferenceByType(
                referenceEvId: String,
                equationType: String
        ): ReferenceEvEquationEntity?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertEquationRelation(relation: ReferenceEvEquationEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertEquationRelations(relations: List<ReferenceEvEquationEntity>)

        @Query("DELETE FROM REFERENCE_EV_EQUATIONS WHERE referenceEvId = :referenceEvId")
        suspend fun deleteEquationsForReference(referenceEvId: String)

        @Query(
                "DELETE FROM REFERENCE_EV_EQUATIONS WHERE referenceEvId = :referenceEvId AND equationType = :equationType"
        )
        suspend fun deleteEquationForReferenceByType(referenceEvId: String, equationType: String)

        // Relations avec les coefficients
        @Query("SELECT * FROM REFERENCE_EV_COEFFICIENTS WHERE referenceEvId = :referenceEvId")
        suspend fun getCoefficientsForReference(
                referenceEvId: String
        ): List<ReferenceEvCoefficientEntity>

        @Query(
                "SELECT * FROM REFERENCE_EV_COEFFICIENTS WHERE referenceEvId = :referenceEvId AND groupType = :groupType"
        )
        suspend fun getCoefficientsForReferenceByGroup(
                referenceEvId: String,
                groupType: String
        ): List<ReferenceEvCoefficientEntity>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertCoefficient(coefficient: ReferenceEvCoefficientEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertCoefficients(coefficients: List<ReferenceEvCoefficientEntity>)

        @Query("DELETE FROM REFERENCE_EV_COEFFICIENTS WHERE referenceEvId = :referenceEvId")
        suspend fun deleteCoefficientsForReference(referenceEvId: String)

        @Query(
                "DELETE FROM REFERENCE_EV_COEFFICIENTS WHERE referenceEvId = :referenceEvId AND groupType = :groupType"
        )
        suspend fun deleteCoefficientsForReferenceByGroup(referenceEvId: String, groupType: String)

        // Relations avec les nutriments
        @Query("SELECT * FROM REFERENCE_EV_NUTRIENTS WHERE referenceEvId = :referenceEvId")
        suspend fun getNutrientsForReference(referenceEvId: String): List<ReferenceEvNutrientEntity>

        @Query(
                "SELECT * FROM REFERENCE_EV_NUTRIENTS WHERE referenceEvId = :referenceEvId AND reflevel = :reflevel"
        )
        suspend fun getNutrientsForReferenceByLevel(
                referenceEvId: String,
                reflevel: String
        ): List<ReferenceEvNutrientEntity>

        @Query(
                "SELECT * FROM REFERENCE_EV_NUTRIENTS WHERE referenceEvId = :referenceEvId AND nutrientCode = :nutrientCode AND reflevel = :reflevel"
        )
        suspend fun getNutrientForReference(
                referenceEvId: String,
                nutrientCode: String,
                reflevel: String
        ): ReferenceEvNutrientEntity?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertNutrient(nutrient: ReferenceEvNutrientEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertNutrients(nutrients: List<ReferenceEvNutrientEntity>)

        @Query("DELETE FROM REFERENCE_EV_NUTRIENTS WHERE referenceEvId = :referenceEvId")
        suspend fun deleteNutrientsForReference(referenceEvId: String)

        @Query(
                "DELETE FROM REFERENCE_EV_NUTRIENTS WHERE referenceEvId = :referenceEvId AND reflevel = :reflevel"
        )
        suspend fun deleteNutrientsForReferenceByLevel(referenceEvId: String, reflevel: String)

        @Query(
                "DELETE FROM REFERENCE_EV_NUTRIENTS WHERE referenceEvId = :referenceEvId AND nutrientCode = :nutrientCode AND reflevel = :reflevel"
        )
        suspend fun deleteNutrientForReference(
                referenceEvId: String,
                nutrientCode: String,
                reflevel: String
        )

        // Méthodes pour vider toutes les données
        @Query("DELETE FROM REFERENCE_EV_EQUATIONS") suspend fun deleteAllEquationRelations()

        @Query("DELETE FROM REFERENCE_EV_COEFFICIENTS") suspend fun deleteAllCoefficients()

        @Query("DELETE FROM REFERENCE_EV_NUTRIENTS") suspend fun deleteAllNutrients()
}

/** DAO pour accéder aux sections HTML réutilisables */
@Dao
interface HtmlSectionDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertSection(section: HtmlSectionEntity)

        @Update suspend fun updateSection(section: HtmlSectionEntity)

        @Delete suspend fun deleteSection(section: HtmlSectionEntity)

        @Query("SELECT * FROM HTML_SECTIONS") suspend fun getAllSections(): List<HtmlSectionEntity>

        @Query("SELECT * FROM HTML_SECTIONS WHERE id = :id")
        suspend fun getSectionById(id: String): HtmlSectionEntity?

        @Query("SELECT * FROM HTML_SECTIONS WHERE category = :category")
        suspend fun getSectionsByCategory(category: String): List<HtmlSectionEntity>

        @Query("SELECT * FROM HTML_SECTIONS WHERE isTemplate = 1")
        suspend fun getTemplateSections(): List<HtmlSectionEntity>

        @Query(
                "SELECT * FROM HTML_SECTIONS WHERE title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'"
        )
        suspend fun searchSections(query: String): List<HtmlSectionEntity>

        @Query("DELETE FROM HTML_SECTIONS") suspend fun deleteAllSections()

        // DAO pour les bibliothèques de sections
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertLibrary(library: HtmlSectionLibraryEntity)

        @Update suspend fun updateLibrary(library: HtmlSectionLibraryEntity)

        @Delete suspend fun deleteLibrary(library: HtmlSectionLibraryEntity)

        @Query("SELECT * FROM HTML_SECTION_LIBRARIES")
        suspend fun getAllLibraries(): List<HtmlSectionLibraryEntity>

        @Query("SELECT * FROM HTML_SECTION_LIBRARIES WHERE id = :id")
        suspend fun getLibraryById(id: String): HtmlSectionLibraryEntity?

        @Query("DELETE FROM HTML_SECTION_LIBRARIES") suspend fun deleteAllLibraries()

        // Requêtes spécialisées pour les conseils
        @Query(
                """
            SELECT * FROM HTML_SECTIONS 
            WHERE category LIKE '%CONSEIL%' 
            AND isTemplate = 0
            AND (title LIKE '%' || :query || '%' OR contentJson LIKE '%' || :query || '%')
            ORDER BY usageCount DESC, updatedAt DESC
            LIMIT :limit
        """
        )
        suspend fun searchConseils(query: String, limit: Int = 50): List<HtmlSectionEntity>

        @Query(
                """
            SELECT * FROM HTML_SECTIONS 
            WHERE category = :category 
            AND isTemplate = 0
            ORDER BY priority DESC, usageCount DESC
        """
        )
        suspend fun getConseilsByCategory(category: String): List<HtmlSectionEntity>

        @Query(
                """
            SELECT * FROM HTML_SECTIONS 
            WHERE category LIKE '%CONSEIL%' 
            AND isTemplate = 0
            AND lastUsed IS NOT NULL
            ORDER BY lastUsed DESC
            LIMIT :limit
        """
        )
        suspend fun getConseilsRecents(limit: Int = 20): List<HtmlSectionEntity>

        @Query(
                """
            SELECT * FROM HTML_SECTIONS 
            WHERE category LIKE '%CONSEIL%' 
            AND isTemplate = 0
            AND priority >= :minPriority
            ORDER BY priority DESC, usageCount DESC
        """
        )
        suspend fun getConseilsByPriority(minPriority: Int = 0): List<HtmlSectionEntity>

        @Query(
                """
            SELECT * FROM HTML_SECTIONS 
            WHERE category LIKE '%CONSEIL%' 
            AND isTemplate = 0
            AND isActive = 1
            ORDER BY usageCount DESC
            LIMIT :limit
        """
        )
        suspend fun getConseilsPopulaires(limit: Int = 10): List<HtmlSectionEntity>

        @Query(
                """
            SELECT COUNT(*) FROM HTML_SECTIONS 
            WHERE category LIKE '%CONSEIL%' 
            AND isTemplate = 0
            AND isActive = 1
        """
        )
        suspend fun getConseilsCount(): Int
}
