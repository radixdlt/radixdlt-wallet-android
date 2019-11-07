package com.radixdlt.android.apps.wallet.activity

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.helper.DelayHelper
import com.radixdlt.android.apps.wallet.helper.DelayHelper.idlingResource
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
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class CreateNewWalletTest {

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
    fun testCreatingNewWallet() {
        createNewWallet()
    }

    companion object {
        fun createNewWallet() {
            // Click on fields to request focus
            clickOn(R.id.createNewWalletButton)
            writeTo(R.id.inputPasswordTIET, "123456")
            writeTo(R.id.inputRepeatPasswordTIET, "123456")
            clickOn(R.id.createWalletButton)

            // Check progress dialog is showing with the correct label
            assertDisplayed(R.string.enter_password_activity_creating_wallet_progress_dialog)

            DelayHelper.waitTime(TimeUnit.SECONDS.toMillis(8))

            // Check MainFragment has loaded by checking account balance title
            assertDisplayed(R.string.tutorial_receive_dialog_title_xml_text_view)

            // Clean up
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
