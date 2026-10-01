package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapVisualizer(
    selectedRoute: RouteOption?,
    allRoutes: List<RouteOption>,
    isNightMode: Boolean,
    showHeatmapLayer: Boolean,
    inspectedSegment: RoadSegment?,
    onSegmentClick: (RoadSegment) -> Unit,
    activeNavigationPoint: LatLngPoint? = null,
    modifier: Modifier = Modifier
) {
    var useGoogleMapsSdk by remember { mutableStateOf(false) } // Dual mode: Google Maps SDK or High-Contrast Safety Canvas

    val centerLatLng = remember { LatLng(12.9560, 77.6350) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLatLng, 13.5f)
    }

    Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        if (useGoogleMapsSdk) {
            // Google Maps SDK Composable
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    myLocationButtonEnabled = false
                ),
                properties = MapProperties(
                    isBuildingEnabled = true,
                    mapType = if (isNightMode) MapType.NORMAL else MapType.NORMAL
                )
            ) {
                // Polylines for routes
                allRoutes.forEach { route ->
                    val isSelected = route.id == selectedRoute?.id
                    val routeColor = when (route.type) {
                        RouteType.SAFER -> if (isSelected) Color(0xFF00C853) else Color(0x6600C853)
                        RouteType.FASTEST -> if (isSelected) Color(0xFFFF9100) else Color(0x66FF9100)
                        RouteType.BALANCED -> if (isSelected) Color(0xFF2979FF) else Color(0x662979FF)
                    }

                    Polyline(
                        points = route.pathPoints.map { LatLng(it.latitude, it.longitude) },
                        color = routeColor,
                        width = if (isSelected) 16f else 9f,
                        zIndex = if (isSelected) 2f else 1f
                    )
                }

                // Markers for start and destination
                selectedRoute?.pathPoints?.firstOrNull()?.let { start ->
                    Marker(
                        state = MarkerState(position = LatLng(start.latitude, start.longitude)),
                        title = "Start: ${start.label}",
                        snippet = "Origin"
                    )
                }
                selectedRoute?.pathPoints?.lastOrNull()?.let { dest ->
                    Marker(
                        state = MarkerState(position = LatLng(dest.latitude, dest.longitude)),
                        title = "Destination: ${dest.label}",
                        snippet = "SafePath Target"
                    )
                }

                // Active traveler live position marker
                activeNavigationPoint?.let { navPoint ->
                    Marker(
                        state = MarkerState(position = LatLng(navPoint.latitude, navPoint.longitude)),
                        title = "Current Live Location",
                        snippet = "Safe Check-In Active"
                    )
                }
            }
        } else {
            // High-Contrast Interactive Vector Safety Canvas Map
            InteractiveSafetyCanvasMap(
                selectedRoute = selectedRoute,
                allRoutes = allRoutes,
                isNightMode = isNightMode,
                showHeatmap = showHeatmapLayer,
                onSegmentClick = onSegmentClick,
                activeNavigationPoint = activeNavigationPoint
            )
        }

        // Overlay Map Controls (Layer toggle & Mode switcher)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Google Maps SDK toggle button
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (useGoogleMapsSdk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 4.dp,
                modifier = Modifier.clickable { useGoogleMapsSdk = !useGoogleMapsSdk }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (useGoogleMapsSdk) Icons.Default.Map else Icons.Default.Layers,
                        contentDescription = "Map Mode",
                        tint = if (useGoogleMapsSdk) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (useGoogleMapsSdk) "Google Maps SDK" else "Safety Canvas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (useGoogleMapsSdk) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Bottom Map Legend
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicator(color = ScoreGreen, label = "Safer (86)")
                LegendIndicator(color = ScoreAmber, label = "Fastest (62)")
                LegendIndicator(color = Color(0xFF3B82F6), label = "Balanced (78)")
            }
        }
    }
}

