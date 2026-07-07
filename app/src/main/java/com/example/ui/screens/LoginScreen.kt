package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.SupabaseManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    supabaseManager: SupabaseManager,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by supabaseManager.currentUser.collectAsState()
    val authError by supabaseManager.authError.collectAsState()
    val isAuthLoading by supabaseManager.isAuthLoading.collectAsState()
    val isInternetConnected = supabaseManager.isInternetConnected()

    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localErrorMsg by remember { mutableStateOf<String?>(null) }

    // Navigation trigger on login success
    LaunchedEffect(currentUser) {
        if (currentUser?.isLoggedIn == true) {
            onLoginSuccess()
        }
    }

    Scaffold(
        containerColor = DeepCharcoal,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepCharcoal, Color(0xFF0F1215))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Brand Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GraphiteGray)
                        .border(1.5.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "BMS Lightning",
                        tint = NeonGreen,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App Title & Tagline matching Elegant Dark spec
                Text(
                    text = "TOTO CONTROLLER",
                    color = NeonGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SUPABASE CLOUD PORTAL",
                    color = TextWhite.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real-world configuration status card (English & Bengali)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (supabaseManager.isSupabaseConfigured) NeonGreen.copy(alpha = 0.1f) else SaffronColor.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (supabaseManager.isSupabaseConfigured) NeonGreen.copy(alpha = 0.3f) else SaffronColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (supabaseManager.isSupabaseConfigured) Icons.Default.CloudQueue else Icons.Default.Info,
                                contentDescription = "Status",
                                tint = if (supabaseManager.isSupabaseConfigured) NeonGreen else SaffronColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (supabaseManager.isSupabaseConfigured) "CLOUD PERSISTENCE: READY / ক্লাউড পোর্টাল রেডি" else "LOCAL PREVIEW MODE / লোকাল ডেমো মোড",
                                color = if (supabaseManager.isSupabaseConfigured) NeonGreen else SaffronColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (supabaseManager.isSupabaseConfigured) {
                                "Connected to real Supabase database. Sign-In/Sign-Up will authenticate live over the network.\nরিয়েল ডেটাবেসের সাথে কানেক্টেড আছে। রিয়েল টাইমে সাইন ইন/সাইন আপ হবে।"
                            } else {
                                "Using secure offline fallback because Supabase API keys are not set. Login bypass is enabled for testing.\nরিয়েল ডেটাবেস কী সেট করা নেই, তাই ডেমো মোডে চলছে। ড্যাশবোর্ড থেকে রিয়েল কী ইনপুট করা যাবে।"
                            },
                            color = TextWhite.copy(alpha = 0.8f),
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Authentication Card container
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GraphiteGray),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = NeonGreen.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isSignUpMode) "Create an Account" else "Log In to your Account",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSignUpMode) "Create a Cloud Sync Account" else "Log In to Synchronize Telemetry",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = TextGray)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = BorderGray,
                                focusedLabelColor = NeonGreen,
                                unfocusedLabelColor = TextGray,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = BorderGray,
                                focusedLabelColor = NeonGreen,
                                unfocusedLabelColor = TextGray,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Error Banner inside card
                        val errorToDisplay = authError ?: localErrorMsg
                        if (errorToDisplay != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CrimsonRed.copy(alpha = 0.15f))
                                    .border(1.dp, CrimsonRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = errorToDisplay,
                                    color = CrimsonRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                if (email.isEmpty() || password.isEmpty()) {
                                    localErrorMsg = "Please enter both Email and Password"
                                    return@Button
                                }
                                localErrorMsg = null
                                if (isSignUpMode) {
                                    supabaseManager.signUpWithEmail(email, password, onFailure = { localErrorMsg = it })
                                } else {
                                    supabaseManager.loginWithEmail(email, password, onFailure = { localErrorMsg = it })
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignUpMode) GraphiteGray else NeonGreen,
                                contentColor = if (isSignUpMode) TextWhite else Color(0xFF12161A)
                            ),
                            border = if (isSignUpMode) BorderStroke(1.dp, BorderGray) else null,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            enabled = !isAuthLoading
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = if (isSignUpMode) TextWhite else Color(0xFF12161A),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isSignUpMode) "REGISTER ACCOUNT" else "LOG IN",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Switch mode button
                        Text(
                            text = if (isSignUpMode) "Already have an account? Log In" else "New User? Create an Account",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    isSignUpMode = !isSignUpMode
                                    localErrorMsg = null
                                }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // OR Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(BorderGray))
                    Text(
                        text = "OR",
                        color = TextWhite.copy(alpha = 0.3f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(BorderGray))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // GOOGLE SIGN IN BUTTON (Saves compressed telemetry with Supabase)
                Button(
                    onClick = {
                        localErrorMsg = null
                        supabaseManager.loginWithGoogle(onFailure = { localErrorMsg = it })
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF12161A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_login_button"),
                    enabled = !isAuthLoading
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Official Multi-Colored Google Vector Logo
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Diagnostics warning at login
                if (!isInternetConnected) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Wifi Off",
                                tint = CrimsonRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Internet Offline!",
                                    color = CrimsonRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enable network connection to sync with Supabase Cloud Portal.",
                                    color = TextWhite.copy(alpha = 0.7f),
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
