package com.hipsterbait.android.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.BadgeNotFound;
import com.hipsterbait.android.Resources.Exceptions.RankNotFound;
import com.hipsterbait.android.models.Badge;
import com.hipsterbait.android.models.DataCallback;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.Rank;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserBadges;
import com.hipsterbait.android.models.UserRank;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.OnSwipeTouchListener;
import com.hipsterbait.android.other.RanksStore;
import com.hipsterbait.android.widgets.HBTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAccountActivity extends ImmersiveActivity {

    public ImageView avatarImage, badge1Image, badge2Image, rankImage, baitPick, hoarderPick, flagPick;
    public HBTextView displayName, rankLabel, progressLabel, baitNumberLabel, hoarderNumberLabel, flagNumberLabel;
    public View progressContainer, progressBackground, progressBar;

    private User user;
    private Badge badge1, badge2;
    private long badge1Timestamp, badge2Timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        avatarImage = (ImageView) findViewById(R.id.user_account_avatar_image);
        badge1Image = (ImageView) findViewById(R.id.user_account_badge_1_image);
        badge2Image = (ImageView) findViewById(R.id.user_account_badge_2_image);
        rankImage = (ImageView) findViewById(R.id.user_account_rank_image);
        displayName = (HBTextView) findViewById(R.id.user_account_display_name_label);
        rankLabel = (HBTextView) findViewById(R.id.user_account_rank_label);
        progressLabel = (HBTextView) findViewById(R.id.user_account_progress_label);
        progressContainer = findViewById(R.id.user_account_progress_container);
        progressBackground = findViewById(R.id.user_account_progress_background);
        progressBar = findViewById(R.id.user_account_progress);
        baitNumberLabel = (HBTextView) findViewById(R.id.user_account_bait_number);
        hoarderNumberLabel = (HBTextView) findViewById(R.id.user_account_hoarder_number);
        flagNumberLabel = (HBTextView) findViewById(R.id.user_account_flag_number);
        baitPick = (ImageView) findViewById(R.id.user_account_bait_pick);
        hoarderPick = (ImageView) findViewById(R.id.user_account_hoarder_pick);
        flagPick = (ImageView) findViewById(R.id.user_account_flag_pick);
    }

    @Override
    protected void onResume() {
        super.onResume();

        user = ((HBApplication) getApplication()).user;

        if (user == null) {
            finish();
            return;
        }

        Bitmap avatar = user.getAvatarImage();
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), avatar);
        drawable.setCornerRadius(avatarImage.getHeight() / 2);

        long longestHoarded = user.getLongestHoarded() - (7 * 24 * 60 * 60 * 1000);
        int hoardedBadges = ((int) longestHoarded / (24 * 60 * 60 * 1000)) * 10;
        boolean badge1Hoarder = hoardedBadges > 1;
        boolean badge2Hoarder = hoardedBadges > 10;
        boolean rankHoarder = hoardedBadges > BadgesStore.getInstance().getAllBadges().size();

        if (hoardedBadges > 1) {
            hoarderPick.setVisibility(View.VISIBLE);
            hoarderNumberLabel.setText(String.valueOf(user.getHoardedCassettes().size()));
            hoarderNumberLabel.setVisibility(View.VISIBLE);
        }

        displayName.setText(user.getUsername());

        if (rankHoarder) {
            avatarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.hoarder));
        } else {
            if (avatar != null) {
                avatarImage.setImageDrawable(drawable);
            } else {
                avatarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar_generic));
            }
        }

        final UserRank userRank = user.getCurrentRank();
        DecimalFormat formatter = new DecimalFormat("#,###,###");

        if (userRank != null) {
            try {
                final Rank currentRank = RanksStore.getInstance().getRank(userRank.getRank());
                currentRank.setImage(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        Glide.with(UserAccountActivity.this)
                                .load(currentRank.getImageUrl())
                                .into(rankImage);

                        rankImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(UserAccountActivity.this, BadgeDetailActivity.class);
                                intent.putExtra("badge_details_rank", true);
                                intent.putExtra(
                                        "badge_details_key",
                                        currentRank.getKey());
                                intent.putExtra(
                                        getString(R.string.badge_details_name),
                                        currentRank.getName());
                                intent.putExtra(
                                        getString(R.string.badge_details_detail),
                                        currentRank.getFoundText());
                                intent.putExtra(
                                        getString(R.string.badge_details_timestamp),
                                        "Unlocked on " + userRank.getDateString());

                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onFail(String error) {
                        Log.w("HB", error);
                    }
                });
                String rankLabelString = currentRank.getLabel();
                progressLabel.setText(rankLabelString + " - " + formatter.format(user.getPoints()) + " of " + formatter.format(currentRank.getNextLevelPoints()));
                rankLabel.setText(currentRank.getName());

            } catch (Exception e) {
                Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
            }
        }

        Map<String, UserBadges> userBadges = user.getUserBadges();

        if (userBadges.size() > 0) {
            List<UserBadges> badgeValues = new ArrayList<UserBadges>(userBadges.values());
            Collections.sort(badgeValues,
                    new Comparator<UserBadges>() {
                        @Override
                        public int compare(UserBadges o1, UserBadges o2) {
                            return ((Number) (o2.getTimestamp() - o1.getTimestamp())).intValue();
                        }
                    });
            final UserBadges mostRecentBadge = badgeValues.get(0);

            try {
                if (badge1Hoarder) {
                    badge1 = BadgesStore.getInstance().getBadge("hoarder");
                } else {
                    badge1 = BadgesStore.getInstance().getBadge(mostRecentBadge.getBadge());
                }
                badge1Timestamp = mostRecentBadge.getTimestamp();
                badge1.setImage(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        Glide.with(UserAccountActivity.this)
                                .load(badge1.getImageUrl())
                                .into(badge1Image);

                        badge1Image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(UserAccountActivity.this, BadgeDetailActivity.class);
                                intent.putExtra("badge_details_rank", false);
                                intent.putExtra(
                                        "badge_details_key",
                                        badge1.getKey());
                                intent.putExtra(
                                        getString(R.string.badge_details_name),
                                        badge1.getName());
                                intent.putExtra(
                                        getString(R.string.badge_details_detail),
                                        badge1.getFoundText());
                                intent.putExtra(
                                        getString(R.string.badge_details_timestamp),
                                        "Unlocked on " + mostRecentBadge.getDateString());

                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onFail(String error) {
                        Log.w("HB", error);
                    }
                });
            } catch (BadgeNotFound e) {
                Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
            }

            if (userBadges.size() > 1) {
                final UserBadges recentBadge = badgeValues.get(1);

                try {
                    if (badge2Hoarder) {
                        badge2 = BadgesStore.getInstance().getBadge("hoarder");
                    } else {
                        badge2 = BadgesStore.getInstance().getBadge(recentBadge.getBadge());
                    }
                    badge2Timestamp = recentBadge.getTimestamp();
                    badge2.setImage(new ModelPropertySetCallback() {
                        @Override
                        public void onSuccess() {
                            Glide.with(UserAccountActivity.this)
                                    .load(badge2.getImageUrl())
                                    .into(badge2Image);

                            badge2Image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(UserAccountActivity.this, BadgeDetailActivity.class);
                                    intent.putExtra("badge_details_rank", false);
                                    intent.putExtra(
                                            "badge_details_key",
                                            badge2.getKey());
                                    intent.putExtra(
                                            getString(R.string.badge_details_name),
                                            badge2.getName());
                                    intent.putExtra(
                                            getString(R.string.badge_details_detail),
                                            badge2.getFoundText());
                                    intent.putExtra(
                                            getString(R.string.badge_details_timestamp),
                                            "Unlocked on " + recentBadge.getDateString());

                                    startActivity(intent);
                                }
                            });
                        }

                        @Override
                        public void onFail(String error) {
                            Log.w("HB", error);
                        }
                    });


                } catch (BadgeNotFound e) {
                    Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                }
            }
        }

        int points = user.getPoints() % 1000;
        float progress = ((float) points) / 1000;
        ViewGroup.LayoutParams params = progressBar.getLayoutParams();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        params.width = Math.round(progress * ((float) size.x - 32));
        progressBar.setLayoutParams(params);

        int unbaitedCassetteCount = user.getUnhiddenCassettes().size();

        if (unbaitedCassetteCount > 0) {
            baitPick.setVisibility(View.VISIBLE);
            baitNumberLabel.setVisibility(View.VISIBLE);
            baitNumberLabel.setText(String.valueOf(unbaitedCassetteCount));
        }
    }

    public void logoTapped(View v) {
        Intent intent = new Intent(UserAccountActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void burgerTapped(View v) {
        Intent intent = new Intent(UserAccountActivity.this, LoggedMenuActivity.class);
        startActivity(intent);
    }

    public void accountToggleTapped(View v) {
        BottomSheetDialogFragment bottomSheetDialogFragment = new AccountToggleFragment();
        bottomSheetDialogFragment.show(
                getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    public void settingsTapped(View v) {
        Intent intent = new Intent(UserAccountActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    public void baitTapped(View v) {

        if (user.getUnhiddenCassettes().size() < 1) {
            Intent intent = new Intent(UserAccountActivity.this, NoBaitsActivity.class);
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(UserAccountActivity.this, BaitSelectActivity.class);
        startActivity(intent);
        finish();
    }

    public void findTapped(View v) {
        Intent intent = new Intent(UserAccountActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void cassetteBoxTapped(View v) {
        Intent intent = new Intent(UserAccountActivity.this, CassetteBoxActivity.class);
        startActivity(intent);
        finish();
    }
}
