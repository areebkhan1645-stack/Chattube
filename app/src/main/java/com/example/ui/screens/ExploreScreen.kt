package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.data.local.PostEntity
import com.example.ui.viewmodel.ChatTubeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ChatTubeViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    var searchInputText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var previewingPost by remember { mutableStateOf<PostEntity?>(null) }

    val categories = listOf("All", "Trending", "Lenses", "Seoul Cafe", "Skateboards", "Vlogs")

    // Filter dynamic content based on search and category choice
    val filteredPosts = posts.filter { post ->
        val matchesCategory = when (selectedCategory) {
            "All" -> true
            "Lenses" -> post.filterApplied != "None"
            "Seoul Cafe" -> post.caption.contains("Seoul", ignoreCase = true) || post.caption.contains("cafe", ignoreCase = true)
            "Skateboards" -> post.caption.contains("skates", ignoreCase = true) || post.caption.contains("skateboard", ignoreCase = true) || post.caption.contains("tre-flip", ignoreCase = true)
            "Trending" -> post.likesCount > 1000
            "Vlogs" -> post.mediaType == "TUBE"
            else -> true
        }

        val matchesSearch = if (searchInputText.trim().isEmpty()) {
            true
        } else {
            post.caption.contains(searchInputText, ignoreCase = true) ||
            post.username.contains(searchInputText, ignoreCase = true) ||
            post.filterApplied.contains(searchInputText, ignoreCase = true)
        }

        matchesCategory && matchesSearch
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChatTubeColors.DarkBackground,
        topBar = {
            Column(modifier = Modifier.background(ChatTubeColors.DarkBackground)) {
                GlassmorphicHeader(
                    title = "EXPLORE TUBE",
                    subtitle = "Discover Reels",
                    trailingContent = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.TrendingUp, contentDescription = "Trending Activity", tint = ChatTubeColors.Pink)
                        }
                    }
                )

                // Search Bar Box
                OutlinedTextField(
                    value = searchInputText,
                    onValueChange = { searchInputText = it },
                    placeholder = { Text("Search tags, creators, lenses...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = Color.LightGray) },
                    trailingIcon = {
                        if (searchInputText.isNotEmpty()) {
                            IconButton(onClick = { searchInputText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.LightGray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = ChatTubeColors.SurfaceDark,
                        unfocusedContainerColor = ChatTubeColors.SurfaceDark,
                        focusedBorderColor = ChatTubeColors.Pink,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("explore_search_input"),
                    shape = RoundedCornerShape(20.dp)
                )

                // Category badges row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        val bgBrush = if (isSelected) {
                            Brush.linearGradient(ChatTubeColors.Instagradient)
                        } else {
                            Brush.linearGradient(listOf(ChatTubeColors.SurfaceDark, ChatTubeColors.SurfaceDark))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgBrush)
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredPosts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Search, contentDescription = "No results", tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No visual matches found", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Try exploring another tag category or lens vibe!", color = Color.Gray, fontSize = 11.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .testTag("explore_grid"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPosts) { post ->
                        val itemBrush = when(post.filterApplied) {
                            "Golden Hour" -> Brush.verticalGradient(listOf(Color(0xFFFF8B00), Color(0xFF1D1B26)))
                            "Neon Overdrive" -> Brush.verticalGradient(listOf(Color(0xFFFF0055), Color(0xFF00FFFF)))
                            "1995 Nostalgia" -> Brush.verticalGradient(listOf(Color(0xFF85582A), Color(0xFF1D1B26)))
                            else -> Brush.radialGradient(listOf(Color(0xFF331B4D), Color(0xFF0D0B18)))
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(0.8f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(itemBrush)
                                .border(1.dp, ChatTubeColors.BorderDark, RoundedCornerShape(16.dp))
                                .clickable { previewingPost = post },
                            contentAlignment = Alignment.BottomStart
                        ) {
                            // Video marker icon or sticker
                            if (post.mediaType == "TUBE") {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎬", fontSize = 11.sp)
                                }
                            }

                            // Caption snippet overlay
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                        )
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "@" + post.username,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = post.caption,
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Quick High-Fidelity Reel / Post Preview Overlay Card
            if (previewingPost != null) {
                val post = previewingPost!!
                val overlayBrush = when(post.filterApplied) {
                    "Golden Hour" -> Brush.verticalGradient(listOf(Color(0xFFFFAA00), Color(0xFF0F0E17)))
                    "Neon Overdrive" -> Brush.verticalGradient(listOf(Color(0xFFFF2A6D), Color(0xFF9100FF)))
                    "1995 Nostalgia" -> Brush.verticalGradient(listOf(Color(0xFFD4A373), Color(0xFF0F0E17)))
                    else -> Brush.verticalGradient(listOf(Color(0xFF833AB4), Color(0xFFC13584)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable { previewingPost = null }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(520.dp)
                            .clickable(enabled = false) {},
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, ChatTubeColors.Pink)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(overlayBrush)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Creator Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    UserAvatar(username = post.username, avatarIndex = post.userAvatarIndex, size = 36.dp)
                                    Column {
                                        Text(post.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Reel view • Applied ${post.filterApplied}", color = Color.LightGray, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { previewingPost = null },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close preview", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Central Graphics
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (post.mediaType == "TUBE") "🎬 ACTIVE VIDEO REEL 🎬" else "📸 HIGH FIDELITY SNAP 📸",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // Aesthetic visual simulation block
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(60.dp))
                                                .background(Color.White.copy(alpha = 0.1f))
                                                .border(2.dp, Color.White, RoundedCornerShape(60.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = "Delight Vibe",
                                                tint = ChatTubeColors.Yellow,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                }

                                // Interactive Info Bottom Row
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.61f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = post.caption,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.likePost(post.id, post.isLiked)
                                                previewingPost = previewingPost?.copy(
                                                    isLiked = !post.isLiked,
                                                    likesCount = post.likesCount + if (!post.isLiked) 1 else -1
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (post.isLiked) ChatTubeColors.Pink else Color.Black.copy(alpha = 0.5f))
                                        ) {
                                            Text(if (post.isLiked) "❤️ Liked (${post.likesCount})" else "🤍 Like (${post.likesCount})", color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Text(
                                            "❤️ matches Gen-Z Vibe",
                                            color = ChatTubeColors.Yellow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
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
