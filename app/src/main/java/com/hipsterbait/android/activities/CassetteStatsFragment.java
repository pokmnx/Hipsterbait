package com.hipsterbait.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.BadgeNotFound;
import com.hipsterbait.android.Resources.Exceptions.CassetteNotFound;
import com.hipsterbait.android.Resources.Exceptions.PointsNotFound;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.models.Badge;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.NotificationItem;
import com.hipsterbait.android.models.Points;
import com.hipsterbait.android.models.Rating;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserBadges;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.RotatingTexts;
import com.hipsterbait.android.widgets.HBButton;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;
import java.util.Date;

public class CassetteStatsFragment extends Fragment {

    public ImageView yourHorns1, yourHorns2, yourHorns3, yourHorns4, yourHorns5,
        averageHorns1, averageHorns2, averageHorns3, averageHorns4, averageHorns5,
        changeHorns1, changeHorns2, changeHorns3, changeHorns4, changeHorns5,
        pickImage, flagImage;
    public HBTextView pointsLabel, pointsNumber, inPlayStatus, flaggedStatus, flagNumber,
        artistName, songName, rateChangeLabel, rateSassyText;
    public Switch emailSwitch;
    public LinearLayout newRatingLayout;
    public HBButton changeButton;

    private Cassette mCassette;
    private int mPointsEarned = 0, mUserRating = 0, mRatingsCount = 0, mRatingsTotal = 0,
            mAverageRating = 0, mNumberOfFlags = 0, mNewRating = 0;
    private Rating mUserRatingObject;
    private DatabaseReference mDbRef;
    private User mUser;

    private DatabaseReference mRatingsRef, mFlagRef;
    private ChildEventListener mRatingsListener, mFlagListener;

    public static CassetteStatsFragment newInstance(Cassette cassette) {
        CassetteStatsFragment fragment = new CassetteStatsFragment();
        fragment.mCassette = cassette;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cassette_stats, container, false);

