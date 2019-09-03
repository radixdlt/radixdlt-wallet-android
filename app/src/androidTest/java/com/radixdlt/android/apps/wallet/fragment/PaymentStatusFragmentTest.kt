package com.radixdlt.android.apps.wallet.fragment

import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.helper.DelayHelper
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.NewWalletActivity
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaSpinnerInteractions.clickSpinnerItem
import com.schibsted.spain.barista.rule.cleardata.ClearDatabaseRule
import com.schibsted.spain.barista.rule.cleardata.ClearFilesRule
import com.schibsted.spain.barista.rule.cleardata.ClearPreferencesRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PaymentStatusFragmentTest {

    @get:Rule
    var newWalletActivityTestRule = IntentsTestRule(NewWalletActivity::class.java)

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
    fun testStatusDialogShowsTransactionInformationOnSuccess() {
        createWallet()

        navigateToPayScreen()
        inputPaymentDetails(SUCCESS_AMOUNT)
        clickOn(R.id.sendButton)

        assertSummaryMatchesUserInput(SUCCESS_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(5))

        assertDisplayed(R.id.paymentStatusAmountTextView)
        assertDisplayed("$SUCCESS_AMOUNT $ISO")
    }

    @Test
    fun testExplorerButtonOpensCustomTabsOnSuccess() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        createWallet()

        navigateToPayScreen()
        inputPaymentDetails(SUCCESS_AMOUNT)

        clickOn(R.id.sendButton)

        assertSummaryMatchesUserInput(SUCCESS_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(5))

        assertDisplayed(R.id.paymentStatusAmountTextView)
        assertDisplayed("$SUCCESS_AMOUNT $ISO")

        clickOn(R.id.paymentStatusActionButton)

        // Slight delay allowing customTabs to load
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(4))

        intended(toPackage("com.android.chrome"))

        // Press the back button to exit custom tabs
        device.pressBack()

        assertDisplayed(R.id.paymentStatusAmountTextView)
    }

    @Test
    fun testTryAgainButtonOnFailure() {
        createWallet()

        navigateToPayScreen()
        inputPaymentDetails(FAILURE_AMOUNT)
        clickOn(R.id.sendButton)

        assertSummaryMatchesUserInput(FAILURE_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(5))

        assertDisplayed(R.id.paymentStatusFailureTextView)

        clickOn(R.id.paymentStatusActionButton)
    }

    @Test
    fun testCloseButtonOnSuccess() {
        createWallet()

        navigateToPayScreen()
        inputPaymentDetails(SUCCESS_AMOUNT)
        clickOn(R.id.sendButton)

        assertSummaryMatchesUserInput(SUCCESS_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(5))

        assertDisplayed(R.id.paymentStatusAmountTextView)

        clickOn(R.id.paymentStatusCloseButton)

        assertDisplayed(R.id.toolbar_search)
    }

    @Test
    fun testCloseButtonOnFailure() {
        createWallet()

        navigateToPayScreen()
        inputPaymentDetails(FAILURE_AMOUNT)
        clickOn(R.id.sendButton)

        assertSummaryMatchesUserInput(FAILURE_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(5))

        assertDisplayed(R.id.paymentStatusFailureTextView)

        clickOn(R.id.paymentStatusCloseButton)

        assertDisplayed(R.id.toolbar_search)
    }

    @Test
    fun testStatusDialogShowsInformationMessageOnFailure() {
        createWallet()

        navigateToPayScreen()
        inputPaymentDetails(FAILURE_AMOUNT)
        clickOn(R.id.sendButton)

        assertSummaryMatchesUserInput(FAILURE_AMOUNT)

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(5))

        assertDisplayed(R.id.paymentStatusFailureTextView)
    }

    private fun createWallet() {
        clickOn(R.id.importWalletFromMnemonicButton)
        writeTo(R.id.inputMnemonicOrSeedTIET, "instrumentationtest")
        clickOn(R.id.createWalletFromMnemonicButton)
    }

    private fun navigateToPayScreen() {
        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())
        assertDisplayed(R.id.toolbar_search)
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(50))
        clickOn(R.id.payButton)
    }

    private fun inputPaymentDetails(amount: String) {
        writeTo(R.id.inputAddressTIET, ADDRESS_TO)
        writeTo(R.id.amountEditText, amount)
        clickSpinnerItem(R.id.tokenTypeSpinner, 0)
    }

    private fun assertSummaryMatchesUserInput(amount: String) {
        assertDisplayed(R.id.paymentSummaryToAddressTextView, ADDRESS_TO)
        assertDisplayed(R.id.paymentSummaryFromAddressTextView, Identity.api?.address.toString())
        assertDisplayed(amount)
        assertDisplayed(ISO)
        assertDisplayed(R.string.payment_summary_fragment_xrd_fee)
    }

    companion object {
        const val ADDRESS_TO = "9iNGvjXbifbkpPy2252tv8w8QCWnTkixxB1YwrYz1c2AR5xG8VJ"
        const val SUCCESS_AMOUNT = "0.01"
        const val FAILURE_AMOUNT = "30"
        const val ISO = "XRD"
    }
}
