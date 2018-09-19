package com.hipsterbait.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.ImageView;

import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserPoints;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.PointsEarningsManager;
import com.hipsterbait.android.other.RotatingTexts;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;

public class BaitDoneActivity extends ImmersiveActivity {

    public HBTextView baitNumber;
    public ImageView baitPick;

    private User mUser;
    private Cassette mCassette;
    private Journey mJourney;
    private boolean fbShare = false, twShare = false, igShare = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bait_done);

        mUser = ((HBApplication) getApplication()).user;

        baitNumber = (HBTextView) findViewById(R.id.bait_done_bait_number);
        baitPick = (ImageView) findViewById(R.id.bait_done_bait_pick);

        String cassetteKey = getIntent().getStringExtra(getString(R.string.cassette_extra_key));
        mCassette = mUser.getCassetteByKey(cassetteKey);
        mJourney = getIntent().getParcelableExtra(getString(R.string.journey_extra));

        fbShare = getIntent().getBooleanExtra(getString(R.string.fb_share_extra), false);
        twShare = getIntent().getBooleanExtra(getString(R.string.tw_share_extra), false);
        igShare = getIntent().getBooleanExtra(getString(R.string.ig_share_extra), false);

        int unbaitedCassetteCount = mUser.getUnhiddenCassettes().size();

        if (unbaitedCassetteCount > 0) {
            baitPick.setVisibility(View.VISIBLE);
            baitNumber.setVisibility(View.VISIBLE);
            baitNumber.setText(String.valueOf(unbaitedCassetteCount));
        }

        int oldPoints = mUser.getPoints();

        ArrayList<UserPoints> result = PointsEarningsManager.awardPointsForLeavingHint(
                        this, mUser, mCassette, mJourney, fbShare, twShare, igShare);

        int newPoints = 0;
        for (UserPoints userPoints : result) {
            newPoints += userPoints.getValue();
        }

        Bundle args = new Bundle();
        args.putString(getString(R.string.title_string_arg), RotatingTexts.getString(RotatingTexts.HINT_SHARE));
        args.putParcelableArrayList(getString(R.string.userpoints_arraylist_arg), result);
        args.putInt(getString(R.string.old_points), oldPoints);
        args.putInt(getString(R.string.new_points), newPoints);
        BottomSheetDialogFragment pointsFrag = new PointsReceiptFragment();
        pointsFrag.setArguments(args);
        pointsFrag.show(getSupportFragmentManager(), pointsFrag.getTag());
    }

    public void baitTapped(View v) {
        Intent intent = new Intent(BaitDoneActivity.this, BaitSelectActivity.class);
        startActivity(intent);
        finish();
    }

    public void findTapped(View v) {
        Intent intent = new Intent(BaitDoneActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void closeButtonTapped(View v) {
        finish();
    }
}
