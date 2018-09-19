package com.hipsterbait.android.activities;

import android.os.Bundle;
import android.view.View;

import com.hipsterbait.android.R;

public class ArchiveErrorActivity extends ImmersiveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_error);
    }

    public void closeButtonTapped(View v) {
        finish();
    }
}
