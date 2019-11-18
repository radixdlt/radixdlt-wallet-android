package com.radixdlt.android.apps.wallet.fragment

import android.view.InputDevice
import android.view.MotionEvent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
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
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.helper.DelayHelper
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions.assertDisabled
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions.assertEnabled
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickBack
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.rule.cleardata.ClearDatabaseRule
import com.schibsted.spain.barista.rule.cleardata.ClearFilesRule
import com.schibsted.spain.barista.rule.cleardata.ClearPreferencesRule
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class GreetingFragmentTest {

    @get:Rule
    var newWalletActivityTestRule = activityScenarioRule<StartActivity>()

    // Clear all app's SharedPreferences
    @get:Rule
    var clearPreferencesRule = ClearPreferencesRule()

    // Delete all tables from all the app's SQLite Databases
    @get:Rule
    var clearDatabaseRule = ClearDatabaseRule()

    // Delete all files in getFilesDir() and getCacheDir()
    @get:Rule
    var clearFilesRule = ClearFilesRule()

    @get:Rule(order = 0)
    var flakyRule = FlakyTestRule()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

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
    @AllowFlaky(attempts = 5)
    fun testClickingOnLinksOpensCustomTabs() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // In the event of a flaky test where it has been destroyed
        if (newWalletActivityTestRule.scenario.state == Lifecycle.State.DESTROYED) {
            launchActivity<StartActivity>()
        }

        assertDisabled(R.id.greetingGetStartedButton)

        clickOn(R.id.greetingTermsAndConditionsCheckBox)

        // Slight delay allowing customTabs to load
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(5))

        intended(IntentMatchers.toPackage("com.android.chrome"))

        // Press the back button to exit custom tabs
        device.pressBack()

        clickOn(R.id.greetingPrivacyPolicyCheckBox)

        // Slight delay allowing customTabs to load
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(5))

        intended(IntentMatchers.toPackage("com.android.chrome"), times(2))

        // Press the back button to exit custom tabs
        device.pressBack()

        assertDisabled(R.id.greetingGetStartedButton)

        // Clean up
        IdlingRegistry.getInstance().unregister(DelayHelper.idlingResource)
    }

    @Test
    fun testGetStartedButtonNavigatesToNewScreen() {
        assertDisabled(R.id.greetingGetStartedButton)

        onView(withId(R.id.greetingTermsAndConditionsCheckBox)).perform(clickIn(0, 0))
        onView(withId(R.id.greetingPrivacyPolicyCheckBox)).perform(clickIn(0, 0))

        assertEnabled(R.id.greetingGetStartedButton)

        clickOn(R.id.greetingGetStartedButton)

        assertDisplayed(R.string.create_wallet_fragment_welcome_title_xml)
    }

    @Test
    @Throws(NoActivityResumedException::class)
    fun testGreetingFragmentDoesNotShowAfterTermsHaveBeenAccepted() {
        assertDisabled(R.id.greetingGetStartedButton)

        onView(withId(R.id.greetingTermsAndConditionsCheckBox)).perform(clickIn(0, 0))
        onView(withId(R.id.greetingPrivacyPolicyCheckBox)).perform(clickIn(0, 0))

        assertEnabled(R.id.greetingGetStartedButton)

        clickOn(R.id.greetingGetStartedButton)

        assertDisplayed(R.string.create_wallet_fragment_welcome_title_xml)

        try {
            clickBack()
            fail("Should have thrown NoActivityResumedException")
        } catch (e: NoActivityResumedException) {
            Timber.e(e)
        }

        // Start the app again
        launchActivity<StartActivity>()

        // Greeting screen should not be displayed and create a wallet screen is now shown at start
        assertDisplayed(R.string.create_wallet_fragment_welcome_title_xml)
    }

    @Test
    @Throws(NoActivityResumedException::class)
    fun testGreetingFragmentDoesNotShowAfterDeletingWallet() {
        assertDisabled(R.id.greetingGetStartedButton)

        onView(withId(R.id.greetingTermsAndConditionsCheckBox)).perform(clickIn(0, 0))
        onView(withId(R.id.greetingPrivacyPolicyCheckBox)).perform(clickIn(0, 0))

        assertEnabled(R.id.greetingGetStartedButton)

        clickOn(R.id.greetingGetStartedButton)

        assertDisplayed(R.string.create_wallet_fragment_welcome_title_xml)

        clickOn(R.id.createWalletCreateNewWalletButton)

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        assertDisplayed(R.id.toolbar_search)

        clickOn(R.id.menu_bottom_settings)

        assertDisplayed(R.string.settings_fragment_xml_delete_wallet)

        clickOn(R.id.settingsDeleteWalletTextView)

        clickOn(R.id.deleteWalletButton)

        try {
            clickBack()
            fail("Should have thrown NoActivityResumedException")
        } catch (e: NoActivityResumedException) {
            Timber.e(e)
        }

        // Start the app again
        launchActivity<StartActivity>()

        // Greeting screen should not be displayed and create a wallet screen is now shown at start
        assertDisplayed(R.string.create_wallet_fragment_welcome_title_xml)
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
