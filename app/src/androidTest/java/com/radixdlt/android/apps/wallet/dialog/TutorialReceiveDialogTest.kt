package com.radixdlt.android.apps.wallet.dialog

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.fragment.GreetingFragmentTest
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.ui.start.StartActivity
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.rule.cleardata.ClearDatabaseRule
import com.schibsted.spain.barista.rule.cleardata.ClearFilesRule
import com.schibsted.spain.barista.rule.cleardata.ClearPreferencesRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class TutorialReceiveDialogTest {

    /**
     * [ActivityTestRule] is a JUnit [@Rule][Rule] to launch your activity under test.
     *
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @get:Rule
    var activityScenarioRule = activityScenarioRule<StartActivity>()

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
    fun testTutorialRecieveDialogIsShown() {
        clickPastGreetingScreen()
        clickOn(R.id.createWalletCreateNewWalletButton)

        // Check MainFragment has loaded by checking account balance title
        assertDisplayed(R.string.tutorial_receive_dialog_title_xml_text_view)
    }

    @Test
    fun testTutorialRecieveDialogCloseButtonDismissesDialog() {
        clickPastGreetingScreen()
        clickOn(R.id.createWalletCreateNewWalletButton)

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        assertDisplayed(R.id.toolbarSearch)
    }

    @Test
    fun testTutorialReceiveDialogClickOnReceiveOpensReceiveScreen() {
        clickPastGreetingScreen()
        clickOn(R.id.createWalletCreateNewWalletButton)

        clickOn(R.id.receiveButton)

        assertDisplayed(R.string.receive_radix_dialog_title)
    }

    @Test
    fun testTutorialReceiveDialogDoesNotOpenAfterDeletingWalletAndCreatingNewOne() {
        clickPastGreetingScreen()
        clickOn(R.id.createWalletCreateNewWalletButton)

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        clickOn(R.id.menu_bottom_settings)

        assertDisplayed(R.string.settings_fragment_xml_delete_wallet)

        clickOn(R.id.settingsDeleteWalletTextView)

        clickOn(R.id.deleteWalletButton)

        clickOn(R.id.createWalletCreateNewWalletButton)

        // Toolbar with search should be visible and tutorial dialog
        // should not have shown
        assertDisplayed(R.id.toolbarSearch)
    }

    private fun clickPastGreetingScreen() {
        Espresso.onView(ViewMatchers.withId(R.id.greetingTermsAndConditionsCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        Espresso.onView(ViewMatchers.withId(R.id.greetingPrivacyPolicyCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        BaristaEnabledAssertions.assertEnabled(R.id.greetingGetStartedButton)
        clickOn((R.id.greetingGetStartedButton))
    }
}
