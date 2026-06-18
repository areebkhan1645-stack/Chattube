package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.example.data.local.NotificationEntity
import com.example.data.local.PostEntity
import com.example.data.local.UserStatsEntity
import com.example.data.local.StoryEntity
import com.example.ui.viewmodel.ChatTubeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: ChatTubeViewModel,
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val scope = rememberCoroutineScope()
    var showUploadReelBottomSheet by remember { mutableStateOf(false) }
    
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

    val snackbarHostState = remember { SnackbarHostState() }
    val uploadError by viewModel.uploadError.collectAsState()

    var showNotificationsBottomSheet by remember { mutableStateOf(false) }
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }
    val hasNewNotification = unreadCount > 0

    LaunchedEffect(uploadError) {
        if (uploadError != null) {
            snackbarHostState.showSnackbar(uploadError!!)
            viewModel.clearUploadError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChatTubeColors.DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(ChatTubeColors.DarkBackground)) {
                GlassmorphicHeader(
                    title = "TUBE & INTERACTION",
                    subtitle = "ChatTube",
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseScale"
                            )

                            IconButton(
                                onClick = { 
                                    showNotificationsBottomSheet = true 
                                    viewModel.markNotificationsRead()
                                }
                            ) {
                                Box {
                                    Icon(
                                        imageVector = if (hasNewNotification) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Notifications",
                                        tint = if (hasNewNotification) Color.Red else ChatTubeColors.TextPrimary,
                                        modifier = Modifier.scale(if (hasNewNotification) pulseScale else 1f)
                                    )
                                    if (unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(ChatTubeColors.Yellow)
                                                .align(Alignment.TopEnd),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                                color = Color.Black,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { showUploadReelBottomSheet = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ChatTubeColors.SurfaceDark)
                                    .border(1.dp, ChatTubeColors.BorderDark, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Upload Reel",
                                    tint = ChatTubeColors.Pink
                                )
                            }
                            IconButton(
                                onClick = onNavigateToCamera,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(ChatTubeColors.Tubegradient))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Quick Snap",
                                    tint = ChatTubeColors.TextPrimary
                                )
                            }
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
                            Text("Feed 📸", color = ChatTubeColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedFeedType == "Reels") activeBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                                .clickable { selectedFeedType = "Reels" }
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        ) {
                            Text("Reels 🎬", color = ChatTubeColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Daily Stories",
                                    color = ChatTubeColors.TextPrimary.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                TextButton(onClick = { viewModel.loadAdStory() }) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Earn Coins",
                                        tint = ChatTubeColors.Pink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Earn Money (Load Ad)", color = ChatTubeColors.Pink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
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
                                currentUsername = userStats?.username,
                                currentUserProfilePicUri = userStats?.profilePicUri,
                                onLikeToggle = { viewModel.likePost(post.id, post.isLiked, post.username) },
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
                            Text("LIVE TUBE REEL", color = ChatTubeColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                        }

                        // Central visuals feedback
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicVideo,
                                contentDescription = "Visual Sync",
                                tint = ChatTubeColors.TextPrimary.copy(alpha = 0.3f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Rendering Ambient Soundscape...",
                                color = ChatTubeColors.TextPrimary.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        // Right-Hand Floating Reels controls
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // User Profile Avatar Circle
                            val reelProfilePic = if (currentReel.username == userStats?.username) userStats?.profilePicUri else null
                            UserAvatar(username = currentReel.username, avatarIndex = currentReel.userAvatarIndex, size = 42.dp, profilePicUri = reelProfilePic)

                            // Heart React
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { viewModel.likePost(currentReel.id, currentReel.isLiked, currentReel.username) },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (currentReel.isLiked) ChatTubeColors.Pink else Color.Black.copy(alpha = 0.4f))
                                ) {
                                    Icon(
                                        imageVector = if (currentReel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like Reel",
                                        tint = ChatTubeColors.TextPrimary
                                    )
                                }
                                Text("${currentReel.likesCount}k", color = ChatTubeColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Comments trigger
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { activeCommentsPostId = currentReel.id },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.4f))
                                ) {
                                    Icon(Icons.Default.ModeComment, contentDescription = "Comments", tint = ChatTubeColors.TextPrimary)
                                }
                                Text("${currentReel.commentsCount}", color = ChatTubeColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Share
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { /* Implement generic share later */ },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.4f))
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = ChatTubeColors.TextPrimary)
                                }
                                Text("Share", color = ChatTubeColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Easy next reel navigation (infinite looping)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { activeReelIndex++ },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(ChatTubeColors.Yellow)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Next Reel", tint = Color.Black)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Next Reel", color = ChatTubeColors.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Bottom descriptive text layout
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth(0.8f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${currentReel.username}",
                                    color = ChatTubeColors.TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                RankBadge(rank = currentReel.rankTag, modifier = Modifier.padding(start = 6.dp))
                            }
                            Text(
                                text = currentReel.caption,
                                color = ChatTubeColors.TextPrimary.copy(alpha = 0.9f),
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
                                    color = ChatTubeColors.TextPrimary,
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = comment.first,
                                                    color = ChatTubeColors.TextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                val rank = if (comment.first == userStats?.username) {
                                                    val coins = userStats?.coins ?: 0
                                                    when {
                                                        coins >= 1600 -> "Pro Max VIP"
                                                        coins >= 800 -> "Pro VIP"
                                                        coins >= 400 -> "VIP"
                                                        else -> "None"
                                                    }
                                                } else "None"
                                                RankBadge(rank = rank, modifier = Modifier.padding(start = 6.dp))
                                            }
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
                                        focusedTextColor = ChatTubeColors.TextPrimary,
                                        unfocusedTextColor = ChatTubeColors.TextPrimary,
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
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Post Comment", tint = ChatTubeColors.TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Bottom Sheet for Upload Reel
    if (showUploadReelBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUploadReelBottomSheet = false },
            containerColor = ChatTubeColors.SurfaceDark
        ) {
            var caption by remember { mutableStateOf("") }
            var videoSelected by remember { mutableStateOf(false) }
            val context = LocalContext.current
            
            // Check for media permissions before letting them select
            val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_VIDEO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            
            var hasMediaPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(context, mediaPermission) == PackageManager.PERMISSION_GRANTED
                )
            }
            
            val mediaPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    videoSelected = true
                }
            }
            
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                hasMediaPermission = granted
                if (granted) {
                    mediaPickerLauncher.launch("video/*") // auto launch after granted
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Upload a New Reel", color = ChatTubeColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)

                if (!hasMediaPermission) {
                    // Show a designated permission button as requested
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            permissionLauncher.launch(mediaPermission)
                        },
                        colors = CardDefaults.cardColors(containerColor = ChatTubeColors.DarkBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "Folder", tint = ChatTubeColors.Yellow, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Storage Permission Required", color = ChatTubeColors.TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Tap to Grant Permission", color = ChatTubeColors.TextSecondary, fontSize = 12.sp)
                        }
                    }
                } else {
                    // Placeholder for Video Selection Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChatTubeColors.DarkBackground)
                            .clickable { mediaPickerLauncher.launch("video/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (videoSelected) {
                            Text("🎥 video_01.mp4 selected", color = ChatTubeColors.Pink, fontWeight = FontWeight.Bold)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = ChatTubeColors.TextSecondary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Select Video from Gallery", color = ChatTubeColors.TextSecondary, fontSize = 14.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("Write a catchy description...", color = ChatTubeColors.TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ChatTubeColors.TextPrimary,
                        unfocusedTextColor = ChatTubeColors.TextPrimary,
                        focusedBorderColor = ChatTubeColors.Pink,
                        unfocusedBorderColor = ChatTubeColors.BorderDark,
                        cursorColor = ChatTubeColors.Pink
                    ),
                    maxLines = 3
                )
                
                LiquidGlassButton(
                    onClick = {
                        if (videoSelected) {
                            viewModel.uploadReel(
                                mediaUrl = "sample_reel",
                                caption = caption,
                                filterApplied = "Normal"
                            )
                            showUploadReelBottomSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = videoSelected && caption.isNotEmpty()
                ) {
                    Text("Publish Reel", color = ChatTubeColors.SurfaceDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showNotificationsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsBottomSheet = false },
            containerColor = ChatTubeColors.SurfaceDark,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            val df = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Notifications", color = ChatTubeColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(notifications) { notif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = ChatTubeColors.DarkBackground)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (notif.type) {
                                    "like" -> Icons.Default.Favorite to Color.Red
                                    "request" -> Icons.Default.PersonAdd to ChatTubeColors.Yellow
                                    "story_react" -> Icons.Default.EmojiEmotions to ChatTubeColors.Pink
                                    else -> Icons.Default.Notifications to Color.White
                                }
                                Icon(icon.first, contentDescription = null, tint = icon.second, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when(notif.type) {
                                            "like" -> "@${notif.senderId} liked your post/reel."
                                            "request" -> "@${notif.senderId} sent you a friend request."
                                            "story_react" -> "@${notif.senderId} reacted to your story."
                                            else -> "New notification from @${notif.senderId}"
                                        },
                                        color = ChatTubeColors.TextPrimary,
                                        fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = df.format(java.util.Date(notif.createdAt)),
                                        color = ChatTubeColors.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    if (notifications.isEmpty()) {
                        item {
                            Text("No new notifications.", color = ChatTubeColors.TextSecondary, modifier = Modifier.padding(16.dp))
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
            color = if (story.isViewed) Color.Gray else ChatTubeColors.TextPrimary,
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
    currentUsername: String?,
    currentUserProfilePicUri: String?,
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
                val postProfilePic = if (post.username == currentUsername) currentUserProfilePicUri else null
                UserAvatar(username = post.username, avatarIndex = post.userAvatarIndex, size = 38.dp, profilePicUri = postProfilePic)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.username,
                            color = ChatTubeColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        RankBadge(rank = post.rankTag, modifier = Modifier.padding(start = 6.dp))
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
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Tube", tint = ChatTubeColors.TextPrimary, modifier = Modifier.size(10.dp))
                                    Text("TUBE", color = ChatTubeColors.TextPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
                            tint = ChatTubeColors.TextPrimary.copy(alpha = 0.85f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPlaying) "Playing Tube Short... 🔊" else "Tube Short Paused 🔇",
                            color = ChatTubeColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            text = "Original Audio - sam_skaters Remix 🎧",
                            color = ChatTubeColors.TextPrimary.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = "Snap Shot",
                            tint = ChatTubeColors.TextPrimary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Snap Shot 📸",
                            color = ChatTubeColors.TextPrimary.copy(alpha = 0.9f),
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
                            color = ChatTubeColors.TextPrimary,
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
                            .background(ChatTubeColors.TextPrimary.copy(alpha = 0.2f))
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
                            .background(ChatTubeColors.TextPrimary.copy(alpha = 0.2f)),
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
                        tint = if (post.isLiked) Color(0xFFFF0D55) else ChatTubeColors.TextPrimary
                    )
                }

                IconButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = ChatTubeColors.TextPrimary
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Direct share",
                        tint = ChatTubeColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = ChatTubeColors.TextPrimary
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
                    color = ChatTubeColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = post.username,
                        color = ChatTubeColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    RankBadge(rank = post.rankTag, modifier = Modifier.padding(end = 6.dp))
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
