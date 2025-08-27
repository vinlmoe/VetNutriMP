package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin

/**
 * Configuration des préférences par défaut de l'application
 * 
 * Ce fichier centralise toutes les valeurs par défaut pour faciliter
 * la personnalisation des préférences initiales de l'application.
 */
object DefaultPreferencesConfig {
    
    /** Version des préférences par défaut */
    const val DEFAULT_VERSION = 1
    
    /** Type d'expression des besoins par défaut */
    val DEFAULT_EXPRESSION_TYPE = TypeExpressionBesoin.PAR_KCAL
    
    /** Nutriments par défaut sélectionnés par catégorie */
    object DefaultNutrients {
        /** Nutriments de base sélectionnés par défaut */
        val BASE = listOf(
            0,
            1,  // Protéines
            2,  // Lipides
            4,  // ENA (Extrait non azoté)
            6,
            7   // Cendres
        )
        // Nutriments de base non sélectionnés par défaut :
        // 0,  // Humidité
        // 3,  // Glucides
        // 6,  // Cellulose (nutriment important pour les fibres)
        // 8,  // Énergie
        // 9,  // Sucres
        // 10, // Amidon
        // 11, // Fibre soluble
        // 12, // Fibre totale
        // 13, // NDF (Fibres neutres détergentes)
        // 14  // ADF (Fibres acides détergentes)
        
        /** Macronutriments sélectionnés par défaut */
        val MACRO = listOf(
            0,  // Calcium (Ca)
            1,  // Phosphore (P)
            2,  // Magnésium (Mg)
            3   // Sodium (Na)
        )
        // Macronutriments non sélectionnés par défaut :
        // 4,  // Potassium (K)
        // 5   // Chlore (Cl)
        
        /** Minéraux sélectionnés par défaut */
        val MIN: List<Int> = emptyList()
        // Minéraux non sélectionnés par défaut :
        // 0,  // Fer (Fe)
        // 1,  // Cuivre (Cu)
        // 2,  // Zinc (Zn)
        // 3,  // Manganèse (Mn)
        // 4,  // Iode (I)
        // 5   // Sélénium (Se)
        
        /** Vitamines sélectionnées par défaut */
        val VITAM = listOf(
            0,  // Vitamine A
            2,  // Vitamine D
            3   // Vitamine E
        )
        // Vitamines non sélectionnées par défaut :
        // 1,  // Vitamine C
        // 4,  // Vitamine K
        // 5,  // Thiamine (B1)
        // 6,  // Riboflavine (B2)
        // 7,  // Nicotinamide/Niacine (B3/PP)
        // 8,  // Acide pantothénique (B5)
        // 9,  // Pyridoxine (B6)
        // 10, // Biotine (B8)
        // 11, // Acide folique (B9)
        // 12, // Cyanocobalamine (B12)
        // 13, // Choline
        // 14, // Rétinol
        // 15  // Bêta-carotène
        
        /** Acides gras sélectionnés par défaut */
        val LIPID = listOf(
            0   // Acides gras saturés
        )
        // Acides gras non sélectionnés par défaut :
        // 1,  // Acides gras mono-insaturés
        // 2,  // Acides gras poly-insaturés
        // 3,  // C4:0 (Butyrique)
        // 4,  // C6:0 (Caproïque)
        // 5,  // C8:0 (Caprylique)
        // 6,  // C10:0 (Caprique)
        // 7,  // C12:0 (Laurique)
        // 8,  // C14:0 (Myristique)
        // 9,  // C16:0 (Palmitique)
        // 10, // C18:0 (Stéarique)
        // 11, // C18:1-n9 (Oléique)
        // 12, // C18:2-n6 (Linoléique)
        // 13, // C18:3-n3 (Linolénique)
        // 14, // C20:4-n6 (Arachidonique)
        // 15, // EPA (C20:5-n3)
        // 16, // DHA (C22:6-n3)
        // 17, // Cholestérol
        // 18, // Oméga 3
        // 19, // Oméga 6
        // 20  // EPA+DHA
        
        /** Acides aminés sélectionnés par défaut */
        val AMA = listOf(
            0,  // Alanine
            1,  // Arginine
            2,  // Asparagine
            3,  // Asparate
            4,  // Cystéine
            5,  // Glutamate
            6,  // Glutamine
            7,  // Glycine
            8,  // Histidine
            9,  // Isoleucine
            10, // Leucine
            11, // Lysine
            12, // Méthionine
            13, // Phénylalanine
            14, // Proline
            15, // Pyrrolysine
            16, // Sélénocystéine
            17, // Sérine
            18, // Thréonine
            19, // Tryptophane
            20, // Tyrosine
            21  // Valine
        )
        // Tous les acides aminés essentiels sont sélectionnés par défaut
        
