package com.radixdlt.android.apps.wallet.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionEntity
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set
import com.radixdlt.client.core.atoms.RadixHash
import com.radixdlt.client.core.util.Base58
import io.github.novacrypto.bip32.ExtendedPrivateKey
import io.github.novacrypto.bip32.networks.Bitcoin
import io.github.novacrypto.bip44.AddressIndex
import io.github.novacrypto.bip44.BIP44
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.bouncycastle.util.encoders.Hex
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.math.BigDecimal
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

object EmptyTextWatcher : TextWatcher {
    override fun afterTextChanged(p0: Editable?) {}
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}

/**
 * @return the start of the day in milliseconds
 */
fun getStartOfDay(time: Long): Long {
    val gregorianCalendar = GregorianCalendar()
    gregorianCalendar.timeInMillis = time

    gregorianCalendar.set(Calendar.HOUR_OF_DAY, 0)
    gregorianCalendar.set(Calendar.MINUTE, 0)
    gregorianCalendar.set(Calendar.SECOND, 0)
    gregorianCalendar.set(Calendar.MILLISECOND, 0)

    return gregorianCalendar.timeInMillis
}

private var lastClickTime: Long = 0

fun multiClickingPrevention(timeToElapse: Long): Boolean {
    // mis-clicking prevention, using threshold of timeToElapse ms
    if (SystemClock.elapsedRealtime() - lastClickTime < timeToElapse) {
        return true
    }
    lastClickTime = SystemClock.elapsedRealtime()

    return false
}

fun hideKeyboard(view: View) {
    val inputMethodManager = view.context.getSystemService(
        Activity.INPUT_METHOD_SERVICE
    ) as InputMethodManager?
    inputMethodManager!!.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showKeyboard(view: View) {
    val imm = view.context.getSystemService(
        Context.INPUT_METHOD_SERVICE
    ) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun isRadixAddress(addressBase58: String): Boolean {

    // TODO: temporary for now quick fix length check
    if (addressBase58.length < 5) return false

    val raw: ByteArray
    try {
        raw = Base58.fromBase58(addressBase58)
    } catch (e: IllegalArgumentException) {
        return false
    }
    val check = RadixHash.of(raw, 0, raw.size - 4)
    for (i in 0..3) {
        if (check.get(i) != raw[raw.size - 4 + i]) {
            return false
        }
    }

    return true
}

fun formatDateDay(dateUnix: Long): String {
    val localDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(dateUnix), ZoneId.systemDefault()
    )
    var displayValue = localDateTime.format(
        DateTimeFormatter.ofPattern("EEEE, ?", Locale.getDefault())
    )
    val day = localDateTime.format(
        DateTimeFormatter.ofPattern("dd", Locale.getDefault())
    )
    val dayOrdinal = day.toInt().getOrdinal()
    displayValue = displayValue.replace("?", dayOrdinal!!)

    return displayValue
}

fun formatDateMonthYear(dateUnix: Long): String {
    val localDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(dateUnix), ZoneId.systemDefault()
    )

    return localDateTime.format(
        DateTimeFormatter.ofPattern("MMMM, yyyy", Locale.getDefault())
    )
}

fun sumStoredTransactions(transactionsEntityOM: List<TransactionEntity>): BigDecimal {
    val sumSent = transactionsEntityOM.asSequence().filter { transactions ->
        transactions.sent
    }.map { transactionEntity ->
        transactionEntity.amount
    }.fold(BigDecimal.ZERO, BigDecimal::add)

    val sumReceived = transactionsEntityOM.asSequence().filterNot { transactions ->
        transactions.sent
    }.map { transactionEntity ->
        transactionEntity.amount
    }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumReceived - sumSent
}

fun copyToClipboard(context: Context, myAddress: String) {
    val clipboard = context.getSystemService(
        Context.CLIPBOARD_SERVICE
    ) as ClipboardManager
    val clip = ClipData.newPlainText("address", myAddress)
    clipboard.setPrimaryClip(clip)
}

/**
 * Resets data to default values
 *
 * @param context
 * */
fun resetData(context: Context) {
    Completable.fromAction { Vault.resetKey() }
        .subscribeOn(Schedulers.computation())
        .subscribe()

    context.defaultPrefs()[Pref.WALLET_BACKED_UP] = false
    context.defaultPrefs()[Pref.PIN_SET] = false
    context.defaultPrefs()[Pref.USE_BIOMETRICS] = false
    context.defaultPrefs()[Pref.AUTHENTICATE_ON_LAUNCH] = false
    Identity.clear()
}

/**
 * Deletes stored keystore file
 *
 * @param context
 * */
private fun deleteKeystoreFile(context: Context) {
    val myKeyFile = File(context.filesDir, "keystore.key")
    myKeyFile.delete()
}

/**
 * Deletes all stored data from currently active wallet
 *
 * @param context
 * */
fun deleteAllData(context: Context) {
    resetData(context)
    deleteKeystoreFile(context)
}

/**
 * Generates a private key from a calculated seed derived from a mnemonic
 *
 * @param seed calculated seed derived from a mnemonic
 * @param index address index
 *
 * @return private key as hex string
 * */
fun privateKeyFromSeedAtIndex(seed: ByteArray, index: Int): String {
    // BIP32: Root Key
    val rootKey = ExtendedPrivateKey.fromSeed(seed, Bitcoin.MAIN_NET)

    // m/44'/0'/0'/0 /0
    val addressIndex = BIP44
        .m()
        .purpose44()
        .coinType(0)
        .account(0)
        .external()
        .address(index)

    // Derive BIP44 ExtendedPrivateKey at desired address index
    val bip44ExtendedPrivateKeyAtAddressIndex =
        rootKey.derive(addressIndex, AddressIndex.DERIVATION)
    val bip44ExtendedPrivateKeyAtAddressIndexHex =
        Hex.toHexString(bip44ExtendedPrivateKeyAtAddressIndex.extendedKeyByteArray())

    // Extract PrivateKey from ExtendedPrivateKey
    val privateKeyStartIndex = 92
    val lengthOfPrivateKeyAsHex = 64
    return bip44ExtendedPrivateKeyAtAddressIndexHex.substring(
        privateKeyStartIndex,
        privateKeyStartIndex + lengthOfPrivateKeyAsHex
    )
}
