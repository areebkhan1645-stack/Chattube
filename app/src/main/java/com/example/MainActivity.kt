package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatTubeViewModel

enum class ChatTubeTab(val label: String) {
    FEED("Home"),
    EXPLORE("Explore"),
    CAMERA("Camera"),
    CHAT("Chats"),
    PROFILE("Profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ChatTubeViewModel = viewModel()
                val userStats by viewModel.userStats.collectAsState()
                var currentTab by remember { mutableStateOf(ChatTubeTab.FEED) }
                var isSettingsOpen by remember { mutableStateOf(false) }
                var isAddingAccount by remember { mutableStateOf(false) }

                LaunchedEffect(userStats?.username) {
                    if (userStats?.isLoggedIn == true) {
                        isAddingAccount = false
                    }
                }

                if ((userStats == null || !userStats!!.isLoggedIn) || isAddingAccount) {
                    // Instagram-style Secure signup and login screen interceptor
                    AuthScreen(viewModel = viewModel)
                } else if (isSettingsOpen) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onClose = { isSettingsOpen = false },
                        onAddAccount = { isAddingAccount = true }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = ChatTubeColors.DarkBackground,
                        bottomBar = {
                            NavigationBar(
                                containerColor = ChatTubeColors.SurfaceDark,
                                tonalElevation = 8.dp,
                                modifier = Modifier
                                    .border(1.dp, ChatTubeColors.BorderDark, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .testTag("bottom_nav_bar")
                            ) {
                                ChatTubeTab.values().forEach { tab ->
                                    val selected = currentTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { currentTab = tab },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp,
                                                color = if (selected) ChatTubeColors.TextPrimary else Color.Gray
                                            )
                                        },
                                        icon = {
                                            val iconSelected = when (tab) {
                                                ChatTubeTab.FEED -> Icons.Filled.Home
                                                ChatTubeTab.EXPLORE -> Icons.Filled.Search
                                                ChatTubeTab.CAMERA -> Icons.Filled.CameraAlt
                                                ChatTubeTab.CHAT -> Icons.Filled.ChatBubble
                                                ChatTubeTab.PROFILE -> Icons.Filled.Person
                                            }
                                            val iconUnselected = when (tab) {
                                                ChatTubeTab.FEED -> Icons.Outlined.Home
                                                ChatTubeTab.EXPLORE -> Icons.Outlined.Search
                                                ChatTubeTab.CAMERA -> Icons.Outlined.CameraAlt
                                                ChatTubeTab.CHAT -> Icons.Outlined.ChatBubbleOutline
                                                ChatTubeTab.PROFILE -> Icons.Outlined.Person
                                            }
                                            
                                            // Give camera tab custom vibrant amber glowing background matching Snapchat shutter design
                                            if (tab == ChatTubeTab.CAMERA) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (selected) {
                                                                Brush.linearGradient(ChatTubeColors.Snapgradient)
                                                            } else {
                                                                Brush.linearGradient(listOf(Color(0xFF222222), Color(0xFF111111)))
                                                            }
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = iconSelected,
                                                        contentDescription = tab.label,
                                                        tint = if (selected) ChatTubeColors.Pink else Color.Gray,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = if (selected) iconSelected else iconUnselected,
                                                    contentDescription = tab.label,
                                                    tint = if (selected) ChatTubeColors.Pink else Color.LightGray
                                                )
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = ChatTubeColors.WhiteTranslucent
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Render Active Screen based on selection state with 5 modern tabs
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "TabTransition"
                            ) { targetTab ->
                                when (targetTab) {
                                    ChatTubeTab.FEED -> FeedScreen(
                                        viewModel = viewModel,
                                        onNavigateToCamera = { currentTab = ChatTubeTab.CAMERA },
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                    )
                                    ChatTubeTab.EXPLORE -> ExploreScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                    )
                                    ChatTubeTab.CAMERA -> CameraScreen(
                                        viewModel = viewModel,
                                        onNavigateToFeed = { currentTab = ChatTubeTab.FEED },
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                    )
                                    ChatTubeTab.CHAT -> ChatScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                    )
                                    ChatTubeTab.PROFILE -> ProfileScreen(
                                        viewModel = viewModel,
                                        onOpenSettings = { isSettingsOpen = true },
                                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                    )
                                }
                            }

                        // Full Screen Overlay for automatic Stories viewing on top of any tab
                        StoryViewerOverlay(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
}
