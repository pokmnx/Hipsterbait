package com.hipsterbait.android.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
import com.hipsterbait.android.other.CacheManager;

import java.util.HashMap;
import java.util.Map;

public class Band {

    private String mKey;
    private boolean mStatus;
    private String mBio;
    private String mCassetteModelRef;
    private String mName;
    private String mImageRef;
    private Map<String, Boolean> mEmailList;
    private long mAnnouncement;

    private DatabaseReference mRef;

    private Bitmap mImage;
    private CassetteModel mCassetteModel;

    Band(String userRef, boolean status, String bio, String cassetteModelRef, String name, String imageRef, Map<String, Boolean> emailList, long announcement, String key) {

        if (key == null) {
            mKey = "";
        } else {
            mKey = key;
        }

        mStatus = status;
        mBio = bio;
        mCassetteModelRef = cassetteModelRef;
        mName = name;
        mImageRef = imageRef;
        mEmailList = emailList;
        mAnnouncement = announcement;

        mRef = null;
    }

    Band(DataSnapshot snapshot) throws RequiredValueMissing {
        mKey = snapshot.getKey();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();

        if (    !value.containsKey("status") ||
                !value.containsKey("bio") ||
                !value.containsKey("cassetteModelRef") ||
                !value.containsKey("name") ||
                !value.containsKey("imageRef") ||
                !value.containsKey("emailList") ) {
            throw new RequiredValueMissing("Band is missing required values " + mKey);
        }

        mStatus = (boolean) value.get("status");
        mBio = (String) value.get("bio");
        mCassetteModelRef = (String) value.get("cassetteModelRef");
        mName = (String) value.get("name");
        mImageRef = (String) value.get("imageRef");

        if (value.get("emailList") instanceof Map) {
            mEmailList = (Map<String, Boolean>) value.get("emailList");
        } else {
            throw new RequiredValueMissing("Band is missing required values " + mKey);
        }

        mAnnouncement = ((Number) value.get("announcement")).longValue() * 1000; // convert to milliseconds

        mRef = snapshot.getRef();
    }

    public void save() {
        if (mRef == null) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("bands")
                    .child(mKey);
        }

        mRef.setValue(this.toMap());
    }

    private Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("status", mStatus);
        result.put("bio", mBio);
        result.put("cassetteModelRef", mCassetteModelRef);
        result.put("name", mName);
        result.put("imageRef", mImageRef);
        result.put("emailList", mEmailList);
        result.put("announcement", mAnnouncement / 1000); // Convert to seconds

        return result;
    }

    public void setImage() {
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("bands")
                .child(mImageRef);

        try {
            byte[] data = CacheManager.getInstance().getImageData(reference);
            mImage = BitmapFactory.decodeByteArray(data, 0, data.length);

        } catch (Exception e) {
            reference.getBytes(1024 * 1024)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            mImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            CacheManager.getInstance().cacheImageData(bytes, reference);
                        }
                    });
        }
    }

    public void setCassetteModel(final ModelPropertySetCallback callback) {

        if (mCassetteModel != null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("cassetteModels")
                .child(mCassetteModelRef);

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

    public String getName() { return mName; }

    public Map getEmailList() { return mEmailList; }

    public void addToEmailList(String userKey) { mEmailList.put(userKey, true); }

    public void removeFromEmailList(String userKey) { mEmailList.remove(userKey); }
}
