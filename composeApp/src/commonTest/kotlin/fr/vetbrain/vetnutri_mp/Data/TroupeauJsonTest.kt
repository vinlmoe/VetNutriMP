package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toData
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
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
                    TypeAnimalTroupeau(
                            id = "t-lait", nom = "Juments suitées", nombre = 40, poids = 650.0,
                            espece = Espece.CHEVAL.name, race = "Percheron", sexId = Sex.FEMELLE_ENTIERE.id
                    ),
                    TypeAnimalTroupeau(id = "t-tar", nom = "Juments taries", nombre = 8, poids = 700.0, espece = Espece.CHEVAL.name)
            )

    private val parametres =
            mutableListOf(
                    ParametresTypeTroupeau(
                            typeId = "t-lait", nombre = 38, poids = 640.0, referenceId = "ref-lait", k = 1.15,
                            variables = mapOf("L" to 30.0), bcs = 3.25
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
        assertEquals<List<TypeAnimalTroupeau>?>(emptyList(), TroupeauJson.typesDepuisJson(TroupeauJson.typesVersJson(emptyList())))
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
        assertEquals<List<TypeAnimalTroupeau>?>(emptyList(), TroupeauJson.typesDepuisJson("{pas du json"))
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

    // --- Espèce, race et sexe des individus (le troupeau n'en a pas) --------------------------

    @Test
    fun groupes_especeRaceSexeSurviventAuxFormats() {
        val animal = troupeau()
        val viaEntite = animal.toEntity().toData().typesTroupeau!!.first()
        val viaApi = json.decodeFromString(AnimalApi.serializer(), json.encodeToString(AnimalApi.serializer(), animal.toApi()))
                .toDomain().typesTroupeau!!.first()
        val viaHistorique = json.decodeFromString(AnimalEvJson.serializer(), json.encodeToString(AnimalEvJson.serializer(), animal.toJson()))
                .toData().typesTroupeau!!.first()
        listOf(viaEntite, viaApi, viaHistorique).forEach { groupe ->
            assertEquals(Espece.CHEVAL, groupe.especeEnum())
            assertEquals("Percheron", groupe.race)
            assertEquals(Sex.FEMELLE_ENTIERE, groupe.sexe())
        }
    }

    @Test
    fun groupes_ancienJsonSansEspece_prendLEspeceDeLaFiche() {
        val groupe = TroupeauJson.typesDepuisJson("""[{"id":"t1","nom":"Adultes","nombre":3,"poids":60.0}]""")!!.single()
        assertNull(groupe.espece)
        assertNull(groupe.especeEnum())
        assertEquals("", groupe.race)
        assertNull(groupe.sexe())
        val fiche = AnimalEv(specieId = Espece.CHAT.label, typesTroupeau = mutableListOf(groupe))
        assertEquals(Espece.CHAT, fiche.especeDuGroupe(groupe))
        // Espèce ou sexe inconnus : ignorés plutôt que de faire planter
        assertNull(groupe.copy(espece = "INCONNUE").especeEnum())
        assertNull(groupe.copy(sexId = 999).sexe())
    }

    @Test
    fun troupeauMixte_especesDesGroupesEtEspeceDeLaFiche() {
        val mixte =
                AnimalEv(
                        specieId = Espece.CHIEN.label,
                        typesTroupeau =
                                mutableListOf(
                                        TypeAnimalTroupeau(id = "o", nom = "Lapines", espece = Espece.LAPIN.name),
                                        TypeAnimalTroupeau(id = "c", nom = "Furets", espece = Espece.FURET.name),
                                        TypeAnimalTroupeau(id = "o2", nom = "Lapereaux", espece = Espece.LAPIN.name)
                                )
                )
        assertEquals(listOf(Espece.LAPIN, Espece.FURET), mixte.especesTroupeau())
        // La fiche garde l'espèce du premier groupe (écrans communs)
        assertEquals(Espece.LAPIN, mixte.avecEspeceDuTroupeau().getEspece())
        // Individu, ou groupes sans espèce : fiche inchangée
        assertEquals(Espece.CHIEN, AnimalEv(specieId = Espece.CHIEN.label).avecEspeceDuTroupeau().getEspece())
        val sansEspece = AnimalEv(specieId = Espece.CHIEN.label, typesTroupeau = mutableListOf(TypeAnimalTroupeau(nom = "A")))
        assertEquals(Espece.CHIEN, sansEspece.avecEspeceDuTroupeau().getEspece())
        assertEquals(listOf(Espece.CHIEN), sansEspece.especesTroupeau())
    }

    @Test
    fun etatDuTroupeau_necConserveeEtAbsenteDesAnciensJson() {
        val relus = TroupeauJson.parametresDepuisJson(TroupeauJson.parametresVersJson(parametres))
        assertEquals(3.25, relus.first().bcs)
        assertNull(relus.last().bcs)
        assertNull(TroupeauJson.parametresDepuisJson("""[{"typeId":"t1","nombre":5}]""").single().bcs)
        val viaApi = json.decodeFromString(AnimalApi.serializer(), json.encodeToString(AnimalApi.serializer(), troupeau().toApi()))
                .toDomain().consultations.single().parametresTroupeau.first()
        assertEquals(3.25, viaApi.bcs)
    }
}
