package com.radixdlt.android.apps.wallet.ui.fragment.contacts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.data.model.message.MessageEntity
import com.radixdlt.android.apps.wallet.ui.activity.ConversationActivity
import com.radixdlt.android.apps.wallet.ui.adapter.ContactsAdapter
import com.radixdlt.android.apps.wallet.ui.dialog.NewConversationDialog
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.isRadixAddress
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_contacts.*
import org.jetbrains.anko.toast
import javax.inject.Inject

class ContactsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var contactsViewModel: ContactsViewModel

    private lateinit var contactsAdapter: ContactsAdapter

    private val messages = mutableListOf<MessageEntity>()

    private var loadedFromNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initialiseViewModels()
    }

    private fun initialiseViewModels() {
        contactsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ContactsViewModel::class.java)

        contactsViewModel.contacts.observe(this,
            Observer<MutableList<MessageEntity>?> { messages ->
                messages?.apply {
                    bindContacts(messages)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)

        initialiseRecyclerView()
        initialiseSwipeRefreshLayout()
        initialiseLoadingState()
        initialiseNewConversationButton()
    }

    private fun initialiseRecyclerView() {
        val itemDecorator = DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(activity!!, R.drawable.divider)!!)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(itemDecorator)
        contactsAdapter = ContactsAdapter(messages, click)
        recyclerView.adapter = contactsAdapter
    }

    private fun initialiseNewConversationButton() {
        newConversationButton.setOnClickListener {
            val newConversationDialog = NewConversationDialog.newInstance()
            newConversationDialog.setTargetFragment(
                this@ContactsFragment,
                REQUEST_CODE_NEW_CONVERSATION
            )
            newConversationDialog.show(fragmentManager!!, null)
        }
    }

    private fun initialiseSwipeRefreshLayout() {
        swipe_refresh_layout.setColorSchemeResources(
            R.color.colorPrimary, R.color.colorAccent, R.color.colorAccentSecondary
        )
        swipe_refresh_layout.setOnRefreshListener { refreshMessages() }
    }

    private fun refreshMessages() {
        setRefreshLayoutIsRefreshing(false)
    }

    private fun bindContacts(messageEntities: MutableList<MessageEntity>) {
        when {
            messageEntities.isEmpty() -> {
                // Added a counter since we query the DB first and then instantly query the
                // network for any existing contacts which may have not been stored such as
                // when loading an empty wallet
                if (loadedFromNetwork) {
                    setRefreshLayoutIsRefreshing(false)
                    return
                }
                loadedFromNetwork = true
                return
            }
            else -> addMessages(messageEntities)
        }
    }

    private fun addMessages(messageEntities: List<MessageEntity>) {
        this.messages.clear()
        this.messages.addAll(messageEntities)
        setLayoutResources()
        showMessages()
    }

    private fun showMessages() {
        if (swipe_refresh_layout.isRefreshing) {
            setRefreshLayoutIsRefreshing(false)
        }
        contactsAdapter.notifyDataSetChanged()
    }

    private val click = fun(address: String, longClick: Boolean) {
        if (longClick) {
            copyToClipboard(activity!!, address)
            activity?.toast("$address ${activity!!.getString(R.string.toast_copied_clipboard)}")
        } else {
            ConversationActivity.newIntent(activity!!, address)
        }
    }

    private fun initialiseLoadingState() {
        if (messages.isEmpty()) {
            setRefreshLayoutIsRefreshing(true)
        }
        setLayoutResources()
    }

    private fun setLayoutResources() {
        if (messages.isEmpty() && loadedFromNetwork) {
            setLayoutResourcesWithEmptyMessages()
        } else if (messages.isNotEmpty()) {
            setLayoutResourcesWithMessages()
        }
    }

    private fun setLayoutResourcesWithEmptyMessages() {
        contactsBackGroundImageView.visibility = View.VISIBLE
    }

    private fun setLayoutResourcesWithMessages() {
        contactsBackGroundImageView.visibility = View.GONE
        swipe_refresh_layout.setBackgroundColor(
            ContextCompat.getColor(activity!!, R.color.white)
        )
    }

    private fun setRefreshLayoutIsRefreshing(isRefreshing: Boolean) {
        swipe_refresh_layout.isRefreshing = isRefreshing
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == REQUEST_CODE_NEW_CONVERSATION) {
            if (data == null) return

            val address = data.getStringExtra(NewConversationDialog.EXTRA_ADDRESS)
            if (address.isNotEmpty() && isRadixAddress(address)) {
                ConversationActivity.newIntent(activity!!, address)
            } else {
                activity!!.toast(getString(R.string.invalid_address_toast))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        contactsAdapter.notifyDataSetChanged()
    }

    companion object {
        private const val REQUEST_CODE_NEW_CONVERSATION = 0
    }
}
