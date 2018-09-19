package com.hipsterbait.android.other;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RankNotFound;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.models.Rank;

import java.util.ArrayList;

public class RanksStore {

    private static RanksStore singleton = null;

    private ArrayList<Rank> mRanks;
    private DatabaseReference mRanksRef;
    private ChildEventListener mRanksListener;

    private RanksStore() {
        mRanks = new ArrayList<>();
    }

    public static RanksStore getInstance() {
        if (singleton == null) {
            singleton = new RanksStore();
            singleton.setListener();
        }
        return singleton;
    }

    private void setListener() {

        if (mRanksListener == null) {
            mRanksRef = FirebaseDatabase.getInstance().getReference()
                    .child("ranks");

            mRanksListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        float scale = HBApplication.getInstance().getApplicationContext()
                                .getResources()
                                .getDisplayMetrics().density;
                        mRanks.add(new Rank(dataSnapshot, scale));

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

            mRanksRef.orderByChild("level").addChildEventListener(mRanksListener);
        }
    }

    public void stopListener() {
        if (mRanksListener != null) {
            mRanksRef.removeEventListener(mRanksListener);
            mRanksListener = null;
            mRanksRef = null;
        }
    }

    public Rank getRank(int level) throws RankNotFound {
        for (Rank rank : mRanks) {
            if (rank.getLevel() == level) {
                return rank;
            }
        }

        throw new RankNotFound("No rank for level " + level);
    }

    public Rank getRank(String identifier) throws RankNotFound {
        for (Rank rank : mRanks) {
            if (rank.getKey().equals(identifier)) {
                return rank;
            }
        }

        throw new RankNotFound("No rank with identifier " + identifier);
    }

    public ArrayList<Rank> getRanks() { return mRanks; }
}
