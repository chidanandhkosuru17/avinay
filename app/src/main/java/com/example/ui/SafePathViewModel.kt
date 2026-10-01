package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SafePathRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    SETUP_WIZARD,       // Step-by-step setup (Profile + Address + Route)
    MAP_ROUTE_EXPLORER, // Main Map & Route Comparison
    RISK_HEATMAP,       // Continuous Heatmap & Segment Inspector
    ACTIVE_JOURNEY,     // Live Navigation & Safe Check-In with Emergency Parent Contact
    REPORT_ISSUE,       // Community Incident Reporting
    ADMIN_DASHBOARD,    // Municipal / Smart City Dashboard & Heatmap Analytics
    ETHICS_POLICY       // Responsible AI & Data Privacy Policy
}

enum class SetupStep {
    STEP_1_PROFILE_EMERGENCY_CONTACT,
    STEP_2_ADDRESS_SELECTION,
    STEP_3_ROUTE_PREVIEW
}

data class SafeCheckInState(
    val isNavigating: Boolean = false,
    val selectedRoute: RouteOption? = null,
    val currentStepIndex: Int = 0,
    val progressPct: Float = 0.0f,
    val elapsedSeconds: Int = 0,
    val totalEstimatedSeconds: Int = 14 * 60,
    val isCheckInDue: Boolean = false,
    val lastCheckInAcknowledged: Boolean = true,
    val emergencySmsSent: Boolean = false,
    val emergencyContactCalled: Boolean = false
)

class SafePathViewModel(application: Application) : AndroidViewModel(application) {

    val repository = SafePathRepository()

    private val _currentScreen = MutableStateFlow(AppScreen.MAP_ROUTE_EXPLORER)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _setupStep = MutableStateFlow(SetupStep.STEP_1_PROFILE_EMERGENCY_CONTACT)
    val setupStep: StateFlow<SetupStep> = _setupStep.asStateFlow()

    // Setup Wizard Form States
    val inputUserName = MutableStateFlow("Ananya Sharma")
    val inputContactName = MutableStateFlow("Sunita Sharma (Mother)")
    val inputContactPhone = MutableStateFlow("+91 98765 43210")
    val inputContactRelation = MutableStateFlow("Parent")
    val inputCategory = MutableStateFlow("Woman Traveler")

    val inputOrigin = MutableStateFlow("Koramangala 5th Block (Jyoti Nivas Junction)")
    val inputDestination = MutableStateFlow("Indiranagar Metro Station, 100 Feet Rd")
    val selectedCity = MutableStateFlow("Bengaluru")

    // Routes state
    private val _routes = MutableStateFlow<List<RouteOption>>(emptyList())
    val routes: StateFlow<List<RouteOption>> = _routes.asStateFlow()

    private val _selectedRoute = MutableStateFlow<RouteOption?>(null)
    val selectedRoute: StateFlow<RouteOption?> = _selectedRoute.asStateFlow()

    // Selected segment for Inspector Dialog
    private val _inspectedSegment = MutableStateFlow<RoadSegment?>(null)
    val inspectedSegment: StateFlow<RoadSegment?> = _inspectedSegment.asStateFlow()

    // Active Journey & Safe Check-In State
    private val _journeyState = MutableStateFlow(SafeCheckInState())
    val journeyState: StateFlow<SafeCheckInState> = _journeyState.asStateFlow()

    // Active scenario & Night mode
    val activeScenario = repository.activeScenario
    val isNightMode = repository.isNightMode
    val weights = repository.weights
    val communityReports = repository.communityReports
    val userProfile = repository.userProfile

    private var navigationJob: Job? = null

    init {
        refreshRoutes()
    }

