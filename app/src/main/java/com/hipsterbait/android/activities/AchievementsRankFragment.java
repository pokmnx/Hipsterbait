package com.hipsterbait.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RankNotFound;
import com.hipsterbait.android.models.DataCallback;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.Rank;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserRank;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.RanksStore;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;

public class AchievementsRankFragment extends Fragment {

    public HBTextView currentRankLabel, progressLabel;
    public ImageView progressView;
    public GridView gridView;

    private User user;

    public static AchievementsRankFragment newInstance() {
        AchievementsRankFragment fragment = new AchievementsRankFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_achievements_rank, container, false);

        currentRankLabel = (HBTextView) view.findViewById(R.id.ranks_current_label);
        progressLabel = (HBTextView) view.findViewById(R.id.ranks_progress_label);

        progressView = (ImageView) view.findViewById(R.id.ranks_progress_view);

        user = ((HBApplication) getActivity().getApplication()).user;

        UserRank currentRank = user.getCurrentRank();

        if (currentRank == null) {
            Log.e(getString(R.string.hb_log_tag), getString(R.string.no_current_rank));
            getActivity().finish();
            return view;
        }

        Rank rank;

        try {
            rank = RanksStore.getInstance().getRank(currentRank.getRank());
        } catch (RankNotFound e) {
            Log.e(getString(R.string.hb_log_tag), e.getLocalizedMessage());
            return view;
        }

        int points = user.getPoints() % 1000;
        currentRankLabel.setText("Current Rank: Level " + rank.getLevel() +
                " \"" + rank.getName() + "\"");

        int pointsAway = 1000 - points;
        progressLabel.setText(pointsAway + " points away from completing this level.");

        float progress = (float) points / (float) 1000;

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) progressView.getLayoutParams();
        layoutParams.width = (int) (progress * 71);
        progressView.setLayoutParams(layoutParams);

        gridView = (GridView) view.findViewById(R.id.ranks_gridview);
        gridView.setAdapter(new RankItemAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Rank rank = RanksStore.getInstance().getRanks().get(position);

                final UserRank userRank = user.getUserRank(rank.getKey());

                if (userRank != null) {
                    rank.setImage(new ModelPropertySetCallback() {
                        @Override
                        public void onSuccess() {
                            Intent intent = new Intent(getActivity(), BadgeDetailActivity.class);
                            intent.putExtra("badge_details_rank", true);
                            intent.putExtra(
                                    "badge_details_key",
                                    rank.getKey());
                            intent.putExtra(
                                    getString(R.string.badge_details_name),
                                    rank.getName());
                            intent.putExtra(
                                    getString(R.string.badge_details_detail),
                                    rank.getFoundText());
                            intent.putExtra(
                                    getString(R.string.badge_details_timestamp),
                                    "Unlocked on " + userRank.getDateString());

                            startActivity(intent);
                        }

                        @Override
                        public void onFail(String error) {
                            Log.w("HB", error);
                        }
                    });
                }
            }
        });

        return view;
    }

    private class RankItemAdapter extends BaseAdapter {

        private ArrayList<Rank> ranks;

        public RankItemAdapter() {
            ranks = RanksStore.getInstance().getRanks();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = new View(getActivity());

                gridView = inflater.inflate(R.layout.rank_grid_item, parent, false);
            } else {
                gridView = convertView;
            }

            final ImageView imageView = (ImageView) gridView.findViewById(R.id.rank_item_imageview);
            ImageView disclosure = (ImageView) gridView.findViewById(R.id.rank_item_disclosure);
            HBTextView textView = (HBTextView) gridView.findViewById(R.id.rank_item_textview);
            HBTextView dateText = (HBTextView) gridView.findViewById(R.id.rank_item_date);

            Rank rank;
            try {
                rank = ranks.get(position);
            } catch (IndexOutOfBoundsException e) {
                return gridView;
            }

            UserRank userRank = user.getUserRank(rank.getKey());

            textView.setText("Level " + rank.getLevel() + " \"" + rank.getName() + "\"");

            if (userRank != null) {
                final Rank finalRank = rank;
                rank.setImage(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        Glide.with(AchievementsRankFragment.this)
                                .load(finalRank.getImageUrl())
                                .into(imageView);
                    }

                    @Override
                    public void onFail(String error) {
                        Log.w("HB", error);
                    }
                });
                dateText.setText(
                        DateFormat.format("MM/dd/yyyy", userRank.getTimestamp()).toString());
                disclosure.setVisibility(View.VISIBLE);
            } else {
                imageView.setImageDrawable(ContextCompat
                        .getDrawable(getActivity(), R.drawable.locked_badge_bg));
                dateText.setText("");
                disclosure.setVisibility(View.INVISIBLE);

            }

            return gridView;
        }

        @Override
        public int getCount() {
            return ranks.size();
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
