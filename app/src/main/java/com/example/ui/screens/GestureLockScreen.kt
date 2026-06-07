package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

enum class GestureLockMode {
    SETUP_INITIAL,
    SETUP_CONFIRM,
    VERIFY
}

@Composable
fun GestureLockScreen(
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("GestureLockPrefs", Context.MODE_PRIVATE)
    
    val isSkipped = prefs.getBoolean("is_gesture_skipped", false)
    val savedGestureString = prefs.getString("saved_gesture", null)
    
    var mode by remember { 
        mutableStateOf(if (savedGestureString == null) GestureLockMode.SETUP_INITIAL else GestureLockMode.VERIFY) 
    }

    LaunchedEffect(Unit) {
        if (isSkipped) {
            onUnlocked()
        }
    }

    if (isSkipped) return
    
    var initialGesture by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var currentGesture by remember { mutableStateOf<List<Offset>>(emptyList()) }
    
    var showError by remember { mutableStateOf(false) }
    var glowColor by remember { mutableStateOf(Color(0xFF00E5FF)) } // Neon Cyan
    
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    fun normalizeGesture(points: List<Offset>): List<Offset> {
        if (points.isEmpty()) return emptyList()
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE
        
        for (p in points) {
            if (p.x < minX) minX = p.x
            if (p.y < minY) minY = p.y
            if (p.x > maxX) maxX = p.x
            if (p.y > maxY) maxY = p.y
        }
        
        val width = max(maxX - minX, 1f)
        val height = max(maxY - minY, 1f)
        
        val scale = 100f / max(width, height)
        
        return points.map { 
            Offset((it.x - minX) * scale, (it.y - minY) * scale) 
        }
    }
    
    fun resample(points: List<Offset>, n: Int): List<Offset> {
        if (points.isEmpty()) return emptyList()
        val resampled = mutableListOf<Offset>()
        resampled.add(points.first())
        
        var totalLength = 0f
        val distances = mutableListOf<Float>()
        for (i in 1 until points.size) {
            val dist = (points[i] - points[i-1]).getDistance()
            distances.add(dist)
            totalLength += dist
        }
        
        val interval = max(totalLength / (n - 1), 0.001f) // Ensure no division by zero
        var D = 0f
        var i = 1
        var currentPoint = points[0]
        
        while (i < points.size) {
            val d = (points[i] - currentPoint).getDistance()
            if (D + d >= interval) {
                val qx = currentPoint.x + ((interval - D) / d) * (points[i].x - currentPoint.x)
                val qy = currentPoint.y + ((interval - D) / d) * (points[i].y - currentPoint.y)
                val q = Offset(qx, qy)
                resampled.add(q)
                currentPoint = q
                D = 0f
            } else {
                D += d
                currentPoint = points[i]
                i++
            }
        }
        
        if (resampled.size < n) {
            resampled.add(points.last())
        }
        return resampled.take(n)
    }

    fun calculateMatchScore(p1: List<Offset>, p2: List<Offset>): Float {
        val norm1 = resample(normalizeGesture(p1), 50)
        val norm2 = resample(normalizeGesture(p2), 50)
        
        if (norm1.size != norm2.size || norm1.isEmpty()) return Float.MAX_VALUE
        
        var distance = 0f
        for (i in norm1.indices) {
            distance += (norm1[i] - norm2[i]).getDistance()
        }
        return distance / norm1.size
    }
    
    fun triggerError() {
        coroutineScope.launch {
            glowColor = Color(0xFFFF1744) // Neon Red
            showError = true
            
            // Shake animation
            shakeOffset.animateTo(20f, tween(50, easing = LinearEasing))
            shakeOffset.animateTo(-20f, tween(50))
            shakeOffset.animateTo(20f, tween(50))
            shakeOffset.animateTo(-20f, tween(50))
            shakeOffset.animateTo(0f, tween(50))
            
            delay(500)
            currentGesture = emptyList()
            glowColor = Color(0xFF00E5FF)
            showError = false
        }
    }
    
    fun triggerSuccess(onComplete: () -> Unit) {
        coroutineScope.launch {
            glowColor = Color(0xFF00E676) // Neon Green
            delay(400)
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1121)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, end = 24.dp)
        ) {
            if (mode == GestureLockMode.SETUP_INITIAL) {
                TextButton(
                    onClick = { 
                        prefs.edit().putBoolean("is_gesture_skipped", true).apply()
                        onUnlocked()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        "Skip",
                        color = Color(0xFF00E5FF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Skip",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = when (mode) {
                GestureLockMode.SETUP_INITIAL -> "Set New Gesture"
                GestureLockMode.SETUP_CONFIRM -> "Confirm Gesture"
                GestureLockMode.VERIFY -> "Draw Gesture to Unlock"
            },
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (showError) "Match failed. Please try again." else "Draw continuously anywhere on the pad",
            color = if (showError) Color(0xFFFF1744) else Color.LightGray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .offset(x = shakeOffset.value.dp)
                .background(Color(0xFF1E293B).copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                .pointerInput(mode) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentGesture = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            currentGesture = currentGesture + change.position
                        },
                        onDragEnd = {
                            if (currentGesture.size < 10) {
                                currentGesture = emptyList()
                                return@detectDragGestures
                            }
                            
                            when (mode) {
                                GestureLockMode.SETUP_INITIAL -> {
                                    initialGesture = currentGesture
                                    currentGesture = emptyList()
                                    mode = GestureLockMode.SETUP_CONFIRM
                                }
                                GestureLockMode.SETUP_CONFIRM -> {
                                    val score = calculateMatchScore(initialGesture, currentGesture)
                                    if (score < 25f) { // Tolerance Threshold
                                        // Save gesture
                                        val gestureStr = initialGesture.joinToString(";") { "${it.x},${it.y}" }
                                        prefs.edit().putString("saved_gesture", gestureStr).apply()
                                        triggerSuccess { onUnlocked() }
                                    } else {
                                        triggerError()
                                        mode = GestureLockMode.SETUP_INITIAL
                                        initialGesture = emptyList()
                                    }
                                }
                                GestureLockMode.VERIFY -> {
                                    val savedStr = prefs.getString("saved_gesture", "") ?: ""
                                    if (savedStr.isNotEmpty()) {
                                        val savedPoints = savedStr.split(";").mapNotNull { 
                                            val parts = it.split(",")
                                            if (parts.size == 2) Offset(parts[0].toFloat(), parts[1].toFloat()) else null
                                        }
                                        val score = calculateMatchScore(savedPoints, currentGesture)
                                        if (score < 25f) { // Match success
                                            triggerSuccess { onUnlocked() }
                                        } else {
                                            triggerError()
                                        }
                                    } else {
                                        onUnlocked() 
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (currentGesture.size > 1) {
                    val path = Path().apply {
                        moveTo(currentGesture.first().x, currentGesture.first().y)
                        for (i in 1 until currentGesture.size) {
                            lineTo(currentGesture[i].x, currentGesture[i].y)
                        }
                    }
                    
                    // Outer Glow
                    drawPath(
                        path = path,
                        color = glowColor.copy(alpha = 0.2f),
                        style = Stroke(width = 40f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Inner Glow
                    drawPath(
                        path = path,
                        color = glowColor.copy(alpha = 0.5f),
                        style = Stroke(width = 20f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Core Line
                    drawPath(
                        path = path,
                        color = glowColor,
                        style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (mode == GestureLockMode.SETUP_CONFIRM) {
            TextButton(
                onClick = { 
                    mode = GestureLockMode.SETUP_INITIAL
                    initialGesture = emptyList()
                    currentGesture = emptyList()
                }
            ) {
                Text("Reset Profile Gesture", color = Color(0xFF00E5FF))
            }
            Spacer(modifier = Modifier.height(20.dp))
        } else {
            Spacer(modifier = Modifier.height(68.dp))
        }
    }
}
