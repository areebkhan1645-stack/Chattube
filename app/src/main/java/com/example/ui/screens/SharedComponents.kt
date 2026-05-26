package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatTubeLogo(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        
        // 1. Draw Background (SQUIRCLE with beautiful LinearGradient matching the image)
        val bgBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF0F48F2), Color(0xFF0A0B29)),
            start = Offset(width, 0f),
            end = Offset(0f, height)
        )
        // Squircle corner radius
        val cornerRadius = width * 0.22f
        drawRoundRect(
            brush = bgBrush,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
        )
        
        // 2. Compute Speech Bubble Coordinates
        val left = width * 0.16f
        val top = height * 0.16f
        val right = width * 0.84f
        val bottom = height * 0.68f
        val bubbleCorner = width * 0.15f
        
        // Construct Speech Bubble Path with Tail on bottom left
        val bubblePath = Path().apply {
            moveTo(left + bubbleCorner, top)
            lineTo(right - bubbleCorner, top)
            quadraticBezierTo(right, top, right, top + bubbleCorner)
            lineTo(right, bottom - bubbleCorner)
            quadraticBezierTo(right, bottom, right - bubbleCorner, bottom)
            
            // Go along bottom towards left, but build the speech bubble pointer tail
            lineTo(width * 0.44f, bottom)
            quadraticBezierTo(width * 0.32f, height * 0.82f, width * 0.28f, height * 0.82f)
            quadraticBezierTo(left, height * 0.70f, left, height * 0.58f)
            
            lineTo(left, top + bubbleCorner)
            quadraticBezierTo(left, top, left + bubbleCorner, top)
            close()
        }
        
        // Setup Neon Gradient list (Teal/Cyan to Hot Magenta/Pink)
        val neonColors = listOf(Color(0xFF00FFFF), Color(0xFF00CCFF), Color(0xFFE1306C), Color(0xFF833AB4))
        val neonBrush = Brush.linearGradient(
            colors = neonColors,
            start = Offset(0f, height * 0.20f),
            end = Offset(width, height * 0.80f)
        )
        
        // Underglow effect (Shadow-like backing stroke for high-fidelity)
        drawPath(
            path = bubblePath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0x3300FFFF), Color(0x33E1306C)),
                start = Offset(0f, 0f),
                end = Offset(width, height)
            ),
            style = Stroke(width = width * 0.045f, cap = StrokeCap.Round)
        )
        
        // Main crisp glow bubble stroke
        drawPath(
            path = bubblePath,
            brush = neonBrush,
            style = Stroke(width = width * 0.022f, cap = StrokeCap.Round)
        )
        
        // 3. Draw Camera Body path
        val camLeft = width * 0.26f
        val camRight = width * 0.74f
        val camBottom = height * 0.61f
        val camTop = height * 0.34f
        val camCorner = width * 0.07f
        val bumpHeight = height * 0.04f
        
        val camPath = Path().apply {
            moveTo(camLeft + camCorner, camTop + bumpHeight)
            // Rightwards bump
            lineTo(width * 0.43f, camTop + bumpHeight)
            quadraticBezierTo(width * 0.46f, camTop, width * 0.49f, camTop)
            lineTo(width * 0.51f, camTop)
            quadraticBezierTo(width * 0.54f, camTop, width * 0.57f, camTop + bumpHeight)
            lineTo(camRight - camCorner, camTop + bumpHeight)
            
            quadraticBezierTo(camRight, camTop + bumpHeight, camRight, camTop + bumpHeight + camCorner)
            lineTo(camRight, camBottom - camCorner)
            quadraticBezierTo(camRight, camBottom, camRight - camCorner, camBottom)
            lineTo(camLeft + camCorner, camBottom)
            quadraticBezierTo(camLeft, camBottom, camLeft, camBottom - camCorner)
            lineTo(camLeft, camTop + bumpHeight + camCorner)
            quadraticBezierTo(camLeft, camTop + bumpHeight, camLeft + camCorner, camTop + bumpHeight)
            close()
        }
        
        // Underglow stroke for camera body
        drawPath(
            path = camPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0x3300D8F6), Color(0x33F000FF)),
                start = Offset(camLeft, camTop),
                end = Offset(camRight, camBottom)
            ),
            style = Stroke(width = width * 0.038f, cap = StrokeCap.Round)
        )
        
        // Crisp camera stroke
        drawPath(
            path = camPath,
            brush = neonBrush,
            style = Stroke(width = width * 0.02f, cap = StrokeCap.Round)
        )
        
        // 4. Center lens circle and play button
        val lensCx = width * 0.50f
        val lensCy = height * 0.47f
        val lensRadius = width * 0.09f
        
        // Draw lens backing glow
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0x3300FFFF), Color(0x33E1306C)),
                start = Offset(lensCx - lensRadius, lensCy - lensRadius),
                end = Offset(lensCx + lensRadius, lensCy + lensRadius)
            ),
            radius = lensRadius * 1.25f,
            center = Offset(lensCx, lensCy),
            style = Stroke(width = width * 0.022f)
        )
        
        // Draw crisp lens circle
        drawCircle(
            brush = neonBrush,
            radius = lensRadius,
            center = Offset(lensCx, lensCy),
            style = Stroke(width = width * 0.018f)
        )
        
        // 5. Draw Solid Play Triangle inside
        val triPath = Path().apply {
            val triSize = lensRadius * 0.45f
            val startX = lensCx - triSize * 0.4f
            val endX = lensCx + triSize * 0.8f
            val topY = lensCy - triSize * 0.6f
            val bottomY = lensCy + triSize * 0.6f
            
            moveTo(startX, topY)
            lineTo(endX, lensCy)
            lineTo(startX, bottomY)
            close()
        }
        
        // Fill play button with glowing white/cyan gradient
        val playBrush = Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFC4FFFF)),
            start = Offset(lensCx, lensCy - lensRadius),
            end = Offset(lensCx, lensCy + lensRadius)
        )
        drawPath(
            path = triPath,
            brush = playBrush
        )
    }
}

