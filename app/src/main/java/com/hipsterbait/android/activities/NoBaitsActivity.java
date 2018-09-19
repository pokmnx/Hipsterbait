package com.hipsterbait.android.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hipsterbait.android.R;

public class NoBaitsActivity extends ImmersiveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_baits);
    }

    public void burgerTapped(View v) {
        Intent intent = new Intent(NoBaitsActivity.this, LoggedMenuActivity.class);
        startActivity(intent);
        finish();
    }

    public void closeTapped(View v) {
        Intent intent = new Intent(NoBaitsActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
