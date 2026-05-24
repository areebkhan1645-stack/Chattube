package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ChatTubeViewModel

@Composable
fun ProfileScreen(
    viewModel: ChatTubeViewModel,
    modifier: Modifier = Modifier
) {
    val userStats by viewModel.userStats.collectAsState()
    val posts by viewModel.posts.collectAsState()
    
    // Edit profile modal states
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf("") }
    var editBioInput by remember { mutableStateOf("") }

    // Filter our posts vs other accounts
    val myPosts = posts.filter { it.username == (userStats?.username ?: "chattuber_pro") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChatTubeColors.DarkBackground,
        topBar = {
            GlassmorphicHeader(
                title = "MY STUDIO",
                subtitle = "Creator Lab",
                trailingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (userStats != null) {
                                    editNameInput = userStats!!.name
                                    editBioInput = userStats!!.bio
                                    showEditProfileDialog = true
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Edit Bio", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                viewModel.logout()
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = 0.15f))
                                .testTag("logout_button")
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Exit Studio", tint = Color.Red)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("profile_content_view")
            ) {
                // Profile Avatar & Streak Stats Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        username = userStats?.username ?: "You",
                        avatarIndex = 0,
                        size = 76.dp,
                        hasStory = true,
                        storyViewed = false
                    )
                    
                    Spacer(modifier = Modifier.width(18.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userStats?.name ?: "ChatTuber Premium",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "@" + (userStats?.username ?: "chattuber_pro"),
                            color = ChatTubeColors.Yellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = Color(0xFFFF5500), modifier = Modifier.size(16.dp))
                            Text(
                                text = "${userStats?.tubeStreak ?: 0} Day Tube Streak! 🔥",
                                color = Color(0xFFFFCC00),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Snap score & Streaks high contrast badging tray
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Snap score card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, ChatTubeColors.BorderDark, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = ChatTubeColors.SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SNAP SCORE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${userStats?.snapScore ?: 0}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("Earned by lens activity 👻", color = Color.Gray, fontSize = 8.sp)
                        }
                    }

                    // Tubes checked card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, ChatTubeColors.BorderDark, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = ChatTubeColors.SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("MY CREATIVE TUBE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${myPosts.size} Posts",
                                color = ChatTubeColors.Pink,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("Total published shots 🎬", color = Color.Gray, fontSize = 8.sp)
                        }
                    }
                }

                // Streak keep-alive button (Classic Snapchat engagement loop)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clickable { viewModel.addPost("neon_skateboard", "TUBE", "Keeping the Chattube speed run active! 🔥⚡️ #streak", "Neon Overdrive") }
                        .border(1.dp, Color(0xFFFF5500).copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0x11FF5500)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5500)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = "Active Speed", tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Keep Streak Alive! 🔥", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Publish a rapid streak Tube Post to gain 10 Snap points!", color = Color.LightGray, fontSize = 10.sp)
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Go", tint = Color.Gray)
                    }
                }

                // Biography Info
                Text(
                    text = "BIOGRAPHY",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
                Text(
                    text = userStats?.bio ?: "No bio added yet. Tell people about your visual aesthetics in Chattube Studio!",
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Grid of My Published creations (Instagram profile style grid!)
                Text(
                    text = "MY CHATTUBE GRID (${myPosts.size})",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                
                if (myPosts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Library empty", tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your studio is empty! 📭", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Take a snap with dynamic lenses in the camera room and send it here!", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(myPosts) { post ->
                            // Custom colorful thumbnail matching aesthetic
                            val postBrush = when(post.filterApplied) {
                                "Golden Hour" -> Brush.verticalGradient(listOf(Color(0xFFFFAA00), Color(0xFF1D1B26)))
                                "Neon Overdrive" -> Brush.verticalGradient(listOf(Color(0xFFFF00FF), Color(0xFF00FFFF)))
                                "1995 Nostalgia" -> Brush.verticalGradient(listOf(Color(0xFFD4A373), Color(0xFF1D1B26)))
                                else -> Brush.radialGradient(listOf(Color(0xFF833AB4), Color(0xFF0D0B18)))
                            }

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1.0f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(postBrush)
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (post.mediaType == "TUBE") Icons.Default.PlayArrow else Icons.Default.CameraAlt,
                                        contentDescription = "Post preview",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (post.filterApplied != "None") post.filterApplied else "Original",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Edit Profile Modal Overlay
            if (showEditProfileDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { showEditProfileDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clickable(enabled = false, onClick = {}),
                        colors = CardDefaults.cardColors(containerColor = ChatTubeColors.SurfaceDark),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ChatTubeColors.BorderDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Edit Profile Studio 🎨🎛️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            
                            Column {
                                Text("CREATOR NAME", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = editNameInput,
                                    onValueChange = { editNameInput = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = ChatTubeColors.Yellow,
                                        unfocusedBorderColor = ChatTubeColors.BorderDark
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column {
                                Text("CREATOR BIO / BYLINE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = editBioInput,
                                    onValueChange = { editBioInput = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = ChatTubeColors.Yellow,
                                        unfocusedBorderColor = ChatTubeColors.BorderDark
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showEditProfileDialog = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("Discard")
                                }

                                Button(
                                    onClick = {
                                        viewModel.updateUserProfile(editNameInput, editBioInput)
                                        showEditProfileDialog = false
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChatTubeColors.Yellow)
                                ) {
                                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
