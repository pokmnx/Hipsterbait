package com.hipsterbait.android.models;


import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;

import java.util.HashMap;
import java.util.Map;

public class Points {

    private String mKey;

    private String mName;
    private String mDescription;
    private int mValue;
    private int mIncrement;
    private boolean mStatus;

    private DatabaseReference mRef;

    public Points(String name, String description, int value, int increment, boolean status, String key) {

        mName = name;
        mDescription = description;
        mValue = value;
        mIncrement = increment;
        mStatus = status;

        mRef = null;
    }

    public Points(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("name") ||
                !value.containsKey("description") ||
                !value.containsKey("value") ||
                !value.containsKey("increment") ||
                !value.containsKey("status")) {
            throw new RequiredValueMissing("Points is missing required values " + mKey);
        }

        mName = (String) value.get("name");
        mDescription = (String) value.get("description");
        mValue = ((Number) value.get("value")).intValue();
        mIncrement = ((Number) value.get("increment")).intValue();
        mStatus = (boolean) value.get("status");

        mRef = snapshot.getRef();
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("name", mName);
        result.put("description", mDescription);
        result.put("value", mValue);
        result.put("increment", mIncrement);
        result.put("status", mStatus);

        return result;
    }

    public String getKey() { return mKey; }

    public String getName() { return mName; }

    public int getValue() { return mValue; }

    public long getIncrement() { return mIncrement; }
}
