package com.hipsterbait.android.activities;

import android.os.Bundle;
import android.view.View;

import com.hipsterbait.android.R;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.widgets.HBTextView;

public class ConfirmUnarchiveActivity extends ImmersiveActivity {

    private String mCassetteKey, mName;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_unarchive);

        mCassetteKey = getIntent().getStringExtra(getString(R.string.cassette_extra_key));
        mName = getIntent().getStringExtra(getString(R.string.cassette_extra_name));
        mUser = ((HBApplication) getApplication()).user;

        ((HBTextView) findViewById(R.id.unarchive_confirm_cassette_name)).setText(mName);
    }

    public void confirmTapped(View v) {
        mUser.unArchiveCassette(mCassetteKey);
        finish();
    }

    public void closeButtonTapped(View v) {
        finish();
    }
}
