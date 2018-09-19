package com.hipsterbait.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.BadgeNotFound;
import com.hipsterbait.android.models.Badge;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.NotificationItem;
import com.hipsterbait.android.models.Points;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserBadges;
import com.hipsterbait.android.models.UserPoints;
import com.hipsterbait.android.other.BadgesAwardManager;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.HBLocationManager;
import com.hipsterbait.android.other.PointsEarningsManager;
import com.hipsterbait.android.widgets.HBTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BaitLureHipstersActivity extends ImmersiveActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    public HBTextView addressLabel;

    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 666;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private int mJourneys;
    private String mStreet, mCity, mCountry, mRegional, mState;
    private User mUser;
    private DatabaseReference mDbRef;
    private Location mLocation;
    private Cassette mCassette;
    private Handler mHandler;
    private Boolean mTapped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bait_lure_hipsters);

        mUser = ((HBApplication) getApplication()).user;

        addressLabel = (HBTextView) findViewById(R.id.bait_lure_address);

        String cassetteKey = getIntent().getStringExtra(getString(R.string.cassette_extra_key));

        mCassette = mUser.getCassetteByKey(cassetteKey);

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this,
                            android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("Permission Required")
                        .setMessage("Hipster Bait needs your location in order to capture cassettes.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(BaitLureHipstersActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(BaitLureHipstersActivity.this,
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
    }

    public void locationPermissionGranted() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.bait_lure_map);
        mapFragment.getMapAsync(this);

        mDbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));

            if (success == false) {
                Log.e(getString(R.string.hb_log_tag), getString(R.string.style_parsing_failed));
            }
        } catch (Resources.NotFoundException e) {
            Log.e(getString(R.string.hb_log_tag), getString(R.string.cant_find_style), e);
        }

        try {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocation = HBLocationManager.getInstance().getCurrentLocation();
                LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bait_marker)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

                try {
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(this, Locale.getDefault());
                    addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);

                    String address = addresses.get(0).getAddressLine(0);
                    mStreet = addresses.get(0).getThoroughfare();
                    mCity = addresses.get(0).getLocality();
                    mRegional = addresses.get(0).getSubAdminArea();
                    mState = addresses.get(0).getAdminArea();
                    mCountry = addresses.get(0).getCountryName();

                    switch (mState) {
                        case "British Columbia":
                            mState = "BC";
                            break;
                        case "Alberta":
                            mState = "AB";
                            break;
                        case "Manitoba":
                            mState = "MB";
                            break;
                        case "New Brunswick":
                            mState = "NB";
                            break;
                        case "Newfoundland and Labrador":
                            mState = "NL";
                            break;
                        case "Nova Scotia":
                            mState = "NS";
                            break;
                        case "Nunavut":
                            mState = "NU";
                            break;
                        case "Ontario":
                            mState = "ON";
                            break;
                        case "Prince Edward Island":
                            mState = "PE";
                            break;
                        case "Quebec":
                            mState = "QC";
                            break;
                        case "Saskatchewan":
                            mState = "SK";
                            break;
                        case "Yukon":
                            mState = "YK";
                            break;
                        default:
                            break;
                    }

                    addressLabel.setText(address + " " + mCity);

                } catch (IOException e) {
                    Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                }
            }
        } catch (SecurityException e) {
            Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }
        mMap.getUiSettings().setAllGesturesEnabled(false);
    }

    public void onConnected(Bundle bundle) {

    }

    public void onConnectionSuspended(int code) {

    }

    public void onConnectionFailed(ConnectionResult result) {

    }

    public void setTrapTapped(View v) {
        if (mLocation == null) {
            return;
        }

        if (mTapped) {
            return;
        }

        mTapped = true;

        mCassette.setHidden(true);
        mCassette.save(mLocation);

        mJourneys = mCassette.getJourneys().size();

        Journey journey = new Journey(
                getString(R.string.hidden),
                addressLabel.getText().toString(),
                mUser.getKey(),
                mCassette.getKey(),
                mLocation.getAltitude(),
                mStreet, mCity, mRegional, mState, mCountry,
                null, null);
        journey.save(mLocation);

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               journeyAdded();
            }
        }, 100);
    }

    public void journeyAdded() {
        if (mCassette.getJourneys().size() > mJourneys) {

            mUser.setCassette(mCassette, false);
            final Journey lastJourney = mCassette.getJourneys().get(mCassette.getJourneys().size() - 2);
            final Journey newJourney = mCassette.getJourneys().get(mCassette.getJourneys().size() - 1);

            newJourney.pullLocation(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {
                    lastJourney.pullLocation(new ModelPropertySetCallback() {
                        @Override
                        public void onSuccess() {
                            ArrayList<UserBadges> newBadges = BadgesAwardManager.awardBadgesForHide(BaitLureHipstersActivity.this, mUser, mCassette, lastJourney, newJourney);
                            ArrayList<NotificationItem> items = new ArrayList<>();

                            for (UserBadges userBadge : newBadges) {
                                try {
                                    Badge badge = BadgesStore.getInstance().getBadge(userBadge.getBadge());
                                    if (mUser.getNotifications().containsKey(badge.getKey()) == false) {
                                        items.add(new NotificationItem(badge.getKey(), false));
                                        mUser.setNotification(badge.getKey());
                                    }
                                } catch (BadgeNotFound e) {
                                    Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                }
                            }

                            int oldPoints = mUser.getPoints();

                            ArrayList<UserPoints> result = PointsEarningsManager.awardPointsForHiding(BaitLureHipstersActivity.this, mUser, mCassette, newJourney, lastJourney);
                            mUser.incrementHiddenCount();
                            mUser.setLastHideTimestamp(new Date().getTime());
                            mUser.save();

                            Intent ignoreIntent = new Intent();
                            ignoreIntent.setAction(HomeMapActivity.IGNORE_KEY);
                            ignoreIntent.putExtra(getString(R.string.cassette_extra_key), mCassette.getKey());
                            sendBroadcast(ignoreIntent);

                            Intent intent = new Intent(BaitLureHipstersActivity.this, BaitEarningsActivity.class);
                            intent.putExtra(getString(R.string.old_points), oldPoints);
                            intent.putExtra(getString(R.string.journey_extra), newJourney);
                            intent.putExtra(getString(R.string.cassette_extra_key), mCassette.getKey());
                            intent.putExtra(getString(R.string.userpoints_arraylist_arg), result);
                            startActivity(intent);

                            if (items.size() > 0) {
                                Intent badgeIntent = new Intent(BaitLureHipstersActivity.this, BadgeNotificationActivity.class);
                                badgeIntent.putParcelableArrayListExtra(getString(R.string.arraylist_extra), items);
                                startActivity(badgeIntent);
                            }

                            finish();
                        }

                        @Override
                        public void onFail(String error) {
                            Toast.makeText(BaitLureHipstersActivity.this, "ERROR: Couldn't hide bait: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFail(String error) {
                    Toast.makeText(BaitLureHipstersActivity.this, "ERROR: Couldn't hide bait: " + error, Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Log.d(getString(R.string.hb_log_tag), "WAITING FOR JOURNEY");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    journeyAdded();
                }
            }, 100);
        }
    }

    public void closeTapped(View v) {
        Intent intent = new Intent(BaitLureHipstersActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
