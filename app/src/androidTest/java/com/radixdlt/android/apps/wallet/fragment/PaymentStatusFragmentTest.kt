package com.radixdlt.android.apps.wallet.fragment

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsChecker
import com.radixdlt.android.apps.wallet.helper.DelayHelper
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
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

@RunWith(AndroidJUnit4::class)
class PaymentStatusFragmentTest {

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
        IdlingRegistry.getInstance().unregister(DelayHelper.idlingResource)
    }

    @Test
    fun testStatusDialogShowsTransactionInformationOnSuccess() {
        importWallet()
        setPin()
        checkBiometrics()

        navigateToPayScreen()
        inputPaymentDetails(SUCCESS_AMOUNT)
        clickOn(R.id.paymentInputSendButton)

        assertSummaryMatchesUserInput(SUCCESS_AMOUNT)
        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        enterPin()

        assertDisplayed(R.id.paymentStatusAmountTextView)
        assertDisplayed("$SUCCESS_AMOUNT $ISO")
    }

    @Test
    @AllowFlaky(attempts = 5)
    fun testExplorerButtonOpensCustomTabsOnSuccess() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        importWallet()
        setPin()
        checkBiometrics()

        navigateToPayScreen()
        inputPaymentDetails(SUCCESS_AMOUNT)

        clickOn(R.id.paymentInputSendButton)

        assertSummaryMatchesUserInput(SUCCESS_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        enterPin()

        assertDisplayed(R.id.paymentStatusAmountTextView)
        assertDisplayed("$SUCCESS_AMOUNT $ISO")

        clickOn(R.id.paymentStatusActionButton)

        intended(toPackage("com.android.chrome"))

        // Press the back button to exit custom tabs
        device.pressBack()

        assertDisplayed(R.id.paymentStatusAmountTextView)
    }

    @Test
    fun testTryAgainButtonOnFailure() {
        importWallet()
        setPin()
        checkBiometrics()

        navigateToPayScreen()
        inputPaymentDetails(FAILURE_AMOUNT)
        clickOn(R.id.paymentInputSendButton)

        assertSummaryMatchesUserInput(FAILURE_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        enterPin()

        assertDisplayed(R.id.paymentStatusFailureTextView)

        clickOn(R.id.paymentStatusActionButton)
    }

    @Test
    fun testCloseButtonOnSuccess() {
        importWallet()
        setPin()
        checkBiometrics()

        navigateToPayScreen()
        inputPaymentDetails(SUCCESS_AMOUNT)
        clickOn(R.id.paymentInputSendButton)

        assertSummaryMatchesUserInput(SUCCESS_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        enterPin()

        assertDisplayed(R.id.paymentStatusAmountTextView)

        clickOn(R.id.paymentStatusCloseButton)

        assertDisplayed(R.id.toolbar_search)
    }

    @Test
    fun testCloseButtonOnFailure() {
        importWallet()
        setPin()
        checkBiometrics()

        navigateToPayScreen()
        inputPaymentDetails(FAILURE_AMOUNT)
        clickOn(R.id.paymentInputSendButton)

        assertSummaryMatchesUserInput(FAILURE_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        enterPin()

        assertDisplayed(R.id.paymentStatusFailureTextView)

        clickOn(R.id.paymentStatusCloseButton)

        assertDisplayed(R.id.toolbar_search)
    }

    @Test
    fun testStatusDialogShowsInformationMessageOnFailure() {
        importWallet()
        setPin()
        checkBiometrics()

        navigateToPayScreen()
        inputPaymentDetails(FAILURE_AMOUNT)
        clickOn(R.id.paymentInputSendButton)

        assertSummaryMatchesUserInput(FAILURE_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        enterPin()

        assertDisplayed(R.id.paymentStatusFailureTextView)
    }

    private fun enterPin() {
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(DELAY))

        assertDisplayed(R.string.payment_pin_dialog_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(DELAY))
    }

    private fun navigateToPayScreen() {
        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())
        assertDisplayed(R.id.toolbar_search)
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(3))
        clickOn(R.id.payButton)
    }

    private fun inputPaymentDetails(amount: String) {
        writeTo(R.id.paymentInputAddressTIET, ADDRESS_TO)
        writeTo(R.id.paymentInputAmountTIET, amount)
    }

    private fun assertSummaryMatchesUserInput(amount: String) {
        assertDisplayed(R.id.paymentSummaryToAddressTextView, ADDRESS_TO)
        assertDisplayed(R.id.paymentSummaryFromAddressTextView, Identity.api?.address.toString())
        assertDisplayed(amount)
        assertDisplayed(ISO)
        assertDisplayed(R.string.payment_summary_fragment_xrd_fee)
    }

    private fun importWallet() {
        Espresso.onView(ViewMatchers.withId(R.id.greetingTermsAndConditionsCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        Espresso.onView(ViewMatchers.withId(R.id.greetingPrivacyPolicyCheckBox))
            .perform(GreetingFragmentTest.clickIn(0, 0))
        BaristaEnabledAssertions.assertEnabled(R.id.greetingGetStartedButton)
        clickOn(R.id.greetingGetStartedButton)
        assertDisplayed(R.string.create_wallet_fragment_welcome_title_xml)
        clickOn(R.id.createWalletImportWalletButton)

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val mnemonic = "dance taxi nature account nurse split picture wage frame promote fluid reason"

        // Makes sure that copying happens on the correct thread when testing
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            copyToClipboard(context, mnemonic)
        }

        clickOn(R.id.restoreWalletPasteImageButton)

        clickOn(R.id.restoreWalletConfirmButton)
    }

    private fun setPin() {
        assertDisplayed(R.string.setup_pin_dialog_set_pin_header)
        clickOn(R.id.one)
        clickOn(R.id.two)
        clickOn(R.id.three)
        clickOn(R.id.four)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(DELAY))

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

    companion object {
        const val ADDRESS_TO = "9iNGvjXbifbkpPy2252tv8w8QCWnTkixxB1YwrYz1c2AR5xG8VJ"
        const val SUCCESS_AMOUNT = "0.01"
        const val FAILURE_AMOUNT = "1.00"
        const val ISO = "XRD"

        const val DELAY = 3L
    }
}
