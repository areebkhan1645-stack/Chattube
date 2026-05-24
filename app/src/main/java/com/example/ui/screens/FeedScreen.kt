package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PostEntity
import com.example.data.local.StoryEntity
import com.example.ui.viewmodel.ChatTubeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    viewModel: ChatTubeViewModel,
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Segmented tab state for Feed vs Vertical Reels
    var selectedFeedType by remember { mutableStateOf("Feed") }
    var activeReelIndex by remember { mutableIntStateOf(0) }
    
    // Comments overlay state
    var activeCommentsPostId by remember { mutableStateOf<Long?>(null) }
    var commentText by remember { mutableStateOf("") }
    val mockCommentsList = remember {
        mutableStateMapOf<Long, List<Pair<String, String>>>(
            1L to listOf("sarah_travels" to "OMGG this café is spectacular! 😍", "alex_vlogs" to "Need to go here ASAP!", "cyber_pro" to "Love the golden filter!!"),
            2L to listOf("sam_skaters" to "Sick flip dude 🛹✨", "jake_skate" to "Cleanest land of the day!", "sarah_travels" to "the neon trail on the wheels is awesome"),
            3L to listOf("kim_chi" to "Looks like Blade Runner! ⛈️", "alex_vlogs" to "Classic analog cam aesthetics.")
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChatTubeColors.DarkBackground,
        topBar = {
            Column(modifier = Modifier.background(ChatTubeColors.DarkBackground)) {
                GlassmorphicHeader(
                    title = "TUBE & INTERACTION",
                    subtitle = "ChatTube",
                    trailingContent = {
                        IconButton(
                            onClick = onNavigateToCamera,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Brush.linearGradient(ChatTubeColors.Tubegradient))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Quick Snap",
                                tint = Color.White
                            )
                        }
                    }
                )

                // Premium Selector: Feed vs Reels Mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val activeBrush = Brush.linearGradient(ChatTubeColors.Tubegradient)
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ChatTubeColors.SurfaceDark)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedFeedType == "Feed") activeBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                                .clickable { selectedFeedType = "Feed" }
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        ) {
                            Text("Feed 📸", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedFeedType == "Reels") activeBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                                .clickable { selectedFeedType = "Reels" }
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        ) {
                            Text("Reels 🎬", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedFeedType == "Feed") {
                // Classic scrollable feed
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("feed_scroll_posts"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Stories Horizontal Section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Daily Stories",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                            
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(stories) { idx, story ->
                                    StoryCircleItem(
                                        story = story,
                                        onClick = {
                                            viewModel.setViewingStoryIndex(idx)
                                        }
                                    )
                                }
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                                color = ChatTubeColors.BorderDark
                            )
                        }
                    }

                    // Feed List
                    if (posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = ChatTubeColors.Pink)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Building your custom dynamic feed...", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        items(posts, key = { it.id }) { post ->
                            PostItemCard(
                                post = post,
                                onLikeToggle = { viewModel.likePost(post.id, post.isLiked) },
                                onCommentClick = { activeCommentsPostId = post.id }
                            )
                        }
                    }
                }
            } else {
                // HIGH-FIDELITY REELS VERTICAL INFINITE LOOP VIEW
                val reelsPosts = posts.filter { it.mediaType == "TUBE" || it.mediaType == "VIDEO" }
                if (reelsPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active short videos. Head to Camera and upload some Tubes!", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    val currentReel = reelsPosts[activeReelIndex % reelsPosts.size]
                    val reelBrush = when(currentReel.filterApplied) {
                        "Golden Hour" -> Brush.verticalGradient(listOf(Color(0xFFFFAA00), Color(0xFF1D1B26)))
                        "Neon Overdrive" -> Brush.verticalGradient(listOf(Color(0xFFFF0055), Color(0xFF7A00FF)))
                        "1995 Nostalgia" -> Brush.verticalGradient(listOf(Color(0xFF8B5A2B), Color(0xFF110033)))
                        else -> Brush.verticalGradient(listOf(ChatTubeColors.Purple, ChatTubeColors.DarkBackground))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(reelBrush)
                            .testTag("reels_scroller_panel")
                    ) {
                        // Substantial pulsing recording marker
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Text("LIVE TUBE REEL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                        }

                        // Central visuals feedback
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicVideo,
                                contentDescription = "Visual Sync",
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Rendering Ambient Soundscape...",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        // Right-Hand Floating Reels controls
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // User Profile Avatar Circle
                            UserAvatar(username = currentReel.username, avatarIndex = currentReel.userAvatarIndex, size = 42.dp)

                            // Heart React
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { viewModel.likePost(currentReel.id, currentReel.isLiked) },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (currentReel.isLiked) ChatTubeColors.Pink else Color.Black.copy(alpha = 0.4f))
                                ) {
                                    Icon(
                                        imageVector = if (currentReel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like Reel",
                                        tint = Color.White
                                    )
                                }
                                Text("${currentReel.likesCount}k", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Comments trigger
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { activeCommentsPostId = currentReel.id },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.4f))
                                ) {
                                    Icon(Icons.Default.ModeComment, contentDescription = "Comments", tint = Color.White)
                                }
                                Text("${currentReel.commentsCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Easy next reel navigation (infinite looping)
                            IconButton(
                                onClick = { activeReelIndex++ },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ChatTubeColors.Yellow)
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Next Reel", tint = Color.Black)
                            }
                            Text("Next Reel", color = ChatTubeColors.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        // Bottom descriptive text layout
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth(0.8f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "@${currentReel.username}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = currentReel.caption,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = "Track", tint = ChatTubeColors.Yellow, modifier = Modifier.size(12.dp))
                                Text("Original Audio Soundboard - ${currentReel.username}", color = ChatTubeColors.Yellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // High-fidelity Floating Comments Sheet (Instagram/Snapchat style overlay)
            if (activeCommentsPostId != null) {
                val currentPostId = activeCommentsPostId!!
                val comments = mockCommentsList[currentPostId] ?: emptyList()
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { activeCommentsPostId = null },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.65f)
                            .clickable(enabled = false, onClick = {}) // block dismissal clicks
                            .border(1.dp, ChatTubeColors.BorderDark, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                        colors = CardDefaults.cardColors(containerColor = ChatTubeColors.SurfaceDark),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                                .imePadding()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Comments (${comments.size})",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { activeCommentsPostId = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                                }
                            }
                            
                            HorizontalDivider(color = ChatTubeColors.BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                            
                            // Comments lazy column
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(comments) { comment ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        UserAvatar(username = comment.first, avatarIndex = comment.first.hashCode(), size = 32.dp)
                                        Column {
                                            Text(
                                                text = comment.first,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = comment.second,
                                                color = Color.LightGray,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // New comment bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    placeholder = { Text("Add comment info...", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.Black,
                                        unfocusedContainerColor = Color.Black,
                                        focusedBorderColor = ChatTubeColors.Pink,
                                        unfocusedBorderColor = ChatTubeColors.BorderDark
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                IconButton(
                                    onClick = {
                                        if (commentText.trim().isNotEmpty()) {
                                            val updated = comments + ("You" to commentText)
                                            mockCommentsList[currentPostId] = updated
                                            commentText = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(ChatTubeColors.Pink)
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Post Comment", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryCircleItem(
    story: StoryEntity,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("story_item_${story.username}")
    ) {
        UserAvatar(
            username = story.username,
            avatarIndex = story.userAvatarIndex,
            size = 64.dp,
            hasStory = true,
            storyViewed = story.isViewed
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = story.username,
            color = if (story.isViewed) Color.Gray else Color.White,
            fontSize = 11.sp,
            fontWeight = if (story.isViewed) FontWeight.Normal else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(68.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItemCard(
    post: PostEntity,
    onLikeToggle: () -> Unit,
    onCommentClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Playback state of custom TUBE video simulation
    var isPlaying by remember { mutableStateOf(true) }
    var tubePlaybackProgress by remember { mutableFloatStateOf(0.0f) }
    
    // Double tap heart float-in animation
    var showDoubleTapLikeHeart by remember { mutableStateOf(false) }
    
    // Timer update for Tube progress
    if (post.mediaType == "TUBE" && isPlaying) {
        LaunchedEffect(isPlaying) {
            while (isPlaying) {
                delay(60)
                tubePlaybackProgress = (tubePlaybackProgress + 0.012f)
                if (tubePlaybackProgress >= 1.0f) {
                    tubePlaybackProgress = 0.0f
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .border(1.dp, ChatTubeColors.BorderDark, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = ChatTubeColors.SurfaceDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Header Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(username = post.username, avatarIndex = post.userAvatarIndex, size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.username,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (post.mediaType == "TUBE") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Brush.linearGradient(ChatTubeColors.Tubegradient))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Tube", tint = Color.White, modifier = Modifier.size(10.dp))
                                    Text("TUBE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Text(
                        text = if (post.filterApplied != "None") "Lensed with ${post.filterApplied} 🪄" else "Original Shot ✨",
                        color = ChatTubeColors.WhiteTranslucent.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More details", tint = Color.LightGray)
                }
            }

            // Central Media Canvas (With custom overlays and filters simulation based on metadata)
            val canvasColorBrush = when (post.mediaUrl) {
                "neon_cafe" -> Brush.radialGradient(listOf(Color(0xFFFF00CC), Color(0xFF110055)))
                "neon_skateboard" -> Brush.sweepGradient(listOf(Color(0xFF00FFCC), Color(0xFF7A00FF), Color(0xFF00FFCC)))
                "cyberpunk_rain" -> Brush.verticalGradient(listOf(Color(0xFF0D0B18), Color(0xFF225ED2), Color(0xFFFF0055)))
                else -> Brush.radialGradient(listOf(Color(0xFFE1306C), Color(0xFF2E2B3D)))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(canvasColorBrush)
                    .combinedClickable(
                        onDoubleClick = {
                            if (!post.isLiked) {
                                onLikeToggle()
                            }
                            showDoubleTapLikeHeart = true
                            scope.launch {
                                delay(800)
                                showDoubleTapLikeHeart = false
                            }
                        },
                        onClick = {
                            if (post.mediaType == "TUBE") {
                                isPlaying = !isPlaying
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Apply specific static filter overlays matching Snapchat/Insta mix
                when (post.filterApplied) {
                    "Golden Hour" -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x35FFAA00))
                    )
                    "Neon Overdrive" -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0x28FF00FF), Color(0x2800FFFF))))
                    )
                    "1995 Nostalgia" -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x25D4A373))
                    )
                }

                // Render Graphic details inside media block
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(20.dp)
                ) {
                    if (post.mediaType == "TUBE") {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PlayCircleFilled else Icons.Filled.PauseCircle,
                            contentDescription = "Playing",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPlaying) "Playing Tube Short... 🔊" else "Tube Short Paused 🔇",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            text = "Original Audio - sam_skaters Remix 🎧",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = "Snap Shot",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Snap Shot 📸",
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Top sticker lens labels
                if (post.filterApplied != "None") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when(post.filterApplied) {
                                "Golden Hour" -> "🌅 Golden Lensed"
                                "Neon Overdrive" -> "⚡️ Neon Lensed"
                                "1995 Nostalgia" -> "📼 Retro Lensed"
                                else -> "🪄 AI Lensed"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Auto playing dynamic timeline bar for video tube matching snapchat story progress
                if (post.mediaType == "TUBE") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(tubePlaybackProgress)
                                .background(Brush.linearGradient(ChatTubeColors.Tubegradient))
                        )
                    }
                }

                // Animated Heart Popup on Double-Tap! High level visual delight!
                androidx.compose.animation.AnimatedVisibility(
                    visible = showDoubleTapLikeHeart,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Liked!",
                            tint = Color(0xFFFF0D55),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
            }

            // Bottom Actions Panel (Instagram styling with high response states)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heart Icon Button with scale motion
                val scale by animateFloatAsState(
                    targetValue = if (post.isLiked) 1.2f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
                )

                IconButton(
                    onClick = onLikeToggle,
                    modifier = Modifier.scale(scale)
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like Post",
                        tint = if (post.isLiked) Color(0xFFFF0D55) else Color.White
                    )
                }

                IconButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Direct share",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = Color.White
                    )
                }
            }

            // Caption Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
            ) {
                Text(
                    text = "${post.likesCount} liking snaps & tubes",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = post.username,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = post.caption,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (post.commentsCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View all ${post.commentsCount} comments...",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onCommentClick() }
                    )
                }
            }
        }
    }
}