        /** Acides aminés non essentiels (vide par défaut) */
        // Tous les acides aminés (0-21) sont sélectionnés par défaut dans AMA
        // Voir la liste complète dans AAEnum.kt
        
        /** Autres nutriments (vide par défaut) */
        val OTHER: List<Int> = emptyList()
        // Autres nutriments disponibles (non sélectionnés par défaut) :
        // 0,  // Taurine
        // 1,  // L-Carnitine
        // 2,  // FOS (Fructo-oligosaccharides)
        // 3,  // MOS (Mannan-oligosaccharides)
        // 5,  // Saccharose
        // 6,  // Fructose
        // 7,  // Lactose
        // 8,  // Maltose
        // 9,  // Acide oxalique
        // 10, // Galactose
        // 11, // Glucose
        // 12  // Dextrose
        
        /** Indicateurs (vide par défaut) */
        val INDICAT: List<Int> = emptyList()
        // Indicateurs disponibles (non sélectionnés par défaut) :
        // Aucun indicateur spécifique défini actuellement
        
        /** Ingrédients (vide par défaut) */
        val INGREDIENT: List<Int> = emptyList()
        // Ingrédients disponibles (non sélectionnés par défaut) :
        // Aucun ingrédient spécifique défini actuellement
        
        /** Énergie (vide par défaut) */
        val ENERGIE: List<Int> = emptyList()
        // Énergie disponible (non sélectionnée par défaut) :
        // 0,  // Énergie totale
        // 1,  // iDE (Énergie digestible)
        // 2,  // DEDM (Densité énergétique matière sèche)
        // 4,  // K, PERC, BEE, BE, MW, KPRED (coefficients)
        
        /** NutrientAnalysis (Analyses et ratios - vide par défaut) */
        val ANA: List<Int> = emptyList()
        // NutrientAnalysis disponibles (non sélectionnés par défaut) :
        // 0,  // K/Na (Rapport potassium/sodium)
        // 1,  // P/Ca (Rapport phosphocalcique)
        // 2,  // O6/O3 (Rapport oméga 6/oméga 3)
        // 3,  // Zn/Cu (Rapport zinc/cuivre)
        // 4,  // Prot/P (Rapport protéines/phosphore)
        // 5,  // Méthionine+Cystéine
        // 6,  // Phénylalanine+Tyrosine
        // 7,  // Phosphore non osseux (%)
        // 8,  // Protéine non osseuse (%)
        // 9   // Ratio Prot/Phos non osseux
    }
    
    /** Équations complémentaires par défaut (vide par défaut) */
    val DEFAULT_EQUATIONS = emptyMap<String, String>()
    
    /** Préférences spécifiques par espèce */
    object SpeciesSpecificDefaults {
        
        /** Préférences par défaut pour les chiens */
        val CHIEN = PreferencesEspece(
            espece = Espece.CHIEN.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KCAL.id,
            nutrimentsSelectionnes = mapOf(
                "BASE" to DefaultNutrients.BASE,
                "MACRO" to DefaultNutrients.MACRO,
                "MIN" to DefaultNutrients.MIN,
                "VITAM" to DefaultNutrients.VITAM,
                "LIPID" to DefaultNutrients.LIPID,
                "AMA" to DefaultNutrients.AMA,
                "ANA" to DefaultNutrients.ANA,
                "OTHER" to DefaultNutrients.OTHER,
                "INDICAT" to DefaultNutrients.INDICAT,
                "INGREDIENT" to DefaultNutrients.INGREDIENT,
                "ENERGIE" to DefaultNutrients.ENERGIE
            ),
            equationsComplementaires = DEFAULT_EQUATIONS
        )
        
        /** Préférences par défaut pour les chats */
        val CHAT = PreferencesEspece(
            espece = Espece.CHAT.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KCAL.id,
            nutrimentsSelectionnes = mapOf(
                "BASE" to DefaultNutrients.BASE,
                "MACRO" to DefaultNutrients.MACRO,
                "MIN" to DefaultNutrients.MIN,
                "VITAM" to DefaultNutrients.VITAM,
                "LIPID" to DefaultNutrients.LIPID,
                "AMA" to DefaultNutrients.AMA,
                "ANA" to DefaultNutrients.ANA,
                "OTHER" to DefaultNutrients.OTHER,
                "INDICAT" to DefaultNutrients.INDICAT,
                "INGREDIENT" to DefaultNutrients.INGREDIENT,
                "ENERGIE" to DefaultNutrients.ENERGIE
            ),
            equationsComplementaires = DEFAULT_EQUATIONS
        )
        
