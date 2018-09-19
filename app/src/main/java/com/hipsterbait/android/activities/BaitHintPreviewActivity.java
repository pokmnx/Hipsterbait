package com.hipsterbait.android.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.BadgeNotFound;
import com.hipsterbait.android.Resources.Exceptions.RankNotFound;
import com.hipsterbait.android.models.Badge;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Hint;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.NotificationItem;
import com.hipsterbait.android.models.Rank;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserBadges;
import com.hipsterbait.android.models.UserPoints;
import com.hipsterbait.android.other.BadgesAwardManager;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.PointsEarningsManager;
import com.hipsterbait.android.other.RanksStore;
import com.hipsterbait.android.other.RotatingTexts;
import com.hipsterbait.android.widgets.HBTextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class BaitHintPreviewActivity extends ImmersiveActivity {

    public HBTextView congratsLabel, hintTitle, hintDescription, hintHashtags, hintDate, hintPlayerName;
    public ImageView hintImage, hintAvatar;

    private Cassette mCassette;
    private Journey mJourney;
    private User mUser;
    private Hint mHint;
    private Uri mPhotoUri;
    private DatabaseReference mDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bait_hint_preview);

        mUser = ((HBApplication) getApplication()).user;
        mDbRef = FirebaseDatabase.getInstance().getReference();

        congratsLabel = (HBTextView) findViewById(R.id.bait_preview_title);
        hintTitle = (HBTextView) findViewById(R.id.bait_preview_hint_title);
        hintDescription = (HBTextView) findViewById(R.id.bait_preview_hint_description);
        hintHashtags = (HBTextView) findViewById(R.id.bait_preview_hint_hashtags);
        hintDate = (HBTextView) findViewById(R.id.bait_preview_hint_date);
        hintPlayerName = (HBTextView) findViewById(R.id.bait_preview_hint_player_name);
        hintImage = (ImageView) findViewById(R.id.bait_preview_image);
        hintAvatar = (ImageView) findViewById(R.id.bait_preview_hint_avatar_image);

        congratsLabel.setText(RotatingTexts.getString(RotatingTexts.HINT_PREVIEW));

        String cassetteKey = getIntent().getStringExtra(getString(R.string.cassette_extra_key));
        mCassette = mUser.getCassetteByKey(cassetteKey);
        mJourney = getIntent().getParcelableExtra(getString(R.string.journey_extra));
        mHint = getIntent().getParcelableExtra(getString(R.string.hint_extra));
        String photoExtra = getIntent().getStringExtra(getString(R.string.image_extra));
        if (photoExtra != null) {
            mPhotoUri = Uri.parse(photoExtra);
        }

        if (mPhotoUri != null) {
            hintImage.setImageURI(mPhotoUri);
            hintImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BaitHintPreviewActivity.this, PhotoFullscreenActivity.class);
                    intent.putExtra(getString(R.string.image_extra), mPhotoUri.toString());
                    startActivity(intent);
                }
            });
        }

        String description = mHint.getDescription();
        if (description != null) {
            hintDescription.setText(mHint.getDescription());
        } else {
            hintDescription.setTag("No comment.");
        }

        hintDate.setText(mHint.dateFormattedVerbose());

        Bitmap avatar = mUser.getAvatarImage();

        if (avatar != null) {
            hintAvatar.setImageBitmap(avatar);
        } else {
            hintAvatar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar_generic));
        }

        String rankText = "";

        try {
            Rank rank = RanksStore.getInstance().getRank(mUser.getCurrentRank().getRank());
            rankText = rank.getName();
        } catch (RankNotFound e) {
            Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        hintPlayerName.setText(mUser.getUsername() + " " + rankText);
    }

    public void editTapped(View v) {
        finish();
    }

    public void finishTapped(View v) {
        ArrayList<UserBadges> newBadges = BadgesAwardManager.awardBadgesForLeavingHint(this.getApplicationContext(), mHint, mUser, mCassette, mJourney);
        ArrayList<NotificationItem> items = new ArrayList<>();

        for (UserBadges userBadge : newBadges) {
            try {
                Badge badge = BadgesStore.getInstance().getBadge(userBadge.getBadge());
                if (mUser.getNotifications().containsKey(badge.getKey()) == false) {
                    items.add(new NotificationItem(badge.getKey(), false));
                    mUser.setNotification(badge.getKey());
                }
            } catch (BadgeNotFound e) {
                Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
            }
        }

        mUser.save();

        int oldPoints = mUser.getPoints();

        ArrayList<UserPoints> result = PointsEarningsManager.awardPointsForLeavingHint(this.getApplicationContext(), mHint, mUser, mCassette, mJourney);

        setResult(RESULT_OK);

        Intent intent = new Intent(BaitHintPreviewActivity.this, BaitShareActivity.class);
        intent.putExtra(getString(R.string.old_points), oldPoints);
        intent.putExtra(getString(R.string.cassette_extra_key), mCassette.getKey());
        intent.putExtra(getString(R.string.journey_extra), mJourney);
        if (mPhotoUri != null) {
            intent.putExtra(getString(R.string.image_extra), mPhotoUri.toString());
        }
        intent.putExtra(getString(R.string.hint_extra), mHint);
        intent.putExtra(getString(R.string.userpoints_arraylist_arg), result);
        startActivity(intent);

        if (items.size() > 0) {
            Intent badgeIntent = new Intent(this, BadgeNotificationActivity.class);
            badgeIntent.putParcelableArrayListExtra(getString(R.string.arraylist_extra), items);
            startActivity(badgeIntent);
        }

        finish();
    }

    public void closeButtonTapped(View v) {
        Intent intent = new Intent(BaitHintPreviewActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
