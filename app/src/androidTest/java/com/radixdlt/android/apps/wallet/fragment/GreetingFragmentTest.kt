package com.radixdlt.android.apps.wallet.fragment

import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.helper.DelayHelper
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions.assertDisabled
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions.assertEnabled
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickBack
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaDialogInteractions.clickDialogPositiveButton
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.rule.cleardata.ClearDatabaseRule
import com.schibsted.spain.barista.rule.cleardata.ClearFilesRule
import com.schibsted.spain.barista.rule.cleardata.ClearPreferencesRule
import junit.framework.TestCase.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class GreetingFragmentTest {

    @get:Rule
    var newWalletActivityTestRule = IntentsTestRule(StartActivity::class.java)

    // Clear all app's SharedPreferences
    @get:Rule
    var clearPreferencesRule = ClearPreferencesRule()

    // Delete all tables from all the app's SQLite Databases
    @get:Rule
    var clearDatabaseRule = ClearDatabaseRule()

    // Delete all files in getFilesDir() and getCacheDir()
    @get:Rule
    var clearFilesRule = ClearFilesRule()

    @Test
    fun testGreetingScreenIsShownWhenOpeningAppFirstTime() {
        assertDisplayed(R.id.greetingBackground)
    }

    @Test
    fun testButtonEnabledAfterBothCheckBoxesHaveBeenChecked() {

        assertDisabled(R.id.greetingGetStartedButton)

        onView(withId(R.id.greetingTermsAndConditionsCheckBox)).perform(clickIn(0, 0))
        onView(withId(R.id.greetingPrivacyPolicyCheckBox)).perform(clickIn(0, 0))

        assertEnabled(R.id.greetingGetStartedButton)
    }

    @Test
    fun testButtonDisabledWhenEitherCheckBoxNotChecked() {
        assertDisabled(R.id.greetingGetStartedButton)

        onView(withId(R.id.greetingTermsAndConditionsCheckBox)).perform(clickIn(0, 0))

        assertDisabled(R.id.greetingGetStartedButton)

        onView(withId(R.id.greetingTermsAndConditionsCheckBox)).perform(clickIn(0, 0))
        onView(withId(R.id.greetingPrivacyPolicyCheckBox)).perform(clickIn(0, 0))
        assertDisabled(R.id.greetingGetStartedButton)
    }

    @Test
    fun testClickingOnLinksOpensCustomTabs() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        assertDisabled(R.id.greetingGetStartedButton)

        clickOn(R.id.greetingTermsAndConditionsCheckBox)

        // Slight delay allowing customTabs to load
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(3))

        intended(IntentMatchers.toPackage("com.android.chrome"))

        // Press the back button to exit custom tabs
        device.pressBack()

        clickOn(R.id.greetingPrivacyPolicyCheckBox)

        // Slight delay allowing customTabs to load
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(3))

        intended(IntentMatchers.toPackage("com.android.chrome"), times(2))

        // Press the back button to exit custom tabs
        device.pressBack()

        assertDisabled(R.id.greetingGetStartedButton)
    }

    @Test
    fun testGetStartedButtonNavigatesToNewScreen() {
        assertDisabled(R.id.greetingGetStartedButton)

        onView(withId(R.id.greetingTermsAndConditionsCheckBox)).perform(clickIn(0, 0))
        onView(withId(R.id.greetingPrivacyPolicyCheckBox)).perform(clickIn(0, 0))

        assertEnabled(R.id.greetingGetStartedButton)

        clickOn(R.id.greetingGetStartedButton)

        assertDisplayed(R.string.new_wallet_activity_xml_welcome_title)
    }

    @Test
    fun testGreetingFragmentDoesNotShowAfterTermsHaveBeenAccepted() {
        assertDisabled(R.id.greetingGetStartedButton)

        onView(withId(R.id.greetingTermsAndConditionsCheckBox)).perform(clickIn(0, 0))
        onView(withId(R.id.greetingPrivacyPolicyCheckBox)).perform(clickIn(0, 0))

        assertEnabled(R.id.greetingGetStartedButton)

        clickOn(R.id.greetingGetStartedButton)

        assertDisplayed(R.string.new_wallet_activity_xml_welcome_title)

        try {
            clickBack()
            fail("Should have thrown NoActivityResumedException")
        } catch (e: NoActivityResumedException) {
            // This is necessary in order to relaunch activity
            Intents.release()
        }

        // Start the app again
        newWalletActivityTestRule.launchActivity(null)

        // Greeting screen should not be displayed and create a wallet screen is now shown at start
        assertDisplayed(R.string.new_wallet_activity_xml_welcome_title)
    }

    @Test
    fun testGreetingFragmentDoesNotShowAfterDeletingWallet() {
        assertDisabled(R.id.greetingGetStartedButton)

        onView(withId(R.id.greetingTermsAndConditionsCheckBox)).perform(clickIn(0, 0))
        onView(withId(R.id.greetingPrivacyPolicyCheckBox)).perform(clickIn(0, 0))

        assertEnabled(R.id.greetingGetStartedButton)

        clickOn(R.id.greetingGetStartedButton)

        assertDisplayed(R.string.new_wallet_activity_xml_welcome_title)

        clickOn(R.id.importWalletFromMnemonicButton)
        writeTo(R.id.inputMnemonicOrSeedTIET, "instrumentationtesting")
        clickOn(R.id.createWalletFromMnemonicButton)

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        assertDisplayed(R.id.toolbar_search)

        clickOn(R.id.menu_bottom_settings)

        assertDisplayed(R.string.more_options_fragment_xml_delete_wallet)

        clickOn(R.id.deleteWalletTextView)

        clickDialogPositiveButton()

        try {
            clickBack()
            fail("Should have thrown NoActivityResumedException")
        } catch (e: NoActivityResumedException) {
            // This is necessary in order to relaunch activity
            Intents.release()
        }

        // Start the app again
        newWalletActivityTestRule.launchActivity(null)

        // Greeting screen should not be displayed and create a wallet screen is now shown at start
        assertDisplayed(R.string.new_wallet_activity_xml_welcome_title)
    }

    companion object {
        // Click in a certain part of a View
        // We need this since we are using a spannable which intercepts the normal click
        // and hence checkbox does not get checked or unchecked
        fun clickIn(x: Int, y: Int): ViewAction {
            return GeneralClickAction(
                Tap.SINGLE,
                CoordinatesProvider { view ->
                    val screenPos = IntArray(2)
                    view?.getLocationOnScreen(screenPos)

                    val screenX = (screenPos[0] + x).toFloat()
                    val screenY = (screenPos[1] + y).toFloat()

                    floatArrayOf(screenX, screenY)
                },
                Press.FINGER,
                InputDevice.SOURCE_MOUSE,
                MotionEvent.BUTTON_PRIMARY
            )
        }
    }
}
