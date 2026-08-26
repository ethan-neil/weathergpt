package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.DialectSummary
import com.example.ui.components.WeatherDialectSummaryCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        WeatherDialectSummaryCard(
          tempC = 28,
          feelsLikeC = 30,
          conditionEmoji = "⛅",
          conditionDesc = "Partly Cloudy",
          dialectSummary = DialectSummary(
            "en",
            "English",
            "Partly Cloudy",
            "Mild sunny intervals with light breeze.",
            "Favorable for spraying."
          ),
          locationName = "Pune, Maharashtra",
          isSpeaking = false,
          onSpeakClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
