package com.hipsterbait.android.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

public class CassetteModel implements Parcelable, Comparable<CassetteModel> {

    private String mKey;
    private String mName;
    private boolean mStatus;
    private String mDescription;
    private String mSongRef;
    private String mCassetteArtRef;
    private String mCoverArtRef;
    private long mAnnouncement;
    private String mHeardThemFirst;

    private DatabaseReference mRef;

    private String mCassetteArtURL;
    private String mCoverArtURL;
    private Uri mCoverArtURI;

    private Song mSong;

    CassetteModel(String name, boolean status, String description, String songRef, String cassetteArtRef, String coverArtRef, long announcement, String heardThemFirst, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mName = name;
        mStatus = status;
        mDescription = description;
        mSongRef = songRef;
        mCassetteArtRef= cassetteArtRef;
        mCoverArtRef = coverArtRef;
        mAnnouncement = announcement;
        mHeardThemFirst = heardThemFirst;

        mRef = null;
    }

    CassetteModel(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("name") ||
                !value.containsKey("status") ||
                !value.containsKey("description") ||
                !value.containsKey("songRef") ||
                !value.containsKey("cassetteArtRef") ||
                !value.containsKey("coverArtRef") ||
                !value.containsKey("announcement") ) {
            throw new RequiredValueMissing("Cassette Model is missing required values " + mKey);
        }

        mName = (String) value.get("name");
        mStatus = (boolean) value.get("status");
        mDescription = (String) value.get("description");
        mSongRef = (String) value.get("songRef");
        mCassetteArtRef = (String) value.get("cassetteArtRef");
        mCoverArtRef = (String) value.get("coverArtRef");
        mAnnouncement = ((Number) value.get("announcement")).longValue() * 1000; // convert to milliseconds
        mHeardThemFirst = (String) value.get("heardThemFirst");

        mRef = snapshot.getRef();
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("name", mName);
        result.put("status", mStatus);
        result.put("description", mDescription);
        result.put("songRef", mSongRef);
        result.put("cassetteArtRef", mCassetteArtRef);
        result.put("coverArtRef", mCoverArtRef);
        result.put("announcement", mAnnouncement);
        result.put("heardThemFirst", mHeardThemFirst);

        return result;
    }

    public void save() {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("cassetteModels")
                    .child(mKey);
        }

        mRef.setValue(this.toMap());
    }

    public void downloadArt() {

        setCassetteArt(null);
        setCoverArt(null);
    }

    public void setCassetteArt(final ModelPropertySetCallback callback) {
        if (mCassetteArtURL != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("cassetteArt")
                .child(mCassetteArtRef);

        try {
            reference.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mCassetteArtURL = uri.toString();
                            if (callback != null) callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("HB", e.getLocalizedMessage());
                            if (callback != null) callback.onFail(e.getLocalizedMessage());
                        }
                    });
        } catch (RejectedExecutionException e) {
            Log.e("HB", e.getLocalizedMessage());
        }
    }

    public void setCoverArt(final ModelPropertySetCallback callback) {

        if (mCoverArtURL != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("coverArt")
                .child(mCoverArtRef);

        try {
            reference.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mCoverArtURL = uri.toString();
                            if (callback != null) callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("HB", e.getLocalizedMessage());
                            if (callback != null) callback.onFail(e.getLocalizedMessage());
                        }
                    });
        } catch (RejectedExecutionException e) {
            Log.e("HB", e.getLocalizedMessage());
        }
    }

    public void setSong(final ModelPropertySetCallback callback) {

        if (mSong != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("songs")
                .child(mSongRef);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    mSong = new Song(dataSnapshot);
                    if (callback != null) callback.onSuccess();

                } catch (RequiredValueMissing e) {
                    if (callback != null) callback.onFail(e.getLocalizedMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("HB",
                        "Failed to get download song for " + mKey);
                if (callback != null) callback.onFail(databaseError.getMessage());
            }
        });
    }

    public String getKey() {
        return mKey;
    }

    public String getName() { return mName; }

    public long getAnnouncement() { return mAnnouncement; }

    public String getDescription() { return mDescription; }

    public String getCassetteArtURL() { return mCassetteArtURL; }

    public String getCoverArtURL() { return mCoverArtURL; }

    public String getSongRef() { return mSongRef; }

    public Song getSong() { return mSong; }

    public String getHeardThemFirst() { return mHeardThemFirst; }

    public void setHeardThemFirst(String heardThemFirst) { mHeardThemFirst = heardThemFirst; }

    // PARCELABLE
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mKey);
        out.writeString(mName);
        out.writeByte((byte) (mStatus ? 1 : 0));
        out.writeString(mDescription);
        out.writeString(mSongRef);
        out.writeString(mCassetteArtRef);
        out.writeString(mCoverArtRef);
        out.writeLong(mAnnouncement);
        out.writeString(mHeardThemFirst);
        out.writeString(mRef.toString());
        out.writeString(mCassetteArtURL);
        out.writeString(mCoverArtURL);
    }

    public static final Parcelable.Creator<CassetteModel> CREATOR = new Parcelable.Creator<CassetteModel>() {
        public CassetteModel createFromParcel(Parcel in) {
            return new CassetteModel(in);
        }

        public CassetteModel[] newArray(int size) {
            return new CassetteModel[size];
        }
    };

    private CassetteModel(Parcel in) {
        mKey = in.readString();
        mName = in.readString();
        mStatus = in.readByte() != 0;
        mDescription = in.readString();
        mSongRef = in.readString();
        mCassetteArtRef = in.readString();
        mCoverArtRef = in.readString();
        mAnnouncement = in.readLong();
        mHeardThemFirst = in.readString();
        try {
            String path = new URL(in.readString()).getPath();
            path = path.replaceFirst("/", "");
            mRef = FirebaseDatabase.getInstance().getReference().child(path);
        } catch (MalformedURLException e) {
            mRef = null;
        }
        mCassetteArtURL = in.readString();
        mCoverArtURL = in.readString();

        // setSong(null);
    }

    public int compareTo(@NonNull CassetteModel o) {
        if (mSong == null) {
            return 1;
        }

        if (o.mSong == null) {
            return -1;
        }

        return mSong.getAverageRating() - o.mSong.getAverageRating();
    }
}
