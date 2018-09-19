package com.hipsterbait.android.activities;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.widgets.HBButton;
import com.hipsterbait.android.widgets.HBTextView;

public class HoarderWarningActivity extends ImmersiveActivity {

    private Cassette mCassette;
    private HBTextView mHeading, mDetail, mSubheading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoarder_warning);

        mHeading = findViewById(R.id.hoarder_alert_heading);
        mDetail = findViewById(R.id.hoarder_alert_detail);
        mSubheading = findViewById(R.id.hoarder_alert_subheading);

        final HBButton hideButton = findViewById(R.id.hoarder_alert_button);

        mCassette = getIntent().getParcelableExtra(getString(R.string.cassette_extra));
        final boolean hoarded = getIntent().getBooleanExtra("hoarded_extra", false);
        final int count = getIntent().getIntExtra("count_extra", 1);
        final int days = getIntent().getIntExtra("days_extra", 0);

        mCassette.setCassetteModel(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                if (hoarded) {

                    hideButton.setBackgroundColor(ContextCompat.getColor(HoarderWarningActivity.this, R.color.hbOrange));

                    if (count > 1) {
                        // 1 cassette hoarded
                        mHeading.setText("You didn't heed our warning...");
                        mDetail.setText("#" + mCassette.getNumber() + " " + mCassette.getCassetteModel().getName() +
                                " has been in your possession for " + String.valueOf(days) +
                                " days now. Not good.\n\n You are now a Hoarder. Yeah, it earned you a shiny new badge, but soon this will be your only badge.");
                        mSubheading.setText("Hide the cassette soon and everything will return to normal.");
                    } else {
                        // Multiple cassettes hoarded
                        mHeading.setText("You didn't heed our warning...");
                        mDetail.setText("You have " + String.valueOf(count) +
                                " Bait Cassettes that have been in your possession for 7 or more days now." +
                                " Not good.\n\n You are now a Hoarder. Yeah, it earned you a shiny new badge, but soon this will be your only badge.");
                        mSubheading.setText("Hide the cassette soon and everything will return to normal.");
                    }
                } else {
                    if (count > 1) {
                        // 1 cassette warning
                        mHeading.setText("You really got to let it go...");
                        mDetail.setText("#" + mCassette.getNumber() + " " + mCassette.getCassetteModel().getName() +
                                " has been in your possession for " + String.valueOf(days) +
                                " days now.\n\nYou really should consider re-hiding this cassette for others to find. Nothing bad is going to happen... yet.");
                        mSubheading.setText("Hide the cassette soon and we can forget about the whole thing.");
                    } else {
                        // Multiple cassettes warning
                        mHeading.setText("You really got to let them go...");
                        mDetail.setText("You have " + String.valueOf(count) +
                                " Bait Cassettes that have been in your possession for 3 or more days now." +
                                " Not good.\n\n You are now a Hoarder. Yeah, it earned you a shiny new badge, but soon this will be your only badge.");
                        mSubheading.setText("Hide the cassette soon and we can forget about the whole thing.");
                    }
                }
            }

            @Override
            public void onFail(String error) {
                Log.w("HB", error);
            }
        });

    }

    public void hideTapped(View v) {
        Intent hideIntent = new Intent(HoarderWarningActivity.this, BaitSelectActivity.class);
        hideIntent.putExtra(getString(R.string.cassette_extra_key), mCassette.getKey());
        startActivity(hideIntent);
    }

    public void closeTapped(View v) {
        finish();
    }
}
