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

    @Transaction
    @Query("SELECT * FROM apps ORDER BY appName COLLATE NOCASE ASC")
    fun getAllAppsWithTags(): Flow<List<AppWithTags>>

    @Transaction
    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getAppWithTags(packageName: String): AppWithTags?

    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): AppEntity?

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
