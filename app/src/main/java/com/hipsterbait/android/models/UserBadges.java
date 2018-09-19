package com.hipsterbait.android.models;

import android.text.format.DateFormat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserBadges {
    private String mKey;
    private String mUserRef;
    private String mBadge;
    private String mCassetteRef;
    private String mJourneyRef;
    private long mTimestamp;

    private DatabaseReference mRef;

    public UserBadges(String userRef, String badge, String cassetteRef, String journeyRef, String key) {
        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mUserRef = userRef;
        mBadge = badge;
        mCassetteRef = cassetteRef;
        mJourneyRef = journeyRef;
        mTimestamp = new Date().getTime();

        mRef = null;
    }

    public UserBadges(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("userRef") ||
                !value.containsKey("badge") ||
                !value.containsKey("timestamp") ) {
            throw new RequiredValueMissing("User is missing required values " + mKey);
        }

        mUserRef = (String) value.get("userRef");
        mBadge = (String) value.get("badge");
        mCassetteRef = (String) value.get("cassetteRef");
        mJourneyRef = (String) value.get("journeyRef");
        mTimestamp = ((Number) value.get("timestamp")).longValue() * 1000; // convert to milliseconds

        mRef = snapshot.getRef();
    }

    public void save() {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("userbadges")
                    .child(mUserRef)
                    .push();
            mKey = this.mRef.getKey();
        }

        mRef.setValue(this.toMap());
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("userRef", mUserRef);
        result.put("badge", mBadge);
        result.put("cassetteRef", mCassetteRef);
        result.put("journeyRef", mJourneyRef);
        result.put("timestamp", mTimestamp / 1000); // Convert to seconds

        return result;
    }

    public String getKey() {
        return mKey;
    }

    public String getBadge() { return mBadge; }

    public long getTimestamp() { return mTimestamp; }

    public String getDateString() { return DateFormat.format("MM/dd/yy", mTimestamp).toString(); }
}