    fun refreshRoutes() {
        val calculated = repository.computeRoutes()
        _routes.value = calculated
        if (_selectedRoute.value == null || !calculated.any { it.id == _selectedRoute.value?.id }) {
            // Default select the Safer route
            _selectedRoute.value = calculated.find { it.type == RouteType.SAFER } ?: calculated.firstOrNull()
        } else {
            // Refresh with updated score calculations
            _selectedRoute.value = calculated.find { it.id == _selectedRoute.value?.id }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSetupStep(step: SetupStep) {
        _setupStep.value = step
    }

    fun selectRoute(route: RouteOption) {
        _selectedRoute.value = route
    }

    fun inspectSegment(segment: RoadSegment?) {
        _inspectedSegment.value = segment
    }

    fun toggleNightMode() {
        repository.setNightMode(!repository.isNightMode.value)
        refreshRoutes()
    }

    fun selectScenario(scenario: DemoScenario) {
        repository.setScenario(scenario)
        refreshRoutes()
        if (scenario == DemoScenario.SCENARIO_5_JOURNEY_MONITORING) {
            // Start simulated active navigation and trigger check-in
            val route = _selectedRoute.value ?: _routes.value.first()
            startJourney(route)
        }
    }

    fun updateWeights(newWeights: SafetyWeights) {
        repository.updateWeights(newWeights)
        refreshRoutes()
    }

    fun saveProfileAndContinueToAddress() {
        repository.updateProfile(
            name = inputUserName.value,
            contactName = inputContactName.value,
            contactPhone = inputContactPhone.value,
            relation = inputContactRelation.value,
            category = inputCategory.value
        )
        _setupStep.value = SetupStep.STEP_2_ADDRESS_SELECTION
    }

    fun saveAddressesAndContinueToRoutes() {
        repository.updateAddresses(
            from = inputOrigin.value,
            to = inputDestination.value,
            city = selectedCity.value
        )
        refreshRoutes()
        _setupStep.value = SetupStep.STEP_3_ROUTE_PREVIEW
    }

    fun completeSetupAndOpenExplorer() {
        _currentScreen.value = AppScreen.MAP_ROUTE_EXPLORER
    }

    fun submitCommunityReport(category: String, description: String, location: String, severity: String) {
        repository.addCommunityReport(category, description, location, severity)
        refreshRoutes()
    }

    // Start Live Journey Tracking
    fun startJourney(route: RouteOption) {
        _selectedRoute.value = route
        _journeyState.value = SafeCheckInState(
            isNavigating = true,
            selectedRoute = route,
            currentStepIndex = 0,
            progressPct = 0.05f,
            elapsedSeconds = 0,
            totalEstimatedSeconds = route.travelTimeMin * 60,
            isCheckInDue = false,
            lastCheckInAcknowledged = true,
            emergencySmsSent = false
        )
        _currentScreen.value = AppScreen.ACTIVE_JOURNEY

        navigationJob?.cancel()
        navigationJob = viewModelScope.launch {
            val totalSeconds = route.travelTimeMin * 60
            for (sec in 1..totalSeconds) {
                delay(1000) // 1 second per step
                val fraction = (sec.toFloat() / totalSeconds).coerceIn(0f, 1f)
                val isDue = sec >= (totalSeconds * 0.45f) && !_journeyState.value.lastCheckInAcknowledged

                // In Scenario 5 or after 5 seconds, prompt check-in required
                val triggerCheckIn = isDue || (repository.activeScenario.value == DemoScenario.SCENARIO_5_JOURNEY_MONITORING && sec >= 4 && !_journeyState.value.lastCheckInAcknowledged)

                _journeyState.update { current ->
                    current.copy(
                        elapsedSeconds = sec,
                        progressPct = fraction,
                        isCheckInDue = triggerCheckIn,
                        currentStepIndex = ((route.pathPoints.size - 1) * fraction).toInt()
                    )
                }

                if (fraction >= 1.0f) {
                    break
                }
            }
        }
    }

    fun acknowledgeCheckIn() {
        _journeyState.update { it.copy(isCheckInDue = false, lastCheckInAcknowledged = true) }
    }

    fun stopJourney() {
        navigationJob?.cancel()
        _journeyState.value = SafeCheckInState(isNavigating = false)
        _currentScreen.value = AppScreen.MAP_ROUTE_EXPLORER
    }

    fun sendEmergencySms(context: Context) {
        val profile = userProfile.value
        val lat = 12.9640
        val lng = 77.6390
        val locationName = inputOrigin.value

        val message = "🚨 SafePath India Alert: Hi ${profile.emergencyContactName}, ${profile.name} has shared emergency check-in from: $locationName ($lat, $lng). Track live route: https://maps.google.com/?q=$lat,$lng"

        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${profile.emergencyContactPhone}")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            _journeyState.update { it.copy(emergencySmsSent = true) }
            Toast.makeText(context, "Emergency SMS dispatched to ${profile.emergencyContactName} (${profile.emergencyContactPhone})", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            _journeyState.update { it.copy(emergencySmsSent = true) }
            Toast.makeText(context, "Simulated SMS Alert sent to ${profile.emergencyContactPhone}!", Toast.LENGTH_SHORT).show()
        }
    }

    fun callEmergencyContact(context: Context) {
        val phone = userProfile.value.emergencyContactPhone
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            _journeyState.update { it.copy(emergencyContactCalled = true) }
        } catch (e: Exception) {
            Toast.makeText(context, "Dialing emergency contact: $phone", Toast.LENGTH_SHORT).show()
        }
    }

    fun callIndiaERSS112(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Dialing 112 India ERSS Emergency Services", Toast.LENGTH_SHORT).show()
        }
    }
}
