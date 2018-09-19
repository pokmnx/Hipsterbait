package com.hipsterbait.android.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hipsterbait.android.R;
import com.hipsterbait.android.other.LoadingPick;

public class CreateAccountActivity extends ImmersiveActivity {

    public EditText emailField, passwordField, repeatPasswordField;
    public LinearLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        emailField = (EditText) findViewById(R.id.create_account_email_field);
        passwordField = (EditText) findViewById(R.id.create_account_password_field);
        repeatPasswordField = (EditText) findViewById(R.id.create_account_repeat_password_field);

        loadingLayout = (LinearLayout) findViewById(R.id.create_account_loading_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        immersiveMode();
    }

    public void createAccountTapped(View createAccountButton) {

        if (formIsValid()) {

            loadingLayout.setVisibility(View.VISIBLE);

            final FirebaseAuth auth = FirebaseAuth.getInstance();

            auth.createUserWithEmailAndPassword(emailField.getText().toString(), passwordField.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.w("TEST", "createAccountTapped");
                                FirebaseUser user = auth.getCurrentUser();
                                Intent intent = new Intent(CreateAccountActivity.this, HomeMapActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();

                            } else {
                                loadingLayout.setVisibility(View.INVISIBLE);
                                Log.w(getString(R.string.fir_log_tag), task.getException());
                                Toast.makeText(CreateAccountActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private boolean formIsValid() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String repeatPassword = repeatPasswordField.getText().toString();

        boolean valid = true;

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches() == false) {
            emailField.setError(getString(R.string.email_invalid_error));
            valid = false;
        }

        if (email.equals("")) {
            emailField.setError(getString(R.string.email_required_error));
            valid = false;
        }

        if (password.equals("")) {
            passwordField.setError(getString(R.string.password_required_error));
            valid = false;
        }

        if (repeatPassword.equals(password) == false) {
            repeatPasswordField.setError(getString(R.string.password_mismatch_error));
            valid = false;
        }

        return valid;
    }

    public void closeButtonTapped(View v) {
        finish();
    }

    private void immersiveMode() {
        // Fullscreen Acitivy
        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            immersiveMode();
        }
    }
}
