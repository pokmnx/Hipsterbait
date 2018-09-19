package com.hipsterbait.android.other;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.PointsNotFound;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.models.Points;

import java.util.ArrayList;

public class PointsStore {

    private static PointsStore singleton = null;
    private ArrayList<Points> mPoints;
    private DatabaseReference mPointsRef;
    private ChildEventListener mPointsListener;

    private PointsStore() {
        mPoints = new ArrayList<>();
    }

    public static PointsStore getInstance() {
        if (singleton == null) {
            singleton = new PointsStore();
            singleton.startListener();
        }
        return singleton;
    }

    private void startListener() {

        if (mPointsListener == null) {
            mPointsRef = FirebaseDatabase.getInstance().getReference()
                    .child("points");

            mPointsListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        mPoints.add(new Points(dataSnapshot));

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

            mPointsRef.addChildEventListener(mPointsListener);
        }
    }

    private void stopListener() {
        if (mPointsListener != null) {
            mPointsRef.removeEventListener(mPointsListener);
            mPointsListener = null;
            mPointsRef = null;
        }
    }

    public Points getPoints(String identifier) throws PointsNotFound {
        for (Points points : mPoints) {
            if (points.getKey().equals(identifier)) {
                return points;
            }
        }

        throw new PointsNotFound("No points with identifier " + identifier);
    }
}
