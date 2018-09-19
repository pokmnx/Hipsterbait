package com.hipsterbait.android.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.BadgeNotFound;
import com.hipsterbait.android.models.Badge;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.FoundInfo;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.NotificationItem;
import com.hipsterbait.android.models.Rating;
import com.hipsterbait.android.models.Song;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserBadges;
import com.hipsterbait.android.models.UserPoints;
import com.hipsterbait.android.other.AudioPlayerManager;
import com.hipsterbait.android.other.BadgesAwardManager;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.PointsEarningsManager;
import com.hipsterbait.android.other.ProgressCircle;
import com.hipsterbait.android.other.RotatingTexts;
import com.hipsterbait.android.widgets.HBTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class FoundCassetteActivity extends ImmersiveActivity implements View.OnClickListener {

    public HBTextView congratsLabel, cassetteNameLabel, rateChangeLabel, sassyTextLabel, baitNumberLabel;
    public ImageView horns1, horns2, horns3, horns4, horns5, cassetteArt, playPause, baitPick;
    public Button takeCassetteButton;
    public LinearLayout buttonsLayout;
    public ProgressBar progressBar;

    private AudioPlayerManager mManager;
    private User mUser;
    private Cassette mCassette;
    private int mRating, mPreviousRating, mPointsBefore, mAudioDuration;
    private boolean mSongLoaded = false, mSongPlayed = false, mRated = false, mPlaying = false, mNoHints = false, mMorePunk = false, mTaken = false;
    private MediaPlayer mPlayer;
    private ProgressCircle mCircle;
    private Handler mProgressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_cassette);

        congratsLabel = (HBTextView) findViewById(R.id.found_congrats_label);
        cassetteNameLabel = (HBTextView) findViewById(R.id.found_cassette_name);
        rateChangeLabel = (HBTextView) findViewById(R.id.found_rate_change);
        sassyTextLabel = (HBTextView) findViewById(R.id.found_sassy_text);
        baitNumberLabel = (HBTextView) findViewById(R.id.found_bait_number);
        baitPick = (ImageView) findViewById(R.id.found_bait_pick);

        horns1 = (ImageView) findViewById(R.id.found_horns_1);
        horns1.setOnClickListener(this);
        horns2 = (ImageView) findViewById(R.id.found_horns_2);
        horns2.setOnClickListener(this);
        horns3 = (ImageView) findViewById(R.id.found_horns_3);
        horns3.setOnClickListener(this);
        horns4 = (ImageView) findViewById(R.id.found_horns_4);
        horns4.setOnClickListener(this);
        horns5 = (ImageView) findViewById(R.id.found_horns_5);
        horns5.setOnClickListener(this);
        cassetteArt = (ImageView) findViewById(R.id.found_cassette_art);
        playPause = (ImageView) findViewById(R.id.found_play_pause);
        playPause.setOnClickListener(this);
        buttonsLayout = (LinearLayout) findViewById(R.id.found_buttons_layout);
        progressBar = (ProgressBar) findViewById(R.id.found_progress_bar);
        mCircle = (ProgressCircle) findViewById(R.id.found_cirlce);
        takeCassetteButton = (Button) findViewById(R.id.found_take_cassette);

        mManager = AudioPlayerManager.getInstance();

        mCassette = getIntent().getParcelableExtra(getString(R.string.cassette_extra));

        ChildEventListener tooSlowListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(getString(R.string.hidden))) {
                    if (mTaken) {
                        return;
                    }
                    boolean value = (Boolean) dataSnapshot.getValue();
                    if (value == false) {
                        Intent intent = new Intent(FoundCassetteActivity.this, TooSlowActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        mCassette.getRef().addChildEventListener(tooSlowListener);
        mChildListeners.put(mCassette.getRef(), tooSlowListener);

        mUser = ((HBApplication) getApplication()).user;
        mPointsBefore = mUser.getPoints();
        mPreviousRating = -1;

        fetchCassetteDetails();

        int unbaitedCassetteCount = mUser.getUnhiddenCassettes().size() + 1;

        baitPick.setVisibility(View.VISIBLE);
        baitNumberLabel.setVisibility(View.VISIBLE);
        baitNumberLabel.setText(String.valueOf(unbaitedCassetteCount));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressHandler != null) mProgressHandler.removeCallbacksAndMessages(null);
        if (mPlayer != null) mPlayer.release();
        Runtime.getRuntime().gc();
    }

    private void fetchCassetteDetails() {
        mCassette.setCassetteModel(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                mCassette.getCassetteModel().setSong(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {
                        mCassette.getCassetteModel().getSong().setBand(null);
                        mCassette.getCassetteModel().getSong().downloadSong(new ModelPropertySetCallback() {
                            @Override
                            public void onSuccess() {

                                try {
                                    mPlayer = new MediaPlayer();
                                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    mPlayer.setDataSource(
                                            FoundCassetteActivity.this,
                                            mCassette.getCassetteModel().getSong().getDataPath());
                                    mPlayer.prepare();
                                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            mp.stop();
                                            paused();
                                            mCircle.setAngle(0);

                                            mPlayer.reset();
                                            try {
                                                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                                mPlayer.setDataSource(
                                                        FoundCassetteActivity.this,
                                                        mCassette.getCassetteModel().getSong().getDataPath());
                                                mPlayer.prepare();
                                            } catch (IOException e) {
                                                Log.w(getString(R.string.hb_log_tag), e);
                                            }
                                        }
                                    });

                                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                    mmr.setDataSource(FoundCassetteActivity.this, mCassette.getCassetteModel().getSong().getDataPath());
                                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                    mAudioDuration = Integer.parseInt(durationStr);

                                    mSongLoaded = true;

                                    progressBar.setVisibility(View.GONE);

                                } catch (IOException e) {
                                    Log.w(getString(R.string.hb_log_tag), e);
                                    Toast.makeText(FoundCassetteActivity.this, "Song load failed.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFail(String error) {
                                Log.w(getString(R.string.hb_log_tag), "Couldn't download song: " + error);
                                Toast.makeText(FoundCassetteActivity.this, "Song load failed.", Toast.LENGTH_LONG).show();
                            }
                        });

                        congratsLabel.setText("Congratulations! You have found cassette #" + mCassette.getNumber());
                        cassetteNameLabel.setText("\"" + mCassette.getCassetteModel().getName() + "\"");

                        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference()
                                .child(getString(R.string.ratings))
                                .child(mCassette.getCassetteModel().getSongRef())
                                .child(mUser.getKey());

                        ValueEventListener ratingListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot == null) {
                                    Log.w(getString(R.string.hb_log_tag), "Rating dataSnapshot null");
                                    return;
                                }

                                if (dataSnapshot.exists() == false) {
                                    Log.w(getString(R.string.hb_log_tag), "Rating dataSnapshot exists null");
                                    return;
                                }

                                Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();

                                if (value == null) {
                                    Log.w(getString(R.string.hb_log_tag), "Null value " + dataSnapshot.getKey());
                                    return;
                                }

                                if (value.containsKey(getString(R.string.rating))) {
                                    mPreviousRating = ((Number) value.get(getString(R.string.rating))).intValue();

                                    mSongPlayed = true;

                                    View dummy = new View(FoundCassetteActivity.this);
                                    dummy.setTag(String.valueOf(mPreviousRating - 1));
                                    ratingButtonTapped(dummy);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(getString(R.string.hb_log_tag), databaseError.getMessage());
                            }
                        };

                        ratingsRef.addListenerForSingleValueEvent(ratingListener);

                        mValueListeners.put(ratingsRef, ratingListener);

                        mCassette.getCassetteModel().setCassetteArt(new ModelPropertySetCallback() {
                            @Override
                            public void onSuccess() {
                                try {
                                    Glide.with(FoundCassetteActivity.this)
                                        .load(mCassette.getCassetteModel().getCassetteArtURL())
                                        .into(cassetteArt);
                                } catch (Exception e) {
                                    Log.w(getString(R.string.hb_log_tag), e);
                                }
                            }

                            @Override
                            public void onFail(String error) {
                                Log.w(getString(R.string.hb_log_tag), error);
                            }
                        });
                    }

                    @Override
                    public void onFail(String error) {
                        Log.e(getString(R.string.hb_log_tag), "Failed to fetch song: " + error);
                        Toast.makeText(FoundCassetteActivity.this, "Song load failed.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFail(String error) {
                Log.e(getString(R.string.hb_log_tag), "Failed to fetch cassette model: " + error);
                Toast.makeText(FoundCassetteActivity.this,
                        "Failed to fetch cassette: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSongLoaded)
                    Toast.makeText(FoundCassetteActivity.this, "Network error.", Toast.LENGTH_LONG).show();
            }
        }, 30000);
    }

    private void ratingButtonTapped(View v) {
        if (mSongPlayed == false) {
            sassyTextLabel.setText(R.string.listen_first);
            sassyTextLabel.setVisibility(View.VISIBLE);
            return;
        }

        rateChangeLabel.setText(R.string.change_display);

        horns1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.horns_pink));
        horns2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.horns_charcoal));
        horns3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.horns_charcoal));
        horns4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.horns_charcoal));
        horns5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.horns_charcoal));

        int tag = Integer.parseInt((String) v.getTag());

        sassyTextLabel.setVisibility(View.VISIBLE);
        sassyTextLabel.setText(RotatingTexts.getString(RotatingTexts.ONE_STAR_RATINGS));

        if (tag > 0) {
            sassyTextLabel.setText(RotatingTexts.getString(RotatingTexts.TWO_STAR_RATINGS));
            horns2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.horns_pink));
        }
        if (tag > 1) {
            sassyTextLabel.setText(RotatingTexts.getString(RotatingTexts.THREE_STAR_RATINGS));
            horns3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.horns_pink));
        }
        if (tag > 2) {
            sassyTextLabel.setText(RotatingTexts.getString(RotatingTexts.FOUR_STAR_RATINGS));
            horns4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.horns_pink));
        }
        if (tag > 3) {
            sassyTextLabel.setText(RotatingTexts.getString(RotatingTexts.FIVE_STAR_RATINGS));
            horns5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.horns_pink));
        }

        mRating = tag + 1;
    }

    private void playPauseTapped() {
        if (mPlayer == null) {
            return;
        }

        try {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            }

            if (mSongLoaded == false || mPlayer == null) {
                Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();
                return;
            }

            mSongPlayed = true;

            if (mPlaying) {
                mPlayer.pause();
                paused();
            } else {
                mPlayer.start();
                played();
            }
        } catch (IllegalStateException e) {
            Log.w(getString(R.string.hb_log_tag), e);
        }
    }

    private void played() {
        playPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.found_pause_button));
        mPlaying = true;

        mProgressHandler = new Handler();
        mProgressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCircle.setAngle(mPlayer.getCurrentPosition() / (float) mAudioDuration);
                mProgressHandler.postDelayed(this, 100);
            }
        }, 100);

    }

    private void paused() {
        playPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.found_play_button));
        mPlaying = false;

        mProgressHandler.removeCallbacksAndMessages(null);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.found_horns_1:
            case R.id.found_horns_2:
            case R.id.found_horns_3:
            case R.id.found_horns_4:
            case R.id.found_horns_5:
                ratingButtonTapped(v);
                break;
            case R.id.found_play_pause:
                playPauseTapped();
                break;
        }
    }

    public void takeCassetteTapped(View v) {

        mTaken = true;

        if (mCassette.getJourneys().size() < 1) {
            Log.w(getString(R.string.hb_log_tag), "ERROR: Cassette was not hidden");
            Toast.makeText(this, "Error getting cassette data.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Journey lastJourney = mCassette.getJourneys().get(mCassette.getJourneys().size() - 1);

        if (lastJourney == null) {
            Log.w(getString(R.string.hb_log_tag), "ERROR: Cassette has no Journeys");
            Toast.makeText(this, "Error getting cassette data.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lastJourney.getAction().equals(getString(R.string.hidden)) == false) {
            Log.w(getString(R.string.hb_log_tag), "ERROR: Cassette was not hidden");
            Toast.makeText(this, "Error getting cassette data.", Toast.LENGTH_SHORT).show();
            return;
        }

        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(getString(R.string.hints))
                .child(mCassette.getJourneys().get(mCassette.getJourneys().size() - 1).getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChildren() == false) {
                                mNoHints = true;
                            }
                        } else {
                            mNoHints = true;
                        }
                        checkMorePunk();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
                        checkMorePunk();
                    }
                });
    }

    public void checkMorePunk() {

        final Journey lastJourney = mCassette.getJourneys().get(mCassette.getJourneys().size() - 1);

        // More Punk Than You
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(getString(R.string.users))
                .child(lastJourney.getUserRef())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getKey().equals(mUser.getKey())) {
                            takeCassette();
                            return;
                        }

                        if (dataSnapshot.exists() == false) {
                            takeCassette();
                            return;
                        }

                        Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();

                        if (value.containsKey(getString(R.string.last_hide_timestamp))) {
                            long lastHide = ((Number) value.get(getString(R.string.last_hide_timestamp))).longValue();
                            if (value.containsKey(getString(R.string.last_find_timestamp))) {
                                long lastFind = ((Number) value.get(getString(R.string.last_find_timestamp))).longValue();
                                if (lastFind < lastHide) {
                                    mMorePunk = true;
                                }
                            } else {
                                mMorePunk = true;
                            }
                        }
                        takeCassette();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
                        takeCassette();
                    }
                });
    }

    public void takeCassette() {

        final Journey lastJourney = mCassette.getJourneys().get(mCassette.getJourneys().size() - 1);

        takeCassetteButton.setVisibility(View.INVISIBLE);
        buttonsLayout.setVisibility(View.VISIBLE);

        // Create a new journey
        final Journey newJourney = new Journey(
                getString(R.string.found),
                lastJourney.getAddress(),
                mUser.getKey(),
                mCassette.getKey(),
                lastJourney.getElevation(),
                lastJourney.getStreet(),
                lastJourney.getCity(),
                lastJourney.getRegional(),
                lastJourney.getState(),
                lastJourney.getCountry(),
                lastJourney.getIATAIdentifier(), null);
        newJourney.save(null);

        lastJourney.pullLocation(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                newJourney.save(lastJourney.getLocation());
                newJourney.pullLocation(new ModelPropertySetCallback() {
                    @Override
                    public void onSuccess() {

                        int finds = 0;

                        for (Journey journey : mCassette.getJourneys()) {
                            if (journey.getAction().equals(getString(R.string.found))) {
                                finds += 1;
                            }
                        }

                        mCassette.setHidden(false);
                        mCassette.save(null);

                        mUser.setCassette(mCassette, true);
                        mUser.setLastFindTimestamp(newJourney.getTimestamp());

                        final ArrayList<UserBadges> newBadges = new ArrayList<>();

                        if (mRating > 0) {
                            if (mPreviousRating < 0 ) {
                                mRated = true;
                                mUser.incrementRatingCount();
                            }

                            Rating newRating = new Rating(
                                    mUser.getKey(),
                                    mCassette.getCassetteModel().getSongRef(),
                                    mRating,
                                    newJourney.getCity(), null);
                            newRating.save();

                            // Award Badges
                            if (mUser.getRatingCount() > 3) {
                                if (mUser.getUserBadge(getString(R.string.master_rater)) == null) {
                                    UserBadges userBadges = new UserBadges(
                                            mUser.getKey(),
                                            getString(R.string.master_rater),
                                            mCassette.getKey(),
                                            newJourney.getKey(), null);
                                    userBadges.save();
                                    mUser.setBadge(userBadges);
                                    newBadges.add(userBadges);
                                }

                                if (mPreviousRating > 0) {

                                }
                            }
                        }

                        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference ratingsRef = dbRef.child(getString(R.string.found_infos))
                                .child(lastJourney.getUserRef())
                                .child(mCassette.getKey());
                        ValueEventListener ratingsListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
                                    if (value.containsKey(getString(R.string.rating_given)) == false) {
                                        return;
                                    }

                                    long ratingGiven = ((Number) value.get(getString(R.string.rating_given))).longValue();
                                    if (mRating == ratingGiven) {
                                        dbRef.child(getString(R.string.users))
                                                .child(lastJourney.getUserRef())
                                                .child(getString(R.string.badges))
                                                .child("feedback")
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            return;
                                                        }

                                                        UserBadges userBadges = new UserBadges(lastJourney.getUserRef(), "feedback", mCassette.getKey(), newJourney.getKey(), null);
                                                        userBadges.save();
                                                        dataSnapshot.getRef().setValue(userBadges.getKey());
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                        Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
                                                    }
                                                });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        };
                        ratingsRef
                                .orderByChild(getString(R.string.date_found))
                                .addValueEventListener(ratingsListener);
                        mValueListeners.put(ratingsRef, ratingsListener);

                        if (mNoHints) {
                            if (mUser.getBadges().containsKey(getString(R.string.mic_drop)) == false) {
                                UserBadges userBadges = new UserBadges(mUser.getKey(), getString(R.string.mic_drop), mCassette.getKey(), newJourney.getKey(), null);
                                userBadges.save();
                                mUser.setBadge(userBadges);
                                newBadges.add(userBadges);
                            }
                        }

                        if (mMorePunk) {
                            if (mUser.getBadges().containsKey(getString(R.string.more_punk_than_you)) == false) {
                                UserBadges userBadges = new UserBadges(mUser.getKey(), getString(R.string.more_punk_than_you), mCassette.getKey(), newJourney.getKey(), null);
                                userBadges.save();
                                mUser.setBadge(userBadges);
                                newBadges.add(userBadges);
                            }
                        }

                        if (mUser.userHasFoundCassette(mCassette) == false) {
                            mUser.incrementFoundCount();
                        }

                        // Give the user points
                        ArrayList<UserPoints> result = PointsEarningsManager.awardPointsForFinding(FoundCassetteActivity.this, mUser, mCassette, newJourney, lastJourney, mRated);

                        int newPoints = 0;
                        for (UserPoints userPoints : result) {
                            newPoints += userPoints.getValue();
                        }

                        FoundInfo foundInfo = new FoundInfo(newJourney.getTimestamp(), false, newPoints, mRating, newJourney.getKey(), null);
                        foundInfo.setRef(mUser, mCassette);
                        foundInfo.save();

                        newBadges.addAll(BadgesAwardManager.awardBadgesForFind(FoundCassetteActivity.this, mUser, mCassette, lastJourney, newJourney));
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
                            Intent intent = new Intent(FoundCassetteActivity.this, BadgeNotificationActivity.class);
                            intent.putParcelableArrayListExtra(getString(R.string.arraylist_extra), items);
                            startActivity(intent);
                        }

                        mUser.save();

                        String findsText = "";

                        int ones = finds % 10;
                        int tens = (int) (Math.floor((double) (finds / 10)));
                        tens = tens % 10;
                        if (tens == 1) {
                            findsText = finds + "th";
                        } else {
                            switch (ones) {
                                case 1:
                                    findsText = finds + "st";
                                    break;
                                case 2:
                                    findsText = finds + "nd";
                                    break;
                                case 3:
                                    findsText = finds + "rd";
                                    break;
                                default:
                                    findsText = finds + "th";
                                    break;
                            }
                        }

                        if (!FoundCassetteActivity.this.isFinishing()) {
                            Bundle args = new Bundle();
                            args.putString(getString(R.string.title_string_arg), "You are the " + findsText + " person to discover #" + mCassette.getNumber() + " \"" + mCassette.getCassetteModel().getName() + "\"");
                            args.putParcelableArrayList(getString(R.string.userpoints_arraylist_arg), result);
                            args.putInt(getString(R.string.old_points), mPointsBefore);
                            args.putInt(getString(R.string.new_points), newPoints);
                            BottomSheetDialogFragment pointsFrag = new PointsReceiptFragment();
                            pointsFrag.setArguments(args);
                            pointsFrag.show(getSupportFragmentManager(), pointsFrag.getTag());
                        }
                    }

                    @Override
                    public void onFail(String error) {
                        Log.w(getString(R.string.hb_log_tag),
                                "ERROR: Couldn't fetch location for Journey: " + newJourney.getKey());
                    }
                });
            }

            @Override
            public void onFail(String error) {
                Log.w(getString(R.string.hb_log_tag),
                        "ERROR: Couldn't fetch location for Journey: " + lastJourney.getKey());
            }
        });
    }

    public void findButtonTapped(View v) {
        Intent intent = new Intent(FoundCassetteActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void baitButtonTapped(View v) {
        Intent intent = new Intent(FoundCassetteActivity.this, BaitSelectActivity.class);
        startActivity(intent);
        finish();
    }

    public void closeButtonTapped(View v) {
        finish();
    }
}
