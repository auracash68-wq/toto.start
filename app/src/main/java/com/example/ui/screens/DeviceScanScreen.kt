package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BluetoothBmsManager
import com.example.data.BmsDevice
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScanScreen(
    bmsManager: BluetoothBmsManager,
    onDeviceConnected: (BmsDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    val isScanning by bmsManager.isScanning.collectAsState()
    val devices by bmsManager.scannedDevices.collectAsState()

    val isBluetoothOn by bmsManager.isBluetoothOn.collectAsState()
    val isInternetOn by bmsManager.isInternetOn.collectAsState()
    val isPermissionGranted by bmsManager.isPermissionGranted.collectAsState()

    // Determine relevant runtime permissions based on Android Version
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    // Permission launcher to handle BLE gracefully at runtime
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms.values.all { it }
        bmsManager.updateDiagnostics(granted)
        bmsManager.startScan()
    }

    // Infinite rotation for scan animation
    val transition = rememberInfiniteTransition(label = "scan_rotation")
    val rotationAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Trigger initial scan under permissions check
    LaunchedEffect(Unit) {
        bmsManager.updateDiagnostics(true)
        launcher.launch(permissionsToRequest)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "TOTO START Bolt",
                            tint = Color(0xFFFF9933), // Saffron
                            modifier = Modifier.size(24.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTO ",
                                color = Color(0xFFFF9933), // Saffron
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "START",
                                color = Color(0xFF138808), // Green
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            bmsManager.updateDiagnostics(true)
                            launcher.launch(permissionsToRequest) 
                        },
                        modifier = Modifier.testTag("top_bar_refresh")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Scan",
                            tint = NeonGreen,
                            modifier = Modifier.rotate(if (isScanning) rotationAngle else 0f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepCharcoal
                ),
                modifier = Modifier.border(0.dp, Color.Transparent) // Borderless look
            )
        },
        containerColor = DeepCharcoal,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Section Title
            Text(
                text = "Available Devices",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle indicating scanning status
            Text(
                text = if (isScanning) "Scanning for nearby power management systems..." else "Scan finished. Found ${devices.size} systems.",
                color = TextGray,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Professional System Status Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GraphiteGray)
                    .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "SYSTEM DIAGNOSTICS",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                // 1. Bluetooth Check
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isBluetoothOn) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isBluetoothOn) NeonGreen else CrimsonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Bluetooth Adapter",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = if (isBluetoothOn) "ACTIVE" else "DISABLED",
                        color = if (isBluetoothOn) NeonGreen else CrimsonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 2. Internet Check
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isInternetOn) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isInternetOn) NeonGreen else CrimsonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Supabase Cloud Database",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = if (isInternetOn) "ONLINE" else "OFFLINE",
                        color = if (isInternetOn) NeonGreen else CrimsonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3. Permissions Check
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isPermissionGranted) NeonGreen else CrimsonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Hardware Permissions",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = if (isPermissionGranted) "GRANTED" else "DENIED",
                        color = if (isPermissionGranted) NeonGreen else CrimsonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Warnings with exact troubleshooting guidance
                if (!isBluetoothOn || !isInternetOn || !isPermissionGranted) {
                    Divider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isBluetoothOn) {
                            Text(
                                text = "⚠️ Bluetooth is disabled. Please turn on Bluetooth in your system settings to connect to Daly BMS.",
                                color = CrimsonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!isInternetOn) {
                            Text(
                                text = "⚠️ No internet connection. Please enable network data or Wi-Fi to synchronize telemetry with Supabase.",
                                color = CrimsonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!isPermissionGranted) {
                            Text(
                                text = "⚠️ Bluetooth scan & location permissions required. Press the refresh button to grant permissions.",
                                color = CrimsonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Devices list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (devices.isEmpty() && isScanning) {
                    // Show a neat scanning empty state with loading spinner
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = NeonGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Searching airwaves...",
                            color = TextGray,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(devices) { device ->
                            DeviceRowItem(
                                device = device,
                                onConnectTap = {
                                    bmsManager.connectToBms(device)
                                    onDeviceConnected(device)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom action: SCAN AGAIN
            Button(
                onClick = { launcher.launch(permissionsToRequest) },
                enabled = !isScanning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GraphiteGray,
                    disabledContainerColor = GraphiteGray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .navigationBarsPadding() // Respects navigation bar notch safely!
                    .testTag("scan_again_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan Icon",
                        tint = if (isScanning) TextGray else TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "SCAN AGAIN",
                        color = if (isScanning) TextGray else TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceRowItem(
    device: BmsDevice,
    onConnectTap: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = GraphiteGray
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .testTag("device_card_${device.address}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circular Square Bluetooth Icon Container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepCharcoal)
                        .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth Device",
                        tint = NeonGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Name and Address info
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = device.name,
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = device.address,
                        color = TextGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // CONNECT button
            Button(
                onClick = onConnectTap,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(38.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(10.dp),
                        clip = false,
                        ambientColor = NeonGreen,
                        spotColor = NeonGreen
                    )
                    .testTag("connect_btn_${device.address}")
            ) {
                Text(
                    text = "CONNECT",
                    color = DeepCharcoal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
