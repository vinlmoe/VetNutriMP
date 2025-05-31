package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Classe représentant une référence évaluée basée sur la classe ReferenceEv du projet Java original
 * Cette classe gère les références nutritionnelles pour les différents nutriments
 */
data class ReferenceEv(
        val uuid: String = genUUID(),
        var nom: String = "",
        var description: String = "",
        var maladie: Boolean = false,
        var nomMaladie: String = "",
        var nomEnergie: String = "",
        var consistent: Int = 0,
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

        init {
                // Initialisation des coefficients par défaut
                modk1.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 0))
                modk2.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 1))
                modk3.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 2))
                modk4.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 3))
                modk5.add(CoefP(description = "Normal", coef = 1.0f, groupUUID = 4))
        }

        // Constructeur secondaire pour compatibilité
        constructor(uuid: String? = null) : this(uuid = uuid ?: genUUID())

        /**
         * Définit la valeur d'un nutriment pour un niveau de référence donné
         *
         * @param valeur La valeur du nutriment
         * @param nutrient Le nutriment concerné
         * @param niveauRef Le niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
         * @param uniteReq L'unité de la valeur
         * @param biblio La référence bibliographique associée
         * @param unitEnum L'unité physique personnalisée (optionnel, utilise celle du nutriment par
         * défaut)
         */
        fun definirNutriment(
                valeur: Float,
                nutrient: Nutrient,
                niveauRef: Reflevel,
                uniteReq: UnitReqEnum,
                biblio: BiblioRef,
                unitEnum: UnitEnum = nutrient.ue
        ) {
                obtenirMap(niveauRef)[nutrient] =
                        Nut4Ref(
                                nutrient = nutrient,
                                niveauRelatif = niveauRef,
                                quantite = valeur,
                                unite = unitEnum,
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
         * Récupère l'UnitEnum d'un nutriment pour un niveau de référence donné
         *
         * @param nutrient Le nutriment concerné
         * @param niveauRef Le niveau de référence
         * @return L'UnitEnum ou l'UnitEnum par défaut du nutriment si non trouvée
         */
        fun obtenirUnitEnumNutriment(nutrient: Nutrient, niveauRef: Reflevel): UnitEnum {
                return if (contientNutriment(nutrient, niveauRef)) {
                        obtenirMap(niveauRef)[nutrient]?.unite ?: nutrient.ue
                } else {
                        nutrient.ue
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

        // Getters publics pour l'accès aux propriétés privées

        /** Récupère la liste des coefficients k1 */
        fun getModk1(): ArrayList<CoefP> = modk1

        /** Récupère la liste des coefficients k2 */
        fun getModk2(): ArrayList<CoefP> = modk2

        /** Récupère la liste des coefficients k3 */
        fun getModk3(): ArrayList<CoefP> = modk3

        /** Récupère la liste des coefficients k4 */
        fun getModk4(): ArrayList<CoefP> = modk4

        /** Récupère la liste des coefficients k5 */
        fun getModk5(): ArrayList<CoefP> = modk5

        /** Récupère la map des nutriments MIN */
        fun getRefMapMin(): MutableMap<Nutrient, Nut4Ref> = refMapMin

        /** Récupère la map des nutriments MAX */
        fun getRefMapMax(): MutableMap<Nutrient, Nut4Ref> = refMapMax

        /** Récupère la map des nutriments OPTIMIN */
        fun getRefMapOMin(): MutableMap<Nutrient, Nut4Ref> = refMapOMin

        /** Récupère la map des nutriments OPTIMAX */
        fun getRefMapOMax(): MutableMap<Nutrient, Nut4Ref> = refMapOMax

        /** Alias pour obtenirToutesEquations - pour compatibilité avec le code d'importation */
        fun getAllEquations(): ArrayList<Equation> = obtenirToutesEquations()

        /** Alias pour obtenirToutesBiblios - pour compatibilité avec le code d'importation */
        fun getAllBiblioRefs(): ArrayList<BiblioRef> = obtenirToutesBiblios()

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
