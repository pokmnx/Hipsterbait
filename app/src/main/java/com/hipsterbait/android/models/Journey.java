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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Journey implements Parcelable {

    private String mKey;
    private String mAction;
    private String mAddress;
    private String mUserRef;
    private String mCassetteRef;
    private long mTimestamp;
    private double mElevation;
    private String mStreet;
    private String mCity;
    private String mRegional;
    private String mState;
    private String mCountry;
    private String mIATAIdentifier;
    private DatabaseReference mRef;

    private Location mLocation;

    public Journey(String action, String address, String userRef, String cassetteRef, double elevation, String street, String city, String regional, String state, String country, String IATAIdentifier, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mAction = action;
        mAddress = address;
        mUserRef = userRef;
        mCassetteRef = cassetteRef;
        mTimestamp = new Date().getTime();
        mElevation = elevation;
        mStreet = street;
        mCity = city;
        mRegional = regional;
        mState = state;
        mCountry = country;
        mIATAIdentifier = IATAIdentifier;

        mRef = null;

        mLocation = null;
    }

    public Journey(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("action") ||
                !value.containsKey("address") ||
                !value.containsKey("userRef") ||
                !value.containsKey("cassetteRef") ||
                !value.containsKey("elevation") ||
                !value.containsKey("timestamp")
                ) {
            throw new RequiredValueMissing("Journey is missing required values " + this.mKey);
        }

        mAction = (String) value.get("action");
        mAddress = (String) value.get("address");
        mUserRef = (String) value.get("userRef");
        mCassetteRef = (String) value.get("cassetteRef");
        mTimestamp = ((Number) value.get("timestamp")).longValue() * 1000; // Convert to milliseconds
        mElevation = ((Number) value.get("elevation")).doubleValue();
        mStreet = (String) value.get("street");
        mCity = (String) value.get("city");
        mRegional = (String) value.get("regional");
        mState = (String) value.get("state");
        mCountry = (String) value.get("country");
        mIATAIdentifier = (String) value.get("IATAIdentifier");

        mRef = snapshot.getRef();

        pullLocation(null);
    }

    public void save(Location location) {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference().child("journeys").child(mCassetteRef).push();
            mKey = mRef.getKey();
        }

        mRef.setValue(this.toMap());

        DatabaseReference geoRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("geo")
                .child("journeys");

        GeoFire geoFire = new GeoFire(geoRef);

        if (location != null) {
            GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
            geoFire.setLocation(mKey, geoLocation);
        }

        pullLocation(null);
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("action", mAction);
        result.put("address", mAddress);
        result.put("userRef", mUserRef);
        result.put("cassetteRef", mCassetteRef);
        result.put("timestamp", mTimestamp / 1000); // Convert to seconds
        result.put("elevation", mElevation);
        result.put("street", mStreet);
        result.put("city", mCity);
        result.put("regional", mRegional);
        result.put("state", mState);
        result.put("country", mCountry);
        result.put("IATAIdentifier", mIATAIdentifier);

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
                .child("journeys");

        GeoFire geoFire = new GeoFire(geoRef);

        geoFire.getLocation(mKey, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location == null) return;

                Location androidLocation = new Location("firebase");
                androidLocation.setLatitude(location.latitude);
                androidLocation.setLongitude(location.longitude);
                setLocation(androidLocation);

                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (callback != null) callback.onFail("Couldn't get location for Journey " +
                        databaseError.getMessage());
            }
        });
    }

    public String getKey() { return mKey; }

    public String getAction() { return mAction; }

    public double getElevation() { return mElevation; }

    public String getStreet() { return mStreet; }

    public String getCity() { return mCity; }

    public String getRegional() { return mRegional; }

    public String getState() { return mState; }

    public String getCountry() { return mCountry; }

    public String getIATAIdentifier() { return mIATAIdentifier; }

    public String getAddress() { return mAddress; }

    public String getUserRef() { return mUserRef; }

    public Location getLocation() { return mLocation; }

    public long getTimestamp() { return mTimestamp; }

    public String dateFormattedComplete() {
        return DateFormat.format("MMM d, yyyy HH:mm z", mTimestamp).toString();
    }

    public String dateFormattedMDY() {
        return DateFormat.format("MM/dd/yy", mTimestamp).toString();
    }

    public String dateFormattedVerbose() {
        return DateFormat.format("MMM d, yyyy", mTimestamp).toString();
    }

    // PARCELABLE
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mKey);
        out.writeString(mAction);
        out.writeString(mAddress);
        out.writeString(mUserRef);
        out.writeString(mCassetteRef);
        out.writeLong(mTimestamp);
        out.writeString(mCity);
        out.writeString(mRegional);
        out.writeString(mCountry);
        out.writeString(mIATAIdentifier);
        out.writeString(mRef.toString());
        out.writeDouble(mLocation.getLatitude());
        out.writeDouble(mLocation.getLongitude());
    }

    public static final Parcelable.Creator<Journey> CREATOR = new Parcelable.Creator<Journey>() {
        public Journey createFromParcel(Parcel in) {
            return new Journey(in);
        }

        public Journey[] newArray(int size) {
            return new Journey[size];
        }
    };

    private Journey(Parcel in) {
        mKey = in.readString();
        mAction = in.readString();
        mAddress = in.readString();
        mUserRef = in.readString();
        mCassetteRef = in.readString();
        mTimestamp = in.readLong();
        mCity = in.readString();
        mRegional = in.readString();
        mCountry = in.readString();
        mIATAIdentifier = in.readString();
        try {
            String path = new URL(in.readString()).getPath();
            path = path.replaceFirst("/", "");
            mRef = FirebaseDatabase.getInstance().getReference().child(path);
        } catch (MalformedURLException e) {
            mRef = null;
        }
        mLocation = new Location("gps");
        mLocation.setLatitude(in.readDouble());
        mLocation.setLongitude(in.readDouble());
    }
}
