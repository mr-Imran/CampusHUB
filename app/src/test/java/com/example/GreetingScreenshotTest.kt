package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.model.Opportunity
import com.example.ui.screens.OpportunityCard
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
        Box(modifier = Modifier.padding(16.dp)) {
          OpportunityCard(
            opportunity = Opportunity(
              id = 1,
              title = "Global Inter-University Hackathon 2026",
              description = "Join student developers to build solutions for real-world sustainability challenges. Win prizes and secure placements.",
              category = "Competition",
              organization = "Campus Developers Club",
              dateTime = System.currentTimeMillis() + 864000000L,
              location = "Stanford Engineering Hall",
              deadline = System.currentTimeMillis() + 432000000L,
              contactEmail = "support@university.edu",
              requirements = "Laptop, basic coding",
              tags = "Coding,Tech,Design",
              posterEmail = "head@university.edu"
            ),
            onClick = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
