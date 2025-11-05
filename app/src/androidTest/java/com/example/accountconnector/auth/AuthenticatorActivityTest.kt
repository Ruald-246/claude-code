package com.example.accountconnector.auth

import android.accounts.AccountManager
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticatorActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<AuthenticatorActivity>()

    @Test
    fun loginScreen_displaysCorrectly() {
        // Verify all UI elements are present
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
    }

    @Test
    fun loginButton_isDisabled_whenFieldsEmpty() {
        // Login button should be disabled when fields are empty
        composeTestRule.onNodeWithText("Sign In")
            .assertIsNotEnabled()
    }

    @Test
    fun loginButton_isEnabled_whenFieldsFilled() {
        // Fill in username and password
        composeTestRule.onNodeWithText("Username").performTextInput("demo")
        composeTestRule.onNodeWithText("Password").performTextInput("password")

        // Login button should be enabled
        composeTestRule.onNodeWithText("Sign In")
            .assertIsEnabled()
    }

    @Test
    fun testCredentials_areDisplayed() {
        // Verify test credentials hint is shown
        composeTestRule.onNodeWithText("Test credentials:", substring = true)
            .assertIsDisplayed()
    }
}
