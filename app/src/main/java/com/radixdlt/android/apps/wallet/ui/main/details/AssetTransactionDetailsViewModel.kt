package com.radixdlt.android.apps.wallet.ui.main.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.util.getOrdinal
import com.radixdlt.client.core.atoms.particles.RRI
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.math.RoundingMode
import java.util.Locale

class AssetTransactionDetailsViewModel : ViewModel() {

    private val _assetTransactionDetailsAction = MutableLiveData<AssetTransactionDetailsAction>()
    val assetTransactionDetailsAction: LiveData<AssetTransactionDetailsAction> get() = _assetTransactionDetailsAction

    private val _accountName = MutableLiveData<String>()
    val accountName: LiveData<String> get() = _accountName

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val _amount = MutableLiveData<String>()
    val amount: LiveData<String> get() = _amount

    private val _note = MutableLiveData<String>()
    val note: LiveData<String> get() = _note

    private val _rri = MutableLiveData<String>()
    val rri: LiveData<String> get() = _rri

    private val _tokenSymbol = MutableLiveData<String>()
    val tokenSymbol: LiveData<String> get() = _tokenSymbol

    private val _noteVisible = MutableLiveData<Boolean>()
    val noteVisible: LiveData<Boolean> get() = _noteVisible

    private val _date = MutableLiveData<String>()
    val date: LiveData<String> get() = _date

    private val _sent = MutableLiveData<Boolean>()
    val sent: LiveData<Boolean> get() = _sent

    fun showTransactionDetails(args: AssetTransactionDetailsFragmentArgs) {
        val transaction = args.transaction
        _accountName.value = "" // Future when supporting multi account
        _address.value = transaction.address

        val amount = transaction.amount
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()

        _amount.value = if (transaction.sent) amount else "+ $amount"

        _rri.value = transaction.rri
        val rri = RRI.fromString(transaction.rri)
        _tokenSymbol.value = rri.name

        // Show or hide note
        if (transaction.message.isNullOrEmpty()) {
            _noteVisible.value = false
        } else {
            _note.value = transaction.message
            _noteVisible.value = true
        }

        _date.value = formatDate(transaction.timestamp)

        _sent.value = transaction.sent
    }

    fun onCopyAddressClickListener(address: String) {
        _assetTransactionDetailsAction.value = AssetTransactionDetailsAction.CopyToClipboard(address)
    }

    private fun formatDate(dateUnix: Long): String {
        val localDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(dateUnix), ZoneId.systemDefault()
        )
        var displayValue = localDateTime.format(
            DateTimeFormatter.ofPattern("EEEE, ? MMMM, yyyy", Locale.getDefault())
        )
        val day = localDateTime.format(
            DateTimeFormatter.ofPattern("dd", Locale.getDefault())
        )
        val dayOrdinal = day.toInt().getOrdinal()
        displayValue = displayValue.replace("?", dayOrdinal!!)

        return displayValue
    }
}
