package com.radixdlt.android.apps.wallet.fragment

import android.content.ClipboardManager
import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsChecker
import com.radixdlt.android.apps.wallet.helper.DelayHelper
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
import com.schibsted.spain.barista.assertion.BaristaCheckedAssertions.assertChecked
import com.schibsted.spain.barista.assertion.BaristaCheckedAssertions.assertUnchecked
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions.assertEnabled
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import com.schibsted.spain.barista.rule.cleardata.ClearDatabaseRule
import com.schibsted.spain.barista.rule.cleardata.ClearFilesRule
import com.schibsted.spain.barista.rule.cleardata.ClearPreferencesRule
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
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
@SmallTest
class SettingsFragmentTest {

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
    fun testChangePinSucceeds() {
        navigateToSetupPinFromWarningSign()

        setPin()
        if (checkBiometrics()) {
            doNotUseBiometrics()
        }

        clickOn(R.id.menu_bottom_settings)
        scrollTo(R.string.settings_fragment_xml_change_pin)
        clickOn(R.id.settingsChangePinTextView)

        assertDisplayed(R.string.change_pin_dialog_enter_current_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        assertDisplayed(R.string.change_pin_dialog_new_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.one)
        clickOn(R.id.one)
        clickOn(R.id.one)

        assertDisplayed(R.string.change_pin_dialog_confirm_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.one)
        clickOn(R.id.one)
        clickOn(R.id.one)

        assertDisplayed(R.string.settings_fragment_change_pin_success_snackbar)
    }

    @Test
    fun testWrongCurrentPinShakes() {
        navigateToSetupPinFromWarningSign()

        setPin()
        if (checkBiometrics()) {
            doNotUseBiometrics()
        }

        clickOn(R.id.menu_bottom_settings)
        scrollTo(R.string.settings_fragment_xml_change_pin)
        clickOn(R.id.settingsChangePinTextView)

        assertDisplayed(R.string.change_pin_dialog_enter_current_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.two)
        clickOn(R.id.two)

        assertDisplayed(R.string.change_pin_dialog_enter_current_pin_header)
    }

    @Test
    fun testWrongConfirmationPinShakes() {
        navigateToSetupPinFromWarningSign()

        setPin()
        if (checkBiometrics()) {
            doNotUseBiometrics()
        }

        clickOn(R.id.menu_bottom_settings)
        scrollTo(R.string.settings_fragment_xml_change_pin)
        clickOn(R.id.settingsChangePinTextView)

        assertDisplayed(R.string.change_pin_dialog_enter_current_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        assertDisplayed(R.string.change_pin_dialog_new_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        assertDisplayed(R.string.change_pin_dialog_confirm_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.one)
        clickOn(R.id.one)
        clickOn(R.id.two)

        assertDisplayed(R.string.change_pin_dialog_confirm_pin_header)
    }

    @Test
    fun testBackingUpWalletShowsChangePinOptions() {
        navigateToSetupPinFromSettings()

        setPin()
        if (checkBiometrics()) {
            doNotUseBiometrics()
        }

        assertDisplayed(R.string.settings_fragment_xml_change_pin)
        if (checkBiometrics()) {
            assertDisplayed(R.string.settings_fragment_xml_use_biometrics)
            assertUnchecked(R.id.settingsUseBiometricsSwitch)
        }
    }

    @Test
    fun testIfDeviceHasBiometricsEnablingItWillShowItDuringPayment() {
        navigateToSetupPinFromSettings()

        setPin()
        if (checkBiometrics()) {
            doNotUseBiometrics()
        }

        assertDisplayed(R.string.settings_fragment_xml_change_pin)

        if (checkBiometrics()) {
            assertDisplayed(R.string.settings_fragment_xml_use_biometrics)
            assertUnchecked(R.id.settingsUseBiometricsSwitch)
            clickOn(R.id.settingsUseBiometricsSwitch)
            assertChecked(R.id.settingsUseBiometricsSwitch)
            navigateToPayScreen()
            inputPaymentDetails()
            clickOn(R.id.paymentInputSendButton)

            assertSummaryMatchesUserInput()
            clickOn(R.id.paymentSummaryConfirmAndSendButton)

            // Press the back button to exit app
            val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            mDevice.pressBack()

            DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(3))

            assertDisplayed(R.string.payment_biometrics_dialog_xml_text_view)
        } else {
            assertDisplayed(R.string.settings_fragment_xml_change_pin)
            assertNotDisplayed(R.string.settings_fragment_xml_use_biometrics)
        }
    }

    @Test
    fun testDeleteWalletShowsDeleteWalletDialog() {
        createWallet()

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        clickOn(R.id.menu_bottom_settings)
        scrollTo(R.string.settings_fragment_xml_delete_wallet)
        assertDisplayed(R.string.settings_fragment_xml_delete_wallet)

        clickOn(R.id.settingsDeleteWalletTextView)
        assertDisplayed(R.string.delete_wallet_dialog_xml_title_text_view)
    }

    @Test
    fun testDeleteWalletConfirmationReturnsToCreateWalletScreen() {
        createWallet()

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        clickOn(R.id.menu_bottom_settings)
        scrollTo(R.string.settings_fragment_xml_delete_wallet)
        assertDisplayed(R.string.settings_fragment_xml_delete_wallet)

        clickOn(R.id.settingsDeleteWalletTextView)

        clickOn(R.id.deleteWalletButton)

        assertDisplayed(R.string.create_wallet_fragment_welcome_title_xml)
    }

    @Test
    fun testDeleteWalletCancellationDismissesDialog() {
        createWallet()

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        clickOn(R.id.menu_bottom_settings)
        scrollTo(R.string.settings_fragment_xml_delete_wallet)
        assertDisplayed(R.string.settings_fragment_xml_delete_wallet)

        clickOn(R.id.settingsDeleteWalletTextView)

        clickOn(R.id.cancelButton)

        assertDisplayed(R.string.settings_fragment_xml_delete_wallet)
    }

    @Test
    @AllowFlaky(attempts = 5)
    fun testOpenReportIssueWebView() {
        createWallet()

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        clickOn(R.id.menu_bottom_settings)
        scrollTo(R.string.settings_fragment_xml_report_an_issue)
        assertDisplayed(R.string.settings_fragment_xml_report_an_issue)

        clickOn(R.id.settingsReportAnIssueTextView)

        intended(toPackage("com.android.chrome"))

        // Press the back button to exit app
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mDevice.pressBack()
    }

    private fun assertSummaryMatchesUserInput() {
        assertDisplayed(R.id.paymentSummaryToAddressTextView, PaymentPinDialogTest.ADDRESS_TO)
        assertDisplayed(R.id.paymentSummaryFromAddressTextView, Identity.api?.address.toString())
        assertDisplayed(PaymentPinDialogTest.SUCCESS_AMOUNT)
        assertDisplayed(PaymentPinDialogTest.ISO)
        assertDisplayed(R.string.payment_summary_fragment_xrd_fee)
    }

    private fun navigateToPayScreen() {
        clickOn(R.id.menu_bottom_assets)
        assertDisplayed(R.id.toolbar_search)
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(PaymentPinDialogTest.DELAY))
        clickOn(R.id.payButton)
    }

    private fun inputPaymentDetails() {
        writeTo(R.id.paymentInputAddressTIET, PaymentPinDialogTest.ADDRESS_TO)
        writeTo(R.id.paymentInputAmountTIET, PaymentPinDialogTest.SUCCESS_AMOUNT)
    }

    private fun setPin() {
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
    }

    private fun checkBiometrics(): Boolean {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return BiometricsChecker.getInstance(context).isUsingBiometrics
    }

    private fun doNotUseBiometrics() {
        clickOn(R.id.setupBiometricsNotRightNowButton)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(5))
    }

