package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import com.example.data.local.MessageEntity
import com.example.ui.viewmodel.ChatTubeViewModel
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(
    viewModel: ChatTubeViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    var activeChattingFriendName by remember { mutableStateOf<String?>(null) }
    var chatInputText by remember { mutableStateOf("") }
    
    // Self-destruct snap active viewer state
    val activeViewingSnap by viewModel.activeViewingSnap.collectAsState()
    var snapCountdownValue by remember { mutableIntStateOf(5) }

    // Aggregate unique friends we had chat interactions with
    val defaultContactNames = listOf("Sarah Travels", "Alex Vlogs", "Jake Skates", "Foodie Vibes")

    // Dynamic timer for snap destruction
    if (activeViewingSnap != null) {
        LaunchedEffect(activeViewingSnap) {
            val snapId = activeViewingSnap!!.id
            snapCountdownValue = activeViewingSnap!!.durationSeconds
            while (snapCountdownValue > 0) {
                delay(1000)
                snapCountdownValue--
            }
            // self destruct completed: delete from database and chat list instantly
            viewModel.deleteMessage(snapId)
            viewModel.setViewingSnapMessage(null)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChatTubeColors.DarkBackground,
        topBar = {
            GlassmorphicHeader(
                title = "MESSAGE SNAPS",
                subtitle = "ChatRoom Logs",
                trailingContent = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "Add Friend", tint = ChatTubeColors.TextPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // Main Friend chat logs
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("chat_logs_list")
            ) {
                items(defaultContactNames) { friend ->
                    // Find the absolute latest chat interaction for subtext
                    val lastInteraction = messages.filter {
                        (it.senderName == friend && it.receiverName == "You") ||
                        (it.senderName == "You" && it.receiverName == friend)
                    }.lastOrNull()

                    FriendChatRow(
                        friendName = friend,
                        lastMessage = lastInteraction,
                        onRowClick = {
                            activeChattingFriendName = friend
                        },
                        onOpenSnap = { snapMsg ->
                            viewModel.setViewingSnapMessage(snapMsg)
                        }
                    )
                }
            }

            // Chat Room Overlay (High level overlay matching classic direct chats)
            if (activeChattingFriendName != null) {
                val friend = activeChattingFriendName!!
                val chatMessages = messages.filter {
                    val matchesFriend = ((it.senderName == friend && it.receiverName == "You") ||
                                         (it.senderName == "You" && it.receiverName == friend))
                    val isOpenedSnap = it.messageType == "SNAP" && it.isOpened
                    matchesFriend && !isOpenedSnap
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ChatTubeColors.DarkBackground)
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = ChatTubeColors.DarkBackground,
                        topBar = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ChatTubeColors.SurfaceDark)
                                    .statusBarsPadding()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { activeChattingFriendName = null }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ChatTubeColors.TextPrimary)
                                }
                                
                                UserAvatar(username = friend, avatarIndex = friend.hashCode(), size = 36.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(friend, color = ChatTubeColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF39FF14)))
                                        Text("Active in Chattube", color = Color.Gray, fontSize = 10.sp)
                                    }
                                }

                                Row {
                                    IconButton(onClick = {}) {
                                        Icon(Icons.Default.Phone, contentDescription = "Dial", tint = ChatTubeColors.TextPrimary)
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(Icons.Default.VideoCall, contentDescription = "Tube Call", tint = Color.LightGray)
                                    }
                                }
                            }
                        }
                    ) { chatPadding ->
                        Column(
                            modifier = Modifier
                                .padding(chatPadding)
                                .fillMaxSize()
                                .imePadding()
                        ) {
                            // Messages Area
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                items(chatMessages) { msg ->
                                    ChatBubbleItem(
                                        message = msg,
                                        isMe = msg.senderName == "You",
                                        onSnapOpenTrigger = {
                                            viewModel.setViewingSnapMessage(msg)
                                        }
                                    )
                                }
                            }

                            // Dynamic snap shortcut inside keyboard entry
                            HorizontalDivider(color = ChatTubeColors.BorderDark)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ChatTubeColors.SurfaceDark)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Camera icon to instantly send a snap!
                                IconButton(
                                    onClick = {
                                        // Send a quick default snap
                                        viewModel.sendSnapMessage(
                                            friendName = friend,
                                            snapSpecDescription = "⚡️ High Energy Retro Quick Snap!",
                                            durationSeconds = 6,
                                            filterApplied = "Neon Overdrive"
                                        )
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(ChatTubeColors.TextPrimary.copy(alpha = 0.08f))
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Quick Cam Snap", tint = ChatTubeColors.Yellow)
                                }

                                OutlinedTextField(
                                    value = chatInputText,
                                    onValueChange = { chatInputText = it },
                                    placeholder = { Text("Send Chat Message...", color = Color.Gray, fontSize = 13.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = ChatTubeColors.TextPrimary,
                                        unfocusedTextColor = ChatTubeColors.TextPrimary,
                                        focusedContainerColor = Color.Black,
                                        unfocusedContainerColor = Color.Black,
                                        focusedBorderColor = ChatTubeColors.Pink,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("chat_text_input"),
                                    shape = RoundedCornerShape(20.dp)
                                )

                                IconButton(
                                    onClick = {
                                        if (chatInputText.isNotEmpty()) {
                                            viewModel.sendTextMessage(friend, chatInputText)
                                            chatInputText = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(ChatTubeColors.Tubegradient))
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = ChatTubeColors.TextPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Snapchat Self-destructing Snap Full-screen Viewer Overlay
            if (activeViewingSnap != null) {
                val currentViewing = activeViewingSnap!!
                
                // Overlay base gradient
                val overlayBrush = when (currentViewing.appliedFilter) {
                    "Golden Hour" -> Brush.verticalGradient(listOf(Color(0xFFFFAA00), Color(0xFF1D1B26)))
                    "Neon Overdrive" -> Brush.verticalGradient(listOf(Color(0xFFFF00FF), Color(0xFF00FFFF)))
                    "1995 Nostalgia" -> Brush.verticalGradient(listOf(Color(0xFFD4A373), Color(0xFF121016)))
                    else -> Brush.radialGradient(listOf(Color(0xFF833AB4), Color(0xFF0F0E17)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(overlayBrush)
                        .clickable { viewModel.setViewingSnapMessage(null) }
                        .testTag("snap_active_overlay")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Countdown dial top row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SNAP FROM", color = ChatTubeColors.TextPrimary.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(currentViewing.senderName, color = ChatTubeColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }

                            // Pulsing countdown circular indicator badge
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ChatTubeColors.Yellow.copy(alpha = 0.9f))
                                    .border(2.dp, ChatTubeColors.TextPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$snapCountdownValue",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Central Graphic (representing lensed image detail)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterFrames,
                                contentDescription = "Visual frame focus",
                                tint = ChatTubeColors.TextPrimary.copy(alpha = 0.4f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            
                            // Caption Overlay Box
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp)
                                    .background(Color.Black.copy(alpha = 0.82f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = currentViewing.content,
                                    color = ChatTubeColors.TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Lens label bottom
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(bottom = 36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.HourglassBottom, contentDescription = "Active Filter", tint = ChatTubeColors.Yellow, modifier = Modifier.size(12.dp))
                                Text(
                                    text = if (currentViewing.appliedFilter != "None") "Lensed with ${currentViewing.appliedFilter}" else "Original",
                                    color = ChatTubeColors.TextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendChatRow(
    friendName: String,
    lastMessage: MessageEntity?,
    onRowClick: () -> Unit,
    onOpenSnap: (MessageEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Friend avatar icon color based on name hash
        UserAvatar(username = friendName, avatarIndex = friendName.hashCode(), size = 48.dp)
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                friendName,
                color = ChatTubeColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Build Snapchat status text indicators
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (lastMessage == null) {
                    Text("No messages yet. Say hi 👋", color = Color.Gray, fontSize = 11.sp)
                } else {
                    val isSnap = lastMessage.messageType == "SNAP"
                    val isMe = lastMessage.senderName == "You"
                    
                    // Render status icon prefixes
                    if (isSnap) {
                        if (isMe) {
                            Icon(Icons.Default.ArrowOutward, contentDescription = "Sent", tint = Color.Red, modifier = Modifier.size(10.dp))
                            Text("Sent Snap • Delivered", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            if (lastMessage.isOpened) {
                                Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = "Opened", tint = Color.Red, modifier = Modifier.size(12.dp))
                                Text("Opened Snap", color = Color.Gray, fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.Square, contentDescription = "New Snap!", tint = Color.Red, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "New Snap • Tap to Open! 👻",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.clickable { onOpenSnap(lastMessage) }
                                )
                            }
                        }
                    } else {
                        // Text message
                        if (isMe) {
                            Icon(Icons.Default.ArrowOutward, contentDescription = "Sent", tint = Color.Blue, modifier = Modifier.size(10.dp))
                            Text("Sent Text", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            Text(
                                "Received: ${lastMessage.content}",
                                color = ChatTubeColors.TextPrimary.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Camera Button Shortcut inside row (matching Snapchat right-aligned quick snap triggers)
        IconButton(
            onClick = onRowClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(ChatTubeColors.TextPrimary.copy(alpha = 0.05f))
        ) {
            Icon(Icons.Default.ModeComment, contentDescription = "Open Chat", tint = Color.LightGray, modifier = Modifier.size(18.dp))
        }
    }
    
    HorizontalDivider(color = ChatTubeColors.BorderDark, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun ChatBubbleItem(
    message: MessageEntity,
    isMe: Boolean,
    onSnapOpenTrigger: () -> Unit
) {
    val horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    val containerColor = if (isMe) ChatTubeColors.Pink else ChatTubeColors.SurfaceDark
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {
        if (message.messageType == "SNAP") {
            // Snapchat Style Snap Card inside chat thread
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .clickable(enabled = !isMe && !message.isOpened) {
                        onSnapOpenTrigger()
                    },
                colors = CardDefaults.cardColors(containerColor = ChatTubeColors.SurfaceDark),
                border = BorderStroke(1.dp, if (message.isOpened) ChatTubeColors.BorderDark else Color.Red)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (message.isOpened) Color.Transparent else Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (message.isOpened) Icons.Default.Drafts else Icons.Default.Camera,
                            contentDescription = "Camera Icon",
                            tint = if (message.isOpened) Color.LightGray else ChatTubeColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isMe) "Sent Snap" else if (message.isOpened) "Opened Snap" else "Tap to Play Snap! 🍿",
                            color = ChatTubeColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (message.isOpened) "Cannot replay" else "${message.durationSeconds}s self-destruct countdown",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        } else {
            // Standard Text Chat Bubble
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(
                        topStart = 16.dp, 
                        topEnd = 16.dp, 
                        bottomStart = if (isMe) 16.dp else 0.dp, 
                        bottomEnd = if (isMe) 0.dp else 16.dp
                    ))
                    .background(containerColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    color = ChatTubeColors.TextPrimary,
                    fontSize = 13.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (isMe) "Delivered" else "Received",
            color = Color.Gray,
            fontSize = 8.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
