package com.hipsterbait.android.other;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.BadgeNotFound;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.models.Badge;

import java.util.ArrayList;

public class BadgesStore {

    private static BadgesStore singleton = null;

    private ArrayList<Badge> mBadges;
    private DatabaseReference mBadgesRef;
    private ChildEventListener mBadgesListener;

    private BadgesStore() {
        mBadges = new ArrayList<>();
    }

    public static BadgesStore getInstance() {
        if (singleton == null) {
            singleton = new BadgesStore();
            singleton.startListener();
        }
        return singleton;
    }

    private void startListener() {

        if (mBadgesListener == null) {
            mBadgesRef = FirebaseDatabase.getInstance().getReference()
                    .child("badges");

            mBadgesListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        float scale = HBApplication.getInstance().getApplicationContext()
                                .getResources()
                                .getDisplayMetrics().density;
                        mBadges.add(new Badge(dataSnapshot, scale));

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

            mBadgesRef.addChildEventListener(mBadgesListener);
        }
    }

    private void stopListener() {
        if (mBadgesListener != null) {
            mBadgesRef.removeEventListener(mBadgesListener);
            mBadgesListener = null;
            mBadgesRef = null;
        }
    }

    public Badge getBadge(String identifier) throws BadgeNotFound {
        for (Badge badge : mBadges) {
            if (badge.getKey().equals(identifier)) {
                return badge;
            }
        }

        throw new BadgeNotFound("No badge with identifier " + identifier);
    }

    public ArrayList<Badge> getBadges(String type) {
        ArrayList<Badge> result = new ArrayList<>();

        for (Badge badge : mBadges) {
            if (badge.getType().equals(type)) {
                result.add(badge);
            }
        }

        return result;
    }

    public ArrayList<Badge> getAllBadges() {
        return mBadges;
    }
}
