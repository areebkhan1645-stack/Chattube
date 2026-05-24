package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ChatTubeViewModel
import kotlinx.coroutines.delay

@Composable
fun StoryViewerOverlay(
    viewModel: ChatTubeViewModel,
    modifier: Modifier = Modifier
) {
    val stories by viewModel.stories.collectAsState()
    val activeIdx by viewModel.activeViewingStoryIndex.collectAsState()

    // Render nothing if index is null/out of bounds
    if (activeIdx == null || activeIdx!! < 0 || activeIdx!! >= stories.size) {
        return
    }

    val currentStory = stories[activeIdx!!]
    var storyProgress by remember { mutableFloatStateOf(0.0f) }
    var isTimerActive by remember { mutableStateOf(true) }

    // Tick clock effect for loading indicators
    LaunchedEffect(activeIdx, isTimerActive) {
        if (isTimerActive) {
            storyProgress = 0.0f
            val totalTicks = 80
            val tickTime = (currentStory.durationSeconds * 1000) / totalTicks
            for (i in 0..totalTicks) {
                delay(tickTime.toLong())
                storyProgress = i.toFloat() / totalTicks
            }
            // Auto advance
            if (activeIdx!! + 1 < stories.size) {
                viewModel.setViewingStoryIndex(activeIdx!! + 1)
            } else {
                viewModel.setViewingStoryIndex(-1) // close
            }
        }
    }

    // Story brush layout
    val gradientBrush = when (currentStory.mediaUrl) {
        "gradient_blue" -> Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF0F0E17)))
        "gradient_rose" -> Brush.verticalGradient(listOf(Color(0xFFFF2A6D), Color(0xFF9100FF), Color(0xFF0F0E17)))
        "gradient_amber" -> Brush.verticalGradient(listOf(Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFF0F0E17)))
        else -> Brush.verticalGradient(listOf(Color(0xFF833AB4), Color(0xFFC13584), Color(0xFF0F0E17)))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("story_viewer_canvas")
    ) {
        // High fidelity background canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Loading Tick Row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Progress Horizontal Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.25f)),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        stories.forEachIndexed { idx, s ->
                            val itemProgress = when {
                                idx < activeIdx!! -> 1.0f
                                idx == activeIdx!! -> storyProgress
                                else -> 0.0f
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (idx <= activeIdx!!) Color.White else Color.White.copy(alpha = 0.3f),
                                        RoundedCornerShape(2.dp)
                                    )
                                    .fillMaxWidth(itemProgress)
                                    .background(ChatTubeColors.Yellow, RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    // Avatar details + Close triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            UserAvatar(username = currentStory.username, avatarIndex = currentStory.userAvatarIndex, size = 36.dp)
                            Column {
                                Text(currentStory.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Schedule, contentDescription = "Active time", tint = Color.LightGray, modifier = Modifier.size(10.dp))
                                    Text("Lensed Story • Auto-playing", color = Color.LightGray, fontSize = 9.sp)
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.setViewingStoryIndex(-1) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close story", tint = Color.White)
                        }
                    }
                }

                // Central Focus Card representing custom camera snaps in stories
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        currentStory.username.uppercase() + " SNAP!",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "🎬 Daily Vlog Vibe",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Check out this amazing filter effect on Chattube!",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }

                // Interactive skip Zones left and right (Snapchat/Instagram layout mechanism!)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Previous skip zone
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .clickable {
                                    if (activeIdx!! - 1 >= 0) {
                                        viewModel.setViewingStoryIndex(activeIdx!! - 1)
                                    } else {
                                        viewModel.setViewingStoryIndex(-1) // close
                                    }
                                }
                        )
                        // Next skip zone
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .clickable {
                                    if (activeIdx!! + 1 < stories.size) {
                                        viewModel.setViewingStoryIndex(activeIdx!! + 1)
                                    } else {
                                        viewModel.setViewingStoryIndex(-1) // close
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
