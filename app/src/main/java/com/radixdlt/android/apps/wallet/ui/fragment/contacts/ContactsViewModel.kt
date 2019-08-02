package com.radixdlt.android.apps.wallet.ui.fragment.contacts

import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.message.ContactsRepository
import javax.inject.Inject
import javax.inject.Named

class ContactsViewModel @Inject constructor(
    @Named("contacts") val contacts: ContactsRepository
) : ViewModel()
