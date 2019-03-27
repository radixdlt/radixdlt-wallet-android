package com.radixdlt.android.ui.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.radixdlt.android.R
import com.radixdlt.android.util.KEYSTORE_FILE
import com.radixdlt.android.util.createProgressDialog
import kotlinx.android.synthetic.main.activity_new_wallet.*
import org.jetbrains.anko.startActivity
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader

class NewWalletActivity : BaseActivity() {

    companion object {
        const val READ_REQUEST_CODE = 42
    }

    private var deepLink: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_wallet)

        deepLink = intent.data
        progressDialog = createProgressDialog(this)

        val myKeyFile = File(filesDir, KEYSTORE_FILE)
        if (myKeyFile.exists()) {

            // In case of dealing with intent which has a uri extra, check if launched from
            // history to make sure that either the transactionList or conversation screen does
            // not open again. This ensures that the intent only opens when uri is passed.
            val launchFromHistory = if (intent != null) {
                intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
            } else {
                false
            }

            if (!launchFromHistory && Intent.ACTION_VIEW == intent.action && deepLink != null) {
                EnterPasswordActivity.newIntent(this, deepLink!!)
            } else {
                EnterPasswordActivity.newIntent(this)
            }
            finish()
        }

        createNewWalletButton.setOnClickListener {
            if (Intent.ACTION_VIEW == intent.action && deepLink != null) {
                EnterPasswordActivity.newIntent(this, deepLink!!)
            } else {
                startActivity<EnterPasswordActivity>()
            }
        }

        createPaperKeyWalletButton.setOnClickListener {
            startActivity<NewPaperKeyWalletActivity>()
        }

        importWalletButton.setOnClickListener {
            performFileSearch()
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select a file.
     */
    private fun performFileSearch() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)

        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // To search for all documents available via installed storage providers, it would be "*/*".
        intent.type = "*/*" // ideally I would use "text/*" but desktop sets and application/* mime

        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri: Uri?
            if (resultData != null) {
                uri = resultData.data
                Timber.i("Uri: ${uri!!} and type ${contentResolver.getType(uri)}")
                val fileContents = readTextFromUri(uri)
                createFile(fileContents, File(filesDir, "keystore.key").path)
                if (deepLink != null) {
                    EnterPasswordActivity.newIntent(this, deepLink!!)
                } else {
                    startActivity<EnterPasswordActivity>()
                }
                finish()
            }
        }
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (true) {
            line = reader.readLine()
            if (line == null) break
            stringBuilder.append(line)
        }
        inputStream?.close()
        return stringBuilder.toString()
    }

    private fun createFile(fileContents: String, filePath: String) {
        var writer: FileWriter? = null
        try {
            writer = FileWriter(filePath)
            writer.write(fileContents)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
