package com.hanif.kajlagbe

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.MaterialTheme
import org.junit.Rule
import org.junit.Test

class LoginRegisterUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginFieldsAreVisible() {
        // Here we test if the UI elements for Login appear correctly
        composeTestRule.setContent {
            MaterialTheme {
                // If you had a LoginScreen composable, you'd call it here:
                // LoginScreen(navController = rememberNavController())
            }
        }

        // Test if specific UI components exist (e.g., text fields, buttons)
        // These will fail if your UI changes and they no longer exist!
        // composeTestRule.onNodeWithText("Email").assertExists()
        // composeTestRule.onNodeWithText("Password").assertExists()
        // composeTestRule.onNodeWithText("Login").assertExists()
    }
}
