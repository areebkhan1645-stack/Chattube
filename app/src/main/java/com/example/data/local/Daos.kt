package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE isActive = 1 LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE isActive = 1 LIMIT 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Query("SELECT * FROM user_stats WHERE isLoggedIn = 1")
    fun getLoggedInAccountsFlow(): Flow<List<UserStatsEntity>>

    @Query("SELECT * FROM user_stats WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserStatsEntity?

    @Query("SELECT * FROM user_stats WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUserStats(stats: UserStatsEntity)

    @Update
    suspend fun updateUserStats(stats: UserStatsEntity)
    
    @Query("UPDATE user_stats SET isActive = 0")
    suspend fun clearActiveAccounts()

    @Query("UPDATE user_stats SET isActive = 0, isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("UPDATE user_stats SET isActive = 1 WHERE username = :username")
    suspend fun setActiveAccount(username: String)

    @Query("UPDATE user_stats SET isActive = 0, isLoggedIn = 0 WHERE username = :username")
    suspend fun logoutUser(username: String)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Query("UPDATE posts SET isLiked = :isLiked, likesCount = likesCount + (case when :isLiked = 1 then 1 else -1 end) WHERE id = :postId")
    suspend fun updatePostLiked(postId: Long, isLiked: Boolean)

    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Query("UPDATE stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markStoryAsViewed(storyId: Long)

    @Query("DELETE FROM stories")
    suspend fun clearAllStories()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET isOpened = 1 WHERE id = :messageId")
    suspend fun markMessageAsOpened(messageId: Long)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}
