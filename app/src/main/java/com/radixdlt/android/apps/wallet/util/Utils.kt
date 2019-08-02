package com.radixdlt.android.apps.wallet.util

import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.SystemClock
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.helper.TextFormatHelper
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.client.core.atoms.RadixHash
import com.radixdlt.client.core.util.Base58
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.math.BigDecimal
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.regex.Pattern

object EmptyTextWatcher : TextWatcher {
    override fun afterTextChanged(p0: Editable?) {}
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}

/**
 * Checks if the device has any active internet connection.
 *
 * @return true device with internet connection, otherwise false.
 */
fun isThereInternet(context: Context): Boolean {
    val isConnected: Boolean

    val connectivityManager = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    isConnected = networkInfo != null && networkInfo.isConnected

    return isConnected
}

/**
 * @return true if the supplied when is today else false
 */
fun isYesterday(`when`: Long): Boolean {
    val time = GregorianCalendar()
    time.timeInMillis = `when`

    val thenDayOfYear = time.get(Calendar.DAY_OF_YEAR)
    val wasLeapYear = time.isLeapYear(Calendar.YEAR)

    time.timeInMillis = System.currentTimeMillis()
    var yesterdayDayOfYear = time.get(Calendar.DAY_OF_YEAR) - 1

    if (yesterdayDayOfYear == 0 && wasLeapYear) {
        yesterdayDayOfYear = 366
    } else if (yesterdayDayOfYear == 0) {
        yesterdayDayOfYear = 365
    }

    return thenDayOfYear == yesterdayDayOfYear
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

/**
 * There is a bug in < jdk 8 which doesn't remove trailing zeros
 * for zero value and since Android doesn't fully utilize java 8
 * where the bug is fixed, the problem persists.
 *
 * Checks if value is 0 and return 0 as a whole value else strips normally.
 *
 * @return BigDecimal without trailing zeros including zero values.
 * @see <a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6480539">https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6480539</a>
 */
fun BigDecimal.fixedStripTrailingZeros(): BigDecimal {
    val zero = BigDecimal.ZERO
    return if (this.compareTo(zero) == 0) {
        zero
    } else {
        this.stripTrailingZeros()
    }
}

fun createProgressDialog(context: Context): ProgressDialog {
    val progressDialog = ProgressDialog(context, R.style.CustomProgressDialogColors)
    progressDialog.setCancelable(false)

    return progressDialog
}

fun setDialogMessage(progressDialog: ProgressDialog, message: String) {
    progressDialog.setMessage(message)
}

fun setProgressDialogVisible(progressDialog: ProgressDialog, show: Boolean) {
    if (show) {
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
    } else {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
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

fun setAddressWithColors(
    ctx: Context,
    myAddress: String,
    @ColorRes middleColorId: Int = R.color.white
): CharSequence {

    val firstFive = myAddress.substring(0, 5)
    val middle = myAddress.substring(5, myAddress.length - 5)
    val lastFive = myAddress.substring(myAddress.length - 5, myAddress.length)

    return TextFormatHelper.normal(
        TextFormatHelper.color(ContextCompat.getColor(ctx, R.color.radixBlue), firstFive),
        TextFormatHelper.color(ContextCompat.getColor(ctx, middleColorId), middle),
        TextFormatHelper.color(
            ContextCompat.getColor(ctx, R.color.colorAccentSecondary), lastFive
        )
    )
}

fun formatCharactersForAmount(string: CharSequence, string2: CharSequence): SpannableStringBuilder {
    val spanTxt = SpannableStringBuilder(string)
    spanTxt.append(".$string2")
    // make the textsize 0.7f times.
    spanTxt.setSpan(
        RelativeSizeSpan(0.7f), spanTxt.length - ".$string2".length,
        spanTxt.length, 0
    )

    return spanTxt
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

fun formatDateTime(dateUnix: Long): String {
    val localDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(dateUnix), ZoneId.systemDefault()
    )
    var displayValue = localDateTime.format(
        DateTimeFormatter.ofPattern("EEEE, ? MMM, HH:mm", Locale.getDefault())
    )
    val day = localDateTime.format(
        DateTimeFormatter.ofPattern("dd", Locale.getDefault())
    )
    val dayOrdinal = day.toInt().getOrdinal()
    displayValue = displayValue.replace("?", dayOrdinal!!)

    return displayValue
}

fun formatDateYear(dateUnix: Long): String {
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

fun copyToClipboard(context: Context, myAddress: String) {
    val clipboard = context.getSystemService(
        Context.CLIPBOARD_SERVICE
    ) as ClipboardManager
    val clip = ClipData.newPlainText("address", myAddress)
    clipboard.primaryClip = clip
}

fun validateIPAddress(ipAddress: String): Boolean {
    val ipv4 = Pattern.compile(IPV4_ADDRESS_PATTERN).matcher(ipAddress)
    if (ipv4.matches()) {
        return true
    }

    return Pattern.compile(IPV6_ADDRESS_PATTERN).matcher(ipAddress).matches()
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

    QueryPreferences.setPrefAddress(context, "")
    QueryPreferences.setPrefPasswordEnabled(context, true)
    QueryPreferences.setPrefAutoLockTimeOut(context, 2000)
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

fun sumStoredTransactions(transactionEntities: List<TransactionEntity2>): BigDecimal {
    val sumSent = transactionEntities.asSequence().filter { transactions ->
        transactions.sent
    }.map { transactionEntity ->
        transactionEntity.amount
    }.fold(BigDecimal.ZERO, BigDecimal::add)

    val sumReceived = transactionEntities.asSequence().filterNot { transactions ->
        transactions.sent
    }.map { transactionEntity ->
        transactionEntity.amount
    }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumReceived - sumSent
}
