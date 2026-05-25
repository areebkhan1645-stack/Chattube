package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DatabaseProvider
import com.example.data.gemini.AIFilterResult
import com.example.data.gemini.GeminiService
import com.example.data.local.MessageEntity
import com.example.data.local.PostEntity
import com.example.data.local.StoryEntity
import com.example.data.local.UserStatsEntity
import com.example.data.repository.ChatTubeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CameraUiState {
    object Idle : CameraUiState
    object LoadingFilter : CameraUiState
    data class FilterLoaded(val filter: AIFilterResult) : CameraUiState
    data class Error(val message: String) : CameraUiState
}

class ChatTubeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = ChatTubeRepository(
        userStatsDao = db.userStatsDao(),
        postDao = db.postDao(),
        storyDao = db.storyDao(),
        messageDao = db.messageDao()
    )

    // State flows representing database contents
    val posts: StateFlow<List<PostEntity>> = repository.posts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.stories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<MessageEntity>> = repository.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStatsEntity?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active visual States
    private val _activeViewingStoryIndex = MutableStateFlow<Int?>(-1) // index in stories or story list, null/negative if none
    val activeViewingStoryIndex: StateFlow<Int?> = _activeViewingStoryIndex.asStateFlow()

    private val _cameraState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val cameraState: StateFlow<CameraUiState> = _cameraState.asStateFlow()

    private val _aiCaptionState = MutableStateFlow<String>("")
    val aiCaptionState: StateFlow<String> = _aiCaptionState.asStateFlow()

    private val _isGeneratingCaption = MutableStateFlow(false)
    val isGeneratingCaption: StateFlow<Boolean> = _isGeneratingCaption.asStateFlow()

    // Active snap/camera configuration
    private val _activeCameraLens = MutableStateFlow<AIFilterResult?>(null)
    val activeCameraLens: StateFlow<AIFilterResult?> = _activeCameraLens.asStateFlow()

    private val _activeViewingSnap = MutableStateFlow<MessageEntity?>(null)
    val activeViewingSnap: StateFlow<MessageEntity?> = _activeViewingSnap.asStateFlow()

    // Temporary user text inputs for feeds
    val newCommentText = MutableStateFlow("")

    init {
        // Seed database if empty inside coroutine scope
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Repository operations exposed securely to UI
    fun likePost(postId: Long, currentLikeStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleLikePost(postId, !currentLikeStatus)
        }
    }

    fun addPost(mediaUrl: String, mediaType: String, caption: String, filterApplied: String) {
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats()
            repository.addPost(
                username = stats.username,
                userAvatarIndex = 0, // 0 is You
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                caption = caption,
                filterApplied = filterApplied
            )
            repository.incrementStreak() // posting keeps streak alive!
        }
    }

    fun addStory(mediaUrl: String) {
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats()
            repository.addStory(
                username = stats.username,
                userAvatarIndex = 0,
                mediaUrl = mediaUrl
            )
        }
    }

    fun sendTextMessage(friendName: String, text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            repository.sendMessage(
                sender = "You",
                receiver = friendName,
                type = "TEXT",
                content = text
            )
        }
    }

    fun sendSnapMessage(friendName: String, snapSpecDescription: String, durationSeconds: Int = 5, filterApplied: String = "None") {
        viewModelScope.launch {
            repository.sendMessage(
                sender = "You",
                receiver = friendName,
                type = "SNAP",
                content = snapSpecDescription,
                duration = durationSeconds,
                filter = filterApplied
            )
        }
    }

    fun clickStory(storyId: Long) {
        viewModelScope.launch {
            repository.markStoryAsViewed(storyId)
        }
    }

    fun setViewingStoryIndex(index: Int?) {
        _activeViewingStoryIndex.value = index
        if (index != null && index >= 0) {
            viewModelScope.launch {
                val currentStories = stories.value
                if (index < currentStories.size) {
                    repository.markStoryAsViewed(currentStories[index].id)
                }
            }
        }
    }

    fun setViewingSnapMessage(message: MessageEntity?) {
        _activeViewingSnap.value = message
        if (message != null) {
            viewModelScope.launch {
                repository.openSnap(message.id)
                repository.incrementSnapScore(2) // open snap score helper
            }
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    // Gemini Generative API Methods
    fun applyAILensPrompt(vibePrompt: String) {
        if (vibePrompt.trim().isEmpty()) return
        _cameraState.value = CameraUiState.LoadingFilter
        viewModelScope.launch {
            try {
                val filterResult = GeminiService.generateCustomFilter(vibePrompt)
                _activeCameraLens.value = filterResult
                _cameraState.value = CameraUiState.FilterLoaded(filterResult)
                repository.incrementSnapScore(10) // reward user for creating custom lenses
            } catch (e: Exception) {
                _cameraState.value = CameraUiState.Error(e.message ?: "Failed loading filter")
            }
        }
    }

    fun clearAILens() {
        _activeCameraLens.value = null
        _cameraState.value = CameraUiState.Idle
    }

    fun generateAICaptionForPost(promptText: String, vibe: String) {
        _isGeneratingCaption.value = true
        viewModelScope.launch {
            try {
                val caption = GeminiService.generateCaption(promptText, vibe)
                _aiCaptionState.value = caption
            } catch (e: Exception) {
                _aiCaptionState.value = "Loving life here on Chattube! 📸✨ #chattube"
            } finally {
                _isGeneratingCaption.value = false
            }
        }
    }

    fun clearAICaptionState() {
        _aiCaptionState.value = ""
    }

    fun updateUserProfile(name: String, bio: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, bio)
        }
    }

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

    fun signup(phone: String, passwordHash: String, username: String, name: String, bio: String) {
        viewModelScope.launch {
            _authError.value = null
            val success = repository.signupUser(phone, passwordHash, username, name, bio)
            if (!success) {
                _authError.value = "Username or phone number already taken."
            }
        }
    }

    fun login(identifier: String, passwordHash: String) {
        viewModelScope.launch {
            _authError.value = null
            val success = repository.loginUser(identifier, passwordHash)
            if (!success) {
                _authError.value = "Invalid credentials."
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
        }
    }
}
