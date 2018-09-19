package com.hipsterbait.android.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.widgets.HBEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChangeUsernameActivity extends ImmersiveActivity {

    public HBEditText usernameField;
    public ImageView avatarImage;

    private User userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);

        usernameField = (HBEditText) findViewById(R.id.change_username_username_field);

        avatarImage = (ImageView) findViewById(R.id.change_username_avatar_image);

        userModel = ((HBApplication) getApplication()).user;

        Bitmap avatar = userModel.getAvatarImage();

        if (avatar != null) {
            avatarImage.setImageBitmap(avatar);
        } else {
            avatarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar_generic));
        }
    }

    public void homeButtonTapped(View v) {
        Intent intent = new Intent(ChangeUsernameActivity.this, UserAccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void doneTapped(View v) {

        if (usernameIsValid()) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(usernameField.getText().toString())
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                userModel.setUsername(usernameField.getText().toString());
                                userModel.save();

                                Toast.makeText(ChangeUsernameActivity.this, R.string.name_changed, Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(ChangeUsernameActivity.this, UserAccountActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(ChangeUsernameActivity.this, R.string.name_changed_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void cancelTapped(View v) {
        finish();
    }

    private boolean usernameIsValid() {
        String username = usernameField.getText().toString();
        boolean valid = true;

        if (username.equals("")) {
            usernameField.setError(getString(R.string.required));
            valid = false;
        }

        return valid;
    }
}
