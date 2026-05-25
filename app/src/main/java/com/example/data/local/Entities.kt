package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val username: String,
    val name: String = "You",
    val phone: String = "",
    val passwordHash: String = "",
    val bio: String = "Creator & Visual Philosopher 📸✨",
    val avatarUrl: String = "",
    val snapScore: Int = 0,
    val tubeStreak: Int = 0,
    val nextStreakHours: Int = 6,
    val serverRegion: String = "Asia-South (India)",
    val coins: Int = 0,
    val isVip: Boolean = false,
    val profilePicUri: String? = null,
    val isLoggedIn: Boolean = false,
    val isActive: Boolean = false
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val userAvatarIndex: Int, // index to get consistent avatar icon color or drawing
    val mediaUrl: String, // can be a drawable name or mock uri
    val mediaType: String, // "IMAGE" or "TUBE"
    val caption: String,
    val likesCount: Int,
    val isLiked: Boolean,
    val commentsCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val filterApplied: String = "None"
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val userAvatarIndex: Int,
    val mediaUrl: String,
    val durationSeconds: Int = 5,
    val isViewed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val receiverName: String,
    val messageType: String, // "TEXT" or "SNAP"
    val content: String, // actual text message or description/reference to a Snap camera capture
    val isOpened: Boolean = false, // Snapchat-style open once state
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 5,
    val appliedFilter: String = "None"
)
