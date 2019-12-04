package com.radixdlt.android.apps.wallet.ui.main

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.connectivity.ConnectivityLiveData
import javax.inject.Inject

class MainViewModel @Inject constructor(
    val connectivityLiveData: ConnectivityLiveData
) : ViewModel() {

    private val _mainLoadingState: MutableLiveData<MainLoadingState> = MutableLiveData()

    val mainLoadingState: LiveData<MainLoadingState>
        get() = _mainLoadingState

    private val _navigationCheckedItem = MutableLiveData<Int>()
    val navigationCheckedItem: LiveData<Int> get() = _navigationCheckedItem

    private val _showBackUpWalletNotification = MutableLiveData<Boolean>()
    val showBackUpWalletNotification: LiveData<Boolean>
        get() = _showBackUpWalletNotification

    private val _launchAuthenticationAction = LiveEvent<LaunchAuthenticationAction>()
    val launchAuthenticationAction: LiveEvent<LaunchAuthenticationAction>
        get() = _launchAuthenticationAction

    init {
        _showBackUpWalletNotification.value = true
        _mainLoadingState.value = MainLoadingState.LOADING
    }

    fun unlock() {
        _launchAuthenticationAction.value =
            LaunchAuthenticationAction.UNLOCK
    }

    fun usePin() {
        _launchAuthenticationAction.value =
            LaunchAuthenticationAction.USE_PIN
    }

    fun logout() {
        _launchAuthenticationAction.value =
            LaunchAuthenticationAction.LOGOUT
    }

    fun setBottomNavigationCheckedItem(@IdRes item: Int) {
        _navigationCheckedItem.value = item
    }

    fun showBackUpWalletNotification(show: Boolean) {
        _showBackUpWalletNotification.value = show
    }
}

enum class LaunchAuthenticationAction {
    UNLOCK, USE_PIN, LOGOUT
}
