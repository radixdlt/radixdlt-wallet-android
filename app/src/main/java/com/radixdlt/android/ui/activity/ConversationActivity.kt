package com.radixdlt.android.ui.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.radixdlt.android.R
import com.radixdlt.android.data.model.message.MessageEntity
import com.radixdlt.android.ui.adapter.ConversationAdapter
import com.radixdlt.android.util.EmptyTextWatcher
import com.radixdlt.android.util.QueryPreferences
import com.radixdlt.android.util.setAddressWithColors
import com.radixdlt.android.util.showKeyboard
import com.radixdlt.client.core.network.AtomSubmissionUpdate
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_conversation.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import timber.log.Timber
import javax.inject.Inject

class ConversationActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var conversationViewModel: ConversationViewModel

    private var listConvo = mutableListOf<MessageEntity>()
    private lateinit var conversationAdapter: ConversationAdapter
    private val compositeDisposable = CompositeDisposable()

    private lateinit var myAddress: String
    private lateinit var addressTo: String

    companion object {
        private const val EXTRA_CONVERSATION_ADDRESS = "com.radixdlt.android.address"
        private const val EXTRA_URI = "com.radixdlt.android.uri"

        fun newIntent(ctx: Context, address: String) {
            ctx.startActivity<ConversationActivity>(EXTRA_CONVERSATION_ADDRESS to address)
        }

        fun newIntent(ctx: Context, uri: Uri) {
            ctx.startActivity<ConversationActivity>(EXTRA_URI to uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val keyAddress: String? = intent.getStringExtra(EXTRA_CONVERSATION_ADDRESS)
        val uri: Uri? = intent.getParcelableExtra(EXTRA_URI)

        myAddress = QueryPreferences.getPrefAddress(this)

        keyAddress?.let {
            addressTo = it
            initialiseViewModels(it)
        }

        uri?.let {
            addressTo = it.getQueryParameter("to")!!
            inputMsg.setText(it.getQueryParameter("attachment"))
            sendButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this@ConversationActivity,
                    R.drawable.ic_send_filled_rounded_icon
                )
            )
        }

        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarTextView.text = setAddressWithColors(this, addressTo)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true // This makes recycler stick at the bottom
        recyclerView.layoutManager = layoutManager
        conversationAdapter = ConversationAdapter(myAddress, listConvo)

        recyclerView.adapter = conversationAdapter

        inputMsg.addTextChangedListener(object : TextWatcher by EmptyTextWatcher {
            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    sendButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@ConversationActivity,
                            R.drawable.ic_send_icon
                        )
                    )
                } else {
                    sendButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@ConversationActivity,
                            R.drawable.ic_send_filled_rounded_icon
                        )
                    )
                }
            }
        })

        sendButton.setOnClickListener {
            val message = inputMsg.text.toString()
            if (message.isBlank()) return@setOnClickListener
            // Send a message!
            if (addressTo.isBlank()) return@setOnClickListener
            Timber.d(addressTo)
            it.isEnabled = false
            preventKeyboardInput(true)
            conversationViewModel.sendMessage(addressTo, message)
        }
    }

    private fun initialiseViewModels(keyAddress: String) {
        conversationViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ConversationViewModel::class.java)

        conversationViewModel.conversationTwoParticipants(myAddress, keyAddress)

        conversationViewModel.conversationRepository.observe(this, Observer { messages ->
            messages?.apply {

                this.forEach {
                    if (!listConvo.contains(it)) listConvo.add(it)
                }
                conversationAdapter.notifyDataSetChanged()
                // for some reason we need toAddress help totally scroll to bottom as on some occasions it doesn't
                Handler().postDelayed({
                    recyclerView.scrollToPosition(conversationAdapter.itemCount - 1)
                }, 20)
            }
        })

        conversationViewModel.sendMessageLiveData.observe(this, Observer { status ->
            status?.apply {
                if (this == AtomSubmissionUpdate.AtomSubmissionState.STORED.name) {
                    inputMsg.text.clear()
                } else {
                    toast(getString(R.string.conversation_activity_send_message_error))
                }
                preventKeyboardInput(false)
            }
        })
    }

    /**
     * Temporary function and functionality until contacts are inserted directly into
     * DB and state handled there. Currently, we are waiting until STORED state is
     * received hence lagging message input.
     * */
    private fun preventKeyboardInput(prevent: Boolean) {
        if (prevent) {
            inputMsg.setTextColor(
                ContextCompat.getColor(
                    this@ConversationActivity, R.color.materialLightGrey
                )
            )
            inputMsg.isEnabled = false
            showKeyboard(inputMsg)
            inputMsg.inputType = InputType.TYPE_NULL
        } else {
            sendButton.isEnabled = true
            inputMsg.isEnabled = true
            inputMsg.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            inputMsg.setTextColor(
                ContextCompat.getColor(
                    this@ConversationActivity, R.color.materialGrey900
                )
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        Timber.d(compositeDisposable.size().toString())
        compositeDisposable.clear()
        super.onDestroy()
    }
}
