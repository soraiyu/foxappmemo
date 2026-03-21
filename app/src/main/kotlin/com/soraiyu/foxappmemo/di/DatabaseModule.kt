package com.soraiyu.foxappmemo.di

import android.content.Context
import androidx.room.Room
import com.soraiyu.foxappmemo.data.db.AppDao
import com.soraiyu.foxappmemo.data.db.AppDatabase
import com.soraiyu.foxappmemo.data.db.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "foxappmemo.db",
        ).build()

    @Provides
    fun provideAppDao(db: AppDatabase): AppDao = db.appDao()

    @Provides
    fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()
}
