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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Hint;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.widgets.HBButton;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;

public class LocationHintsFragment extends Fragment {

    public GridView gridView;
    public HBButton addHintBlue;

    private User mUser;
    private Journey mJourney;
    private Cassette mCassette;
    private ArrayList<Hint> mHints;
    private HintsAdapter mAdapter = new HintsAdapter();

    private DatabaseReference mHintsRef;
    private ChildEventListener mHintsListener;

    public static LocationHintsFragment newInstance() {
        LocationHintsFragment fragment = new LocationHintsFragment();
        fragment.mHints = new ArrayList<>();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location_hints, container, false);

        mUser = ((HBApplication) getActivity().getApplication()).user;

        gridView = (GridView) view.findViewById(R.id.location_hints_gridview);
        gridView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        addHintBlue = (HBButton) view.findViewById(R.id.location_hints_add_hint_blue);
        addHintBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHintTapped(v);
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHintsRef != null) {
            mHintsRef.removeEventListener(mHintsListener);
        }
    }

    public void setData(Cassette cassette, final Context context) {
        mJourney = cassette.getJourneys().get(cassette.getJourneys().size() - 1);
        mCassette = cassette;

        mHintsRef = FirebaseDatabase.getInstance().getReference()
                .child(context.getString(R.string.hints))
                .child(mJourney.getKey());
        mHintsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    Hint hint = new Hint(dataSnapshot);
                    mHints.add(hint);
                    hint.setHintImage(new ModelPropertySetCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFail(String error) {

                        }
                    });
                    if (mAdapter != null) {
                        gridView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (RequiredValueMissing e) {
                    Log.w(getActivity().getString(R.string.hb_log_tag), e);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        mHintsRef.addChildEventListener(mHintsListener);
    }

    public void addHintTapped(View v) {

        if (mJourney.getAction().equals(getString(R.string.hidden)) == false) {
            Toast.makeText(getActivity(), "This cassette is not in play.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mJourney.getUserRef().equals(mUser.getKey()) == false) {
            Toast.makeText(getActivity(), "You were not the last person to hide this cassette.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), BaitHintActivity.class);
        intent.putExtra(getString(R.string.cassette_extra_key), mCassette.getKey());
        intent.putExtra(getString(R.string.journey_extra), mJourney);
        startActivity(intent);
    }

    private class HintsAdapter extends BaseAdapter {
        public View getView(int position, View convertView, ViewGroup parent) {
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

            final ImageView hintImage = (ImageView) gridView
                    .findViewById(R.id.hint_griditem_image);
            hint.setHintImage(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {
                    final String imageUrl = hint.getHintImageURL();
                    if (imageUrl != null) {
                        try {
                            Glide.with(getActivity())
                                .load(imageUrl)
                                .into(hintImage);
                            hintImage.setScaleType(ImageView.ScaleType.CENTER);
                        } catch (Exception e) {
                            Log.w(getString(R.string.hb_log_tag), e);
                        }
                    }
                    hintImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), PhotoFullscreenActivity.class);
                            intent.putExtra(getString(R.string.image_extra), imageUrl);
                            startActivity(intent);
                        }
                    });
                }

                @Override
                public void onFail(String error) {

                }
            });

            HBTextView title = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_title);
            title.setText(String.format(getString(R.string.location_hint), position + 1));

            HBTextView detail = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_text);
            detail.setText(hint.getDescription());

            HBTextView hashtags = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_hashtags);
            String city = mJourney.getCity();
            if (city == null) {
                hashtags.setText(R.string.hashtag_hipsterbait);
            } else {
                hashtags.setText(R.string.hashtags);
            }

            HBTextView date = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_date);
            date.setText(hint.dateFormattedMDY());

            final ImageView avatarImage = (ImageView) gridView
                    .findViewById(R.id.hint_griditem_avatar_image);
            hint.setAvatarImage(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {
                    String userAvatarImageUrl = hint.getUserAvatarImageURL();
                    if (userAvatarImageUrl != null) {
                        try {
                            Glide.with(getActivity())
                                .load(userAvatarImageUrl)
                                .into(avatarImage);
                        } catch (Exception e) {
                            Log.w(getString(R.string.hb_log_tag), e);
                            avatarImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.avatar_generic));
                        }
                    } else {
                        avatarImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.avatar_generic));
                    }
                }

                @Override
                public void onFail(String error) {
                    avatarImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.avatar_generic));
                }
            });

            HBTextView username = (HBTextView) gridView
                    .findViewById(R.id.hint_griditem_player_name);
            username.setText(hint.getFoundBy());

            return gridView;
        }

        @Override
        public int getCount() {
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
}