// Centralized colors for ChatTube
object ChatTubeColors {
    val Yellow = Color(0xFFFFB300) // Darker yellow for visibility
    val Pink = Color(0xFFE1306C) // Instagram Pink
    val Purple = Color(0xFF833AB4) // Instagram Purple
    val Blue = Color(0xFF405DE6) // Cool Blue
    val DarkBackground = Color(0xFFF5F5F7) // Light Grey background
    val SurfaceDark = Color(0xFFFFFFFF) // White card background
    val BorderDark = Color(0xFFE5E5EA) // Light grey border
    val WhiteTranslucent = Color(0x11000000)
    val PureWhite = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF1C1C1E) // Dark Grey/Black for text
    val TextSecondary = Color(0xFF8E8E93) // Grey for secondary text
    
    // Gradient lists
    val Instagradient = listOf(Pink, Purple, Blue)
    val Snapgradient = listOf(Yellow, Color(0xFFFF9E00))
    val Tubegradient = listOf(Color(0xFFFF0055), Color(0xFF7A00FF))
    val Cybergradient = listOf(Color(0xFF00F0FF), Color(0xFFFF007F))
    val EtherealGradient = listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
}

@Composable
fun UserAvatar(
    username: String,
    avatarIndex: Int,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
    hasStory: Boolean = false,
    storyViewed: Boolean = false,
    profilePicUri: String? = null,
    onClick: (() -> Unit)? = null
) {
    // Determine gradient list based on avatar index for diverse fun branding
    val gradientColors = when (avatarIndex % 5) {
        0 -> listOf(Color(0xFFF72585), Color(0xFF7209B7))
        1 -> listOf(Color(0xFF3F37C9), Color(0xFF4CC9F0))
        2 -> listOf(Color(0xFF4F5D75), Color(0xFFEF8354))
        3 -> listOf(Color(0xFF10B981), Color(0xFF059669))
        else -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    }

    val brush = Brush.linearGradient(gradientColors)
    
    val outerModifier = modifier
        .size(size)
        .let {
            if (hasStory) {
                val storyColor = if (storyViewed) {
                    Brush.linearGradient(listOf(Color(0xFF444444), Color(0xFF222222)))
                } else {
                    Brush.linearGradient(ChatTubeColors.Instagradient)
                }
                it.border(2.dp, storyColor, CircleShape).padding(4.dp)
            } else {
                it.border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape).padding(2.dp)
            }
        }
        .clip(CircleShape)
        .background(brush)
        .let {
            if (onClick != null) it.clickable { onClick() } else it
        }

    Box(
        modifier = outerModifier,
        contentAlignment = Alignment.Center
    ) {
        if (!profilePicUri.isNullOrEmpty()) {
            AsyncImage(
                model = profilePicUri,
                contentDescription = "$username's profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            val initial = if (username.isNotEmpty()) username.uppercase().take(1) else "U"
            Text(
                text = initial,
                color = Color.White,
                fontSize = (size.value * 0.45f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GlassmorphicHeader(
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable (BoxScope.() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(ChatTubeColors.DarkBackground, ChatTubeColors.DarkBackground.copy(alpha = 0.8f))
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = ChatTubeColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
            Text(
                text = subtitle ?: "ChatTube",
                color = ChatTubeColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
        
        if (trailingContent != null) {
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                trailingContent()
            }
        }
    }
}
