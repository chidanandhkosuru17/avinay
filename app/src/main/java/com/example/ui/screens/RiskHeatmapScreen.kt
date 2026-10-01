package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RoadSegment
import com.example.ui.SafePathViewModel
import com.example.ui.components.MapVisualizer
import com.example.ui.components.SafetyIndicatorModalSheet
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskHeatmapScreen(
    viewModel: SafePathViewModel
) {
    val routes by viewModel.routes.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()
    val weights by viewModel.weights.collectAsState()
    val inspectedSegment by viewModel.inspectedSegment.collectAsState()
    val communityReports by viewModel.communityReports.collectAsState()

    var selectedFilter by remember { mutableStateOf("All Segments") }
    val filters = listOf("All Segments", "Lighting Deficits", "High Footfall", "Incident Hotspots")

    // Collect all unique road segments across routes
    val allSegments = remember(routes) {
        routes.flatMap { it.segments }.distinctBy { it.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        Text(
            text = "Continuous Environmental Risk Heatmap",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Aggregated street-level environmental indicators. SafePath evaluates granular road segments rather than labeling entire neighborhoods.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.sp
        )

        // Heatmap Legend Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeatmapLegendItem(color = ScoreGreen, label = "Low Risk", range = "Safety > 75")
                HeatmapLegendItem(color = ScoreAmber, label = "Moderate Risk", range = "Safety 55-75")
                HeatmapLegendItem(color = ScoreRed, label = "Higher Risk", range = "Safety < 55")
            }
        }

        // Map with Heatmap Layer Activated
        MapVisualizer(
            selectedRoute = selectedRoute,
            allRoutes = routes,
            isNightMode = isNightMode,
            showHeatmapLayer = true,
            inspectedSegment = inspectedSegment,
            onSegmentClick = { seg -> viewModel.inspectSegment(seg) },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) }
                )
            }
        }

        // Segment Inspection List
        Text(
            text = "Inspect Road Segments (Tap for Full Breakdown)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        val filteredSegments = when (selectedFilter) {
            "Lighting Deficits" -> allSegments.filter { it.lightingCoveragePct < 70 }
            "High Footfall" -> allSegments.filter { it.calculateCrowdScore() >= 80 }
            "Incident Hotspots" -> allSegments.filter { it.hasRecentIncidentReport || it.historicalIncidentScore < 70 }
            else -> allSegments
        }

        filteredSegments.forEach { segment ->
            val safetyScore = segment.calculateSafetyScore(weights, isNightMode)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.inspectSegment(segment) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = segment.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lighting: ${segment.lightingCoveragePct}% • Crowd: ${segment.crowdDensity.label.take(15)}...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            safetyScore >= 75 -> ScoreGreen.copy(alpha = 0.15f)
                            safetyScore >= 55 -> ScoreAmber.copy(alpha = 0.15f)
                            else -> ScoreRed.copy(alpha = 0.15f)
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$safetyScore/100",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    safetyScore >= 75 -> ScoreGreen
                                    safetyScore >= 55 -> ScoreAmber
                                    else -> ScoreRed
                                }
                            )
                            Text(
                                text = if (safetyScore >= 75) "Low Risk" else if (safetyScore >= 55) "Mod Risk" else "High Risk",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Prominent Ethical Disclaimer Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "“Scores are estimates based on available data and do not guarantee personal safety.” SafePath dynamically integrates crowd density, municipal lighting audits, and citizen reports without permanently stereotyping communities.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    if (inspectedSegment != null) {
        SafetyIndicatorModalSheet(
            segment = inspectedSegment,
            weights = weights,
            isNightMode = isNightMode,
            onDismiss = { viewModel.inspectSegment(null) }
        )
    }
}

@Composable
fun HeatmapLegendItem(color: Color, label: String, range: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = range, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
