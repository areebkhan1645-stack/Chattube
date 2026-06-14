package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.Response

// Models for Remote API
data class RemoteUserStats(
    val id: String,
    val username: String,
    val coins: Int,
    val snapScore: Int,
    val streakDays: Int,
    val isVip: Boolean
)

data class RemotePost(
    val id: String,
    val username: String,
    val userAvatarIndex: Int,
    val mediaUrl: String,
    val mediaType: String,
    val caption: String,
    val likesCount: Int,
    val isLiked: Boolean,
    val commentsCount: Int,
    val filterApplied: String,
    val rankTag: String
)

data class UploadReelRequest(
    val username: String,
    val mediaUrl: String,
    val caption: String,
    val filterApplied: String
)

data class UploadReelResponse(
    val success: Boolean,
    val newTierUnlocked: String?,
    val coins: Int
)

/**
 * Real Server API Interface for Chattube.
 * This interface defines the endpoints for your actual backend server.
 */
interface ChattubeApiService {

    @GET("api/v1/users/{username}/stats")
    suspend fun getUserStats(@Path("username") username: String): Response<RemoteUserStats>

    @POST("api/v1/users/gamify")
    suspend fun gamifyReelUpload(@Body request: UploadReelRequest): Response<UploadReelResponse>

    @GET("api/v1/feed")
    suspend fun getFeed(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<List<RemotePost>>

    @POST("api/v1/posts")
    suspend fun createPost(@Body post: RemotePost): Response<RemotePost>
    
    @POST("api/v1/posts/{postId}/like")
    suspend fun toggleLike(
        @Path("postId") postId: String,
        @Query("isLiked") isLiked: Boolean
    ): Response<Unit>
}
