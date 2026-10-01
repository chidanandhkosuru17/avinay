package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class SafePathRepository {

    private val _weights = MutableStateFlow(SafetyWeights())
    val weights: StateFlow<SafetyWeights> = _weights.asStateFlow()

    private val _isNightMode = MutableStateFlow(true) // Default to 9:30 PM demonstration scenario
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = "Priya Rao",
            emergencyContactName = "Sunita Rao (Mother)",
            emergencyContactPhone = "+91 98765 43210",
            emergencyContactRelation = "Parent",
            userCategory = "Woman Traveler",
            isConfigured = true
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _activeScenario = MutableStateFlow(DemoScenario.SCENARIO_2_SAFER_ALTERNATIVE)
    val activeScenario: StateFlow<DemoScenario> = _activeScenario.asStateFlow()

    // Current city selection
    val availableCities = listOf("Bengaluru", "New Delhi", "Mumbai", "Hyderabad", "Pune")
    private val _selectedCity = MutableStateFlow("Bengaluru")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _originAddress = MutableStateFlow("Koramangala 5th Block (Jyoti Nivas Junction)")
    val originAddress: StateFlow<String> = _originAddress.asStateFlow()

    private val _destinationAddress = MutableStateFlow("Indiranagar Metro Station, 100 Feet Rd")
    val destinationAddress: StateFlow<String> = _destinationAddress.asStateFlow()

    // Community Reports list
    private val _communityReports = MutableStateFlow<List<CommunityReport>>(
        listOf(
            CommunityReport(
                id = "REP-101",
                category = "Street Light Outage",
                description = "Dark stretch near 80ft cross, 4 consecutive streetlights non-functional.",
                locationName = "Intermediate Ring Road Bypass",
                segmentId = "SEG-FAST-2",
                severity = "Medium",
                upvotes = 7
            ),
            CommunityReport(
                id = "REP-102",
                category = "Isolated Road",
                description = "Construction debris blocking pedestrian sidewalk, very low footfall after 8 PM.",
                locationName = "Old Airport Link Lane",
                segmentId = "SEG-FAST-1",
                severity = "Low",
                upvotes = 4
            ),
            CommunityReport(
                id = "REP-103",
                category = "Harassment Concern",
                description = "Loitering near poorly lit bus stop, no police patrol present.",
                locationName = "Defense Colony Back Gate",
                segmentId = "SEG-FAST-3",
                severity = "High",
                upvotes = 12
            )
        )
    )
    val communityReports: StateFlow<List<CommunityReport>> = _communityReports.asStateFlow()

    // Historical Anonymized Reports
    val historicalReports = listOf(
        HistoricalSafetyReport(
            id = "HIST-01",
            title = "Lighting Infrastructure Deficit",
            category = "Lighting Deficiency",
            locationName = "Indiranagar 12th Main Backlane",
            lat = 12.9716,
            lng = 77.6412,
            dateAgo = "18 days ago",
            severity = "Medium",
            confidence = "Municipal Smart City Audit",
            daysOld = 18
        ),
        HistoricalSafetyReport(
            id = "HIST-02",
            title = "Anonymized Distress Call Aggregation",
            category = "Harassment Concern",
            locationName = "Ring Road Flyover Underpass",
            lat = 12.9634,
            lng = 77.6321,
            dateAgo = "24 days ago",
            severity = "High",
            confidence = "ERSS 112 Aggregated Telemetry",
            daysOld = 24
        ),
        HistoricalSafetyReport(
            id = "HIST-03",
            title = "CCTV & Police Kiosk Coverage",
            category = "High Security Zone",
            locationName = "100 Feet Main Road Junction",
            lat = 12.9784,
            lng = 77.6408,
            dateAgo = "5 days ago",
            severity = "Low",
            confidence = "Bengaluru City Police Post",
            daysOld = 5
        )
    )

    fun updateProfile(name: String, contactName: String, contactPhone: String, relation: String, category: String) {
        _userProfile.value = UserProfile(
            name = name.ifBlank { "Traveler" },
            emergencyContactName = contactName.ifBlank { "Parent" },
            emergencyContactPhone = contactPhone.ifBlank { "+91 98765 43210" },
            emergencyContactRelation = relation,
            userCategory = category,
            isConfigured = true
        )
    }

    fun updateAddresses(from: String, to: String, city: String? = null) {
        _originAddress.value = from
        _destinationAddress.value = to
        if (city != null) _selectedCity.value = city
    }

    fun setNightMode(isNight: Boolean) {
        _isNightMode.value = isNight
    }

    fun setScenario(scenario: DemoScenario) {
        _activeScenario.value = scenario
    }

    fun updateWeights(newWeights: SafetyWeights) {
        _weights.value = newWeights
    }

    fun addCommunityReport(category: String, description: String, location: String, severity: String) {
        val newRep = CommunityReport(
            id = "REP-${System.currentTimeMillis() % 10000}",
            category = category,
            description = description,
            locationName = location,
            segmentId = "SEG-FAST-2",
            severity = severity,
            status = "Verified & Live in Model",
            upvotes = 1
        )
        _communityReports.value = listOf(newRep) + _communityReports.value
    }

    // Generate simulated road network segments and calculate Route A (Fastest), Route B (Safer), Route C (Balanced)
    fun computeRoutes(): List<RouteOption> {
        val scenario = _activeScenario.value
        val isNight = _isNightMode.value
        val weights = _weights.value

        // Segment definitions:
        // Shortcut back road (Used by Route A - Fastest)
        val segDarkAlley = RoadSegment(
            id = "SEG-FAST-1",
            name = "Intermediate Ring Rd Bypass (Alley)",
            startLat = 12.9352,
            startLng = 77.6245,
            endLat = 12.9510,
            endLng = 77.6320,
            distanceKm = 1.3,
            travelTimeMin = 4.8,
            lightingCoveragePct = if (scenario == DemoScenario.SCENARIO_4_LIGHTING_OUTAGE) 20 else 35,
            crowdDensity = CrowdDensityLevel.LOW,
            historicalIncidentScore = if (scenario == DemoScenario.SCENARIO_3_HISTORICAL_SPIKE) 35 else 52,
            publicActivityScore = 30,
            emergencyAccessibilityScore = 40,
            outageReportCount = if (scenario == DemoScenario.SCENARIO_4_LIGHTING_OUTAGE) 4 else 1,
            hasRecentIncidentReport = scenario == DemoScenario.SCENARIO_3_HISTORICAL_SPIKE
        )

        val segInnerLink = RoadSegment(
            id = "SEG-FAST-2",
            name = "Ejipura Inner Transit Lane",
            startLat = 12.9510,
            startLng = 77.6320,
            endLat = 12.9640,
            endLng = 77.6390,
            distanceKm = 1.5,
            travelTimeMin = 6.2,
            lightingCoveragePct = 48,
            crowdDensity = CrowdDensityLevel.LOW,
            historicalIncidentScore = 58,
            publicActivityScore = 45,
            emergencyAccessibilityScore = 55,
            outageReportCount = if (scenario == DemoScenario.SCENARIO_4_LIGHTING_OUTAGE) 2 else 0
        )

        // Main arterial boulevard (Used by Route B - Safer)
        val segMainAvenue1 = RoadSegment(
            id = "SEG-SAFE-1",
            name = "Koramangala 80ft Main Road (Commercial)",
            startLat = 12.9352,
            startLng = 77.6245,
            endLat = 12.9535,
            endLng = 77.6350,
            distanceKm = 1.6,
            travelTimeMin = 6.5,
            lightingCoveragePct = 95,
            crowdDensity = CrowdDensityLevel.HIGH,
            historicalIncidentScore = 90,
            publicActivityScore = 88,
            emergencyAccessibilityScore = 92
        )

        val segMainAvenue2 = RoadSegment(
            id = "SEG-SAFE-2",
            name = "Indiranagar 100 Feet Corridor (Active Shops & Police Post)",
            startLat = 12.9535,
            startLng = 77.6350,
            endLat = 12.9784,
            endLng = 77.6408,
            distanceKm = 1.6,
            travelTimeMin = 7.5,
            lightingCoveragePct = 92,
            crowdDensity = CrowdDensityLevel.HIGH,
            historicalIncidentScore = 88,
            publicActivityScore = 85,
            emergencyAccessibilityScore = 94
        )

        // Balanced mixed route (Route C)
        val segMixed1 = RoadSegment(
            id = "SEG-BAL-1",
            name = "Sony World Junction to Domlur Flyover",
            startLat = 12.9352,
            startLng = 77.6245,
            endLat = 12.9600,
            endLng = 77.6370,
            distanceKm = 1.4,
            travelTimeMin = 5.5,
            lightingCoveragePct = 80,
            crowdDensity = CrowdDensityLevel.MEDIUM,
            historicalIncidentScore = 78,
            publicActivityScore = 70,
            emergencyAccessibilityScore = 82
        )

        val segMixed2 = RoadSegment(
            id = "SEG-BAL-2",
            name = "HAL 2nd Stage Main Link",
            startLat = 12.9600,
            startLng = 77.6370,
            endLat = 12.9784,
            endLng = 77.6408,
            distanceKm = 1.6,
            travelTimeMin = 6.5,
            lightingCoveragePct = 75,
            crowdDensity = CrowdDensityLevel.MEDIUM,
            historicalIncidentScore = 76,
            publicActivityScore = 68,
            emergencyAccessibilityScore = 78
        )

        // Calculate segment averages for Route A (Fastest)
        val routeASegments = listOf(segDarkAlley, segInnerLink)
        val routeADist = (segDarkAlley.distanceKm + segInnerLink.distanceKm)
        val routeATime = (segDarkAlley.travelTimeMin + segInnerLink.travelTimeMin).roundToInt()
        val routeASafety = routeASegments.map { it.calculateSafetyScore(weights, isNight) }.average().roundToInt()
        val routeARisk = 100 - routeASafety

        // Route B (Safer)
        val routeBSegments = listOf(segMainAvenue1, segMainAvenue2)
        val routeBDist = (segMainAvenue1.distanceKm + segMainAvenue2.distanceKm)
        val routeBTime = (segMainAvenue1.travelTimeMin + segMainAvenue2.travelTimeMin).roundToInt()
        val routeBSafety = routeBSegments.map { it.calculateSafetyScore(weights, isNight) }.average().roundToInt()
        val routeBRisk = 100 - routeBSafety

        // Route C (Balanced)
        val routeCSegments = listOf(segMixed1, segMixed2)
        val routeCDist = (segMixed1.distanceKm + segMixed2.distanceKm)
        val routeCTime = (segMixed1.travelTimeMin + segMixed2.travelTimeMin).roundToInt()
        val routeCSafety = routeCSegments.map { it.calculateSafetyScore(weights, isNight) }.average().roundToInt()
        val routeCRisk = 100 - routeCSafety

        // Path coordinates for map rendering
        val pathA = listOf(
            LatLngPoint(12.9352, 77.6245, "Koramangala 5th Block"),
            LatLngPoint(12.9420, 77.6280, "Intermediate Link"),
            LatLngPoint(12.9510, 77.6320, "Ejipura Inner Lane"),
            LatLngPoint(12.9640, 77.6390, "Domlur Backway"),
            LatLngPoint(12.9784, 77.6408, "Indiranagar Metro")
        )

        val pathB = listOf(
            LatLngPoint(12.9352, 77.6245, "Koramangala 5th Block"),
            LatLngPoint(12.9400, 77.6310, "80ft Main Road"),
            LatLngPoint(12.9535, 77.6350, "Sony World Junction"),
            LatLngPoint(12.9680, 77.6380, "100ft Active High-Street"),
            LatLngPoint(12.9740, 77.6400, "Police Kiosk Post"),
            LatLngPoint(12.9784, 77.6408, "Indiranagar Metro")
        )

        val pathC = listOf(
            LatLngPoint(12.9352, 77.6245, "Koramangala 5th Block"),
            LatLngPoint(12.9460, 77.6330, "Sony Junction"),
            LatLngPoint(12.9600, 77.6370, "Domlur Flyover Side"),
            LatLngPoint(12.9700, 77.6395, "HAL 2nd Stage"),
            LatLngPoint(12.9784, 77.6408, "Indiranagar Metro")
        )

        val routeA = RouteOption(
            id = "ROUTE-A",
            type = RouteType.FASTEST,
            name = "Fastest Route via Ejipura Bypass",
            distanceKm = String.format("%.1f", routeADist).toDouble(),
            travelTimeMin = routeATime,
            safetyScore = routeASafety,
            riskScore = routeARisk,
            riskLevel = if (routeASafety >= 75) "Lower Risk" else if (routeASafety >= 55) "Moderate Risk" else "Higher Risk",
            segments = routeASegments,
            highlights = listOf("Shortest transit distance", "Low streetlight coverage (35-48%)", "Isolated back-alleys after 8 PM"),
            explanation = "Takes narrow intermediate link roads saving 3 minutes, but has poor street lighting (35%), solitary footpaths, and higher historical incident reports.",
            isRecommended = false,
            pathPoints = pathA
        )

        val routeB = RouteOption(
            id = "ROUTE-B",
            type = RouteType.SAFER,
            name = "Safer Route via 80ft & 100ft Arterial",
            distanceKm = String.format("%.1f", routeBDist).toDouble(),
            travelTimeMin = routeBTime,
            safetyScore = routeBSafety,
            riskScore = routeBRisk,
            riskLevel = if (routeBSafety >= 75) "Lower Risk" else if (routeBSafety >= 55) "Moderate Risk" else "Higher Risk",
            segments = routeBSegments,
            highlights = listOf(
                "95% Streetlight illumination",
                "Continuous commercial footfall & open storefronts",
                "Direct proximity to ERSS 112 Police Kiosk"
            ),
            explanation = "Approximately 3 minutes longer (+0.4 km), but provides 95% continuous smart streetlighting, active retail pedestrian flow, and high emergency accessibility.",
            isRecommended = true,
            pathPoints = pathB
        )

        val routeC = RouteOption(
            id = "ROUTE-C",
            type = RouteType.BALANCED,
            name = "Balanced Route via Domlur Link",
            distanceKm = String.format("%.1f", routeCDist).toDouble(),
            travelTimeMin = routeCTime,
            safetyScore = routeCSafety,
            riskScore = routeCRisk,
            riskLevel = if (routeCSafety >= 75) "Lower Risk" else if (routeCSafety >= 55) "Moderate Risk" else "Higher Risk",
            segments = routeCSegments,
            highlights = listOf("Good main road lighting", "Medium traffic flow", "Adequate bus stop visibility"),
            explanation = "Balanced compromise between speed and lighting quality with moderate public activity.",
            isRecommended = false,
            pathPoints = pathC
        )

        return listOf(routeB, routeA, routeC) // Default display with recommended Safer route first
    }
}