        /** Préférences par défaut pour les chevaux */
        val CHEVAL = PreferencesEspece(
            espece = Espece.CHEVAL.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG_METABOLIQUE.id,
            nutrimentsSelectionnes = mapOf(
                "BASE" to DefaultNutrients.BASE,
                "MACRO" to DefaultNutrients.MACRO,
                "MIN" to DefaultNutrients.MIN,
                "VITAM" to DefaultNutrients.VITAM,
                "LIPID" to DefaultNutrients.LIPID,
                "AMA" to DefaultNutrients.AMA,
                "ANA" to DefaultNutrients.ANA,
                "OTHER" to DefaultNutrients.OTHER,
                "INDICAT" to DefaultNutrients.INDICAT,
                "INGREDIENT" to DefaultNutrients.INGREDIENT,
                "ENERGIE" to DefaultNutrients.ENERGIE
            ),
            equationsComplementaires = DEFAULT_EQUATIONS
        )
        
        /** Préférences par défaut pour les lapins */
        val LAPIN = PreferencesEspece(
            espece = Espece.LAPIN.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KCAL.id,
            nutrimentsSelectionnes = mapOf(
                "BASE" to DefaultNutrients.BASE,
                "MACRO" to DefaultNutrients.MACRO,
                "MIN" to DefaultNutrients.MIN,
                "VITAM" to DefaultNutrients.VITAM,
                "LIPID" to DefaultNutrients.LIPID,
                "AMA" to DefaultNutrients.AMA,
                "ANA" to DefaultNutrients.ANA,
                "OTHER" to DefaultNutrients.OTHER,
                "INDICAT" to DefaultNutrients.INDICAT,
                "INGREDIENT" to DefaultNutrients.INGREDIENT,
                "ENERGIE" to DefaultNutrients.ENERGIE
            ),
            equationsComplementaires = DEFAULT_EQUATIONS
        )
        
        /** Préférences par défaut pour les furets */
        val FURET = PreferencesEspece(
            espece = Espece.FURET.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KCAL.id,
            nutrimentsSelectionnes = mapOf(
                "BASE" to DefaultNutrients.BASE,
                "MACRO" to DefaultNutrients.MACRO,
                "MIN" to DefaultNutrients.MIN,
                "VITAM" to DefaultNutrients.VITAM,
                "LIPID" to DefaultNutrients.LIPID,
                "AMA" to DefaultNutrients.AMA,
                "ANA" to DefaultNutrients.ANA,
                "OTHER" to DefaultNutrients.OTHER,
                "INDICAT" to DefaultNutrients.INDICAT,
                "INGREDIENT" to DefaultNutrients.INGREDIENT,
                "ENERGIE" to DefaultNutrients.ENERGIE
            ),
            equationsComplementaires = DEFAULT_EQUATIONS
        )
        
        /** Préférences par défaut pour les rongeurs (rats, souris) */
        val RONGEURS = PreferencesEspece(
            espece = Espece.RAT.name, // Utilisé comme modèle pour tous les rongeurs
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KCAL.id,
            nutrimentsSelectionnes = mapOf(
                "BASE" to DefaultNutrients.BASE,
                "MACRO" to DefaultNutrients.MACRO,
                "MIN" to DefaultNutrients.MIN,
                "VITAM" to DefaultNutrients.VITAM,
                "LIPID" to DefaultNutrients.LIPID,
                "AMA" to DefaultNutrients.AMA,
                "ANA" to DefaultNutrients.ANA,
                "OTHER" to DefaultNutrients.OTHER,
                "INDICAT" to DefaultNutrients.INDICAT,
                "INGREDIENT" to DefaultNutrients.INGREDIENT,
                "ENERGIE" to DefaultNutrients.ENERGIE
            ),
            equationsComplementaires = DEFAULT_EQUATIONS
        )
        
