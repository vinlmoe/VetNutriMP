package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toData
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

/**
 * Troupeau : types d'animaux et paramètres par consultation dans tous les formats JSON (colonnes
 * Room, export API, ancien format AnimalEvJson) ; les anciens fichiers sans ces champs restent
 * des individus.
 */
class TroupeauJsonTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val types =
            mutableListOf(
                    TypeAnimalTroupeau(id = "t-lait", nom = "Vaches en lactation", nombre = 40, poids = 650.0),
                    TypeAnimalTroupeau(id = "t-tar", nom = "Taries", nombre = 8, poids = 700.0)
            )

    private val parametres =
            mutableListOf(
                    ParametresTypeTroupeau(
                            typeId = "t-lait", nombre = 38, poids = 640.0, referenceId = "ref-lait", k = 1.15,
                            variables = mapOf("L" to 30.0)
                    ),
                    ParametresTypeTroupeau(typeId = "t-tar", nombre = 9, poids = 710.0, referenceId = "ref-entretien")
            )

    private fun troupeau() =
            AnimalEv(
                    uuid = "a-herd",
                    nom = "Élevage Martin",
                    typesTroupeau = types.toMutableList(),
                    consultations =
                            mutableListOf(
                                    ConsultationEv(
                                            uuid = "c-herd",
                                            idAnim = "a-herd",
                                            date = LocalDate(2026, 10, 10),
                                            parametresTroupeau = parametres.toMutableList()
                                    )
                            )
            )

    // --- TroupeauJson (colonnes herdTypesJson / herdParamsJson) ---------------------------------

    @Test
    fun troupeauJson_allerRetourDesTypesEtDesParametres() {
        assertEquals(types, TroupeauJson.typesDepuisJson(TroupeauJson.typesVersJson(types)))
        assertEquals(parametres, TroupeauJson.parametresDepuisJson(TroupeauJson.parametresVersJson(parametres)))
    }

    @Test
    fun troupeauJson_individuEtParametresVides() {
        // Individu : colonne NULL ; troupeau sans type : liste vide (reste un troupeau)
        assertNull(TroupeauJson.typesVersJson(null))
        assertNull(TroupeauJson.typesDepuisJson(null))
        assertNull(TroupeauJson.typesDepuisJson(""))
        assertEquals(emptyList<TypeAnimalTroupeau>(), TroupeauJson.typesDepuisJson(TroupeauJson.typesVersJson(emptyList())))
        // Aucun paramètre : colonne NULL, relue en liste vide
        assertNull(TroupeauJson.parametresVersJson(emptyList()))
        assertTrue(TroupeauJson.parametresDepuisJson(null).isEmpty())
        assertTrue(TroupeauJson.parametresDepuisJson("  ").isEmpty())
    }

    @Test
    fun troupeauJson_valeursParDefautEtClesInconnues() {
        val types = TroupeauJson.typesDepuisJson("""[{"id":"t1","nom":"Brebis","champFutur":true}]""")!!
        assertEquals(TypeAnimalTroupeau(id = "t1", nom = "Brebis", nombre = 1, poids = 0.0), types.single())
        val p = TroupeauJson.parametresDepuisJson("""[{"typeId":"t1"}]""").single()
        assertEquals(ParametresTypeTroupeau(typeId = "t1"), p)
        assertEquals(1, p.nombre)
        assertEquals(1.0, p.k)
        assertNull(p.referenceId)
        assertTrue(p.variables.isEmpty())
    }

    @Test
    fun troupeauJson_contenuIllisible_neFaitPasPlanter() {
        // Un troupeau dont le JSON est abîmé reste un troupeau (sans type) plutôt qu'un individu
        assertEquals(emptyList<TypeAnimalTroupeau>(), TroupeauJson.typesDepuisJson("{pas du json"))
        assertTrue(TroupeauJson.parametresDepuisJson("[{\"nombre\":2}]").isEmpty())
    }

    // --- Entités Room --------------------------------------------------------------------------

    @Test
    fun entites_allerRetourAnimalEtConsultation() {
        val animal = troupeau()
        val entite = animal.toEntity()
        assertNotNull(entite.herdTypesJson)
        val relu = entite.toData()
        assertTrue(relu.estTroupeau)
        assertEquals(types, relu.typesTroupeau)
        assertEquals(48, relu.effectifTroupeau)

        val consultation = animal.consultations.single()
        val entiteConsultation = consultation.toEntity()
        assertNotNull(entiteConsultation.herdParamsJson)
        assertEquals(parametres, entiteConsultation.toData().parametresTroupeau)
    }

    @Test
    fun entites_individu_colonnesNulles() {
        val individu = AnimalEv(uuid = "a-ind", nom = "Rex")
        assertNull(individu.toEntity().herdTypesJson)
        assertFalse(individu.toEntity().toData().estTroupeau)
        val consultation = ConsultationEv(uuid = "c-ind", idAnim = "a-ind")
        assertNull(consultation.toEntity().herdParamsJson)
        assertTrue(consultation.toEntity().toData().parametresTroupeau.isEmpty())
    }

    // --- Export API (sauvegardes) --------------------------------------------------------------

    @Test
    fun api_allerRetourParChaineJson() {
        val encode = json.encodeToString(AnimalApi.serializer(), troupeau().toApi())
        assertTrue(encode.contains("herdTypes"))
        assertTrue(encode.contains("herdParameters"))
        val relu = json.decodeFromString(AnimalApi.serializer(), encode).toDomain()

        assertTrue(relu.estTroupeau)
        assertEquals(types, relu.typesTroupeau)
        assertEquals(parametres, relu.consultations.single().parametresTroupeau)
        assertEquals(mapOf("L" to 30.0), relu.consultations.single().parametresTroupeau.first().variables)
    }

    @Test
    fun api_enveloppeComplete() {
        val envelope = ApiEnvelope(version = "test", generatedAtEpochMs = 0L, animals = listOf(troupeau().toApi(), AnimalEv(uuid = "a-ind").toApi()))
        val relu = json.decodeFromString(ApiEnvelope.serializer(), json.encodeToString(ApiEnvelope.serializer(), envelope))
        val animaux = relu.animals.map { it.toDomain() }
        assertTrue(animaux.first { it.uuid == "a-herd" }.estTroupeau)
        assertFalse(animaux.first { it.uuid == "a-ind" }.estTroupeau)
    }

    @Test
    fun api_ancienExportSansChampsTroupeau_resteUnIndividu() {
        val ancien =
                """
                {"uuid":"old","name":"Rex","isDead":false,"sexId":0,"specieId":"CHIEN","ownerName":"",
                "breed":"","summary":"","consultations":[{"uuid":"c-old","rations":[]}]}
                """.trimIndent()
        val relu = json.decodeFromString(AnimalApi.serializer(), ancien).toDomain()
        assertFalse(relu.estTroupeau)
        assertNull(relu.typesTroupeau)
        assertTrue(relu.consultations.single().parametresTroupeau.isEmpty())
    }

    // --- Ancien format AnimalEvJson (partage, import) ------------------------------------------

    @Test
    fun formatHistorique_allerRetourParChaineJson() {
        val encode = json.encodeToString(AnimalEvJson.serializer(), troupeau().toJson())
        val relu = json.decodeFromString(AnimalEvJson.serializer(), encode).toData()
        assertTrue(relu.estTroupeau)
        assertEquals(types, relu.typesTroupeau)
        assertEquals(parametres, relu.consultations.single().parametresTroupeau)
    }

    @Test
    fun formatHistorique_sansChampsTroupeau_resteUnIndividu() {
        val ancien =
                """
                {"UUID":"old","nom":"Rex","consultations":[{"UUID":"c-old","date":"2024-03-01"}]}
                """.trimIndent()
        val relu = json.decodeFromString(AnimalEvJson.serializer(), ancien).toData()
        assertFalse(relu.estTroupeau)
        assertTrue(relu.consultations.single().parametresTroupeau.isEmpty())
    }
}
