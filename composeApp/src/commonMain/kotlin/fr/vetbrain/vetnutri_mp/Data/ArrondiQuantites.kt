package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Utils.NumberUtils

/**
 * Règles métier d'arrondi des quantités d'aliments d'une ration.
 * Partagées par le dialogue d'ajustement multi-nutriments (View/AnalNut), le solveur
 * d'ajustement par contraintes (RationConstraintAdjuster) et l'exploration multiration.
 */

/** La cellulose est ajustée à un objectif clinique de cinq fois le besoin de référence. */
fun adjustmentNeedMultiplier(nutrient: Nutrient): Double =
        if (nutrient == NutrientMain.CELLULOSE) 5.0 else 1.0

/**
 * Arrondit la quantité selon les règles métier
 * @param alimentRation L'aliment concerné
 * @param quantite La quantité à arrondir
 * @return La quantité arrondie
 */
fun arrondirQuantiteSelonRegles(alimentRation: AlimentRation, quantite: Double): Double {
        val aliment = alimentRation.aliment
        if (aliment != null) {
                val cont = aliment.cont
                val quantInt = aliment.quantInt

                // Règle contenant: si contenant défini et quantInt > 0, arrondir au multiple de quantInt/2
                if (cont != null && cont != ContEnum.NO && quantInt != null && quantInt > 0.0) {
                        val step = quantInt / 2.0
                        return NumberUtils.arrondirAuPas(quantite, step).coerceAtLeast(0.0)
                }
        }

        // Règles générales par défaut
        val arrondie = when {
                quantite < 20.0 -> kotlin.math.round(quantite) // Arrondi au gramme
                quantite < 200.0 -> kotlin.math.round(quantite / 5.0) * 5.0 // Arrondi aux 5g
                else -> kotlin.math.round(quantite / 25.0) * 25.0 // Arrondi aux 25g
        }
        return arrondie.coerceAtLeast(0.0)
}
