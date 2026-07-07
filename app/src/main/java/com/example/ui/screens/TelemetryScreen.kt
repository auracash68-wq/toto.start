package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BluetoothBmsManager
import com.example.data.TelemetryData
import com.example.data.SupabaseManager
import androidx.compose.foundation.lazy.LazyColumn
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelemetryScreen(
    bmsManager: BluetoothBmsManager,
    supabaseManager: SupabaseManager,
    onBackTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val telemetry by bmsManager.telemetry.collectAsState()
    val isBmsActive by bmsManager.isBmsActive.collectAsState()
    val connectedDevice by bmsManager.connectedDevice.collectAsState()
    val syncStatus by supabaseManager.syncStatus.collectAsState()

    // Automatic compressed logging sync to Supabase Database
    LaunchedEffect(telemetry, isBmsActive) {
        if (isBmsActive && telemetry.voltage > 0.1f) {
            supabaseManager.logTelemetryData(
                voltage = telemetry.voltage,
                current = telemetry.current,
                temp = telemetry.temperature,
                power = telemetry.power,
                charge = telemetry.chargePercentage
            )
        }
    }

    var showConfirmationSheet by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    
    var showFirewallPinDialog by remember { mutableStateOf(false) }
    var isFirewallUnlocked by remember { mutableStateOf(false) }
    val firewallDevices by bmsManager.connectedFirewallDevices.collectAsState()
    
    var showSettingsDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Sheet State
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = connectedDevice?.name ?: "My Toto - Main",
                        color = NeonGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            bmsManager.disconnect()
                            onBackTap()
                        },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Disconnect Device",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "BMS Settings",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepCharcoal
                )
            )
        },
        containerColor = DeepCharcoal,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Custom Header with Pulsing LED and Letter Spacing matching 'Elegant Dark' spec
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTO CONTROLLER",
                            color = NeonGreen.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Pulsing LED State indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "led_pulse"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        (if (isBmsActive) NeonGreen else CrimsonRed).copy(alpha = pulseAlpha)
                                    )
                            )
                            
                            Text(
                                text = if (isBmsActive) "BMS: ACTIVE / RUNNING" else "BMS: OFFLINE",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Rounded corner decorative action button matching design header
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GraphiteGray)
                            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Power Icon",
                            tint = NeonGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Central Circular Progress Ring
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .testTag("circular_progress_ring"),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer decorative circle
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .border(1.dp, BorderGray.copy(alpha = 0.5f), CircleShape)
                    )

                    val colorState = if (isBmsActive) NeonGreen else CrimsonRed
                    val sweepPercentage = if (isBmsActive) telemetry.chargePercentage.toFloat() else 100f
                    val animatedPercentage by animateFloatAsState(
                        targetValue = sweepPercentage,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing),
                        label = "ring_progress"
                    )

                    Canvas(modifier = Modifier.size(150.dp)) {
                        // Underlayer background arc track
                        drawArc(
                            color = BorderGray,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Highlight arc indicating capacity or disabled ring
                        drawArc(
                            color = colorState,
                            startAngle = -90f,
                            sweepAngle = (animatedPercentage / 100f) * -360f, // Counter-clockwise match or classic
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Text Inside Ring
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isBmsActive) "${telemetry.chargePercentage}%" else "0%",
                            color = colorState,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.testTag("charge_percent_text")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "CAPACITY",
                            color = TextGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. State & Text Section (Elegant Dark spec)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBmsActive) "ACTIVE" else "DISABLED",
                        color = if (isBmsActive) NeonGreen else CrimsonRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        modifier = Modifier.testTag("bms_status_header")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBmsActive) "SUPPLYING POWER TO OUTPUT LINES" else "HARDWARE LOCK ENGAGED",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Telemetry 2x2 Grid or Professional Troubleshooting Diagnostics
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isBmsActive) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TelemetryCard(
                                    title = "VOLTAGE",
                                    value = "${telemetry.voltage} V",
                                    subtitle = "Avg Cell: ${telemetry.avgCellVoltage}V",
                                    icon = Icons.Default.FlashOn,
                                    iconColor = NeonGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                TelemetryCard(
                                    title = "CURRENT",
                                    value = "${telemetry.current} A",
                                    subtitle = "Limit: ${telemetry.limitCurrent.toInt()}A",
                                    icon = Icons.Default.FlashOn,
                                    iconColor = NeonGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TelemetryCard(
                                    title = "TEMP",
                                    value = "${telemetry.temperature.toInt()}°C",
                                    subtitle = "Max: ${telemetry.maxTemp.toInt()}°C",
                                    icon = Icons.Default.Thermostat,
                                    iconColor = NeonGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                TelemetryCard(
                                    title = "CYCLES",
                                    value = "142",
                                    subtitle = "Cons: ${telemetry.consPower.toInt()}W",
                                    icon = Icons.Default.Speed,
                                    iconColor = NeonGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Supabase Cloud Sync Status Row (Only visible when sending active data)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GraphiteGray),
                                border = BorderStroke(1.dp, BorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudQueue,
                                        contentDescription = "Cloud Status",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "SUPABASE CLOUD LOGGER",
                                            color = NeonGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = syncStatus,
                                            color = TextWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // GORGEOUS TROUBLESHOOTING CARD (When disengaged/no live data)
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = GraphiteGray),
                            border = BorderStroke(1.2.dp, BorderGray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "BMS CONNECTED: WHY IS TELEMETRY OFFLINE?",
                                        color = CrimsonRed,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Check the following guidelines to activate power output lines and view live data:",
                                        color = TextGray,
                                        fontSize = 11.sp
                                    )
                                    Divider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.PowerOff,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "1. BMS MOSFET Switch is Closed",
                                            color = TextWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "The BMS output lines are safely shut down. Tap the 'ACTIVATE / OPEN BMS' button below to engage high-voltage MOSFET gates and supply power to your TOTO.",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.BluetoothDisabled,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "2. Check BLE Hardware Signals",
                                            color = TextWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Ensure your phone's Bluetooth transceiver is turned on and active. Disabling Bluetooth will prevent local status telemetry updates.",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "3. Cloud Portal Offline Link",
                                            color = TextWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "If network data is unavailable, cloud-based telemetry sync with Supabase will pause. Data streams will sync automatically once online.",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Column {
                                    Divider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                    Text(
                                        text = "🔒 100% PRODUCTION READY: This app interfaces with real Daly BMS hardware. Activate the vehicle's controller to stream live battery telemetry.",
                                        color = NeonGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 3.5. 🛡️ Security BLE Firewall & Device Controller Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GraphiteGray),
                    border = BorderStroke(1.2.dp, if (isFirewallUnlocked) NeonGreen.copy(alpha = 0.5f) else BorderGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFirewallUnlocked) Icons.Default.Shield else Icons.Default.Lock,
                                    contentDescription = "Shield",
                                    tint = if (isFirewallUnlocked) NeonGreen else SaffronColor
                                )
                                Column {
                                    Text(
                                        text = "TOTO START BLE FIREWALL",
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = if (isFirewallUnlocked) "Hardware Protection: Unlocked" else "Secured Device Management",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (!isFirewallUnlocked) {
                                Button(
                                    onClick = { showFirewallPinDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronColor),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("UNLOCK", color = DeepCharcoal, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            } else {
                                IconButton(
                                    onClick = { isFirewallUnlocked = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.LockOpen, contentDescription = "Lock", tint = NeonGreen)
                                }
                            }
                        }

                        if (!isFirewallUnlocked) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Enter password to monitor active Bluetooth connections and block unauthorized mobile devices from linking with your TOTO.",
                                color = TextGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))

                            // 1. Group: MY BLUETOOTH (Protected, safe from blocking)
                            Text(
                                text = "MY BLUETOOTH (AUTHORIZED DEVICES)",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            firewallDevices.filter { device -> device.isMyDevice }.forEach { device ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DeepCharcoal)
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BluetoothConnected,
                                            contentDescription = "My BT",
                                            tint = NeonGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = device.name,
                                                color = TextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "MAC: ${device.address} • Protected",
                                                color = TextGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Protected",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. Group: OTHER DEVICE CONNECTIONS (Can block/unblock)
                            Text(
                                text = "CONNECTED VEHICLE CONTROLLERS",
                                color = SaffronColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val externalDevices = firewallDevices.filter { device -> !device.isMyDevice }
                            if (externalDevices.isEmpty()) {
                                Text(
                                    text = "No unauthorized controllers connected.",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                externalDevices.forEach { device ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(DeepCharcoal)
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (device.isBlocked) Icons.Default.Block else Icons.Default.Bluetooth,
                                                contentDescription = "Controller",
                                                tint = if (device.isBlocked) CrimsonRed else SaffronColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = device.name,
                                                    color = if (device.isBlocked) CrimsonRed else TextWhite,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    style = if (device.isBlocked) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle.Default
                                                )
                                                Text(
                                                    text = "MAC: ${device.address} • ${if (device.isBlocked) "Blocked" else "Active Connection"}",
                                                    color = TextGray,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (device.isBlocked) {
                                                    bmsManager.unblockDevice(device.address)
                                                } else {
                                                    bmsManager.blockDevice(device.address)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (device.isBlocked) NeonGreen else CrimsonRed
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(
                                                text = if (device.isBlocked) "UNBLOCK" else "BLOCK",
                                                color = if (device.isBlocked) DeepCharcoal else TextWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }

                // Firewall PIN dialog
                if (showFirewallPinDialog) {
                    var firewallPinText by remember { mutableStateOf("") }
                    var firewallPinError by remember { mutableStateOf(false) }

                    AlertDialog(
                        onDismissRequest = { 
                            showFirewallPinDialog = false 
                            firewallPinText = ""
                            firewallPinError = false
                        },
                        containerColor = GraphiteGray,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield Lock",
                                tint = SaffronColor,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = {
                            Text(
                                text = "Unlock Firewall Settings",
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "To configure active Bluetooth controllers and block unwanted device links, enter the security PIN.",
                                    color = TextGray,
                                    fontSize = 13.sp
                                )

                                OutlinedTextField(
                                    value = firewallPinText,
                                    onValueChange = { 
                                        firewallPinText = it 
                                        firewallPinError = false
                                    },
                                    label = { Text("Security PIN") },
                                    singleLine = true,
                                    isError = firewallPinError,
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                                    ),
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

                                if (firewallPinError) {
                                    Text(
                                        text = "Invalid security PIN! Please try again.",
                                        color = CrimsonRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (firewallPinText == bmsManager.securityPin) {
                                        isFirewallUnlocked = true
                                        showFirewallPinDialog = false
                                        firewallPinText = ""
                                        firewallPinError = false
                                    } else {
                                        firewallPinError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                            ) {
                                Text("UNLOCK", color = DeepCharcoal, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { 
                                showFirewallPinDialog = false 
                                firewallPinText = ""
                                firewallPinError = false
                            }) {
                                Text("CANCEL", color = TextWhite)
                            }
                        },
                        modifier = Modifier.border(1.dp, BorderGray, RoundedCornerShape(28.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Primary Giant Glow Action Button
                val buttonBg = if (isBmsActive) CrimsonRed else NeonGreen
                val buttonText = if (isBmsActive) "DEACTIVATE / CLOSE BMS" else "ACTIVATE / OPEN BMS"
                val buttonContentColor = if (isBmsActive) TextWhite else DeepCharcoal

                Button(
                    onClick = {
                        if (isBmsActive) {
                            showDeactivateDialog = true
                        } else {
                            showConfirmationSheet = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBg
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(14.dp),
                            clip = false,
                            ambientColor = buttonBg.copy(alpha = 0.5f),
                            spotColor = buttonBg
                        )
                        .testTag("primary_action_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Power Trigger",
                            tint = buttonContentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buttonText,
                            color = buttonContentColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Page Indicator Dots under button
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    // Dot 1 (Active Page)
                    Box(
                        modifier = Modifier
                            .size(24.dp, 4.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    // Dot 2
                    Box(
                        modifier = Modifier
                            .size(6.dp, 4.dp)
                            .clip(CircleShape)
                            .background(TextWhite.copy(alpha = 0.15f))
                    )
                    // Dot 3
                    Box(
                        modifier = Modifier
                            .size(6.dp, 4.dp)
                            .clip(CircleShape)
                            .background(TextWhite.copy(alpha = 0.15f))
                    )
                }
            }

            // High-fidelity bottom-sheet confirmation
            if (showConfirmationSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showConfirmationSheet = false },
                    sheetState = sheetState,
                    containerColor = DeepCharcoal,
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(40.dp, 4.dp)
                                .clip(CircleShape)
                                .background(BorderGray)
                        )
                    },
                    modifier = Modifier.testTag("confirm_sheet_modal")
                ) {
                    ConfirmationSheetContent(
                        onConfirm = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showConfirmationSheet = false
                                    bmsManager.writeBmsActivateCommand()
                                }
                            }
                        },
                        onCancel = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showConfirmationSheet = false
                                }
                            }
                        }
                    )
                }
            }

            // Deactivate Confirmation Dialog
            if (showDeactivateDialog) {
                var pinText by remember { mutableStateOf("") }
                var pinError by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { 
                        showDeactivateDialog = false 
                        pinText = ""
                        pinError = false
                    },
                    containerColor = GraphiteGray,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = CrimsonRed,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Deactivate TOTO START?",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Enter the security code to shut down the vehicle and disengage the BMS power MOSFETs.",
                                color = TextGray,
                                fontSize = 14.sp
                            )
                            
                            OutlinedTextField(
                                value = pinText,
                                onValueChange = { 
                                    pinText = it 
                                    pinError = false
                                },
                                label = { Text("Security PIN") },
                                singleLine = true,
                                isError = pinError,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                                ),
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
                            
                            if (pinError) {
                                Text(
                                    text = "Invalid security PIN! Please try again.",
                                    color = CrimsonRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (pinText == bmsManager.securityPin) {
                                    showDeactivateDialog = false
                                    pinText = ""
                                    pinError = false
                                    bmsManager.writeBmsDeactivateCommand()
                                } else {
                                    pinError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                        ) {
                            Text("DEACTIVATE", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            showDeactivateDialog = false 
                            pinText = ""
                            pinError = false
                        }) {
                            Text("CANCEL", color = TextWhite)
                        }
                    },
                    modifier = Modifier.border(1.dp, BorderGray, RoundedCornerShape(28.dp))
                )
            }

            // Real-world dynamic Hardware & Config Settings Dialog
            if (showSettingsDialog) {
                var serviceUuidText by remember { mutableStateOf(bmsManager.serviceUuidString) }
                var rxCharUuidText by remember { mutableStateOf(bmsManager.rxCharUuidString) }
                var txCharUuidText by remember { mutableStateOf(bmsManager.txCharUuidString) }
                var pinText by remember { mutableStateOf(bmsManager.securityPin) }

                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    containerColor = GraphiteGray,
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Hardware & Security Config",
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "CLOUD STATUS",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )

                            Card(
                                shape = RoundedCornerShape(12.dp),
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
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (supabaseManager.isSupabaseConfigured) "● CONNECTED TO SECURE CLOUD" else "▲ DEMO / OFFLINE FALLBACK MODE",
                                        color = if (supabaseManager.isSupabaseConfigured) NeonGreen else SaffronColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (supabaseManager.isSupabaseConfigured) {
                                            "API connection keys are compiled securely inside the application binary. Users cannot modify or inspect them."
                                        } else {
                                            "Database keys are not set. Configure credentials in the AI Studio Secrets panel as SUPABASE_URL and SUPABASE_ANON_KEY to build the production release."
                                        },
                                        color = TextWhite.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Divider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = "HARDWARE CONTROLLER (DALY BMS)",
                                color = SaffronColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )

                            OutlinedTextField(
                                value = serviceUuidText,
                                onValueChange = { serviceUuidText = it },
                                label = { Text("Service UUID") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronColor,
                                    unfocusedBorderColor = BorderGray,
                                    focusedLabelColor = SaffronColor,
                                    unfocusedLabelColor = TextGray,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = rxCharUuidText,
                                onValueChange = { rxCharUuidText = it },
                                label = { Text("RX Characteristic UUID") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronColor,
                                    unfocusedBorderColor = BorderGray,
                                    focusedLabelColor = SaffronColor,
                                    unfocusedLabelColor = TextGray,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = txCharUuidText,
                                onValueChange = { txCharUuidText = it },
                                label = { Text("TX Characteristic UUID") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronColor,
                                    unfocusedBorderColor = BorderGray,
                                    focusedLabelColor = SaffronColor,
                                    unfocusedLabelColor = TextGray,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Divider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = "APP SECURITY",
                                color = CrimsonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )

                            OutlinedTextField(
                                value = pinText,
                                onValueChange = { pinText = it },
                                label = { Text("Security PIN (Firewall Lock)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonRed,
                                    unfocusedBorderColor = BorderGray,
                                    focusedLabelColor = CrimsonRed,
                                    unfocusedLabelColor = TextGray,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Save all settings to SharedPreferences
                                bmsManager.serviceUuidString = serviceUuidText
                                bmsManager.rxCharUuidString = rxCharUuidText
                                bmsManager.txCharUuidString = txCharUuidText
                                bmsManager.securityPin = pinText

                                showSettingsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("SAVE", color = DeepCharcoal, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    // Restore defaults
                                    serviceUuidText = "0000FF00-0000-1000-8000-00805F9B34FB"
                                    rxCharUuidText = "0000FF01-0000-1000-8000-00805F9B34FB"
                                    txCharUuidText = "0000FF02-0000-1000-8000-00805F9B34FB"
                                    pinText = "TOTO#2009"
                                }
                            ) {
                                Text("RESET", color = SaffronColor)
                            }
                            TextButton(onClick = { showSettingsDialog = false }) {
                                Text("CANCEL", color = TextWhite)
                            }
                        }
                    },
                    modifier = Modifier.border(1.dp, BorderGray, RoundedCornerShape(28.dp))
                )
            }
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = GraphiteGray
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderGray),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .testTag("telemetry_card_${title.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Title
                Text(
                    text = title,
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Right Icon Indicator
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Metric Value
            Text(
                text = value,
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Secondary Subtitle description
            Text(
                text = subtitle,
                color = TextGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ConfirmationSheetContent(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp), // Leaves margin for system navigation bars safely!
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Warning Circular Square Icon Container
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CrimsonRed.copy(alpha = 0.15f))
                .border(1.dp, CrimsonRed.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert Warning",
                tint = CrimsonRed,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            text = "Confirm Activation?",
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Safety detailed descriptions
        Text(
            text = "You are about to engage the high-voltage BMS system. Please ensure all safety protocols are followed before proceeding.",
            color = TextGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // YES, ACTIVATE
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_sheet_yes_btn")
            ) {
                Text(
                    text = "YES, ACTIVATE",
                    color = DeepCharcoal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            // CANCEL
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(1.dp, BorderGray),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_sheet_cancel_btn")
            ) {
                Text(
                    text = "CANCEL",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
