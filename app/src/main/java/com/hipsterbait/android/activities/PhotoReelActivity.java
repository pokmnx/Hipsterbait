package com.hipsterbait.android.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Hint;
import com.hipsterbait.android.other.OnSwipeTouchListener;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;

public class PhotoReelActivity extends ImmersiveActivity {

    public ImageView imageView;
    public RelativeLayout container;
    public HBTextView titleLabel, userLabel;

    private boolean mSliding = false;
    private ArrayList<Hint> mHints;
    private Hint mSelectedHint;
    private int mSelectedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_reel);

        imageView = (ImageView) findViewById(R.id.photo_reel_image_view);
        container = (RelativeLayout) findViewById(R.id.photo_reel_art_container);
        titleLabel = (HBTextView) findViewById(R.id.photo_reel_title);
        userLabel = (HBTextView) findViewById(R.id.photo_reel_user);

        mHints = getIntent().getParcelableArrayListExtra(getString(R.string.hints_extra));

        mSelectedIndex = getIntent().getIntExtra(getString(R.string.index_extra), 0);
        mSelectedHint = mHints.get(mSelectedIndex);

        showImageAtIndex(mSelectedIndex);

        imageView.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeLeft() {
                leftArrowTapped(imageView);
            }

            public void onSwipeRight() {
                rightArrowTapped(imageView);
            }
        });
    }

    public void leftArrowTapped(View v) {
        showImageAtIndex(mSelectedIndex + 1);
    }

    public void rightArrowTapped(View v) {
        showImageAtIndex(mSelectedIndex - 1);
    }

    public void setDetails() {
        titleLabel.setText("Title: " + mSelectedHint.getTitle());
        userLabel.setText("By: " + mSelectedHint.getFoundBy());
    }

    public void showImageAtIndex(int index) {
        if (mSliding) return;
        mSliding = true;

        boolean next = true;

        if (index < mSelectedIndex) {
            next = false;
        }

        if (index > mHints.size() - 1) {
            index = 0;
        }

        if (index < 0) {
            index = mHints.size() - 1;
        }

        final ImageView previousCassetteArt = new ImageView(this);
        RelativeLayout.LayoutParams previousLayoutParams =
                new RelativeLayout.LayoutParams(imageView.getWidth(), imageView.getHeight());
        previousCassetteArt.setLayoutParams(previousLayoutParams);
        container.addView(previousCassetteArt);
        Glide.with(this)
                .load(mSelectedHint.getHintImageURL())
                .into(previousCassetteArt);

        mSelectedIndex = index;
        mSelectedHint = mHints.get(mSelectedIndex);

        setDetails();

        imageView.setVisibility(View.INVISIBLE);

        final ImageView newCassetteArt = new ImageView(this);
        RelativeLayout.LayoutParams nextLayoutParams =
                new RelativeLayout.LayoutParams(container.getWidth(), container.getHeight());
        newCassetteArt.setLayoutParams(nextLayoutParams);
        container.addView(newCassetteArt);
        Glide.with(this)
                .load(mSelectedHint.getHintImageURL())
                .into(newCassetteArt);

        Glide.with(this)
                .load(mSelectedHint.getHintImageURL())
                .into(imageView);

        if (next) {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    container.removeView(previousCassetteArt);
                    container.removeView(newCassetteArt);
                    imageView.setVisibility(View.VISIBLE);
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
                    container.removeView(previousCassetteArt);
                    container.removeView(newCassetteArt);
                    imageView.setVisibility(View.VISIBLE);
                    mSliding = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            previousCassetteArt.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
            newCassetteArt.startAnimation(slideIn);
        }
    }
}
