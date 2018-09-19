package com.hipsterbait.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.BadgeNotFound;
import com.hipsterbait.android.models.Badge;
import com.hipsterbait.android.models.DataCallback;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserBadges;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.OnSwipeTouchListener;
import com.hipsterbait.android.widgets.HBButton;

import java.util.ArrayList;

public class AchievementsBadgeFragment extends Fragment implements View.OnClickListener {

    public HBButton locationButton, activityButton, randomButton;
    public GridView gridView;

    private User mUser;

    private HBButton mSelectedButton;

    private ArrayList<Badge> locationBadges, activityBadges, randomBadges;
    private BadgeItemAdapter locationAdapter, activityAdapter, randomAdapter;

    private static final int LOCATION = 0;
    private static final int ACTIVITY = 1;
    private static final int RANDOM = 2;

    private int hoarderBadges = 0;

    public static AchievementsBadgeFragment newInstance() {
        AchievementsBadgeFragment fragment = new AchievementsBadgeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_achievements_badge, container, false);

        super.onCreate(savedInstanceState);

        mUser = ((HBApplication) getActivity().getApplication()).user;

        locationButton = (HBButton) view.findViewById(R.id.badges_location_button);
        locationButton.setOnClickListener(this);
        activityButton = (HBButton) view.findViewById(R.id.badges_activity_button);
        activityButton.setOnClickListener(this);
        randomButton = (HBButton) view.findViewById(R.id.badges_random_button);
        randomButton.setOnClickListener(this);

        mSelectedButton = locationButton;

        locationBadges = BadgesStore.getInstance().
                getBadges(getString(R.string.location));
        activityBadges = BadgesStore.getInstance().
                getBadges(getString(R.string.event));
        randomBadges = BadgesStore.getInstance()
                .getBadges(getString(R.string.random));

        long hoardedTime = mUser.getLongestHoarded() - (7 * 24 * 60 * 60 * 1000);
        hoarderBadges = ((int) (hoardedTime / (24 * 60 * 60 * 1000))) * 10;

        locationAdapter = new BadgeItemAdapter(locationBadges);
        activityAdapter = new BadgeItemAdapter(activityBadges);
        randomAdapter = new BadgeItemAdapter(randomBadges);

        gridView = (GridView) view.findViewById(R.id.badges_gridview);
        gridView.setAdapter(locationAdapter);
        gridView.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            public void onSwipeLeft() {
                int selectedIndex = Integer.parseInt((String) mSelectedButton.getTag());
                selectedIndex += 1;
                if (selectedIndex > 2) {
                    return;
                }
                switch (selectedIndex) {
                    case LOCATION:
                    default:
                        AchievementsBadgeFragment.this.onClick(locationButton);
                        break;
                    case ACTIVITY:
                        AchievementsBadgeFragment.this.onClick(activityButton);
                        break;
                    case RANDOM:
                        AchievementsBadgeFragment.this.onClick(randomButton);
                        break;
                }
            }

            public void onSwipeRight() {
                int selectedIndex = Integer.parseInt((String) mSelectedButton.getTag());
                selectedIndex -= 1;
                if (selectedIndex < 0) {
                    return;
                }
                switch (selectedIndex) {
                    case LOCATION:
                    default:
                        AchievementsBadgeFragment.this.onClick(locationButton);
                        break;
                    case ACTIVITY:
                        AchievementsBadgeFragment.this.onClick(activityButton);
                        break;
                    case RANDOM:
                        AchievementsBadgeFragment.this.onClick(randomButton);
                        break;
                }
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });



        return view;
    }

    public void onClick(View v) {
        mSelectedButton.setBackgroundResource(R.color.hbCharcoal60);

        mSelectedButton = (HBButton) v;
        mSelectedButton.setBackgroundResource(R.color.hbBlue);

        gridView.invalidateViews();

        switch (Integer.parseInt((String) mSelectedButton.getTag())) {
            case LOCATION:
            default:
                gridView.setAdapter(locationAdapter);
                break;
            case ACTIVITY:
                gridView.setAdapter(activityAdapter);
                break;
            case RANDOM:
                gridView.setAdapter(randomAdapter);
                break;
        }
    }

    private class BadgeItemAdapter extends BaseAdapter {

        private ArrayList<Badge> badges;
        private int selectedFilter;

        private BadgeItemAdapter(ArrayList<Badge> badges) {
            this.badges = badges;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = new View(getActivity());

                gridView = inflater.inflate(R.layout.badge_grid_item, null);

            } else {
                gridView = convertView;
            }

            Badge badge;

            try {
                badge = badges.get(position);
            } catch (IndexOutOfBoundsException e) {
                return gridView;
            }

            final ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.badgeitem_imageview);

            if (((HBApplication) getActivity().getApplication()).user.hasBadge(badge.getKey())) {

                if (position < hoarderBadges) {
                    try {
                        badge = BadgesStore.getInstance().getBadge(getString(R.string.hoarder));
                    } catch (BadgeNotFound e) {
                        return gridView;
                    }
                }

                final Badge finalBadge = badge;
                badge.setImage(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        Glide.with(AchievementsBadgeFragment.this)
                                .load(finalBadge.getImageUrl())
                                .into(imageView);
                    }

                    @Override
                    public void onFail(String error) {

                    }
                });
            } else {

                if (position < hoarderBadges) {
                    try {
                        badge = BadgesStore.getInstance().getBadge(getString(R.string.hoarder));
                    } catch (BadgeNotFound e) {
                        return gridView;
                    }
                }

                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(), R.drawable.locked_badge_bg));
            }

            TextView textView = (TextView) gridView
                    .findViewById(R.id.badgeitem_textview);
            textView.setText(badge.getName());

            final Badge finalBadge = badge;
            gridView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    finalBadge.setImage(new ModelPropertySetCallback() {
                        @Override
                        public void onSuccess() {

                            Intent intent = new Intent(getActivity(), BadgeDetailActivity.class);

                            UserBadges userBadges = mUser.getUserBadge(finalBadge.getKey());

                            if (userBadges == null) {
                                intent.putExtra("badge_details_rank", false);
                                intent.putExtra(
                                        "badge_details_key",
                                        "locked_badge");
                                intent.putExtra(
                                        getString(R.string.badge_details_name),
                                        finalBadge.getName());
                                intent.putExtra(
                                        getString(R.string.badge_details_detail),
                                        finalBadge.getNotFoundText());
                                intent.putExtra(
                                        getString(R.string.badge_details_timestamp),
                                        "");
                            } else {
                                intent.putExtra("badge_details_rank", false);
                                intent.putExtra(
                                        "badge_details_key",
                                        finalBadge.getKey());
                                intent.putExtra(
                                        getString(R.string.badge_details_name),
                                        finalBadge.getName());
                                intent.putExtra(
                                        getString(R.string.badge_details_detail),
                                        finalBadge.getFoundText());
                                intent.putExtra(
                                        getString(R.string.badge_details_timestamp),
                                        "Unlocked on " + userBadges.getDateString());
                            }

                            startActivity(intent);
                        }

                        @Override
                        public void onFail(String error) {
                            Log.w("HB", error);
                        }
                    });

                }
            });
            return gridView;
        }

        @Override
        public int getCount() {
            return badges.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}
