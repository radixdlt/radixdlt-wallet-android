package com.radixdlt.android.apps.wallet.dialog

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.helper.clickOn
import com.radixdlt.android.apps.wallet.helper.navigationIconMatcher
import com.radixdlt.android.apps.wallet.ui.activity.NewWalletActivity
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
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
    fun testTutorialRecieveDialogIsShown() {
        clickOn(R.id.importWalletFromMnemonicButton)
        writeTo(R.id.inputMnemonicOrSeedTIET, "instrumentationtesting")
        clickOn(R.id.createWalletFromMnemonicButton)

        // Check MainFragment has loaded by checking account balance title
        assertDisplayed(R.string.tutorial_receive_dialog_title_xml_text_view)
    }

    @Test
    fun testTutorialRecieveDialogCloseButtonDismissesDialog() {
        clickOn(R.id.importWalletFromMnemonicButton)
        writeTo(R.id.inputMnemonicOrSeedTIET, "instrumentationtesting")
        clickOn(R.id.createWalletFromMnemonicButton)

        // Click on x on the toolbar to dismiss
        clickOn(navigationIconMatcher())

        assertDisplayed(R.id.toolbar_search)
    }

    @Test
    fun testTutorialReceiveDialogClickOnReceiveOpensReceiveScreen() {
        clickOn(R.id.importWalletFromMnemonicButton)
        writeTo(R.id.inputMnemonicOrSeedTIET, "instrumentationtesting")
        clickOn(R.id.createWalletFromMnemonicButton)

        clickOn(R.id.receiveButton)

        assertDisplayed(R.string.receive_radix_dialog_title)
    }
}
