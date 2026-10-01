package com.example.model

data class UserProfile(
    val name: String = "Ananya Sharma",
    val emergencyContactName: String = "Mother (Sunita Sharma)",
    val emergencyContactPhone: String = "+91 98765 43210",
    val emergencyContactRelation: String = "Parent",
    val userCategory: String = "Woman Traveler",
    val isConfigured: Boolean = false
)

data class LatLngPoint(
    val latitude: Double,
    val longitude: Double,
    val label: String = ""
)

data class RoadSegment(
    val id: String,
    val name: String,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val distanceKm: Double,
    val travelTimeMin: Double,
    val lightingCoveragePct: Int, // 0 - 100
    val crowdDensity: CrowdDensityLevel,
    val historicalIncidentScore: Int, // 0 - 100 (higher = safer, i.e. fewer/milder incidents)
    val publicActivityScore: Int, // 0 - 100
    val emergencyAccessibilityScore: Int, // 0 - 100 (proximity to police/hospitals)
    val outageReportCount: Int = 0,
    val hasRecentIncidentReport: Boolean = false
) {
    fun calculateLightingScore(): Int {
        val outagePenalty = (outageReportCount * 25).coerceAtMost(70)
        return (lightingCoveragePct - outagePenalty).coerceIn(5, 100)
    }

    fun calculateCrowdScore(): Int {
        return when (crowdDensity) {
            CrowdDensityLevel.HIGH -> 90
            CrowdDensityLevel.MEDIUM -> 65
            CrowdDensityLevel.LOW -> 30
        }
    }

    fun calculateHistoricalScore(): Int {
        val incidentPenalty = if (hasRecentIncidentReport) 35 else 0
        return (historicalIncidentScore - incidentPenalty).coerceIn(10, 100)
    }

    fun calculateSafetyScore(weights: SafetyWeights, isNightTime: Boolean): Int {
        val activeWeights = if (isNightTime) weights.forNight() else weights
        val lighting = calculateLightingScore()
        val crowd = calculateCrowdScore()
        val history = calculateHistoricalScore()
        val activity = publicActivityScore
        val emergency = emergencyAccessibilityScore
        val timeOfDay = if (isNightTime) 45 else 85

        val totalWeighted = (lighting * activeWeights.lighting) +
                (crowd * activeWeights.crowdDensity) +
                (history * activeWeights.historicalReports) +
                (activity * activeWeights.publicActivity) +
                (emergency * activeWeights.emergencyAccessibility) +
                (timeOfDay * activeWeights.timeOfDay)

        val sumWeights = activeWeights.totalSum()
        return if (sumWeights > 0) (totalWeighted / sumWeights).toInt().coerceIn(0, 100) else 50
    }

    fun calculateRiskScore(weights: SafetyWeights, isNightTime: Boolean): Int {
        return 100 - calculateSafetyScore(weights, isNightTime)
    }
}

enum class CrowdDensityLevel(val label: String) {
    LOW("Low Activity (Isolated)"),
    MEDIUM("Moderate Pedestrian Flow"),
    HIGH("High Activity (Vibrant/Busy)")
}

data class SafetyWeights(
    val lighting: Double = 0.25,
    val crowdDensity: Double = 0.20,
    val historicalReports: Double = 0.25,
    val publicActivity: Double = 0.10,
    val emergencyAccessibility: Double = 0.10,
    val timeOfDay: Double = 0.10,
    // Routing cost weights: Cost = α * TravelTime + β * SafetyRisk + γ * Distance
    val alphaTime: Double = 0.35,
    val betaRisk: Double = 0.45,
    val gammaDistance: Double = 0.20
) {
    fun totalSum(): Double = lighting + crowdDensity + historicalReports + publicActivity + emergencyAccessibility + timeOfDay

    fun forNight(): SafetyWeights {
        return copy(
            lighting = 0.35,
            crowdDensity = 0.25,
            historicalReports = 0.20,
            publicActivity = 0.05,
            emergencyAccessibility = 0.10,
            timeOfDay = 0.05
        )
    }
}

enum class RouteType(val title: String) {
    FASTEST("Fastest Route"),
    SAFER("Safer Route"),
    BALANCED("Balanced Route")
}

data class RouteOption(
    val id: String,
    val type: RouteType,
    val name: String,
    val distanceKm: Double,
    val travelTimeMin: Int,
    val safetyScore: Int,
    val riskScore: Int,
    val riskLevel: String, // "Lower Risk", "Moderate Risk", "Higher Risk"
    val segments: List<RoadSegment>,
    val highlights: List<String>,
    val explanation: String,
    val isRecommended: Boolean = false,
    val pathPoints: List<LatLngPoint>
)

data class HistoricalSafetyReport(
    val id: String,
    val title: String,
    val category: String, // "Lighting Deficiency", "Harassment Concern", "Isolated Area", "Road Hazard"
    val locationName: String,
    val lat: Double,
    val lng: Double,
    val dateAgo: String,
    val severity: String, // "Low", "Medium", "High"
    val confidence: String, // "Citizen Verified", "Police ERSS Aggregated", "Municipal Sensor"
    val daysOld: Int = 12
) {
    // Time decay weight: older reports have less influence
    fun getDecayFactor(): Double {
        return (1.0 / (1.0 + (daysOld * 0.05))).coerceIn(0.2, 1.0)
    }
}

data class CommunityReport(
    val id: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val category: String,
    val description: String,
    val locationName: String,
    val segmentId: String,
    val severity: String,
    val status: String = "Moderated & Applied",
    val upvotes: Int = 1
)

enum class DemoScenario(val title: String, val description: String) {
    SCENARIO_1_POOR_LIGHTING(
        "Scenario 1: Dark Alley Fastest Route",
        "Fastest route cuts through an unlit shortcut (35% lighting, moderate risk)."
    ),
    SCENARIO_2_SAFER_ALTERNATIVE(
        "Scenario 2: Illuminated Main Road",
        "Alternative route adds +3 mins, but provides 95% street lighting & high pedestrian presence."
    ),
    SCENARIO_3_HISTORICAL_SPIKE(
        "Scenario 3: Recent Incident Spike",
        "Multiple recent community incident reports lower safety on the back road."
    ),
    SCENARIO_4_LIGHTING_OUTAGE(
        "Scenario 4: Live Streetlight Outage",
        "Community reports 3 broken streetlights on 100ft Road, recalculating segment risk."
    ),
    SCENARIO_5_JOURNEY_MONITORING(
        "Scenario 5: Check-In & Emergency Alert",
        "Active journey simulation: user delayed, trigger Check-in & SMS to emergency contact."
    )
}
