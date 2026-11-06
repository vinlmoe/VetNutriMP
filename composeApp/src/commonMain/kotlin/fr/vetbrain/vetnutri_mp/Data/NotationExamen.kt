package fr.vetbrain.vetnutri_mp.Data

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Outils purs de la notation d'examen (analyses croisées) : points du barème, statistiques des
 * notes, taux de réussite par critère et identifiants de copies.
 */

/** Type de critère du barème, pour regrouper les taux de réussite. */
enum class TypeCritereNotation { INGREDIENTS, QUANTITE, DENSITE, NUTRIMENT, REFERENCE, CONSEILS, PERSONNALISE }

/** Un critère évaluable du barème : actif, configuré (une condition au moins) et ses points. */
data class CritereBareme(val cle: String, val type: TypeCritereNotation, val libelle: String, val points: Double)

private fun MinMaxPointsRule.configure() = enabled && (min != null || max != null)

/**
 * Critères qui peuvent rapporter des points : un critère désactivé ou sans condition (ni bornes, ni
 * mots) ne rapporte jamais rien, comme dans le calcul des notes.
 */
fun criteresBareme(rule: ExamGradingRuleSet): List<CritereBareme> = buildList {
    val ing = rule.ingredientRule
    if (ing.enabled && (ing.required.any { it.isNotBlank() } || ing.forbidden.any { it.isNotBlank() }))
        add(CritereBareme("ingredients", TypeCritereNotation.INGREDIENTS, "", ing.points.toDouble()))
    if (rule.rationQuantityRule.configure())
        add(CritereBareme("quantite", TypeCritereNotation.QUANTITE, "", rule.rationQuantityRule.points.toDouble()))
    if (rule.energyDensityRule.configure())
        add(CritereBareme("densite", TypeCritereNotation.DENSITE, "", rule.energyDensityRule.points.toDouble()))
    rule.nutrientRules.forEachIndexed { i, n ->
        if (n.enabled && n.nutrientLabel.isNotBlank() && (n.min != null || n.max != null))
            add(CritereBareme("nutriment:$i", TypeCritereNotation.NUTRIMENT, n.nutrientLabel, n.points.toDouble()))
    }
    val ref = rule.referenceRule
    if (ref.enabled && ref.allowedReferenceIds.any { it.isNotBlank() })
        add(CritereBareme("reference", TypeCritereNotation.REFERENCE, "", ref.points.toDouble()))
    val conseils = rule.adviceRule
    if (conseils.enabled && (conseils.requiredWords.any { it.isNotBlank() } || conseils.forbiddenWords.any { it.isNotBlank() }))
        add(CritereBareme("conseils", TypeCritereNotation.CONSEILS, "", conseils.points.toDouble()))
    rule.customCriteria.forEachIndexed { i, c ->
        val numerique = c.metric !in setOf(FlexibleMetricKind.INGREDIENT_KEYWORDS, FlexibleMetricKind.ADVICE_KEYWORDS, FlexibleMetricKind.REFERENCE_ID)
        val configure = if (numerique) c.min != null || c.max != null else c.includes.any { it.isNotBlank() } || c.excludes.any { it.isNotBlank() }
        if (c.enabled && configure)
            add(CritereBareme("perso:${c.id.ifBlank { i.toString() }}", TypeCritereNotation.PERSONNALISE, c.label, c.points))
    }
}

/** Total des points que le barème peut attribuer. */
fun pointsBareme(rule: ExamGradingRuleSet): Double = criteresBareme(rule).sumOf { it.points }

/**
 * Note automatique à partir des points obtenus : ramenée à la note maximale si le barème est
 * proportionnel, sinon plafonnée à cette note. Arrondie au centième.
 */
fun noteAutomatique(pointsObtenus: Double, rule: ExamGradingRuleSet): Double {
    val max = rule.autoScoreMax.coerceAtLeast(0.0)
    val total = pointsBareme(rule)
    val brute = if (rule.notationProportionnelle && total > 0.0) pointsObtenus * max / total else pointsObtenus
    return kotlin.math.round(brute.coerceIn(0.0, max) * 100.0) / 100.0
}

/** Réussite d'un critère sur l'ensemble des copies notées. */
data class ReussiteCritere(val critere: CritereBareme, val valides: Int, val copies: Int) {
    val taux: Double get() = if (copies == 0) 0.0 else valides.toDouble() / copies
}

/** Une classe de l'histogramme des notes : [de, a[ (la dernière inclut la note maximale). */
data class ClasseNotes(val de: Double, val a: Double, val copies: Int)

data class StatistiquesNotes(
        val copies: Int,
        val moyenne: Double,
        val ecartType: Double,
        val mediane: Double,
        val min: Double,
        val max: Double,
        /** Copies dont la note finale atteint la moitié de la note maximale. */
        val auDessusMoyenne: Int,
        val histogramme: List<ClasseNotes>,
        val reussites: List<ReussiteCritere>
)

