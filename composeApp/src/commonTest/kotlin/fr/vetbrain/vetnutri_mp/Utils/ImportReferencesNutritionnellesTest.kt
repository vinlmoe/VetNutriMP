package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.ConformiteStatus
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.ValeurNutritionnelle
import fr.vetbrain.vetnutri_mp.Data.calculerConformite
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlin.test.AfterTest
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Import des référentiels nutritionnels (.vbnr.json) : lecture sans base de données. */
class ImportReferencesNutritionnellesTest {

    @AfterTest
    fun nettoyer() = ImportUtils.clearResolutionsProblematiques()

    private val biblio = """
        {"UUID": "bib-1", "firstAuthor": "NRC", "year": 2006, "completeRef": "Nutrient Requirements of Dogs and Cats", "consistent": 1}
    """.trimIndent()

    private fun besoin(
        nutrient: String,
        quantity: Double,
        level: String = "MIN",
        unit: String = "G",
        unitRequirement: String = "MCAL",
        biblioRef: String = """{"UUID": "bib-1"}"""
    ) = """{"nutrient": "$nutrient", "referenceLevel": "$level", "quantity": $quantity,
            "unit": "$unit", "unitRequirement": "$unitRequirement", "bibliographicReference": $biblioRef}"""

    private fun equation(uuid: String, name: String, kind: String) = """
        {"UUID": "$uuid", "name": "$name", "equationScript": "130 * BW^0.75", "kind": $kind,
         "Specie": "CHIEN", "bib": $biblio}
    """.trimIndent()

    private fun referentiel(
        reference: String = """{"UUID": "ref-1", "nom": "Chien adulte NRC", "species": "CHIEN", "sPhysio": "ADULTE"}""",
        equations: List<String> = emptyList(),
        besoins: List<String> = emptyList()
    ) = """
        {"reference": $reference,
         "allEquations": [${equations.joinToString(",")}],
         "allBibliographicReferences": [$biblio],
         "nutrientRequirements": [${besoins.joinToString(",")}]}
    """.trimIndent()

    private fun lire(json: String) = ImportUtils.lireReferencesNutritionnelles(json)

