package com.hipsterbait.android.models;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;

import java.util.HashMap;
import java.util.Map;

public class FoundInfo {

    private String mKey;
    private long mDateFound;
    private boolean mHidden;
    private int mPointsEarned;
    private int mRatingGiven;
    private String mJourneyRef;
    private DatabaseReference mRef;

    public FoundInfo(long dateFound, boolean hidden, int pointsEarned, int ratingGiven, String journeyRef, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mDateFound = dateFound;
        mHidden = hidden;
        mPointsEarned = pointsEarned;
        mRatingGiven = ratingGiven;
        mJourneyRef = journeyRef;

        mRef = null;
    }

    FoundInfo(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("dateFound") ||
                !value.containsKey("hidden") ||
                !value.containsKey("pointsEarned") ||
                !value.containsKey("ratingGiven") ) {
            throw new RequiredValueMissing("FoundInfo missing required values " + this.mKey);
        }

        mDateFound = ((Number) value.get("dateFound")).longValue() * 1000 * -1; // Convert to milliseconds
        mHidden = (boolean) value.get("hidden");
        mPointsEarned = ((Number) value.get("pointsEarned")).intValue();
        mRatingGiven = ((Number) value.get("ratingGiven")).intValue();
        mJourneyRef = (String) value.get("journeyRef");

        mRef = snapshot.getRef();
    }

    public void setRef(User user, Cassette cassette) {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference().child("foundInfos").child(user.getKey()).child(cassette.getKey()).push();

        } else {
            Log.d("HB", "Ref already set");
        }
    }

    public void save() {
        if (mRef != null) {
            mRef.setValue(this.toMap());

        } else {
            Log.d("HB", "Ref already set");
        }
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("dateFound", (mDateFound * -1) / 1000); // Convert to seconds
        result.put("hidden", mHidden);
        result.put("pointsEarned", mPointsEarned);
        result.put("ratingGiven", mRatingGiven);
        result.put("journeyRef", mJourneyRef);

        return result;
    }

    public int getPointsEarned() {
        return mPointsEarned;
    }

    public long getDateFound() {
        return mDateFound;
    }

    public int getRatingGiven() {
        return mRatingGiven;
    }
}