/** Le critère [critere] est-il validé par la copie ? */
fun critereValide(critere: CritereBareme, detail: ExamGradeDetail, rule: ExamGradingRuleSet): Boolean =
        when (critere.type) {
            TypeCritereNotation.INGREDIENTS -> detail.ingredientOk
            TypeCritereNotation.QUANTITE -> detail.rationOk
            TypeCritereNotation.DENSITE -> detail.energyOk
            TypeCritereNotation.REFERENCE -> detail.referenceOk
            TypeCritereNotation.CONSEILS -> detail.adviceOk
            TypeCritereNotation.NUTRIMENT -> {
                val index = critere.cle.substringAfter(':').toIntOrNull()
                val label = index?.let { rule.nutrientRules.getOrNull(it)?.nutrientLabel } ?: critere.libelle
                detail.nutrientResults.firstOrNull { it.nutrientLabel == label }?.ok == true
            }
            TypeCritereNotation.PERSONNALISE -> {
                val id = critere.cle.substringAfter(':')
                detail.customCriteriaResults.firstOrNull { it.id == id || it.label == critere.libelle }?.ok == true
            }
        }

/**
 * Statistiques des notes finales et taux de réussite de chaque critère du barème : un critère
 * que presque toutes les copies valident (ou aucune) mérite d'être revu.
 */
fun statistiquesNotes(grades: List<ExamGrade>, rule: ExamGradingRuleSet?, noteMax: Double = rule?.autoScoreMax ?: 20.0): StatistiquesNotes? {
    if (grades.isEmpty()) return null
    val notes = grades.map { it.finalScore }.sorted()
    val moyenne = notes.average()
    val ecartType = sqrt(notes.map { (it - moyenne).pow(2) }.average())
    val mediane = if (notes.size % 2 == 0) (notes[notes.size / 2 - 1] + notes[notes.size / 2]) / 2.0 else notes[notes.size / 2]
    val haut = noteMax.coerceAtLeast(notes.last()).coerceAtLeast(1.0)
    // Classes de 2 points sur 20 (10 classes), proportionnelles pour un autre maximum
    val nbClasses = 10
    val largeur = haut / nbClasses
    val histogramme = (0 until nbClasses).map { i ->
        val de = i * largeur
        val a = de + largeur
        ClasseNotes(de, a, notes.count { n -> n >= de && (n < a || (i == nbClasses - 1 && n <= a)) })
    }
    val reussites = rule?.let { r ->
        criteresBareme(r).map { c -> ReussiteCritere(c, grades.count { critereValide(c, it.detail, r) }, grades.size) }
    }.orEmpty()
    return StatistiquesNotes(
            copies = notes.size, moyenne = moyenne, ecartType = ecartType, mediane = mediane,
            min = notes.first(), max = notes.last(),
            auDessusMoyenne = notes.count { it >= noteMax / 2.0 },
            histogramme = histogramme, reussites = reussites
    )
}

/**
 * Identifiant de copie unique par animal. Le numéro d'étudiant sert de clé d'enregistrement des
 * notes : vide, il est remplacé par l'identifiant de l'animal ; partagé par plusieurs animaux, il
 * est complété par cet identifiant (sinon une copie écraserait l'autre).
 */
fun identifiantsCopies(copies: List<Pair<String, String?>>): Map<String, String> {
    val parEtudiant = copies.filter { it.second?.isNotBlank() == true }.groupBy { it.second!!.trim() }
    return copies.associate { (animalId, etudiant) ->
        val numero = etudiant?.trim().orEmpty()
        animalId to when {
            numero.isEmpty() -> "animal:$animalId"
            (parEtudiant[numero]?.size ?: 0) > 1 -> "$numero ($animalId)"
            else -> numero
        }
    }
}

/** Un couple examen / exercice présent dans les consultations, avec son nombre de copies. */
data class ExamenDisponible(val examId: String, val exerciseId: String, val copies: Int)

/** Examens et exercices trouvés dans les consultations (copies = animaux distincts). */
fun examensDisponibles(items: List<Triple<String?, String?, String>>): List<ExamenDisponible> =
        items.filter { !it.first.isNullOrBlank() && !it.second.isNullOrBlank() }
                .groupBy { it.first!!.trim() to it.second!!.trim() }
                .map { (cle, liste) -> ExamenDisponible(cle.first, cle.second, liste.map { it.third }.distinct().size) }
                .sortedWith(compareBy({ it.examId }, { it.exerciseId }))

/** Identifiant de format du fichier de notation (refus de tout autre JSON à la reprise). */
const val FORMAT_DOSSIER_NOTATION = "vetnutri-notation-examen"

/**
 * Sauvegarde complète d'une notation, pour la reprendre plus tard ou sur un autre poste : barème
 * (et ses versions enregistrées), notes (manuelles comprises), dernier import de copies et
 * copies elles-mêmes (export API des animaux, consultations, aliments et référentiels liés).
 */
@kotlinx.serialization.Serializable
data class DossierNotation(
        val format: String = FORMAT_DOSSIER_NOTATION,
        val version: Int = 1,
        val enregistreLeEpochMs: Long = 0,
        val examId: String,
        val exerciseId: String,
        val bareme: ExamGradingRuleSet? = null,
        val baremesPrecedents: List<RuleSnapshotEntry> = emptyList(),
        val notes: List<ExamGrade> = emptyList(),
        val sessionImport: ExamCopyImportSession? = null,
        /** Export API des copies (ApiEnvelope en JSON), null si les copies ne sont pas incluses. */
        val copiesJson: String? = null
)