        pointsLabel = (HBTextView) view.findViewById(R.id.track_stats_points_title);
        pointsNumber = (HBTextView) view.findViewById(R.id.track_stats_points_total);
        yourHorns1 = (ImageView) view.findViewById(R.id.track_stats_your_horns_1);
        yourHorns2 = (ImageView) view.findViewById(R.id.track_stats_your_horns_2);
        yourHorns3 = (ImageView) view.findViewById(R.id.track_stats_your_horns_3);
        yourHorns4 = (ImageView) view.findViewById(R.id.track_stats_your_horns_4);
        yourHorns5 = (ImageView) view.findViewById(R.id.track_stats_your_horns_5);
        averageHorns1 = (ImageView) view.findViewById(R.id.track_stats_average_horns_1);
        averageHorns2 = (ImageView) view.findViewById(R.id.track_stats_average_horns_2);
        averageHorns3 = (ImageView) view.findViewById(R.id.track_stats_average_horns_3);
        averageHorns4 = (ImageView) view.findViewById(R.id.track_stats_average_horns_4);
        averageHorns5 = (ImageView) view.findViewById(R.id.track_stats_average_horns_5);
        changeHorns1 = (ImageView) view.findViewById(R.id.track_stats_horns_1);
        changeHorns1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingButtonTapped(v);
            }
        });
        changeHorns2 = (ImageView) view.findViewById(R.id.track_stats_horns_2);
        changeHorns2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingButtonTapped(v);
            }
        });
        changeHorns3 = (ImageView) view.findViewById(R.id.track_stats_horns_3);
        changeHorns3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingButtonTapped(v);
            }
        });
        changeHorns4 = (ImageView) view.findViewById(R.id.track_stats_horns_4);
        changeHorns4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingButtonTapped(v);
            }
        });
        changeHorns5 = (ImageView) view.findViewById(R.id.track_stats_horns_5);
        changeHorns5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingButtonTapped(v);
            }
        });
        rateSassyText = (HBTextView) view.findViewById(R.id.track_stats_sassy);
        rateChangeLabel = (HBTextView) view.findViewById(R.id.track_stats_rate_change);
        rateChangeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateButtonTapped(v);
            }
        });
        inPlayStatus = (HBTextView) view.findViewById(R.id.track_stats_status_detail);
        pickImage = (ImageView) view.findViewById(R.id.track_stats_pick);
        flaggedStatus = (HBTextView) view.findViewById(R.id.track_stats_flagged_detail);
        flagNumber = (HBTextView) view.findViewById(R.id.track_stats_flagged_number);
        flagImage = (ImageView) view.findViewById(R.id.track_stats_flag);
        artistName = (HBTextView) view.findViewById(R.id.track_stats_artist_detail);
        songName = (HBTextView) view.findViewById(R.id.track_stats_song_detail);
        emailSwitch = (Switch) view.findViewById(R.id.track_stats_switch);
        newRatingLayout = (LinearLayout) view.findViewById(R.id.track_stats_change_rating_layout);
        changeButton = (HBButton) view.findViewById(R.id.track_stats_change_button);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonTapped(v);
            }
        });

        mDbRef = FirebaseDatabase.getInstance().getReference();
        mUser = ((HBApplication) getActivity().getApplication()).user;

        try {
            mPointsEarned = mUser.getPoints(mCassette);
        } catch (CassetteNotFound e) {
            Log.w(getActivity().getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        pointsNumber.setText(String.valueOf(mPointsEarned));

        mCassette.setCassetteModel(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                mRatingsRef= mDbRef
                        .child(getActivity().getString(R.string.ratings))
                        .child(mCassette.getCassetteModel().getSongRef());

                mRatingsListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {
                            if (dataSnapshot == null) {
                                return;
                            }

                            if (dataSnapshot.exists() == false) {
                                return;
                            }

                            if (dataSnapshot.getValue() == null) {
                                return;
                            }

                            Rating rating = new Rating(dataSnapshot);

                            if (rating.getUserRef().equals(mUser.getKey())) {
                                mUserRating = rating.getRating();
                                mUserRatingObject = rating;
                                mNewRating = rating.getRating();
                            }

                            mRatingsCount += 1;
                            mRatingsTotal += rating.getRating();

                            mAverageRating = mRatingsTotal / mRatingsCount;

                            setRatingImages();

                        } catch (RequiredValueMissing e) {
                            Log.w(getActivity().getString(R.string.hb_log_tag), e.getMessage());
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(getActivity().getString(R.string.hb_log_tag), databaseError.getMessage());
                    }
                };

                mRatingsRef.addChildEventListener(mRatingsListener);

                mFlagRef = mDbRef
                        .child(getActivity().getString(R.string.flags))
                        .child(mCassette.getCassetteModel().getKey())
                        .child(mCassette.getKey());

                mFlagListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        mNumberOfFlags += 1;

                        flagNumber.setText(String.valueOf(mNumberOfFlags));
                        flagNumber.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(getActivity().getString(R.string.hb_log_tag), databaseError.getMessage());
                    }
                };

                mFlagRef.addChildEventListener(mFlagListener);

                mCassette.getCassetteModel().setSong(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        mCassette.getCassetteModel().getSong().setBand(new ModelPropertySetCallback() {
                            @Override
                            public void onSuccess() {
                                if (mCassette.getCassetteModel().getAnnouncement() < new Date().getTime()) {
                                    String aName =  mCassette.getCassetteModel().getSong().getBand().getName();
                                    if (aName != null)
                                        artistName.setText(aName);
                                    String sName = mCassette.getCassetteModel().getSong().getName();
                                    if (sName != null)
                                        songName.setText(sName);
                                }

                                if (mCassette
                                        .getCassetteModel()
                                        .getSong()
                                        .getBand()
                                        .getEmailList()
                                        .containsKey(mUser.getKey())) {
                                    emailSwitch.toggle();
                                }

                                emailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            mCassette.getCassetteModel().getSong().getBand().addToEmailList(mUser.getKey());
                                            mCassette.getCassetteModel().getSong().getBand().save();
                                        } else {
                                            mCassette.getCassetteModel().getSong().getBand().removeFromEmailList(mUser.getKey());
                                            mCassette.getCassetteModel().getSong().getBand().save();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFail(String error) {
                                Log.w(getActivity().getString(R.string.hb_log_tag), error);
                            }
                        });
                    }

                    @Override
                    public void onFail(String error) {
                        Log.w(getActivity().getString(R.string.hb_log_tag), error);
                    }
                });
            }

            @Override
            public void onFail(String error) {
                Log.w(getActivity().getString(R.string.hb_log_tag), error);
            }
        });

        if (mCassette.isHidden()) {
            inPlayStatus.setText("In Play");
            pickImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pick_green));
        } else {
            inPlayStatus.setText("Out of Play");
            pickImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pick_char));
        }

        if (mCassette.isFlagged()) {
            flaggedStatus.setText("Yes");
            flagImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.flag_orange));

            inPlayStatus.setText("Out of Play");
            pickImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pick_char));
        } else {
            flaggedStatus.setTag("No");
            flagImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.flag_charcoal));
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRatingsRef != null) {
            mRatingsRef.removeEventListener(mRatingsListener);
        }

        if (mFlagRef != null) {
            mFlagRef.removeEventListener(mFlagListener);
        }
    }

    private void setRatingImages() {
        if (mUserRating > 0) {
            yourHorns1.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
            changeHorns1.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
        } else {
            yourHorns1.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
            changeHorns1.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }
        if (mUserRating > 1) {
            yourHorns2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
            changeHorns2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
        } else {
            yourHorns2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
            changeHorns2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }
        if (mUserRating > 2) {
            yourHorns3.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
            changeHorns3.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
        } else {
            yourHorns3.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
            changeHorns3.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }
        if (mUserRating > 3) {
            yourHorns4.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
            changeHorns4.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
        } else {
            yourHorns4.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
            changeHorns4.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }
        if (mUserRating > 4) {
            yourHorns5.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
            changeHorns5.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
        } else {
            yourHorns5.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
            changeHorns5.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }

        if (mAverageRating > 0) {
            averageHorns1.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
        } else {
            averageHorns1.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }
        if (mAverageRating > 1) {
            averageHorns2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
        } else {
            averageHorns2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }
        if (mAverageRating > 2) {
            averageHorns3.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
        } else {
            averageHorns3.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }
        if (mAverageRating > 3) {
            averageHorns4.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
        } else {
            averageHorns4.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }
        if (mAverageRating > 4) {
            averageHorns5.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_blue));
        } else {
            averageHorns5.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        }
    }

    public void changeButtonTapped(View v) {
        newRatingLayout.setVisibility(View.VISIBLE);
    }

    public void ratingButtonTapped(View v) {
        changeHorns1.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
        changeHorns2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        changeHorns3.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        changeHorns4.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        changeHorns5.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_char10));
        rateSassyText.setText(RotatingTexts.getString(RotatingTexts.ONE_STAR_RATINGS));

        if (Integer.parseInt((String) v.getTag()) > 0) {
            changeHorns2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
            rateSassyText.setText(RotatingTexts.getString(RotatingTexts.TWO_STAR_RATINGS));
        }

        if (Integer.parseInt((String) v.getTag()) > 1) {
            changeHorns3.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
            rateSassyText.setText(RotatingTexts.getString(RotatingTexts.THREE_STAR_RATINGS));
        }

        if (Integer.parseInt((String) v.getTag()) > 2) {
            changeHorns4.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
            rateSassyText.setText(RotatingTexts.getString(RotatingTexts.FOUR_STAR_RATINGS));
        }

        if (Integer.parseInt((String) v.getTag()) > 3) {
            changeHorns5.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.horns_pink));
            rateSassyText.setText(RotatingTexts.getString(RotatingTexts.FIVE_STAR_RATINGS));
        }

        mNewRating = Integer.parseInt((String) v.getTag()) + 1;
        rateSassyText.setVisibility(View.VISIBLE);
    }

    private void rateButtonTapped(View v) {
        if (mUserRatingObject == null) {
            mUserRatingObject = new Rating(
                    mUser.getKey(),
                    mCassette.getCassetteModel().getSongRef(),
                    mNewRating, mCassette.getJourneys().get(mCassette.getJourneys().size() - 1).getCity(),
                    null);
            mUser.incrementRatingCount();
            mUser.save();
        } else {

            if (mCassette.getCassetteModel().getSong().getBand() != null && mNewRating != mUserRatingObject.getRating()) {
                if (mUser.getUserBadge(getString(R.string.fucking_axl)) == null) {
                    UserBadges userBadges = new UserBadges(
                            mUser.getKey(),
                            getString(R.string.fucking_axl),
                            mCassette.getKey(),
                            null, null);
                    userBadges.save();
                    mUser.setBadge(userBadges);
                }
            }

            mUserRatingObject.setRating(mNewRating);
            mUserRatingObject.setTimestamp(new Date().getTime());
        }
        mUserRatingObject.save();
        mUserRating = mNewRating;

        ArrayList<UserBadges> newBadges = new ArrayList<>();

        if (mUser.getRatingCount() > 3) {
            if (mUser.getUserBadge(getActivity().getString(R.string.master_rater)) == null) {
                UserBadges userBadges = new UserBadges(
                        mUser.getKey(),
                        getActivity().getString(R.string.master_rater),
                        mCassette.getKey(),
                        mCassette.getJourneys().get(mCassette.getJourneys().size() - 1).getKey(),
                        null);
                userBadges.save();
                mUser.setBadge(userBadges);
                mUser.save();
                newBadges.add(userBadges);
            }
        }

        setRatingImages();

        newRatingLayout.setVisibility(View.INVISIBLE);

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

        if (items.size() > 0) {
            Intent intent = new Intent(getActivity(), BadgeNotificationActivity.class);
            intent.putParcelableArrayListExtra(getString(R.string.arraylist_extra), items);
            startActivity(intent);
        }
    }
}
