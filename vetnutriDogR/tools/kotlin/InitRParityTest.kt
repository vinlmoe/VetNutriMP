package fr.vetbrain.vetnutri_mp.Interop

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.InMemoryEquationRepository
import fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator
import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertTrue

/** Independent live Kotlin oracle consumed by vetnutriDogR/tests/init-parity.R. */
class InitRParityTest {
    @Test
    fun exportLiveInitOracle() = runBlocking {
        val source = File("src/commonMain/resources/data/vetnutri_export_init.json")
            .takeIf { it.exists() }
            ?: File("composeApp/src/commonMain/resources/data/vetnutri_export_init.json")
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        val root = json.parseToJsonElement(source.readText()).jsonObject
        val foods = root.getValue("foods").jsonArray.map { json.decodeFromJsonElement<FoodApi>(it) }
        val equations = root.getValue("equations").jsonArray.map {
            json.decodeFromJsonElement<EquationApi>(it).toDomain()
        }.associateBy { it.uuid }
        val repository = InMemoryEquationRepository()
        equations.values.forEach { repository.saveEquation(it) }
        val refs = root.getValue("references").jsonArray.map {
            json.decodeFromJsonElement<ReferenceEvApi>(it)
        }.filter { !it.maladie && it.espece == "CHIEN" && it.equationBEE != null && it.equationBW != null }
            .filter { it.stadePhysio == "ADULTE" && !equations.getValue(it.equationBEE!!).equationScript.contains("AW") }.take(3)
        val dogFoods = foods.filter { it.species.isEmpty() || it.species.any { s -> s == "CHIEN" || s == "CH" } }
        val samples = dogFoods.distinctBy { it.kind } + dogFoods.filter { it.energyPerSpecies.isNotEmpty() }.take(2)
        val records = buildJsonArray {
            for (api in refs) {
                val ref = ReferenceEv(uuid = api.uuid, espece = Espece.valueOf(api.espece))
                ref.equationDEcom = equations[api.equationDEcom]
                ref.equationDEraw = equations[api.equationDEraw]
                ref.equationsNut = api.equationsNut.mapNotNull { equations[it] }.toMutableList()
                for (food in samples) {
                    val domain = food.toDomain()
                    val item = AlimentRation(aliment = domain, quantite = 100.0)
                    val energy = EquationEvaluator.calculerEnergiePour100g(item, repository, ref)
                    val nutrients = buildJsonObject {
                        for (label in food.nutrients.keys + "DM") {
                            val nutrient = NutrientResolver.AllNutrientResolver(label) ?: continue
                            val value = item.getNutrientWithComplementary(nutrient, repository, ref)
                            put(label, value?.let { JsonPrimitive(it) } ?: JsonNull)
                        }
                    }
                    add(buildJsonObject {
                        put("reference_id", api.uuid)
                        put("food_id", food.uuid)
                        put("name", domain.nom)
                        put("group", domain.group?.name)
                        put("kind", domain.typeAliment?.name)
                        put("energy", energy)
                        put("nutrients", nutrients)
                        put("units", buildJsonObject {
                            for (label in food.nutrients.keys + "DM") {
                                val nutrient = NutrientResolver.AllNutrientResolver(label) ?: continue
                                put(label, nutrient.ue.displayName)
                            }
                        })
                        put("standard_kcal", ExpressionEvaluator.evaluer(equations.getValue(api.equationBEE!!).equationScript, mapOf("BW" to 10.0)))
                        put("metabolic_weight", ExpressionEvaluator.evaluer(equations.getValue(api.equationBW!!).equationScript, mapOf("BW" to 10.0)))
                    })
                }
            }
        }
        val requirements = buildJsonArray {
            for (ref in refs) {
                val bee = ExpressionEvaluator.evaluer(equations.getValue(ref.equationBEE!!).equationScript, mapOf("BW" to 10.0))
                val mw = ExpressionEvaluator.evaluer(equations.getValue(ref.equationBW!!).equationScript, mapOf("BW" to 10.0))
                for (n in ref.nutrients) {
                    val nutrient = NutrientResolver.AllNutrientResolver(n.nutrientLabel) ?: continue
                    val absolute = if (estNutrimentAnalysisRatio(nutrient)) n.quantity
                        else calculerBesoinAbsolu(n.quantity, UnitReqEnum.getById(n.uniteReqId), bee, 10.0, mw)
                    add(buildJsonObject {
                        put("reference_id", ref.uuid)
                        put("nutrient_id", n.nutrientLabel)
                        put("reflevel", n.reflevel)
                        put("quantity", n.quantity)
                        put("uniteReqId", n.uniteReqId)
                        put("unit", nutrient.ue.displayName)
                        put("species", ref.espece)
                        put("stage", ref.stadePhysio)
                        put("absolute_requirement", absolute)
                    })
                }
            }
        }
        assertTrue(records.isNotEmpty())
        val output = File(System.getProperty("vetnutri.dog.module"), "build/kotlin-parity.json")
        output.parentFile.mkdirs()
        output.writeText(buildJsonObject {
            put("source_md5", MessageDigest.getInstance("MD5").digest(source.readBytes()).joinToString("") { "%02x".format(it) })
            put("records", records)
            put("requirements", requirements)
        }.toString())
    }
}
