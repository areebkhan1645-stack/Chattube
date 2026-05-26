package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CameraUiState
import com.example.ui.viewmodel.ChatTubeViewModel

@Composable
fun CameraScreen(
    viewModel: ChatTubeViewModel,
    onNavigateToFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraState by viewModel.cameraState.collectAsState()
    val aiCaption by viewModel.aiCaptionState.collectAsState()
    val isGeneratingCaption by viewModel.isGeneratingCaption.collectAsState()
    val activeLens by viewModel.activeCameraLens.collectAsState()
    
    // Camera operational states
    var customVibeInput by remember { mutableStateOf("") }
    var zoomValue by remember { mutableFloatStateOf(1.0f) }
    var brightnessValue by remember { mutableFloatStateOf(1.0f) }
    
    // Shutter capture state
    var capturedBitmapVibe by remember { mutableStateOf<String?>(null) } // if not null, user is in captured editing view
    var showSendToFriendDialog by remember { mutableStateOf(false) }
    var selectedDurationSeconds by remember { mutableIntStateOf(5) }
    var customCaptionText by remember { mutableStateOf("") }
    var imageDetailsPrompt by remember { mutableStateOf("") }

    // Friend list for selection
    val mockFriends = listOf("Sarah Travels", "Alex Vlogs", "Jake Skates", "Foodie Vibes")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (capturedBitmapVibe == null) {
                // --- CAMERA VIEWFINDER MODE ---
                
                // Camera View Finder canvas simulation
                val activeLensBrush = if (activeLens != null) {
                    val color1 = Color(android.graphics.Color.parseColor(activeLens!!.hexColorOverlay1))
                    val color2 = Color(android.graphics.Color.parseColor(activeLens!!.hexColorOverlay2))
                    Brush.verticalGradient(listOf(color1, color2))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF14131A), Color(0xFF2E2B3D)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(activeLensBrush)
                        .testTag("camera_viewfinder"),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated zoom text indication
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Camera lens focus",
                            tint = ChatTubeColors.TextPrimary.copy(alpha = 0.35f),
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "Zoom: ${String.format("%.1fx", zoomValue)} | Brightness: ${String.format("%.1fx", brightnessValue)}",
                            color = Color.LightGray.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    // Floating filter sticker top-right
                    if (activeLens != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(24.dp)
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(ChatTubeColors.TextPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeLens!!.stickerDescription,
                                fontSize = 28.sp
                            )
                        }
                        
                        // Active lens description banner bottom
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = 80.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Lens: " + activeLens!!.filterName,
                                color = ChatTubeColors.Yellow,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = activeLens!!.description,
                                color = ChatTubeColors.TextPrimary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Header controls (Close, AI Toggle, Flash)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateToFeed,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ChatTubeColors.TextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.dp, ChatTubeColors.Yellow, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CHATTUBE AI CAM 🪄",
                            color = ChatTubeColors.Yellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearAILens() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Clear lens", tint = ChatTubeColors.TextPrimary)
                    }
                }

                // Sidebar controls (Zoom & Brightness sliders)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { zoomValue = (zoomValue + 0.5f).coerceIn(1.0f, 4.0f) }) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom", tint = ChatTubeColors.TextPrimary)
                    }
                    IconButton(onClick = { zoomValue = 1.0f }) {
                        Text("1x", color = ChatTubeColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { brightnessValue = (brightnessValue + 0.2f).coerceIn(0.5f, 2.0f) }) {
                        Icon(Icons.Default.LightMode, contentDescription = "Level", tint = ChatTubeColors.TextPrimary)
                    }
                    IconButton(onClick = { brightnessValue = 1.0f }) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Cam Rotate", tint = ChatTubeColors.TextPrimary)
                    }
                }

                // Bottom HUD (Custom Gemini AI Lens search, Shutter click, pre-defined filters scroll)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ChatTube Prompt Input for Gemini Custom Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customVibeInput,
                            onValueChange = { customVibeInput = it },
                            placeholder = { Text("Describe visual lens e.g 'galaxy violet', 'cyberpunk'", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ChatTubeColors.TextPrimary,
                                unfocusedTextColor = ChatTubeColors.TextPrimary,
                                focusedContainerColor = Color.Black.copy(alpha = 0.8f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.6f),
                                focusedBorderColor = ChatTubeColors.Yellow,
                                unfocusedBorderColor = Color.Gray
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("ai_lens_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                if (customVibeInput.trim().isNotEmpty()) {
                                    viewModel.applyAILensPrompt(customVibeInput)
                                }
                            },
                            enabled = cameraState !is CameraUiState.LoadingFilter,
                            colors = ButtonDefaults.buttonColors(containerColor = ChatTubeColors.Yellow),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("apply_lens_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (cameraState is CameraUiState.LoadingFilter) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                            } else {
                                Text("AILens", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    // Shutter and Standard bubbles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lens Preset 1
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.applyAILensPrompt("sunset") }
                        ) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFF9E00)))
                            Text("Sunset", color = ChatTubeColors.TextPrimary, fontSize = 9.sp)
                        }

                        // Capture Shutter (Snap button matching Snapchat)
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(5.dp, ChatTubeColors.TextPrimary, CircleShape)
                                .background(ChatTubeColors.TextPrimary.copy(alpha = 0.2f))
                                .padding(6.dp)
                                .clickable {
                                    // Capture simulation! Save parameters.
                                    capturedBitmapVibe = "ACTIVE"
                                    customCaptionText = ""
                                    viewModel.clearAICaptionState()
                                }
                                .testTag("camera_shutter"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(ChatTubeColors.TextPrimary)
                            )
                        }

                        // Lens Preset 2
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.applyAILensPrompt("cyberpunk") }
                        ) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF00F0FF)))
                            Text("Neon", color = ChatTubeColors.TextPrimary, fontSize = 9.sp)
                        }
                    }
                }

            } else {
                // --- PHOTO CAPTURED AND EDIT MODE ---
                
                // Keep background lensed
                val editedLensBrush = if (activeLens != null) {
                    val color1 = Color(android.graphics.Color.parseColor(activeLens!!.hexColorOverlay1))
                    val color2 = Color(android.graphics.Color.parseColor(activeLens!!.hexColorOverlay2))
                    Brush.verticalGradient(listOf(color1, color2))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF14131A), Color(0xFF2E2B3D)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(editedLensBrush)
                        .testTag("captured_edit_canvas"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = "Flares", tint = ChatTubeColors.Yellow, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("SNAP CAPTURED!", color = ChatTubeColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = if (activeLens != null) "Coded Filter: ${activeLens!!.filterName}" else "Camera Raw Lens",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        
                        // Floating active sticker description top right
                        if (activeLens != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(activeLens!!.stickerDescription, fontSize = 54.sp)
                        }
                        
                        // Show AI Generated caption if present
                        if (aiCaption.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = aiCaption,
                                    color = ChatTubeColors.TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Edit Panel (Top side buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { capturedBitmapVibe = null },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Discard Capture", tint = ChatTubeColors.TextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("Captured Snap 📸", color = ChatTubeColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Self destruct timer button (Classic Snapchat!)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                selectedDurationSeconds = when (selectedDurationSeconds) {
                                    3 -> 5
                                    5 -> 8
                                    8 -> 10
                                    else -> 3
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = "Timer", tint = ChatTubeColors.Yellow, modifier = Modifier.size(14.dp))
                        Text("${selectedDurationSeconds}s", color = ChatTubeColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Bottom Editing Deck (User caption prompt for Gemini AI caption, plus Send targets)
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .border(1.dp, ChatTubeColors.BorderDark, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    colors = CardDefaults.cardColors(containerColor = ChatTubeColors.SurfaceDark),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Writers Block", tint = ChatTubeColors.Yellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gemini AI Caption Creator Assist 🤖", color = ChatTubeColors.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Prompt input for AI Caption generator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = imageDetailsPrompt,
                                onValueChange = { imageDetailsPrompt = it },
                                placeholder = { Text("Describe shot e.g 'sipping pink boba at beach'...", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ChatTubeColors.TextPrimary,
                                    unfocusedTextColor = ChatTubeColors.TextPrimary,
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black,
                                    focusedBorderColor = ChatTubeColors.Pink,
                                    unfocusedBorderColor = ChatTubeColors.BorderDark
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("ai_caption_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    val activeVibeDescription = activeLens?.filterName ?: "Default"
                                    viewModel.generateAICaptionForPost(imageDetailsPrompt, activeVibeDescription)
                                },
                                enabled = !isGeneratingCaption,
                                colors = ButtonDefaults.buttonColors(containerColor = ChatTubeColors.Pink),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isGeneratingCaption) {
                                    CircularProgressIndicator(color = ChatTubeColors.TextPrimary, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("Draft", color = ChatTubeColors.TextPrimary, fontSize = 11.sp)
                                }
                            }
                        }

                        // Manual Edit text box to combine or override AI caption
                        OutlinedTextField(
                            value = customCaptionText,
                            onValueChange = { customCaptionText = it },
                            placeholder = { Text("Write custom snap caption or use AI generated draft...", color = Color.Gray, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ChatTubeColors.TextPrimary,
                                unfocusedTextColor = ChatTubeColors.TextPrimary,
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = ChatTubeColors.BorderDark
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("custom_caption_box")
                        )
                        
                        // Action Send Buttons (Post to Feed, Story, or direct Friends)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Stories direct
                            OutlinedButton(
                                onClick = {
                                    val finalCaption = if (customCaptionText.isNotEmpty()) customCaptionText else if (aiCaption.isNotEmpty()) aiCaption else "Living high in Chattube stories! ✨🎥"
                                    viewModel.addStory("gradient_blue")
                                    capturedBitmapVibe = null
                                    onNavigateToFeed()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChatTubeColors.TextPrimary)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add story", modifier = Modifier.size(14.dp))
                                    Text("My Story", fontSize = 11.sp)
                                }
                            }

                            // Post to Feed
                            OutlinedButton(
                                onClick = {
                                    val finalCaption = if (customCaptionText.isNotEmpty()) customCaptionText else if (aiCaption.isNotEmpty()) aiCaption else "Lensed snap! ⚡️"
                                    viewModel.addPost(
                                        mediaUrl = "neon_skateboard",
                                        mediaType = "IMAGE",
                                        caption = finalCaption,
                                        filterApplied = activeLens?.filterName ?: "None"
                                    )
                                    capturedBitmapVibe = null
                                    onNavigateToFeed()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChatTubeColors.TextPrimary)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.DynamicFeed, contentDescription = "Add post", modifier = Modifier.size(14.dp))
                                    Text("Feed Post", fontSize = 11.sp)
                                }
                            }

                            // Send directly to Friends list (Snapchat style)
                            Button(
                                onClick = { showSendToFriendDialog = true },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("send_to_friend_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChatTubeColors.Yellow)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Send To", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Icon(Icons.Default.Send, contentDescription = "Send Direct", tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Snapchat Direct Friend Selector Dialog
            if (showSendToFriendDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { showSendToFriendDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clickable(enabled = false, onClick = {}),
                        colors = CardDefaults.cardColors(containerColor = ChatTubeColors.SurfaceDark),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Select Friend to Snap! 👻🤳", color = ChatTubeColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Your snap will self-destruct in ${selectedDurationSeconds} seconds.", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            mockFriends.forEach { friend ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            val finalCaption = if (customCaptionText.isNotEmpty()) customCaptionText else if (aiCaption.isNotEmpty()) aiCaption else "Capturing moments with ChatTube Cam 📸"
                                            viewModel.sendSnapMessage(
                                                friendName = friend,
                                                snapSpecDescription = finalCaption,
                                                durationSeconds = selectedDurationSeconds,
                                                filterApplied = activeLens?.filterName ?: "None"
                                            )
                                            showSendToFriendDialog = false
                                            capturedBitmapVibe = null
                                            onNavigateToFeed()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        UserAvatar(username = friend, avatarIndex = friend.hashCode(), size = 32.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(friend, color = ChatTubeColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Go", tint = Color.Gray, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
