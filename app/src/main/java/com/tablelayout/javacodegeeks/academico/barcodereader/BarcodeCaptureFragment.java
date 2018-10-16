package com.tablelayout.javacodegeeks.academico.barcodereader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.tablelayout.javacodegeeks.academico.R;
import com.tablelayout.javacodegeeks.academico.barcodereader.ui.camera.CameraSource;
import com.tablelayout.javacodegeeks.academico.barcodereader.ui.camera.CameraSourcePreview;
import com.tablelayout.javacodegeeks.academico.barcodereader.ui.camera.GraphicOverlay;

public class BarcodeCaptureFragment extends Fragment implements BarcodeGraphicTracker.BarcodeDetectorListener {

  private static final String TAG = "Barcode-reader";
  private static final int RC_HANDLE_GMS = 9001;

  private static final int RC_HANDLE_CAMERA_PERM = 2;

  public static final String BarcodeObject = "Barcode";

  private CameraSource mCameraSource;
  private CameraSourcePreview mPreview;
  private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

  // helper objects for detecting taps and pinches.
  private ScaleGestureDetector scaleGestureDetector;
  private GestureDetector gestureDetector;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View inflate = inflater.inflate(R.layout.barcode_capture, container, false);

    int rc = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
    if (rc == PackageManager.PERMISSION_GRANTED)
      createCameraSource();
    else
      requestCameraPermission();

    gestureDetector = new GestureDetector(getContext(), new CaptureGestureListener());
    scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

    Snackbar
        .make(mGraphicOverlay, "Posicione para capturar automático ou foque e segure para capturar.",
        Snackbar.LENGTH_LONG)
        .show();

    return inflate;
  }

  private void requestCameraPermission() {
    Log.w(TAG, "Camera permission is not granted. Requesting permission");

    final String[] permissions = new String[]{Manifest.permission.CAMERA};

    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
        Manifest.permission.CAMERA)) {
      ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_CAMERA_PERM);
      return;
    }

    Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
        Snackbar.LENGTH_INDEFINITE)
        .setAction(R.string.ok, new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            ActivityCompat.requestPermissions(getActivity(), permissions,
                RC_HANDLE_CAMERA_PERM);
          }
        })
        .show();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mPreview = (CameraSourcePreview) getView().findViewById(R.id.preview);
    mGraphicOverlay = (GraphicOverlay<BarcodeGraphic>) getView().findViewById(R.id.graphicOverlay);
  }

  @Override
  public void onObjectDetected(Barcode data) {

  }

  private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {

      return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
    }
  }

  private boolean onTap(float rawX, float rawY) {
    // Find tap point in preview frame coordinates.
    int[] location = new int[2];
    mGraphicOverlay.getLocationOnScreen(location);
    float x = (rawX - location[0]) / mGraphicOverlay.getWidthScaleFactor();
    float y = (rawY - location[1]) / mGraphicOverlay.getHeightScaleFactor();

    Barcode barcode = null;
    float bestDistance = Float.MAX_VALUE;

    for (BarcodeGraphic graphic : mGraphicOverlay.getGraphics()) {
      Barcode barcode_graphic = graphic.getBarcode();
      if(barcode_graphic != null) {
        if (barcode_graphic.getBoundingBox().contains((int) x, (int) y)) {
          // Exact hit, no need to keep looking.
          barcode = barcode_graphic;
          break;
        }
        float dx = x - barcode_graphic.getBoundingBox().centerX();
        float dy = y - barcode_graphic.getBoundingBox().centerY();
        float distance = (dx * dx) + (dy * dy);  // actually squared distance
        if (distance < bestDistance) {
          barcode = barcode_graphic;
          bestDistance = distance;
        }
      }
    }

    if (barcode != null) {
      Intent data = new Intent();
      data.putExtra(BarcodeObject, barcode);
      getActivity().setResult(CommonStatusCodes.SUCCESS, data);
      getActivity().finish();
    }
    else
      Log.d(TAG,"no barcode detected");
    return barcode != null;
  }

  private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
      mCameraSource.doZoom(detector.getScaleFactor());
    }
  }

  @SuppressLint("InlinedApi")
  private void createCameraSource() {
    Context context = getContext();

    // A barcode detector is created to track barcodes.  An associated multi-processor instance
    // is set to receive the barcode detection results, track the barcodes, and maintain
    // graphics for each barcode on screen.  The factory is used by the multi-processor to
    // create a separate tracker instance for each barcode.
    BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
    BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, true?this:null);
    barcodeDetector.setProcessor(
        new MultiProcessor.Builder<>(barcodeFactory).build());

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
      Log.w(TAG, "Detector dependencies are not yet available.");

      // Check for low storage.  If there is low storage, the native library will not be
      // downloaded, so detection will not become operational.
      IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
      boolean hasLowStorage = getActivity().registerReceiver(null, lowstorageFilter) != null;

      if (hasLowStorage) {
        Toast.makeText(getContext(), R.string.low_storage_error, Toast.LENGTH_LONG).show();
        Log.w(TAG, getString(R.string.low_storage_error));
      }
    }

    DisplayMetrics metrics = new DisplayMetrics();
    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

    // Creates and starts the camera.  Note that this uses a higher resolution in comparison
    // to other detection examples to enable the barcode detector to detect small barcodes
    // at long distances.
    @SuppressWarnings("SuspiciousNameCombination")
    CameraSource.Builder builder = new CameraSource.Builder(getContext(), barcodeDetector)
        .setFacing(CameraSource.CAMERA_FACING_BACK)
        .setRequestedPreviewSize(metrics.heightPixels, metrics.widthPixels)
        .setRequestedFps(30.0f);

    // make sure that auto focus is an available option
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
      builder = builder.setFocusMode(
          true ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);

    mCameraSource = builder
        .setFlashMode(false ? Camera.Parameters.FLASH_MODE_TORCH : null)
        .build();
  }

}
