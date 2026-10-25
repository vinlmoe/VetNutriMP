package fr.vetbrain.vetnutri_mp.Export

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Rendu PDF desktop du document HTML produit par [HtmlDocumentBuilder].
 *
 * `PdfExporter.exportHtmlDocument` (desktop) enchaîne le rendu openhtmltopdf et une boîte de
 * dialogue Swing d'enregistrement (`FileUtils.saveBinaryFileDialog`) : cette dernière ne peut pas
 * être exercée sans écran. Le test reproduit donc, à l'identique, l'étape de rendu de
 * `PdfExporter.desktop.kt` (PdfRendererBuilder + BatikSVGDrawer) sur le HTML réellement généré,
 * et écrit le résultat dans un fichier temporaire : cela garantit que le HTML de l'application
 * reste du XHTML acceptable pour openhtmltopdf (balises auto-fermantes, entités échappées).
 */
class PdfExporterDesktopTest {

    private lateinit var tempDir: File

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("vetnutri-pdf-test").toFile()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    /** Même pipeline que PdfExporter.desktop.kt, sans la boîte de dialogue d'enregistrement. */
    private fun rendrePdf(html: String, destination: File) {
        destination.outputStream().use { flux ->
            val builder = PdfRendererBuilder()
            builder.useSVGDrawer(BatikSVGDrawer())
            builder.withHtmlContent(html, null)
            builder.toStream(flux)
            builder.run()
        }
    }

    private val croquettes =
            AlimentEv(uuid = "croq", nom = "Croquettes adulte", brand = "Marque").apply {
                setNutrient(NutrientMain.PROTEINE, 25.0)
                setNutrient(NutrientMain.ENERGIE, 350.0)
            }

    private val ration =
            Ration(
                    uuid = "r1",
                    idConsult = "c",
                    name = "Actuelle",
                    actual = true,
                    alimentMutableList =
                            mutableListOf(
                                    AlimentRation(uuid = "a1", refAlimUnif = "croq", quantite = 150.0, aliment = croquettes)
                            )
            )

    private val animal = AnimalEv(uuid = "a", nom = "Rex & Co", id = "ID-42")

    @Test
    fun ordonnance_renduePdf_fichierValide() = runTest {
        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.PRESCRIPTION,
                        ExportData(
                                animal = animal,
                                ration = null,
                                reference = null,
                                rations = listOf(ration),
                                consultation = ConsultationEv(uuid = "c", idAnim = "a", weight = 12.0, rations = mutableListOf(ration)),
                                conseils = listOf("Fractionner en 2 repas"),
                                additionalText = "Revoir dans 1 mois"
                        )
                )
        val pdf = File(tempDir, "ordonnance.pdf")
        rendrePdf(html, pdf)

        assertTrue(pdf.exists(), "le PDF doit être écrit")
        assertTrue(pdf.length() > 1024, "le PDF doit faire plus de 1 Ko (${pdf.length()} octets)")
        val entete = pdf.inputStream().use { flux -> ByteArray(4).also { flux.read(it) } }
        assertEquals("%PDF", String(entete, Charsets.US_ASCII), "signature PDF")
    }

    @Test
    fun analyseDeRation_renduePdf_fichierValide() = runTest {
        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.RATION_ANALYSIS,
                        ExportData(
                                animal = animal,
                                ration = ration,
                                reference = null,
                                poidsAnimal = 12.0,
                                poidsMetabolique = 6.45,
                                besoinEnergetiqueStandard = 645.0,
                                besoinEnergetiqueTotal = 645.0,
                                energieApportee = 525.0,
                                pourcentageCouverture = 81.4
                        )
                )
        val pdf = File(tempDir, "analyse.pdf")
        rendrePdf(html, pdf)

        assertTrue(pdf.length() > 1024, "le PDF doit faire plus de 1 Ko (${pdf.length()} octets)")
        val entete = pdf.inputStream().use { flux -> ByteArray(4).also { flux.read(it) } }
        assertEquals("%PDF", String(entete, Charsets.US_ASCII), "signature PDF")
    }
}
