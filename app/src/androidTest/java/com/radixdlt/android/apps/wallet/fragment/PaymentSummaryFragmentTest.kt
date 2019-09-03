package com.radixdlt.android.apps.wallet.fragment

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.helper.DelayHelper
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
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
class PaymentSummaryFragmentTest {

    @get:Rule
    var newWalletActivityTestRule: ActivityTestRule<NewWalletActivity> =
        ActivityTestRule(NewWalletActivity::class.java)

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
    fun testCopyAddressButtonCopiesCorrectAddress() {
        createWallet()

        navigateToPayScreen()

        inputPaymentDetails()

        clickOn(R.id.sendButton)

        assertSummaryMatchesUserInput()

        clickOn(R.id.paymentSummaryFromImageButton)
        assertDisplayed(ADDRESS_FROM)

        clickOn(R.id.paymentSummaryToImageButton)
        assertDisplayed(ADDRESS_TO)
    }

    @Test
    fun testNoteIsDisplayedWhenTransactionContainsMessage() {
        createWallet()

        navigateToPayScreen()
        inputPaymentDetails()

        // Write message for transaction
        writeTo(R.id.inputMessageTIET, "Hello World")

        clickOn(R.id.sendButton)

        assertSummaryMatchesUserInput()

        assertDisplayed(R.id.paymentSummaryNoteConstraintLayout)
        assertDisplayed(R.id.paymentSummaryFromNoteTextView, "Hello World")
    }

    @Test
    fun testConfirmAndSendButtonOpensPaymentStatusDialog() {
        createWallet()

        navigateToPayScreen()
        inputPaymentDetails()

        clickOn(R.id.sendButton)

        assertSummaryMatchesUserInput()

        clickOn(R.id.paymentSummaryConfirmAndSendButton)

        assertDisplayed(R.id.paymentStatusLoadingMessageTextView)
    }

    private fun createWallet() {
        clickOn(R.id.importWalletFromMnemonicButton)
        writeTo(R.id.inputMnemonicOrSeedTIET, "hosted")
        clickOn(R.id.createWalletFromMnemonicButton)
    }

    private fun navigateToPayScreen() {
        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())
        assertDisplayed(R.id.toolbar_search)
        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(50))
        clickOn(R.id.payButton)
    }

    private fun inputPaymentDetails() {
        writeTo(R.id.inputAddressTIET, ADDRESS_TO)
        writeTo(R.id.amountEditText, AMOUNT)
        clickSpinnerItem(R.id.tokenTypeSpinner, 1)
    }

    private fun assertSummaryMatchesUserInput() {
        assertDisplayed(R.id.paymentSummaryToAddressTextView, ADDRESS_TO)
        assertDisplayed(R.id.paymentSummaryFromAddressTextView, ADDRESS_FROM)
        assertDisplayed(AMOUNT)
        assertDisplayed(ISO)
        assertDisplayed(R.string.payment_summary_fragment_xrd_fee)
    }

    companion object {
        const val ADDRESS_TO = "9iNGvjXbifbkpPy2252tv8w8QCWnTkixxB1YwrYz1c2AR5xG8VJ"
        const val ADDRESS_FROM = "9gAyDHC8EgYrph36j4zz4Ebty7JwZ66u4PDnfTWmRDhYJzHUmYM"
        const val AMOUNT = "42.00"
        const val ISO = "XRD"
    }
}
