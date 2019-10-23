package com.radixdlt.android.apps.wallet.fragment

import android.content.ClipboardManager
import android.content.Context
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
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
class SetupPinDialogTest {

    /**
     * [ActivityTestRule] is a JUnit [@Rule][Rule] to launch your activity under test.
     *
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
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
    fun testSetupPinAsksToConfirm() {
        navigateToSetupPin()
        assertDisplayed(R.string.setup_pin_dialog_set_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        assertDisplayed(R.string.setup_pin_dialog_confirm_pin_header)
    }

    @Test
    fun testConfirmPinSucceedsWhenSamePinIsEntered() {
        navigateToSetupPin()
        assertDisplayed(R.string.setup_pin_dialog_set_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        assertDisplayed(R.string.setup_pin_dialog_confirm_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        assertDisplayed(R.id.toolbar_search)
    }

    @Test
    fun testConfirmPinSucceedsWhenDifferentPinIsEntered() {
        navigateToSetupPin()
        assertDisplayed(R.string.setup_pin_dialog_set_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        assertDisplayed(R.string.setup_pin_dialog_confirm_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.one)
        clickOn(R.id.one)
        clickOn(R.id.one)

        assertDisplayed(R.string.setup_pin_dialog_confirm_pin_header)
    }

    @Test
    fun testClickingUpButtonDismissesSetupPinDialog() {
        navigateToSetupPin()
        assertDisplayed(R.string.setup_pin_dialog_set_pin_header)
        // Click on up button on the toolbar to dismiss
        clickOn(navigationIconMatcher())
        assertDisplayed(R.string.confirm_backup_wallet_fragment_title_xml)
    }

    private fun navigateToSetupPin() {
        clickPastGreetingScreen()
        clickOn(R.id.createWalletCreateNewWalletButton)
        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())
        assertDisplayed(R.string.assets_fragment_xml_warning_back_up_message)
        clickOn(R.id.assetsWarningSign)
        assertDisplayed(R.string.backup_wallet_fragment_welcome_title_xml)
        clickOn(R.id.backupWalletCopyImageButton)
        clickOn(R.id.backupWalletNextButton)
        assertDisplayed(R.string.confirm_backup_wallet_fragment_title_xml)

        getMnemonicFromClipboard()

        clickOn(R.id.confirmBackupWalletConfirmButton)
    }

    private fun getMnemonicFromClipboard() {
        lateinit var mnemonicStringArray: Array<String>
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Makes sure that copying happens on the correct thread when testing
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip?.getItemAt(0)?.let {
                mnemonicStringArray = it.text.trim().split(" ").toTypedArray()
                println(mnemonicStringArray.contentToString())
            }
        }
        clickMnemonicInCorrectOrder(mnemonicStringArray)
    }

    private fun clickMnemonicInCorrectOrder(mnemonicStringArray: Array<String>) {
        mnemonicStringArray.forEach {
            clickOn(it)
        }
    }

    private fun clickPastGreetingScreen(){
        Espresso.onView(ViewMatchers.withId(R.id.greetingTermsAndConditionsCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        Espresso.onView(ViewMatchers.withId(R.id.greetingPrivacyPolicyCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        BaristaEnabledAssertions.assertEnabled(R.id.greetingGetStartedButton)
        clickOn((R.id.greetingGetStartedButton))
    }
}
