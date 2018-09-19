package com.hipsterbait.android.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.hipsterbait.android.R;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class QRScannerActivity extends ImmersiveActivity {

    private final int CAMERA_ID = 0;
    public ImageView qrScanTarget;
    public FrameLayout preview;

    private boolean torchActive = false;
    private Camera mCamera;
    private CameraPreview mPreview;

    private static Hashtable hints;
        static {
            hints = new Hashtable(1);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        }

    private Timer cameraTimer;
    private CameraTimerTask cameraTimerTask;
    private Camera.PreviewCallback cameraPreviewCallback;

    final static int WIDTH = 320;
    final static int HEIGHT = 480;
    final static int PERMISSION_CAMERA_CODE = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        qrScanTarget = (ImageView) findViewById(R.id.qrscanner_scan_target);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("Permission Required")
                        .setMessage("Hipster Bait needs your camera in order to capture cassettes.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(QRScannerActivity.this,
                                        new String[]{android.Manifest.permission.CAMERA},
                                        PERMISSION_CAMERA_CODE);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(QRScannerActivity.this,
                                        "Hipster Bait requires your camera", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_CAMERA_CODE);
            }
        } else {
            cameraPermissionGranted();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraTimerTask != null) cameraTimerTask.cancel();
        releaseCamera();
    }

    private void cameraPermissionGranted() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            try {
                mCamera = Camera.open();
                mPreview = new CameraPreview(this, mCamera);
                preview = (FrameLayout) findViewById(R.id.qrscanner_camera_preview);
                preview.addView(mPreview);
                qrScanTarget.bringToFront();

                cameraTimer = new Timer();
                cameraTimerTask = new CameraTimerTask();
                cameraTimer.schedule(cameraTimerTask, 2000, 80);

                cameraPreviewCallback = new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        try {
                            Camera.Size size = camera.getParameters().getPreviewSize();
                            final PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, size.width, size.height, 0, 0, size.width, size.height, false);
                            final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                            new DecodeImageTask().execute(bitmap);
                        } catch (Exception e) {
                            Log.w("HB", e.getLocalizedMessage());
                        }
                    }
                };
            } catch (Exception e) {
                Log.e(getString(R.string.hb_log_tag), getString(R.string.camera_open_failed));
                finish();
            }
        } else {
            finish();
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    public void flashButtonTapped(View v) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Camera.Parameters p = mCamera.getParameters();
            if (torchActive) {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            mCamera.setParameters(p);
            torchActive = !torchActive;

        } else {
            Toast.makeText(this, "Your device does not have a camera flash", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void closeButtonTapped(View v) {
        finish();
    }

    private class CameraTimerTask extends TimerTask {
        @Override
        public void run() {
            mPreview.autoFocusAndPreviewCallback();
        }
    }

    private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(getString(R.string.hb_log_tag), "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(getString(R.string.hb_log_tag), "Error starting camera preview: " + e.getMessage());
            }
        }

        public void autoFocusAndPreviewCallback() {
            if (mCamera != null) {
                try {
//                    mCamera.cancelAutoFocus();
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                camera.setOneShotPreviewCallback(cameraPreviewCallback);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.w(getString(R.string.hb_log_tag), e);
                }
            }
        }
    }

    private class DecodeImageTask extends AsyncTask<BinaryBitmap, Void, String> {

        @Override
        protected String doInBackground(BinaryBitmap... bitmap) {
            String decodedText = null;
            final Reader reader = new QRCodeReader();
            try {
                final Result result = reader.decode(bitmap[0], hints);
                decodedText = result.getText();
                // cameraTimer.cancel();
            } catch (Exception e) {
                decodedText = null;
            }
            return decodedText;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {
                return;
            }

            Toast.makeText(QRScannerActivity.this, result, Toast.LENGTH_LONG)
                    .show();

            qrScanTarget.setImageDrawable(ContextCompat.getDrawable(
                    QRScannerActivity.this, R.drawable.qr_scan_active));
        }
    }
}
