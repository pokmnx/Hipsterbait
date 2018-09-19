package com.hipsterbait.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.OnSwipeTouchListener;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;
import java.util.Date;

public class BaitSelectActivity extends ImmersiveActivity {

    public RelativeLayout artContainer;
    public ImageButton leftArrow, rightArrow;
    public ImageView coverImage, hoarderPick;
    public HBTextView cassetteName;

    private boolean mSliding;
    private User mUser;
    private ArrayList<Cassette> mCassettes;
    private int mSelectedIndex = -1;
    private Cassette mSelectedCassette;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bait_select);

        mUser = ((HBApplication) getApplication()).user;

        leftArrow = (ImageButton) findViewById(R.id.bait_select_left_button);
        rightArrow = (ImageButton) findViewById(R.id.bait_select_right_button);
        coverImage = (ImageView) findViewById(R.id.bait_select_cover);
        hoarderPick = (ImageView) findViewById(R.id.bait_select_hoarder_pick);
        cassetteName = (HBTextView) findViewById(R.id.bait_select_cassette_name);
        artContainer = (RelativeLayout) findViewById(R.id.bait_select_art_container);

        artContainer.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeLeft() {
                nextCassette(artContainer);
            }

            public void onSwipeRight() {
                prevCassette(artContainer);
            }
        });

        mCassettes = mUser.getUnhiddenCassettes();

        String cassetteKey = getIntent().getStringExtra(getString(R.string.cassette_extra_key));
        if (cassetteKey != null) {
            int index = 0;
            int foundIndex = -1;

            for (Cassette cassette : mCassettes) {
                if (cassette.getKey().equals(cassetteKey)) {
                    foundIndex = index;
                    break;
                }
                index += 1;
            }

            if (foundIndex >= 0) {
                mSelectedIndex = foundIndex;
                mSelectedCassette = mCassettes.get(mSelectedIndex);
            }
        }

        if (mCassettes.size() < 1) {
            finish();
            return;
        }

        if (mSelectedIndex < 0) {
            mSelectedIndex = 0;

            mSelectedCassette = mCassettes.get(0);
        }

        Glide.with(this)
                .load(mSelectedCassette.getCassetteModel().getCassetteArtURL())
                .into(coverImage);

        setCassetteDetails();
    }

    public void setCassetteDetails() {
        cassetteName.setText("#" + mSelectedCassette.getNumber() + " " + mSelectedCassette.getCassetteModel().getName());

        Glide.with(this)
                .load(mSelectedCassette.getCassetteModel().getCassetteArtURL())
                .into(coverImage);

        if (mSelectedIndex == 0) {
            leftArrow.setVisibility(View.INVISIBLE);
        } else {
            leftArrow.setVisibility(View.VISIBLE);
        }

        if (mSelectedIndex == mCassettes.size() - 1) {
            rightArrow.setVisibility(View.INVISIBLE);
        } else {
            rightArrow.setVisibility(View.VISIBLE);
        }

        long dateFound = mUser.getDateFoundByCassetteKey(mSelectedCassette.getKey());
        long diff = Math.abs(dateFound - new Date().getTime());
        if (diff > 7 * 24 * 60 * 60 * 1000) {
            hoarderPick.setVisibility(View.VISIBLE);
        } else {
            hoarderPick.setVisibility(View.INVISIBLE);
        }
    }

    public void selectCassette(int index) {

        if (mSliding) return;
        mSliding = true;

        if (index > mCassettes.size() - 1 || index < 0) {
            mSliding = false;
            return;
        }

        boolean next = true;

        if (index < mSelectedIndex) {
            next = false;
        }

        final ImageView previousCassetteArt = new ImageView(this);
        RelativeLayout.LayoutParams previousLayoutParams =
                new RelativeLayout.LayoutParams(artContainer.getWidth(), artContainer.getHeight());
        previousCassetteArt.setLayoutParams(previousLayoutParams);
        artContainer.addView(previousCassetteArt);
        Glide.with(this)
                .load(mSelectedCassette.getCassetteModel().getCassetteArtURL())
                .into(previousCassetteArt);

        mSelectedIndex = index;
        mSelectedCassette = mCassettes.get(mSelectedIndex);

        setCassetteDetails();

        coverImage.setVisibility(View.INVISIBLE);

        final ImageView newCassetteArt = new ImageView(this);
        RelativeLayout.LayoutParams nextLayoutParams =
                new RelativeLayout.LayoutParams(artContainer.getWidth(), artContainer.getHeight());
        newCassetteArt.setLayoutParams(nextLayoutParams);
        artContainer.addView(newCassetteArt);
        Glide.with(this)
                .load(mSelectedCassette.getCassetteModel().getCassetteArtURL())
                .into(newCassetteArt);

        Glide.with(this)
                .load(mSelectedCassette.getCassetteModel().getCassetteArtURL())
                .into(coverImage);

        if (next) {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    artContainer.removeView(previousCassetteArt);
                    artContainer.removeView(newCassetteArt);
                    coverImage.setVisibility(View.VISIBLE);
                    mSliding = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            previousCassetteArt.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
            newCassetteArt.startAnimation(slideIn);
        } else {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    artContainer.removeView(previousCassetteArt);
                    artContainer.removeView(newCassetteArt);
                    coverImage.setVisibility(View.VISIBLE);
                    mSliding = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            previousCassetteArt.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
            newCassetteArt.startAnimation(slideIn);
        }
    }

    public void nextCassette(View v) {
        selectCassette(mSelectedIndex + 1);
    }

    public void prevCassette(View v) {
        selectCassette(mSelectedIndex - 1);
    }

    public void hideTapped(View v) {
        Intent intent = new Intent(BaitSelectActivity.this, BaitLureHipstersActivity.class);
        intent.putExtra(getString(R.string.cassette_extra_key), mSelectedCassette.getKey());
        startActivity(intent);
        finish();
    }

    public void closeTapped(View v) {
        finish();
    }

    private class CassetteAnimation extends Animation {

        private boolean mNext;
        private ImageView mPreviousCassetteArt, mNextCassetteArt;

        private CassetteAnimation(boolean next, ImageView previousCassetteArt, ImageView nextCassetteArt) {
            mNext = next;
            mPreviousCassetteArt = previousCassetteArt;
            mNextCassetteArt = nextCassetteArt;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            if (mNext) {
                ((RelativeLayout.LayoutParams) mPreviousCassetteArt.getLayoutParams())
                        .setMarginEnd((int) (artContainer.getWidth() * interpolatedTime));
                ((RelativeLayout.LayoutParams) mNextCassetteArt.getLayoutParams())
                        .setMarginEnd((int) (artContainer.getWidth() * (1 - interpolatedTime)));
            } else {
                ((RelativeLayout.LayoutParams) mPreviousCassetteArt.getLayoutParams())
                        .setMarginStart((int) (artContainer.getWidth() * interpolatedTime));
                ((RelativeLayout.LayoutParams) mNextCassetteArt.getLayoutParams())
                        .setMarginStart((int) (artContainer.getWidth() * (1 - interpolatedTime)));
            }

            artContainer.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
