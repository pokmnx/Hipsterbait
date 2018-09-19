package com.hipsterbait.android.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.other.HBAnimationDrawable;

import java.util.Random;

public class ARCaptureActivity extends ImmersiveActivity implements SensorEventListener {

    public RelativeLayout preview, arrowContainer;
    public ImageView arrowUp, arrowLeft, arrowDown, arrowRight;

    private ImageView mCassetteSprite;
    private Camera mCamera;
    private CameraPreview mPreview;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor rotation;

    private Cassette mCassette;

    private AlertDialog dialog;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float[] mOrientation = new float[3];

    private float mYawPositionIn360;
    private float mPitchPositionIn360;
    private float mCassetteYawPosition;
    private float mCassettePitchPosition;
    private int mPositionsCount = 0;
    private float[] mYawPositions = new float[POSITIONS_ARRAY_SIZE];
    private float[] mPitchPositions = new float[POSITIONS_ARRAY_SIZE];

    private int mHalfScreenWidth;
    private int mHalfScreenHeight;
    private float mHorizontalDegreeMultiplier;
    private float mVerticalDegreeMultiplier;
    private float mHorizontalFOV;
    private float mVerticalFOV;
    private boolean mTapped = false;

    public static final float ALPHA = 0.10f;
    public static final int POSITIONS_ARRAY_SIZE = 10;
    public static final int PERMISSION_CAMERA_CODE = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcapture);

        mCassette = getIntent().getParcelableExtra(getString(R.string.cassette_extra));

        preview = (RelativeLayout) findViewById(R.id.ar_capture_layout);
        arrowUp = (ImageView) findViewById(R.id.ar_arrow_up);
        arrowLeft = (ImageView) findViewById(R.id.ar_arrow_left);
        arrowDown = (ImageView) findViewById(R.id.ar_arrow_down);
        arrowRight = (ImageView) findViewById(R.id.ar_arrow_right);
        arrowContainer = (RelativeLayout) findViewById(R.id.ar_arrow_container);

        mCassetteYawPosition = 1 + new Random().nextInt(360 - 1 + 1);
        mCassettePitchPosition = 45 + new Random().nextInt(135 - 45 + 1);

        ChildEventListener tooSlowListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(getString(R.string.hidden))) {
                    boolean value = (Boolean) dataSnapshot.getValue();
                    if (value == false) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        Intent intent = new Intent(ARCaptureActivity.this, TooSlowActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        mCassette.getRef().addChildEventListener(tooSlowListener);

        mCassette.setCassetteModel(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                mCassette.getCassetteModel().setSong(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        mCassette.getCassetteModel().getSong().setBand(null);
                        mCassette.getCassetteModel().getSong().downloadSong(new ModelPropertySetCallback() {
                            @Override
                            public void onSuccess() {
                                Log.w("TEST", "Song Loaded");
                            }
                            @Override
                            public void onFail(String error) {

                            }
                        });
                    }
                    @Override
                    public void onFail(String error) {

                    }
                });
            }
            @Override
            public void onFail(String error) {

            }
        });

        mChildListeners.put(mCassette.getRef(), tooSlowListener);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_capture, null);

        final AlertDialog alertD = new AlertDialog.Builder(this).create();
        alertD.setView(promptView);

        Button okbtn = (Button) promptView.findViewById(R.id.dialog_capture_ok_button);
        Button cancelbtn = (Button) promptView.findViewById(R.id.dialog_capture_cancel_button);

        okbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertD.dismiss();
            }
        });

        cancelbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertD.dismiss();
                setResult(HomeMapActivity.CAPTURE_CODE, new Intent());
                finish();
            }
        });
        alertD.show();

        mCassetteSprite = new ImageView(this);

        final float scale = getResources().getDisplayMetrics().density;

        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                Math.round(230 * scale), Math.round(160 * scale));
        layoutParams.setMargins(5000, 100, 0, 0);
        mCassetteSprite.setLayoutParams(layoutParams);

        mCassetteSprite.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.alert_animation_1));
        final AnimationDrawable alertAnim = (AnimationDrawable) mCassetteSprite.getDrawable();
        alertAnim.start();

        preview.addView(mCassetteSprite);

        mCassetteSprite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTapped = true;

                alertAnim.stop();

                mCassetteSprite.getDrawable().setCallback(null);
                mCassetteSprite.setImageDrawable(null);

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                RelativeLayout.LayoutParams initialParams = (RelativeLayout.LayoutParams) mCassetteSprite.getLayoutParams();
                final int initialMargin = initialParams.topMargin;
                final int finalMargin = size.y;
                Animation dropAnim = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCassetteSprite.getLayoutParams();
                        params.topMargin = initialMargin + (int)(finalMargin * interpolatedTime);
                        mCassetteSprite.setLayoutParams(params);
                    }
                };

                final RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mCassetteSprite.getLayoutParams();
                currentParams.height = Math.round(170 * scale);
                currentParams.width = Math.round(170 * scale);
                mCassetteSprite.setLayoutParams(currentParams);

                mCassetteSprite.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.spin_animation));
                final AnimationDrawable spinAnim = (AnimationDrawable) mCassetteSprite.getDrawable();
                spinAnim.start();

                dropAnim.setDuration(500);

                dropAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        spinAnim.stop();
                        Intent intent = new Intent(ARCaptureActivity.this, FoundCassetteActivity.class);
                        intent.putExtra(getString(R.string.cassette_extra), mCassette);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });

                mCassetteSprite.startAnimation(dropAnim);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                dialog = builder.setTitle("Permission Required")
                        .setMessage("Hipster Bait needs your camera in order to capture cassettes.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(ARCaptureActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSION_CAMERA_CODE);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(ARCaptureActivity.this,
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

//        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    cameraPermissionGranted();
                } else {
                    Toast.makeText(this, "Hipster Bait requires your camera", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mHalfScreenWidth = preview.getWidth() / 2;
        mHalfScreenHeight = preview.getHeight() / 2;
    }

    private void cameraPermissionGranted() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            try {
                mCamera = Camera.open();
                mPreview = new CameraPreview(this, mCamera);
                preview.addView(mPreview);
                preview.bringChildToFront(mCassetteSprite);
                preview.bringChildToFront(arrowContainer);

                mHorizontalFOV = mCamera.getParameters().getVerticalViewAngle();
                mVerticalFOV = mCamera.getParameters().getHorizontalViewAngle();

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                mHalfScreenWidth = size.x / 2;
                mHalfScreenHeight = (size.y / 2) - 50;
                mHorizontalDegreeMultiplier = size.x / mHorizontalFOV;
                mVerticalDegreeMultiplier = size.y / mVerticalFOV;

            } catch (Exception e) {
                Log.e(getString(R.string.hb_log_tag), getString(R.string.camera_open_failed));
                finish();
            }
        }
    }

    private void updateX() {
        float difference = 0;

        if (mYawPositionIn360 < mHorizontalFOV / 2) {
            if (mCassetteYawPosition > 360 - (mHorizontalFOV / 2)) {
                // Cassette is on the right
                difference = (360 - mCassetteYawPosition) + mYawPositionIn360;
                float xPos = mHalfScreenWidth + (difference * mHorizontalDegreeMultiplier);
                setCassetteSpriteX(xPos);
            } else {
                runStandardXPositionCheck(difference);
            }
        } else if (mYawPositionIn360 > 360 - (mHorizontalFOV / 2)) {
            if (mCassetteYawPosition < mHorizontalFOV / 2) {

                difference = mCassetteYawPosition + (360 - mYawPositionIn360);
                float xPos = mHalfScreenWidth - (difference * mHorizontalDegreeMultiplier);
                setCassetteSpriteX(xPos);
            } else {
                runStandardXPositionCheck(difference);
            }
        } else {
            runStandardXPositionCheck(difference);
        }
    }

    private void runStandardXPositionCheck(float difference) {
        if (mCassetteYawPosition > mYawPositionIn360) {
            // Cassette is on the left
            difference = mCassetteYawPosition - mYawPositionIn360;
            float xPos = mHalfScreenWidth - (difference * mHorizontalDegreeMultiplier);
            setCassetteSpriteX(xPos);
        } else {
            // Cassette is on the right
            difference = mYawPositionIn360 - mCassetteYawPosition;
            float xPos = mHalfScreenWidth + (difference * mHorizontalDegreeMultiplier);
            setCassetteSpriteX(xPos);
        }
    }

    private void setCassetteSpriteX(float xPos) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                mCassetteSprite.getLayoutParams();

        float difference = (mHalfScreenWidth * 2) - xPos;
        int rightMargin = 0;
        if (difference < layoutParams.width && xPos >= 0 && difference > 0) {
            rightMargin = Math.round(layoutParams.width - difference);
            rightMargin = -1 * rightMargin;
        }

        layoutParams.setMargins(
                (int) xPos,
                layoutParams.topMargin,
                rightMargin,
                layoutParams.bottomMargin);
        mCassetteSprite.setLayoutParams(layoutParams);
    }

    private void updateY() {
        float difference = 0;

        if (mPitchPositionIn360 < mVerticalFOV / 2) {
            if (mCassettePitchPosition > 360 - (mVerticalFOV / 2)) {
                difference = (360 - mCassettePitchPosition) + mPitchPositionIn360;
                float yPos = mHalfScreenHeight + (difference * mVerticalDegreeMultiplier);
                setCassetteSpriteY(yPos);
            } else {
                runStandardYPositionCheck(difference);
            }
        } else if (mPitchPositionIn360 > 360 - (mVerticalFOV / 2)) {
            if (mCassettePitchPosition < mVerticalFOV / 2) {
                difference = mCassettePitchPosition + (360 - mPitchPositionIn360);
                float yPos = mHalfScreenHeight - (difference * mVerticalDegreeMultiplier);
                setCassetteSpriteY(yPos);
            } else {
                runStandardYPositionCheck(difference);
            }
        } else {
            runStandardYPositionCheck(difference);
        }
    }

    private void runStandardYPositionCheck(float difference) {
        if (mCassettePitchPosition > mPitchPositionIn360) {
            difference = mCassettePitchPosition - mPitchPositionIn360;
            float yPos = mHalfScreenHeight - (difference * mVerticalDegreeMultiplier);
            setCassetteSpriteY(yPos);
        } else {
            difference = mPitchPositionIn360 - mCassettePitchPosition;
            float yPos = mHalfScreenHeight + (difference * mVerticalDegreeMultiplier);
            setCassetteSpriteY(yPos);
        }
    }

    private void setCassetteSpriteY(float yPos) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                mCassetteSprite.getLayoutParams();

        float difference = (mHalfScreenHeight * 2) - yPos;
        int bottomMargin = 0;
        if (difference < layoutParams.height && yPos >= 0 && difference > 0) {
            bottomMargin = Math.round(layoutParams.height - difference);
            bottomMargin = -1 * bottomMargin;
        }

        layoutParams.setMargins(
                layoutParams.leftMargin,
                (int) yPos,
                layoutParams.rightMargin,
                bottomMargin);
        mCassetteSprite.setLayoutParams(layoutParams);
    }

    private void showHideIndicators() {
        float cassetteLeftBound = mCassetteSprite.getLeft();
        float cassetteTopBound = mCassetteSprite.getTop();
        float cassetteRightBound = mCassetteSprite.getLeft() + mCassetteSprite.getWidth();
        float cassetteBottomBound = mCassetteSprite.getTop() + mCassetteSprite.getHeight();

        float containerLeftBound = 0;
        float containerTopBound = 0;
        float containerRightBound = preview.getWidth();
        float containerBottomBound = preview.getHeight();

        boolean inHorizontal = false;
        boolean left = true;
        float horDiff = mYawPositionIn360 - mCassetteYawPosition;

        if (horDiff > 0 && Math.abs(horDiff) < 180) {
            left = false;
        } else if (horDiff > 0 && Math.abs(horDiff) > 180) {
            left = true;
        } else if (horDiff < 0 && Math.abs(horDiff) < 180) {
            left = true;
        } else if (horDiff < 0 && Math.abs(horDiff) > 180) {
            left = false;
        }

        if (cassetteLeftBound > containerLeftBound && cassetteRightBound < containerRightBound) {
            inHorizontal = true;
            arrowRight.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_right_off));
            arrowLeft.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_left_off));
        } else if (left) {
            arrowRight.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_right_off));
            arrowLeft.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_down_left));
        } else {
            arrowRight.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_down_right));
            arrowLeft.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_left_off));
        }

        boolean inVertical = false;
        boolean up = true;
        float verDiff = mPitchPositionIn360 - mCassettePitchPosition;

        if (verDiff > 0 && Math.abs(verDiff) < 180) {
            up = false;
        } else if (verDiff > 0 && Math.abs(verDiff) > 180) {
            up = true;
        } else if (verDiff < 0 && Math.abs(verDiff) < 180) {
            up = true;
        } else if (verDiff < 0 && Math.abs(verDiff) < 180) {
            up = false;
        }

        if (cassetteTopBound > containerTopBound && cassetteBottomBound < containerBottomBound) {
            inVertical = true;
            arrowUp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_up_off));
            arrowDown.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_down_off));
        } else if (up) {
            arrowUp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_down_up));
            arrowDown.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_down_off));
        } else {
            arrowUp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_up_off));
            arrowDown.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ar_arrow_down_on));
        }

        if (inHorizontal && inVertical) {
            arrowContainer.setVisibility(View.INVISIBLE);
        } else {
            arrowContainer.setVisibility(View.VISIBLE);
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public void onSensorChanged(SensorEvent event) {
        if (mTapped) {
            return;
        }

        float R[] = new float[16];
        float[] cameraRotation = new float[16];

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(R, event.values);
        }

        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X,
        SensorManager.AXIS_Z, cameraRotation);   // Remap coordinate System to compensate for the landscape position of device
        SensorManager.getOrientation(cameraRotation, mOrientation);

        float yawPositionIn360 = (float) (-1 * Math.toDegrees((double) mOrientation[0]));
        if (yawPositionIn360 < 0) {
            yawPositionIn360 = 360 + yawPositionIn360;
        }

        float pitchPositionIn360 = (float) (-1 * Math.toDegrees((double) mOrientation[1]));
        if (pitchPositionIn360 < 0) {
            pitchPositionIn360 = 360 + pitchPositionIn360;
        }

        pitchPositionIn360 += 90;
        if (pitchPositionIn360 > 360) {
            pitchPositionIn360 -= 360;
        }

        mYawPositionIn360 = yawPositionIn360;
        mPitchPositionIn360 = pitchPositionIn360;

        updateX();
        updateY();

        showHideIndicators();

