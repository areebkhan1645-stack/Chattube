package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserStatsEntity::class,
        PostEntity::class,
        StoryEntity::class,
        MessageEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userStatsDao(): UserStatsDao
    abstract fun postDao(): PostDao
    abstract fun storyDao(): StoryDao
    abstract fun messageDao(): MessageDao
}
