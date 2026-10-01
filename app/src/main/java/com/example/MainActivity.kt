package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: SafePathViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isNightMode by viewModel.isNightMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val journeyState by viewModel.journeyState.collectAsState()

            MyApplicationTheme(darkTheme = isNightMode) {
                if (currentScreen == AppScreen.SETUP_WIZARD) {
                    SetupWizardScreen(
                        viewModel = viewModel,
                        onFinish = { viewModel.navigateTo(AppScreen.MAP_ROUTE_EXPLORER) }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(SafeSaffron, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "SafePath India",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp
                                            )
                                            Text(
                                                text = if (isNightMode) "Night-Time Safety Routing" else "Daytime Navigation",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    // Night mode toggle
                                    IconButton(onClick = { viewModel.toggleNightMode() }) {
                                        Icon(
                                            imageVector = if (isNightMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                            contentDescription = "Toggle Night Mode",
                                            tint = if (isNightMode) Color(0xFFFFD54F) else Color(0xFFFF9800)
                                        )
                                    }

                                    // Step-by-step Setup / Profile re-entry
                                    IconButton(onClick = { viewModel.navigateTo(AppScreen.SETUP_WIZARD) }) {
                                        Icon(
                                            imageVector = Icons.Default.ManageAccounts,
                                            contentDescription = "Edit Profile & Emergency Contact"
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.MAP_ROUTE_EXPLORER,
                                    onClick = { viewModel.navigateTo(AppScreen.MAP_ROUTE_EXPLORER) },
                                    icon = { Icon(Icons.Default.Directions, contentDescription = "Routes") },
                                    label = { Text("Routes", fontSize = 11.sp) }
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.RISK_HEATMAP,
                                    onClick = { viewModel.navigateTo(AppScreen.RISK_HEATMAP) },
                                    icon = { Icon(Icons.Default.Layers, contentDescription = "Heatmap") },
                                    label = { Text("Heatmap", fontSize = 11.sp) }
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.ACTIVE_JOURNEY,
                                    onClick = {
                                        if (!journeyState.isNavigating) {
                                            viewModel.startJourney(viewModel.selectedRoute.value ?: viewModel.routes.value.first())
                                        } else {
                                            viewModel.navigateTo(AppScreen.ACTIVE_JOURNEY)
                                        }
                                    },
                                    icon = {
                                        BadgedBox(badge = {
                                            if (journeyState.isCheckInDue) {
                                                Badge { Text("!") }
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.CheckCircleOutline,
                                                contentDescription = "Check-In",
                                                tint = if (journeyState.isCheckInDue) ScoreAmber else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    label = { Text("Check-In", fontSize = 11.sp) }
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.REPORT_ISSUE,
                                    onClick = { viewModel.navigateTo(AppScreen.REPORT_ISSUE) },
                                    icon = { Icon(Icons.Default.AddAlert, contentDescription = "Report") },
                                    label = { Text("Report", fontSize = 11.sp) }
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.ADMIN_DASHBOARD,
                                    onClick = { viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD) },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Admin") },
                                    label = { Text("Admin", fontSize = 11.sp) }
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.ETHICS_POLICY,
                                    onClick = { viewModel.navigateTo(AppScreen.ETHICS_POLICY) },
                                    icon = { Icon(Icons.Default.Policy, contentDescription = "Ethics") },
                                    label = { Text("Ethics", fontSize = 11.sp) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                AppScreen.MAP_ROUTE_EXPLORER -> MapRouteExplorerScreen(
                                    viewModel = viewModel,
                                    onNavigateTo = { screen -> viewModel.navigateTo(screen) }
                                )
                                AppScreen.RISK_HEATMAP -> RiskHeatmapScreen(
                                    viewModel = viewModel
                                )
                                AppScreen.ACTIVE_JOURNEY -> ActiveJourneyScreen(
                                    viewModel = viewModel,
                                    onStopJourney = { viewModel.stopJourney() }
                                )
                                AppScreen.REPORT_ISSUE -> CommunityReportScreen(
                                    viewModel = viewModel
                                )
                                AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(
                                    viewModel = viewModel
                                )
                                AppScreen.ETHICS_POLICY -> EthicsPolicyScreen()
                                else -> MapRouteExplorerScreen(
                                    viewModel = viewModel,
                                    onNavigateTo = { screen -> viewModel.navigateTo(screen) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
