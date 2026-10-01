package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommunityReport
import com.example.ui.SafePathViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityReportScreen(
    viewModel: SafePathViewModel
) {
    val communityReports by viewModel.communityReports.collectAsState()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("Street Light Outage") }
    var locationInput by remember { mutableStateOf("Ejipura Inner Transit Lane") }
    var descriptionInput by remember { mutableStateOf("") }
    var severityLevel by remember { mutableStateOf("Medium") }

    val categories = listOf(
        "Street Light Outage",
        "Isolated Road / Poor Footfall",
        "Harassment Concern",
        "Unsafe Infrastructure",
        "Road Obstruction",
        "Suspicious Environmental Condition"
    )

    val severities = listOf("Low", "Medium", "High")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Anonymous Community Safety Reporting",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Crowdsourced reports help alert other vulnerable travelers and notify municipal authorities. All reports are strictly anonymous and moderated.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.sp
        )

        // Submit New Report Card Form
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Submit Safety Observation",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Category selector
                Text(text = "Issue Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (selectedCategory == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            onClick = { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cat, fontSize = 13.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                // Location input
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    label = { Text("Location / Street Landmark") },
                    placeholder = { Text("e.g. 80 Feet Road Junction") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SafeTeal) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Description input
                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    label = { Text("Anonymous Description (No personal info)") },
                    placeholder = { Text("e.g. Dark stretch of approximately 100 meters, 3 street lights flickering.") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Severity selector
                Text(text = "Environmental Severity", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    severities.forEach { sev ->
                        FilterChip(
                            selected = severityLevel == sev,
                            onClick = { severityLevel = sev },
                            label = { Text(sev) }
                        )
                    }
                }

                Button(
                    onClick = {
                        val desc = descriptionInput.ifBlank { "Citizen observed safety condition at $locationInput." }
                        viewModel.submitCommunityReport(
                            category = selectedCategory,
                            description = desc,
                            location = locationInput,
                            severity = severityLevel
                        )
                        descriptionInput = ""
                        Toast.makeText(context, "Report verified & integrated into safety routing model!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SafeSaffronDark)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Anonymous Report", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Feed of Recent Reports
        Text(
            text = "Active Verified Reports in Area (${communityReports.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        communityReports.forEach { report ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        when (report.severity) {
                                            "High" -> ScoreRed
                                            "Medium" -> ScoreAmber
                                            else -> ScoreGreen
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = report.category,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SafeGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = report.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📍 ${report.locationName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Anonymous Citizen ID • Real-time Safety Model Impacted",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = SafeTeal)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${report.upvotes} validations", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
