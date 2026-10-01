package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.LatLngPoint
import com.example.model.RouteType
import com.example.ui.SafePathViewModel
import com.example.ui.components.MapVisualizer
import com.example.ui.theme.*

@Composable
fun ActiveJourneyScreen(
    viewModel: SafePathViewModel,
    onStopJourney: () -> Unit
) {
    val journeyState by viewModel.journeyState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()
    val context = LocalContext.current

    val route = journeyState.selectedRoute ?: routes.first()

    // Current navigation point along route
    val currentPoint = remember(journeyState.currentStepIndex, route) {
        val points = route.pathPoints
        if (points.isNotEmpty()) {
            val idx = journeyState.currentStepIndex.coerceIn(0, points.size - 1)
            points[idx]
        } else null
    }

    val minutesLeft = remember(journeyState.elapsedSeconds, journeyState.totalEstimatedSeconds) {
        val rem = ((journeyState.totalEstimatedSeconds - journeyState.elapsedSeconds) / 60).coerceAtLeast(1)
        rem
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Navigation Status Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (journeyState.isCheckInDue) ScoreAmber.copy(alpha = 0.15f) else SafeGreen.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (journeyState.isCheckInDue) ScoreAmber else SafeGreen)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (journeyState.isCheckInDue) ScoreAmber else SafeGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (journeyState.isCheckInDue) Icons.Default.Warning else Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (journeyState.isCheckInDue) "Safe Check-In Required" else "Journey Started • Safe Navigation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (journeyState.isCheckInDue) "Journey check scheduled. Confirm your safety below." else "Tracking progress toward ${route.pathPoints.lastOrNull()?.label ?: "Destination"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Check-In Alert Box (if check-in due)
        AnimatedVisibility(visible = journeyState.isCheckInDue) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ScoreAmber)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ScoreAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Are you progressing safely?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "It's time for your scheduled safety check. If unacknowledged, an automated SMS update can be sent to your parent contact.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.acknowledgeCheckIn() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                        ) {
                            Text("I am Safe", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.sendEmergencySms(context) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Alert Parent", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live Map Visualizer with Traveler Location Pin
        MapVisualizer(
            selectedRoute = route,
            allRoutes = routes,
            isNightMode = isNightMode,
            showHeatmapLayer = false,
            inspectedSegment = null,
            onSegmentClick = { },
            activeNavigationPoint = currentPoint,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        // Progress Metrics Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Journey Progress", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${(journeyState.progressPct * 100).toInt()}% completed",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeTeal
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { journeyState.progressPct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SafeTeal,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "$minutesLeft mins", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SafeGreen)
                        Text(text = "Est. Remaining Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column {
                        Text(text = "${route.safetyScore}/100", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ScoreGreen)
                        Text(text = "Safety Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "${route.distanceKm} km", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Total Distance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Emergency Parent Contact Assistance Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, SafeTeal.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Emergency Parent Contact",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${userProfile.emergencyContactName} • ${userProfile.emergencyContactPhone}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Send Live Location SMS, Call Contact, India 112
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.sendEmergencySms(context) },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = SafeTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send SMS Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.callEmergencyContact(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = SafeGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.callIndiaERSS112(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = SafeSaffron.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.LocalPolice, contentDescription = null, modifier = Modifier.size(16.dp), tint = SafeSaffronDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("112 India", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafeSaffronDark)
                    }
                }
            }
        }

        // Stop Journey / End Navigation Button
        OutlinedButton(
            onClick = onStopJourney,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("End Navigation")
        }
    }
}