    private fun navigateToSetupPinFromWarningSign() {
        clickPastGreetingScreen()
        clickOn(R.id.createWalletCreateNewWalletButton)
        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())
        assertDisplayed(R.string.assets_fragment_xml_warning_back_up_message)
        clickOn(R.id.assetsWarningSign)
        assertDisplayed(R.string.backup_wallet_fragment_welcome_title_xml)
        clickOn(R.id.backupWalletCopyImageButton)
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(2))
        clickOn(R.id.backupWalletNextButton)
        assertDisplayed(R.string.confirm_backup_wallet_fragment_title_xml)

        val mnemonicStringArray = getMnemonicFromClipboard()
        clickMnemonicInCorrectOrder(mnemonicStringArray)

        clickOn(R.id.confirmBackupWalletConfirmButton)
    }

    private fun navigateToSetupPinFromSettings() {
        clickPastGreetingScreen()
        clickOn(R.id.createWalletCreateNewWalletButton)
        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())
        assertDisplayed(R.string.assets_fragment_xml_warning_back_up_message)
        clickOn(R.id.menu_bottom_settings)
        clickOn(R.id.settingsBackupWalletTextView)
        assertDisplayed(R.string.backup_wallet_fragment_welcome_title_xml)
        clickOn(R.id.backupWalletCopyImageButton)
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(2))
        clickOn(R.id.backupWalletNextButton)
        assertDisplayed(R.string.confirm_backup_wallet_fragment_title_xml)

        val mnemonicStringArray = getMnemonicFromClipboard()
        clickMnemonicInCorrectOrder(mnemonicStringArray)

        clickOn(R.id.confirmBackupWalletConfirmButton)
    }

    private fun clickPastGreetingScreen() {
        onView(ViewMatchers.withId(R.id.greetingTermsAndConditionsCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        onView(ViewMatchers.withId(R.id.greetingPrivacyPolicyCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        assertEnabled(R.id.greetingGetStartedButton)
        clickOn((R.id.greetingGetStartedButton))
    }

    private fun getMnemonicFromClipboard(): Array<String> {
        lateinit var mnemonicStringArray: Array<String>
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Makes sure that getting from clipboard happens on the correct thread when testing
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip?.getItemAt(0)?.let {
                mnemonicStringArray = it.text.trim().split(" ").toTypedArray()
            }
        }
        return mnemonicStringArray
    }

    private fun clickMnemonicInCorrectOrder(mnemonicStringArray: Array<String>) {
        mnemonicStringArray.forEach {
            clickOn(it)
        }
    }

    private fun createWallet() {
        onView(ViewMatchers.withId(R.id.greetingTermsAndConditionsCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        onView(ViewMatchers.withId(R.id.greetingPrivacyPolicyCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        assertEnabled(R.id.greetingGetStartedButton)
        clickOn(R.id.greetingGetStartedButton)
        assertDisplayed(R.string.create_wallet_fragment_welcome_title_xml)
        clickOn(R.id.createWalletCreateNewWalletButton)
    }
}
