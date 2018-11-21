package com.radixdlt.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager

object QueryPreferences {
    private const val PREF_ADDRESS = "address"
    private const val PREF_PASSWORD = "password"
    private const val PREF_AUTOLOCK_TIMEOUT = "autolock_timeout"

    fun getPrefAddress(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_ADDRESS, "")!!
    }

    @SuppressLint("ApplySharedPref")
    fun setPrefAddress(context: Context, address: String) {
        // Using commit instead of apply as it happens async already and
        // we want it saved immediately.
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_ADDRESS, address)
                .commit()
    }

    fun getPrefPasswordEnabled(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_PASSWORD, true)
    }

    fun setPrefPasswordEnabled(context: Context, enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_PASSWORD, enabled)
                .apply()
    }

    fun getPrefAutoLockTimeOut(context: Context): Long {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(PREF_AUTOLOCK_TIMEOUT, 2000)
    }

    fun setPrefAutoLockTimeOut(context: Context, timeout: Long) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(PREF_AUTOLOCK_TIMEOUT, timeout)
                .apply()
    }
}
