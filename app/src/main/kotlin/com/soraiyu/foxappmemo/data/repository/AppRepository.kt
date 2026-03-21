package com.soraiyu.foxappmemo.data.repository

import com.soraiyu.foxappmemo.data.db.AppDao
import com.soraiyu.foxappmemo.data.db.TagDao
import com.soraiyu.foxappmemo.data.entity.AppEntity
import com.soraiyu.foxappmemo.data.entity.AppTagCrossRef
import com.soraiyu.foxappmemo.data.entity.AppWithTags
import com.soraiyu.foxappmemo.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val appDao: AppDao,
    private val tagDao: TagDao,
) {
    // ── Read ─────────────────────────────────────────────────────────────────

    /** All apps with tags, ordered by installDate DESC. */
    fun getAllAppsWithTags(): Flow<List<AppWithTags>> = appDao.getAllAppsWithTags()

    /** All tags alphabetically. */
    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()

    suspend fun getAppWithTags(packageName: String): AppWithTags? =
        appDao.getAppWithTags(packageName)

    // ── Filtered queries ─────────────────────────────────────────────────────

    /** Apps whose [AppEntity.status] matches [status]. */
    fun getAppsByStatus(status: String): Flow<List<AppWithTags>> =
        appDao.getAppsByStatus(status)

    /** Apps that have tag [tagId] attached. */
    fun getAppsByTag(tagId: Long): Flow<List<AppWithTags>> =
        appDao.getAppsByTag(tagId)

    /** Apps whose [AppEntity.rating] is ≥ [minRating]. */
    fun getAppsByMinRating(minRating: Int): Flow<List<AppWithTags>> =
        appDao.getAppsByMinRating(minRating)

    /**
     * Full-text search across appName and packageName (case-insensitive LIKE).
     * The [query] string is wrapped with `%` wildcards automatically.
     * LIKE special characters (`%`, `_`, `\`) in [query] are escaped so that
     * they are treated as literals, not SQL wildcards.
     */
    fun searchApps(query: String): Flow<List<AppWithTags>> {
        // Escape LIKE special characters before wrapping with wildcards.
        val escaped = query.trim()
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        return appDao.searchApps("%$escaped%")
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun saveApp(app: AppEntity, tagNames: List<String>) {
        appDao.insertApp(app)
        // Replace all cross-refs for this app
        appDao.deleteCrossRefsForApp(app.packageName)
        tagNames.forEach { name ->
            val trimmed = name.trim()
            if (trimmed.isNotEmpty()) {
                val existingTag = tagDao.getTagByName(trimmed)
                val tagId = existingTag?.id ?: tagDao.insertTag(TagEntity(name = trimmed))
                if (tagId > 0) {
                    appDao.insertCrossRef(AppTagCrossRef(app.packageName, tagId))
                }
            }
        }
        tagDao.deleteUnusedTags()
    }

    suspend fun deleteApp(packageName: String) {
        appDao.deleteAppByPackageName(packageName)
        tagDao.deleteUnusedTags()
    }

    suspend fun getOrCreateTag(name: String): TagEntity {
        val existing = tagDao.getTagByName(name)
        if (existing != null) return existing
        val id = tagDao.insertTag(TagEntity(name = name))
        return TagEntity(id = id, name = name)
    }
}