        /** Préférences par défaut pour les primates */
        val PRIMATE = PreferencesEspece(
            espece = Espece.PRIMATE.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KCAL.id,
            nutrimentsSelectionnes = mapOf(
                "BASE" to DefaultNutrients.BASE,
                "MACRO" to DefaultNutrients.MACRO,
                "MIN" to DefaultNutrients.MIN,
                "VITAM" to DefaultNutrients.VITAM,
                "LIPID" to DefaultNutrients.LIPID,
                "AMA" to DefaultNutrients.AMA,
                "ANA" to DefaultNutrients.ANA,
                "OTHER" to DefaultNutrients.OTHER,
                "INDICAT" to DefaultNutrients.INDICAT,
                "INGREDIENT" to DefaultNutrients.INGREDIENT,
                "ENERGIE" to DefaultNutrients.ENERGIE
            ),
            equationsComplementaires = DEFAULT_EQUATIONS
        )
        
        /** Préférences par défaut pour les herbivores */
        val HERBIVORE = PreferencesEspece(
            espece = Espece.HERBIVORE.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KCAL.id,
            nutrimentsSelectionnes = mapOf(
                "BASE" to DefaultNutrients.BASE,
                "MACRO" to DefaultNutrients.MACRO,
                "MIN" to DefaultNutrients.MIN,
                "VITAM" to DefaultNutrients.VITAM,
                "LIPID" to DefaultNutrients.LIPID,
                "AMA" to DefaultNutrients.AMA,
                "ANA" to DefaultNutrients.ANA,
                "OTHER" to DefaultNutrients.OTHER,
                "INDICAT" to DefaultNutrients.INDICAT,
                "INGREDIENT" to DefaultNutrients.INGREDIENT,
                "ENERGIE" to DefaultNutrients.ENERGIE
            ),
            equationsComplementaires = DEFAULT_EQUATIONS
        )
        
        /** Préférences par défaut pour les folivores */
        val FOLIVORE = PreferencesEspece(
            espece = Espece.FOLIVORE.name,
            typeExpressionBesoinId = TypeExpressionBesoin.PAR_KCAL.id,
            nutrimentsSelectionnes = mapOf(
                "BASE" to DefaultNutrients.BASE,
                "MACRO" to DefaultNutrients.MACRO,
                "MIN" to DefaultNutrients.MIN,
                "VITAM" to DefaultNutrients.VITAM,
                "LIPID" to DefaultNutrients.LIPID,
                "AMA" to DefaultNutrients.AMA,
                "ANA" to DefaultNutrients.ANA,
                "OTHER" to DefaultNutrients.OTHER,
                "INDICAT" to DefaultNutrients.INDICAT,
                "INGREDIENT" to DefaultNutrients.INGREDIENT,
                "ENERGIE" to DefaultNutrients.ENERGIE
            ),
            equationsComplementaires = DEFAULT_EQUATIONS
        )
    }
    
    /** Obtient les préférences par défaut pour une espèce donnée */
    fun getDefaultPreferencesForSpecies(espece: Espece): PreferencesEspece {
        return when (espece) {
            Espece.CHIEN -> SpeciesSpecificDefaults.CHIEN
            Espece.CHAT -> SpeciesSpecificDefaults.CHAT
            Espece.CHEVAL -> SpeciesSpecificDefaults.CHEVAL
            Espece.LAPIN -> SpeciesSpecificDefaults.LAPIN
            Espece.FURET -> SpeciesSpecificDefaults.FURET
            Espece.RAT, Espece.SOURIS -> SpeciesSpecificDefaults.RONGEURS
            Espece.PRIMATE -> SpeciesSpecificDefaults.PRIMATE
            Espece.HERBIVORE -> SpeciesSpecificDefaults.HERBIVORE
            Espece.FELIN -> SpeciesSpecificDefaults.CHAT // Utilise les mêmes préférences que les chats
            Espece.CANIN -> SpeciesSpecificDefaults.CHIEN // Utilise les mêmes préférences que les chiens
            Espece.FOLIVORE -> SpeciesSpecificDefaults.FOLIVORE
            Espece.CH -> SpeciesSpecificDefaults.CHIEN // Espèce générique, utilise les préférences des chiens
        }
    }
    
    /** Obtient toutes les préférences par défaut pour toutes les espèces */
    fun getAllDefaultPreferences(): Map<String, PreferencesEspece> {
        return Espece.valuesExcept().associate { espece ->
            espece.name to getDefaultPreferencesForSpecies(espece)
        }
    }
    
    /** Crée une instance PreferencesApplication avec toutes les préférences par défaut */
    fun createDefaultPreferencesApplication(): PreferencesApplication {
        return PreferencesApplication(
            preferencesParEspece = getAllDefaultPreferences(),
            versionPreferences = DEFAULT_VERSION
        )
    }
}