package com.hipsterbait.android.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.hipsterbait.android.R;
import com.hipsterbait.android.other.LoadingPick;

public class TestLoadingActivity extends AppCompatActivity {

    public LoadingPick loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_loading);

        loadingLayout = (LoadingPick) findViewById(R.id.loading_pick);
    }
}
