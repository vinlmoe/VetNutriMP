package fr.vetbrain.vetnutri_mp.Repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ParametresTypeTroupeau
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.Data.TypeAnimalTroupeau
import fr.vetbrain.vetnutri_mp.Data.WeightDate
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Sauvegarde complète puis restauration sur une base vide (Room réelle) : le second export doit
 * être identique au premier. Données : le jeu initial livré (aliments, référentiels, équations,
 * bibliographie, recettes, conseils, mots-clés) et deux animaux types (individu avec plan
 * évolutif, troupeau).
 */
class ExportImportRoundTripTest {

    private val bases = mutableListOf<AppDatabase>()

    @AfterTest
    fun fermer() = bases.forEach { it.close() }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }

    private val donneesInitiales: String by lazy {
        val moduleDir = File(System.getProperty("user.dir")).let { dir ->
            if (File(dir, "src/commonMain").isDirectory) dir else File(dir, "composeApp")
        }
        File(moduleDir, "src/commonMain/resources/data/vetnutri_export_init.json").readText()
    }

    private class Depots(db: AppDatabase) {
        val animaux = DatabaseAnimalRepository(db.animalDao(), db.foodDao(), db.nutrientValueDao())
        val aliments = DatabaseFoodRepository(
            db.foodDao(), db.nutrientValueDao(), db.customNutrientDao(), db.alimentBiblioRefDao(),
            db.biblioRefDao(), db.energyPerSpeciesDao()
        )
        val consultations = DatabaseConsultationRepository(db.consultationDao(), aliments)
        val equations = DatabaseEquationRepository(db.equationDao(), db.biblioRefDao())
        val referentiels = DatabaseReferenceEvRepository(db.referenceEvDao(), db.equationDao(), db.biblioRefDao())
        val exportImport = ExportImportRepository(
            animalRepository = animaux,
            foodRepository = aliments,
            equationRepository = equations,
            referenceRepository = referentiels,
            biblioRepository = DatabaseBiblioRefRepository(db.biblioRefDao()),
            consultationRepository = consultations,
            recipeRepository = RecipeRepository(db.recipeDao(), db.foodDao()),
            conseilRepository = ConseilRepository(db.htmlSectionDao())
        )
    }

    private fun nouvelleBase(): Depots =
        Depots(Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()).build().also { bases += it })

    /** Comparaison indépendante de l'ordre de lecture en base et de la date d'export. */
    private fun normaliser(envelope: ApiEnvelope): JsonElement {
        fun trier(e: JsonElement): JsonElement = when (e) {
            is JsonObject -> JsonObject(e.filterKeys { it != "generatedAtEpochMs" }.mapValues { trier(it.value) }.toSortedMap())
            is JsonArray -> JsonArray(e.map(::trier).sortedBy { it.toString() })
            else -> e
        }
        return trier(json.encodeToJsonElement(ApiEnvelope.serializer(), envelope))
    }

    /** Premières différences, chemin par chemin, pour un message d'échec lisible. */
    private fun differences(a: JsonElement, b: JsonElement, chemin: String = "", sortie: MutableList<String> = mutableListOf()): List<String> {
        if (sortie.size >= 15 || a == b) return sortie
        when {
            a is JsonObject && b is JsonObject -> (a.keys + b.keys).forEach { k ->
                val va = a[k]; val vb = b[k]
                if (va == null || vb == null) sortie += "$chemin.$k : ${va ?: "absent"} → ${vb ?: "absent"}"
                else differences(va, vb, "$chemin.$k", sortie)
            }
            a is JsonArray && b is JsonArray && a.size == b.size ->
                a.indices.forEach { i -> differences(a[i], b[i], "$chemin[$i]", sortie) }
            a is JsonArray && b is JsonArray -> sortie += "$chemin : ${a.size} éléments → ${b.size}"
            else -> sortie += "$chemin : ${a.toString().take(200)} → ${b.toString().take(200)}"
        }
        return sortie
    }

    private suspend fun ajouterAnimauxTypes(depots: Depots, init: ApiEnvelope) {
        val aliments = init.foods.take(3).map { it.uuid }
        val referentiel = init.references.first { !it.maladie }.uuid
        val maladie = init.references.firstOrNull { it.maladie }?.uuid
        val motCle = init.consultationKeywords.first().uuid
        val conseil = init.conseils.first().id

        val chien = AnimalEv(
            uuid = "chien-rex", nom = "Rex", ownerName = "M. Martin", race = "Labrador",
            birthdate = LocalDate(2019, 4, 2), summary = "Surpoids",
            weightHistory = mutableListOf(
                WeightDate(uuid = "p1", refAnimal = "chien-rex", date = LocalDate(2026, 1, 10), value = 34.5),
                WeightDate(uuid = "p2", refAnimal = "chien-rex", date = LocalDate(2026, 3, 10), value = 32.0)
            )
        )
        depots.animaux.saveAnimal(chien)
        val principale = Ration(
            uuid = "r-principale", idConsult = "c-rex", name = "Ration actuelle", actual = true,
            description = "Croquettes et légumes",
            alimentMutableList = mutableListOf(
                AlimentRation(uuid = "ar1", uuidUnif = aliments[0], quantite = 320.0, refAlimUnif = aliments[0], refRation = "r-principale"),
                AlimentRation(uuid = "ar2", uuidUnif = aliments[1], quantite = 80.0, refAlimUnif = aliments[1], refRation = "r-principale", refTarget = 2)
            )
        )
        val etape = Ration(
            uuid = "r-etape", idConsult = "c-rex", name = "Étape 30 kg", number = 2, etapeEvolutive = true,
            poids = 30.0, refRationParente = "r-principale", nomLibre = "Amaigrissement",
            suppVarp = mutableListOf(SupplementalvariableP(VariableKind.AdultWeight, 30.0)),
            alimentMutableList = mutableListOf(
                AlimentRation(uuid = "ar3", uuidUnif = aliments[2], quantite = 250.0, refAlimUnif = aliments[2], refRation = "r-etape")
            )
        )
        depots.consultations.saveConsultation(ConsultationEv(
            uuid = "c-rex", idAnim = "chien-rex", date = LocalDate(2026, 3, 10), objectConsult = "Obésité",
            observation = "NEC 7/9", cRendu = "Plan d'amaigrissement", weight = 32.0, idealWeight = 27.0,
            BCS = 7, k1Value = 0.8, k2Value = 1.0, coefficientAjustement = 0.9,
            suppVarp = mutableListOf(SupplementalvariableP(VariableKind.AdultWeight, 28.0)),
            referenceGeneraleId = referentiel, referencesMaladies = listOfNotNull(maladie).toMutableList(),
            keywordIds = mutableListOf(motCle),
            prescriptionAnamnese = "Sédentaire", prescriptionSelectedConseilIds = mutableListOf(conseil),
            prescriptionSelectedRationIds = mutableListOf("r-principale"),
            rations = mutableListOf(principale, etape)
        ))

        val troupeau = AnimalEv(
            uuid = "troupeau-1", nom = "Élevage", ownerName = "GAEC",
            typesTroupeau = mutableListOf(
                TypeAnimalTroupeau(id = "g-adultes", nom = "Adultes", nombre = 12, poids = 25.0, espece = "CHIEN", sexId = 1),
                TypeAnimalTroupeau(id = "g-chiots", nom = "Chiots", nombre = 5, poids = 6.0, espece = "CHIEN")
            )
        )
        depots.animaux.saveAnimal(troupeau)
        depots.consultations.saveConsultation(ConsultationEv(
            uuid = "c-troupeau", idAnim = "troupeau-1", date = LocalDate(2026, 2, 1),
            parametresTroupeau = mutableListOf(
                ParametresTypeTroupeau("g-adultes", nombre = 12, poids = 26.0, referenceId = referentiel, k = 1.1, bcs = 5.0),
                ParametresTypeTroupeau("g-chiots", nombre = 5, poids = 6.5, variables = mapOf("AW" to 25.0))
            ),
            rations = mutableListOf(Ration(
                uuid = "r-troupeau", idConsult = "c-troupeau", name = "Ration du chenil",
                alimentMutableList = mutableListOf(
                    AlimentRation(uuid = "ar4", uuidUnif = aliments[0], quantite = 5200.0, refAlimUnif = aliments[0], refRation = "r-troupeau")
                )
            ))
        ))
    }

    @Test
    fun sauvegardePuisRestaurationSurBaseVide_aucuneDonneePerdue() = runTest {
        val init = json.decodeFromString(ApiEnvelope.serializer(), donneesInitiales)
        val source = nouvelleBase()
        source.exportImport.importAll(init).requireComplete()
        ajouterAnimauxTypes(source, init)

        val sauvegarde = source.exportImport.exportAllEnvelope()
        assertEquals(init.foods.size, sauvegarde.foods.size)
        assertEquals(init.references.size, sauvegarde.references.size)
        assertEquals(2, sauvegarde.animals.size)
        assertTrue(sauvegarde.rations.size >= 3)

        val restauree = nouvelleBase()
        val compte = restauree.exportImport.importAll(json.encodeToString(ApiEnvelope.serializer(), sauvegarde)).requireComplete()
        assertEquals(2, compte.animals)
        assertEquals(3, compte.rations)

        val avant = normaliser(sauvegarde)
        val apres = normaliser(restauree.exportImport.exportAllEnvelope())
        assertTrue(avant == apres, "Différences après restauration :\n" + differences(avant, apres).joinToString("\n"))
    }

    @Test
    fun restaurationSurBaseDejaRemplie_neDoublePasLesDonnees() = runTest {
        val init = json.decodeFromString(ApiEnvelope.serializer(), donneesInitiales)
        val base = nouvelleBase()
        base.exportImport.importAll(init).requireComplete()
        ajouterAnimauxTypes(base, init)
        val sauvegarde = base.exportImport.exportAllEnvelope()

        // Restauration au-dessus de la même base (cas courant : retour à une sauvegarde récente)
        base.exportImport.importAll(sauvegarde).requireComplete()

        val avant = normaliser(sauvegarde)
        val apres = normaliser(base.exportImport.exportAllEnvelope())
        assertTrue(avant == apres, "Différences après seconde restauration :\n" + differences(avant, apres).joinToString("\n"))
    }

    @Test
    fun exportDUnAnimal_seRestaureSeulAvecCeQuIlUtilise() = runTest {
        val init = json.decodeFromString(ApiEnvelope.serializer(), donneesInitiales)
        val source = nouvelleBase()
        source.exportImport.importAll(init).requireComplete()
        ajouterAnimauxTypes(source, init)

        val export = source.exportImport.exportWithSelectionEnvelope(
            ExportImportRepository.ExportSelectionOptions(includeRations = true, animalIds = setOf("chien-rex"))
        )
        assertEquals(listOf("chien-rex"), export.animals.map { it.uuid })
        assertEquals(3, export.foods.size) // seulement les aliments de ses rations
        assertTrue(export.references.isNotEmpty())

        val cible = nouvelleBase()
        cible.exportImport.importAll(export).requireComplete()
        val consultation = assertNotNull(cible.consultations.getConsultationById("c-rex"))
        assertEquals(setOf("r-principale", "r-etape"), consultation.rations.map { it.uuid }.toSet())
        assertEquals("r-principale", consultation.rations.first { it.uuid == "r-etape" }.refRationParente)
        assertEquals(3, cible.aliments.getAllFoods().size)
        val avant = normaliser(export)
        val apres = normaliser(cible.exportImport.exportWithSelectionEnvelope(
            ExportImportRepository.ExportSelectionOptions(includeRations = true, animalIds = setOf("chien-rex"))
        ))
        assertTrue(avant == apres, "Différences :\n" + differences(avant, apres).joinToString("\n"))
    }

    @Test
    fun exportSelectif_lectureDesConsultationsEnEchec_echoueAuLieuDExporterSansElles() = runTest {
        val depots = nouvelleBase()
        depots.animaux.saveAnimal(AnimalEv(uuid = "a1", nom = "Rex"))
        val enPanne = object : ConsultationRepository by depots.consultations {
            override suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEv> =
                error("lecture impossible")
        }
        val export = ExportImportRepository(depots.animaux, consultationRepository = enPanne)
        assertFailsWith<IllegalStateException> {
            export.exportWithSelectionEnvelope(ExportImportRepository.ExportSelectionOptions(animalIds = setOf("a1")))
        }
    }

    @Test
    fun donneesInitiales_energieDUneRationAvecCelluloseApprochee() = runTest {
        // Chien adulte > 25 kg, 200 g de Baby dog milk (fibre totale 0,5 %, cellulose absente) :
        // cellulose approchée par la fibre totale dans DEcom → 541,66 kcal/100 g, soit 1083,3 kcal.
        // Les équations complémentaires relues en base doivent garder leur nutriment.
        val init = json.decodeFromString(ApiEnvelope.serializer(), donneesInitiales)
        val base = nouvelleBase()
        base.exportImport.importAll(init).requireComplete()

        val reference = assertNotNull(base.referentiels.getReferenceEvById("105a39b1-c797-4b72-a4db-79035df1c19d"))
        assertEquals("Adulte >25kg", reference.nom)
        assertTrue(reference.equationsNut.any { it.nutrient?.label == "CELLULOSE" && it.equationScript == "FIBRETOT" })
        val lait = assertNotNull(base.aliments.getFoodById("RC-23-002"))
        val energie = AlimentRation(aliment = lait, quantite = 200.0, weight = 1.0).getEnergie(reference, base.equations)
        assertTrue(kotlin.math.abs(energie - 1083.31) < 0.01, "énergie $energie")
    }
}
