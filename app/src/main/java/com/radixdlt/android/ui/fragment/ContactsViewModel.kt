package com.radixdlt.android.ui.fragment

import androidx.lifecycle.ViewModel
import com.radixdlt.android.data.model.message.ContactsRepository
import javax.inject.Inject
import javax.inject.Named

class ContactsViewModel @Inject constructor(
    @Named("contacts") val contacts: ContactsRepository
) : ViewModel()
