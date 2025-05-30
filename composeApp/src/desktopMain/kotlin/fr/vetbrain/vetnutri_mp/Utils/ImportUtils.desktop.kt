package fr.vetbrain.vetnutri_mp.Utils

import java.io.File

/**
 * Version desktop de l'importateur de références nutritionnelles Similaire aux autres importateurs
 * existants dans le projet
 */

/** Importe des références nutritionnelles depuis un fichier .vbnr.json */
fun importNutritionalRequirementsFromFile(
        filePath: String
): List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> {
  return try {
    println("📂 Lecture du fichier: $filePath")
    val file = File(filePath)

    if (!file.exists()) {
      println("❌ Fichier non trouvé: $filePath")
      return emptyList()
    }

    if (!file.canRead()) {
      println("❌ Impossible de lire le fichier: $filePath")
      return emptyList()
    }

    val jsonContent = file.readText(Charsets.UTF_8)
    println("✅ Fichier lu avec succès (${jsonContent.length} caractères)")

    // Utiliser la fonction d'importation commune
    ImportUtils.importNutritionalRequirementsFromJson(jsonContent)
  } catch (e: Exception) {
    println("💥 Erreur lors de la lecture du fichier: ${e.message}")
    e.printStackTrace()
    emptyList()
  }
}

/** Teste l'importation d'un fichier de références nutritionnelles */
fun testNutritionalRequirementImportFromFile(filePath: String): String {
  return try {
    println("🧪 Test d'importation du fichier: $filePath")
    val file = File(filePath)

    if (!file.exists()) {
      return "❌ Fichier non trouvé: $filePath"
    }

    val jsonContent = file.readText(Charsets.UTF_8)

    // Utiliser la fonction de test commune
    ImportUtils.testNutritionalRequirementImport(jsonContent)
  } catch (e: Exception) {
    "💥 Erreur lors du test: ${e.message}\n${e.stackTraceToString()}"
  }
}

/**
 * Fonction utilitaire pour créer un exemple de données de test Utile pour les tests et la
 * démonstration
 */
fun createSampleNutritionalRequirementJson(): String {
  return """
    [
      {
        "reference": {
          "UUID": "123e4567-e89b-12d3-a456-426614174000",
          "nom": "Besoins nutritionnels chien adulte FEDIAF",
          "description": "Référence FEDIAF pour chien adulte en bonne santé",
          "disease": false,
          "nameDisease": "",
          "nameEnergy": "Énergie métabolisable",
          "consistent": 1,
          "species": "CHIEN",
          "sPhysio": "ADULTE",
          "namek1": "Facteur activité",
          "namek2": "Facteur température",
          "namek3": "",
          "namek4": "",
          "namek5": ""
        },
        "allEquations": [
          {
            "UUID": "987fcdeb-51a2-43d7-8c9b-123456789abc",
            "name": "BEE Chien Adulte",
            "description": "Besoin énergétique basal pour chien adulte selon FEDIAF",
            "equationScript": "130 * BW^0.75",
            "kind": 0,
            "specie": "CHIEN",
            "consistent": true,
            "jvscript": false,
            "var": ["BW"]
          }
        ],
        "allBibliographicReferences": [
          {
            "UUID": "456e7890-a12b-34c5-d678-901234567890",
            "author": "FEDIAF",
            "title": "Nutritional Guidelines for Complete and Complementary Pet Food",
            "journal": "FEDIAF Guidelines",
            "year": 2021,
            "volume": "",
            "issue": "",
            "pages": "",
            "doi": "",
            "pmid": "",
            "url": "https://fediaf.org/self-regulation/nutrition/",
            "note": "Recommandations officielles FEDIAF 2021"
          }
        ],
        "nutrientRequirements": [
          {
            "nutrient": {
              "MNE": "BASE",
              "coef": 0,
              "label": "Protéines",
              "ue": "PERCENT"
            },
            "referenceLevel": "MIN",
            "quantity": 18.0,
            "unit": "PERCENT",
            "unitRequirement": "DM",
            "bibliographicReference": {
              "UUID": "456e7890-a12b-34c5-d678-901234567890",
              "author": "FEDIAF",
              "title": "Nutritional Guidelines",
              "year": 2021
            }
          },
          {
            "nutrient": {
              "MNE": "BASE",
              "coef": 1,
              "label": "Lipides",
              "ue": "PERCENT"
            },
            "referenceLevel": "MIN",
            "quantity": 5.5,
            "unit": "PERCENT",
            "unitRequirement": "DM",
            "bibliographicReference": {
              "UUID": "456e7890-a12b-34c5-d678-901234567890",
              "author": "FEDIAF",
              "title": "Nutritional Guidelines",
              "year": 2021
            }
          }
        ]
      }
    ]
    """.trimIndent()
}

/** Sauvegarde un exemple de fichier .vbnr.json pour les tests */
fun saveSampleNutritionalRequirementFile(filePath: String): Boolean {
  return try {
    val file = File(filePath)
    file.parentFile?.mkdirs()
    file.writeText(createSampleNutritionalRequirementJson(), Charsets.UTF_8)
    println("✅ Fichier d'exemple créé: $filePath")
    true
  } catch (e: Exception) {
    println("❌ Erreur lors de la création du fichier d'exemple: ${e.message}")
    false
  }
}

/** Fonction principale de démonstration Peut être appelée pour tester l'importateur */
fun demonstrateNutritionalRequirementImport() {
  println("🚀 Démonstration de l'importateur de références nutritionnelles")
  println("=".repeat(60))

  // Créer un fichier d'exemple
  val sampleFilePath = "sample_nutritional_requirements.vbnr.json"

  println("\n1. Création d'un fichier d'exemple...")
  if (saveSampleNutritionalRequirementFile(sampleFilePath)) {

    println("\n2. Test de l'importation...")
    val testResult = testNutritionalRequirementImportFromFile(sampleFilePath)
    println(testResult)

    println("\n3. Importation réelle...")
    val references = importNutritionalRequirementsFromFile(sampleFilePath)

    if (references.isNotEmpty()) {
      println("🎉 Importation réussie! ${references.size} référence(s) importée(s)")
      references.forEach { ref ->
        println("  • ${ref.nom} (${ref.espece} - ${ref.stadePhysio})")
        println("    - Nutriments MIN: ${ref.getRefMapMin().size}")
        println("    - Nutriments MAX: ${ref.getRefMapMax().size}")
        println("    - Équations: ${ref.equationsNut.size}")
        if (ref.equationBEE != null) println("    - Équation BEE: ${ref.equationBEE!!.name}")
      }
    } else {
      println("❌ Aucune référence importée")
    }

    // Nettoyer le fichier d'exemple
    File(sampleFilePath).delete()
    println("\n✨ Démonstration terminée")
  }
}
