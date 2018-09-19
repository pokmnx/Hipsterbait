package com.hipsterbait.android.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Hint;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.GenericFileProvider;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.LoadingPick;
import com.hipsterbait.android.other.RotatingTexts;
import com.hipsterbait.android.widgets.HBTextView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class BaitHintActivity extends ImmersiveActivity {
    private static final int CAMERA_REQUEST = 2112;
    private static final int BAIT_PREVIEW_REQUEST = 3030;
    private static final int PERMISSION_CAMERA_CODE = 666;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1337;

    public EditText editText;
    public ImageView photoButton;
    public HBTextView characterCount, sassyTitle;
    public RelativeLayout mainLayout;
    public LinearLayout loadingLayout;

    private AlertDialog dialog;

    private User mUser;
    private Cassette mCassette;
    private Journey mJourney;
    private Hint mHint;
    private DatabaseReference mDbRef;
    private boolean mPhoto = false;
    private File mPhotoFile;
    private Bitmap mPhotoBitmap;
    private Uri mPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bait_hint);

        mUser = ((HBApplication) getApplication()).user;
        mDbRef = FirebaseDatabase.getInstance().getReference();

        loadingLayout = (LinearLayout) findViewById(R.id.bait_hint_loading_layout);

        mainLayout = (RelativeLayout) findViewById(R.id.activity_bait_hint);
        editText = (EditText) findViewById(R.id.bait_hint_edittext);
        photoButton = (ImageView) findViewById(R.id.bait_hint_photo_button);
        characterCount = (HBTextView) findViewById(R.id.bait_hint_letter_count);
        sassyTitle = (HBTextView) findViewById(R.id.bait_hint_sassy_title);

        String cassetteKey = getIntent().getStringExtra(getString(R.string.cassette_extra_key));
        mCassette = mUser.getCassetteByKey(cassetteKey);
        mJourney = getIntent().getParcelableExtra(getString(R.string.journey_extra));

        sassyTitle.setText(RotatingTexts.getString(RotatingTexts.HINT_EARNINGS));
    }

    @Override
    public void onResume() {
        super.onResume();

        loadingLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPhotoUri != null) {
            outState.putString("cameraImageUri", mPhotoUri.toString());
        }
        outState.putBoolean("photo", mPhoto);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            mPhotoUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
        mPhoto = savedInstanceState.getBoolean("photo");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            mPhoto = true;
            photoButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.photo_button_on));
            mPhotoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/new_hint.jpg");
            mPhotoUri = GenericFileProvider.getUriForFile(this, "com.hipsterbait.provider", mPhotoFile);
            CropImage.activity(mPhotoUri)
                    .setFixAspectRatio(true)
                    .start(this);
        }

        if (requestCode == BAIT_PREVIEW_REQUEST && resultCode == Activity.RESULT_OK) {
            finish();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Toast.makeText(this, "Error: Couldn't perform image crop", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                mPhotoUri = result.getUri();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mPhotoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/new_hint.jpg");
                        mPhotoUri = GenericFileProvider.getUriForFile(this, "com.hipsterbait.provider", mPhotoFile);
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    } catch (Exception e) {
                        Toast.makeText(this, "Couldn't start camera: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        Log.w(getString(R.string.hb_log_tag), e);
                    }
                } else {
                    Toast.makeText(this, "Hipster Bait requires your location", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    public void cameraButtonTapped(View v) {

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
                                ActivityCompat.requestPermissions(BaitHintActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSION_CAMERA_CODE);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(BaitHintActivity.this,
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

    public void cameraPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                android.support.v7.app.AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new android.support.v7.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new android.support.v7.app.AlertDialog.Builder(this);
                }
                builder.setTitle("Permission Required")
                        .setMessage("Hipster Bait needs your storage to download cassette tracks.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(BaitHintActivity.this,
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(BaitHintActivity.this,
                                        "Hipster Bait requires your storage", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
        } else {
            try {
                mPhotoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/new_hint.jpg");
                mPhotoUri = GenericFileProvider.getUriForFile(this, "com.hipsterbait.provider", mPhotoFile);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } catch (Exception e) {
                Toast.makeText(this, "Couldn't start camera: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.w(getString(R.string.hb_log_tag), e);
            }
        }
    }

    public void previewTapped(View v) {
        if ((editText.getText() != null &&
                editText.getText().toString().equals("") == false &&
                editText.getText().toString().equals("Write something about the location...") == false)
                || mPhoto) {

            final float scale = getResources().getDisplayMetrics().density;

            loadingLayout.setVisibility(View.VISIBLE);

            if (editText.getText().toString().equals("") ||
                    editText.getText().toString().equals("Write something about the location...")) {
                editText.setText("No Comment.");
            }

            if (mHint == null) {
                mHint = new Hint(mUser.getKey(), mJourney.getKey(), editText.getText().toString(), null, mUser.getUsername(), "Location Hint 1", new Date().getTime(), null);
            } else {
                mHint.setDescription(editText.getText().toString());
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHint.save();

                    if (mPhoto) {
                        StorageReference ref = FirebaseStorage.getInstance().getReference()
                                .child(getString(R.string.hints))
                                .child(mHint.getKey());

                        try {
                            mPhotoBitmap = MediaStore.Images.Media.getBitmap(BaitHintActivity.this.getContentResolver(), mPhotoUri);

                            int width = mPhotoBitmap.getWidth();
                            int height = mPhotoBitmap.getHeight();

                            if (width > height) {
                                // landscape
                                int ratio = width / Math.round(200 * scale);
                                width = Math.round(200 * scale);
                                height = height / ratio;
                            } else if (height > width) {
                                // portrait
                                int ratio = height / Math.round(200 * scale);
                                height = Math.round(200 * scale);
                                width = width / ratio;
                            } else {
                                // square
                                height = Math.round(200 * scale);
                                width = Math.round(200 * scale);
                            }
                            mPhotoBitmap = Bitmap.createScaledBitmap(mPhotoBitmap, width, height, false);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            mPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            FileOutputStream fOut = new FileOutputStream(mPhotoFile);

                            mPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                            fOut.close();

                            byte[] byteArray = stream.toByteArray();
                            ref.putBytes(byteArray);

                            mHint.setImageRef(mHint.getKey());
                            mHint.save();
                        } catch (IOException e) {
                            Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(BaitHintActivity.this, BaitHintPreviewActivity.class);
                            intent.putExtra(getString(R.string.journey_extra), mJourney);
                            intent.putExtra(getString(R.string.cassette_extra_key), mCassette.getKey());
                            intent.putExtra(getString(R.string.hint_extra), mHint);
                            if (mPhotoUri != null) {
                                intent.putExtra(getString(R.string.image_extra), mPhotoUri.toString());
                            }

                            startActivityForResult(intent, BAIT_PREVIEW_REQUEST);
                        }
                    });
                }
            }).start();

        } else {
            Toast.makeText(this, "You can't leave an empty hint!", Toast.LENGTH_SHORT).show();
        }
    }

    public void closeTapped(View v) {
        finish();
    }
}
