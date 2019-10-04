package com.radixdlt.android.apps.wallet.ui.fragment.moreoptions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.jakewharton.processphoenix.ProcessPhoenix
import com.radixdlt.android.BuildConfig
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.data.model.message.MessagesDao
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionsDao
import com.radixdlt.android.apps.wallet.helper.CustomTabsHelper.openCustomTab
import com.radixdlt.android.apps.wallet.helper.WebviewFallback
import com.radixdlt.android.apps.wallet.ui.activity.BaseActivity
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
import com.radixdlt.android.apps.wallet.ui.dialog.AutoLockTimeOutDialog
import com.radixdlt.android.apps.wallet.ui.dialog.ChooseNetworkDialog
import com.radixdlt.android.apps.wallet.ui.dialog.DeleteWalletDialog
import com.radixdlt.android.apps.wallet.ui.dialog.InputNodeIPAddressDialog
import com.radixdlt.android.apps.wallet.ui.dialog.WarningDialog
import com.radixdlt.android.apps.wallet.util.ALPHANET
import com.radixdlt.android.apps.wallet.util.ALPHANET2
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.URL_REPORT_ISSUE
import com.radixdlt.android.apps.wallet.util.deleteAllData
import com.radixdlt.android.apps.wallet.util.multiClickingPrevention
import com.radixdlt.android.apps.wallet.util.resetData
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
    lateinit var transactionsDao2: TransactionsDao2

    @Inject
    lateinit var messagesDao: MessagesDao

    lateinit var ctx: Context

    private lateinit var customTabsIntent: CustomTabsIntent

    private var universe: Int = 0
    private var randomSelection = false
    private lateinit var ipAddress: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_more_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)

        val isPasswordEnabled = initialisePasswordSwitch()
        passwordEnabledUI(isPasswordEnabled)
        autoLockTimeOutTimeTextView.text = displayAutoLockTime()
        networkSelectedTextView.text = QueryPreferences.getPrefNetwork(activity!!)

        if (QueryPreferences.getPrefIsRandomNodeSelection(activity!!)) {
            inputNodeSelectedTextView.text =
                getString(R.string.more_options_fragment_xml_random_node)
        } else {
            inputNodeSelectedTextView.text = QueryPreferences.getPrefNodeIP(activity!!)
        }
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
        setChooseNetworkClickListener()
        setSelectNodeListener()
        setReportAnIssueClickListener()

        // Quick fix in the event wallet is created with seed or mnemonic
        // No further implementation will be done since V2 of the wallet will be
        // a complete new design and implementation
        if (QueryPreferences.getPrefCreatedByMnemonicOrSeed(activity!!)) {
            enablePasswordSwitch.isEnabled = false
            enablePasswordSwitch.isClickable = false
            selectNodeLayout.isEnabled = false
            selectNodeLayout.isClickable = false
            chooseNetworkLayout.isEnabled = false
            chooseNetworkLayout.isClickable = false
        }
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
                this@MoreOptionsFragment,
                REQUEST_CODE_SET_TIME_OUT
            )
            autoLockTimeOutDialog.show(fragmentManager!!, null)
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
                this@MoreOptionsFragment,
                REQUEST_CODE_DELETE_WALLET
            )
            deleteWalletDialog.show(fragmentManager!!, "DELETE_WALLET_DIALOG")
        }
    }

    private fun setChooseNetworkClickListener() {
        chooseNetworkLayout.setOnClickListener {
            val chooseNetworkDialog = ChooseNetworkDialog.newInstance()
            chooseNetworkDialog.setTargetFragment(
                this@MoreOptionsFragment,
                REQUEST_CODE_CHOOSE_NETWORK
            )
            chooseNetworkDialog.show(fragmentManager!!, "CHOOSE_NETWORK_DIALOG")
        }
    }

    private fun setSelectNodeListener() {
        selectNodeLayout.setOnClickListener {
            val inputNodeIPDialog = InputNodeIPAddressDialog.newInstance()
            inputNodeIPDialog.setTargetFragment(
                this@MoreOptionsFragment,
                REQUEST_CODE_SELECT_NODE
            )
            inputNodeIPDialog.show(fragmentManager!!, "SELECT_NODE_DIALOG")
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

        when (requestCode) {
            REQUEST_CODE_SET_TIME_OUT -> autoLockTimeOutTimeTextView.text = displayAutoLockTime()
            REQUEST_CODE_DELETE_WALLET -> {
                deleteWallet()

                activity!!.startActivity<StartActivity>()
                activity!!.finish()
            }
            REQUEST_CODE_CHOOSE_NETWORK -> {
                universe = data!!.getIntExtra(ChooseNetworkDialog.EXTRA_UNIVERSE, 0)

                val warningDialog = WarningDialog.newInstance(false, null)
                warningDialog.setTargetFragment(
                    this@MoreOptionsFragment,
                    REQUEST_CODE_WARNING
                )
                warningDialog.show(fragmentManager!!, "WARNING_DIALOG")
            }
            REQUEST_CODE_SELECT_NODE -> {
                ipAddress = data!!.getStringExtra(InputNodeIPAddressDialog.EXTRA_ADDRESS)
                randomSelection =
                    data.getBooleanExtra(InputNodeIPAddressDialog.EXTRA_RANDOM_SELECTION, false)
                val warningDialog = WarningDialog.newInstance(true, randomSelection)
                warningDialog.setTargetFragment(
                    this@MoreOptionsFragment,
                    REQUEST_CODE_WARNING
                )
                warningDialog.show(fragmentManager!!, "WARNING_DIALOG")
            }
            REQUEST_CODE_WARNING -> {
                if (data!!.getBooleanExtra(WarningDialog.EXTRA_NODE_SELECTION, false)) {
                    QueryPreferences.setPrefNodeIP(activity!!, ipAddress)
                    QueryPreferences.setPrefRandomNodeSelection(activity!!, randomSelection)
                    ProcessPhoenix.triggerRebirth(activity)
                } else {
                    if (universe == 0) {
                        QueryPreferences.setPrefNetwork(
                            activity!!,
                            ALPHANET
                        )
                        QueryPreferences.setPrefRandomNodeSelection(activity!!, true)
                        QueryPreferences.setPrefNodeIP(activity!!, "")
                    } else {
                        QueryPreferences.setPrefNetwork(
                            activity!!,
                            ALPHANET2
                        )
                        QueryPreferences.setPrefRandomNodeSelection(activity!!, true)
                        QueryPreferences.setPrefNodeIP(activity!!, "")
                    }
                }

                changeUniverse()
                ProcessPhoenix.triggerRebirth(activity)
            }
        }
    }

    private fun changeUniverse() {
        resetData(activity!!)
        deleteTables()
    }

    private fun deleteWallet() {
        deleteAllData(activity!!)
        deleteTables()
    }

    private fun deleteTables() {
        Completable.fromAction {
            transactionsDao.deleteTable()
            transactionsDao2.deleteTable()
            messagesDao.deleteTable()
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
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
        private const val REQUEST_CODE_CHOOSE_NETWORK = 2
        private const val REQUEST_CODE_WARNING = 3
        private const val REQUEST_CODE_SELECT_NODE = 4
    }
}