//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//            mGravity = lowPass(event.values.clone(), mGravity);
//        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
//            mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
//        if (mGravity != null && mGeomagnetic != null) {
//            float R[] = new float[9];
//            float I[] = new float[9];
//            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
//            if (success) {
////                SensorManager.getOrientation(R, mOrientation);
//
//                float[] cameraRotation = new float[9];
//                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, cameraRotation);
//                SensorManager.getOrientation(cameraRotation, mOrientation);
//
//                float yawPositionIn360 = (float) (-1 * Math.toDegrees((double) mOrientation[0]));
//                if (yawPositionIn360 < 0) {
//                    yawPositionIn360 = 360 + yawPositionIn360;
//                }
//
//                float pitchPositionIn360 = (float) (-1 * Math.toDegrees((double) mOrientation[1]));
//                if (pitchPositionIn360 < 0) {
//                    pitchPositionIn360 = 360 + pitchPositionIn360;
//                }
//
//                pitchPositionIn360 += 90;
//                if (pitchPositionIn360 > 360) {
//                    pitchPositionIn360 -= 360;
//                }
//
//                if (mPositionsCount < POSITIONS_ARRAY_SIZE) {
//                    mYawPositions[mPositionsCount] = yawPositionIn360;
//                    mPitchPositions[mPositionsCount] = pitchPositionIn360;
//                    mPositionsCount += 1;
//                } else {
//
//                    float totalYaw = 0;
//                    float[] newYawPositions = new float[POSITIONS_ARRAY_SIZE];
//                    int index = 0;
//                    for (float position : mYawPositions) {
//                        totalYaw += position;
//                        if (index > 0) {
//                            newYawPositions[index - 1] = position;
//                        }
//                        index += 1;
//                    }
//                    newYawPositions[POSITIONS_ARRAY_SIZE - 1] = yawPositionIn360;
//                    mYawPositions = newYawPositions;
//                    mYawPositionIn360 = totalYaw / mYawPositions.length;
//
//                    updateX();
//
//                    float totalPitch = 0;
//                    float[] newPitchPositions = new float[POSITIONS_ARRAY_SIZE];
//                    index = 0;
//                    for (float position : mPitchPositions) {
//                        totalPitch += position;
//                        if (index > 0) {
//                            newPitchPositions[index - 1] = position;
//                        }
//                        index += 1;
//                    }
//                    newPitchPositions[POSITIONS_ARRAY_SIZE - 1] = pitchPositionIn360;
//                    mPitchPositions = newPitchPositions;
//                    mPitchPositionIn360 = totalPitch / mPitchPositions.length;
//
//                    Log.w("TEST", "YAW: " + String.valueOf(mYawPositionIn360));
////                    Log.w("TEST", "PITCH: " + String.valueOf(mPitchPositionIn360));
//                    Log.w("TEST", "azimut: " + String.valueOf(mOrientation[0]));
//
//                    updateY();
//
//                    showHideIndicators();
//                }
//            }
//        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void closeButtonTapped(View v) {
        finish();
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
                if (mCamera == null) {
                    finish();
                    return;
                }

                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
            } catch (Exception e) {
                Log.d(getString(R.string.hb_log_tag), "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();

            } catch (Exception e) {
                Log.d(getString(R.string.hb_log_tag), "Error starting camera preview: " + e.getMessage());
            }
        }
    }

}
