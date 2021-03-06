package com.radixdlt.android.apps.wallet.util

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

object Pref {
    const val ADDRESS = "address"
    const val PASSWORD = "password"
    const val MNEMONIC_SEED = "mnemonic_seed"
    const val USING_REMOTE_FAUCET = "faucet"
    const val TUTORIAL_RECEIVE = "tutorial_receive"
    const val TERMS_ACCEPTED = "terms_accepted"
    const val WALLET_BACKED_UP = "wallet_backed_up"
    const val PIN_SET = "pin_set"
    const val USE_BIOMETRICS = "use_biometrics"
    const val AUTHENTICATE_ON_LAUNCH = "authenticate_on_launch"
    const val PREF_FAUCET = "faucet"

    const val FRAGMENT_ID = "fragment_id"
    const val LOCK_ACTIVE = "lock_active"

    fun Context.defaultPrefs(): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this)

    fun Context.customPrefs(name: String): SharedPreferences =
        getSharedPreferences(name, Context.MODE_PRIVATE)

    fun Fragment.defaultPrefs(): SharedPreferences =
        view?.let {
            PreferenceManager.getDefaultSharedPreferences(activity)
        } ?: throw NullPointerException()

    inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    /**
     * puts a key value pair in shared prefs if doesn't exists,
     * otherwise updates value on given [key]
     */
    operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is String? -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not implemented")
        }
    }

    /**
     * finds value on given key.
     * [T] is the type of value
     * @param defaultValue optional default value - will take null for strings,
     * false for bool and -1 for numeric values if [defaultValue] is not specified
     */
    inline operator fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T?): T {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String ?: "") as T
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T
            else -> throw UnsupportedOperationException("Not implemented")
        }
    }
}
