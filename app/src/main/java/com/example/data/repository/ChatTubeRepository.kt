package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class ChatTubeRepository(
    private val userStatsDao: UserStatsDao,
    private val postDao: PostDao,
    private val storyDao: StoryDao,
    private val messageDao: MessageDao
) {
    val userStats: Flow<UserStatsEntity?> = userStatsDao.getUserStatsFlow()
    val loggedInAccounts: Flow<List<UserStatsEntity>> = userStatsDao.getLoggedInAccountsFlow()
    val posts: Flow<List<PostEntity>> = postDao.getAllPosts()
    val stories: Flow<List<StoryEntity>> = storyDao.getAllStories()
    val messages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    suspend fun getOrCreateUserStats(): UserStatsEntity {
        val existing = userStatsDao.getUserStats()
        if (existing == null) {
            // Should not happen if correctly gated, but provide fallback to prevent crash
            return UserStatsEntity(username = "guest") 
        }
        return existing
    }

    suspend fun incrementSnapScore(amount: Int) {
        val current = userStatsDao.getUserStats() ?: return
        userStatsDao.updateUserStats(current.copy(snapScore = current.snapScore + amount))
    }

    suspend fun incrementStreak() {
        val current = userStatsDao.getUserStats() ?: return
        userStatsDao.updateUserStats(current.copy(
            tubeStreak = current.tubeStreak + 1,
            nextStreakHours = 24
        ))
    }

    suspend fun gamifyReelUpload() {
        val current = userStatsDao.getUserStats() ?: return
        val newCoins = current.coins + 1
        val newIsVip = current.isVip || newCoins >= 400
        userStatsDao.updateUserStats(current.copy(coins = newCoins, isVip = newIsVip))
    }

    suspend fun updateUserProfile(name: String, bio: String, serverRegion: String) {
        val current = userStatsDao.getUserStats() ?: return
        userStatsDao.updateUserStats(current.copy(name = name, bio = bio, serverRegion = serverRegion))
    }

    suspend fun updateProfilePic(uri: String) {
        val current = userStatsDao.getUserStats() ?: return
        userStatsDao.updateUserStats(current.copy(profilePicUri = uri))
    }

    suspend fun signupUser(phone: String, passwordHash: String, username: String, name: String, bio: String): Boolean {
        val existingUsername = userStatsDao.getUserByUsername(username)
        val existingPhone = userStatsDao.getUserByPhone(phone)
        if (existingUsername != null || (phone.isNotEmpty() && existingPhone != null)) {
             return false // Already taken
        }

        userStatsDao.clearActiveAccounts()

        userStatsDao.insertUserStats(UserStatsEntity(
            username = username,
            name = name,
            phone = phone,
            passwordHash = passwordHash,
            bio = bio,
            isLoggedIn = true,
            isActive = true
        ))
        return true
    }

    suspend fun loginUser(identifier: String, passwordHash: String): Boolean {
        val matchingUser = userStatsDao.getUserByUsername(identifier) ?: userStatsDao.getUserByPhone(identifier)
        if (matchingUser != null && matchingUser.passwordHash == passwordHash) {
             userStatsDao.clearActiveAccounts()
             userStatsDao.updateUserStats(matchingUser.copy(isLoggedIn = true, isActive = true))
             return true
        }
        return false
    }

    suspend fun logoutUser() {
        val current = userStatsDao.getUserStats()
        if (current != null) {
            userStatsDao.logoutUser(current.username)
        }
    }

    suspend fun switchActiveAccount(username: String) {
        userStatsDao.clearActiveAccounts()
        userStatsDao.setActiveAccount(username)
    }

    suspend fun logoutAll() {
        userStatsDao.logoutAll()
    }

    suspend fun addPost(
        username: String,
        userAvatarIndex: Int,
        mediaUrl: String,
        mediaType: String,
        caption: String,
        filterApplied: String = "None"
    ) {
        val newPost = PostEntity(
            username = username,
            userAvatarIndex = userAvatarIndex,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            caption = caption,
            likesCount = 0,
            isLiked = false,
            commentsCount = 0,
            filterApplied = filterApplied
        )
        postDao.insertPost(newPost)
        incrementSnapScore(15) // earn points for posting!
    }

    suspend fun toggleLikePost(postId: Long, isLiked: Boolean) {
        postDao.updatePostLiked(postId, isLiked)
    }

    suspend fun addStory(username: String, userAvatarIndex: Int, mediaUrl: String) {
        val newStory = StoryEntity(
            username = username,
            userAvatarIndex = userAvatarIndex,
            mediaUrl = mediaUrl,
            durationSeconds = 5,
            isViewed = false
        )
        storyDao.insertStory(newStory)
        incrementSnapScore(10)
    }

    suspend fun markStoryAsViewed(storyId: Long) {
        storyDao.markStoryAsViewed(storyId)
    }

    suspend fun sendMessage(sender: String, receiver: String, type: String, content: String, duration: Int = 5, filter: String = "None") {
        val newMessage = MessageEntity(
            senderName = sender,
            receiverName = receiver,
            messageType = type,
            content = content,
            isOpened = false,
            durationSeconds = duration,
            appliedFilter = filter
        )
        messageDao.insertMessage(newMessage)
        incrementSnapScore(5) // earn score on chat snap
    }

    suspend fun openSnap(messageId: Long) {
        messageDao.markMessageAsOpened(messageId)
    }

    suspend fun deleteMessage(messageId: Long) {
        messageDao.deleteMessageById(messageId)
    }

    // Initialize database default dummy database entries if empty
    suspend fun seedDatabaseIfEmpty() {
        // App is using real accounts now, so we don't automatically insert a guest user.
    }
}
