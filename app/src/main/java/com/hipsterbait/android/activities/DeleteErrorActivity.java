package com.hipsterbait.android.activities;

import android.os.Bundle;
import android.view.View;

import com.hipsterbait.android.R;

public class DeleteErrorActivity extends ImmersiveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_error);
    }

    public void closeButtonTapped(View v) {
        finish();
    }
}
