package com.hipsterbait.android.models;

import android.content.Context;
import android.text.format.DateFormat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserRank {
    private String mKey;
    private String mUserRef;
    private String mRank;
    private long mTimestamp;

    private DatabaseReference mRef;

    public UserRank(String userRef, String rank, String key) {
        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mUserRef = userRef;
        mRank = rank;
        mTimestamp = new Date().getTime();

        mRef = null;
    }

    public UserRank(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("userRef") ||
                !value.containsKey("rank") ||
                !value.containsKey("timestamp") ) {
            throw new RequiredValueMissing("UserRank is missing required values " + mKey);
        }

        mUserRef = (String) value.get("userRef");
        mRank = (String) value.get("rank");
        mTimestamp = ((Number) value.get("timestamp")).longValue() * 1000; // convert to milliseconds

        mRef = snapshot.getRef();
    }

    public void save() {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("userranks")
                    .child(mUserRef)
                    .push();
            mKey = this.mRef.getKey();
        }

        mRef.setValue(this.toMap());
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("userRef", mUserRef);
        result.put("rank", mRank);
        result.put("timestamp", mTimestamp / 1000); // Convert to seconds

        return result;
    }

    public String getKey() {
        return mKey;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getDateString() { return DateFormat.format("MM/dd/yy", mTimestamp).toString(); }

    public String getRank() {
        return mRank;
    }
}