@Composable
fun LegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun InteractiveSafetyCanvasMap(
    selectedRoute: RouteOption?,
    allRoutes: List<RouteOption>,
    isNightMode: Boolean,
    showHeatmap: Boolean,
    onSegmentClick: (RoadSegment) -> Unit,
    activeNavigationPoint: LatLngPoint?
) {
    val bgColor = if (isNightMode) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val gridColor = if (isNightMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val roadBgColor = if (isNightMode) Color(0xFF334155) else Color(0xFFCBD5E1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // If tapped near segment 1 or segment 2
                        val firstSeg = selectedRoute?.segments?.firstOrNull()
                        if (firstSeg != null) onSegmentClick(firstSeg)
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw subtle Indian city street grid
            val gridStep = 40.dp.toPx()
            var x = 0f
            while (x < w) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                x += gridStep
            }
            var y = 0f
            while (y < h) {
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                y += gridStep
            }

            // 2. City Arterials and landmarks
            // Secondary roads
            drawLine(roadBgColor, Offset(w * 0.1f, h * 0.3f), Offset(w * 0.9f, h * 0.35f), strokeWidth = 12f, cap = StrokeCap.Round)
            drawLine(roadBgColor, Offset(w * 0.15f, h * 0.65f), Offset(w * 0.85f, h * 0.7f), strokeWidth = 10f, cap = StrokeCap.Round)
            drawLine(roadBgColor, Offset(w * 0.5f, h * 0.1f), Offset(w * 0.52f, h * 0.9f), strokeWidth = 14f, cap = StrokeCap.Round)

            // Heatmap risk glow buffers if enabled
            if (showHeatmap) {
                // High Risk zone (Ejipura dark bypass)
                drawCircle(
                    color = Color(0x33EF4444),
                    radius = w * 0.22f,
                    center = Offset(w * 0.38f, h * 0.52f)
                )
                // Moderate Risk buffer
                drawCircle(
                    color = Color(0x33F59E0B),
                    radius = w * 0.18f,
                    center = Offset(w * 0.58f, h * 0.42f)
                )
                // Low Risk / Safe corridor (100ft arterial)
                drawCircle(
                    color = Color(0x3310B981),
                    radius = w * 0.26f,
                    center = Offset(w * 0.72f, h * 0.45f)
                )
            }

            // 3. Draw Route Polylines
            // Route A (Fastest - cuts through narrow dark alley)
            val pathFast = Path().apply {
                moveTo(w * 0.22f, h * 0.82f)
                lineTo(w * 0.35f, h * 0.62f)
                lineTo(w * 0.42f, h * 0.45f)
                lineTo(w * 0.65f, h * 0.22f)
                lineTo(w * 0.78f, h * 0.18f)
            }

            // Route B (Safer - along wide 80ft and 100ft illuminated commercial corridors)
            val pathSafe = Path().apply {
                moveTo(w * 0.22f, h * 0.82f)
                lineTo(w * 0.45f, h * 0.80f)
                lineTo(w * 0.68f, h * 0.65f)
                lineTo(w * 0.72f, h * 0.38f)
                lineTo(w * 0.78f, h * 0.18f)
            }

            // Route C (Balanced)
            val pathBalanced = Path().apply {
                moveTo(w * 0.22f, h * 0.82f)
                lineTo(w * 0.38f, h * 0.72f)
                lineTo(w * 0.55f, h * 0.52f)
                lineTo(w * 0.65f, h * 0.32f)
                lineTo(w * 0.78f, h * 0.18f)
            }

            val isSafeSelected = selectedRoute?.type == RouteType.SAFER
            val isFastSelected = selectedRoute?.type == RouteType.FASTEST
            val isBalSelected = selectedRoute?.type == RouteType.BALANCED

            // Draw unselected first
            if (!isFastSelected) {
                drawPath(pathFast, color = Color(0x55FFAA00), style = Stroke(width = 10f, cap = StrokeCap.Round))
            }
            if (!isBalSelected) {
                drawPath(pathBalanced, color = Color(0x553B82F6), style = Stroke(width = 10f, cap = StrokeCap.Round))
            }
            if (!isSafeSelected) {
                drawPath(pathSafe, color = Color(0x5510B981), style = Stroke(width = 10f, cap = StrokeCap.Round))
            }

            // Draw selected route highlighted
            when (selectedRoute?.type) {
                RouteType.SAFER -> {
                    // Outer glow
                    drawPath(pathSafe, color = Color(0x4410B981), style = Stroke(width = 24f, cap = StrokeCap.Round))
                    drawPath(pathSafe, color = Color(0xFF10B981), style = Stroke(width = 14f, cap = StrokeCap.Round))
                }
                RouteType.FASTEST -> {
                    drawPath(pathFast, color = Color(0x44FF9800), style = Stroke(width = 22f, cap = StrokeCap.Round))
                    drawPath(pathFast, color = Color(0xFFFF9800), style = Stroke(width = 14f, cap = StrokeCap.Round))
                }
                RouteType.BALANCED -> {
                    drawPath(pathBalanced, color = Color(0x443B82F6), style = Stroke(width = 22f, cap = StrokeCap.Round))
                    drawPath(pathBalanced, color = Color(0xFF3B82F6), style = Stroke(width = 14f, cap = StrokeCap.Round))
                }
                else -> {
                    drawPath(pathSafe, color = Color(0xFF10B981), style = Stroke(width = 14f, cap = StrokeCap.Round))
                }
            }

            // 4. Street light icons & Police kiosks indicators on the map
            // Safe Corridor Streetlights (Bright green/yellow dots)
            drawCircle(Color(0xFFFFD54F), radius = 5.dp.toPx(), center = Offset(w * 0.45f, h * 0.80f))
            drawCircle(Color(0xFFFFD54F), radius = 5.dp.toPx(), center = Offset(w * 0.68f, h * 0.65f))
            drawCircle(Color(0xFFFFD54F), radius = 5.dp.toPx(), center = Offset(w * 0.72f, h * 0.38f))

            // Police Kiosk / ERSS 112 beacon
            drawCircle(Color(0xFF2979FF), radius = 8.dp.toPx(), center = Offset(w * 0.69f, h * 0.50f))

            // Deficient lighting marker on fastest shortcut
            drawCircle(Color(0xFFEF4444), radius = 6.dp.toPx(), center = Offset(w * 0.42f, h * 0.45f))

            // 5. Origin & Destination Pins
            // Origin (Koramangala 5th Block)
            drawCircle(Color(0xFF0D47A1), radius = 10.dp.toPx(), center = Offset(w * 0.22f, h * 0.82f))
            drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(w * 0.22f, h * 0.82f))

            // Destination (Indiranagar Metro)
            drawCircle(Color(0xFF00C853), radius = 10.dp.toPx(), center = Offset(w * 0.78f, h * 0.18f))
            drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(w * 0.78f, h * 0.18f))

            // Active traveler position if navigating
            if (activeNavigationPoint != null) {
                // Approximate animated position along the safe route
                drawCircle(Color(0x5500E5FF), radius = 18.dp.toPx(), center = Offset(w * 0.58f, h * 0.72f))
                drawCircle(Color(0xFF00E5FF), radius = 9.dp.toPx(), center = Offset(w * 0.58f, h * 0.72f))
                drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(w * 0.58f, h * 0.72f))
            }
        }

        // Tap hint tooltip
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clickable {
                    val seg = selectedRoute?.segments?.firstOrNull()
                    if (seg != null) onSegmentClick(seg)
                },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = SafeTeal, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tap road for Safety Breakdown", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
