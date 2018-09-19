package com.hipsterbait.android.models;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.BadgeNotFound;
import com.hipsterbait.android.Resources.Exceptions.CassetteNotFound;
import com.hipsterbait.android.Resources.Exceptions.RankNotFound;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.activities.BadgeNotificationActivity;
import com.hipsterbait.android.other.AudioPlayerManager;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.CacheManager;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.RanksStore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class User {
    private String mKey;
    private String mEmail;
    private String mUsername;
    private String mFirstname;
    private String mLastname;
    private String mAvatarImageRef;
    private Map<String, String> mRanks;
    private Map<String, String> mBadges;
    private Map<String, Boolean> mNotifications;
    private Map<String, Boolean> mCassettes;
    private Map<String, Boolean> mArchivedCassettes;
    private Map<String, Boolean> mJourneys;
    private String mRank;
    private String mBand;
    private long mFoundCount;
    private long mHiddenCount;
    private long mRatingCount;
    private long mLetterPounderCount;
    private long mPaparazziCount;
    private long mBaitsFoundCount;
    private long mLastFindTimestamp;
    private long mLastHideTimestamp;
    private long mLongestHoarded;
    private Map<String, Number> mShadow;
    private Map<String, Number> mAutotune;
    private Map<String, ArrayList> mDrumRoll;
    private long mLoggedIn;
    private long mDaysLogged;
    private boolean mStatus;

    private DatabaseReference mRef;

    private Bitmap mAvatarImage;

    private ArrayList<UserPoints> mUserPoints;
    private int mPoints;
    private DatabaseReference mPointsRef;
    private ChildEventListener mPointsListener;

    private Map<String, UserRank> mUserRanks;
    private UserRank mCurrentRank;
    private DatabaseReference mRanksRef;
    private ChildEventListener mRanksListener;

    private Map<String, UserBadges> mUserBadges;
    private DatabaseReference mBadgesRef;
    private ChildEventListener mBadgesListener;

    private Band mBandModel;

    private DatabaseReference mCassettesRef;
    private ChildEventListener mCassettesListener;
    private ArrayList<Cassette> mCassettesArrayList;
    private Map<String, Cassette> mCassettesByKey;
    private ArrayList<CassetteModel> mCassetteModelsArrayList;

    private DatabaseReference mArchivedCassettesRef;
    private ChildEventListener mArchivedCassettesListener;
    private ArrayList<Cassette> mArchivedCassettesArrayList;
    private Map<String, Cassette> mArchivedCassettesByKey;
    private ArrayList<CassetteModel> mArchivedCassetteModelsArrayList;

    private ArrayList<Cassette> mWarningCassettes;
    private ArrayList<Cassette> mHoardedCassettes;

    private Map<String, Integer> mPointsByCassetteKey;
    private Map<String, Boolean> mHiddenByCassetteKey;
    private Map<String, Long> mDateFoundByCassetteKey;
    private Map<String, Integer> mRatingByCassetteKey;
    private Map<String, String> mCassetteModelKeyByCassetteKey;
    private Map<String, Integer> mPointsByArchivedCassetteKey;
    private Map<String, Long> mDateFoundByArchivedCassetteKey;
    private Map<String, Integer> mRatingByArchivedCassetteKey;
    private Map<String, String> mCassetteModelKeyByArchivedCassetteKey;

    public User(String email, String username, String firstname, String lastname, String avatarImageRef, Map<String, String> ranks, String rank, String band, boolean status, long lastFindTimestamp, long lastHideTimestamp, String key) {
        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mEmail = email;
        mUsername = username;
        mFirstname = firstname;
        mLastname = lastname;
        mAvatarImageRef = avatarImageRef;
        mRanks = ranks;
        mRank = rank;
        mBand = band;
        mBandModel = null;
        mCassettes = new HashMap<>();
        mBadges = new HashMap<>();
        mNotifications = new HashMap<>();
        mArchivedCassettes = new HashMap<>();
        mJourneys = new HashMap<>();
        mShadow = new HashMap<>();
        mAutotune = new HashMap<>();
        mDrumRoll = new HashMap<>();
        mLoggedIn = 0;
        mDaysLogged = 0;
        mStatus = status;

        mRef = null;

        mAvatarImage = null;

        mPoints = 0;
        mFoundCount = 0;
        mHiddenCount = 0;
        mRatingCount = 0;
        mBaitsFoundCount = 0;
        mLetterPounderCount = 0;
        mPaparazziCount = 0;
        mLastFindTimestamp = lastFindTimestamp;
        mLastHideTimestamp = lastHideTimestamp;
        mLongestHoarded = 0;
        mUserPoints = new ArrayList<>();
        mUserRanks = new HashMap<>();
        mUserBadges = new HashMap<>();

        mCassettesArrayList = new ArrayList<>();
        mCassettesByKey = new HashMap<>();
        mCassetteModelsArrayList = new ArrayList<>();
        mArchivedCassettesArrayList = new ArrayList<>();
        mArchivedCassettesByKey = new HashMap<>();
        mArchivedCassetteModelsArrayList = new ArrayList<>();

        mWarningCassettes = new ArrayList<>();
        mHoardedCassettes = new ArrayList<>();

        mPointsByCassetteKey = new HashMap<>();
        mHiddenByCassetteKey = new HashMap<>();
        mDateFoundByCassetteKey = new HashMap<>();
        mRatingByCassetteKey = new HashMap<>();
        mCassetteModelKeyByCassetteKey = new HashMap<>();
        mPointsByArchivedCassetteKey = new HashMap<>();
        mDateFoundByArchivedCassetteKey = new HashMap<>();
        mRatingByArchivedCassetteKey = new HashMap<>();
        mCassetteModelKeyByArchivedCassetteKey = new HashMap<>();

        setImage();
        setBand();

        setCassettesListener();
        setArchivedCassettesListener();
        setPointsListener();
        setRanksListener();
        setBadgesListener();
        checkNotifications();
        setLoggedIn();
    }

    public User(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("email") ||
                !value.containsKey("username") ||
                !value.containsKey("status") ) {
            throw new RequiredValueMissing("User is missing required values " + mKey);
        }

        mEmail = (String) value.get("email");
        mUsername = (String) value.get("username");
        mFirstname = (String) value.get("firstname");
        mLastname = (String) value.get("lastname");
        mAvatarImageRef = (String) value.get("avatarImageRef");
        mRanks = (value.get("ranks") == null) ?
                new HashMap<String, String>() : (HashMap<String, String>) value.get("ranks");
        mRank = (value.get("rank") == null) ?
                "Roadie" : (String) value.get("rank");
        mBand = (String) value.get("band");
        mCassettes = (value.get("cassettes") == null) ?
                new HashMap<String, Boolean>() : (HashMap<String, Boolean>) value.get("cassettes");
        mBadges = (value.get("badges") == null) ?
                new HashMap<String, String>() : (HashMap<String, String>) value.get("badges");
        mNotifications = (value.get("notifications") == null) ?
                new HashMap<String, Boolean>() : (HashMap<String, Boolean>) value.get("notifications");
        mArchivedCassettes = (value.get("archivedCassettes") == null) ?
                new HashMap<String, Boolean>() : (HashMap<String, Boolean>) value.get("archivedCassettes");
        mJourneys = (value.get("journeys") == null) ?
                new HashMap<String, Boolean>() : (HashMap<String, Boolean>) value.get("journeys");
        mStatus = (boolean) value.get("status");
        mFoundCount = (value.get("foundCount") == null) ?
                0 : ((Number) value.get("foundCount")).longValue();
        mHiddenCount = (value.get("hiddenCount") == null) ?
                0 : ((Number) value.get("hiddenCount")).longValue();
        mRatingCount = (value.get("ratingCount") == null) ?
                0 : ((Number) value.get("ratingCount")).longValue();
        mLetterPounderCount = (value.get("letter_pounder_count") == null) ?
                0 : ((Number) value.get("letter_pounder_count")).longValue();
        mPaparazziCount = (value.get("paparazzi_count") == null) ?
                0 : ((Number) value.get("paparazzi_count")).longValue();
        mBaitsFoundCount = (value.get("baitsFoundCount") == null) ?
                0 : ((Number) value.get("baitsFoundCount")).longValue();
        mLastFindTimestamp = (value.get("lastFindTimestamp") == null) ?
                0 : ((Number) value.get("lastFindTimestamp")).longValue() * 1000; // convert to milliseconds
        mLastHideTimestamp = (value.get("lastHideTimestamp") == null) ?
                0 : ((Number) value.get("lastHideTimestamp")).longValue() * 1000; // convert to milliseconds
        mLongestHoarded = 0;
        mShadow = (value.get("shadow") == null) ? new HashMap<String, Number>() : (HashMap<String, Number>) value.get("shadow");
        mAutotune = (value.get("auto-tune") == null) ? new HashMap<String, Number>() : (HashMap<String, Number>) value.get("auto-tune");
        mDrumRoll = (value.get("drum-roll") == null) ? new HashMap<String, ArrayList>() : (HashMap<String, ArrayList>) value.get("drum-roll");
        mLoggedIn = (value.get("loggedIn") == null) ? 0 : ((Number) value.get("loggedIn")).longValue() * 1000; // convert to milliseconds
        mDaysLogged = (value.get("daysLogged") == null) ? 0 : ((Number) value.get("daysLogged")).longValue();

        mRef = snapshot.getRef();

        mAvatarImage = null;

        mUserPoints = new ArrayList<>();
        mUserRanks = new HashMap<>();
        mPoints = 0;
        mUserBadges = new HashMap<>();

        mCassettesArrayList = new ArrayList<>();
        mCassettesByKey = new HashMap<>();
        mCassetteModelsArrayList = new ArrayList<>();
        mArchivedCassettesArrayList = new ArrayList<>();
        mArchivedCassettesByKey = new HashMap<>();
        mArchivedCassetteModelsArrayList = new ArrayList<>();

        mWarningCassettes = new ArrayList<>();
        mHoardedCassettes = new ArrayList<>();

        mPointsByCassetteKey = new HashMap<>();
        mHiddenByCassetteKey = new HashMap<>();
        mDateFoundByCassetteKey = new HashMap<>();
        mRatingByCassetteKey = new HashMap<>();
        mCassetteModelKeyByCassetteKey = new HashMap<>();
        mPointsByArchivedCassetteKey = new HashMap<>();
        mDateFoundByArchivedCassetteKey = new HashMap<>();
        mRatingByArchivedCassetteKey = new HashMap<>();
        mCassetteModelKeyByArchivedCassetteKey = new HashMap<>();

        setImage();
        setBand();

        setCassettesListener();
        setArchivedCassettesListener();
        setPointsListener();
        setRanksListener();
        setBadgesListener();
        checkNotifications();
        setLoggedIn();
    }

    public void save() {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(mKey);
        }
        mRef.setValue(this.toMap());    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("email", mEmail);
        result.put("username", mUsername);
        result.put("lastname", mLastname);
        result.put("firstname", mFirstname);
        result.put("avatarImageRef", mAvatarImageRef);
        result.put("ranks", mRanks);
        result.put("badges", mBadges);
        result.put("notifications", mNotifications);
        result.put("rank", mRank);
        result.put("band", mBand);
        result.put("cassettes", mCassettes);
        result.put("archivedCassettes", mArchivedCassettes);
        result.put("journeys", mJourneys);
        result.put("status", mStatus);
        result.put("foundCount", mFoundCount);
        result.put("hiddenCount", mHiddenCount);
        result.put("ratingCount", mRatingCount);
        result.put("letter_pounder_count", mLetterPounderCount);
        result.put("paparazzi_count", mPaparazziCount);
        result.put("baitsFoundCount", mBaitsFoundCount);
        result.put("lastFindTimestamp", mLastFindTimestamp / 1000);
        result.put("lastHideTimestamp", mLastHideTimestamp / 1000);
        result.put("shadow", mShadow);
        result.put("auto-tune", mAutotune);
        result.put("drum-roll", mDrumRoll);
        result.put("loggedIn", mLoggedIn / 1000);
        result.put("daysLogged", mDaysLogged);

        return result;
    }

    public void logout() {
        stopPointsListner();
        stopBadgesListener();
        stopRanksListener();
        stopCassetteListener();
        stopArchivedCassettesListener();

        mKey = "";
        mEmail = "";
        mUsername = "";
        mFirstname = "";
        mLastname = "";
        mAvatarImageRef = "";
        mAvatarImage = null;
        mRanks = new HashMap<>();
        mRank = "";
        mBand = "";
        mBandModel = null;
        mCassettes = new HashMap<>();
        mBadges = new HashMap<>();
        mNotifications = new HashMap<>();
        mArchivedCassettes = new HashMap<>();
        mJourneys = new HashMap<>();
        mStatus = false;

        mUserPoints = new ArrayList<>();
        mUserRanks = new HashMap<>();
        mPoints = 0;
        mUserBadges = new HashMap<>();

        mRef = null;

        mPoints = 0;
        mFoundCount = 0;
        mHiddenCount = 0;
        mRatingCount = 0;
        mLetterPounderCount = 0;
        mPaparazziCount = 0;
        mBaitsFoundCount = 0;
        mLastFindTimestamp = 0;
        mLastHideTimestamp = 0;
        mLongestHoarded = 0;
        mUserPoints = new ArrayList<>();
        mUserRanks = new HashMap<>();
        mUserBadges = new HashMap<>();

        mCassettesArrayList = new ArrayList<>();
        mCassettesByKey = new HashMap<>();
        mCassetteModelsArrayList = new ArrayList<>();
        mArchivedCassettesArrayList = new ArrayList<>();
        mArchivedCassetteModelsArrayList = new ArrayList<>();

        mWarningCassettes = new ArrayList<>();
        mHoardedCassettes = new ArrayList<>();

        mPointsByCassetteKey = new HashMap<>();
        mHiddenByCassetteKey = new HashMap<>();
        mDateFoundByCassetteKey = new HashMap<>();
        mRatingByCassetteKey = new HashMap<>();
        mCassetteModelKeyByCassetteKey = new HashMap<>();
        mPointsByArchivedCassetteKey = new HashMap<>();
        mDateFoundByArchivedCassetteKey = new HashMap<>();
        mRatingByArchivedCassetteKey = new HashMap<>();
        mCassetteModelKeyByArchivedCassetteKey = new HashMap<>();

        AudioPlayerManager.getInstance().stop();
        AudioPlayerManager.getInstance().setPlaylist(new ArrayList<CassetteModel>());
        AudioPlayerManager.getInstance().setIndex(0);
    }

    public void setLoggedIn() {
        long now = new Date().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(now));
        int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTime(new Date(mLoggedIn));
        int thenDay = calendar.get(Calendar.DAY_OF_MONTH);

        if (now - mLoggedIn < 1000 * 60 * 60 * 24 && nowDay != thenDay) {
            mDaysLogged += 1;
        } else if (nowDay != thenDay) {
            mDaysLogged = 0;
        }

        mLoggedIn = now;

        if (mDaysLogged > 6) {
            if (getBadges().containsKey("autoplay") == false) {
                UserBadges userBadges = new UserBadges(mKey, "autoplay", null, null, null);
                userBadges.save();
                setBadge(userBadges);
                save();
            }
        }
    }

    public void setImage() {
        if (mAvatarImageRef != null && mAvatarImage == null) {
            final StorageReference reference = FirebaseStorage.getInstance().getReference()
                    .child("avatars")
                    .child(mKey)
                    .child("thumbnail");

            try {
                byte[] data = CacheManager.getInstance().getImageData(reference);
                mAvatarImage = BitmapFactory.decodeByteArray(data, 0, data.length);

            } catch (Exception e) {
                reference.getBytes(30 * 1024 * 1024)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                mAvatarImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                CacheManager.getInstance().cacheImageData(bytes, reference);
                            }
                        });
            }
        }
    }

    private void setBand() {
        if (mBandModel != null || mBand == null) {
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("bands")
                .child(mBand);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    mBandModel = new Band(dataSnapshot);
                    mBandModel.setCassetteModel(null);
                    mBandModel.setImage();

                } catch (RequiredValueMissing e) {
                    Log.w("HB", "Couldn't get band for reference " + mBand);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void setPointsListener() {
        if (mPointsListener == null) {
            mPointsRef = FirebaseDatabase.getInstance().getReference()
                    .child("userpoints")
                    .child(mKey);

            mPointsListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        final UserPoints userPoints = new UserPoints(dataSnapshot);
                        mPoints += userPoints.getValue();
                        mUserPoints.add(userPoints);

                        // If the user gains enough points, give them a new rank
                        if (mCurrentRank == null)  {
                            return;
                        }

                        try {
                            Rank rank = RanksStore.getInstance()
                                    .getRank(mCurrentRank.getRank());

                            if (mPoints >= rank.getPoints()) {
                                final Rank newRank = RanksStore.getInstance()
                                        .getRank(rank.getLevel() + 1);

                                if (mRanks.get(newRank.getKey()) == null) {
                                    newRank.setImage(new ModelPropertySetCallback() {
                                        @Override
                                        public void onSuccess() {
                                            UserRank newUserRank = new UserRank(mKey, newRank.getKey(), null);
                                            newUserRank.save();

                                            mRanks.put(newRank.getKey(), newUserRank.getKey());

                                            ArrayList<NotificationItem> items = new ArrayList<>();
                                            items.add(new NotificationItem(newRank.getKey(), true));
                                            mNotifications.put(newRank.getName(), true);

                                            if (newRank.getLevel() == 13) {
                                                if (mBadges.get("level-head") == null) {
                                                    try {
                                                        Badge badge = BadgesStore.getInstance().getBadge("level-head");
                                                        UserBadges userBadges = new UserBadges(mKey, badge.getKey(), userPoints.getCassetteRef(), userPoints.getJourneyRef(), null);
                                                        userBadges.save();
                                                        mBadges.put(userBadges.getBadge(), userBadges.getKey());
                                                        save();
                                                        items.add(new NotificationItem(badge.getKey(), false));
                                                        mNotifications.put(badge.getName(), true);
                                                    } catch (BadgeNotFound e) {
                                                        Log.w("HB", e.getLocalizedMessage());
                                                    }
                                                }
                                            }

                                            save();

                                            if (items.size() > 0) {
                                                Intent intent = new Intent(HBApplication.getInstance(), BadgeNotificationActivity.class);
                                                intent.putParcelableArrayListExtra("arraylist_extra", items);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                HBApplication.getInstance().startActivity(intent);
                                            }
                                        }

                                        @Override
                                        public void onFail(String error) {
                                            Log.w("HB", error);
                                        }
                                    });
                                }
                            }

                        } catch (RankNotFound e) {
                            Log.w("HB", e.getLocalizedMessage());
                        }

                    } catch (RequiredValueMissing e) {
                        Log.w("HB", e.getLocalizedMessage());
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

            mPointsRef.addChildEventListener(mPointsListener);
        }
    }

    private void stopPointsListner() {
        if (mPointsListener != null) {
            mPointsRef.removeEventListener(mPointsListener);
            mPointsListener = null;
            mPointsRef = null;
        }
    }

    private void setRanksListener() {
        if (mRanksListener == null) {
            mRanksRef = FirebaseDatabase.getInstance().getReference()
                    .child("userranks")
                    .child(mKey);

            mRanksListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        UserRank userRank = new UserRank(dataSnapshot);

                        mUserRanks.put(userRank.getRank(), userRank);

                        Rank rank = RanksStore.getInstance().getRank(userRank.getRank());
                        rank.setImage(null);

                        if (mCurrentRank == null) {
                            mCurrentRank = userRank;
                            mRank = rank.getName();

                        } else {
                            String rankKey = mRank.toLowerCase().replace(" ", "-");
                            Rank currentRank = RanksStore.getInstance().getRank(rankKey);
                            if (rank.getLevel() > currentRank.getLevel()) {
                                mCurrentRank = userRank;
                                mRank = rank.getName();
                            }
                        }

                    } catch (Exception e) {
                        Log.w("HB", e.getLocalizedMessage());
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

            mRanksRef.addChildEventListener(mRanksListener);
        }
    }

    public void stopRanksListener() {
        if (mRanksListener != null)  {
            mRanksRef.removeEventListener(mRanksListener);
            mRanksListener = null;
            mRanksRef = null;
        }
    }

    private void setBadgesListener() {

        if (mBadgesListener == null) {
            mBadgesRef = FirebaseDatabase.getInstance().getReference()
                    .child("userbadges")
                    .child(mKey);

            mBadgesListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        UserBadges userBadges = new UserBadges(dataSnapshot);
                        mUserBadges.put(userBadges.getBadge(), userBadges);
                        Badge badge = BadgesStore.getInstance().getBadge(userBadges.getBadge());
                        badge.setImage(null);
                    } catch (Exception e) {
                        Log.w("HB", e.getLocalizedMessage());
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

            mBadgesRef.addChildEventListener(mBadgesListener);
        }
    }

    private void stopBadgesListener() {
        if (mBadgesRef != null) {
            mBadgesRef.removeEventListener(mBadgesListener);
            mBadgesListener = null;
            mBadgesRef = null;
        }
    }

    private void checkNotifications() {
        DatabaseReference userranks = FirebaseDatabase.getInstance().getReference()
                .child("userranks").child(mKey);
        final DatabaseReference userbadges = FirebaseDatabase.getInstance().getReference()
                .child("userbadges").child(mKey);

        userranks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final ArrayList<NotificationItem> items = new ArrayList<>();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> value = (Map<String, Object>) childSnapshot.getValue();
                    String name = (String) value.get("rank");
                    try {
                        Rank rank = RanksStore.getInstance().getRank(name);
                        if (getNotifications().containsKey(rank.getKey()) == false) {
                            items.add(new NotificationItem(rank.getKey(), false));
                            setNotification(rank.getKey());
                        }

                    } catch (RankNotFound e) {
                        Log.w("HB", e.getLocalizedMessage());
                    }
                }

                userbadges.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Map<String, Object> value = (Map<String, Object>) childSnapshot.getValue();
                            String name = (String) value.get("badge");
                            try {
                                Badge badge = BadgesStore.getInstance().getBadge(name);
                                if (getNotifications().containsKey(badge.getKey()) == false) {
                                    items.add(new NotificationItem(badge.getKey(), false));
                                    setNotification(badge.getKey());
                                }

                            } catch (BadgeNotFound e) {
                                Log.w("HB", e.getLocalizedMessage());
                            }
                        }

                        save();

                        if (items.size() > 0) {
                            Intent intent = new Intent(HBApplication.getInstance(), BadgeNotificationActivity.class);
                            intent.putParcelableArrayListExtra("arraylist_extra", items);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            HBApplication.getInstance().startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setCassettesListener()  {
        if (mCassettesListener == null) {
            mCassettesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(mKey)
                    .child("cassettes");

            mCassettesListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final boolean inCassetteBox = (boolean) dataSnapshot.getValue();

                    FirebaseDatabase.getInstance().getReference()
                            .child("cassettes")
                            .child(dataSnapshot.getKey())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    try {
                                        if (dataSnapshot.exists()) {
                                            Cassette cassette = new Cassette(dataSnapshot);
                                            mCassettesArrayList.add(cassette);
                                            mCassettesByKey.put(cassette.getKey(), cassette);
                                            mHiddenByCassetteKey.put(cassette.getKey(), inCassetteBox);

                                            getFoundInfo(cassette);
                                            fetchModel(cassette);
                                        }

                                    } catch (RequiredValueMissing e) {
                                        Log.w("HB", e.getLocalizedMessage());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w("HB", databaseError.getMessage());
                                }
                            });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    boolean inCassetteBox = (boolean) dataSnapshot.getValue();
                    boolean inCassetteBoxPrevious = mHiddenByCassetteKey.get(dataSnapshot.getKey());

                    if (inCassetteBox == inCassetteBoxPrevious) return;

                    if (inCassetteBox == false) {
                        Cassette cassette = getCassetteByKey(dataSnapshot.getKey());
                        if (mHoardedCassettes.contains(cassette)) {
                            mHoardedCassettes.remove(cassette);
                        }
                        if (mWarningCassettes.contains(cassette)) {
                            mWarningCassettes.remove(cassette);
                        }

                        setLongestHoarded();
                    }

                    mHiddenByCassetteKey.put(dataSnapshot.getKey(), inCassetteBox);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Cassette removingCassette = null;

                    for (Cassette cassette : mCassettesArrayList) {
                        if (cassette.getKey().equals(dataSnapshot.getKey())) {
                            removingCassette = cassette;
                        }
                    }

                    if (removingCassette == null) {
                        return;
                    }

                    AudioPlayerManager.getInstance()
                            .removeFromPlaylist(removingCassette.getCassetteModel());
                    mCassettesArrayList.remove(removingCassette);
                    mCassettesByKey.remove(removingCassette.getKey());
                    mHiddenByCassetteKey.remove(removingCassette.getKey());
                    mPointsByCassetteKey.remove(removingCassette.getKey());
                    mDateFoundByCassetteKey.remove(removingCassette.getKey());
                    mRatingByCassetteKey.remove(removingCassette.getKey());
                    if (mHoardedCassettes.contains(removingCassette)) {
                        mHoardedCassettes.remove(removingCassette);
                    }
                    if (mWarningCassettes.contains(removingCassette)) {
                        mWarningCassettes.remove(removingCassette);
                    }

                    setLongestHoarded();

                    String modelKey = mCassetteModelKeyByCassetteKey.get(removingCassette.getKey());
                    mCassetteModelKeyByCassetteKey.remove(removingCassette.getKey());

                    CassetteModel cassetteModel = null;

                    for (CassetteModel model : mCassetteModelsArrayList) {
                        if (model.getKey().equals(modelKey)) {
                            cassetteModel = model;
                            break;
                        }
                    }

                    if (cassetteModel != null) {
                        mCassetteModelsArrayList.remove(cassetteModel);
                        mCassetteModelKeyByCassetteKey.remove(removingCassette.getKey());

                        // TODO: Remove model from AudioPlayerManager playlist
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
        }

        mCassettesRef.addChildEventListener(mCassettesListener);
    }

    private void getFoundInfo(final Cassette cassette) {
        boolean first = true;

//        if (mBadges.containsKey("jailbreak")) {
//            first = false;
//        }

        final boolean isFirst = first;

        ChildEventListener listener = new ChildEventListener() {
            boolean innerFirst = isFirst;

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    FoundInfo foundInfo = new FoundInfo(dataSnapshot);

                    if (innerFirst) {

                        if (isHoldingCassette(cassette.getKey())) {
                            long diff = new Date().getTime() - foundInfo.getDateFound();
                            if (diff > 7 * 24 * 60 * 60 * 1000) {
                                mHoardedCassettes.add(cassette);

                                if (getBadges().containsKey("hoarder") == false) {
                                    UserBadges userBadges = new UserBadges(getKey(), "hoarder", cassette.getKey(), null, null);
                                    userBadges.save();
                                    setBadge(userBadges);
                                    save();
                                }
                            } else if (diff > 3 * 24 * 60 * 60 * 1000) {
                                mWarningCassettes.add(cassette);
                            }

                            if (diff > mLongestHoarded) {
                                mLongestHoarded = diff;
                            }
                        }

                        mDateFoundByCassetteKey.put(cassette.getKey(), foundInfo.getDateFound());
                        mRatingByCassetteKey.put(cassette.getKey(), foundInfo.getRatingGiven());

                        innerFirst = false;
                    }

                    int newPoints = foundInfo.getPointsEarned();
                    if (mPointsByCassetteKey.get(cassette.getKey()) != null) {
                        newPoints += mPointsByCassetteKey.get(cassette.getKey());
                    }

                    mPointsByCassetteKey.put(cassette.getKey(), newPoints);

                } catch (RequiredValueMissing e) {
                    Log.w("HB", e.getLocalizedMessage());
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

        FirebaseDatabase.getInstance().getReference()
                .child("foundInfos")
                .child(mKey)
                .child(cassette.getKey())
                .orderByChild("dateFound")
                .addChildEventListener(listener);
    }

    private void fetchModel(final Cassette cassette) {

        cassette.setCassetteModel(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                cassette.getCassetteModel().downloadArt();
//                cassette.getCassetteModel().setSong(new ModelPropertySetCallback() {
//                    @Override
//                    public void onSuccess() {
//                        cassette.getCassetteModel().getSong().downloadSong(new ModelPropertySetCallback() {
//                            @Override
//                            public void onSuccess() {}
//
//                            @Override
//                            public void onFail(String error) {
//                                Log.w("HB", error);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onFail(String error) {
//                        Log.w("HB", error);
//                    }
//                });
                mCassetteModelsArrayList.add(cassette.getCassetteModel());
                mCassetteModelKeyByCassetteKey.put(
                        cassette.getKey(), cassette.getCassetteModel().getKey());

                AudioPlayerManager.getInstance().addToPlaylist(cassette.getCassetteModel());
            }

            @Override
            public void onFail(String error) {
                Log.w("HB", error);
            }
        });
    }

    public void stopCassetteListener() {
        if (mCassettesListener != null) {
            mCassettesRef.removeEventListener(mCassettesListener);
            mCassettesListener = null;
            mCassettesRef = null;
        }
    }

    public void setArchivedCassettesListener() {
        if (mArchivedCassettesListener == null) {
            mArchivedCassettesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(mKey)
                    .child("archivedCassettes");

            mArchivedCassettesListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final boolean isNotDeleted = (boolean) dataSnapshot.getValue();

                    if (isNotDeleted == false) {
                        return;
                    }

                    FirebaseDatabase.getInstance().getReference()
                            .child("cassettes")
                            .child(dataSnapshot.getKey())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    try {
                                        Cassette cassette = new Cassette(dataSnapshot);
                                        mArchivedCassettesArrayList.add(cassette);
                                        mArchivedCassettesByKey.put(cassette.getKey(), cassette);

                                        getArchivedFoundInfo(cassette);
                                        fetchArchivedModel(cassette);

                                    } catch (RequiredValueMissing e) {
                                        Log.w("HB", e.getLocalizedMessage());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w("HB", databaseError.getMessage());
                                }
                            });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    boolean isNotDeleted = (boolean) dataSnapshot.getValue();

                    if (isNotDeleted == false) {

                        Cassette deletingCassette = null;
                        for (Cassette cassette : mArchivedCassettesArrayList) {
                            if (cassette.getKey().equals(deletingCassette.getKey())) {
                                deletingCassette = cassette;
                                break;
                            }
                        }

                        if (deletingCassette == null) {
                            return;
                        }

                        mArchivedCassettesArrayList.remove(deletingCassette);
                        mPointsByArchivedCassetteKey.remove(deletingCassette.getKey());
                        mDateFoundByArchivedCassetteKey.remove(deletingCassette.getKey());
                        mRatingByArchivedCassetteKey.remove(deletingCassette.getKey());

                        String modelKey = mCassetteModelKeyByArchivedCassetteKey.get(deletingCassette.getKey());
                        CassetteModel deletingCassetteModel = null;

                        for (CassetteModel model : mArchivedCassetteModelsArrayList) {
                            if (model.getKey().equals(modelKey)) {
                                deletingCassetteModel = model;
                            }
                        }

                        if (deletingCassetteModel != null) {
                            mArchivedCassetteModelsArrayList.remove(deletingCassetteModel);
                            mCassetteModelKeyByArchivedCassetteKey.remove(deletingCassette.getKey());
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Cassette deletingCassette = null;
                    for (Cassette cassette : mArchivedCassettesArrayList) {
                        if (cassette.getKey().equals(dataSnapshot.getKey())) {
                            deletingCassette = cassette;
                            break;
                        }
                    }

                    if (deletingCassette == null) {
                        return;
                    }

                    AudioPlayerManager.getInstance()
                            .removeFromPlaylist(deletingCassette.getCassetteModel());
                    mArchivedCassettesArrayList.remove(deletingCassette);
                    mPointsByArchivedCassetteKey.remove(deletingCassette.getKey());
                    mDateFoundByArchivedCassetteKey.remove(deletingCassette.getKey());
                    mRatingByArchivedCassetteKey.remove(deletingCassette.getKey());

                    String modelKey = mCassetteModelKeyByArchivedCassetteKey.get(deletingCassette.getKey());
                    CassetteModel deletingCassetteModel = null;

                    for (CassetteModel model : mArchivedCassetteModelsArrayList) {
                        if (model.getKey().equals(modelKey)) {
                            deletingCassetteModel = model;
                        }
                    }

                    if (deletingCassetteModel != null) {
                        mArchivedCassetteModelsArrayList.remove(deletingCassetteModel);
                        mCassetteModelKeyByArchivedCassetteKey.remove(deletingCassette.getKey());
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
        }

        mArchivedCassettesRef.addChildEventListener(mArchivedCassettesListener);
    }
    private void getArchivedFoundInfo(final Cassette cassette) {
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    FoundInfo foundInfo = new FoundInfo(dataSnapshot);

                    int newPoints = foundInfo.getPointsEarned();
                    if (mPointsByArchivedCassetteKey.get(cassette.getKey()) != null) {
                        newPoints += mPointsByArchivedCassetteKey.get(cassette.getKey());
                    }

                    mPointsByArchivedCassetteKey.put(cassette.getKey(), newPoints);
                    mDateFoundByArchivedCassetteKey.put(cassette.getKey(), foundInfo.getDateFound());
                    mRatingByArchivedCassetteKey.put(cassette.getKey(), foundInfo.getRatingGiven());

                } catch (RequiredValueMissing e) {
                    Log.w("HB", e.getLocalizedMessage());
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

        FirebaseDatabase.getInstance().getReference()
                .child("foundInfos")
                .child(mKey)
                .child(cassette.getKey())
                .orderByChild("dateFound")
                .addChildEventListener(listener);
    }

    private void fetchArchivedModel(final Cassette cassette) {

        cassette.setCassetteModel(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                cassette.getCassetteModel().downloadArt();
                mArchivedCassetteModelsArrayList.add(cassette.getCassetteModel());
                mCassetteModelKeyByArchivedCassetteKey.put(
                        cassette.getKey(), cassette.getCassetteModel().getKey());
                AudioPlayerManager.getInstance().addToPlaylist(cassette.getCassetteModel());
            }

            @Override
            public void onFail(String error) {
                Log.w("HB", error);
            }
        });
    }

    private void stopArchivedCassettesListener() {
        if (mArchivedCassettesListener != null) {
            mArchivedCassettesRef.removeEventListener(mArchivedCassettesListener);
            mArchivedCassettesRef = null;
            mArchivedCassettesListener = null;
        }
    }

    public ArrayList<Cassette> getHiddenCassettes() {
        ArrayList<Cassette> result = new ArrayList<>();

        for (Cassette cassette : mCassettesArrayList) {
            if (mHiddenByCassetteKey.get(cassette.getKey()) == false) {
                result.add(cassette);
            }
        }

        return result;
    }

    public ArrayList<Cassette> getUnhiddenCassettes() {
        ArrayList<Cassette> result = new ArrayList<>();

        for (Cassette cassette : mCassettesArrayList) {
            if (mHiddenByCassetteKey.get(cassette.getKey())) {
                result.add(cassette);
            }
        }

        return result;
    }

    public ArrayList<Cassette> getCassettesByDate() {
        ArrayList<Cassette> result = new ArrayList<>();
        for (Cassette cassette : mCassettesArrayList) {
            int index = 0;

            if (mDateFoundByCassetteKey.get(cassette.getKey()) == null) {
                result.add(index, cassette);
                continue;
            }

            long dateFound = mDateFoundByCassetteKey.get(cassette.getKey());
            for (Cassette resultCassette : result) {
                long resultDateFound = mDateFoundByCassetteKey.get(resultCassette.getKey());
                if (dateFound > resultDateFound) {
                    break;
                }
                index += 1;
            }
            result.add(index, cassette);
        }
        return result;
    }

    public ArrayList<Cassette> getCassettesByPoints() {
        ArrayList<Cassette> result = new ArrayList<>();
        for (Cassette cassette : mCassettesArrayList) {
            int index = 0;

            if (mPointsByCassetteKey.get(cassette.getKey()) == null) {
                result.add(index, cassette);
                continue;
            }

            int points = mPointsByCassetteKey.get(cassette.getKey());
            for (Cassette resultCassette : result) {
                int resultPoints = mPointsByCassetteKey.get(resultCassette.getKey());
                if (points > resultPoints) {
                    break;
                }
                index += 1;
            }
            result.add(index, cassette);
        }
        return result;
    }

    public ArrayList<Cassette> getCassettesByRating() {
        ArrayList<Cassette> result = new ArrayList<>();
        for (Cassette cassette : mCassettesArrayList) {
            int index = 0;

            if (mRatingByCassetteKey.get(cassette.getKey()) == null) {
                result.add(index, cassette);
                continue;
            }

            int rating = mRatingByCassetteKey.get(cassette.getKey());
            for (Cassette resultCassette : result) {
                int resultRating = mRatingByCassetteKey.get(resultCassette.getKey());
                if (rating > resultRating) {
                    break;
                }
                index += 1;
            }
            result.add(index, cassette);
        }
        for (Cassette cassette : result) {
            Log.w("TEST", cassette.getNumber() + mRatingByCassetteKey.get(cassette.getKey()));
        }
        return result;
    }

    public ArrayList<Cassette> getCassettesByNumber() {
        ArrayList<Cassette> result = new ArrayList<>();
        for (Cassette cassette : mCassettesArrayList) {
            int number = Integer.parseInt(cassette.getNumber());
            int index = 0;
            for (Cassette resultCassette : result) {
                int resultNumber = Integer.parseInt(resultCassette.getNumber());
                if (number > resultNumber) {
                    break;
                }
                index += 1;
            }
            result.add(index, cassette);
        }
        return result;
    }

    public ArrayList<Cassette> getArchivedCassettesByDate() {
        ArrayList<Cassette> result = new ArrayList<>();
        for (Cassette cassette : mArchivedCassettesArrayList) {
            long dateFound = mDateFoundByArchivedCassetteKey.get(cassette.getKey());
            int index = 0;
            for (Cassette resultCassette : result) {
                long resultDateFound = mDateFoundByArchivedCassetteKey.get(resultCassette.getKey());
                if (dateFound > resultDateFound) {
                    break;
                }
                index += 1;
            }
            result.add(index, cassette);
        }
        return result;
    }

    public ArrayList<Cassette> getArchivedCassettesByPoints() {
        ArrayList<Cassette> result = new ArrayList<>();
        for (Cassette cassette : mArchivedCassettesArrayList) {
            int points = mPointsByArchivedCassetteKey.get(cassette.getKey());
            int index = 0;
            for (Cassette resultCassette : result) {
                int resultPoints = mPointsByArchivedCassetteKey.get(resultCassette.getKey());
                if (points > resultPoints) {
                    break;
                }
                index += 1;
            }
            result.add(index, cassette);
        }
        return result;
    }

    public ArrayList<Cassette> getArchivedCassettesByRating() {
        ArrayList<Cassette> result = new ArrayList<>();
        for (Cassette cassette : mArchivedCassettesArrayList) {
            int rating = mRatingByArchivedCassetteKey.get(cassette.getKey());
            int index = 0;
            for (Cassette resultCassette : result) {
                int resultRating = mRatingByArchivedCassetteKey.get(resultCassette.getKey());
                if (rating > resultRating) {
                    break;
                }
                index += 1;
            }
            result.add(index, cassette);
        }
        return result;
    }

    public ArrayList<Cassette> getArchivedCassettesByNumber() {
        ArrayList<Cassette> result = new ArrayList<>();
        for (Cassette cassette : mArchivedCassettesArrayList) {
            int number = Integer.parseInt(cassette.getNumber());
            int index = 0;
            for (Cassette resultCassette : result) {
                int resultNumber = Integer.parseInt(resultCassette.getNumber());
                if (number > resultNumber) {
                    break;
                }
                index += 1;
            }
            result.add(index, cassette);
        }
        return result;
    }

    public void addRank(Rank rank, UserRank userRank) {
        mRanks.put(rank.getKey(), userRank.getKey());
    }

    public BestDayPointsResult getBestPointsDay() {

        int bestPoints = 0;
        int currentPoints = 0;
        int bestDay = 0;
        int currentDay = Calendar.SUNDAY;
        String bestMonth = "";
        int currentMonth = 0;
        int bestYear = 0;
        int currentYear = 0;

        Collections.sort(mUserPoints, new TimestampComparator());

        for (UserPoints points : mUserPoints) {
            Date date = new Date(points.getTimestamp());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            if (currentDay != cal.get(Calendar.DAY_OF_MONTH)) {
                currentDay = cal.get(Calendar.DAY_OF_MONTH);
                currentMonth = cal.get(Calendar.MONTH);
                currentYear = cal.get(Calendar.YEAR);
                currentPoints = 0;
            }

            currentPoints += points.getValue();

            if (currentPoints > bestPoints) {
                bestPoints = currentPoints;
                bestDay = currentDay;
                bestMonth = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
                bestYear = currentYear;
            }
        }

        return new BestDayPointsResult(bestPoints, bestDay, bestMonth, bestYear);
    }

    public BestMonthPointsResult getBestPointsMonth() {

        int bestPoints = 0;
        int currentPoints = 0;
        String bestMonth = "";
        int currentMonth = 0;
        int bestYear = 0;
        int currentYear = 0;

        Collections.sort(mUserPoints, new TimestampComparator());

        for (UserPoints points : mUserPoints) {
            Date date = new Date(points.getTimestamp());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            if (currentMonth != cal.get(Calendar.MONTH)) {
                currentMonth = cal.get(Calendar.MONTH);
                currentYear = cal.get(Calendar.YEAR);
                currentPoints = 0;
            }

            currentPoints += points.getValue();

            if (currentPoints > bestPoints) {
                bestPoints = currentPoints;
                bestMonth = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
                bestYear = currentYear;
            }
        }

        return new BestMonthPointsResult(bestPoints, bestMonth, bestYear);
    }

    public BestCassetteResult getBestCassettePoints() {

        if (mCassettesArrayList.size() == 0 && mArchivedCassettesArrayList.size() == 0) {
            Log.e("HB", "Cassettes array empty");
            return new BestCassetteResult(0, "No Cassettes Found");
        }

        String cassetteModelId = "";
        String cassetteNumber = "";
        int mostPoints = 0;
        boolean archived = false;

        for (Cassette cassette : mCassettesArrayList) {
            if (mPointsByCassetteKey.get(cassette.getKey()) != null) {
                int points = mPointsByCassetteKey.get(cassette.getKey());

                if (points > mostPoints) {
                    mostPoints = points;
                    cassetteModelId = cassette.getCassetteRef();
                    cassetteNumber = cassette.getNumber();
                }
            }
        }

        for (Cassette cassette : mArchivedCassettesArrayList) {
            if (mPointsByArchivedCassetteKey.get(cassette.getKey()) != null) {
                int points = mPointsByArchivedCassetteKey.get(cassette.getKey());

                if (points > mostPoints) {
                    mostPoints = points;
                    cassetteModelId = cassette.getCassetteRef();
                    cassetteNumber = cassette.getNumber();
                    archived = true;
                }
            }
        }

        CassetteModel bestModel = null;

        if (archived) {
            for (CassetteModel model : mArchivedCassetteModelsArrayList) {
                if (model.getKey().equals(cassetteModelId)) {
                    bestModel = model;
                    break;
                }
            }
        } else {
            for (CassetteModel model : mCassetteModelsArrayList) {
                if (model.getKey().equals(cassetteModelId)) {
                    bestModel = model;
                    break;
                }
            }
        }

        if (bestModel == null) {
            Log.e("ERR", "Couldn't find model");
            return new BestCassetteResult(0, "No Cassettes Found");
        }

        return new BestCassetteResult(mostPoints, "#" + cassetteNumber + " " + bestModel.getName());
    }

    private void setLongestHoarded() {
        if (mHoardedCassettes.isEmpty()) {
            mLongestHoarded = 0;
        } else {
            for (Cassette cassette : mHoardedCassettes) {
                long timestamp = getDateFoundByCassetteKey(cassette.getKey());
                long diff = new Date().getTime() - timestamp;
                if (diff > mLongestHoarded) {
                    mLongestHoarded = diff;
                }
            }
        }
    }

    public DatabaseReference getRef() { return mRef; }

    public String getKey() { return mKey; }

    public String getUsername() { return mUsername; }

    public long getRatingCount() { return mRatingCount; }

    public long getFoundCount() { return mFoundCount; }

    public String getAvatarImageRef() { return mAvatarImageRef; }

    public Bitmap getAvatarImage() { return mAvatarImage; }

    public UserRank getCurrentRank() { return mCurrentRank; }

    public UserRank getUserRank(String key) { return mUserRanks.get(key); }

    public UserBadges getUserBadge(String key) { return mUserBadges.get(key); }

    public int getPoints() { return mPoints; }

    public Map<String, Boolean> getCassettes() { return mCassettes; }

    public Map<String, Boolean> getArchivedCassettes() { return mArchivedCassettes; }

    public ArrayList<Cassette> getCassettesArrayList() { return mCassettesArrayList; }

    public ArrayList<CassetteModel> getCassetteModelsArrayList() { return mCassetteModelsArrayList; }

    public Cassette getCassetteByKey(String key) { return mCassettesByKey.get(key); }

    public Cassette getArchivedCassetteByKey(String key) { return mArchivedCassettesByKey.get(key); }

    public ArrayList<Cassette> getArchivedCassettesArrayList() { return mArchivedCassettesArrayList; }

    public String getPointsString() { return ((Integer) mPoints).toString(); }

    public Map<String, String> getRanks() { return mRanks; }

    public Map<String, String> getBadges() { return mBadges; }

    public Map<String, Boolean> getNotifications() { return mNotifications; }

    public long getHiddenCount() { return mHiddenCount; }

    public long getLetterPounderCount() { return mLetterPounderCount; }

    public long getPaparazziCount() { return mPaparazziCount; }

    public boolean hasBadge(String key) { return mUserBadges.get(key) != null; }

    public Boolean isHoldingCassette(String key) { return mCassettes.get(key); }

    public Map<String, UserBadges> getUserBadges() { return mUserBadges; }

    public Long getDateFoundByCassetteKey(String key) { return mDateFoundByCassetteKey.get(key); }

    public Long getDateFoundByArchivedCassetteKey(String key) { return mDateFoundByArchivedCassetteKey.get(key); }

    public Long getLastHideTimestamp() { return mLastHideTimestamp; }

    public Long getLastFindTimestamp() { return mLastFindTimestamp; }

    public long getLongestHoarded() { return mLongestHoarded; }

    public ArrayList<Cassette> getHoardedCassettes() { return mHoardedCassettes; }

    public int getRatingByCassetteKey(String key) { return mRatingByCassetteKey.get(key); }

    public Map<String, Number> getShadow() { return mShadow; }

    public Map<String, Number> getAutotune() { return mAutotune; }

    public Map<String, ArrayList> getDrumRoll() { return mDrumRoll; }

    public Long getLoggedIn() { return mLoggedIn; }

    public Long getDaysLogged() { return mDaysLogged; }

    public int getPoints(Cassette cassette) throws CassetteNotFound {
        if (mPointsByCassetteKey.containsKey(cassette.getKey()) != false) {
            return mPointsByCassetteKey.get(cassette.getKey());
        }
        if (mPointsByArchivedCassetteKey.containsKey(cassette.getKey()) != false) {
            return mPointsByArchivedCassetteKey.get(cassette.getKey());
        }

        throw new CassetteNotFound("Cassette not found in box");
    }

    public boolean inCassetteBoxByCassetteKey(String key) { return mHiddenByCassetteKey.get(key); }

    public void archiveCassette(String key) {
        mCassettes.put(key, null);
        mArchivedCassettes.put(key, true);
        save();
    }

    public void deleteCassette(String key) {
        mCassettes.put(key, null);
        mArchivedCassettes.put(key, false);
        save();
    }

    public void unArchiveCassette(String key) {
        mCassettes.put(key, false);
        mArchivedCassettes.put(key, null);
        save();
    }

    public void setAvatarImageRef(String avatarImageRef) { mAvatarImageRef = avatarImageRef; }

    public void setAvatarImage(Bitmap avatarImage) { mAvatarImage = avatarImage; }

    public void setUsername(String username) { mUsername = username; }

    public void setLastFindTimestamp(long timestamp) { mLastFindTimestamp = timestamp; }

    public void setLastHideTimestamp(long timestamp) { mLastHideTimestamp = timestamp; }

    public void setBadge(UserBadges userBadges) {
        mBadges.put(userBadges.getBadge(), userBadges.getKey());
    }

    public void setNotification(String name) {
        mNotifications.put(name, true);
    }

    public void setCassette(Cassette cassette, Boolean inCassetteBox) {
        mCassettes.put(cassette.getKey(), inCassetteBox);
    }

    public void setLoggedIn(long loggedIn) {
        mLoggedIn = loggedIn;
    }

    public void incrementDaysLogged() {
        mDaysLogged += 1;
    }

    public void setDrumRoll(Map<String, ArrayList> drumRoll) {
        mDrumRoll = drumRoll;
    }

    public void setShadow(Map<String, Number> shadow) {
        mShadow = shadow;
    }

    public void setAutotune(Map<String, Number> autotune) {
        mAutotune = autotune;
    }

    public void removeArchivedCassette(Cassette cassette) {
        mArchivedCassettes.remove(cassette.getKey());
    }

    public boolean userHasFoundCassette(Cassette cassette) {
        return (mCassettes.containsKey(cassette.getKey()) || mArchivedCassettes.containsKey(cassette.getKey()));
    }

    public void incrementRatingCount() { mRatingCount += 1; }

    public void incrementFoundCount() { mFoundCount += 1; }

    public void incrementHiddenCount() { mHiddenCount += 1; }

    public void incrementLetterPounderCount() { mLetterPounderCount += 1; }

    public void incrementPaparazziCount() { mPaparazziCount += 1; }

    private class TimestampComparator implements Comparator<UserPoints> {
        @Override
        public int compare(UserPoints o1, UserPoints o2) {
            return ((Long) o1.getTimestamp()).compareTo(o2.getTimestamp());
        }
    }

    public class BestDayPointsResult {
        int points;
        int day;
        String month;
        int year;

        BestDayPointsResult(int points, int day, String month, int year) {
            this.points = points;
            this.day = day;
            this.month = month;
            this.year = year;
        }

        public int getPoints() {
            return points;
        }

        public int getDay() {
            return day;
        }

        public String getMonth() {
            return month;
        }

        public int getYear() {
            return year;
        }
    }

    public class BestMonthPointsResult {
        int points;
        String month;
        int year;

        BestMonthPointsResult(int points, String month, int year) {
            this.points = points;
            this.month = month;
            this.year = year;
        }

        public int getPoints() {
            return points;
        }

        public String getMonth() {
            return month;
        }

        public int getYear() {
            return year;
        }
    }

    public class BestCassetteResult {
        int points;
        String name;

        BestCassetteResult(int points, String name) {
            this.points = points;
            this.name = name;
        }

        public int getPoints() {
            return points;
        }

        public String getName() {
            return name;
        }
    }
}
