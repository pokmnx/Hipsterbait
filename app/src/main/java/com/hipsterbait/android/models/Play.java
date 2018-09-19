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

public class Play {

    private String mKey;
    private String mUserRef;
    private String mSongRef;
    private long mTimestamp;
    private boolean mStatus;
    private String mCity;

    private DatabaseReference mRef;

    private Location mLocation;

    Play(String userRef, String songRef, boolean status, String city, String key) {
        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mUserRef = userRef;
        mSongRef = songRef;
        mTimestamp = new Date().getTime();
        mStatus = status;
        mCity = city;

        mRef = null;

        mLocation = null;
    }

    Play(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("status") ||
                !value.containsKey("userRef") ||
                !value.containsKey("songRef") ||
                !value.containsKey("timestamp") ) {
            throw new RequiredValueMissing("Play is missing required values " + mKey);
        }

        mUserRef = (String) value.get("userRef");
        mSongRef = (String) value.get("songRef");
        mTimestamp = ((Number) value.get("timestamp")).longValue() * 1000; // Convert to milliseconds
        mStatus = (boolean) value.get("status");
        mCity = (String) value.get("city");

        mRef = snapshot.getRef();

        pullLocation(null);
    }

    public void save() {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference().child("plays").child(mSongRef).push();
            mKey = this.mRef.getKey();
        }

        mRef.setValue(this.toMap());

        DatabaseReference geoRef = FirebaseDatabase.getInstance().getReference().child("geo").child("plays");
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
        result.put("timestamp", mTimestamp / 1000); // Convert to seconds
        result.put("status", mStatus);
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
                Location androidLocation = new Location("firebase");
                androidLocation.setLatitude(location.latitude);
                androidLocation.setLongitude(location.longitude);
                setLocation(androidLocation);

                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (callback != null) callback.onFail("Coudn't get location for Play " +
                        databaseError.getMessage());
            }
        });
    }
}
