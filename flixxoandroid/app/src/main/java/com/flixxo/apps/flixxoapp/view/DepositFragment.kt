package com.flixxo.apps.flixxoapp.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.flixxo.apps.flixxoapp.R
import com.flixxo.apps.flixxoapp.viewModel.DepositViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_deposit.*
import net.glxn.qrgen.android.QRCode
import org.koin.androidx.viewmodel.ext.android.viewModel

class DepositFragment : Fragment() {
    private val viewModel: DepositViewModel by viewModel()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var bundle: Bundle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_deposit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bundle = Bundle()
        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        firebaseAnalytics.logEvent("deposit_screen", bundle)

        viewModel.token.observe(this, Observer { value ->
            value?.let {
                token.text = it

                val bitmap = QRCode.from(it).bitmap()
                image_qr.setImageBitmap(bitmap)
            }
        })

        viewModel.getToken()
        if (viewModel.token.value == null || viewModel.token.toString().isEmpty()) {

            viewModel.createToken()
            viewModel.getToken()

        }
        copy_button.setOnClickListener {
            val clipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("text", token.text)
            clipboard.primaryClip = clip

            Toast.makeText(context!!, getString(R.string.tokenCopied), Toast.LENGTH_SHORT).show()
        }
    }
}
