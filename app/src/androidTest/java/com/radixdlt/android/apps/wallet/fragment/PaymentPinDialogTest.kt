package com.radixdlt.android.apps.wallet.fragment

import android.content.ClipboardManager
import android.content.Context
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsChecker
import com.radixdlt.android.apps.wallet.helper.DelayHelper
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.rule.cleardata.ClearDatabaseRule
import com.schibsted.spain.barista.rule.cleardata.ClearFilesRule
import com.schibsted.spain.barista.rule.cleardata.ClearPreferencesRule
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@MediumTest
class PaymentPinDialogTest {

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

    @After
    fun tearDown() {
        // Clean up
        IdlingRegistry.getInstance().unregister(DelayHelper.idlingResource)
    }

    @Test
    fun testEnteringCorrectPinNavigatesToPaymentStatus() {
        navigateToSetupPin()

        setPin()
        checkBiometrics()
        assertDisplayed(R.id.toolbar_search)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(3))

        navigateToPayScreen()
        inputPaymentDetails()
        clickOn(R.id.paymentInputSendButton)

        assertSummaryMatchesUserInput()
        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        enterPin()

        assertDisplayed(R.id.paymentStatusAmountTextView)
        assertDisplayed("$SUCCESS_AMOUNT $ISO")
    }

    @Test
    fun testEnteringWrongPinShakes() {
        navigateToSetupPin()

        setPin()
        checkBiometrics()
        assertDisplayed(R.id.toolbar_search)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(3))

        navigateToPayScreen()
        inputPaymentDetails()
        clickOn(R.id.paymentInputSendButton)

        assertSummaryMatchesUserInput()
        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        enterWrongPin()

        assertDisplayed(R.string.payment_pin_dialog_pin_header)
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

    private fun checkBiometrics() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        if (BiometricsChecker.getInstance(context).isUsingBiometrics) {
            clickOn(R.id.setupBiometricsNotRightNowButton)
        }
    }

    private fun enterPin() {
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(DELAY_AFTER_CLICK))

        assertDisplayed(R.string.payment_pin_dialog_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(DELAY_AFTER_CLICK))
    }

    private fun enterWrongPin() {
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(DELAY_AFTER_CLICK))

        assertDisplayed(R.string.payment_pin_dialog_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.three)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(DELAY_AFTER_CLICK))
    }

    private fun navigateToPayScreen() {
        assertDisplayed(R.id.toolbar_search)
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(3))
        clickOn(R.id.payButton)
    }

    private fun inputPaymentDetails() {
        writeTo(R.id.paymentInputAddressTIET, ADDRESS_TO)
        writeTo(R.id.paymentInputAmountTIET, SUCCESS_AMOUNT)
    }

    private fun assertSummaryMatchesUserInput() {
        assertDisplayed(R.id.paymentSummaryToAddressTextView, ADDRESS_TO)
        assertDisplayed(R.id.paymentSummaryFromAddressTextView, Identity.api?.address.toString())
        assertDisplayed(SUCCESS_AMOUNT)
        assertDisplayed(ISO)
        assertDisplayed(R.string.payment_summary_fragment_xrd_fee)
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

        val mnemonicStringArray = getMnemonicFromClipboard()
        clickMnemonicInCorrectOrder(mnemonicStringArray)

        clickOn(R.id.confirmBackupWalletConfirmButton)
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

    private fun clickPastGreetingScreen() {
        Espresso.onView(ViewMatchers.withId(R.id.greetingTermsAndConditionsCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        Espresso.onView(ViewMatchers.withId(R.id.greetingPrivacyPolicyCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        BaristaEnabledAssertions.assertEnabled(R.id.greetingGetStartedButton)
        clickOn((R.id.greetingGetStartedButton))
    }

    companion object {
        const val ADDRESS_TO = "9iNGvjXbifbkpPy2252tv8w8QCWnTkixxB1YwrYz1c2AR5xG8VJ"
        const val SUCCESS_AMOUNT = "0.01"
        const val ISO = "XRD"

        const val DELAY_AFTER_CLICK = 3L
    }
}
