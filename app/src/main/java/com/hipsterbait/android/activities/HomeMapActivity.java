package com.hipsterbait.android.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.CassetteItem;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.HBClusterRenderer;
import com.hipsterbait.android.other.HBLocationManager;
import com.hipsterbait.android.widgets.HBButton;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HomeMapActivity extends ImmersiveActivity implements
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, HBLocationManager.HBLocationListener {

    public HBButton signUpButton;
    public ImageButton searchButton;
    public HBTextView closestLabel, acknowledgements;
    public LinearLayout logoLayout, producerLogoLayout, searchBackContainer;
    public RelativeLayout loadingLayout, splashLayout;
    public ImageView loadingPick1, loadingPick2, loadingPick3;
    public GridView searchTable;
    public EditText searchBar;
    public View darkenView;
    public SupportMapFragment mapFragment;

    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 666,
            MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 3030;
    public static final int CAPTURE_CODE = 2112;
    public static final String IGNORE_KEY = "IGNORE_KEY";

    private boolean mIsLoggedIn, mFound, mDoneLoading;
    private DatabaseReference mDbRef;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private ClusterManager<CassetteItem> mClusterManager;
    private Map<String, CassetteItem> mClusterItems;
    private Map<String, Boolean> mIgnoredCassettes = new HashMap<>();
    private HBLocationManager locationManager;
    private Cassette mFoundCassette;
    private GeoQuery mCircleQuery = null;
    private GeoQueryEventListener mGeoListener = null;
    protected GeoDataClient mGeoDataClient;

    private PredictionAdapter mPredictionAdapter = new PredictionAdapter();

    private int mLoadingIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_map);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
        mGeoDataClient = Places.getGeoDataClient(this, null);

        signUpButton = (HBButton) findViewById(R.id.home_signup_button);
        closestLabel = (HBTextView) findViewById(R.id.home_closest_label);
        acknowledgements = (HBTextView) findViewById(R.id.splash_acknowledgements);
        logoLayout = (LinearLayout) findViewById(R.id.splash_acknowledgements_logos);
        producerLogoLayout = (LinearLayout) findViewById(R.id.splash_producer_logos);
        splashLayout = (RelativeLayout) findViewById(R.id.home_loading_layout);
        loadingLayout = (RelativeLayout) findViewById(R.id.splash_loading_layout);
        loadingPick1 = (ImageView) findViewById(R.id.splash_loading_pick_1);
        loadingPick2 = (ImageView) findViewById(R.id.splash_loading_pick_2);
        loadingPick3 = (ImageView) findViewById(R.id.splash_loading_pick_3);
        searchButton = (ImageButton) findViewById(R.id.home_search_button);
        searchBackContainer = (LinearLayout) findViewById(R.id.home_search_back_container);
        searchTable = (GridView) findViewById(R.id.home_search_table);
        searchBar = (EditText) findViewById(R.id.home_search_edit);
        darkenView = findViewById(R.id.home_darken_view);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.home_map);

        mDoneLoading = false;

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Location location = locationManager.getCurrentLocation();
                if (location == null) return;
                AutocompleteFilter filter = new AutocompleteFilter.Builder()
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                        .build();
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(new LatLng(location.getLatitude(), location.getLongitude()))
                        .build();
                Task<AutocompletePredictionBufferResponse> results =
                        mGeoDataClient.getAutocompletePredictions(s.toString(), bounds,
                                filter);

                results.addOnCompleteListener(new OnCompleteListener<AutocompletePredictionBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<AutocompletePredictionBufferResponse> task) {
                        if (task.isSuccessful()) {
                            AutocompletePredictionBufferResponse predictions = task.getResult();
                            Iterator<AutocompletePrediction> iterator = predictions.iterator();
                            ArrayList<PredictionText> list = new ArrayList<>();
                            if (iterator.hasNext()) {
                                while (iterator.hasNext()) {
                                    AutocompletePrediction prediction = iterator.next();
                                    PredictionText text = new PredictionText(
                                            prediction.getPrimaryText(null),
                                            prediction.getSecondaryText(null),
                                            prediction.getPlaceId());
                                    list.add(text);
                                }
                            }

                            mPredictionAdapter.predictions = list;
                            searchTable.setAdapter(mPredictionAdapter);

                        } else {
                            Log.d("TEST", task.getException().getLocalizedMessage());
                        }

                        try {
                            task.getResult().release();
                        } catch (Exception e) {
                            Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                        }
                    }
                });
            }
        });

        searchTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PredictionText item = mPredictionAdapter.predictions.get(position);
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, item.placeId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {
                                    final Place myPlace = places.get(0);
                                    LatLng queriedLocation = myPlace.getLatLng();
                                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(queriedLocation, 14);
                                    mMap.moveCamera(update);
                                    searchBackTapped(searchBackContainer);
                                }
                                places.release();
                            }
                        });
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("Permission Required")
                        .setMessage("Hipster Bait needs your location in order to capture cassettes.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(HomeMapActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HomeMapActivity.this,
                                        "Hipster Bait requires your location", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
        } else {
            locationPermissionGranted();
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("Permission Required")
                        .setMessage("Hipster Bait needs your storage to download cassette tracks.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(HomeMapActivity.this,
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HomeMapActivity.this,
                                        "Hipster Bait requires your storage", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
        }
    }

    @Override
    protected void onResume() {
        mIsLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (mIsLoggedIn == false) {
            signUpButton.setVisibility(View.VISIBLE);
        } else {
            signUpButton.setVisibility(View.GONE);
        }

        mFound = false;

        super.onResume();
    }

    @Override
    public void onPause() {
        mFound = true;

        if (mCircleQuery != null) {
            mCircleQuery.removeAllListeners();
            mCircleQuery = null;
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        locationManager.removeLocationListener(this);

        super.onDestroy();
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
                    Toast.makeText(this, "Hipster Bait requires your location", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAPTURE_CODE) {
            if (resultCode == CAPTURE_CODE) {
                if (mFoundCassette != null) {
                    mIgnoredCassettes.put(mFoundCassette.getKey(), true);
                }
            }
        }
    }

    private void locationPermissionGranted() {
        mGoogleApiClient.connect();

        mapFragment.getMapAsync(this);

        mDbRef = FirebaseDatabase.getInstance().getReference();

        mClusterItems = new HashMap<>();

        HBApplication application = (HBApplication) getApplication();

        if (mDoneLoading == false) {
            showLoading();
            application.loaded = true;
        } else {
            splashLayout.setVisibility(View.GONE);
        }

        locationManager = HBLocationManager.getInstance();
        locationManager.startUpdating(this);
        locationManager.addLocationListener(this);
    }

    public void onLocationChanged(Location location) {
        setNearestCassetteLabel(location);
        if (mDoneLoading && !mFound) {
            setCircleQuery(location);
        }
    }

    public void setNearestCassetteLabel(Location userLocation) {
        ArrayList<Float> distances = new ArrayList<>();

        for (CassetteItem item : mClusterItems.values()) {
            Location itemLoc = new Location("");
            itemLoc.setLatitude(item.getPosition().latitude);
            itemLoc.setLongitude(item.getPosition().longitude);

            distances.add(userLocation.distanceTo(itemLoc));
        }

        Collections.sort(distances);
        if (distances.isEmpty()) {
            return;
        }

        Float nearestDistance = distances.get(0);
        Float rounded = Math.round(10*nearestDistance) / 10f;

        if (rounded < 1000f) {
            closestLabel.setText(
                    "The closest cassette is " + String.valueOf(rounded) + "m away.");
        } else {
            rounded = rounded / 1000f;
            Float roundedKm = Math.round(10*rounded) / 10f;
            closestLabel.setText(
                    "The closest cassette is " + String.valueOf(roundedKm) + "km away.");
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            HomeMapActivity.this, R.raw.mapstyle));

            if (success == false) {
                Log.e(getString(R.string.hb_log_tag), getString(R.string.style_parsing_failed));
            }
        } catch (Resources.NotFoundException e) {
            Log.e(getString(R.string.hb_log_tag), getString(R.string.cant_find_style), e);
        }

        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);

            final Handler locationReadyHandler = new Handler();
            locationReadyHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Location location = locationManager.getCurrentLocation();
                    if (location == null) {
                        locationReadyHandler.postDelayed(this, 1000);
                        return;
                    }

                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(
                            locationManager.getCurrentLocation().getLatitude(),
                            locationManager.getCurrentLocation().getLongitude()), 18);
                    mMap.moveCamera(update);
                }
            }, 1000);
        } catch (SecurityException e) {
            Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        setUpClusterer();
    }

    private void setUpClusterer() {
        mClusterManager = new ClusterManager<CassetteItem>(this, mMap);

        mClusterManager.setRenderer(new HBClusterRenderer(this, mMap, mClusterManager));
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<CassetteItem>() {
            @Override
            public boolean onClusterClick(Cluster<CassetteItem> cluster) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(cluster.getPosition().latitude, cluster.getPosition().longitude)));
                mMap.moveCamera(CameraUpdateFactory.zoomIn());
                return true;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<CassetteItem>() {
            @Override
            public boolean onClusterItemClick(CassetteItem cassetteItem) {
                DatabaseReference cassetteRef = FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.cassettes))
                        .child(cassetteItem.getKey());
                cassetteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            Cassette cassette = new Cassette(dataSnapshot);
                            Intent intent = new Intent(
                                    HomeMapActivity.this, CassetteDetailActivity.class);
                            intent.putExtra(getString(R.string.cassette_extra), cassette);
                            startActivity(intent);
                        } catch (RequiredValueMissing e)  {
                            Log.w(getString(R.string.hb_log_tag), e);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
                        Toast.makeText(HomeMapActivity.this,
                                R.string.error_getting_cassette,
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });

                return true;
            }
        });

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        setItems();
    }

    private void setCircleQuery(Location currentLoc) {

        if (mCircleQuery == null) {
            GeoFire geo = new GeoFire(mDbRef
                    .child(getString(R.string.geo))
                    .child(getString(R.string.cassettes)));
            mCircleQuery = geo.queryAtLocation(
                    new GeoLocation(currentLoc.getLatitude(), currentLoc.getLongitude()),
                    0.025f
            );
            mGeoListener = new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (key != null && location != null) {

                        if (mFound) {
                            return;
                        }

                        if (mIgnoredCassettes.containsKey(key)) {
                            if (mIgnoredCassettes.get(key)) return;
                        }

                        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                            return;
                        }

                        mFound = true;

                        mDbRef.child(getString(R.string.cassettes))
                                .child(key)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        try {
                                            Cassette cassette = new Cassette(dataSnapshot);

                                            Intent intent = new Intent(HomeMapActivity.this, ARCaptureActivity.class);
                                            intent.putExtra(getString(R.string.cassette_extra), cassette);

                                            mFound = true;
                                            if (mCircleQuery == null) {
                                                return;
                                            }

                                            mCircleQuery.removeAllListeners();
                                            mCircleQuery = null;

                                            mFoundCassette = cassette;
                                            startActivityForResult(intent, CAPTURE_CODE);

                                        } catch (RequiredValueMissing e) {
                                            mFound = false;
                                            Log.e(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        mFound = false;
                                        Log.e(getString(R.string.hb_log_tag), databaseError.getMessage());
                                    }
                                });
                    }
                }

                @Override
                public void onKeyExited(String key) {}

                @Override
                public void onKeyMoved(String key, GeoLocation location) {}

                @Override
                public void onGeoQueryReady() {}

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.e(getString(R.string.hb_log_tag), error.getMessage());
                }
            };
            mCircleQuery.addGeoQueryEventListener(mGeoListener);

            GeoQuery exitQuery = geo.queryAtLocation(
                    new GeoLocation(currentLoc.getLatitude(), currentLoc.getLongitude()),
                    0.050f);
            exitQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) { }

                @Override
                public void onKeyExited(String key) {
                    mIgnoredCassettes.put(key, false);
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {}

                @Override
                public void onGeoQueryReady() {}

                @Override
                public void onGeoQueryError(DatabaseError error) {}
            });

        } else {
            mCircleQuery.setCenter(new GeoLocation(
                    currentLoc.getLatitude(), currentLoc.getLongitude()));

            float radius = 0.005f;
            float radiusInMeters = radius * 1000;

            if (currentLoc.getAccuracy() > radiusInMeters) {
                radius = currentLoc.getAccuracy() / 1000;
            }

            if (radius > 0.025f) {
                radius = 0.025f;
            }

            mCircleQuery.setRadius(radius);
        }
    }

    private void setItems() {
        DatabaseReference cassettesRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.cassettes));

        DatabaseReference geoRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.geo)).child(getString(R.string.cassettes));

        final GeoFire geoFire = new GeoFire(geoRef);

        ChildEventListener cassettesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    if (Cassette.isHidden(HomeMapActivity.this, dataSnapshot) == false) {
                        return;
                    }

                    geoFire.getLocation(dataSnapshot.getKey(), new LocationCallback() {
                        @Override
                        public void onLocationResult(String key, GeoLocation location) {
                            if (location == null) {
                                return;
                            }

                            CassetteItem item = new CassetteItem(
                                    location.latitude, location.longitude, dataSnapshot.getKey());
                            mClusterManager.addItem(item);
                            mClusterItems.put(dataSnapshot.getKey(), item);
                            if (mDoneLoading) {
                                mClusterManager.cluster();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    if (Cassette.isHidden(HomeMapActivity.this, dataSnapshot)) {

                        geoFire.getLocation(dataSnapshot.getKey(), new LocationCallback() {
                            @Override
                            public void onLocationResult(String key, GeoLocation location) {
                                if (location == null) return;
                                CassetteItem item = new CassetteItem(
                                        location.latitude, location.longitude, dataSnapshot.getKey());
                                mClusterManager.addItem(item);
                                mClusterItems.put(dataSnapshot.getKey(), item);
                                if (mDoneLoading) {
                                    mClusterManager.cluster();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
                            }
                        });

                    } else {

                        CassetteItem item = mClusterItems.get(dataSnapshot.getKey());

                        if (item != null) {
                            mClusterManager.removeItem(item);
                            mClusterItems.remove(dataSnapshot.getKey());
                            if (mDoneLoading) {
                                mClusterManager.cluster();
                            }
                        }

                        if (mClusterItems.get(dataSnapshot.getKey()) != null) {
                            return;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                CassetteItem item = mClusterItems.get(dataSnapshot.getKey());

                if (item != null) {
                    mClusterManager.removeItem(item);
                    mClusterItems.remove(dataSnapshot.getKey());
                    if (mDoneLoading) {
                        mClusterManager.cluster();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
            }
        };

        cassettesRef.addChildEventListener(cassettesListener);

        BroadcastReceiver brd_receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(IGNORE_KEY)) {
                    String ignoreKey = intent.getStringExtra(getString(R.string.cassette_extra_key));
                    mIgnoredCassettes.put(ignoreKey, true);
                }
            }
        };

        registerReceiver(brd_receiver, new IntentFilter(IGNORE_KEY));
    }

    public void burgerTapped(View v) {

        if (mIsLoggedIn) {
            Intent intent = new Intent(HomeMapActivity.this, LoggedMenuActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(HomeMapActivity.this, MenuActivity.class);
            startActivity(intent);
        }
    }

    public void signUpTapped(View v) {
        Intent intent = new Intent(HomeMapActivity.this, LoginMenuActivity.class);
        startActivity(intent);
    }

    public void searchTapped(View v) {
        searchBackContainer.setVisibility(View.VISIBLE);
        searchTable.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.VISIBLE);
        darkenView.setVisibility(View.VISIBLE);
        searchBar.setText("");
    }

    public void searchBackTapped(View v) {
        searchBackContainer.setVisibility(View.INVISIBLE);
        searchTable.setVisibility(View.GONE);
        searchBar.setVisibility(View.GONE);
        searchBar.clearFocus();
        hideKeyboard(this);
        darkenView.setVisibility(View.GONE);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void onConnected(Bundle bundle) {

    }

    public void onConnectionSuspended(int code) {

    }

    public void onConnectionFailed(ConnectionResult result) {

    }

    private void showLoading() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logoLayout.setVisibility(View.VISIBLE);
                        producerLogoLayout.setVisibility(View.VISIBLE);
                        acknowledgements.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 5000);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingIndex += 1;

                        if (mLoadingIndex > 2) {
                            mLoadingIndex = 0;
                        }

                        switch (mLoadingIndex) {
                            case 0:
                            default:
                                loadingPick3.setImageDrawable(ContextCompat
                                        .getDrawable(HomeMapActivity.this, R.drawable.pick_char90));
                                loadingPick1.setImageDrawable(ContextCompat
                                        .getDrawable(HomeMapActivity.this, R.drawable.pick_char80));
                                break;
                            case 1:
                                loadingPick1.setImageDrawable(ContextCompat
                                        .getDrawable(HomeMapActivity.this, R.drawable.pick_char90));
                                loadingPick2.setImageDrawable(ContextCompat
                                        .getDrawable(HomeMapActivity.this, R.drawable.pick_char80));
                                break;
                            case 2:
                                loadingPick2.setImageDrawable(ContextCompat
                                        .getDrawable(HomeMapActivity.this, R.drawable.pick_char90));
                                loadingPick3.setImageDrawable(ContextCompat
                                        .getDrawable(HomeMapActivity.this, R.drawable.pick_char80));
                                break;
                        }

                        if (!mDoneLoading) {
                            handler.postDelayed(this, 1000);
                        }
                    }
                });
            }
        };
        handler.postDelayed(runnable, 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 9000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handler.removeCallbacks(runnable);
                        splashLayout.setVisibility(View.GONE);
                        mDoneLoading = true;
                        mClusterManager.cluster();
                    }
                });
            }
        }, 30000);
    }

    public void checkHoarderWarning() {
        User user = HBApplication.getInstance().user;

        if (user == null) return;

        Intent intent = new Intent(HomeMapActivity.this, HoarderWarningActivity.class);

        if (user.getHoardedCassettes().size() > 1) {
            intent.putExtra("hoarded_extra", true);
            intent.putExtra("count_extra", user.getHoardedCassettes().size());

        } else if (user.getHoardedCassettes().size() > 0) {
            intent.putExtra("hoarded_extra", true);
            intent.putExtra("days_extra", user.getHoardedCassettes().size());

        }
    }

    private class PredictionAdapter extends BaseAdapter {

        private ArrayList<PredictionText> predictions;

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) HomeMapActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = new View(HomeMapActivity.this);

                gridView = inflater.inflate(R.layout.places_griditem, parent, false);
            } else {
                gridView = convertView;
            }

            PredictionText prediction;
            try {
                prediction = predictions.get(position);
            } catch (IndexOutOfBoundsException e) {
                return gridView;
            }

            HBTextView title = (HBTextView) gridView.findViewById(R.id.places_text);
            HBTextView subtitle = (HBTextView) gridView.findViewById(R.id.places_subtext);

            title.setText(prediction.title);
            subtitle.setText(prediction.subtitle);

            return gridView;
        }

        @Override
        public int getCount() {
            return predictions.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    class PredictionText {
        CharSequence title;
        CharSequence subtitle;
        String placeId;

        PredictionText(CharSequence title, CharSequence subtitle, String placeId) {
            this.title = title;
            this.subtitle = subtitle;
            this.placeId = placeId;
        }
    }
}
