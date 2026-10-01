package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SafePathRepository
import com.example.model.DemoScenario
import com.example.model.RouteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun read_appName_from_context() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SafePath India", appName)
  }

  @Test
  fun test_safer_route_has_higher_safety_score_than_fastest_route() {
    val repository = SafePathRepository()
    repository.setNightMode(true)
    repository.setScenario(DemoScenario.SCENARIO_2_SAFER_ALTERNATIVE)

    val routes = repository.computeRoutes()
    val routeSafer = routes.find { it.type == RouteType.SAFER }
    val routeFastest = routes.find { it.type == RouteType.FASTEST }

    assertTrue("Safer route should exist", routeSafer != null)
    assertTrue("Fastest route should exist", routeFastest != null)
    assertTrue(
      "Safer route (${routeSafer?.safetyScore}) should have higher safety score than Fastest route (${routeFastest?.safetyScore})",
      (routeSafer?.safetyScore ?: 0) > (routeFastest?.safetyScore ?: 0)
    )
  }
}
