package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin

/**
 * Exemples de personnalisation des préférences par défaut
 * 
 * Ce fichier montre comment créer des configurations personnalisées
 * pour différents cas d'usage ou environnements.
 * 
 * Pour utiliser ces configurations, copiez-les dans DefaultPreferencesConfig.kt
 * ou créez votre propre fichier de configuration.
 */
object CustomPreferencesExample {
    
    /** Configuration pour un environnement de recherche (plus de nutriments) */
    object ResearchEnvironment {
        
        /** Nutriments étendus pour la recherche */
        val EXTENDED_NUTRIENTS = mapOf(
            "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
            "MACRO" to listOf(10, 11, 12, 13, 14, 15, 16), // Ca, P, Mg, Na, K, Cl, S
            "MIN" to listOf(14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), // Tous les minéraux
            "VITAM" to listOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58), // Toutes les vitamines
            "LIPID" to listOf(25, 26, 27, 28, 29), // Tous les acides gras
            "AMA" to listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), // Tous les acides aminés
            "ANA" to emptyList(),
            "OTHER" to emptyList(),
            "INDICAT" to emptyList(),
            "INGREDIENT" to emptyList(),
            "ENERGIE" to emptyList()
        )
        
        /** Préférences de recherche pour les chiens */
        val CHIEN_RESEARCH = PreferencesEspece(
            espece = Espece.CHIEN.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = EXTENDED_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
    }
    
    /** Configuration pour un environnement clinique (nutriments essentiels uniquement) */
    object ClinicalEnvironment {
        
        /** Nutriments essentiels pour la clinique */
        val ESSENTIAL_NUTRIENTS = mapOf(
            "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
            "MACRO" to listOf(10, 11, 12, 13), // Ca, P, Mg, Na
            "MIN" to listOf(14, 15, 16), // K, Cl, S
            "VITAM" to listOf(45, 46, 47), // Vit A, D, E
            "LIPID" to listOf(25, 26), // AG saturés, insaturés
            "AMA" to listOf(0, 1, 2, 3, 4, 5), // Acides aminés essentiels principaux
            "ANA" to emptyList(),
            "OTHER" to emptyList(),
            "INDICAT" to emptyList(),
            "INGREDIENT" to emptyList(),
            "ENERGIE" to emptyList()
        )
        
        /** Préférences cliniques pour les chats */
        val CHAT_CLINICAL = PreferencesEspece(
            espece = Espece.CHAT.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = ESSENTIAL_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
    }
    
    /** Configuration pour les équidés (chevaux, ânes) */
    object EquineEnvironment {
        
        /** Nutriments spécifiques aux équidés */
        val EQUINE_NUTRIENTS = mapOf(
            "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
            "MACRO" to listOf(10, 11, 12, 13, 14, 15, 16), // Ca, P, Mg, Na, K, Cl, S
            "MIN" to listOf(14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), // Tous les minéraux
            "VITAM" to listOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58), // Toutes les vitamines
            "LIPID" to listOf(25, 26, 27, 28, 29), // Tous les acides gras
            "AMA" to listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), // Tous les acides aminés
            "ANA" to emptyList(),
            "OTHER" to emptyList(),
            "INDICAT" to emptyList(),
            "INGREDIENT" to emptyList(),
            "ENERGIE" to emptyList()
        )
        
        /** Préférences pour les chevaux (par kg métabolique) */
        val CHEVAL_EQUINE = PreferencesEspece(
            espece = Espece.CHEVAL.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG_METABOLIQUE.id,
            nutrimentsSelectionnes = EQUINE_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
    }
    
    /** Configuration pour les rongeurs de laboratoire */
    object LaboratoryEnvironment {
        
        /** Nutriments pour la recherche sur rongeurs */
        val LABORATORY_NUTRIENTS = mapOf(
            "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
            "MACRO" to listOf(10, 11, 12, 13, 14, 15, 16), // Ca, P, Mg, Na, K, Cl, S
            "MIN" to listOf(14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), // Tous les minéraux
            "VITAM" to listOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58), // Toutes les vitamines
            "LIPID" to listOf(25, 26, 27, 28, 29), // Tous les acides gras
            "AMA" to listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), // Tous les acides aminés
            "ANA" to emptyList(),
            "OTHER" to emptyList(),
            "INDICAT" to emptyList(),
            "INGREDIENT" to emptyList(),
            "ENERGIE" to emptyList()
        )
        
        /** Préférences pour les rats de laboratoire */
        val RAT_LABORATORY = PreferencesEspece(
            espece = Espece.RAT.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = LABORATORY_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
        
        /** Préférences pour les souris de laboratoire */
        val SOURIS_LABORATORY = PreferencesEspece(
            espece = Espece.SOURIS.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = LABORATORY_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
    }
    
    /** Configuration pour les animaux exotiques */
    object ExoticEnvironment {
        
        /** Nutriments pour les animaux exotiques */
        val EXOTIC_NUTRIENTS = mapOf(
            "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
            "MACRO" to listOf(10, 11, 12, 13, 14, 15, 16), // Ca, P, Mg, Na, K, Cl, S
            "MIN" to listOf(14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), // Tous les minéraux
            "VITAM" to listOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58), // Toutes les vitamines
            "LIPID" to listOf(25, 26, 27, 28, 29), // Tous les acides gras
            "AMA" to listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), // Tous les acides aminés
            "ANA" to emptyList(),
            "OTHER" to emptyList(),
            "INDICAT" to emptyList(),
            "INGREDIENT" to emptyList(),
            "ENERGIE" to emptyList()
        )
        
        /** Préférences pour les furets */
        val FURET_EXOTIC = PreferencesEspece(
            espece = Espece.FURET.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = EXOTIC_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
        
        /** Préférences pour les lapins */
        val LAPIN_EXOTIC = PreferencesEspece(
            espece = Espece.LAPIN.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = EXOTIC_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
    }
    
    /** Configuration pour les primates */
    object PrimateEnvironment {
        
        /** Nutriments pour les primates */
        val PRIMATE_NUTRIENTS = mapOf(
            "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
            "MACRO" to listOf(10, 11, 12, 13, 14, 15, 16), // Ca, P, Mg, Na, K, Cl, S
            "MIN" to listOf(14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), // Tous les minéraux
            "VITAM" to listOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58), // Toutes les vitamines
            "LIPID" to listOf(25, 26, 27, 28, 29), // Tous les acides gras
            "AMA" to listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), // Tous les acides aminés
            "ANA" to emptyList(),
            "OTHER" to emptyList(),
            "INDICAT" to emptyList(),
            "INGREDIENT" to emptyList(),
            "ENERGIE" to emptyList()
        )
        
        /** Préférences pour les primates */
        val PRIMATE_EXOTIC = PreferencesEspece(
            espece = Espece.PRIMATE.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = PRIMATE_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
    }
    
    /** Configuration pour les herbivores */
    object HerbivoreEnvironment {
        
        /** Nutriments pour les herbivores */
        val HERBIVORE_NUTRIENTS = mapOf(
            "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
            "MACRO" to listOf(10, 11, 12, 13, 14, 15, 16), // Ca, P, Mg, Na, K, Cl, S
            "MIN" to listOf(14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), // Tous les minéraux
            "VITAM" to listOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58), // Toutes les vitamines
            "LIPID" to listOf(25, 26, 27, 28, 29), // Tous les acides gras
            "AMA" to listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), // Tous les acides aminés
            "ANA" to emptyList(),
            "OTHER" to emptyList(),
            "INDICAT" to emptyList(),
            "INGREDIENT" to emptyList(),
            "ENERGIE" to emptyList()
        )
        
        /** Préférences pour les herbivores (par kg métabolique) */
        val HERBIVORE_EXOTIC = PreferencesEspece(
            espece = Espece.HERBIVORE.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG_METABOLIQUE.id,
            nutrimentsSelectionnes = HERBIVORE_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
        
        /** Préférences pour les folivores (par kg métabolique) */
        val FOLIVORE_EXOTIC = PreferencesEspece(
            espece = Espece.FOLIVORE.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG_METABOLIQUE.id,
            nutrimentsSelectionnes = HERBIVORE_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
    }
    
    /** Configuration pour les carnivores */
    object CarnivoreEnvironment {
        
        /** Nutriments pour les carnivores */
        val CARNIVORE_NUTRIENTS = mapOf(
            "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
            "MACRO" to listOf(10, 11, 12, 13, 14, 15, 16), // Ca, P, Mg, Na, K, Cl, S
            "MIN" to listOf(14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), // Tous les minéraux
            "VITAM" to listOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58), // Toutes les vitamines
            "LIPID" to listOf(25, 26, 27, 28, 29), // Tous les acides gras
            "AMA" to listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), // Tous les acides aminés
            "ANA" to emptyList(),
            "OTHER" to emptyList(),
            "INDICAT" to emptyList(),
            "INGREDIENT" to emptyList(),
            "ENERGIE" to emptyList()
        )
        
        /** Préférences pour les félins (par kg) */
        val FELIN_CARNIVORE = PreferencesEspece(
            espece = Espece.FELIN.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = CARNIVORE_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
        
        /** Préférences pour les canins (par kg) */
        val CANIN_CARNIVORE = PreferencesEspece(
            espece = Espece.CANIN.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = CARNIVORE_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
    }
    
    /** Configuration pour les animaux génériques */
    object GenericEnvironment {
        
        /** Nutriments génériques */
        val GENERIC_NUTRIENTS = mapOf(
            "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
            "MACRO" to listOf(10, 11, 12, 13), // Ca, P, Mg, Na
            "MIN" to listOf(14, 15, 16), // K, Cl, S
            "VITAM" to listOf(45, 46, 47), // Vit A, D, E
            "LIPID" to listOf(25, 26), // AG saturés, insaturés
            "AMA" to emptyList(),
            "ANA" to emptyList(),
            "OTHER" to emptyList(),
            "INDICAT" to emptyList(),
            "INGREDIENT" to emptyList(),
            "ENERGIE" to emptyList()
        )
        
        /** Préférences génériques (par kg) */
        val GENERIC_ANIMAL = PreferencesEspece(
            espece = Espece.CH.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
            nutrimentsSelectionnes = GENERIC_NUTRIENTS,
            equationsComplementaires = emptyMap()
        )
    }
}
