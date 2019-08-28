package com.radixdlt.android.apps.wallet.ui.fragment.payment.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.radixdlt.android.R
import kotlinx.android.synthetic.main.fragment_payment_summary.*
import org.jetbrains.anko.px2dip

class PaymentSummaryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_payment_summary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        (activity as AppCompatActivity).supportActionBar?.title = "Summary"
        (activity as AppCompatActivity).supportActionBar?.elevation = view.context.px2dip(0)

        paymentSummaryToImageButton.setOnClickListener {
            Snackbar.make(view.rootView, "Address copied!", Snackbar.LENGTH_SHORT).show()
        }

        paymentSummaryFromImageButton.setOnClickListener {
            Snackbar.make(view.rootView, "Address copied!", Snackbar.LENGTH_SHORT).show()
        }
    }
}
