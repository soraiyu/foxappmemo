package com.soraiyu.foxappmemo.data.repository

import androidx.room.withTransaction
import com.soraiyu.foxappmemo.data.db.AppDao
import com.soraiyu.foxappmemo.data.db.AppDatabase
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
    private val db: AppDatabase,
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
     * The [query] string is trimmed and wrapped with `%` wildcards automatically.
     * Note: `%` and `_` in [query] are treated as SQL LIKE wildcards.
     */
    fun searchApps(query: String): Flow<List<AppWithTags>> {
        val trimmed = query.trim()
        return appDao.searchApps("%$trimmed%")
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun saveApp(app: AppEntity, tagNames: List<String>) {
        db.withTransaction {
            appDao.insertApp(app)
            // Replace all cross-refs for this app
            appDao.deleteCrossRefsForApp(app.packageName)
            tagNames.forEach { name ->
                val trimmed = name.trim()
                if (trimmed.isNotEmpty()) {
                    // Use getOrCreate to safely handle concurrent inserts:
                    // insertTag with IGNORE returns -1 on conflict, so we
                    // re-query to get the actual id.
                    val tagId = tagDao.insertTag(TagEntity(name = trimmed))
                        .takeIf { it > 0 }
                        ?: tagDao.getTagByName(trimmed)?.id
                        ?: return@forEach
                    appDao.insertCrossRef(AppTagCrossRef(app.packageName, tagId))
                }
            }
            tagDao.deleteUnusedTags()
        }
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
