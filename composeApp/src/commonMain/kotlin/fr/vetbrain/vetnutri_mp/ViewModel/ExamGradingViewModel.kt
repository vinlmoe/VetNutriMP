package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import fr.vetbrain.vetnutri_mp.Repository.ExamGradingRepository
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Service.FileService
import fr.vetbrain.vetnutri_mp.Service.createJsonShareService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ExamGradingViewModel(
    private val repository: ExamGradingRepository,
    private val exportImportRepository: ExportImportRepository,
    private val fileService: FileService
) : ViewModel() {
    enum class ExamAnalysisMetricId {
        WEIGHT_KG,
        BEC_KCAL,
        ENERGY_INTAKE_KCAL,
        ENERGY_DENSITY_KCAL_100,
        PROTEIN_G
    }

    data class HistogramBin(
        val from: Double,
        val to: Double,
        val count: Int
    )

    data class MetricDistribution(
        val metricId: ExamAnalysisMetricId,
        val label: String,
        val values: List<Double>,
        val count: Int,
        val min: Double,
        val p10: Double,
        val median: Double,
        val p90: Double,
        val max: Double,
        val suggestedLow: Double,
        val suggestedHigh: Double,
        val histogram: List<HistogramBin>
    )

    private val _rule = MutableStateFlow<ExamGradingRuleSet?>(null)
    val rule: StateFlow<ExamGradingRuleSet?> = _rule.asStateFlow()

    private val _grades = MutableStateFlow<List<ExamGrade>>(emptyList())
    val grades: StateFlow<List<ExamGrade>> = _grades.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isImportingBins = MutableStateFlow(false)
    val isImportingBins: StateFlow<Boolean> = _isImportingBins.asStateFlow()

    private val _importProgress = MutableStateFlow(0.0)
    val importProgress: StateFlow<Double> = _importProgress.asStateFlow()

    private val _importLogs = MutableStateFlow<List<String>>(emptyList())
    val importLogs: StateFlow<List<String>> = _importLogs.asStateFlow()

    private val _lastImportSession = MutableStateFlow<ExamCopyImportSession?>(null)
    val lastImportSession: StateFlow<ExamCopyImportSession?> = _lastImportSession.asStateFlow()
    private val _importSessionHistory = MutableStateFlow<List<ExamCopyImportSession>>(emptyList())
    val importSessionHistory: StateFlow<List<ExamCopyImportSession>> = _importSessionHistory.asStateFlow()

    private val _ruleSnapshotHistory = MutableStateFlow<List<RuleSnapshotEntry>>(emptyList())
    val ruleSnapshotHistory: StateFlow<List<RuleSnapshotEntry>> = _ruleSnapshotHistory.asStateFlow()
    private val _metricDistributions = MutableStateFlow<List<MetricDistribution>>(emptyList())
    val metricDistributions: StateFlow<List<MetricDistribution>> = _metricDistributions.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun clearMessage() { _message.value = null }

    fun clearImportLogs() {
        _importLogs.value = emptyList()
    }

    fun loadRule(examId: String, exerciseId: String) {
        viewModelScope.launch {
            val existing = repository.getRule(examId, exerciseId)
            _rule.value = existing ?: defaultRule(examId, exerciseId)
            loadSavedImportSession(examId, exerciseId)
            loadImportSessionHistory(examId, exerciseId)
            loadRuleSnapshotHistory(examId, exerciseId)
        }
    }

    fun updateRule(rule: ExamGradingRuleSet) {
        _rule.value = rule
    }

    fun saveRule() {
        val current = _rule.value ?: return
        viewModelScope.launch {
            repository.saveRule(current)
            saveRuleSnapshot(current)
            loadRuleSnapshotHistory(current.examId, current.exerciseId)
            _message.value = "✅ Règles sauvegardées"
        }
    }

    fun selectImportSession(sessionId: String) {
        val picked = _importSessionHistory.value.firstOrNull { it.sessionId == sessionId } ?: return
        _lastImportSession.value = picked
        _message.value = "📂 Session d'import chargée (${picked.successCount}/${picked.total})"
    }

    fun selectRuleSnapshot(snapshotId: String) {
        val picked = _ruleSnapshotHistory.value.firstOrNull { it.snapshotId == snapshotId } ?: return
        _rule.value = picked.rule
        _message.value = "📐 Grille snapshot chargée"
    }

    fun computeGrades(items: List<CrossConsultationAnalysisViewModel.ConsultationItem>, examId: String, exerciseId: String) {
        val ruleSet = _rule.value ?: defaultRule(examId, exerciseId)
        _rule.value = ruleSet
        val perAnimal = items.groupBy { it.animalId }
        val grades = perAnimal.values.mapNotNull { consultations ->
            val latest = consultations.maxByOrNull { it.rawDate ?: kotlinx.datetime.LocalDate(1900,1,1) } ?: return@mapNotNull null
            val detail = evaluate(ruleSet, latest)
            val maxScore = ruleSet.autoScoreMax.coerceAtLeast(0.0)
            val autoScore = detailTotal(detail).coerceIn(0.0, maxScore)
            ExamGrade(
                examId = examId,
                exerciseId = exerciseId,
                studentId = latest.studentIdentifier.orEmpty(),
                animalId = latest.animalId,
                animalName = latest.animalName,
                consultationId = latest.consultationId,
                autoScore = autoScore,
                manualScore = null,
                finalScore = autoScore,
                detail = detail
            )
        }
        _grades.value = grades
    }

    fun runRmdLikeAnalysis(items: List<CrossConsultationAnalysisViewModel.ConsultationItem>) {
        val perAnimalLatest = items
            .groupBy { it.animalId }
            .values
            .mapNotNull { consultations ->
                consultations.maxByOrNull { it.rawDate ?: kotlinx.datetime.LocalDate(1900, 1, 1) }
            }
        if (perAnimalLatest.isEmpty()) {
            _metricDistributions.value = emptyList()
            _message.value = "❌ Aucune consultation à analyser"
            return
        }

        val distributions = buildList {
            addDistribution(perAnimalLatest, ExamAnalysisMetricId.WEIGHT_KG, "Poids utilisé (kg)") { item ->
                firstProposedOrAny(item)?.animalWeightKg
            }?.let(::add)
            addDistribution(perAnimalLatest, ExamAnalysisMetricId.BEC_KCAL, "BEC (kcal/j)") { item ->
                firstProposedOrAny(item)?.beeKcal
            }?.let(::add)
            addDistribution(perAnimalLatest, ExamAnalysisMetricId.ENERGY_INTAKE_KCAL, "Apport énergie ration (kcal)") { item ->
                firstProposedOrAny(item)?.energyTotalKcal
            }?.let(::add)
            addDistribution(perAnimalLatest, ExamAnalysisMetricId.ENERGY_DENSITY_KCAL_100, "Densité énergie (kcal/100g)") { item ->
                firstProposedOrAny(item)?.energyDensity
            }?.let(::add)
            addDistribution(perAnimalLatest, ExamAnalysisMetricId.PROTEIN_G, "Protéines ration (g)") { item ->
                firstProposedOrAny(item)?.proteins
            }?.let(::add)
        }

        _metricDistributions.value = distributions
        _message.value = "📊 Analyse terminée (${perAnimalLatest.size} copies)"
    }

    fun applyAnalysisMetricAsCriterion(
        metricId: ExamAnalysisMetricId,
        min: Double?,
        max: Double?,
        points: Double = 0.6
    ) {
        val current = _rule.value ?: return
        val mapped = mapAnalysisMetric(metricId) ?: return
        val criterionId = "analysis_${metricId.name}"
        val nextCriterion = FlexibleCriterionRule(
            id = criterionId,
            label = mapped.label,
            enabled = true,
            metric = mapped.metricKind,
            rationScope = mapped.scope,
            min = min,
            max = max,
            points = points
        )
        val updatedCriteria = current.customCriteria
            .filterNot { it.id == criterionId } + nextCriterion
        _rule.value = current.copy(customCriteria = updatedCriteria)
        _message.value = "✅ Critère appliqué: ${mapped.label}"
    }

    fun applyNutrientCriterion(
        nutrientLabel: String,
        min: Double?,
        max: Double?,
        points: Double = 0.6,
        scope: RationScope = RationScope.FIRST_PROPOSED
    ) {
        val current = _rule.value ?: return
        val label = nutrientLabel.trim()
        if (label.isBlank()) return
        val criterionId = "analysis_nutrient_${label.lowercase().replace(Regex("[^a-z0-9]+"), "_")}"
        val criterion = FlexibleCriterionRule(
            id = criterionId,
            label = "Nutriment $label",
            enabled = true,
            metric = FlexibleMetricKind.NUTRIENT_TOTAL,
            rationScope = scope,
            nutrientLabel = label,
            min = min,
            max = max,
            points = points
        )
        val updatedCriteria = current.customCriteria.filterNot { it.id == criterionId } + criterion
        _rule.value = current.copy(customCriteria = updatedCriteria)
        _message.value = "✅ Critère nutriment appliqué: $label"
    }

    fun updateManualScore(studentId: String, manualScore: Double?) {
        _grades.value = _grades.value.map { grade ->
            if (grade.studentId == studentId) {
                val finalScore = manualScore ?: grade.autoScore
                grade.copy(manualScore = manualScore, finalScore = finalScore)
            } else grade
        }
    }

    fun saveGrades() {
        viewModelScope.launch {
            repository.saveGrades(_grades.value)
            _message.value = "✅ Notes sauvegardées"
        }
    }

    fun loadGrades(examId: String, exerciseId: String) {
        viewModelScope.launch {
            _grades.value = repository.getGrades(examId, exerciseId)
        }
    }

    fun importFromMoodleCsv(
        csvContent: String,
        examId: String,
        exerciseId: String,
        sourceFileName: String = "moodle.csv",
        forceRefresh: Boolean = false,
        offlineOnly: Boolean = false,
        onCompleted: (() -> Unit)? = null
    ) {
        if (examId.isBlank() || exerciseId.isBlank()) {
            _message.value = "❌ Renseigner ID examen et ID exercice"
            return
        }
        viewModelScope.launch {
            _isImportingBins.value = true
            _importProgress.value = 0.0
            _importLogs.value = emptyList()
            appendImportLog("📄 Lecture du CSV Moodle…")
            try {
                val rows = parseMoodleCsv(csvContent)
                if (rows.isEmpty()) {
                    _message.value = "❌ Aucun bin_id trouvé dans le CSV"
                    _isImportingBins.value = false
                    return@launch
                }
                appendImportLog("✅ ${rows.size} copie(s) avec bin_id")

                val shareService = createJsonShareService()
                val entries = mutableListOf<ExamCopyImportEntry>()
                val total = rows.size.coerceAtLeast(1)

                val examDir = getExamCacheDirectory(examId, exerciseId)
                fileService.createDirectoryIfNotExists(examDir)
                val binsDir = PlatformFile.create("${examDir.absolutePath}/bins")
                fileService.createDirectoryIfNotExists(binsDir)

                rows.forEachIndexed { index, row ->
                    _importProgress.value = (index.toDouble() / total.toDouble()).coerceIn(0.0, 1.0)
                    val now = Clock.System.now().toEpochMilliseconds()
                    try {
                        val binId = shareService.extractBinIdFromUrl(row.binId) ?: row.binId.trim()
                        val cacheFile = PlatformFile.create("${binsDir.absolutePath}/${sanitizeFileName(binId)}.json")

                        val cachedText = if (!forceRefresh && fileService.fileExists(cacheFile)) {
                            fileService.readText(cacheFile).getOrNull()?.takeIf { it.isNotBlank() }
                        } else {
                            null
                        }

                        val recordJson: String
                        val fromCache: Boolean
                        if (cachedText != null) {
                            recordJson = normalizeJsonBinPayload(cachedText)
                            fromCache = true
                            appendImportLog("📦 Cache: $binId")
                        } else {
                            if (offlineOnly) {
                                throw IllegalStateException("Absent du cache (mode hors-ligne)")
                            }
                            appendImportLog("🌐 Téléchargement: $binId")
                            val downloaded = shareService.downloadJson(binId).getOrElse { throw it }
                            recordJson = normalizeJsonBinPayload(downloaded)
                            fromCache = false
                            fileService.writeText(cacheFile, recordJson)
                        }

                        exportImportRepository.importAll(recordJson)
                        entries += ExamCopyImportEntry(
                            nom = row.nom,
                            prenom = row.prenom,
                            email = row.email,
                            binId = binId,
                            success = true,
                            fromCache = fromCache,
                            importedAtEpochMs = now
                        )
                    } catch (e: Exception) {
                        entries += ExamCopyImportEntry(
                            nom = row.nom,
                            prenom = row.prenom,
                            email = row.email,
                            binId = row.binId,
                            success = false,
                            fromCache = false,
                            error = e.message ?: "Erreur inconnue",
                            importedAtEpochMs = now
                        )
                        appendImportLog("❌ ${row.binId}: ${e.message}")
                    }
                    _importProgress.value = ((index + 1).toDouble() / total.toDouble()).coerceIn(0.0, 1.0)
                }

                val session = ExamCopyImportSession(
                    sessionId = Clock.System.now().toEpochMilliseconds().toString(),
                    examId = examId,
                    exerciseId = exerciseId,
                    sourceFileName = sourceFileName,
                    importedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                    forceRefresh = forceRefresh,
                    offlineOnly = offlineOnly,
                    entries = entries
                )
                saveImportSession(session)
                _lastImportSession.value = session
                loadImportSessionHistory(examId, exerciseId)

                val ok = session.successCount
                val ko = session.errorCount
                _message.value = "✅ Import terminé: $ok OK, $ko erreur(s)"
                appendImportLog("✅ Import terminé: $ok OK, $ko erreur(s)")
                onCompleted?.invoke()
            } catch (e: Exception) {
                _message.value = "❌ Import CSV impossible: ${e.message}"
                appendImportLog("❌ Erreur globale: ${e.message}")
            } finally {
                _isImportingBins.value = false
            }
        }
    }

    private fun defaultRule(examId: String, exerciseId: String): ExamGradingRuleSet {
        return ExamGradingRuleSet(
            examId = examId,
            exerciseId = exerciseId,
            label = exerciseId,
            autoScoreMax = 20.0,
            ingredientRule = IngredientRule(points = 5),
            rationQuantityRule = MinMaxPointsRule(points = 5),
            energyDensityRule = MinMaxPointsRule(points = 5),
            nutrientRules = emptyList(),
            referenceRule = ReferenceRule(points = 0),
            adviceRule = AdviceRule(points = 3),
            customCriteria = emptyList()
        )
    }

    private suspend fun getExamCacheDirectory(examId: String, exerciseId: String): PlatformFile {
        val dataDir = fileService.getDataDirectory()
        val root = PlatformFile.create("${dataDir.absolutePath}/exam_grading_cache")
        fileService.createDirectoryIfNotExists(root)
        val examDir = PlatformFile.create("${root.absolutePath}/${sanitizeFileName(examId)}_${sanitizeFileName(exerciseId)}")
        fileService.createDirectoryIfNotExists(examDir)
        return examDir
    }

    private suspend fun saveImportSession(session: ExamCopyImportSession) {
        val examDir = getExamCacheDirectory(session.examId, session.exerciseId)
        val sessionId = if (session.sessionId.isBlank()) session.importedAtEpochMs.toString() else session.sessionId
        val withId = session.copy(sessionId = sessionId)
        val latestFile = PlatformFile.create("${examDir.absolutePath}/import_session.json")
        val snapshotFile = PlatformFile.create("${examDir.absolutePath}/import_session_${sessionId}.json")
        val payload = json.encodeToString(ExamCopyImportSession.serializer(), withId)
        fileService.writeText(latestFile, payload)
        fileService.writeText(snapshotFile, payload)
    }

    private suspend fun loadSavedImportSession(examId: String, exerciseId: String) {
        val examDir = getExamCacheDirectory(examId, exerciseId)
        val sessionFile = PlatformFile.create("${examDir.absolutePath}/import_session.json")
        if (!fileService.fileExists(sessionFile)) {
            _lastImportSession.value = null
            return
        }
        val content = fileService.readText(sessionFile).getOrNull()
        if (content.isNullOrBlank()) {
            _lastImportSession.value = null
            return
        }
        _lastImportSession.value = runCatching {
            json.decodeFromString(ExamCopyImportSession.serializer(), content)
        }.getOrNull()
    }

    private suspend fun loadImportSessionHistory(examId: String, exerciseId: String) {
        val examDir = getExamCacheDirectory(examId, exerciseId)
        val files = fileService.listFiles(examDir).filter {
            it.name.startsWith("import_session_") && it.name.endsWith(".json")
        }
        val sessions = files.mapNotNull { file ->
            val content = fileService.readText(file).getOrNull() ?: return@mapNotNull null
            runCatching {
                json.decodeFromString(ExamCopyImportSession.serializer(), content)
                    .let { session ->
                        if (session.sessionId.isBlank()) {
                            session.copy(sessionId = file.name.removePrefix("import_session_").removeSuffix(".json"))
                        } else session
                    }
            }.getOrNull()
        }.sortedByDescending { it.importedAtEpochMs }
        _importSessionHistory.value = sessions.take(20)
    }

    private suspend fun saveRuleSnapshot(rule: ExamGradingRuleSet) {
        val examDir = getExamCacheDirectory(rule.examId, rule.exerciseId)
        val snapshotId = Clock.System.now().toEpochMilliseconds().toString()
        val entry = RuleSnapshotEntry(
            snapshotId = snapshotId,
            savedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            rule = rule
        )
        val file = PlatformFile.create("${examDir.absolutePath}/rule_snapshot_${snapshotId}.json")
        fileService.writeText(file, json.encodeToString(RuleSnapshotEntry.serializer(), entry))
    }

    private suspend fun loadRuleSnapshotHistory(examId: String, exerciseId: String) {
        val examDir = getExamCacheDirectory(examId, exerciseId)
        val files = fileService.listFiles(examDir).filter {
            it.name.startsWith("rule_snapshot_") && it.name.endsWith(".json")
        }
        val snapshots = files.mapNotNull { file ->
            val content = fileService.readText(file).getOrNull() ?: return@mapNotNull null
            runCatching {
                json.decodeFromString(RuleSnapshotEntry.serializer(), content)
            }.getOrNull()
        }.sortedByDescending { it.savedAtEpochMs }
        _ruleSnapshotHistory.value = snapshots.take(20)
    }

    private fun parseMoodleCsv(csvContent: String): List<ExamCsvRow> {
        val cleaned = csvContent.removePrefix("\uFEFF")
        val rows = splitCsvRows(cleaned)
        if (rows.isEmpty()) return emptyList()

        val delimiter = detectDelimiter(rows.first())
        val headers = parseCsvLine(rows.first(), delimiter).map { normalizeHeader(it) }
        val binIndex = headers.indexOfFirst { header ->
            header == "bin_id" ||
                header.startsWith("reponse") ||
                header.startsWith("réponse") ||
                (header.contains("reponse") && header.contains("1")) ||
                (header.contains("réponse") && header.contains("1"))
        }
        if (binIndex < 0) return emptyList()

        val nomIndex = headers.indexOfFirst { it.contains("nom de famille") || it == "nom" }
        val prenomIndex = headers.indexOfFirst { it.contains("prénom") || it.contains("prenom") }
        val emailIndex = headers.indexOfFirst { it.contains("courriel") || it.contains("email") }

        return rows.drop(1).mapNotNull { rowText ->
            val values = parseCsvLine(rowText, delimiter)
            val bin = values.getOrNull(binIndex)?.trim().orEmpty().trim('"')
            if (bin.isBlank()) return@mapNotNull null
            ExamCsvRow(
                nom = values.getOrNull(nomIndex)?.trim().orEmpty().trim('"'),
                prenom = values.getOrNull(prenomIndex)?.trim().orEmpty().trim('"'),
                email = values.getOrNull(emailIndex)?.trim().orEmpty().trim('"'),
                binId = bin
            )
        }
    }

    private fun splitCsvRows(csvContent: String): List<String> {
        if (csvContent.isBlank()) return emptyList()
        val rows = mutableListOf<String>()
        val currentRow = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < csvContent.length) {
            val char = csvContent[i]
            when (char) {
                '"' -> {
                    if (inQuotes && i + 1 < csvContent.length && csvContent[i + 1] == '"') {
                        currentRow.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                        currentRow.append(char)
                    }
                }
                '\n' -> {
                    if (inQuotes) currentRow.append(char)
                    else {
                        rows.add(currentRow.toString())
                        currentRow.clear()
                    }
                }
                '\r' -> {
                    if (inQuotes) currentRow.append(char)
                    else {
                        rows.add(currentRow.toString())
                        currentRow.clear()
                        if (i + 1 < csvContent.length && csvContent[i + 1] == '\n') i++
                    }
                }
                else -> currentRow.append(char)
            }
            i++
        }

        if (currentRow.isNotEmpty()) rows.add(currentRow.toString())
        return rows.filter { it.isNotBlank() }
    }

    private fun detectDelimiter(headerLine: String): Char {
        val commaCount = countCharOutsideQuotes(headerLine, ',')
        val semicolonCount = countCharOutsideQuotes(headerLine, ';')
        return if (semicolonCount > commaCount) ';' else ','
    }

    private fun countCharOutsideQuotes(text: String, target: Char): Int {
        var inQuotes = false
        var count = 0
        for (c in text) {
            if (c == '"') inQuotes = !inQuotes
            if (!inQuotes && c == target) count++
        }
        return count
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val values = mutableListOf<String>()
        val currentValue = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        currentValue.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                char == delimiter && !inQuotes -> {
                    values.add(currentValue.toString())
                    currentValue.clear()
                }
                else -> currentValue.append(char)
            }
            i++
        }

        values.add(currentValue.toString())
        return values
    }

    private fun normalizeHeader(value: String): String {
        return value
            .removePrefix("\uFEFF")
            .trim()
            .lowercase()
            .replace("\"", "")
    }

    private fun sanitizeFileName(value: String): String {
        return value.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    private fun normalizeJsonBinPayload(rawJson: String): String {
        val trimmed = rawJson.trim()
        if (trimmed.isBlank()) return trimmed

        val parsed = runCatching { json.parseToJsonElement(trimmed) }.getOrNull() ?: return trimmed
        val root = parsed as? JsonObject ?: return trimmed
        val record = root["record"] ?: return trimmed
        return when (record) {
            is JsonPrimitive -> record.content
            else -> json.encodeToString(JsonElement.serializer(), record)
        }
    }

    private fun appendImportLog(message: String) {
        _importLogs.value = (_importLogs.value + message).takeLast(300)
    }

    private data class MetricMapping(
        val label: String,
        val metricKind: FlexibleMetricKind,
        val scope: RationScope
    )

    private fun mapAnalysisMetric(metricId: ExamAnalysisMetricId): MetricMapping? {
        return when (metricId) {
            ExamAnalysisMetricId.WEIGHT_KG -> MetricMapping(
                label = "Poids utilisé (kg)",
                metricKind = FlexibleMetricKind.ANIMAL_WEIGHT_KG,
                scope = RationScope.FIRST_PROPOSED
            )
            ExamAnalysisMetricId.BEC_KCAL -> MetricMapping(
                label = "BEC (kcal/j)",
                metricKind = FlexibleMetricKind.BEE_KCAL,
                scope = RationScope.FIRST_PROPOSED
            )
            ExamAnalysisMetricId.ENERGY_INTAKE_KCAL -> MetricMapping(
                label = "Apport énergie ration (kcal)",
                metricKind = FlexibleMetricKind.ENERGY_INTAKE_KCAL,
                scope = RationScope.FIRST_PROPOSED
            )
            ExamAnalysisMetricId.ENERGY_DENSITY_KCAL_100 -> MetricMapping(
                label = "Densité énergie (kcal/100g)",
                metricKind = FlexibleMetricKind.ENERGY_DENSITY,
                scope = RationScope.FIRST_PROPOSED
            )
            ExamAnalysisMetricId.PROTEIN_G -> MetricMapping(
                label = "Protéines ration (g)",
                metricKind = FlexibleMetricKind.PROTEIN_INTAKE_G,
                scope = RationScope.FIRST_PROPOSED
            )
        }
    }

    private fun firstProposedOrAny(item: CrossConsultationAnalysisViewModel.ConsultationItem): CrossConsultationAnalysisViewModel.RationSummary? {
        return item.rations.firstOrNull { !it.actual } ?: item.rations.firstOrNull()
    }

    private fun addDistribution(
        items: List<CrossConsultationAnalysisViewModel.ConsultationItem>,
        metricId: ExamAnalysisMetricId,
        label: String,
        valueExtractor: (CrossConsultationAnalysisViewModel.ConsultationItem) -> Double?
    ): MetricDistribution? {
        val values = items.mapNotNull(valueExtractor).filter { it.isFinite() }.sorted()
        if (values.isEmpty()) return null
        val min = values.first()
        val max = values.last()
        val p10 = percentile(values, 0.10)
        val median = percentile(values, 0.50)
        val p90 = percentile(values, 0.90)
        val suggestedLow = median * 0.9
        val suggestedHigh = median * 1.1
        return MetricDistribution(
            metricId = metricId,
            label = label,
            values = values,
            count = values.size,
            min = min,
            p10 = p10,
            median = median,
            p90 = p90,
            max = max,
            suggestedLow = suggestedLow,
            suggestedHigh = suggestedHigh,
            histogram = buildHistogram(values, 10)
        )
    }

    private fun percentile(sortedValues: List<Double>, p: Double): Double {
        if (sortedValues.isEmpty()) return Double.NaN
        if (sortedValues.size == 1) return sortedValues.first()
        val clamped = p.coerceIn(0.0, 1.0)
        val index = clamped * (sortedValues.size - 1)
        val low = index.toInt()
        val high = (low + 1).coerceAtMost(sortedValues.lastIndex)
        if (low == high) return sortedValues[low]
        val weight = index - low
        return sortedValues[low] * (1.0 - weight) + sortedValues[high] * weight
    }

    private fun buildHistogram(sortedValues: List<Double>, binsCount: Int): List<HistogramBin> {
        if (sortedValues.isEmpty()) return emptyList()
        val min = sortedValues.first()
        val max = sortedValues.last()
        if (min == max) {
            return listOf(HistogramBin(from = min, to = max, count = sortedValues.size))
        }
        val width = (max - min) / binsCount.toDouble()
        val counts = IntArray(binsCount)
        sortedValues.forEach { v ->
            val idx = (((v - min) / width).toInt()).coerceIn(0, binsCount - 1)
            counts[idx]++
        }
        return (0 until binsCount).map { idx ->
            val from = min + idx * width
            val to = if (idx == binsCount - 1) max else (from + width)
            HistogramBin(from = from, to = to, count = counts[idx])
        }
    }

    private fun evaluate(
        rule: ExamGradingRuleSet,
        item: CrossConsultationAnalysisViewModel.ConsultationItem
    ): ExamGradeDetail {
        val ingredientSet = item.rations.flatMap { it.ingredients }.map { it.name.lowercase() }.toSet()

        val ingredientOk =
            if (!rule.ingredientRule.enabled) false
            else {
                val required = rule.ingredientRule.required.map { it.lowercase().trim() }.filter { it.isNotBlank() }
                val forbidden = rule.ingredientRule.forbidden.map { it.lowercase().trim() }.filter { it.isNotBlank() }
                val requiredOk = required.isEmpty() || required.all { ingredientSet.contains(it) }
                val forbiddenOk = forbidden.isEmpty() || forbidden.none { ingredientSet.contains(it) }
                requiredOk && forbiddenOk && (required.isNotEmpty() || forbidden.isNotEmpty())
            }
        val ingredientPoints = if (ingredientOk) rule.ingredientRule.points else 0

        val rationQuantity = item.totalRationQuantity
        val rationOk =
            if (!rule.rationQuantityRule.enabled) false
            else {
                val min = rule.rationQuantityRule.min
                val max = rule.rationQuantityRule.max
                val hasBound = min != null || max != null
                if (!hasBound) false
                else (min == null || rationQuantity >= min) && (max == null || rationQuantity <= max)
            }
        val rationPoints = if (rationOk) rule.rationQuantityRule.points else 0

        val totalQty = item.rations.sumOf { it.quantity }
        val energyDensity =
            if (totalQty > 0.0) item.rations.sumOf { it.energyDensity * it.quantity } / totalQty else null
        val energyOk =
            if (!rule.energyDensityRule.enabled) false
            else {
                val min = rule.energyDensityRule.min
                val max = rule.energyDensityRule.max
                val hasBound = min != null || max != null
                if (!hasBound || energyDensity == null) false
                else (min == null || energyDensity >= min) && (max == null || energyDensity <= max)
            }
        val energyPoints = if (energyOk) rule.energyDensityRule.points else 0

        val nutrientTotals = mutableMapOf<String, Double>()
        item.rations.forEach { ration ->
            ration.nutrientValues.forEach { (label, value) ->
                nutrientTotals[label] = (nutrientTotals[label] ?: 0.0) + value
            }
        }
        val nutrientResults = rule.nutrientRules.map { ruleItem ->
            val value = nutrientTotals[ruleItem.nutrientLabel] ?: 0.0
            val ok =
                if (!ruleItem.enabled) false
                else {
                    val min = ruleItem.min
                    val max = ruleItem.max
                    val hasBound = min != null || max != null
                    if (!hasBound) false
                    else (min == null || value >= min) && (max == null || value <= max)
                }
            NutrientResult(
                nutrientLabel = ruleItem.nutrientLabel,
                value = value,
                ok = ok,
                points = if (ok) ruleItem.points else 0
            )
        }

        val referenceOk =
            if (!rule.referenceRule.enabled) false
            else {
                val allowed = rule.referenceRule.allowedReferenceIds.map { it.trim() }.filter { it.isNotBlank() }
                allowed.isNotEmpty() && item.referenceId != null && allowed.contains(item.referenceId)
            }
        val referencePoints = if (referenceOk) rule.referenceRule.points else 0

        val adviceText = item.adviceText.lowercase()
        val adviceOk =
            if (!rule.adviceRule.enabled) false
            else {
                val required = rule.adviceRule.requiredWords.map { it.lowercase().trim() }.filter { it.isNotBlank() }
                val forbidden = rule.adviceRule.forbiddenWords.map { it.lowercase().trim() }.filter { it.isNotBlank() }
                val requiredOk = required.isEmpty() || required.all { adviceText.contains(it) }
                val forbiddenOk = forbidden.isEmpty() || forbidden.none { adviceText.contains(it) }
                requiredOk && forbiddenOk && (required.isNotEmpty() || forbidden.isNotEmpty())
            }
        val advicePoints = if (adviceOk) rule.adviceRule.points else 0

        val customCriteriaResults = rule.customCriteria.map { criterion ->
            evaluateCustomCriterion(criterion, item)
        }

        return ExamGradeDetail(
            ingredientOk = ingredientOk,
            ingredientPoints = ingredientPoints,
            rationQuantity = rationQuantity,
            rationOk = rationOk,
            rationPoints = rationPoints,
            energyDensity = energyDensity,
            energyOk = energyOk,
            energyPoints = energyPoints,
            nutrientResults = nutrientResults,
            referenceId = item.referenceId,
            referenceOk = referenceOk,
            referencePoints = referencePoints,
            adviceOk = adviceOk,
            advicePoints = advicePoints,
            customCriteriaResults = customCriteriaResults
        )
    }

    private fun detailTotal(detail: ExamGradeDetail): Double {
        val nutrientPoints = detail.nutrientResults.sumOf { it.points }
        val customPoints = detail.customCriteriaResults.sumOf { it.pointsAwarded }
        return (detail.ingredientPoints + detail.rationPoints + detail.energyPoints + nutrientPoints + detail.referencePoints + detail.advicePoints).toDouble() + customPoints
    }

    private fun evaluateCustomCriterion(
        criterion: FlexibleCriterionRule,
        item: CrossConsultationAnalysisViewModel.ConsultationItem
    ): FlexibleCriterionResult {
        val criterionId = criterion.id.ifBlank { "${criterion.metric.name}:${criterion.label}" }
        if (!criterion.enabled) {
            return FlexibleCriterionResult(
                id = criterionId,
                label = criterion.label,
                metric = criterion.metric,
                rationScope = criterion.rationScope
            )
        }

        val rations = selectRationsByScope(item, criterion.rationScope)
        val ingredientText = rations.flatMap { ration -> ration.ingredients }.joinToString(" | ") { ingredient -> ingredient.name }
        val adviceText = item.adviceText
        val referenceId = item.referenceId.orEmpty()

        return when (criterion.metric) {
            FlexibleMetricKind.TOTAL_RATION_QUANTITY -> {
                val value = rations.sumOf { it.quantity }
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.ENERGY_DENSITY -> {
                val totalQty = rations.sumOf { it.quantity }
                val value = if (totalQty > 0.0) rations.sumOf { it.energyDensity * it.quantity } / totalQty else null
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.BEE_KCAL -> {
                val value = rations.mapNotNull { it.beeKcal }.firstOrNull()
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.ENERGY_INTAKE_KCAL -> {
                val value = rations.sumOf { it.energyTotalKcal }
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.PROTEIN_INTAKE_G -> {
                val value = rations.sumOf { it.proteins }
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.ANIMAL_WEIGHT_KG -> {
                val value = rations.map { it.animalWeightKg }.firstOrNull()
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.NUTRIENT_TOTAL -> {
                val nutrient = criterion.nutrientLabel.trim()
                val value = if (nutrient.isBlank()) null else rations.sumOf { it.nutrientValues[nutrient] ?: 0.0 }
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.RATION_COUNT -> {
                val value = rations.size.toDouble()
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.INGREDIENT_KEYWORDS -> {
                buildTextResult(criterionId, criterion, ingredientText)
            }
            FlexibleMetricKind.ADVICE_KEYWORDS -> {
                buildTextResult(criterionId, criterion, adviceText)
            }
            FlexibleMetricKind.REFERENCE_ID -> {
                buildTextResult(criterionId, criterion, referenceId, exactTokenMatch = true)
            }
        }
    }

    private fun selectRationsByScope(
        item: CrossConsultationAnalysisViewModel.ConsultationItem,
        scope: RationScope
    ): List<CrossConsultationAnalysisViewModel.RationSummary> {
        return when (scope) {
            RationScope.ALL -> item.rations
            RationScope.CURRENT_ONLY -> item.rations.filter { it.actual }
            RationScope.PROPOSED_ONLY -> item.rations.filter { !it.actual }
            RationScope.FIRST_PROPOSED -> item.rations.firstOrNull { !it.actual }?.let { listOf(it) } ?: emptyList()
        }
    }

    private fun buildNumericResult(
        id: String,
        criterion: FlexibleCriterionRule,
        value: Double?
    ): FlexibleCriterionResult {
        val hasBounds = criterion.min != null || criterion.max != null
        val ok = if (!hasBounds || value == null || !value.isFinite()) {
            false
        } else {
            (criterion.min == null || value >= criterion.min) &&
                (criterion.max == null || value <= criterion.max)
        }
        return FlexibleCriterionResult(
            id = id,
            label = criterion.label,
            metric = criterion.metric,
            rationScope = criterion.rationScope,
            measuredNumber = value,
            measuredText = value?.toString().orEmpty(),
            ok = ok,
            pointsAwarded = if (ok) criterion.points else 0.0
        )
    }

    private fun buildTextResult(
        id: String,
        criterion: FlexibleCriterionRule,
        sourceText: String,
        exactTokenMatch: Boolean = false
    ): FlexibleCriterionResult {
        val normalizedText = sourceText.lowercase()
        val includes = criterion.includes.map { it.trim().lowercase() }.filter { it.isNotBlank() }
        val excludes = criterion.excludes.map { it.trim().lowercase() }.filter { it.isNotBlank() }
        val includesOk =
            when {
                includes.isEmpty() -> true
                criterion.requireAllIncludes -> includes.all { tokenMatch(normalizedText, it, exactTokenMatch) }
                else -> includes.any { tokenMatch(normalizedText, it, exactTokenMatch) }
            }
        val excludesOk = excludes.none { tokenMatch(normalizedText, it, exactTokenMatch) }
        val hasCondition = includes.isNotEmpty() || excludes.isNotEmpty()
        val ok = hasCondition && includesOk && excludesOk
        return FlexibleCriterionResult(
            id = id,
            label = criterion.label,
            metric = criterion.metric,
            rationScope = criterion.rationScope,
            measuredText = sourceText,
            ok = ok,
            pointsAwarded = if (ok) criterion.points else 0.0
        )
    }

    private fun tokenMatch(text: String, token: String, exactTokenMatch: Boolean): Boolean {
        return if (exactTokenMatch) text.trim() == token else text.contains(token)
    }
}
