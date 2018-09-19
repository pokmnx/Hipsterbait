package com.hipsterbait.android.other;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.hipsterbait.android.R;

import java.util.ArrayList;

public class HBLocationManager {

    private static HBLocationManager singleton = null;

    private final int LOCATION_REFRESH_TIME = 2 * 1000;
    private final int LOCATION_REFRESH_DISTANCE = 0;

    private boolean mRequestingLocationUpdates = false;
    private Location mCurrentLocation;
    private LocationManager mLocationManager;
    private ArrayList<HBLocationListener> mListeners = new ArrayList<>();

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setCurrentLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("HB", provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("HB", provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("HB", provider);
        }
    };

    public static HBLocationManager getInstance() {
        if (singleton == null) {
            singleton = new HBLocationManager();
        }
        return singleton;
    }

    public void startUpdating(Context context) {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( context.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(context.getString(R.string.hb_log_tag), "HBLocationManager: PERMISSIONS NOT GRANTED");
            return;
        }

        if (mRequestingLocationUpdates) { return; }

        mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
        setCurrentLocation(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

        mRequestingLocationUpdates = true;
    }

    private void setCurrentLocation(Location location) {
        mCurrentLocation = location;
        for (HBLocationListener listener : mListeners) {
            listener.onLocationChanged(location);
        }
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public void addLocationListener(HBLocationListener listener) {
        for (HBLocationListener existing : mListeners) {
            if (existing == listener) {
                return;
            }
        }
        mListeners.add(listener);
    }

    public void removeLocationListener(HBLocationListener listener) {
        mListeners.remove(listener);
    }

    public interface HBLocationListener {
        public void onLocationChanged(Location location);
    }
}
