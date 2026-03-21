package com.soraiyu.foxappmemo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.soraiyu.foxappmemo.data.entity.AppEntity
import com.soraiyu.foxappmemo.data.entity.AppTagCrossRef
import com.soraiyu.foxappmemo.data.entity.TagEntity

@Database(
    entities = [AppEntity::class, TagEntity::class, AppTagCrossRef::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun tagDao(): TagDao
}
