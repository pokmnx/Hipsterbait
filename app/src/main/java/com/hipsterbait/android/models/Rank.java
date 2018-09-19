package com.hipsterbait.android.models;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
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

import java.util.HashMap;
import java.util.Map;

public class Rank {

    private String mKey;
    private String mName;
    private int mLevel;
    private int mPoints;
    private int mToLevel;
    private String mFoundText;
    private String mImageName;
    private Float mScaleFactor;

    private String mImageUrl;

    private DatabaseReference mRef;

    Rank(String name, int level, int points, int toLevel, String foundText, String imageName, Float scaleFactor, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mName = name;
        mLevel = level;
        mPoints = points;
        mToLevel = toLevel;
        mFoundText = foundText;
        mImageName = imageName;
        mScaleFactor = scaleFactor;

        mRef = null;

        // setImage(null);
    }

    public Rank(DataSnapshot snapshot, Float scaleFactor) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("name") ||
                !value.containsKey("level") ||
                !value.containsKey("points") ||
                !value.containsKey("toLevel") ||
                !value.containsKey("foundText") ||
                !value.containsKey("imageName") ) {
            throw new RequiredValueMissing("Rank is missing required values " + mKey);
        }

        mName = (String) value.get("name");
        mLevel = ((Number) value.get("level")).intValue();
        mPoints = ((Number) value.get("points")).intValue();
        mToLevel = ((Number) value.get("toLevel")).intValue();
        mFoundText = (String) value.get("foundText");
        mImageName = (String) value.get("imageName");
        mScaleFactor = scaleFactor;

        mRef = snapshot.getRef();

        // setImage(null);
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("name", mName);
        result.put("level", mLevel);
        result.put("points", mPoints);
        result.put("toLevel", mToLevel);
        result.put("foundText", mFoundText);
        result.put("imageName", mImageName);

        return result;
    }

    public void setImage(final ModelPropertySetCallback callback) {
        if (mImageUrl != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        String sizeKey = "android_mdpi";
        if (mScaleFactor > 1.0) {
            sizeKey = "android_hdpi";
        }
        if (mScaleFactor > 1.5) {
            sizeKey = "android_xhdpi";
        }
        if (mScaleFactor > 2.0) {
            sizeKey = "android_xxhdpi";
        }
//        if (mScaleFactor > 3.0) {
//            sizeKey = "android_xxxhdpi";
//        }

        final StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                .child("badges")
                .child(sizeKey)
                .child(mImageName);

        imageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mImageUrl = uri.toString();
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

    public String getKey() { return mKey; }

    public String getName() { return mName; }

    public String getFoundText() { return mFoundText; }

    public int getLevel() { return mLevel; }

    public int getPoints() { return mPoints; }

    public int getNextLevelPoints() { return mPoints; }

    public String getImageUrl() { return mImageUrl; }

    public String getPointsString() { return ((Integer) mPoints).toString(); }

    public String getToLevelString() { return ((Integer) mToLevel).toString(); }

    public String getNextLevelPointsString() { return ((Integer) (mPoints + mToLevel)).toString(); }

    public String getLabel() {
        return "Level " + String.valueOf(mLevel) + " " + mName;
    }
}
