package com.example.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ChatTubeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatTubeViewModel,
    onClose: () -> Unit,
    onAddAccount: () -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()
    val loggedInAccounts by viewModel.loggedInAccounts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChatTubeColors.DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = ChatTubeColors.DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Account Center Logins
            SettingsHeader("Account Center")
            SettingsItem(icon = Icons.Default.Person, title = "Personal Details")
            SettingsItem(icon = Icons.Default.Security, title = "Password & Security")

            Spacer(modifier = Modifier.height(16.dp))
            SettingsHeader("Multi-Account Management")
            loggedInAccounts.forEach { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (account.username != userStats?.username) {
                                viewModel.switchActiveAccount(account.username)
                                onClose()
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(account.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (account.username == userStats?.username) {
                            Text("Active", color = Color.Green, fontSize = 12.sp)
                        }
                    }
                    if (account.username != userStats?.username) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = "Switch", tint = Color.Gray)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = ChatTubeColors.Pink)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddAccount() }
                    .padding(16.dp)
            ) {
                Text("Add Account", color = ChatTubeColors.Pink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            SettingsHeader("Privacy & Safety")
            SettingsToggleItem(icon = Icons.Default.Lock, title = "Private Account", defaultState = false)
            SettingsItem(icon = Icons.Default.Block, title = "Blocked Contacts")
            SettingsToggleItem(icon = Icons.Default.Comment, title = "Allow Comments", defaultState = true)
            SettingsToggleItem(icon = Icons.Default.Visibility, title = "Activity Status", defaultState = true)

            Spacer(modifier = Modifier.height(16.dp))
            SettingsHeader("Notifications")
            SettingsToggleItem(icon = Icons.Default.Favorite, title = "Likes & Comments", defaultState = true)
            SettingsToggleItem(icon = Icons.Default.VideoLibrary, title = "New Reels", defaultState = true)
            SettingsToggleItem(icon = Icons.Default.Email, title = "Direct Messages", defaultState = true)

            Spacer(modifier = Modifier.height(16.dp))
            SettingsHeader("App Preferences")
            SettingsToggleItem(icon = Icons.Default.DarkMode, title = "Dark Mode", defaultState = true)
            SettingsItem(icon = Icons.Default.Language, title = "Language", subtitle = "English")
            SettingsItem(icon = Icons.Default.DataUsage, title = "Data Usage & Media Quality")

            Spacer(modifier = Modifier.height(16.dp))
            SettingsHeader("Help & Support")
            SettingsItem(icon = Icons.Default.HelpOutline, title = "Help Center")
            SettingsItem(icon = Icons.Default.ReportProblem, title = "Report a Problem")
            SettingsItem(icon = Icons.Default.Info, title = "About & Terms of Service")

            Spacer(modifier = Modifier.height(24.dp))
            
            // Logout Options
            TextButton(
                onClick = {
                    viewModel.logout()
                    onClose()
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Log Out ${userStats?.username ?: ""}", color = ChatTubeColors.Pink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = {
                    viewModel.logoutAll()
                    onClose()
                },
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
            ) {
                Text("Log Out All Accounts", color = ChatTubeColors.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 16.sp)
            if (subtitle != null) {
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun SettingsToggleItem(icon: ImageVector, title: String, defaultState: Boolean) {
    var isChecked by remember { mutableStateOf(defaultState) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            colors = SwitchDefaults.colors(checkedThumbColor = ChatTubeColors.Pink, checkedTrackColor = ChatTubeColors.Pink.copy(alpha = 0.5f))
        )
    }
}
