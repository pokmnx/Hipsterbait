package com.hipsterbait.android.models;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Cassette implements Parcelable {

    private String mKey;
    private String mCassetteRef;
    private boolean mStatus;
    private boolean mHidden;
    private boolean mFlagged;
    private boolean mPhysical;
    private String mNumber;

    private DatabaseReference mRef;

    private ArrayList<Journey> mJourneys;
    private DatabaseReference mJourneysRef, mHiddenRef;
    private ChildEventListener mJourneysListener;
    private ValueEventListener mHiddenListener;

    private CassetteModel mCassetteModel;

    Cassette(String cassetteRef, boolean status, boolean hidden, boolean flagged, boolean physical, String number, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mCassetteRef = cassetteRef;
        mStatus = status;
        mHidden = hidden;
        mFlagged = flagged;
        mPhysical = physical;
        mNumber = number;

        mRef = null;

        mJourneys = new ArrayList<>();

        startJourneysListener();
        startHiddenListener();
    }

    public Cassette(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("cassetteRef") ||
                !value.containsKey("status") ||
                !value.containsKey("hidden") ||
                !value.containsKey("flagged") ||
                !value.containsKey("number") ||
                !value.containsKey("physical") ) {
            throw new RequiredValueMissing("Cassette is missing required values " + mKey);
        }

        mCassetteRef = (String) value.get("cassetteRef");
        mStatus = (boolean) value.get("status");
        mHidden = (boolean) value.get("hidden");
        mFlagged = (boolean) value.get("flagged");
        mPhysical = (boolean) value.get("physical");
        mNumber = (String) value.get("number");

        mRef = snapshot.getRef();

        mJourneys = new ArrayList<>();

        startJourneysListener();
        startHiddenListener();
    }

    public void save(Location location) {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("cassettes")
                    .push();
            mKey = mRef.getKey();
        }

        mRef.setValue(this.toMap());

        startJourneysListener();

        DatabaseReference geoRef = FirebaseDatabase.getInstance().getReference()
                .child("geo")
                .child("cassettes");
        GeoFire geoFire = new GeoFire(geoRef);

        if (location == null) {
            geoFire.removeLocation(mKey);

            mHidden = false;

        } else {
            GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
            geoFire.setLocation(mKey, geoLocation);

            mHidden = true;
        }
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("cassetteRef", mCassetteRef);
        result.put("status", mStatus);
        result.put("hidden", mHidden);
        result.put("flagged", mFlagged);
        result.put("physical", mPhysical);
        result.put("number", mNumber);

        return result;
    }

    public String getKey() {
        return mKey;
    }

    public void startHiddenListener() {
        if (mHiddenListener == null) {
            mHiddenRef = getRef().child("hidden");

            mHiddenListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mHidden = (Boolean) dataSnapshot.getValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

            mHiddenRef.addValueEventListener(mHiddenListener);
        }
    }

    public void startJourneysListener() {

        if (mJourneysListener == null) {
            mJourneysRef = FirebaseDatabase.getInstance().getReference()
                    .child("journeys")
                    .child(mKey);

            mJourneysListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        mJourneys.add(new Journey(dataSnapshot));
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

            mJourneysRef.addChildEventListener(mJourneysListener);
        }
    }

    public void stopHiddenListener() {
        if (mHiddenRef != null && mHiddenListener != null)
            mHiddenRef.removeEventListener(mHiddenListener);
        mHiddenListener = null;
    }

    public void stopJourneysListener() {
        if (mJourneysRef != null && mJourneysListener != null)
            mJourneysRef.removeEventListener(mJourneysListener);
        mJourneysListener = null;
    }

    public void setCassetteModel(final ModelPropertySetCallback callback) {

        if (mCassetteModel != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("cassetteModels")
                .child(mCassetteRef);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    mCassetteModel = new CassetteModel(dataSnapshot);
                    if (callback != null) callback.onSuccess();

                } catch (RequiredValueMissing e) {
                    if (callback != null) callback.onFail(e.getLocalizedMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (callback != null) callback.onFail(databaseError.getMessage());
            }
        });
    }

    public DatabaseReference getRef() { return mRef; }

    public String getCassetteRef() {
        return mCassetteRef;
    }

    public String getNumber() { return mNumber; }

    public ArrayList<Journey> getJourneys() { return mJourneys; }

    public CassetteModel getCassetteModel() { return mCassetteModel; }

    public boolean isPhysical() { return mPhysical; }

    public boolean isHidden() { return mHidden; }

    public boolean isFlagged() { return mFlagged; }

    public void setHidden(Boolean hidden) { mHidden = hidden; }

    public static boolean isHidden(Context context, DataSnapshot snapshot) {
        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if ((boolean) value.get(context.getString(R.string.hidden))) {
            return true;
        }
        return false;
    }

    public static boolean isPhysical(Context context, DataSnapshot snapshot) {
        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if ((boolean) value.get(context.getString(R.string.physical))) {
            return true;
        }
        return false;
    }

    public static String getCassetteModelRef(Context context, DataSnapshot snapshot) {
        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        return (String) value.get(context.getString(R.string.cassette_ref));
    }

    // PARCELABLE
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mKey);
        out.writeString(mCassetteRef);
        out.writeByte((byte) (mStatus ? 1 : 0));
        out.writeByte((byte) (mHidden ? 1 : 0));
        out.writeByte((byte) (mFlagged ? 1 : 0));
        out.writeByte((byte) (mPhysical ? 1 : 0));
        out.writeString(mNumber);
        out.writeString(mRef.toString());
        out.writeString(mJourneysRef.toString());
        out.writeParcelable(mCassetteModel, 0);
        stopHiddenListener();
        stopJourneysListener();
    }

    public static final Parcelable.Creator<Cassette> CREATOR = new Parcelable.Creator<Cassette>() {
        public Cassette createFromParcel(Parcel in) {
            return new Cassette(in);
        }

        public Cassette[] newArray(int size) {
            return new Cassette[size];
        }
    };

    private Cassette(Parcel in) {
        mKey = in.readString();
        mCassetteRef = in.readString();
        mStatus = in.readByte() != 0;
        mHidden = in.readByte() != 0;
        mFlagged = in.readByte() != 0;
        mPhysical = in.readByte() != 0;
        mNumber = in.readString();
        try {
            String path = new URL(in.readString()).getPath();
            path = path.replaceFirst("/", "");
            mRef = FirebaseDatabase.getInstance().getReference().child(path);
        } catch (MalformedURLException e) {
            mRef = null;
        }
        try {
            String path = new URL(in.readString()).getPath();
            path = path.replaceFirst("/", "");
            mJourneysRef = FirebaseDatabase.getInstance().getReference().child(path);
        } catch (MalformedURLException e) {
            mJourneysRef = null;
        }
        mCassetteModel = in.readParcelable(getClass().getClassLoader());
        mJourneys = new ArrayList<>();
        startHiddenListener();
        startJourneysListener();
    }
}
