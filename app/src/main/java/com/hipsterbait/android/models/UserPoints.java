package com.hipsterbait.android.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserPoints implements Parcelable {

    private String mKey;
    private String mUserKey;
    private String mPointKey;
    private long mValue;
    private String mCassetteRef;
    private String mJourneyRef;
    private long mTimestamp;

    private DatabaseReference mRef;

    public UserPoints(String userKey, String pointKey, int value, String cassetteRef, String journeyRef, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mUserKey = userKey;
        mPointKey = pointKey;
        mValue = value;
        mCassetteRef = cassetteRef;
        mJourneyRef = journeyRef;
        mTimestamp = new Date().getTime();

        mRef = null;
    }

    public UserPoints(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("userKey") ||
                !value.containsKey("pointKey") ||
                !value.containsKey("value") ||
                !value.containsKey("timestamp") ) {
            throw new RequiredValueMissing("UserPoints is missing required values " + mKey);
        }

        mUserKey = (String) value.get("userKey");
        mPointKey = (String) value.get("pointKey");
        mValue = ((Number) value.get("value")).longValue();
        mCassetteRef = (String) value.get("cassetteRef");
        mJourneyRef = (String) value.get("journeyRef");
        mTimestamp = ((Number) value.get("timestamp")).longValue() * 1000; // convert to milliseconds

        mRef = snapshot.getRef();
    }

    public void save() {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("userpoints")
                    .child(mUserKey)
                    .push();
            mKey = this.mRef.getKey();
        }

        mRef.setValue(this.toMap());
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("userKey", mUserKey);
        result.put("pointKey", mPointKey);
        result.put("value", mValue);
        result.put("cassetteRef", mCassetteRef);
        result.put("journeyRef", mJourneyRef);
        result.put("timestamp", mTimestamp / 1000); // Convert to seconds

        return result;
    }

    public long getValue() {
        return mValue;
    }

    public long getTimestamp() { return mTimestamp; }

    public String getCassetteRef() {
        return mCassetteRef;
    }

    public String getJourneyRef() {
        return mJourneyRef;
    }

    public String getPointsKey() { return mPointKey; }

    public void setValue(long value) { mValue = value; }

    // PARCELABLE
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mKey);
        out.writeString(mUserKey);
        out.writeString(mPointKey);
        out.writeLong(mValue);
        out.writeString(mCassetteRef);
        out.writeString(mJourneyRef);
        out.writeLong(mTimestamp);
    }

    public static final Parcelable.Creator<UserPoints> CREATOR = new Parcelable.Creator<UserPoints>() {
        public UserPoints createFromParcel(Parcel in) {
            return new UserPoints(in);
        }

        public UserPoints[] newArray(int size) {
            return new UserPoints[size];
        }
    };

    private UserPoints(Parcel in) {
        mKey = in.readString();
        mUserKey = in.readString();
        mPointKey = in.readString();
        mValue = in.readLong();
        mCassetteRef = in.readString();
        mJourneyRef = in.readString();
        mTimestamp = in.readLong();
    }
}
