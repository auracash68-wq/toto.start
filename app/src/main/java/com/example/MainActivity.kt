package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.BluetoothBmsManager
import com.example.data.SupabaseManager
import com.example.ui.screens.DeviceScanScreen
import com.example.ui.screens.TelemetryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val bmsManager = remember { BluetoothBmsManager(applicationContext) }
                val supabaseManager = remember { SupabaseManager(applicationContext) }
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "auth_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("auth_screen") {
                            LoginScreen(
                                supabaseManager = supabaseManager,
                                onLoginSuccess = {
                                    navController.navigate("device_discovery") {
                                        popUpTo("auth_screen") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("device_discovery") {
                            DeviceScanScreen(
                                bmsManager = bmsManager,
                                onDeviceConnected = { device ->
                                    navController.navigate("telemetry_dashboard") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable("telemetry_dashboard") {
                            TelemetryScreen(
                                bmsManager = bmsManager,
                                supabaseManager = supabaseManager,
                                onBackTap = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

