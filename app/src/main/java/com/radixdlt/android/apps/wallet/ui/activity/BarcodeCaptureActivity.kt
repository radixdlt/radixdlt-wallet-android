/*
 * Copyright (C) The Android Open Source Project
 *
 * Modifications copyright (C) 2019 Radix DLT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("DEPRECATION")

package com.radixdlt.android.apps.wallet.ui.activity

import android.Manifest
import android.annotation.SuppressLint

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.Snackbar
import com.radixdlt.android.apps.wallet.BuildConfig
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.ui.camera.BarcodeGraphic
import com.radixdlt.android.apps.wallet.ui.camera.BarcodeGraphicTracker
import com.radixdlt.android.apps.wallet.ui.camera.CameraSource
import com.radixdlt.android.apps.wallet.ui.camera.GraphicOverlay
import kotlinx.android.synthetic.main.barcode_capture.*
import org.jetbrains.anko.toast
import timber.log.Timber
import java.io.IOException

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
class BarcodeCaptureActivity : BaseActivity() {

    private var cameraSource: CameraSource? = null
    private var graphicOverlayBarcodeGraphic: GraphicOverlay<BarcodeGraphic>? = null

    private var gotQRCode: Boolean = false

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barcode_capture)

        setSupportActionBar(toolbar as Toolbar)

        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        @Suppress("UNCHECKED_CAST")
        graphicOverlayBarcodeGraphic = graphicOverlay as? GraphicOverlay<BarcodeGraphic>

        // read parameters from the intent used to launch the activity.
        val autoFocus = intent.getBooleanExtra(AUTO_FOCUS, true)
        val useFlash = intent.getBooleanExtra(USER_FLASH, false)

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash)
        } else {
            requestCameraPermission()
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private fun requestCameraPermission() {
        Timber.w("Camera permission is not granted. Requesting permission")

        PaymentActivity.openedPermissionDialog = true

        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions,
                RC_HANDLE_CAMERA_PERM
            )
            return
        }

        val thisActivity = this

        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(
                thisActivity,
                permissions,
                RC_HANDLE_CAMERA_PERM
            )
        }

        topLayout.setOnClickListener(listener)
        Snackbar.make(
            graphicOverlay!!,
            R.string.permission_camera_rationale,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok, listener).show()
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi", "ObsoleteSdkInt")
    private fun createCameraSource(autoFocus: Boolean, useFlash: Boolean) {
        val context = applicationContext

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        val barcodeDetector =
            BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.QR_CODE).build()

        val graphic = BarcodeGraphic(graphicOverlayBarcodeGraphic!!)
        val barcodeGraphicTracker =
            BarcodeGraphicTracker(graphicOverlayBarcodeGraphic!!, graphic)

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    barcodeGraphicTracker.onUpdate(detections, barcodes.valueAt(0))
                    // got QR code
                    if (!gotQRCode) {
                        gotQRCode = true
                        val data = Intent()
                        data.putExtra(BARCODE_OBJECT, barcodes.valueAt(0))
                        setResult(CommonStatusCodes.SUCCESS, data)
                        finish()
                    }
                } else {
                    if (!gotQRCode) barcodeGraphicTracker.onDone()
                }
            }
        })

        if (!barcodeDetector.isOperational) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Timber.w("Detector dependencies are not yet available.")

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            val lowStorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowStorageFilter) != null

            if (hasLowStorage) {
                toast(R.string.low_storage_error)
                Timber.w(getString(R.string.low_storage_error))
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        var builder: CameraSource.Builder =
            CameraSource.Builder(applicationContext, barcodeDetector).setFacing(
                CameraSource.CAMERA_FACING_BACK
            )
                .setRequestedPreviewSize(
                    WIDTH,
                    HEIGHT
                )
                .setRequestedFps(FPS)

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else null
            )
        }

        cameraSource =
            builder.setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null).build()
    }

    /**
     * Restarts the camera.
     */
    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        startCameraSource()
    }

    /**
     * Stops the camera.
     */
    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
        if (preview != null) {
            preview!!.stop()
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    override fun onDestroy() {
        super.onDestroy()
        if (preview != null) {
            preview!!.release()
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     *
     * @param requestCode The request code passed in [.requestPermissions].
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED]
     * or [PackageManager.PERMISSION_DENIED]. Never null.
     * @see .requestPermissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Timber.d("Got unexpected permission result: %d", requestCode)
            PaymentActivity.openedPermissionDialog = false
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Timber.d("Camera permission granted - initialize the camera source")
            // we have permission, so create the camerasource
            val autoFocus = intent.getBooleanExtra(AUTO_FOCUS, true)
            val useFlash = intent.getBooleanExtra(USER_FLASH, false)
            createCameraSource(autoFocus, useFlash)
            return
        }

        Timber.e(
            "Permission not granted: results len = %d Result code = %s",
            grantResults.size, if (grantResults.isNotEmpty()) grantResults[0] else "(empty)"
        )

        val listener = DialogInterface.OnClickListener { _, _ ->
            PaymentActivity.openedPermissionDialog = false
            finish()
        }

        val settingsListener = DialogInterface.OnClickListener { _, _ ->
            PaymentActivity.openedPermissionDialog = false
            val i = Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            )
            startActivity(i)
            finish()
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.barcode_capture_activity_title))
            .setMessage(R.string.no_camera_permission)
            .setPositiveButton(android.R.string.ok, listener)
            .setNegativeButton(
                getString(R.string.barcode_capture_activity_dialog_negative),
                settingsListener
            ).show()
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    @Throws(SecurityException::class)
    private fun startCameraSource() {
        // check that the device has play services available.
        val code =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance()
                .getErrorDialog(this, code,
                    RC_HANDLE_GMS
                )
            dlg.show()
        }

        if (cameraSource != null) {
            PaymentActivity.openedPermissionDialog = false
            try {
                preview!!.start(cameraSource!!, graphicOverlay!!)
            } catch (e: IOException) {
                Timber.e(e, "Unable to start camera source.")
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    companion object {
        // intent request code to handle updating play services if needed.
        private const val RC_HANDLE_GMS = 9001

        // permission request codes need to be < 256
        private const val RC_HANDLE_CAMERA_PERM = 2

        // constants used to pass extra data in the intent
        const val AUTO_FOCUS = "AutoFocus"
        const val USER_FLASH = "UseFlash"
        const val BARCODE_OBJECT = "Barcode"

        // constants used for preview size
        const val WIDTH = 1600
        const val HEIGHT = 1024

        const val FPS = 30.0f
    }
}
