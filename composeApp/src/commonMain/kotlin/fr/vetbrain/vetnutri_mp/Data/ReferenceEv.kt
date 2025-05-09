package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Classe représentant une référence évaluée basée sur la classe ReferenceEv du projet Java original
 * Cette classe gère les références nutritionnelles pour les différents nutriments
 *
 * @property uuid Identifiant unique de la référence
 * @property nom Nom de la référence
 * @property description Description de la référence
 * @property maladie Indique si la référence concerne une maladie
 * @property nomMaladie Nom de la maladie si applicable
 * @property nomEnergie Nom de l'énergie
 * @property consistent Indique si la référence est consistante (stocké comme Int en base, 1=true, 0=false)
 * @property espece Espèce concernée par la référence
 * @property stadePhysio Stade physiologique concerné par la référence
 */
data class ReferenceEv(
        val uuid: String = UUID.randomUUID().toString(),
        var nom: String = "",
        var description: String = "",
        var maladie: Boolean = false,
        var nomMaladie: String = "",
        var nomEnergie: String = "",
        var consistent: Boolean = false,
        var espece: Espece = Espece.CHIEN,
        var stadePhysio: StadePhysio = StadePhysio.ADULTE
) {
        private val refMapMin: MutableMap<Nutrient, Nut4Ref> = HashMap()
        private val refMapMax: MutableMap<Nutrient, Nut4Ref> = HashMap()
        private val refMapOMin: MutableMap<Nutrient, Nut4Ref> = HashMap()
        private val refMapOMax: MutableMap<Nutrient, Nut4Ref> = HashMap()

        // Variable contenant les équations
        var equationBW: Equation? = null
        var equationBEE: Equation? = null
        var equationDEcom: Equation? = null
        var equationDEraw: Equation? = null
        var equationME: Equation? = null
        var equationsNut: MutableList<Equation> = ArrayList()

        // Coefficients modificateurs
        private val modk1: ArrayList<CoefP> = ArrayList()
        private val modk2: ArrayList<CoefP> = ArrayList()
        private val modk3: ArrayList<CoefP> = ArrayList()
        private val modk4: ArrayList<CoefP> = ArrayList()
        private val modk5: ArrayList<CoefP> = ArrayList()

        var nomk1: String = ""
        var nomk2: String = ""
        var nomk3: String = ""
        var nomk4: String = ""
        var nomk5: String = ""

        companion object {
                /**
                 * Génère un UUID aléatoire
                 * @return Un nouvel UUID sous forme de chaîne
                 */
                fun generateUUID(): String {
                        return (System.currentTimeMillis() + (Math.random() * 10000).toInt())
                                .toString()
                }
        }

        init {
                // Initialisation des coefficients par défaut
                modk1.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 0))
                modk2.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 1))
                modk3.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 2))
                modk4.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 3))
                modk5.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 4))
        }

        // Constructeur secondaire pour compatibilité
        constructor(uuid: String? = null) : this(uuid = uuid ?: generateUUID())

        /**
         * Définit la valeur d'un nutriment pour un niveau de référence donné
         *
         * @param valeur La valeur du nutriment
         * @param nutrient Le nutriment concerné
         * @param niveauRef Le niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
         * @param uniteReq L'unité de la valeur
         * @param biblio La référence bibliographique associée
         */
        fun definirNutriment(
                valeur: Float,
                nutrient: Nutrient,
                niveauRef: Reflevel,
                uniteReq: UnitReqEnum,
                biblio: BiblioRef
        ) {
                obtenirMap(niveauRef)[nutrient] =
                        Nut4Ref(
                                nutrient = nutrient,
                                niveauRef = niveauRef,
                                quantite = valeur,
                                unite = nutrient.ue,
                                uniteReq = uniteReq,
                                citation = biblio
                        )
        }

        /**
         * Récupère la valeur d'un nutriment pour un niveau de référence donné
         *
         * @param nutrient Le nutriment concerné
         * @param niveauRef Le niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
         * @return La valeur du nutriment ou -1 si non trouvé
         */
        fun obtenirNutriment(nutrient: Nutrient, niveauRef: Reflevel): Float {
                return if (contientNutriment(nutrient, niveauRef)) {
                        obtenirMap(niveauRef)[nutrient]?.quantite ?: -1f
                } else {
                        -1f
                }
        }

        /**
         * Vérifie si un nutriment est défini pour un niveau de référence donné
         *
         * @param nutrient Le nutriment concerné
         * @param niveauRef Le niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
         * @return true si le nutriment est défini, false sinon
         */
        fun contientNutriment(nutrient: Nutrient, niveauRef: Reflevel): Boolean {
                return obtenirMap(niveauRef).containsKey(nutrient)
        }

        /**
         * Supprime un nutriment pour un niveau de référence donné
         *
         * @param nutrient Le nutriment concerné
         * @param niveauRef Le niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
         */
        fun supprimerNutriment(nutrient: Nutrient, niveauRef: Reflevel) {
                obtenirMap(niveauRef).remove(nutrient)
        }

        /**
         * Récupère la référence bibliographique associée à un nutriment
         *
         * @param nutrient Le nutriment concerné
         * @param niveauRef Le niveau de référence
         * @return La référence bibliographique ou une référence vide si non trouvée
         */
        fun obtenirBiblioNutriment(nutrient: Nutrient, niveauRef: Reflevel): BiblioRef {
                return if (contientNutriment(nutrient, niveauRef)) {
                        obtenirMap(niveauRef)[nutrient]?.citation ?: BiblioRef()
                } else {
                        BiblioRef()
                }
        }

        /**
         * Récupère l'unité d'un nutriment pour un niveau de référence donné
         *
         * @param nutrient Le nutriment concerné
         * @param niveauRef Le niveau de référence
         * @return L'ID de l'unité ou 0 si non trouvée
         */
        fun obtenirUniteNutriment(nutrient: Nutrient, niveauRef: Reflevel): Int {
                return if (contientNutriment(nutrient, niveauRef)) {
                        obtenirMap(niveauRef)[nutrient]?.uniteReq?.getID() ?: 0
                } else {
                        0
                }
        }

        /**
         * Récupère toutes les équations définies
         *
         * @return La liste des équations
         */
        fun obtenirToutesEquations(): ArrayList<Equation> {
                val listeEquations = ArrayList<Equation>()

                equationBEE?.let { if (it.name.isNotBlank()) listeEquations.add(it) }
                equationBW?.let { if (it.name.isNotBlank()) listeEquations.add(it) }
                equationDEcom?.let { if (it.name.isNotBlank()) listeEquations.add(it) }
                equationDEraw?.let { if (it.name.isNotBlank()) listeEquations.add(it) }
                equationME?.let { if (it.name.isNotBlank()) listeEquations.add(it) }

                listeEquations.addAll(equationsNut)

                return listeEquations
        }

        /**
         * Récupère toutes les références bibliographiques utilisées
         *
         * @return La liste des références bibliographiques
         */
        fun obtenirToutesBiblios(): ArrayList<BiblioRef> {
                val resultat = ArrayList<BiblioRef>()

                // Collecte des références des différentes maps
                for (ref in refMapMin.values) {
                        ajouterBiblioAuTableau(resultat, ref.citation)
                }

                for (ref in refMapOMin.values) {
                        ajouterBiblioAuTableau(resultat, ref.citation)
                }

                for (ref in refMapMax.values) {
                        ajouterBiblioAuTableau(resultat, ref.citation)
                }

                for (ref in refMapOMax.values) {
                        ajouterBiblioAuTableau(resultat, ref.citation)
                }

                // Ajout des références des équations
                ajouterBiblioAuTableau(resultat, equationBEE?.bib)
                ajouterBiblioAuTableau(resultat, equationBW?.bib)
                ajouterBiblioAuTableau(resultat, equationDEraw?.bib)
                ajouterBiblioAuTableau(resultat, equationDEcom?.bib)
                ajouterBiblioAuTableau(resultat, equationME?.bib)

                return resultat
        }

        /**
         * Obtient la Map correspondant au niveau de référence
         *
         * @param niveauRef Le niveau de référence
         * @return La Map correspondante
         */
        private fun obtenirMap(niveauRef: Reflevel): MutableMap<Nutrient, Nut4Ref> {
                return when (niveauRef) {
                        Reflevel.OPTIMIN -> refMapOMin
                        Reflevel.OPTIMAX -> refMapOMax
                        Reflevel.MIN -> refMapMin
                        Reflevel.MAX -> refMapMax
                }
        }

        /**
         * Ajoute une référence bibliographique à un tableau si elle n'y est pas déjà
         *
         * @param resultat Le tableau de résultats
         * @param biblio La référence bibliographique à ajouter, peut être null
         * @return Le tableau mis à jour
         */
        private fun ajouterBiblioAuTableau(
                resultat: ArrayList<BiblioRef>,
                biblio: BiblioRef?
        ): ArrayList<BiblioRef> {
                if (biblio != null &&
                                biblio.toString() != BiblioRef().toString() &&
                                !resultat.contains(biblio)
                ) {
                        resultat.add(biblio)
                }
                return resultat
        }

        /**
         * Récupère tous les nutriments définis pour cette référence
         * @return Une liste de Nut4Ref contenant tous les nutriments définis
         */
        fun obtenirTousLesNutriments(): List<Nut4Ref> {
                val result = ArrayList<Nut4Ref>()
                result.addAll(refMapMin.values)
                result.addAll(refMapMax.values)
                result.addAll(refMapOMin.values)
                result.addAll(refMapOMax.values)
                return result
        }

        /**
         * Récupère tous les coefficients pour un groupe donné
         * @param groupId L'identifiant du groupe (0-4)
         * @return La liste des coefficients du groupe
         */
        fun getCoefficientsForGroup(groupId: Int): List<CoefP> {
                return when (groupId) {
                        0 -> modk1.toList()
                        1 -> modk2.toList()
                        2 -> modk3.toList()
                        3 -> modk4.toList()
                        4 -> modk5.toList()
                        else -> emptyList()
                }
        }

        /**
         * Met à jour les coefficients pour un groupe donné
         * @param groupId L'identifiant du groupe (0-4)
         * @param coefficients La nouvelle liste de coefficients
         * @return true si la mise à jour a réussi, false sinon
         */
        fun updateCoefficientsForGroup(groupId: Int, coefficients: List<CoefP>): Boolean {
                when (groupId) {
                        0 -> {
                                modk1.clear()
                                modk1.addAll(coefficients)
                        }
                        1 -> {
                                modk2.clear()
                                modk2.addAll(coefficients)
                        }
                        2 -> {
                                modk3.clear()
                                modk3.addAll(coefficients)
                        }
                        3 -> {
                                modk4.clear()
                                modk4.addAll(coefficients)
                        }
                        4 -> {
                                modk5.clear()
                                modk5.addAll(coefficients)
                        }
                        else -> return false
                }
                return true
        }

        override fun toString(): String {
                return nom
        }
}
