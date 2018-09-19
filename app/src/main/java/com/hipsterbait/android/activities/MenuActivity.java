package com.hipsterbait.android.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hipsterbait.android.R;
import com.hipsterbait.android.other.HBApplication;

public class MenuActivity extends ImmersiveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void findCassettesTapped(View v) {
        Intent intent = new Intent(MenuActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void loginTapped(View v) {
        Intent intent = new Intent(MenuActivity.this, LoginMenuActivity.class);
        startActivity(intent);
        finish();
    }

    public void artistTapped(View v) {

    }

    public void chartsTapped(View v) {

    }

    public void closeButtonTapped(View v) {
        finish();
    }
}
