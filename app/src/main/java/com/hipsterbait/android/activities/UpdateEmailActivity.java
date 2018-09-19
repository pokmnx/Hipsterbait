package com.hipsterbait.android.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hipsterbait.android.R;
import com.hipsterbait.android.widgets.HBEditText;

public class UpdateEmailActivity extends ImmersiveActivity {

    public HBEditText emailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        emailField = (HBEditText) findViewById(R.id.update_email_email_field);
    }

    public void updateEmailTapped(View v) {

        if (!validateForm()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updateEmail(emailField.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            finish();
                        } else {
                            Log.w("HB", "updateEmail failed " + task.getException().getLocalizedMessage());
                            finish();
                        }
                    }
                });
    }

    public boolean validateForm() {
        String email = emailField.getText().toString();

        boolean valid = true;

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches() == false) {
            emailField.setError(getString(R.string.email_invalid_error));
            valid = false;
        }

        if (email.equals("")) {
            emailField.setError(getString(R.string.email_required_error));
            valid = false;
        }

        return valid;
    }
}
