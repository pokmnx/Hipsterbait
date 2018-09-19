package com.hipsterbait.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Hint;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.Play;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;
import java.util.Date;

public class DetailsHintsFragment extends Fragment {

    private ArrayList<Hint> mHints;
    private String mUsername;
    private Journey mJourney;
    private HintsAdapter mAdapter;
    private RelativeLayout mNoHintsLayout;
    private Cassette mCassette;
    private HintInterface mDelegate;

    public DetailsHintsFragment() {}

    public static DetailsHintsFragment newInstance() {
        DetailsHintsFragment fragment = new DetailsHintsFragment();

        fragment.mHints = new ArrayList<>();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details_hints, container, false);

        GridView gridView = (GridView) view.findViewById(R.id.details_hints_gridview);
        mAdapter = new HintsAdapter();
        gridView.setAdapter(mAdapter);

        mNoHintsLayout = (RelativeLayout) view.findViewById(R.id.details_hints_relative_layout);

        return view;
    }

    public void setData(ArrayList<Hint> hints, Journey journey, Cassette cassette, String username, HintInterface delegate) {
        mHints = hints;
        mJourney = journey;
        mCassette = cassette;
        mUsername = username;
        mDelegate = delegate;

        if (mNoHintsLayout != null) {
            if (mHints.isEmpty()) {
                mNoHintsLayout.setVisibility(View.VISIBLE);
            } else {
                mNoHintsLayout.setVisibility(View.INVISIBLE);
            }
        }

        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    private class HintsAdapter extends BaseAdapter {
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = new View(getActivity());

                gridView = inflater.inflate(R.layout.hint_griditem, null);

            } else {
                gridView = convertView;
            }

            final Hint hint = mHints.get(position);

            ImageView hintImage = (ImageView) gridView
                    .findViewById(R.id.hint_griditem_image);
            final String imageUrl = hint.getHintImageURL();

            if ((imageUrl != null) && !(hint.flagged())) {
                Glide.with(getActivity())
                        .load(imageUrl)
                        .into(hintImage);
                hintImage.setScaleType(ImageView.ScaleType.CENTER);
                hintImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PhotoFullscreenActivity.class);
                        intent.putExtra(getString(R.string.image_extra), imageUrl);
                        startActivity(intent);
                    }
                });
            } else {
                hintImage.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.no_hint));
            }

            HBTextView title = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_title);
            title.setText(String.format(getString(R.string.location_hint), position + 1));

            HBTextView detail = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_text);

            if (hint.flagged()) {
                detail.setText("No Comment.");
            } else {
                detail.setText(hint.getDescription());
            }

            final HBTextView hashtags = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_hashtags);
            String city = mJourney.getCity();

            if (!(hint.flagged())) {
                if (city == null) {
                    hashtags.setText(R.string.hashtag_hipsterbait);
                } else {
                    hashtags.setText(R.string.hashtags);
                }
            }

            final HBTextView date = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_date);
            date.setText(hint.dateFormattedMDY());

            final ImageView avatarImage = (ImageView) gridView
                    .findViewById(R.id.hint_griditem_avatar_image);
            hint.setAvatarImage(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {
                    String userAvatarImageUrl = hint.getUserAvatarImageURL();
                    if (getActivity() != null) {
                        Glide.with(getActivity())
                                .load(userAvatarImageUrl)
                                .into(avatarImage);
                    }
                }

                @Override
                public void onFail(String error) {
                    if (getActivity() != null)
                        avatarImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.avatar_generic));
                }
            });

            final HBTextView username = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_player_name);
            username.setText(hint.getFoundBy());

            final LinearLayout flag = (LinearLayout) gridView
                    .findViewById(R.id.hint_griditem_flag_layout);
            final ImageView flagImage = gridView.findViewById(R.id.hint_griditem_flag);
            final HBTextView flagLabel = gridView.findViewById(R.id.hint_griditem_flag_label);

            if (hint.flagged()) {
                flag.setVisibility(View.GONE);
                hashtags.setVisibility(View.INVISIBLE);
                date.setVisibility(View.INVISIBLE);
                username.setVisibility(View.INVISIBLE);
                avatarImage.setVisibility(View.INVISIBLE);
            } else {
                if (mJourney.getUserRef().equals("hipster-god") || hint.getFlags().containsKey(mUsername)) {
                    flag.setAlpha(0.8f);
                    flagLabel.setTextColor(ContextCompat.getColor(getActivity(), R.color.hbCharcoal80));
                    flagImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.flag_inactive));
                } else {
                    flagLabel.setTextColor(ContextCompat.getColor(getActivity(), R.color.hbOrange));
                    flagImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.flag_orange));
                    flag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            flag.setAlpha(0.8f);
                            flagLabel.setTextColor(ContextCompat.getColor(getActivity(), R.color.hbCharcoal80));
                            flagImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.flag_inactive));
                            mDelegate.hintFlagged(position);
                        }
                    });
                }
            }

            return gridView;
        }

        @Override
        public int getCount() {
            if (mHints == null) {
                return 0;
            }

            return mHints.size();
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

    public interface HintInterface {
        public void hintFlagged(int position);
    }

}
