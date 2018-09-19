package com.hipsterbait.android.other;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.hipsterbait.android.R;

public class HBClusterRenderer extends DefaultClusterRenderer<CassetteItem> {

    public HBClusterRenderer(Context context, GoogleMap map, ClusterManager<CassetteItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(CassetteItem item, MarkerOptions markerOptions) {
        BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bait_marker);
        markerOptions.icon(markerDescriptor);
    }

    @Override
    protected int getColor(int clusterSize) {
        return Color.HSVToColor(new float[]{
                325.12f, 93.07f, 45.29f
        });
    }
}
