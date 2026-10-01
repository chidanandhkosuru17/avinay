package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.RouteType
import com.example.ui.AppScreen
import com.example.ui.SafePathViewModel
import com.example.ui.SetupStep
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWizardScreen(
    viewModel: SafePathViewModel,
    onFinish: () -> Unit
) {
    val step by viewModel.setupStep.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SafePath India Setup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = when (step) {
                                SetupStep.STEP_1_PROFILE_EMERGENCY_CONTACT -> "Step 1 of 3: Traveler & Emergency Contact"
                                SetupStep.STEP_2_ADDRESS_SELECTION -> "Step 2 of 3: Select Journey Route"
                                SetupStep.STEP_3_ROUTE_PREVIEW -> "Step 3 of 3: Safety Risk Evaluation"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (step != SetupStep.STEP_1_PROFILE_EMERGENCY_CONTACT) {
                        IconButton(onClick = {
                            if (step == SetupStep.STEP_3_ROUTE_PREVIEW) {
                                viewModel.setSetupStep(SetupStep.STEP_2_ADDRESS_SELECTION)
                            } else {
                                viewModel.setSetupStep(SetupStep.STEP_1_PROFILE_EMERGENCY_CONTACT)
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onFinish) {
                        Text("Skip to Map", color = SafeSaffron)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Step Progress Indicator Bar
            StepProgressBar(currentStep = step)

            Spacer(modifier = Modifier.height(20.dp))

            when (step) {
                SetupStep.STEP_1_PROFILE_EMERGENCY_CONTACT -> {
                    Step1ProfileAndContact(viewModel = viewModel)
                }
                SetupStep.STEP_2_ADDRESS_SELECTION -> {
                    Step2AddressSelection(viewModel = viewModel)
                }
                SetupStep.STEP_3_ROUTE_PREVIEW -> {
                    Step3RouteSafetyPreview(viewModel = viewModel, onFinish = onFinish)
                }
            }
        }
    }
}

@Composable
fun StepProgressBar(currentStep: SetupStep) {
    val stepIndex = when (currentStep) {
        SetupStep.STEP_1_PROFILE_EMERGENCY_CONTACT -> 1
        SetupStep.STEP_2_ADDRESS_SELECTION -> 2
        SetupStep.STEP_3_ROUTE_PREVIEW -> 3
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepCircle(stepNumber = 1, isActive = stepIndex >= 1, label = "Emergency Contact")
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = if (stepIndex >= 2) SafeTeal else MaterialTheme.colorScheme.surfaceVariant,
            thickness = 2.dp
        )
        StepCircle(stepNumber = 2, isActive = stepIndex >= 2, label = "Addresses")
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = if (stepIndex >= 3) SafeTeal else MaterialTheme.colorScheme.surfaceVariant,
            thickness = 2.dp
        )
        StepCircle(stepNumber = 3, isActive = stepIndex >= 3, label = "Safe Route")
    }
}

@Composable
fun StepCircle(stepNumber: Int, isActive: Boolean, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (isActive) SafeTeal else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$stepNumber",
                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun Step1ProfileAndContact(viewModel: SafePathViewModel) {
    val userName by viewModel.inputUserName.collectAsState()
    val contactName by viewModel.inputContactName.collectAsState()
    val contactPhone by viewModel.inputContactPhone.collectAsState()
    val contactRelation by viewModel.inputContactRelation.collectAsState()
    val category by viewModel.inputCategory.collectAsState()

    val categories = listOf("Woman Traveler", "Student", "Senior Citizen", "Night Commuter", "General")
    val relations = listOf("Mother", "Father", "Parent", "Spouse", "Guardian", "Friend")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SafeSaffron.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = SafeSaffronDark, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Vulnerable Traveler Protection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Set up your emergency parent contact first. SafePath keeps your contact updated via location check-ins and SMS alerts.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Text(
            text = "1. Traveler Details",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = userName,
            onValueChange = { viewModel.inputUserName.value = it },
            label = { Text("Your Full Name") },
            placeholder = { Text("e.g. Priya Sharma") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Category Selector Chips
        Text(text = "Traveler Profile", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(3).forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { viewModel.inputCategory.value = cat },
                    label = { Text(cat, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "2. Emergency Parent / Guardian Contact",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = contactName,
            onValueChange = { viewModel.inputContactName.value = it },
            label = { Text("Emergency Contact Name") },
            placeholder = { Text("e.g. Sunita Sharma (Mother)") },
            leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = contactPhone,
            onValueChange = { viewModel.inputContactPhone.value = it },
            label = { Text("Emergency Parent Phone Number") },
            placeholder = { Text("+91 98765 43210") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Text(text = "Relationship", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            relations.take(4).forEach { rel ->
                FilterChip(
                    selected = contactRelation == rel,
                    onClick = { viewModel.inputContactRelation.value = rel },
                    label = { Text(rel, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.saveProfileAndContinueToAddress() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafeTeal)
        ) {
            Text("Continue to Select Address", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun Step2AddressSelection(viewModel: SafePathViewModel) {
    val origin by viewModel.inputOrigin.collectAsState()
    val destination by viewModel.inputDestination.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()

    val cityPresets = mapOf(
        "Bengaluru" to listOf(
            "Koramangala 5th Block (Jyoti Nivas Junction)" to "Indiranagar Metro Station, 100 Feet Rd",
            "HSR Layout Sector 2" to "Bellandur EcoSpace Tech Park",
            "Majestic Railway Station" to "Malleshwaram 8th Cross"
        ),
        "New Delhi" to listOf(
            "Connaught Place Inner Circle" to "Hauz Khas Village Main Gate",
            "Saket Select Citywalk" to "Cyber Hub Gurgaon",
            "Delhi University North Campus" to "Karol Bagh Metro"
        ),
        "Mumbai" to listOf(
            "Bandra Bandstand" to "Bandra Kurla Complex (BKC)",
            "Dadar West Station" to "Worli Sea Face",
            "Andheri East Metro" to "Juhu Tara Road"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Select Starting Point & Destination",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "SafePath evaluates road lighting, pedestrian density, and historical safety to compute relative risk scores.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // City dropdown / selector
        Text(text = "City", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.repository.availableCities.take(3).forEach { city ->
                FilterChip(
                    selected = selectedCity == city,
                    onClick = { viewModel.selectedCity.value = city },
                    label = { Text(city) }
                )
            }
        }

        OutlinedTextField(
            value = origin,
            onValueChange = { viewModel.inputOrigin.value = it },
            label = { Text("Starting Location (From)") },
            placeholder = { Text("e.g. Koramangala 5th Block") },
            leadingIcon = { Icon(Icons.Default.TripOrigin, contentDescription = null, tint = SafePathLightPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = destination,
            onValueChange = { viewModel.inputDestination.value = it },
            label = { Text("Destination (To)") },
            placeholder = { Text("e.g. Indiranagar Metro Station") },
            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = SafeGreen) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Quick Preset Route Buttons
        Text(text = "Popular Indian City Scenarios", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        val presets = cityPresets[selectedCity] ?: cityPresets["Bengaluru"]!!
        presets.forEach { (presetFrom, presetTo) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        viewModel.inputOrigin.value = presetFrom
                        viewModel.inputDestination.value = presetTo
                    },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, tint = SafeSaffron, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(presetFrom, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("➔ $presetTo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Night Mode toggle card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = if (isNightMode) MidnightCard else MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isNightMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = if (isNightMode) Color(0xFFFFD54F) else Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isNightMode) "Night-Time Mode (9:30 PM)" else "Daytime Navigation Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isNightMode) "Streetlight coverage weight increased to 35%" else "Standard daytime weighting",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isNightMode,
                    onCheckedChange = { viewModel.toggleNightMode() }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { viewModel.saveAddressesAndContinueToRoutes() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafeTeal)
        ) {
            Text("Find Safe Routes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun Step3RouteSafetyPreview(
    viewModel: SafePathViewModel,
    onFinish: () -> Unit
) {
    val routes by viewModel.routes.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val saferRoute = routes.find { it.type == RouteType.SAFER }
    val fastestRoute = routes.find { it.type == RouteType.FASTEST }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "AI Route Comparison & Risk Scoring",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "SafePath balances travel efficiency with environmental safety indicators.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Route comparison cards
        routes.forEach { route ->
            val isSelected = route.id == selectedRoute?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.selectRoute(route) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SafeGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = route.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        if (route.isRecommended) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SafeGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "RECOMMENDED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScoreGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "${route.distanceKm} km", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Distance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column {
                            Text(text = "${route.travelTimeMin} min", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Travel Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${route.safetyScore}/100",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    route.safetyScore >= 75 -> ScoreGreen
                                    route.safetyScore >= 55 -> ScoreAmber
                                    else -> ScoreRed
                                }
                            )
                            Text(text = route.riskLevel, fontSize = 11.sp, fontWeight = FontWeight.Medium)
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

        // Why Route B is safer comparative banner
        if (saferRoute != null && fastestRoute != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SafeGreen.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SafeGreen.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = ScoreGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Why is the Alternative Route Safer?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The alternative route is approximately 3 minutes longer (+0.4 km) but has 95% continuous streetlight illumination, greater pedestrian activity, and direct proximity to an ERSS 112 police kiosk.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Emergency Contact Notice
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = SafeTeal, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Emergency Parent Contact Configured",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${userProfile.emergencyContactName} (${userProfile.emergencyContactPhone})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
            Text("Start Journey & Safe Check-In", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("View Full Interactive Map Explorer")
        }
    }
}
