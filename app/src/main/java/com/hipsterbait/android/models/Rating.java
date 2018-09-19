package com.hipsterbait.android.models;

import android.content.Context;
import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.other.HBLocationManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Rating {
    private String mKey;
    private String mUserRef;
    private String mSongRef;
    private int mRating;
    private long mTimestamp;
    private String mCity;

    private DatabaseReference mRef;

    private Location mLocation;

    public Rating(String userRef, String songRef, int rating, String city, String key) {
        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mUserRef = userRef;
        mSongRef = songRef;
        mRating = rating;
        mTimestamp = new Date().getTime();
        mCity = city;

        mRef = null;
    }

    public Rating(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("userRef") ||
                !value.containsKey("songRef") ||
                !value.containsKey("timestamp") ||
                !value.containsKey("rating") ) {
            throw new RequiredValueMissing("Rating is missing required values " + mKey);
        }

        mUserRef = (String) value.get("userRef");
        mSongRef = (String) value.get("songRef");
        mRating = ((Number) value.get("rating")).intValue();
        mTimestamp = ((Number) value.get("timestamp")).longValue() * 1000; // Convert to milliseconds
        mCity = (String) value.get("city");

        mRef = snapshot.getRef();
    }

    public void save() {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference().child("ratings").child(mSongRef).child(mUserRef);
            mKey = this.mRef.getKey();
        }

        mRef.setValue(this.toMap());

        DatabaseReference geoRef = FirebaseDatabase.getInstance().getReference().child("geo").child("ratings");
        GeoFire geoFire = new GeoFire(geoRef);

        Location location = HBLocationManager.getInstance().getCurrentLocation();

        if (location != null) {
            GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
            geoFire.setLocation(mKey, geoLocation);
        }
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("userRef", mUserRef);
        result.put("songRef", mSongRef);
        result.put("rating", mRating);
        result.put("timestamp", mTimestamp / 1000); // Convert to seconds
        result.put("city", mCity);

        return result;
    }

    private void setLocation(Location location) {
        mLocation = location;
    }

    public void pullLocation(final ModelPropertySetCallback callback) {

        if (mLocation != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        DatabaseReference geoRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("geo")
                .child("plays");

        GeoFire geoFire = new GeoFire(geoRef);

        geoFire.getLocation(mKey, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                Location androidLocation = new Location("gps");
                androidLocation.setLatitude(location.latitude);
                androidLocation.setLongitude(location.longitude);
                setLocation(androidLocation);

                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (callback != null) callback.onFail("Couldn't get location for journey " +
                        databaseError.getMessage());
            }
        });
    }

    public int getRating() {
        return mRating;
    }

    public String getUserRef() { return mUserRef; }

    public void setRating(int newRating) { mRating = newRating; }

    public void setTimestamp(long newTimestamp) { mTimestamp = newTimestamp; }
}
