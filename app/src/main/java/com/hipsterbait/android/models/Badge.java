package com.hipsterbait.android.models;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.other.CacheManager;

import java.util.HashMap;
import java.util.Map;

public class Badge {

    private String mKey;
    private String mImageName;
    private String mName;
    private String mType;
    private String mNotFoundText;
    private String mFoundText;
    private Float mScaleFactor;

    private String mImageUrl;

    private DatabaseReference mRef;

    Badge(String imageName, String name, String type, String notfoundText, String foundText, Float scaleFactor, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mImageName = imageName;
        mName = name;
        mType = type;
        mNotFoundText = notfoundText;
        mFoundText = foundText;
        mScaleFactor = scaleFactor;

        mRef = null;

        // setImage(null);
    }

    public Badge(DataSnapshot snapshot, Float scaleFactor) throws RequiredValueMissing {

        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("imageName") ||
                !value.containsKey("name") ||
                !value.containsKey("type") ||
                !value.containsKey("foundText") ||
                !value.containsKey("notfoundText") ) {
            throw new RequiredValueMissing("Badge is missing required values " + mKey);
        }

        mImageName = (String) value.get("imageName");
        mName = (String) value.get("name");
        mType = (String) value.get("type");
        mNotFoundText = (String) value.get("notfoundText");
        mFoundText = (String) value.get("foundText");
        mScaleFactor = scaleFactor;

        mRef = snapshot.getRef();

        // setImage(null);
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("imageName", mImageName);
        result.put("name", mName);
        result.put("type", mType);
        result.put("foundText", mFoundText);
        result.put("notfoundText", mNotFoundText);

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

    public String getKey() {
        return mKey;
    }

    public String getName() { return mName; }

    public String getType() {
        return mType;
    }

    public String getImageUrl() { return mImageUrl; }

    public String getNotFoundText() { return mNotFoundText; }

    public String getFoundText() { return mFoundText; }
}
