package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserStatsEntity::class,
        PostEntity::class,
        StoryEntity::class,
        MessageEntity::class,
        ChatGroupEntity::class,
        NotificationEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userStatsDao(): UserStatsDao
    abstract fun postDao(): PostDao
    abstract fun storyDao(): StoryDao
    abstract fun messageDao(): MessageDao
    abstract fun chatGroupDao(): ChatGroupDao
    abstract fun notificationDao(): NotificationDao
}
