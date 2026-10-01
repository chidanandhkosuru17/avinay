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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DemoScenario
import com.example.model.RouteType
import com.example.ui.AppScreen
import com.example.ui.SafePathViewModel
import com.example.ui.theme.*

@Composable
fun DemoScenariosScreen(
    viewModel: SafePathViewModel,
    onNavigateTo: (AppScreen) -> Unit
) {
    val activeScenario by viewModel.activeScenario.collectAsState()
    val routes by viewModel.routes.collectAsState()

    val fasterRoute = routes.find { it.type == RouteType.FASTEST }
    val saferRoute = routes.find { it.type == RouteType.SAFER }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Case Study & Scenarios Demonstration",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Explore how changing environmental indicators, community reports, and time of day dynamically adapt safety scores and route recommendations.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.sp
        )

        // Case Study Scenario Switcher Cards (Scenarios 1 to 5)
        DemoScenario.values().forEachIndexed { index, scenario ->
            val isSelected = activeScenario == scenario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.selectScenario(scenario) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SafeTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isSelected) SafeTeal else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scenario.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = scenario.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }

                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeTeal)
                    }
                }
            }
        }

        // Active Scenario Real-Time Impact Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, SafeSaffron.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = SafeSaffronDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Scenario Impact Analysis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Fastest Route (A)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${fasterRoute?.safetyScore ?: 62}/100",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ScoreAmber
                        )
                        Text("${fasterRoute?.distanceKm} km • ${fasterRoute?.travelTimeMin} min", fontSize = 11.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Safer Alternative (B)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${saferRoute?.safetyScore ?: 86}/100",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ScoreGreen
                        )
                        Text("${saferRoute?.distanceKm} km • ${saferRoute?.travelTimeMin} min", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (activeScenario) {
                        DemoScenario.SCENARIO_1_POOR_LIGHTING ->
                            "Variable Impact: Fastest route penalised (-28 pts) due to 35% street lighting coverage on Ejipura bypass."
                        DemoScenario.SCENARIO_2_SAFER_ALTERNATIVE ->
                            "Variable Impact: Route B gains +24 pts due to 95% continuous lighting and high commercial footfall on 100ft road despite +3 mins extra travel."
                        DemoScenario.SCENARIO_3_HISTORICAL_SPIKE ->
                            "Variable Impact: 2 recent harassment reports logged in local database reduced the historical indicator score from 52 to 35 on the shortcut."
                        DemoScenario.SCENARIO_4_LIGHTING_OUTAGE ->
                            "Variable Impact: 4 real-time community reports of luminaire failure reduced lighting score to 20% on the secondary link."
                        DemoScenario.SCENARIO_5_JOURNEY_MONITORING ->
                            "Variable Impact: Journey monitoring initialized with automated checkpoint alert and direct SMS to emergency parent contact."
                    },
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Button to open Map or Active Journey
        Button(
            onClick = {
                if (activeScenario == DemoScenario.SCENARIO_5_JOURNEY_MONITORING) {
                    onNavigateTo(AppScreen.ACTIVE_JOURNEY)
                } else {
                    onNavigateTo(AppScreen.MAP_ROUTE_EXPLORER)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafeTeal)
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Results on Map", fontWeight = FontWeight.Bold)
        }
    }
}
