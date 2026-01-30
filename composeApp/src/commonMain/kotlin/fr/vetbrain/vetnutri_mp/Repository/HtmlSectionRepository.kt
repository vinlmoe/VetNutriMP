package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.DataBase.HtmlSectionDao
import fr.vetbrain.vetnutri_mp.DataBase.HtmlSectionEntity
import fr.vetbrain.vetnutri_mp.Export.*
import fr.vetbrain.vetnutri_mp.Utils.isDebugBuild
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Repository pour gérer les sections HTML réutilisables */
class HtmlSectionRepository(private val dao: HtmlSectionDao) {

    private val json = Json { prettyPrint = isDebugBuild() }

    /** Sauvegarde une section HTML */
    suspend fun saveSection(section: HtmlSection): Result<Unit> {
        return try {
            val entity = section.toEntity()
            dao.insertSection(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Met à jour une section HTML */
    suspend fun updateSection(section: HtmlSection): Result<Unit> {
        return try {
            val entity = section.toEntity()
            dao.updateSection(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Supprime une section HTML */
    suspend fun deleteSection(sectionId: String): Result<Unit> {
        return try {
            val entity = dao.getSectionById(sectionId)
            if (entity != null) {
                dao.deleteSection(entity)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Section not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère une section HTML par son ID */
    suspend fun getSectionById(id: String): Result<HtmlSection?> {
        return try {
            val entity = dao.getSectionById(id)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère toutes les sections HTML */
    suspend fun getAllSections(): Result<List<HtmlSection>> {
        return try {
            val entities = dao.getAllSections()
            val sections = entities.map { it.toDomain() }
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère les sections par catégorie */
    suspend fun getSectionsByCategory(category: SectionCategory): Result<List<HtmlSection>> {
        return try {
            val entities = dao.getSectionsByCategory(category.name)
            val sections = entities.map { it.toDomain() }
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère les sections modèles */
    suspend fun getTemplateSections(): Result<List<HtmlSection>> {
        return try {
            val entities = dao.getTemplateSections()
            val sections = entities.map { it.toDomain() }
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Recherche des sections HTML */
    suspend fun searchSections(query: String): Result<List<HtmlSection>> {
        return try {
            val entities = dao.searchSections(query)
            val sections = entities.map { it.toDomain() }
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Crée une nouvelle section à partir d'un modèle */
    suspend fun createFromTemplate(templateId: String, newTitle: String): Result<HtmlSection> {
        return try {
            val templateEntity = dao.getSectionById(templateId)
            if (templateEntity != null) {
                val template = templateEntity.toDomain()
                val newSection =
                        template.copy(
                                id = generateId(),
                                title = newTitle,
                                isTemplate = false,
                                createdAt = Clock.System.now(),
                                updatedAt = Clock.System.now()
                        )
                saveSection(newSection)
                Result.success(newSection)
            } else {
                Result.failure(Exception("Template not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Duplique une section existante */
    suspend fun duplicateSection(sectionId: String, newTitle: String): Result<HtmlSection> {
        return try {
            val originalEntity = dao.getSectionById(sectionId)
            if (originalEntity != null) {
                val original = originalEntity.toDomain()
                val duplicated =
                        original.copy(
                                id = generateId(),
                                title = newTitle,
                                isTemplate = false,
                                createdAt = Clock.System.now(),
                                updatedAt = Clock.System.now()
                        )
                saveSection(duplicated)
                Result.success(duplicated)
            } else {
                Result.failure(Exception("Section not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Méthodes utilitaires de conversion

    private fun HtmlSection.toEntity(): HtmlSectionEntity {
        return HtmlSectionEntity(
                id = this.id,
                title = this.title,
                contentJson = json.encodeToString(this.content),
                category = this.category.name,
                tagsJson = json.encodeToString(this.tags),
                createdAt = this.createdAt.toEpochMilliseconds(),
                updatedAt = this.updatedAt.toEpochMilliseconds(),
                isTemplate = this.isTemplate,
                priority = this.priority,
                isActive = this.isActive,
                targetSpeciesJson = json.encodeToString(this.targetSpecies),
                targetAgeGroupsJson = json.encodeToString(this.targetAgeGroups),
                usageCount = this.usageCount,
                lastUsed = this.lastUsed?.toEpochMilliseconds()
        )
    }

    private fun HtmlSectionEntity.toDomain(): HtmlSection {
        return HtmlSection(
                id = this.id,
                title = this.title,
                content = json.decodeFromString(this.contentJson),
                category = SectionCategory.valueOf(this.category),
                tags = json.decodeFromString(this.tagsJson),
                createdAt = Instant.fromEpochMilliseconds(this.createdAt),
                updatedAt = Instant.fromEpochMilliseconds(this.updatedAt),
                isTemplate = this.isTemplate,
                priority = this.priority,
                isActive = this.isActive,
                targetSpecies = json.decodeFromString(this.targetSpeciesJson),
                targetAgeGroups = json.decodeFromString(this.targetAgeGroupsJson),
                usageCount = this.usageCount,
                lastUsed = this.lastUsed?.let { Instant.fromEpochMilliseconds(it) }
        )
    }

    private fun generateId(): String =
            "section_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}_${kotlin.random.Random.nextInt(1000)}"
}
