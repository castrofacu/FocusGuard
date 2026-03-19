package com.facucastro.focusguard.presentation.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facucastro.focusguard.providers.presentation.login.providesLoginContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenLoginScreen_whenContentIsDisplayed_thenAllInitialElementsAreVisible() {
        // GIVEN
        composeTestRule.providesLoginContent()

        // THEN
        composeTestRule.onNodeWithText("FocusGuard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Protect your focus").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login with Google").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue as Guest").assertIsDisplayed()
    }

    @Test
    fun givenLoginScreen_whenGoogleLoginIsClicked_thenCallbackIsTriggered() {
        // GIVEN
        var called = false
        composeTestRule.providesLoginContent(
            onGoogleClick = { called = true }
        )

        // WHEN
        composeTestRule.onNodeWithText("Login with Google").performClick()

        // THEN
        assertTrue(called)
    }

    @Test
    fun givenLoginScreen_whenAnonymousLoginIsClicked_thenCallbackIsTriggered() {
        // GIVEN
        var called = false
        composeTestRule.providesLoginContent(
            onAnonymousClick = { called = true }
        )

        // WHEN
        composeTestRule.onNodeWithText("Continue as Guest").performClick()

        // THEN
        assertTrue(called)
    }

    @Test
    fun givenLoginScreenInLoadingState_whenDisplayed_thenButtonsAreDisabledAndLoadingTextIsShown() {
        // GIVEN
        composeTestRule.providesLoginContent(isLoading = true)

        // THEN
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loading...").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Continue as Guest").assertIsNotEnabled()
    }
}
