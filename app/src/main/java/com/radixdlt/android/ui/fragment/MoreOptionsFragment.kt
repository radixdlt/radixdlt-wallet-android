package com.radixdlt.android.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.radixdlt.android.BuildConfig
import com.radixdlt.android.R
import com.radixdlt.android.data.model.message.MessagesDao
import com.radixdlt.android.data.model.transaction.TransactionsDao
import com.radixdlt.android.helper.CustomTabsHelper
import com.radixdlt.android.helper.WebviewFallback
import com.radixdlt.android.identity.Identity
import com.radixdlt.android.ui.activity.BaseActivity
import com.radixdlt.android.ui.activity.NewWalletActivity
import com.radixdlt.android.ui.dialog.AutoLockTimeOutDialog
import com.radixdlt.android.ui.dialog.DeleteWalletDialog
import com.radixdlt.android.util.QueryPreferences
import com.radixdlt.android.util.URL_REPORT_ISSUE
import com.radixdlt.android.util.Vault
import com.radixdlt.android.util.multiClickingPrevention
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_more_options.*
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MoreOptionsFragment : Fragment() {

    @Inject
    lateinit var transactionsDao: TransactionsDao

    @Inject
    lateinit var messagesDao: MessagesDao

    lateinit var ctx: Context

    private lateinit var customTabsIntent: CustomTabsIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_more_options, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context

        val isPasswordEnabled = initialisePasswordSwitch()
        passwordEnabledUI(isPasswordEnabled)
        autoLockTimeOutTimeTextView.text = displayAutoLockTime()
        createCustomTabsBuilder()
        setClickListeners()
    }

    private fun initialisePasswordSwitch(): Boolean {
        val isPasswordEnabled = QueryPreferences.getPrefPasswordEnabled(ctx)
        enablePasswordSwitch.isChecked = isPasswordEnabled
        enablePasswordSwitch.setOnCheckedChangeListener { switchView, checked ->
            QueryPreferences.setPrefPasswordEnabled(switchView.context, checked)
            passwordEnabledUI(checked)
        }
        return isPasswordEnabled
    }

    private fun setClickListeners() {
        setDeleteWalletClickListener()
        setExportWalletClickListener()
        setAutoLockTimeOutClickListener()
        setReportAnIssueClickListener()
    }

    private fun createCustomTabsBuilder() {
        customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
            .setShowTitle(true)
            .enableUrlBarHiding()
            .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow_back))
            .build()
    }

    private fun setAutoLockTimeOutClickListener() {
        autoLockTimeOutLayout.setOnClickListener {
            if (multiClickingPrevention(1000)) return@setOnClickListener
            val autoLockTimeOutDialog = AutoLockTimeOutDialog.newInstance()
            autoLockTimeOutDialog.setTargetFragment(
                this@MoreOptionsFragment, REQUEST_CODE_SET_TIME_OUT
            )
            autoLockTimeOutDialog.show(fragmentManager, null)
        }
    }

    private fun setExportWalletClickListener() {
        exportWalletTextView.setOnClickListener {
            BaseActivity.openedShareDialog = true

            val exportIntent = Intent(Intent.ACTION_SEND)
            exportIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            exportIntent.type = "text/plain"

            val file = File(activity?.filesDir, "keystore.key")
            val apkURI = FileProvider.getUriForFile(
                activity!!, BuildConfig.APPLICATION_ID + ".provider", file
            )
            exportIntent.putExtra(Intent.EXTRA_STREAM, apkURI)
            startActivity(
                Intent.createChooser(
                    exportIntent,
                    activity!!.getString(R.string.more_options_fragment_export_intent_chooser_title)
                )
            )
        }
    }

    private fun setDeleteWalletClickListener() {
        deleteWalletTextView.setOnClickListener {

            val deleteWalletDialog = DeleteWalletDialog.newInstance()
            deleteWalletDialog.setTargetFragment(
                this@MoreOptionsFragment, REQUEST_CODE_DELETE_WALLET
            )
            deleteWalletDialog.show(fragmentManager, "DELETE_WALLET_DIALOG")
        }
    }

    private fun setReportAnIssueClickListener() {
        reportAnIssueTextView.setOnClickListener {
            BaseActivity.openedCustomTabs = true
            openCustomTab(
                activity!!, customTabsIntent, Uri.parse(URL_REPORT_ISSUE),
                WebviewFallback()
            )
        }
    }

    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
     *
     * @param activity The host activity.
     * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
     * @param uri the Uri to be opened.
     * @param fallback a CustomTabFallback to be used if Custom Tabs is not available.
     */
    private fun openCustomTab(
        activity: Activity,
        customTabsIntent: CustomTabsIntent,
        uri: Uri,
        fallback: CustomTabsHelper.CustomTabFallback?
    ) {
        val packageName = CustomTabsHelper.getPackageNameToUse(activity)

        // If we cant find a package name, it means theres no browser that supports
        // Chrome Custom Tabs installed. So, we fallback to the webview
        if (packageName == null) {
            fallback?.openUri(activity, uri)
        } else {
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(activity, uri)
        }
    }

    private fun passwordEnabledUI(isPasswordEnabled: Boolean) {
        autoLockTimeOutLayout.isClickable = isPasswordEnabled
        autoLockTimeOutLayout.isEnabled = isPasswordEnabled

        if (isPasswordEnabled) {
            autoLockTimeOutTextView.setTextColor(
                ContextCompat.getColor(ctx, R.color.materialGrey700)
            )
            autoLockTimeOutTimeTextView.setTextColor(
                ContextCompat.getColor(ctx, R.color.colorAccent)
            )
        } else {
            autoLockTimeOutTextView.setTextColor(
                ContextCompat.getColor(
                    ctx, R.color.notVeryLightGray
                )
            )
            autoLockTimeOutTimeTextView.setTextColor(
                ContextCompat.getColor(ctx, R.color.notVeryLightGray)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == REQUEST_CODE_SET_TIME_OUT) {
            autoLockTimeOutTimeTextView.text = displayAutoLockTime()
        } else if (requestCode == REQUEST_CODE_DELETE_WALLET) {
            Vault.resetKey()
            QueryPreferences.setPrefAddress(activity!!, "")
            QueryPreferences.setPrefPasswordEnabled(activity!!, true)
            QueryPreferences.setPrefAutoLockTimeOut(activity!!, 2000)
            Identity.clear()
            val myKeyFile = File(activity!!.filesDir, "keystore.key")
            myKeyFile.delete()

            Completable.fromAction(::deleteTables)
                .subscribeOn(Schedulers.single())
                .subscribe()

            activity!!.startActivity<NewWalletActivity>()
            activity!!.finish()
        }
    }

    private fun deleteTables() {
        transactionsDao.deleteTable()
        messagesDao.deleteTable()
    }

    private fun displayAutoLockTime(): String {
        val lockTime = QueryPreferences.getPrefAutoLockTimeOut(activity!!)

        return String.format(
            "%02d hr, %02d min, %02d sec",
            TimeUnit.MILLISECONDS.toHours(lockTime),
            TimeUnit.MILLISECONDS.toMinutes(lockTime) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(lockTime)),
            TimeUnit.MILLISECONDS.toSeconds(lockTime) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(lockTime))
        )
    }

    override fun onResume() {
        super.onResume()
        BaseActivity.openedShareDialog = false
        BaseActivity.openedCustomTabs = false
    }

    companion object {
        private const val REQUEST_CODE_SET_TIME_OUT = 0
        private const val REQUEST_CODE_DELETE_WALLET = 1
    }
}
