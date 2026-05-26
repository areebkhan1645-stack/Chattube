package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: ChatTubeViewModel,
    modifier: Modifier = Modifier
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    
    // Form Inputs
    var usernameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var bioInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    val authErrorMsg by viewModel.authError.collectAsState()

    val logoBrush = Brush.linearGradient(ChatTubeColors.Instagradient)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChatTubeColors.DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful Gradient App Logo & Brand header
            ChatTubeLogo(
                modifier = Modifier
                    .size(110.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CHATTUBE",
                color = ChatTubeColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Secure Social Creation Hub",
                color = ChatTubeColors.Yellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Card Form Container
            Card(
                colors = CardDefaults.cardColors(containerColor = ChatTubeColors.SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, ChatTubeColors.BorderDark),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isSignUpMode) "Create Creator Profile" else "Welcome Back",
                        color = ChatTubeColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val displayError = authErrorMsg ?: errorMsg
                    if (displayError.isNotEmpty()) {
                        Text(
                            text = displayError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Fields
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it; errorMsg = "" },
                            label = { Text("Full Creator Name") },
                            placeholder = { Text("e.g. Sarah Connor") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                            modifier = Modifier.fillMaxWidth().testTag("signup_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ChatTubeColors.TextPrimary,
                                unfocusedTextColor = ChatTubeColors.TextPrimary,
                                focusedBorderColor = ChatTubeColors.Pink,
                                unfocusedBorderColor = ChatTubeColors.BorderDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it.lowercase().trim(); errorMsg = "" },
                            label = { Text("Interactive Handle") },
                            placeholder = { Text("e.g. sarah_travels") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username") },
                            modifier = Modifier.fillMaxWidth().testTag("signup_username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ChatTubeColors.TextPrimary,
                                unfocusedTextColor = ChatTubeColors.TextPrimary,
                                focusedBorderColor = ChatTubeColors.Pink,
                                unfocusedBorderColor = ChatTubeColors.BorderDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it; errorMsg = ""; viewModel.clearAuthError() },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("+1 234 567 890") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                            modifier = Modifier.fillMaxWidth().testTag("signup_phone_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ChatTubeColors.TextPrimary,
                                unfocusedTextColor = ChatTubeColors.TextPrimary,
                                focusedBorderColor = ChatTubeColors.Pink,
                                unfocusedBorderColor = ChatTubeColors.BorderDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; errorMsg = "" },
                            label = { Text("Password") },
                            placeholder = { Text("********") },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                            modifier = Modifier.fillMaxWidth().testTag("signup_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ChatTubeColors.TextPrimary,
                                unfocusedTextColor = ChatTubeColors.TextPrimary,
                                focusedBorderColor = ChatTubeColors.Pink,
                                unfocusedBorderColor = ChatTubeColors.BorderDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it.lowercase().trim(); errorMsg = ""; viewModel.clearAuthError() },
                            label = { Text("Mobile Number or Username") },
                            placeholder = { Text("sarah_travels or +1 ...") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Identifier") },
                            modifier = Modifier.fillMaxWidth().testTag("username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ChatTubeColors.TextPrimary,
                                unfocusedTextColor = ChatTubeColors.TextPrimary,
                                focusedBorderColor = ChatTubeColors.Yellow,
                                unfocusedBorderColor = ChatTubeColors.BorderDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; errorMsg = "" },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            placeholder = { Text("********") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                            modifier = Modifier.fillMaxWidth().testTag("password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ChatTubeColors.TextPrimary,
                                unfocusedTextColor = ChatTubeColors.TextPrimary,
                                focusedBorderColor = ChatTubeColors.Yellow,
                                unfocusedBorderColor = ChatTubeColors.BorderDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Main Action Button (Instagram / Snapchat design merge button)
                    Button(
                        onClick = {
                            if (isSignUpMode) {
                                if (nameInput.trim().isEmpty() || usernameInput.trim().isEmpty() || passwordInput.isEmpty()) {
                                    errorMsg = "Name, Handle, and Password are required."
                                } else {
                                    viewModel.signup(
                                        phone = phoneInput.trim(),
                                        passwordHash = passwordInput,
                                        username = usernameInput,
                                        name = nameInput,
                                        bio = if (bioInput.trim().isEmpty()) "Tubes and Snaps Creator! ✨" else bioInput
                                    )
                                }
                            } else {
                                if (usernameInput.trim().isEmpty() || passwordInput.isEmpty()) {
                                    errorMsg = "Please enter your identifier and password."
                                } else {
                                    viewModel.login(usernameInput, passwordInput)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button")
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(if (isSignUpMode) ChatTubeColors.Instagradient else ChatTubeColors.Snapgradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isSignUpMode) "Start Chattubing! 🌈" else "Sign In Safely 🛸",
                                    color = if (isSignUpMode) ChatTubeColors.TextPrimary else Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Forward Action",
                                    tint = if (isSignUpMode) ChatTubeColors.TextPrimary else Color.Black
                                )
                            }
                        }
                    }

                    // Toggle mode option
                    TextButton(
                        onClick = {
                            isSignUpMode = !isSignUpMode
                            errorMsg = ""
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally).testTag("signup_toggle")
                    ) {
                        Text(
                            text = if (isSignUpMode) "Already have a creator lab? Log In" else "New to ChatTube? Register Studio Profile",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
