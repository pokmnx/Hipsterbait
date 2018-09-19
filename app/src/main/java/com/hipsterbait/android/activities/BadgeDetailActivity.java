package com.hipsterbait.android.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Badge;
import com.hipsterbait.android.models.DataCallback;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.Rank;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.RanksStore;
import com.hipsterbait.android.widgets.HBTextView;

public class BadgeDetailActivity extends ImmersiveActivity {

    public RelativeLayout layout;
    public ImageView imageView;
    public HBTextView name, detail, timestamp;

    private boolean expanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_detail);
        layout = (RelativeLayout) findViewById(R.id.activity_badge_detail);

        imageView = (ImageView) findViewById(R.id.badge_details_image);
        name = (HBTextView) findViewById(R.id.badge_details_name);
        detail = (HBTextView) findViewById(R.id.badge_details_detail);
        timestamp = (HBTextView) findViewById(R.id.badge_details_timestamp);

        String key = getIntent().getStringExtra("badge_details_key");
        Boolean isRank = getIntent().getBooleanExtra("badge_details_rank", false);

        if (key.equals("locked_badge")) {
            int imageId = getResources().getIdentifier(
                    getIntent().getStringExtra(getString(R.string.locked_badge_bg)),
                    "drawable", getPackageName());
            imageView.setImageDrawable(ContextCompat.getDrawable(this, imageId));
        } else if (isRank) {
            try {
                final Rank rank = RanksStore.getInstance().getRank(key);

                rank.setImage(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        Glide.with(BadgeDetailActivity.this)
                                .load(rank.getImageUrl())
                                .into(imageView);
                    }

                    @Override
                    public void onFail(String error) {

                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, "Rank not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            try {
                final Badge badge = BadgesStore.getInstance().getBadge(key);

                badge.setImage(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        Glide.with(BadgeDetailActivity.this)
                                .load(badge.getImageUrl())
                                .into(imageView);
                    }

                    @Override
                    public void onFail(String error) {

                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, "Badge not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                Animation anim = new ExpandAnimation(expanded);
                anim.setDuration(300);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (expanded == false) {
                            name.setVisibility(View.VISIBLE);
                            detail.setVisibility(View.VISIBLE);
                            timestamp.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });

                layout.startAnimation(anim);

                if (expanded) {
                    name.setVisibility(View.INVISIBLE);
                    detail.setVisibility(View.INVISIBLE);
                    timestamp.setVisibility(View.INVISIBLE);
                }
            }
        });

        name.setText(getIntent().getStringExtra(getString(R.string.badge_details_name)));
        detail.setText(getIntent().getStringExtra(getString(R.string.badge_details_detail)));
        timestamp.setText(getIntent().getStringExtra(getString(R.string.badge_details_timestamp)));
    }

    private class ExpandAnimation extends Animation {

        float initialWidth, initialHeight;
        float widthDelta, heightDelta;

        public ExpandAnimation(boolean expand) {

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            initialWidth = params.width;
            initialHeight = params.height;

            float expandedDimen = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
            float unexpandedDimen = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 158, getResources().getDisplayMetrics());

            if (expand) {
                widthDelta = expandedDimen - initialWidth;
                heightDelta = expandedDimen - initialHeight;
            } else {
                widthDelta = unexpandedDimen - initialWidth;
                heightDelta = unexpandedDimen - initialHeight;
            }
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            params.width = (int) (initialWidth + (interpolatedTime * widthDelta));
            params.height = (int) (initialHeight + (interpolatedTime * heightDelta));
            imageView.setLayoutParams(params);
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
