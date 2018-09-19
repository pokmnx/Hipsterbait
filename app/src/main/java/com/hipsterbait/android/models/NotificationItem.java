package com.hipsterbait.android.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationItem implements Parcelable {
    public String mKey;
    public boolean mRank;

    public NotificationItem(String key, boolean rank) {
        mKey = key;
        mRank = rank;
    }

    // PARCELABLE
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mKey);
        if (mRank) {
            out.writeInt(1);
        } else {
            out.writeInt(0);
        }
    }

    public static final Parcelable.Creator<NotificationItem> CREATOR = new Parcelable.Creator<NotificationItem>() {
        public NotificationItem createFromParcel(Parcel in) {
            return new NotificationItem(in);
        }

        public NotificationItem[] newArray(int size) {
            return new NotificationItem[size];
        }
    };

    private NotificationItem(Parcel in) {
        mKey = in.readString();
        if (in.readInt() > 0) {
            mRank = true;
        } else {
            mRank = false;
        }
    }
}
