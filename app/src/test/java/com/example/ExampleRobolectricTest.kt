package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.ClearSpaceViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ClearSpace AI", appName)
  }

  @Test
  fun `test clear space view model initialization and features`() = runTest {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ClearSpaceViewModel(application)

    // Verify initial values
    assertEquals(0, viewModel.onboardingStep.value)
    assertEquals("home", viewModel.activeTab.value)
    assertFalse(viewModel.isMainScanning.value)

    // Test onboarding advancement and cancellation
    viewModel.startOnboardingScan()
    assertEquals(1, viewModel.onboardingStep.value)
    
    viewModel.cancelOnboardingScan()
    assertEquals(0, viewModel.onboardingStep.value)
    assertEquals(0f, viewModel.scanProgress.value)

    viewModel.proceedToVaultSetup()
    assertEquals(3, viewModel.onboardingStep.value)

    viewModel.proceedToFinished()
    assertEquals(4, viewModel.onboardingStep.value)

    viewModel.completeOnboarding()
    assertEquals(-1, viewModel.onboardingStep.value)
    assertEquals("home", viewModel.activeTab.value)

    // Test tab selection
    viewModel.selectTab("files")
    assertEquals("files", viewModel.activeTab.value)

    viewModel.selectTab("vault")
    assertEquals("vault", viewModel.activeTab.value)

    viewModel.selectTab("settings")
    assertEquals("settings", viewModel.activeTab.value)
  }
}

