package com.radixdlt.android.apps.wallet.ui.fragment.greeting

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.helper.CustomTabsHelper.openCustomTab
import com.radixdlt.android.apps.wallet.helper.TextFormatHelper
import com.radixdlt.android.apps.wallet.helper.WebviewFallback
import com.radixdlt.android.apps.wallet.ui.activity.BaseActivity
import com.radixdlt.android.apps.wallet.ui.activity.NewWalletActivity
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.TAG_TERMS_AND_CONDITIONS
import com.radixdlt.android.apps.wallet.util.URL_PRIVACY_POLICY
import com.radixdlt.android.apps.wallet.util.URL_TERMS_AND_CONDITIONS
import com.radixdlt.android.apps.wallet.util.getNavigationBarHeight
import com.radixdlt.android.apps.wallet.util.getStatusBarHeight
import com.radixdlt.android.databinding.FragmentGreetingBinding
import kotlinx.android.synthetic.main.fragment_greeting.*
import org.jetbrains.anko.startActivity

class GreetingFragment : Fragment() {

    private lateinit var ctx: Context

    private lateinit var customTabsIntent: CustomTabsIntent

    private val greetingViewModel: GreetingViewModel by viewModels()

    private val marginDimen: Int by lazy {
        resources.getDimension(R.dimen.activity_vertical_margin).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.apply {
            if (QueryPreferences.isTermsAccepted(this)) {
                startActivity<NewWalletActivity>()
                finish()
                return
            }
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentGreetingBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_greeting, container, false)
        binding.viewmodel = greetingViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        setGreetingMessage()
        setViewMargins()
        createCustomTabsBuilder()
        createTermsAndConditionsLink()
        createPrivacyPolicyLink()
        setGetStartedButtonClickListener()
    }

    private fun setGetStartedButtonClickListener() {
        greetingGetStartedButton.setOnClickListener {
            QueryPreferences.setPrefTermsAccepted(ctx, true)
            activity?.apply {
                startActivity<NewWalletActivity>()
                finish()
            }
        }
    }

    private fun createTermsAndConditionsLink() {
        val cs = createLink(getString(R.string.greeting_fragment_terms_and_conditions))
        greetingTermsAndConditionsCheckBox.text = cs
        greetingTermsAndConditionsCheckBox.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun createPrivacyPolicyLink() {
        val cs = createLink(getString(R.string.greeting_fragment_privacy_policy))
        greetingPrivacyPolicyCheckBox.text = cs
        greetingPrivacyPolicyCheckBox.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun createLink(text: String): CharSequence? {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Prevent CheckBox state from being toggled when link is clicked
                widget.cancelPendingInputEvents()
                // Do action for link text...
                BaseActivity.openedCustomTabs = true

                val url = if (widget.tag == TAG_TERMS_AND_CONDITIONS) {
                    URL_TERMS_AND_CONDITIONS
                } else {
                    URL_PRIVACY_POLICY
                }

                openCustomTab(ctx as Activity, customTabsIntent, Uri.parse(url), WebviewFallback())
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        val termsAndConditionsLink = SpannableString(text)
        termsAndConditionsLink.setSpan(
            clickableSpan,
            0,
            termsAndConditionsLink.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        termsAndConditionsLink.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.radixGreen2)),
            0,
            termsAndConditionsLink.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return TextUtils.expandTemplate(
            "${getString(R.string.greeting_fragment_agree_to_the)} ^1", termsAndConditionsLink
        )
    }

    private fun createCustomTabsBuilder() {
        customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
            .setShowTitle(true)
            .enableUrlBarHiding()
            .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow_back))
            .build()
    }

    /**
     * View Margins for getStartedButton and radixLogo are set dynamically due to displaying this
     * view in full screen including the status bar. Different phones have different size status
     * bars which need to be accounted for.
     * */
    private fun setViewMargins() {
        val paramsButton = greetingGetStartedButton.layoutParams as ConstraintLayout.LayoutParams
        paramsButton.bottomMargin = getNavigationBarHeight() + marginDimen
        greetingGetStartedButton.layoutParams = paramsButton

        val paramsLogoImageView = greetingRadixLogo.layoutParams as ConstraintLayout.LayoutParams
        paramsLogoImageView.topMargin = getStatusBarHeight() + marginDimen
        greetingRadixLogo.layoutParams = paramsLogoImageView
    }

    private fun setGreetingMessage() {
        val termsAndConditions = TextFormatHelper.normal(
            getString(R.string.greeting_fragment_send),
            TextFormatHelper.color(
                ContextCompat.getColor(ctx, R.color.radixGreen2),
                getString(R.string.greeting_fragment_money)
            ),
            getString(R.string.greeting_fragment_and),
            TextFormatHelper.color(
                ContextCompat.getColor(ctx, R.color.radixGreen2),
                getString(R.string.greeting_fragment_tokens)
            )
        )
        greetingMessageTextView.text = termsAndConditions
    }

    override fun onResume() {
        super.onResume()
        BaseActivity.openedCustomTabs = false
    }
}
