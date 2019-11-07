package com.radixdlt.android.apps.wallet.fragment

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsChecker
import com.radixdlt.android.apps.wallet.helper.DelayHelper
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaListInteractions.clickListItem
import com.schibsted.spain.barista.rule.cleardata.ClearDatabaseRule
import com.schibsted.spain.barista.rule.cleardata.ClearFilesRule
import com.schibsted.spain.barista.rule.cleardata.ClearPreferencesRule
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
class AssetTransactionsFragmentTest {

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
    fun testAssetTransactionsDetailsAreDisplayed() {
        importWallet()
        setPin()
        checkBiometrics()

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        clickListItem(R.id.assetsRecyclerView, 0)

        clickOn(R.id.pullDownDropFrameLayout)

        assertDisplayed(R.string.asset_transactions_fragment_xml_asset_rri)

        DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(3))

        clickOn(R.id.pullDownDropFrameLayout)

        assertDisplayed(R.string.asset_transactions_fragment_xml_your_balance_title)

        // Clean up
        IdlingRegistry.getInstance().unregister(DelayHelper.idlingResource)
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
}
