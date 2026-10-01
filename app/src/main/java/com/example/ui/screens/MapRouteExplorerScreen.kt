package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.AppScreen
import com.example.ui.SafePathViewModel
import com.example.ui.components.MapVisualizer
import com.example.ui.components.SafetyIndicatorModalSheet
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapRouteExplorerScreen(
    viewModel: SafePathViewModel,
    onNavigateTo: (AppScreen) -> Unit
) {
    val routes by viewModel.routes.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val weights by viewModel.weights.collectAsState()
    val inspectedSegment by viewModel.inspectedSegment.collectAsState()
    val activeScenario by viewModel.activeScenario.collectAsState()
    val originAddress by viewModel.repository.originAddress.collectAsState()
    val destinationAddress by viewModel.repository.destinationAddress.collectAsState()
    val context = LocalContext.current

    var showWeightsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Search & Route Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // From address row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(SafeTeal, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Current Location", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = originAddress, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.SETUP_WIZARD) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Addresses", modifier = Modifier.size(16.dp))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                // To destination row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(SafeGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Destination", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = destinationAddress, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                    IconButton(
                        onClick = { viewModel.toggleNightMode() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                            contentDescription = "Night Mode Toggle",
                            tint = if (isNightMode) Color(0xFFFFD54F) else Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Map View Component (Google Maps SDK + Canvas Layer)
        MapVisualizer(
            selectedRoute = selectedRoute,
            allRoutes = routes,
            isNightMode = isNightMode,
            showHeatmapLayer = false,
            inspectedSegment = inspectedSegment,
            onSegmentClick = { seg -> viewModel.inspectSegment(seg) },
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        // Emergency Parent Contact Bar (Replaces SOS Button as requested)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, SafeTeal.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SafeTeal.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, tint = SafeTeal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Emergency Contact: ${userProfile.emergencyContactName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userProfile.emergencyContactPhone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = { viewModel.sendEmergencySms(context) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = "Send SMS", tint = SafeTeal, modifier = Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(
                        onClick = { viewModel.callEmergencyContact(context) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call Contact", tint = SafeGreen, modifier = Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(
                        onClick = { viewModel.callIndiaERSS112(context) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("112", fontSize = 11.sp, fontWeight = FontWeight.Black, color = SafeSaffronDark)
                    }
                }
            }
        }

        // Route Selection Tabs / Options Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Candidate Routes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { showWeightsDialog = true }) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tune Weights", fontSize = 12.sp)
            }
        }

        // Display Candidate Routes: Safer, Fastest, Balanced
        routes.forEach { route ->
            val isSelected = route.id == selectedRoute?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.selectRoute(route) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SafeGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (route.type) {
                                    RouteType.SAFER -> Icons.Default.Shield
                                    RouteType.FASTEST -> Icons.Default.ElectricBolt
                                    RouteType.BALANCED -> Icons.Default.Balance
                                },
                                contentDescription = null,
                                tint = when (route.type) {
                                    RouteType.SAFER -> ScoreGreen
                                    RouteType.FASTEST -> ScoreAmber
                                    RouteType.BALANCED -> Color(0xFF3B82F6)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = route.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (route.isRecommended) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ScoreGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "HIGHER SAFETY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScoreGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Route metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "${route.distanceKm} km", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Distance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column {
                            Text(text = "${route.travelTimeMin} min", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Travel Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column {
                            Text(
                                text = "${route.safetyScore}/100",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    route.safetyScore >= 75 -> ScoreGreen
                                    route.safetyScore >= 55 -> ScoreAmber
                                    else -> ScoreRed
                                }
                            )
                            Text(text = "Safety Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = route.riskLevel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (route.riskLevel) {
                                    "Lower Risk" -> ScoreGreen
                                    "Moderate Risk" -> ScoreAmber
                                    else -> ScoreRed
                                }
                            )
                            Text(text = "Risk Level", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = route.explanation,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Detailed Comparison Card: Why Route B is safer than Route A
        val saferRoute = routes.find { it.type == RouteType.SAFER }
        val fastestRoute = routes.find { it.type == RouteType.FASTEST }
        if (saferRoute != null && fastestRoute != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = SafeTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Route Comparison: Efficiency vs Safety",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Route A (Fastest): 2.8 km, 11 mins | Safety Score: 62/100 (Moderate Risk)\n" +
                                "• Route B (Safer): 3.2 km, 14 mins | Safety Score: 86/100 (Lower Risk)\n\n" +
                                "Why Route B has a higher safety score:\n" +
                                "Route B takes 100 Feet Main Road which is 3 minutes slower, but features 95% continuous streetlight illumination, active storefronts and food stalls, and an emergency police post, compared to the unlit back-lane on Route A.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Start Journey Button
        Button(
            onClick = {
                val route = selectedRoute ?: routes.first()
                viewModel.startJourney(route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
        ) {
            Icon(Icons.Default.Navigation, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Navigation with Safe Check-In", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        // Mandatory Ethical Disclaimer
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scores are estimates based on available data and do not guarantee personal safety.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Modal Inspector Sheet if segment clicked
    if (inspectedSegment != null) {
        SafetyIndicatorModalSheet(
            segment = inspectedSegment,
            weights = weights,
            isNightMode = isNightMode,
            onDismiss = { viewModel.inspectSegment(null) }
        )
    }

    // Weights Customizer Dialog
    if (showWeightsDialog) {
        WeightsTuningDialog(
            currentWeights = weights,
            onApply = { newWeights ->
                viewModel.updateWeights(newWeights)
                showWeightsDialog = false
            },
            onDismiss = { showWeightsDialog = false }
        )
    }
}

@Composable
fun WeightsTuningDialog(
    currentWeights: SafetyWeights,
    onApply: (SafetyWeights) -> Unit,
    onDismiss: () -> Unit
) {
    var lighting by remember { mutableFloatStateOf(currentWeights.lighting.toFloat()) }
    var crowd by remember { mutableFloatStateOf(currentWeights.crowdDensity.toFloat()) }
    var history by remember { mutableFloatStateOf(currentWeights.historicalReports.toFloat()) }
    var alphaTime by remember { mutableFloatStateOf(currentWeights.alphaTime.toFloat()) }
    var betaRisk by remember { mutableFloatStateOf(currentWeights.betaRisk.toFloat()) }
    var gammaDist by remember { mutableFloatStateOf(currentWeights.gammaDistance.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurable Safety & Routing Weights", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Customize the environmental weighting formula according to personal preference:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                WeightSliderItem(label = "Street Lighting (25%)", value = lighting, onValueChange = { lighting = it })
                WeightSliderItem(label = "Crowd Density (20%)", value = crowd, onValueChange = { crowd = it })
                WeightSliderItem(label = "Historical Reports (25%)", value = history, onValueChange = { history = it })

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Routing Cost: α × Time + β × Risk + γ × Distance", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                WeightSliderItem(label = "α (Travel Time)", value = alphaTime, onValueChange = { alphaTime = it })
                WeightSliderItem(label = "β (Safety Risk)", value = betaRisk, onValueChange = { betaRisk = it })
                WeightSliderItem(label = "γ (Distance)", value = gammaDist, onValueChange = { gammaDist = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(
                        currentWeights.copy(
                            lighting = lighting.toDouble(),
                            crowdDensity = crowd.toDouble(),
                            historicalReports = history.toDouble(),
                            alphaTime = alphaTime.toDouble(),
                            betaRisk = betaRisk.toDouble(),
                            gammaDistance = gammaDist.toDouble()
                        )
                    )
                }
            ) {
                Text("Apply & Recalculate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WeightSliderItem(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("${(value * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.05f..0.60f
        )
    }
}
