package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Classe représentant une référence évaluée basée sur la classe ReferenceEv du projet Java original
 * Cette classe gère les références nutritionnelles pour les différents nutriments
 */
data class ReferenceEv(
        val uuid: String = generateUUID(),
        var nom: String = "",
        var description: String = "",
        var maladie: Boolean = false,
        var nomMaladie: String = "",
        var nomEnergie: String = "",
        var consistent: Int = 1,
        var espece: Espece = Espece.CHIEN,
        var stadePhysio: StadePhysio = StadePhysio.ADULTE
) {
        private val refMapMin: MutableMap<Nutrient, Nut4Ref> = HashMap()
        private val refMapMax: MutableMap<Nutrient, Nut4Ref> = HashMap()
        private val refMapOMin: MutableMap<Nutrient, Nut4Ref> = HashMap()
        private val refMapOMax: MutableMap<Nutrient, Nut4Ref> = HashMap()

        // Équations
        var equationBW: Equation = Equation()
        var equationBEE: Equation = Equation()
        var equationDEcom: Equation = Equation()
        var equationDEraw: Equation = Equation()
        var equationsNut: ArrayList<Equation> = ArrayList()

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
                                niveauRelatif = niveauRef,
                                quantite = valeur,
                                unite = nutrient.ue,
                                uniteReq = uniteReq,
                                biblio = biblio
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
                        obtenirMap(niveauRef)[nutrient]?.biblio ?: BiblioRef()
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

                if (equationBEE.name.isNotBlank()) {
                        listeEquations.add(equationBEE)
                }

                if (equationBW.name.isNotBlank()) {
                        listeEquations.add(equationBW)
                }

                if (equationDEcom.name.isNotBlank()) {
                        listeEquations.add(equationDEcom)
                }

                if (equationDEraw.name.isNotBlank()) {
                        listeEquations.add(equationDEraw)
                }

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
                        ajouterBiblioAuTableau(resultat, ref.biblio)
                }

                for (ref in refMapOMin.values) {
                        ajouterBiblioAuTableau(resultat, ref.biblio)
                }

                for (ref in refMapMax.values) {
                        ajouterBiblioAuTableau(resultat, ref.biblio)
                }

                for (ref in refMapOMax.values) {
                        ajouterBiblioAuTableau(resultat, ref.biblio)
                }

                // Ajout des références des équations
                ajouterBiblioAuTableau(resultat, equationBEE.bib)
                ajouterBiblioAuTableau(resultat, equationBW.bib)
                ajouterBiblioAuTableau(resultat, equationDEraw.bib)
                ajouterBiblioAuTableau(resultat, equationDEcom.bib)

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
         * @param biblio La référence bibliographique à ajouter
         * @return Le tableau mis à jour
         */
        private fun ajouterBiblioAuTableau(
                resultat: ArrayList<BiblioRef>,
                biblio: BiblioRef
        ): ArrayList<BiblioRef> {
                if (biblio.toString() != BiblioRef().toString() && !resultat.contains(biblio)) {
                        resultat.add(biblio)
                }
                return resultat
        }

        override fun toString(): String {
                return nom
        }

        /** Classe interne représentant une référence nutritionnelle */
        inner class Nut4Ref(
                val nutrient: Nutrient,
                val niveauRelatif: Reflevel,
                val quantite: Float,
                val unite: UnitEnum,
                val uniteReq: UnitReqEnum,
                val biblio: BiblioRef
        )
}