    @Test
    fun referenceComplete_identiteEspeceEtBesoins() {
        val refs = lire("[${referentiel(besoins = listOf(besoin("PROTEINE", 45.0), besoin("CA", 1.25), besoin("CA", 6.25, level = "MAX")))}]")

        val ref = refs.single()
        assertEquals("ref-1", ref.uuid)
        assertEquals("Chien adulte NRC", ref.nom)
        assertEquals(Espece.CHIEN, ref.espece)
        assertEquals(StadePhysio.ADULTE, ref.stadePhysio)
        assertEquals(false, ref.maladie)
        assertEquals(45.0, ref.obtenirNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
        assertEquals(1.25, ref.obtenirNutriment(NutrientMacro.CAL, Reflevel.MIN))
        assertEquals(6.25, ref.obtenirNutriment(NutrientMacro.CAL, Reflevel.MAX))
    }

    @Test
    fun objetUniqueAccepteCommeTableau() {
        assertEquals(1, lire(referentiel()).size)
    }

    @Test
    fun besoinParKgDePoidsMetabolique_resteParPoidsMetabolique() {
        // Code V2 KGMW = par kg de poids métabolique (LegacyV2Mappings : KGMW → PERMS) ; il était
        // importé « par kg de poids vif », ce qui fausse le besoin dès que le poids dépasse 1 kg.
        val ref = lire(referentiel(besoins = listOf(
            besoin("PROTEINE", 4.95, unitRequirement = "KGMW"),
            besoin("CA", 0.13, unitRequirement = "KGBW"),
            besoin("NA", 0.2, unitRequirement = "MCAL"),
            besoin("K", 1.0, unitRequirement = "PERC")
        ))).single()

        assertEquals(UnitReqEnum.PERMS, ref.obtenirNutrimentRef(NutrientMain.PROTEINE, Reflevel.MIN)?.uniteReq)
        assertEquals(UnitReqEnum.PERKG, ref.obtenirNutrimentRef(NutrientMacro.CAL, Reflevel.MIN)?.uniteReq)
        assertEquals(UnitReqEnum.PERKCAL, ref.obtenirNutrimentRef(NutrientMacro.NA, Reflevel.MIN)?.uniteReq)
        assertEquals(UnitReqEnum.RATIO, ref.obtenirNutrimentRef(NutrientMacro.K, Reflevel.MIN)?.uniteReq)
    }

    @Test
    fun quantiteEnMilligrammes_rameneeAlUniteDuNutriment() {
        // Potassium exprimé en g dans l'application : 1 500 mg → 1,5 g (et non 1 500 g)
        val ref = lire(referentiel(besoins = listOf(
            besoin("K", 1500.0, unit = "MG"),
            besoin("NA", 0.5, unit = "NO")
        ))).single()

        assertEquals(1.5, ref.obtenirNutriment(NutrientMacro.K, Reflevel.MIN), 1e-9)
        assertEquals(0.5, ref.obtenirNutriment(NutrientMacro.NA, Reflevel.MIN), 1e-9)
    }

    @Test
    fun niveauxDeReference_codesNomsEtLibelles() {
        val ref = lire(referentiel(besoins = listOf(
            besoin("PROTEINE", 1.0, level = "1"),
            besoin("CA", 2.0, level = "MAXIMUM"),
            besoin("NA", 3.0, level = "OPTIMIN"),
            besoin("K", 4.0, level = "optimumMax")
        ))).single()

        assertEquals(1.0, ref.obtenirNutriment(NutrientMain.PROTEINE, Reflevel.MAX))
        assertEquals(2.0, ref.obtenirNutriment(NutrientMacro.CAL, Reflevel.MAX))
        assertEquals(3.0, ref.obtenirNutriment(NutrientMacro.NA, Reflevel.OPTIMIN))
        assertEquals(4.0, ref.obtenirNutriment(NutrientMacro.K, Reflevel.OPTIMAX))
    }

    @Test
    fun equations_rangeesSelonLeurType() {
        val ref = lire(referentiel(equations = listOf(
            equation("eq-bee", "BEE chien adulte", "0"),
            equation("eq-mw", "Poids métabolique", "\"MW\""),
            equation("eq-dec", "Densité commercial", "1"),
            equation("eq-der", "Densité ration ménagère", "\"ENERGYDENSITY\""),
            equation("eq-comp", "Taurine", "\"COMPLEMENTARY_NUTRIENT\""),
            equation("eq-enercomp", "Énergie complémentaire", "6")
        ))).single()

        assertEquals("eq-bee", ref.equationBEE?.uuid)
        assertEquals("eq-mw", ref.equationBW?.uuid)
        assertEquals("eq-dec", ref.equationDEcom?.uuid)
        assertEquals("eq-der", ref.equationDEraw?.uuid)
        val autres = ref.equationsNut.associate { it.uuid to it.kind }
        // Un nom d'énumération en texte était lu comme ENERGYNEED
        assertEquals(EquationKind.COMPLEMENTARY_NUTRIENT, autres["eq-comp"])
        assertEquals(EquationKind.ENERCOMP, autres["eq-enercomp"])
        assertEquals("NRC", ref.equationBEE?.bib?.firstAuthor)
    }

    @Test
    fun bibliographie_parObjetOuParIdentifiant() {
        val ref = lire(referentiel(besoins = listOf(
            besoin("PROTEINE", 45.0),
            besoin("CA", 1.25, biblioRef = "\"bib-1\""),
            besoin("NA", 0.2, biblioRef = "null")
        ))).single()

        assertEquals("NRC", ref.obtenirNutrimentRef(NutrientMain.PROTEINE, Reflevel.MIN)?.biblio?.firstAuthor)
        assertEquals("NRC", ref.obtenirNutrimentRef(NutrientMacro.CAL, Reflevel.MIN)?.biblio?.firstAuthor)
        assertEquals("", ref.obtenirNutrimentRef(NutrientMacro.NA, Reflevel.MIN)?.biblio?.firstAuthor ?: "")
    }

    @Test
    fun maladieEtStade_detectesDepuisLeNom() {
        val refs = lire("""[
            ${referentiel(reference = """{"UUID": "r1", "nom": "Chiot en croissance", "species": "DOG", "sPhysio": "ADULTE"}""")},
            ${referentiel(reference = """{"UUID": "r2", "nom": "Insuffisance rénale chat", "species": "CAT"}""")},
            ${referentiel(reference = """{"UUID": "r3", "nom": "Mucocèle", "species": "CHIEN", "disease": true, "nameDisease": "Mucocèle biliaire"}""")}
        ]""")

        assertEquals(StadePhysio.CROISSANCE, refs[0].stadePhysio)
        assertEquals(Espece.CHAT, refs[1].espece)
        assertTrue(refs[1].maladie)
        assertEquals("Mucocèle biliaire", refs[2].nomMaladie)
    }

    @Test
    fun coefficientsK_nomsEtValeursDedupliques() {
        val ref = lire(referentiel(reference = """
            {"UUID": "ref-k", "nom": "Chien", "species": "CHIEN", "namek1": "Activité",
             "modk1": [{"description": "Sédentaire", "coef": 0.8}, {"description": "Actif", "coef": 1.2},
                       {"description": "Actif", "coef": 1.2}]}
        """.trimIndent())).single()

        assertEquals("Activité", ref.nomk1)
        // « Normal » (1,0) est le coefficient par défaut de toute référence
        assertEquals(
            listOf("Normal" to 1.0, "Sédentaire" to 0.8, "Actif" to 1.2),
            ref.modk1.map { it.description to it.coef }
        )
    }

    @Test
    fun nutrimentMalOrthographie_nEcrasePasUnAutreNutriment() {
        // « SODIUMM » était rapproché d'un nutriment existant par approximation, et « CALCIUM_TOTAL »
        // de celui dont le libellé est contenu dans le sien
        val ref = lire(referentiel(besoins = listOf(
            besoin("NA", 0.2), besoin("SODIUMM", 9.0), besoin("CA", 1.0), besoin("CALCIUM_TOTAL", 9.0)
        ))).single()

        assertEquals(0.2, ref.obtenirNutriment(NutrientMacro.NA, Reflevel.MIN))
        assertEquals(1.0, ref.obtenirNutriment(NutrientMacro.CAL, Reflevel.MIN))
        val rapport = ImportUtils.genererRapportResolutionsProblematiques()
        assertTrue(rapport.contains("SODIUMM") && rapport.contains("CALCIUM_TOTAL"), rapport)
    }

    @Test
    fun nutrimentInconnu_ignoreEtSignaleDansLeRapport() {
        ImportUtils.clearResolutionsProblematiques()
        val ref = lire(referentiel(besoins = listOf(besoin("PROTEINE", 45.0), besoin("NUTRIMENT_INEXISTANT", 1.0)))).single()

        assertEquals(45.0, ref.obtenirNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
        val rapport = ImportUtils.genererRapportResolutionsProblematiques()
        assertTrue(rapport.contains("NUTRIMENT_INEXISTANT"), rapport)
    }

    @Test
    fun contenuInvalideOuEtranger_listeVide() {
        assertTrue(lire("").isEmpty())
        assertTrue(lire("{pas du json").isEmpty())
        assertTrue(lire("""[{"UUID": "food-1", "nom": "Aliment", "valMap": {}}]""").isEmpty())
        // Structure reconnue mais sans objet « reference » : aucune référence fabriquée
        assertTrue(lire("""[{"allEquations": [], "allBibliographicReferences": [], "nutrientRequirements": []}]""").isEmpty())
    }

    @Test
    fun referenceSansIdentifiant_recoitUnUuidEtUnNomParDefaut() {
        val ref = lire(referentiel(reference = "{}")).single()
        assertTrue(ref.uuid.isNotBlank())
        assertEquals("Référence importée 1", ref.nom)
        assertNull(ref.equationBEE)
    }

    @Test
    fun uniteDeBesoinInconnue_besoinEcarteEtSignale() {
        // « DM » n'est pas une base de calcul de l'application : le besoin n'est plus rangé
        // « par kg de poids métabolique » (ni un code inconnu en valeur absolue)
        val ref = lire(referentiel(besoins = listOf(
            besoin("PROTEINE", 18.0, unitRequirement = "DM"),
            besoin("CA", 1.0, unitRequirement = "XYZ"),
            besoin("NA", 0.2)
        ))).single()

        assertNull(ref.obtenirNutrimentRef(NutrientMain.PROTEINE, Reflevel.MIN))
        assertNull(ref.obtenirNutrimentRef(NutrientMacro.CAL, Reflevel.MIN))
        assertEquals(0.2, ref.obtenirNutriment(NutrientMacro.NA, Reflevel.MIN))
        val rapport = ImportUtils.genererRapportResolutionsProblematiques()
        assertTrue(rapport.contains("unité de besoin inconnue : DM") && rapport.contains("XYZ"), rapport)
    }

    @Test
    fun uniteDeBesoinNumerique_codesV2() {
        // Numéros V2 : 0 MCAL, 1 KGBW, 2 KGMW, 3 NO, 4 PERC (comme la migration V2)
        val ref = lire(referentiel(besoins = listOf(
            besoin("PROTEINE", 1.0, unitRequirement = "0"),
            besoin("CA", 1.0, unitRequirement = "1"),
            besoin("NA", 1.0, unitRequirement = "2"),
            besoin("K", 1.0, unitRequirement = "3"),
            besoin("P", 1.0, unitRequirement = "4"),
            besoin("MG", 1.0, unitRequirement = "9")
        ))).single()

        assertEquals(UnitReqEnum.PERKCAL, ref.obtenirNutrimentRef(NutrientMain.PROTEINE, Reflevel.MIN)?.uniteReq)
        assertEquals(UnitReqEnum.PERKG, ref.obtenirNutrimentRef(NutrientMacro.CAL, Reflevel.MIN)?.uniteReq)
        assertEquals(UnitReqEnum.PERMS, ref.obtenirNutrimentRef(NutrientMacro.NA, Reflevel.MIN)?.uniteReq)
        assertEquals(UnitReqEnum.ABSOLUTE, ref.obtenirNutrimentRef(NutrientMacro.K, Reflevel.MIN)?.uniteReq)
        assertEquals(UnitReqEnum.RATIO, ref.obtenirNutrimentRef(NutrientMacro.PHOS, Reflevel.MIN)?.uniteReq)
        assertNull(ref.obtenirNutrimentRef(NutrientMacro.MG, Reflevel.MIN))
    }

    @Test
    fun nutrimentParNomEnumV2_rapportProteinesPhosphore() {
        // Les fichiers V2 écrivent « PhosphProt » (nom d'énumération, libellé « PROTP ») : il était
        // rangé sous le potassium, dont il écrasait le minimum
        val ref = lire(referentiel(besoins = listOf(
            besoin("PhosphProt", 25.0, unit = "NO", unitRequirement = "NO"),
            besoin("K", 1.0, level = "OPTIMIN")
        ))).single()

        assertEquals(25.0, ref.obtenirNutriment(NutrientAnalysis.PhosphProt, Reflevel.MIN))
        assertNull(ref.obtenirNutrimentRef(NutrientMacro.K, Reflevel.MIN))
        assertEquals(1.0, ref.obtenirNutriment(NutrientMacro.K, Reflevel.OPTIMIN))
    }

    // Exemple de ration : chien de 30 kg, poids métabolique 30^0,75 = 12,82 kg, BEE 1 600 kcal
    private val poids = 30.0
    private val poidsMetabolique = poids.pow(0.75)
    private val bee = 1600.0

    private val referentielRation by lazy {
        lire(referentiel(besoins = listOf(
            besoin("PROTEINE", 4.95, unitRequirement = "KGMW"), // 63,5 g
            besoin("CA", 0.13, unitRequirement = "KGBW"), // 3,9 g
            besoin("NA", 0.2, unitRequirement = "MCAL"), // 0,32 g
            besoin("K", 2000.0, unit = "MG", unitRequirement = "NO"), // 2 g
            besoin("PhosphProt", 25.0, unit = "NO", unitRequirement = "NO")
        ))).single()
    }

    private fun analyser(nutriment: Nutrient, apport: Double, ref: ReferenceEv = referentielRation) =
        calculerConformite(
            valeurNutritionnelle = ValeurNutritionnelle(
                nutriment = nutriment, unite = nutriment.ue, valeur = apport, description = "", complete = true
            ),
            referenceUtilisee = ref,
            besoinEnergetiqueEntretien = bee,
            poidsAnimal = poids,
            poidsMetabolique = poidsMetabolique
        )?.status

    @Test
    fun exempleRation_besoinsImportesAppliquesALAnalyse() {
        // Protéines par kg de poids métabolique : 70 g couvrent 63,5 g (l'ancien import par kg de
        // poids vif exigeait 148,5 g et signalait une carence)
        assertNull(analyser(NutrientMain.PROTEINE, 70.0))
        assertEquals(ConformiteStatus.CARENCE, analyser(NutrientMain.PROTEINE, 60.0))
        // Calcium par kg de poids vif
        assertNull(analyser(NutrientMacro.CAL, 4.0))
        assertEquals(ConformiteStatus.CARENCE, analyser(NutrientMacro.CAL, 3.5))
        // Sodium pour 1 000 kcal de BEE
        assertNull(analyser(NutrientMacro.NA, 0.35))
        assertEquals(ConformiteStatus.CARENCE, analyser(NutrientMacro.NA, 0.3))
        // Potassium saisi en mg, valeur absolue : 2 g (et non 2 000 g)
        assertNull(analyser(NutrientMacro.K, 2.5))
        assertEquals(ConformiteStatus.CARENCE, analyser(NutrientMacro.K, 1.5))
        // Rapport protéines/phosphore
        assertNull(analyser(NutrientAnalysis.PhosphProt, 28.0))
        assertEquals(ConformiteStatus.CARENCE, analyser(NutrientAnalysis.PhosphProt, 20.0))
    }

    @Test
    fun exempleRation_besoinEnUniteInconnue_neJugePasLaRation() {
        // Un besoin « DM » écarté : la ration n'est ni conforme ni en carence sur ce nutriment
        val ref = lire(referentiel(besoins = listOf(besoin("PROTEINE", 18.0, unitRequirement = "DM")))).single()
        assertNull(analyser(NutrientMain.PROTEINE, 1.0, ref))
        assertNull(analyser(NutrientMain.PROTEINE, 1000.0, ref))
    }
}
