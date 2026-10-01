package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.ui.SafePathViewModel
import com.example.ui.components.MapVisualizer
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: SafePathViewModel
) {
    val routes by viewModel.routes.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()
    val communityReports by viewModel.communityReports.collectAsState()

    var selectedFilterCategory by remember { mutableStateOf("All Categories") }
    var selectedTimeFilter by remember { mutableStateOf("Night (8PM - 5AM)") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Admin Badge & Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Smart City Municipal Safety Portal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Civic Infrastructure & Lighting Deficiency Analytics",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SafeTeal.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "ADMIN PORTAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeTeal,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Key Municipal Metrics 4-grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminMetricCard(
                title = "Deficient Streetlights",
                value = "14 Poles",
                subtitle = "3 Workorders Auto-Issued",
                icon = Icons.Default.Lightbulb,
                accent = ScoreAmber,
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "Total Citizen Reports",
                value = "${communityReports.size + 19}",
                subtitle = "94% Moderated",
                icon = Icons.Default.ReportProblem,
                accent = SafeSaffronDark,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminMetricCard(
                title = "Safe Route Adoptions",
                value = "82.4%",
                subtitle = "+18% this month",
                icon = Icons.Default.TrendingUp,
                accent = ScoreGreen,
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "ERSS 112 Kiosks",
                value = "6 Active",
                subtitle = "Average 4.2 min response",
                icon = Icons.Default.LocalPolice,
                accent = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
        }

        // Mini Heatmap Preview
        Text("Active Corridor Risk Density", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        MapVisualizer(
            selectedRoute = routes.firstOrNull(),
            allRoutes = routes,
            isNightMode = isNightMode,
            showHeatmapLayer = true,
            inspectedSegment = null,
            onSegmentClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // Filters Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Municipal Audit Filters", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All Categories", "Lighting", "Harassment", "Potholes").forEach { cat ->
                        FilterChip(
                            selected = selectedFilterCategory == cat,
                            onClick = { selectedFilterCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // High Priority Civic Action Items (Hotspots to fix)
        Text("High-Priority Infrastructure Fixes Needed", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        CivicActionItem(
            location = "Ejipura Inner Transit Bypass (SEG-FAST-1)",
            issue = "Lighting coverage at 35% with 4 broken luminaire heads.",
            action = "Dispatching BESCOM municipal repair van (Scheduled 10:00 AM)",
            severity = "High",
            severityColor = ScoreRed
        )

        CivicActionItem(
            location = "Ring Road Underpass Junction",
            issue = "Blind turn with low footfall between 9 PM and 2 AM.",
            action = "Requesting Bengaluru City Police ERSS mobile patrol car post",
            severity = "Medium",
            severityColor = ScoreAmber
        )

        CivicActionItem(
            location = "Intermediate Link Road Pedestrian Walkway",
            issue = "Sidewalk obstruction by construction gravel.",
            action = "BBMP encroachment clearance notification dispatched",
            severity = "Low",
            severityColor = SafeTeal
        )
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = accent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CivicActionItem(
    location: String,
    issue: String,
    action: String,
    severity: String,
    severityColor: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(location, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = severityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = severity,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(issue, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Engineering, contentDescription = null, tint = SafeTeal, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(action, fontSize = 11.sp, color = SafeTeal, fontWeight = FontWeight.Medium)
            }
        }
    }
}
