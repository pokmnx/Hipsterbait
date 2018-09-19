package com.hipsterbait.android.other;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class CassetteItem implements ClusterItem {
    private final LatLng mPostition;
    private final String mKey;

    public CassetteItem(double lat, double lng, String key) {
        mPostition = new LatLng(lat, lng);
        mKey = key;
    }

    @Override
    public LatLng getPosition() { return mPostition; }

    @Override
    public String getTitle() { return ""; }

    @Override
    public String getSnippet() { return ""; }

    public String getKey() { return mKey; }
}
