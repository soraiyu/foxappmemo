package com.soraiyu.foxappmemo.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.soraiyu.foxappmemo.data.entity.AppEntity
import com.soraiyu.foxappmemo.data.entity.AppTagCrossRef
import com.soraiyu.foxappmemo.data.entity.AppWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ── Core queries ─────────────────────────────────────────────────────────

    /** All apps with their tags, newest installs first (nulls last). */
    @Transaction
    @Query(
        """
        SELECT * FROM apps
        ORDER BY installDate DESC, appName COLLATE NOCASE ASC
        """,
    )
    fun getAllAppsWithTags(): Flow<List<AppWithTags>>

    @Transaction
    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getAppWithTags(packageName: String): AppWithTags?

    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): AppEntity?

    // ── Filtered queries ─────────────────────────────────────────────────────

    /** Filter by a single [status] value (e.g. "trying", "main"). */
    @Transaction
    @Query(
        """
        SELECT * FROM apps
        WHERE status = :status
        ORDER BY installDate DESC, appName COLLATE NOCASE ASC
        """,
    )
    fun getAppsByStatus(status: String): Flow<List<AppWithTags>>

    /** Filter by [tagId] — returns apps that have the given tag attached. */
    @Transaction
    @Query(
        """
        SELECT apps.* FROM apps
        INNER JOIN app_tag_cross_ref ON apps.packageName = app_tag_cross_ref.packageName
        WHERE app_tag_cross_ref.tagId = :tagId
        ORDER BY apps.installDate DESC, apps.appName COLLATE NOCASE ASC
        """,
    )
    fun getAppsByTag(tagId: Long): Flow<List<AppWithTags>>

    /**
     * Filter by minimum rating (inclusive). Apps with a null rating are excluded
     * because `null` cannot satisfy `>= minRating`; they are not rated at all.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM apps
        WHERE rating >= :minRating
        ORDER BY rating DESC, installDate DESC, appName COLLATE NOCASE ASC
        """,
    )
    fun getAppsByMinRating(minRating: Int): Flow<List<AppWithTags>>

    /**
     * Full-text search across [appName] and [packageName].
     * [query] must already be a valid LIKE pattern (e.g. `%foo%`);
     * `%` and `_` in the wrapped query are treated as SQL LIKE wildcards.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM apps
        WHERE appName LIKE :query OR packageName LIKE :query
        ORDER BY installDate DESC, appName COLLATE NOCASE ASC
        """,
    )
    fun searchApps(query: String): Flow<List<AppWithTags>>

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)

    @Update
    suspend fun updateApp(app: AppEntity)

    @Delete
    suspend fun deleteApp(app: AppEntity)

    @Query("DELETE FROM apps WHERE packageName = :packageName")
    suspend fun deleteAppByPackageName(packageName: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: AppTagCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: AppTagCrossRef)

    @Query("DELETE FROM app_tag_cross_ref WHERE packageName = :packageName")
    suspend fun deleteCrossRefsForApp(packageName: String)
}
