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
    val posts: Flow<List<PostEntity>> = postDao.getAllPosts()
    val stories: Flow<List<StoryEntity>> = storyDao.getAllStories()
    val messages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    suspend fun getOrCreateUserStats(): UserStatsEntity {
        val existing = userStatsDao.getUserStats()
        if (existing == null) {
            val defaultStats = UserStatsEntity()
            userStatsDao.insertUserStats(defaultStats)
            return defaultStats
        }
        return existing
    }

    suspend fun incrementSnapScore(amount: Int) {
        val current = getOrCreateUserStats()
        userStatsDao.updateUserStats(current.copy(snapScore = current.snapScore + amount))
    }

    suspend fun incrementStreak() {
        val current = getOrCreateUserStats()
        userStatsDao.updateUserStats(current.copy(
            tubeStreak = current.tubeStreak + 1,
            nextStreakHours = 24
        ))
    }

    suspend fun updateUserProfile(name: String, bio: String) {
        val current = getOrCreateUserStats()
        userStatsDao.updateUserStats(current.copy(name = name, bio = bio))
    }

    suspend fun signupUser(username: String, name: String, bio: String) {
        val current = getOrCreateUserStats()
        userStatsDao.updateUserStats(current.copy(
            username = username,
            name = name,
            bio = bio,
            isLoggedIn = true
        ))
    }

    suspend fun loginUser(username: String) {
        val current = getOrCreateUserStats()
        userStatsDao.updateUserStats(current.copy(
            username = username,
            isLoggedIn = true
        ))
    }

    suspend fun logoutUser() {
        val current = getOrCreateUserStats()
        userStatsDao.updateUserStats(current.copy(
            isLoggedIn = false
        ))
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
        val existingPosts = posts.firstOrNull() ?: emptyList()
        val existingStories = stories.firstOrNull() ?: emptyList()
        val existingMessages = messages.firstOrNull() ?: emptyList()

        if (userStatsDao.getUserStats() == null) {
            userStatsDao.insertUserStats(UserStatsEntity())
        }

        if (existingPosts.isEmpty()) {
            val defaults = listOf(
                PostEntity(
                    username = "sarah_travels",
                    userAvatarIndex = 1,
                    mediaUrl = "neon_cafe",
                    mediaType = "IMAGE",
                    caption = "Dreamy magenta vibes at this cyber cafe in Seoul! ☕️🌸 Built on Chattube's Golden Hour AI lens filter.",
                    likesCount = 1422,
                    isLiked = false,
                    commentsCount = 28,
                    filterApplied = "Golden Hour"
                ),
                PostEntity(
                    username = "sam_skaters",
                    userAvatarIndex = 2,
                    mediaUrl = "neon_skateboard",
                    mediaType = "TUBE", // TUBE represent video
                    caption = "ChatTube Shorts checking in! 🛹🚨 Landing this crisp tre-flip under the neon bridges. Sound on!",
                    likesCount = 987,
                    isLiked = true,
                    commentsCount = 49,
                    filterApplied = "Neon Overdrive"
                ),
                PostEntity(
                    username = "cyber_aesthetic",
                    userAvatarIndex = 3,
                    mediaUrl = "cyberpunk_rain",
                    mediaType = "IMAGE",
                    caption = "Tokyo Rain in high definition 🌧️ Tokyo streetlights refracting beautifully. Used the Analog Film filter ✨",
                    likesCount = 3520,
                    isLiked = false,
                    commentsCount = 104,
                    filterApplied = "1995 Nostalgia"
                )
            )
            for (p in defaults) {
                postDao.insertPost(p)
            }
        }

        if (existingStories.isEmpty()) {
            val defaultStories = listOf(
                StoryEntity(
                    username = "alex_vlogs",
                    userAvatarIndex = 4,
                    mediaUrl = "gradient_rose",
                    durationSeconds = 6,
                    isViewed = false
                ),
                StoryEntity(
                    username = "sarah_travels",
                    userAvatarIndex = 1,
                    mediaUrl = "gradient_blue",
                    durationSeconds = 5,
                    isViewed = false
                ),
                StoryEntity(
                    username = "foodie_vibes",
                    userAvatarIndex = 5,
                    mediaUrl = "gradient_amber",
                    durationSeconds = 4,
                    isViewed = true
                )
            )
            for (s in defaultStories) {
                storyDao.insertStory(s)
            }
        }

        if (existingMessages.isEmpty()) {
            val defaultMessages = listOf(
                MessageEntity(
                    senderName = "Sarah Travels",
                    receiverName = "You",
                    messageType = "TEXT",
                    content = "Hey!! Did you try the ChatTube AI camera filters yet? They are wild 🤯🛸",
                    isOpened = false,
                    timestamp = System.currentTimeMillis() - 60000 * 10
                ),
                MessageEntity(
                    senderName = "Alex Vlogs",
                    receiverName = "You",
                    messageType = "SNAP",
                    content = "Check out this sunset view in golden hour!",
                    isOpened = false,
                    durationSeconds = 5,
                    appliedFilter = "Golden Hour",
                    timestamp = System.currentTimeMillis() - 60000 * 5
                ),
                MessageEntity(
                    senderName = "You",
                    receiverName = "Alex Vlogs",
                    messageType = "TEXT",
                    content = "Ahaha nice, looks legendary! Using retro lens?",
                    isOpened = true,
                    timestamp = System.currentTimeMillis() - 60000 * 4
                ),
                MessageEntity(
                    senderName = "Jake Skates",
                    receiverName = "You",
                    messageType = "SNAP",
                    content = "Insane kickflip replay ⚡️",
                    isOpened = true,
                    durationSeconds = 4,
                    appliedFilter = "Neon Overdrive",
                    timestamp = System.currentTimeMillis() - 60000 * 2
                )
            )
            for (m in defaultMessages) {
                messageDao.insertMessage(m)
            }
        }
    }
}
