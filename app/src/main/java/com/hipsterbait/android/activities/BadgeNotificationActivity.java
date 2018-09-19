package com.hipsterbait.android.activities;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Badge;
import com.hipsterbait.android.models.DataCallback;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.NotificationItem;
import com.hipsterbait.android.models.Rank;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.OnSwipeTouchListener;
import com.hipsterbait.android.other.RanksStore;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;

public class BadgeNotificationActivity extends ImmersiveActivity {

    public HBTextView header, title, detail;
    public ImageView imageView, backImageView, selectRight, selectLeft;

    private ArrayList<NotificationItem> mArrayList;
    private int mSelectedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_notification);

        header = (HBTextView) findViewById(R.id.badge_notif_header);
        title = (HBTextView) findViewById(R.id.badge_notif_name);
        detail = (HBTextView) findViewById(R.id.badge_notif_detail);

        imageView = (ImageView) findViewById(R.id.badge_notif_image);
        backImageView = (ImageView) findViewById(R.id.badge_notif_image_back);
        selectRight = (ImageView) findViewById(R.id.badge_notif_arrow_right);
        selectLeft = (ImageView) findViewById(R.id.badge_notif_arrow_left);

        selectLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousItem();
            }
        });

        selectRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextItem();
            }
        });

        imageView.setOnTouchListener(new OnSwipeTouchListener(BadgeNotificationActivity.this) {
            public void onSwipeLeft() {
                nextItem();
            }
            public void onSwipeRight() {
                previousItem();
            }
        });

        mArrayList = getIntent().getExtras().getParcelableArrayList(getString(R.string.arraylist_extra));
        mSelectedIndex = 0;

        if (mArrayList.size() > 1) {
            header.setText("You just earned " + String.valueOf(mArrayList.size()) + " NEW BADGES!");
        } else {
            header.setText("You just earned 1 NEW BADGE!");
            selectRight.setVisibility(View.INVISIBLE);
        }

        setNotificationDetails();
    }

    public void setNotificationDetails() {

        if (mSelectedIndex < mArrayList.size() - 1) {
            selectRight.setVisibility(View.VISIBLE);
        } else {
            selectRight.setVisibility(View.INVISIBLE);
        }

        if (mSelectedIndex > 0) {
            selectLeft.setVisibility(View.VISIBLE);
        } else {
            selectLeft.setVisibility(View.INVISIBLE);
        }

        NotificationItem item = mArrayList.get(mSelectedIndex);

        if (item.mRank) {
            try {
                final Rank rank = RanksStore.getInstance().getRank(item.mKey);

                title.setText(rank.getName());
                detail.setText(rank.getFoundText());

                rank.setImage(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        Glide.with(BadgeNotificationActivity.this)
                                .load(rank.getImageUrl())
                                .into(imageView);
                    }

                    @Override
                    public void onFail(String error) {
                        Log.w("HB", error);
                    }
                });
            } catch (Exception error) {
                Toast.makeText(this, "Error: rank not found", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            try {
                final Badge badge = BadgesStore.getInstance().getBadge(item.mKey);

                title.setText(badge.getName());
                detail.setText(badge.getFoundText());

                badge.setImage(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        Glide.with(BadgeNotificationActivity.this)
                                .load(badge.getImageUrl())
                                .into(imageView);
                    }

                    @Override
                    public void onFail(String error) {
                        Log.w("HB", error);
                    }
                });
            } catch (Exception error) {
                Toast.makeText(this, "Error: rank not found", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (item.mRank) {
            header.setText("You just LEVELLED UP!");
        } else {
            if (mArrayList.size() > 1) {
                header.setText("You just earned " + String.valueOf(mArrayList.size()) + " NEW BADGES!");
            } else {
                header.setText("You just earned 1 NEW BADGE!");
                selectRight.setVisibility(View.INVISIBLE);
            }
        }

        AnimatorSet frontSet = (AnimatorSet) AnimatorInflater.loadAnimator(BadgeNotificationActivity.this, R.animator.front_badge_flip);
        frontSet.setTarget(imageView);
        frontSet.start();
        AnimatorSet backSet = (AnimatorSet) AnimatorInflater.loadAnimator(BadgeNotificationActivity.this, R.animator.back_badge_flip);
        backSet.setTarget(backImageView);
        backSet.start();
    }

    public void nextItem() {
        if (mSelectedIndex + 1 >= mArrayList.size()) {
            return;
        }

        mSelectedIndex += 1;

        setNotificationDetails();
    }

    public void previousItem() {
        if (mSelectedIndex - 1 < 0) {
            return;
        }

        mSelectedIndex -= 1;

        setNotificationDetails();
    }

    public void continueButtonTapped(View v) {
        finish();
    }
}

