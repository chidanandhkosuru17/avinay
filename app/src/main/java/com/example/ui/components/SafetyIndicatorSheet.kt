package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RoadSegment
import com.example.model.SafetyWeights
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyIndicatorModalSheet(
    segment: RoadSegment?,
    weights: SafetyWeights,
    isNightMode: Boolean,
    onDismiss: () -> Unit
) {
    if (segment == null) return

    val lightingScore = segment.calculateLightingScore()
    val crowdScore = segment.calculateCrowdScore()
    val historyScore = segment.calculateHistoricalScore()
    val activityScore = segment.publicActivityScore
    val emergencyScore = segment.emergencyAccessibilityScore
    val overallSafety = segment.calculateSafetyScore(weights, isNightMode)
    val riskScore = 100 - overallSafety

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Road Segment Safety Indicators",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = segment.name,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        overallSafety >= 75 -> ScoreGreen.copy(alpha = 0.15f)
                        overallSafety >= 55 -> ScoreAmber.copy(alpha = 0.15f)
                        else -> ScoreRed.copy(alpha = 0.15f)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$overallSafety/100",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = when {
                                overallSafety >= 75 -> ScoreGreen
                                overallSafety >= 55 -> ScoreAmber
                                else -> ScoreRed
                            }
                        )
                        Text(
                            text = if (overallSafety >= 75) "Lower Risk" else if (overallSafety >= 55) "Moderate Risk" else "Higher Risk",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Indicator metrics grid
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IndicatorMetricRow(
                        icon = Icons.Default.WbIncandescent,
                        label = "Street Lighting Score",
                        score = lightingScore,
                        detail = "${segment.lightingCoveragePct}% coverage • ${if (segment.outageReportCount > 0) "${segment.outageReportCount} reported outages" else "Fully operational"}",
                        accentColor = Color(0xFFF59E0B)
                    )

                    IndicatorMetricRow(
                        icon = Icons.Default.Groups,
                        label = "Crowd Density & Footfall",
                        score = crowdScore,
                        detail = segment.crowdDensity.label,
                        accentColor = Color(0xFF3B82F6)
                    )

                    IndicatorMetricRow(
                        icon = Icons.Default.History,
                        label = "Historical Incident Indicator",
                        score = historyScore,
                        detail = if (segment.hasRecentIncidentReport) "Recent reports recorded (decay factor applied)" else "No recent safety anomalies",
                        accentColor = Color(0xFF8B5CF6)
                    )

                    IndicatorMetricRow(
                        icon = Icons.Default.Storefront,
                        label = "Public Activity & Open Shops",
                        score = activityScore,
                        detail = if (activityScore > 75) "High storefront visibility & active surveillance" else "Sparse business activity",
                        accentColor = Color(0xFF10B981)
                    )

                    IndicatorMetricRow(
                        icon = Icons.Default.LocalPolice,
                        label = "Emergency Accessibility",
                        score = emergencyScore,
                        detail = if (emergencyScore > 80) "Quick access to ERSS 112 kiosk & medical post" else "Intermediate response proximity",
                        accentColor = Color(0xFF06B6D4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mandatory disclaimer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scores are estimates based on available environmental indicators and do not guarantee personal safety. Always trust your instincts and maintain situational awareness.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close Inspector")
            }
        }
    }
}

@Composable
fun IndicatorMetricRow(
    icon: ImageVector,
    label: String,
    score: Int,
    detail: String,
    accentColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "$score/100",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
