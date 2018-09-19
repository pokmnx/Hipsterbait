package com.hipsterbait.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.View;

import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserPoints;
import com.hipsterbait.android.other.HBApplication;

import java.util.ArrayList;

public class BaitEarningsActivity extends ImmersiveActivity {

    private ArrayList<UserPoints> mResult;
    private User mUser;
    private Cassette mCassette;
    private Journey mJourney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bait_earnings);

        mUser = ((HBApplication) getApplication()).user;

        mResult = getIntent().getParcelableArrayListExtra(getString(R.string.userpoints_arraylist_arg));
        String cassetteKey = getIntent().getStringExtra(getString(R.string.cassette_extra_key));
        mCassette = mUser.getCassetteByKey(cassetteKey);
        mJourney = getIntent().getParcelableExtra(getString(R.string.journey_extra));
        int points = getIntent().getIntExtra(getString(R.string.old_points), 0);
        Bundle args = new Bundle();

        int newPoints = 0;
        for (UserPoints userPoints : mResult) {
            newPoints += userPoints.getValue();
        }

        args.putString(getString(R.string.title_string_arg), "Your trap is set");
        args.putParcelableArrayList(getString(R.string.userpoints_arraylist_arg), mResult);
        args.putInt(getString(R.string.old_points), points);
        args.putInt(getString(R.string.new_points), newPoints);
        BottomSheetDialogFragment pointsFrag = new PointsReceiptFragment();
        pointsFrag.setArguments(args);
        pointsFrag.show(getSupportFragmentManager(), pointsFrag.getTag());
    }

    public void leaveHintTapped(View v) {
        Intent intent = new Intent(BaitEarningsActivity.this, BaitHintActivity.class);
        intent.putExtra(getString(R.string.journey_extra), mJourney);
        intent.putExtra(getString(R.string.cassette_extra_key), mCassette.getKey());
        startActivity(intent);
        finish();
    }

    public void closeTapped(View v) {
        Intent intent = new Intent(BaitEarningsActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