private val jsonDossier = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = true
}

fun encoderDossierNotation(dossier: DossierNotation): String =
        jsonDossier.encodeToString(DossierNotation.serializer(), dossier)

/** Lit un fichier de notation ; null si ce n'est pas un fichier de notation VetNutri. */
fun decoderDossierNotation(texte: String): DossierNotation? =
        try {
            jsonDossier.decodeFromString(DossierNotation.serializer(), texte).takeIf {
                it.format == FORMAT_DOSSIER_NOTATION && it.examId.isNotBlank() && it.exerciseId.isNotBlank()
            }
        } catch (e: kotlinx.serialization.SerializationException) {
            fr.vetbrain.vetnutri_mp.Utils.AppLog.d("NotationExamen", "Fichier de notation illisible : ${e.message}")
            null
        } catch (e: IllegalArgumentException) {
            fr.vetbrain.vetnutri_mp.Utils.AppLog.d("NotationExamen", "Fichier de notation invalide : ${e.message}")
            null
        }

/** Un fichier choisi par l'utilisateur : nom (sans chemin) et contenu texte. */
data class FichierJson(val nom: String, val contenu: String)

/**
 * Copie d'un lot de fichiers JSON prête à importer : JSON à passer à l'import, animaux trouvés,
 * animaux rattachés à l'examen, animaux appartenant à un autre examen, ou erreur de lecture.
 */
data class CopiePreparee(
        val fichier: String,
        val json: String? = null,
        val animaux: Int = 0,
        val rattaches: Int = 0,
        val autreExamen: Int = 0,
        val erreur: String? = null
)

private val jsonCopies = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/** Contenu d'un enregistrement jsonbin ({"record": …}) ou le JSON tel quel. */
fun extraireEnregistrementJsonBin(texte: String): String {
    val brut = texte.trim().removePrefix("﻿")
    val racine = try {
        jsonCopies.parseToJsonElement(brut) as? kotlinx.serialization.json.JsonObject
    } catch (e: kotlinx.serialization.SerializationException) {
        fr.vetbrain.vetnutri_mp.Utils.AppLog.d("NotationExamen", "JSON illisible : ${e.message}")
        null
    } ?: return brut
    return when (val record = racine["record"]) {
        null -> brut
        is kotlinx.serialization.json.JsonPrimitive -> record.content
        else -> jsonCopies.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), record)
    }
}

/**
 * Prépare une copie JSON (export VetNutri, éventuellement enveloppé par jsonbin) pour l'examen
 * [examId] / [exerciseId]. Avec [rattacher], un animal sans identifiant d'examen ou d'exercice
 * reçoit ceux de l'examen et, sans numéro d'étudiant, le nom du fichier (sans extension). Un
 * animal déjà rattaché à un autre examen n'est pas modifié : il est compté dans autreExamen.
 */
fun preparerCopieJson(fichier: FichierJson, examId: String, exerciseId: String, rattacher: Boolean): CopiePreparee {
    val envelope = try {
        jsonCopies.decodeFromString(ApiEnvelope.serializer(), extraireEnregistrementJsonBin(fichier.contenu))
    } catch (e: kotlinx.serialization.SerializationException) {
        return CopiePreparee(fichier.nom, erreur = e.message ?: "JSON invalide")
    } catch (e: IllegalArgumentException) {
        return CopiePreparee(fichier.nom, erreur = e.message ?: "JSON invalide")
    }
    if (envelope.animals.isEmpty()) return CopiePreparee(fichier.nom, erreur = "aucun animal")
    val numeroParDefaut = fichier.nom.substringAfterLast('/').substringAfterLast('\\').substringBeforeLast('.').trim()
    var rattaches = 0
    var autreExamen = 0
    val animaux = envelope.animals.map { a ->
        val examen = a.examStudentNumber?.trim().orEmpty()
        val exercice = a.examExerciseId?.trim().orEmpty()
        val memeExamen = examen.equals(examId.trim(), true) && exercice.equals(exerciseId.trim(), true)
        when {
            memeExamen -> a
            rattacher && (examen.isEmpty() || exercice.isEmpty()) -> {
                rattaches++
                a.copy(
                        exam = true,
                        examStudentNumber = examen.ifEmpty { examId.trim() },
                        examExerciseId = exercice.ifEmpty { exerciseId.trim() },
                        examStudentId = a.examStudentId?.takeIf { it.isNotBlank() } ?: numeroParDefaut.ifEmpty { null }
                )
            }
            else -> { autreExamen++; a }
        }
    }
    return CopiePreparee(
            fichier = fichier.nom,
            json = jsonCopies.encodeToString(ApiEnvelope.serializer(), envelope.copy(animals = animaux)),
            animaux = animaux.size,
            rattaches = rattaches,
            autreExamen = autreExamen
    )
}
