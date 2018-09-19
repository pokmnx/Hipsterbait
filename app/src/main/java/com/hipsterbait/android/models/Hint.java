package com.hipsterbait.android.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.other.CacheManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Hint implements Parcelable {

    private String mKey;
    private String mUserRef;
    private String mJourneyRef;
    private String mDescription;
    private String mImageRef;
    private String mFoundBy;
    private String mTitle;
    private long mTimestamp;
    private Map<String, Boolean> mFlags;
    private DatabaseReference mRef;

    private String mUserAvatarImageURL;
    private String mHintImageURL;

    public Hint(String userRef, String journeyRef, String description, String imageRef, String foundBy, String title, long timestamp, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mUserRef = userRef;
        mJourneyRef = journeyRef;
        mDescription = description;
        mImageRef = imageRef;
        mFoundBy = foundBy;
        mTitle = title;
        mTimestamp = timestamp;
        mFlags = new HashMap<>();

        mRef = null;
    }

    public Hint(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("userRef") ||
                !value.containsKey("journeyRef") ||
                !value.containsKey("foundBy") ||
                !value.containsKey("title") ||
                !value.containsKey("timestamp") ) {
            throw new RequiredValueMissing("Hint is missing required values " + mKey);
        }

        mUserRef = (String) value.get("userRef");
        mJourneyRef = (String) value.get("journeyRef");
        mDescription = (String) value.get("description");
        mImageRef = (String) value.get("imageRef");
        mFoundBy = (String) value.get("foundBy");
        mTitle = (String) value.get("title");
        mTimestamp = ((Number) value.get("timestamp")).longValue() * 1000; // Convert to milliseconds

        if (value.containsKey("flags")) {
            mFlags = (HashMap<String, Boolean>) value.get("flags");
        } else {
            mFlags = new HashMap<>();
        }

        mRef = snapshot.getRef();

        setImages();
    }

    public void save() {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("hints")
                    .child(mJourneyRef)
                    .push();
            mKey = this.mRef.getKey();
        }

        mRef.setValue(this.toMap());

        setImages();
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("userRef", mUserRef);
        result.put("journeyRef", mJourneyRef);
        result.put("description", mDescription);
        result.put("imageRef", mImageRef);
        result.put("foundBy", mFoundBy);
        result.put("title", mTitle);
        result.put("timestamp", mTimestamp / 1000); // Convert to seconds
        result.put("flags", mFlags);

        return result;
    }

    private void setImages() {
        setAvatarImage(null);
        setHintImage(null);
    }

    public void setHintImage(final ModelPropertySetCallback callback) {
        if (mImageRef == null || mImageRef.equals("")) {
            if (callback != null) callback.onFail("No image for hint " + mKey);
            return;
        }

        if (mHintImageURL != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        final StorageReference hintRef = FirebaseStorage.getInstance().getReference()
                .child("hints")
                .child(mImageRef);

        hintRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mHintImageURL = uri.toString();
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
    }

    public void setAvatarImage(final ModelPropertySetCallback callback) {
        if (mUserRef == null || mUserRef.equals("")) {
            if (callback != null) callback.onFail("No image for hint " + mKey);
            return;
        }

        if (mUserAvatarImageURL != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        if (mUserAvatarImageURL == null) {
            final StorageReference avatarRef = FirebaseStorage.getInstance().getReference()
                    .child("avatars")
                    .child(mUserRef)
                    .child("thumbnail");

            avatarRef.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mUserAvatarImageURL = uri.toString();
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
        }
    }

    public String dateFormattedVerbose() {
        return DateFormat.format("MMM d, yyyy HH:mm z", mTimestamp).toString();
    }

    public String dateFormattedMDY() {
        return DateFormat.format("MM/dd/yy", mTimestamp).toString();
    }

    public Boolean flagged() {
        if (mFlags.isEmpty()) {
            return false;
        }

        int count = 0;
        for(Map.Entry<String,Boolean> entry : mFlags.entrySet()){
            if (entry.getValue()) {
                count += 1;
            }
        }

        return count > 2;

    }

    public String getKey() { return mKey; }

    public String getHintImageURL() { return mHintImageURL; }

    public String getDescription() { return mDescription; }

    public String getTitle() { return mTitle; }

    public String getImageRef() { return mImageRef; }

    public String getUserAvatarImageURL() { return mUserAvatarImageURL; }

    public String getFoundBy() { return mFoundBy; }

    public Map<String, Boolean> getFlags() { return mFlags; }

    public Boolean hasFlag(String key) {
        if (!mFlags.containsKey(key)) {
            return false;
        }
        return mFlags.get(key);
    }

    public void setDescription(String description) { mDescription = description; }

    public void setImageRef(String imageRef) { mImageRef = imageRef; }

    public void setHintImageURL(String url) { mHintImageURL = url; }

    public void setDummyRef() { mRef = FirebaseDatabase.getInstance().getReference().child(""); }

    public void setFlag(String key) { mFlags.put(key, true); }

    // PARCELABLE
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mKey);
        out.writeString(mUserRef);
        out.writeString(mJourneyRef);
        out.writeString(mDescription);
        out.writeString(mImageRef);
        out.writeString(mFoundBy);
        out.writeString(mTitle);
        out.writeLong(mTimestamp);
        out.writeString(mRef.toString());
        out.writeString(mUserAvatarImageURL);
        out.writeString(mHintImageURL);
        out.writeInt(mFlags.size());
        for(Map.Entry<String,Boolean> entry : mFlags.entrySet()){
            out.writeString(entry.getKey());
            if (entry.getValue()) {
                out.writeInt(1);
            } else {
                out.writeInt(0);
            }
        }
    }

    public static final Parcelable.Creator<Hint> CREATOR = new Parcelable.Creator<Hint>() {
        public Hint createFromParcel(Parcel in) {
            return new Hint(in);
        }

        public Hint[] newArray(int size) {
            return new Hint[size];
        }
    };

    private Hint(Parcel in) {
        mKey = in.readString();
        mUserRef = in.readString();
        mJourneyRef = in.readString();
        mDescription = in.readString();
        mImageRef = in.readString();
        mFoundBy = in.readString();
        mTitle = in.readString();
        mTimestamp = in.readLong();
        try {
            String path = new URL(in.readString()).getPath();
            path = path.replaceFirst("/", "");
            mRef = FirebaseDatabase.getInstance().getReference().child(path);
        } catch (MalformedURLException e) {
            mRef = null;
        }
        mUserAvatarImageURL = in.readString();
        mHintImageURL = in.readString();
        mFlags = new HashMap<>();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            int value = in.readInt();
            mFlags.put(key, (value == 1));
        }
    }
}
