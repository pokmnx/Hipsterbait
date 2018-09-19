package com.hipsterbait.android.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.models.Points;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserPoints;
import com.hipsterbait.android.other.CacheManager;
import com.hipsterbait.android.other.GenericFileProvider;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.RotatingTexts;
import com.hipsterbait.android.widgets.HBEditText;
import com.squareup.picasso.Cache;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ChangePasswordActivity extends ImmersiveActivity {

    public HBEditText oldPasswordField, passwordField, repeatPasswordField;
    public ImageView avatarImage;

    private final int PICK_PHOTO_FOR_AVATAR = 6969;
    private static final int PERMISSION_CAMERA_CODE = 666;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPasswordField = (HBEditText) findViewById(R.id.change_password_oldpass_field);
        passwordField = (HBEditText) findViewById(R.id.change_password_password_field);
        repeatPasswordField = (HBEditText) findViewById(R.id.change_password_repeat_password_field);

        avatarImage = (ImageView) findViewById(R.id.change_password_avatar_image);

        mUser = ((HBApplication) getApplication()).user;

        Bitmap avatar = mUser.getAvatarImage();

        if (avatar != null) {
            avatarImage.setImageBitmap(avatar);
        } else {
            avatarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar_generic));
        }
    }

    public void homeTapped(View v) {
        finish();
    }

    public void avatarTapped(View v) {
        CropImage.activity(null)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
                .start(this);
    }

    public void editTapped(View v) {
        Intent intent = new Intent(ChangePasswordActivity.this, ChangeUsernameActivity.class);
        startActivity(intent);
    }

    public void saveTapped(View v) {

        if (formIsValid()) {

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String newPassword = passwordField.getText().toString();

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPasswordField.getText().toString());

            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(ChangePasswordActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ChangePasswordActivity.this, R.string.password_changed, Toast.LENGTH_SHORT).show();
                                            finish();

                                        } else {
                                            Exception exception = task.getException();

                                            if (exception != null) {
                                                Toast.makeText(ChangePasswordActivity.this,
                                                        task.getException().getLocalizedMessage(),
                                                        Toast.LENGTH_LONG)
                                                        .show();

                                                Intent intent = new Intent(ChangePasswordActivity.this, LoginMenuActivity.class);
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void closeTapped(View v) {
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    //Display an error
                    return;
                }

                try {
                    InputStream inputStream = getContentResolver().openInputStream(result.getUri());
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    int nRead;
                    byte[] bytes = new byte[16384];

                    while ((nRead = inputStream.read(bytes, 0, bytes.length)) != -1) {
                        buffer.write(bytes, 0, nRead);
                    }

                    buffer.flush();

                    final byte[] imgData = buffer.toByteArray();

                    final StorageReference ref = FirebaseStorage.getInstance().getReference().child("avatars").child(mUser.getKey()).child("thumbnail");
                    ref.putBytes(imgData)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                                    if (mUser.getAvatarImageRef() == null) {
                                        DatabaseReference avatarSetPointsRef = FirebaseDatabase.getInstance().getReference().child("points").child("avatarSet");
                                        avatarSetPointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    try {
                                                        int oldPoints = mUser.getPoints();

                                                        Points points = new Points(dataSnapshot);
                                                        UserPoints userPoints = new UserPoints(mUser.getKey(), points.getKey(), points.getValue(), null, null, null);
                                                        userPoints.save();

                                                        ArrayList<UserPoints> result = new ArrayList<>();
                                                        result.add(userPoints);

                                                        int newPoints = 0;
                                                        for (UserPoints uPoints : result) {
                                                            newPoints += uPoints.getValue();
                                                        }

                                                        Bundle args = new Bundle();
                                                        args.putString(getString(R.string.title_string_arg), RotatingTexts.getString(RotatingTexts.HINT_SHARE));
                                                        args.putParcelableArrayList(getString(R.string.userpoints_arraylist_arg), result);
                                                        args.putInt(getString(R.string.old_points), oldPoints);
                                                        args.putInt(getString(R.string.new_points), newPoints);
                                                        BottomSheetDialogFragment pointsFrag = new PointsReceiptFragment();
                                                        pointsFrag.setArguments(args);
                                                        pointsFrag.show(getSupportFragmentManager(), pointsFrag.getTag());

                                                    } catch (RequiredValueMissing e) {
                                                        Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    StorageMetadata metadata = taskSnapshot.getMetadata();
                                    try {
                                        mUser.setAvatarImageRef(metadata.getDownloadUrl().toString());

                                    } catch (NullPointerException e) {
                                        Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                    }

                                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                                    mUser.setAvatarImage(bitmap);
                                    mUser.save();

                                    avatarImage.setImageBitmap(bitmap);

                                    CacheManager.getInstance().cacheImageData(imgData, ref);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                }
                            });

                } catch (FileNotFoundException e) {
                    Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                } catch (IOException e) {
                    Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private boolean formIsValid() {
        String password = passwordField.getText().toString();
        String repeatPassword = repeatPasswordField.getText().toString();
        boolean valid = true;

        if (password.equals("")) {
            passwordField.setError(getString(R.string.required));
            valid = false;
        }

        if (repeatPassword.equals("")) {
            repeatPasswordField.setError(getString(R.string.required));
            valid = false;
        }

        if (repeatPassword.equals(password) == false) {
            repeatPasswordField.setError(getString(R.string.passwords_mismatched));
            valid = false;
        }

        return valid;
    }
}
