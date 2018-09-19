package com.hipsterbait.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.hipsterbait.android.R;
import com.hipsterbait.android.widgets.HBEditText;
import com.hipsterbait.android.widgets.HBRadioButton;

public class EmailLoginActivity extends ImmersiveActivity {

    public HBEditText emailField, passwordField;
    public HBRadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        emailField = (HBEditText) findViewById(R.id.email_login_email_field);
        passwordField = (HBEditText) findViewById(R.id.email_login_password_field);

        radioButton = (HBRadioButton) findViewById(R.id.email_login_stay_logged_button);
    }

    public void loginTapped(View v) {
        if (validateForm() == false) {
            return;
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(emailField.getText().toString(), passwordField.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(EmailLoginActivity.this, HomeMapActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(EmailLoginActivity.this, getString(R.string.login_failed_message), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void closeButtonTapped(View v) {
        finish();
    }

    private boolean validateForm() {
        boolean result = true;

        if (emailField.getText().toString() == "") {
            emailField.setError("Email is required.");
            result = false;
        }

        if (passwordField.getText().toString() == "") {
            passwordField.setError("Password is required.");
            result = false;
        }

        return result;
    }
}
