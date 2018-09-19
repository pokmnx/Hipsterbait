package com.hipsterbait.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Journey;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TrackCassetteFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener {

    private static View view;

    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 666;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDbRef;
    private Cassette mCassette;

    private Map<Marker, Journey> mMarkers;

    public static TrackCassetteFragment newInstance(Cassette cassette) {
        TrackCassetteFragment fragment = new TrackCassetteFragment();
        fragment.mCassette = cassette;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }

        try {
            // Inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_track_cassette, container, false);
        } catch (InflateException e) {
            return view;
        }

        mMarkers = new HashMap<>();

        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getActivity(),
                            android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getActivity());
                }
                builder.setTitle("Permission Required")
                        .setMessage("Hipster Bait needs your location in order to capture cassettes.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getActivity(),
                                        "Hipster Bait requires your location", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
        } else {
            locationPermissionGranted();
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    locationPermissionGranted();
                } else {
                    Toast.makeText(getActivity(), "Hipster Bait requires your location", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void locationPermissionGranted() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage(getActivity(), this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();

            SupportMapFragment mapFragment = (SupportMapFragment)
                    this.getChildFragmentManager()
                            .findFragmentById(R.id.track_map_view);
            mapFragment.getMapAsync(this);
        }

        mDbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.mapstyle));

            if (success == false) {
                Log.e(getString(R.string.hb_log_tag), getString(R.string.style_parsing_failed));
            }
        } catch (Resources.NotFoundException e) {
            Log.e(getString(R.string.hb_log_tag), getString(R.string.cant_find_style), e);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        try {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(false);
            }
        } catch (SecurityException e) {
            Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        mMap.setOnMarkerClickListener(this);
        setItems();
    }

    private void setItems() {
        PolylineOptions options = new PolylineOptions();
        LatLngBounds.Builder builder = LatLngBounds.builder();
        int index = 0;
        for (Journey journey : mCassette.getJourneys()) {
            if (journey.getAction().equals(getString(R.string.found))) {
                index += 1;
                continue;
            }
            LatLng latLng = new LatLng(
                    journey.getLocation().getLatitude(), journey.getLocation().getLongitude());
            MarkerOptions newOptions = new MarkerOptions();
            if (index == mCassette.getJourneys().size() - 1 && mCassette.isHidden()) {
                newOptions.position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_green_white))
                        .anchor(0.5f, 0.5f);
            } else {
                newOptions.position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bait_marker))
                        .anchor(0.5f, 0.5f);
            }

            Marker marker = mMap.addMarker(newOptions);
            mMarkers.put(marker, journey);
            options.add(latLng);
            builder.include(latLng);

            index += 1;
        }
        options.width(8)
                .color(ContextCompat.getColor(getActivity(), R.color.hbPink))
                .pattern(Arrays.asList(new Dash(50), new Gap(30)));
        mMap.addPolyline(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 90));
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Journey journey = mMarkers.get(marker);

        Intent intent = new Intent(getActivity(), CassetteDetailActivity.class);
        intent.putExtra(getString(R.string.cassette_extra), mCassette);
        intent.putExtra(getString(R.string.journey_extra), journey);
        startActivity(intent);

        return true;
    }

    public void onConnected(Bundle bundle) {

    }

    public void onConnectionSuspended(int code) {

    }

    public void onConnectionFailed(ConnectionResult result) {

    }
}
