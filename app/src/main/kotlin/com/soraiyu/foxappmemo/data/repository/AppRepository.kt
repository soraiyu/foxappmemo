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
    fun getAllAppsWithTags(): Flow<List<AppWithTags>> = appDao.getAllAppsWithTags()

    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()

    suspend fun getAppWithTags(packageName: String): AppWithTags? =
        appDao.getAppWithTags(packageName)

    suspend fun saveApp(app: AppEntity, tagNames: List<String>) {
        appDao.insertApp(app)
        // Update tags for this app
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
