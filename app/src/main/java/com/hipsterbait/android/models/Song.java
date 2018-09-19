package com.hipsterbait.android.models;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.other.CacheManager;

import android.net.Uri;
import java.util.Map;

public class Song {

    private String mKey;
    private String mName;
    private String mDescription;
    private String mFileRef;
    private String mBandRef;

    private DatabaseReference mRef;

    private Band mBand;
    private Uri mDataPath;
    private Uri mDownloadURI;

    private int mRatingsCount;
    private int mRatingsTotal;
    private float mAverageRating;
    private DatabaseReference mRatingsRef;
    private ChildEventListener mRatingsListener;

    Song(String name, String description, String fileRef, String bandRef, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mName = name;
        mDescription = description;
        mFileRef = fileRef;
        mBandRef = bandRef;

        mRef = null;
    }

    Song(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("name") ||
                !value.containsKey("description") ||
                !value.containsKey("fileRef") ||
                !value.containsKey("bandRef") ) {
            throw new RequiredValueMissing("Rating model is missing required values " + mKey);
        }

        mName = (String) value.get("name");
        mDescription = (String) value.get("description");
        mFileRef = (String) value.get("fileRef");
        mBandRef = (String) value.get("bandRef");

        mRef = snapshot.getRef();

        setRatingsListener();
    }

    private void deallocate() {
        mKey = "";
        mName = "";
        mDescription = "";
        mFileRef = "";
        mBandRef = "";
        mRef = null;

        mDownloadURI = null;

        if (mBand != null) {
            // mBand.deallocate();
            mBand = null;
        }

        mRatingsTotal = 0;
        mRatingsCount = 0;
        mAverageRating = 0;

        if (mRatingsListener != null) {
            // TODO: Remove observer?
            mRatingsRef.removeEventListener(mRatingsListener);

            mRatingsListener = null;
            mRatingsRef = null;
        }
    }

    private void setRatingsListener() {

        if (mRatingsListener == null) {
            mRatingsRef = FirebaseDatabase.getInstance().getReference()
                    .child("ratings")
                    .child(mKey);
            mRatingsListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    try {
                        Rating rating = new Rating(dataSnapshot);

                        mRatingsCount += 1;
                        mRatingsTotal += rating.getRating();
                        mAverageRating = mRatingsTotal / mRatingsCount;

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
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("HB", databaseError.getMessage());
                }
            };

            mRatingsRef.addChildEventListener(mRatingsListener);
        }
    }

    public void setBand(final ModelPropertySetCallback callback) {

        if (mBand != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        DatabaseReference bandRef = FirebaseDatabase.getInstance().getReference()
                .child("bands")
                .child(mBandRef);

        bandRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    Song.this.mBand = new Band(dataSnapshot);
                    if (callback != null) callback.onSuccess();

                } catch (RequiredValueMissing e) {
                    if (callback != null) callback.onFail(e.getLocalizedMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void downloadSong(final ModelPropertySetCallback callback) {

        final StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("songs")
                .child(mFileRef);

        try {
            mDataPath = CacheManager.getInstance().getSongData(storageRef);

            setDownloadURI(callback);

        } catch (Exception e) {

            storageRef.getBytes(30 * 1024 * 1024)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            mDataPath = CacheManager.getInstance().cacheSongData(bytes, storageRef);

                            if (mDataPath == null) {
                                if (callback != null) callback.onFail("Song cache error");
                                return;
                            }

                            setDownloadURI(callback);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (callback != null) callback.onFail(e.getLocalizedMessage());
                        }
                    });
        }
    }

    private void setDownloadURI(final ModelPropertySetCallback callback) {

        if (mDownloadURI != null) {

            if (callback != null) callback.onSuccess();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("songs")
                .child(mFileRef);

        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mDownloadURI = uri;
                        if (callback != null) callback.onSuccess();
                    }
                })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (callback != null) callback.onFail(e.getLocalizedMessage());
                }
            });
    }

    public String getDescription() { return mDescription; }

    public String getKey() { return mKey; }

    public String getName() { return mName; }

    public Band getBand() { return mBand; }

    public Uri getDataPath() { return mDataPath; }

    public int getAverageRating() { return Math.round(mAverageRating); }
}
