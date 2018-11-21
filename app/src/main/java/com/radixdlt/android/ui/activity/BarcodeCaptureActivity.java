/*
 * Copyright (C) The Android Open Source Project
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
package com.radixdlt.android.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.Snackbar;
import com.radixdlt.android.BuildConfig;
import com.radixdlt.android.R;
import com.radixdlt.android.ui.camera.BarcodeGraphic;
import com.radixdlt.android.ui.camera.BarcodeGraphicTracker;
import com.radixdlt.android.ui.camera.CameraSource;
import com.radixdlt.android.ui.camera.CameraSourcePreview;
import com.radixdlt.android.ui.camera.GraphicOverlay;
import java.io.IOException;
import timber.log.Timber;

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
public final class BarcodeCaptureActivity extends BaseActivity {
  // intent request code to handle updating play services if needed.
  private static final int RC_HANDLE_GMS = 9001;

  // permission request codes need to be < 256
  private static final int RC_HANDLE_CAMERA_PERM = 2;

  // constants used to pass extra data in the intent
  public static final String AutoFocus = "AutoFocus";
  public static final String UseFlash = "UseFlash";
  public static final String BarcodeObject = "Barcode";

  private CameraSource mCameraSource;
  private CameraSourcePreview mPreview;
  private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

  private boolean gotQRCode;

  /**
   * Initializes the UI and creates the detector pipeline.
   */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.barcode_capture);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    //noinspection ConstantConditions
    getSupportActionBar().setHomeButtonEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mPreview = findViewById(R.id.preview);
    mGraphicOverlay = findViewById(R.id.graphicOverlay);

    // read parameters from the intent used to launch the activity.
    boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, true);
    boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);

    // Check for the camera permission before accessing the camera.  If the
    // permission is not granted yet, request permission.
    int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
    if (rc == PackageManager.PERMISSION_GRANTED) {
      createCameraSource(autoFocus, useFlash);
    } else {
      requestCameraPermission();
    }
  }

  /**
   * Handles the requesting of the camera permission.  This includes
   * showing a "Snackbar" message of why the permission is needed then
   * sending the request.
   */
  private void requestCameraPermission() {
    Timber.w("Camera permission is not granted. Requesting permission");

    openedPermissionDialog = true;

    final String[] permissions = new String[] { Manifest.permission.CAMERA };

    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
      ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
      return;
    }

    final Activity thisActivity = this;

    View.OnClickListener listener =
        view -> ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);

    findViewById(R.id.topLayout).setOnClickListener(listener);
    Snackbar.make(mGraphicOverlay,
        R.string.permission_camera_rationale,
        Snackbar.LENGTH_INDEFINITE
    ).setAction(android.R.string.ok, listener).show();
  }

  /**
   * Creates and starts the camera.  Note that this uses a higher resolution in comparison
   * to other detection examples to enable the barcode detector to detect small barcodes
   * at long distances.
   *
   * Suppressing InlinedApi since there is a check that the minimum version is met before using
   * the constant.
   */
  @SuppressLint({ "InlinedApi", "ObsoleteSdkInt" })
  private void createCameraSource(boolean autoFocus, boolean useFlash) {
    Context context = getApplicationContext();

    // A barcode detector is created to track barcodes.  An associated multi-processor instance
    // is set to receive the barcode detection results, track the barcodes, and maintain
    // graphics for each barcode on screen.  The factory is used by the multi-processor to
    // create a separate tracker instance for each barcode.
    final BarcodeDetector barcodeDetector =
        new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.QR_CODE).build();

    BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
    final BarcodeGraphicTracker barcodeGraphicTracker =
        new BarcodeGraphicTracker(mGraphicOverlay, graphic);

    barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
      @Override
      public void release() {

      }

      @Override
      public void receiveDetections(Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();
        if (barcodes.size() == 1) {
          barcodeGraphicTracker.onUpdate(detections, barcodes.valueAt(0));
          // got QR code
          if (!gotQRCode) {
            gotQRCode = true;
            Intent data = new Intent();
            data.putExtra(BarcodeObject, barcodes.valueAt(0));
            setResult(CommonStatusCodes.SUCCESS, data);
            finish();
          }
        } else {
          if (!gotQRCode) barcodeGraphicTracker.onDone();
        }
      }
    });

    if (!barcodeDetector.isOperational()) {
      // Note: The first time that an app using the barcode or face API is installed on a
      // device, GMS will download a native libraries to the device in order to do detection.
      // Usually this completes before the app is run for the first time.  But if that
      // download has not yet completed, then the above call will not detect any barcodes
      // and/or faces.
      //
      // isOperational() can be used to check if the required native libraries are currently
      // available.  The detectors will automatically become operational once the library
      // downloads complete on device.
      Timber.w("Detector dependencies are not yet available.");

      // Check for low storage.  If there is low storage, the native library will not be
      // downloaded, so detection will not become operational.
      IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
      boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

      if (hasLowStorage) {

        Toast.makeText(this, R.string.low_storage_error,
            Toast.LENGTH_LONG
        ).show();

        Timber.w(getString(R.string.low_storage_error));
      }
    }

    // Creates and starts the camera.  Note that this uses a higher resolution in comparison
    // to other detection examples to enable the barcode detector to detect small barcodes
    // at long distances.
    CameraSource.Builder builder =
        new CameraSource.Builder(getApplicationContext(), barcodeDetector).setFacing(
            CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .setRequestedFps(30.0f);

    // make sure that auto focus is an available option
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      builder =
          builder.setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
    }

    mCameraSource =
        builder.setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null).build();
  }

  /**
   * Restarts the camera.
   */
  @Override
  protected void onResume() {
    super.onResume();
    Timber.d("onResume");
    startCameraSource();
  }

  /**
   * Stops the camera.
   */
  @Override
  protected void onPause() {
    super.onPause();
    Timber.d("onPause");
    if (mPreview != null) {
      mPreview.stop();
    }
  }

  /**
   * Releases the resources associated with the camera source, the associated detectors, and the
   * rest of the processing pipeline.
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mPreview != null) {
      mPreview.release();
    }
  }

  /**
   * Callback for the result from requesting permissions. This method
   * is invoked for every call on {@link #requestPermissions(String[], int)}.
   * <p>
   * <strong>Note:</strong> It is possible that the permissions request interaction
   * with the user is interrupted. In this case you will receive empty permissions
   * and results arrays which should be treated as a cancellation.
   * </p>
   *
   * @param requestCode The request code passed in {@link #requestPermissions(String[], int)}.
   * @param permissions The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions
   * which is either {@link PackageManager#PERMISSION_GRANTED}
   * or {@link PackageManager#PERMISSION_DENIED}. Never null.
   * @see #requestPermissions(String[], int)
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (requestCode != RC_HANDLE_CAMERA_PERM) {
      Timber.d("Got unexpected permission result: %d", requestCode);
      openedPermissionDialog = false;
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      return;
    }

    if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Timber.d("Camera permission granted - initialize the camera source");
      // we have permission, so create the camerasource
      boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, true);
      boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
      createCameraSource(autoFocus, useFlash);
      return;
    }

    Timber.e("Permission not granted: results len = %d Result code = %s",
        grantResults.length, grantResults.length > 0 ? grantResults[0] : "(empty)");

    DialogInterface.OnClickListener listener = (dialog, id) -> {
      openedPermissionDialog = false;
      finish();
    };

    DialogInterface.OnClickListener settingsListener = (dialog, id) -> {
      openedPermissionDialog = false;
      Intent i = new Intent(
          android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
          Uri.parse("package:" + BuildConfig.APPLICATION_ID)
      );
      startActivity(i);
      finish();
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("QR code scanner")
        .setMessage(R.string.no_camera_permission)
        .setPositiveButton(android.R.string.ok, listener)
        .setNegativeButton("Settings", settingsListener)
        .show();
  }

  /**
   * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() throws SecurityException {
    // check that the device has play services available.
    int code =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
    if (code != ConnectionResult.SUCCESS) {
      Dialog dlg = GoogleApiAvailability.getInstance()
          .getErrorDialog(this, code, RC_HANDLE_GMS);
      dlg.show();
    }

    if (mCameraSource != null) {
      openedPermissionDialog = false;
      try {
        mPreview.start(mCameraSource, mGraphicOverlay);
      } catch (IOException e) {
        Timber.e(e, "Unable to start camera source.");
        mCameraSource.release();
        mCameraSource = null;
      }
    }
  }
}
