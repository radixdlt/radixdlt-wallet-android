package com.radixdlt.android.apps.wallet.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object Pref {
    const val ADDRESS = "address"
    const val PASSWORD = "password"
    const val AUTOLOCK_TIMEOUT = "autolock_timeout"
    const val NETWORK = "network"
    const val RANDOM_SELECTION = "random_selection"
    const val NODE_IP = "node_ip"
    const val MNEMONIC_SEED = "mnemonic_seed"
    const val FAUCET = "faucet"
    const val TUTORIAL_RECEIVE = "tutorial_receive"
    const val TERMS_ACCEPTED = "terms_accepted"
    const val WALLET_BACKED_UP = "wallet_backed_up"

    fun Context.defaultPrefs(): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this)

    fun Context.customPrefs(name: String): SharedPreferences =
        getSharedPreferences(name, Context.MODE_PRIVATE)

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
