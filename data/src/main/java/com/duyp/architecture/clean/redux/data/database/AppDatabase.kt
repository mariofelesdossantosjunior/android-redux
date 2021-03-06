package com.duyp.architecture.clean.redux.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.duyp.architecture.clean.redux.data.DateConverter
import com.duyp.architecture.clean.redux.data.recentrepo.RecentRepoDao
import com.duyp.architecture.clean.redux.data.recentrepo.RecentRepoLocalData
import com.duyp.architecture.clean.redux.data.repo.RepoLocalData
import com.duyp.architecture.clean.redux.repo.data.database.RepoDao

@Database(
    version = 2, exportSchema = true, entities = [
        RepoLocalData::class,
        RecentRepoLocalData::class
    ]
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun repoDao(): RepoDao

    abstract fun recentRepoDao(): RecentRepoDao
}

