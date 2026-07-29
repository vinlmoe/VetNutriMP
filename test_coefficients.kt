import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.DataBase.*
import fr.vetbrain.vetnutri_mp.Enumer.*

// Test simple pour vérifier l'export/import des coefficients
fun testCoefficients() {
    
    

    // 1. Créer une ReferenceEv avec des coefficients personnalisés
    val ref =
            ReferenceEv(
                    uuid = "test-ref-001",
                    nom = "Test Reference avec Coefficients",
                    description = "Test pour vérifier l'export/import des coefficients",
                    espece = Espece.CHIEN,
                    stadePhysio = StadePhysio.ADULTE
            )

    // Ajouter des coefficients personnalisés
    ref.getModk1().clear()
    ref.getModk1()
            .add(CoefP(description = "Coefficient personnalisé k1", coef = 1.2, groupUUID = 0))
    ref.getModk1().add(CoefP(description = "Autre coef k1", coef = 0.8, groupUUID = 1))

    ref.getModk2().clear()
    ref.getModk2()
            .add(CoefP(description = "Coefficient personnalisé k2", coef = 1.5, groupUUID = 2))

    ref.getModk3().clear()
    ref.getModk3()
            .add(CoefP(description = "Coefficient personnalisé k3", coef = 0.9, groupUUID = 3))

    

    // 2. Tester l'export (toApiRef)
    val refApi = ref.toApiRef()
    
    
    
    

    // Afficher les coefficients exportés
    refApi.coefficients.forEach { coef ->
        
    }

    // 3. Vérifier que les coefficients sont bien exportés
    val k1Count = refApi.coefficients.count { it.groupType == "k1" }
    val k2Count = refApi.coefficients.count { it.groupType == "k2" }
    val k3Count = refApi.coefficients.count { it.groupType == "k3" }

    
    
    
    

    // 4. Test de validation
    val totalExpected = ref.getModk1().size + ref.getModk2().size + ref.getModk3().size
    val totalExported = refApi.coefficients.size

    if (totalExported == totalExpected) {
        
    } else {
        
    }

    
}

// Fonction utilitaire pour répéter une chaîne
operator fun String.times(n: Int): String = repeat(n)

// Lancer le test
fun main() {
    testCoefficients()
}
