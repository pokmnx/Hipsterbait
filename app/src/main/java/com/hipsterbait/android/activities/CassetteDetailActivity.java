package com.hipsterbait.android.activities;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Hint;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.BadgesStore;
import com.hipsterbait.android.other.CacheManager;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.widgets.HBButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CassetteDetailActivity extends ImmersiveActivity implements View.OnClickListener {

    public LinearLayout flagHintLayout;
    public ImageView cassetteImage;
    public HBButton infoButton, hintsButton, photosButton, flagHintButton;
    public ViewPager viewPager;
    public FrameLayout loadingLayout;

    private HBButton mSelectedButton;

    private DetailsInfoFragment mInfoFragment;
    private DetailsHintsFragment mHintsFragment;
    private DetailsPhotosFragment mPhotosFragment;

    private DatabaseReference mDbRef;
    private GeoFire mGeoFire;
    private Cassette mCassette;
    private Journey mJourney;
    private User mUser;

    private Map<String, Object> mInfoData;
    private ArrayList<String> mPhotoUrls;
    private ArrayList<Hint> mCurrentHints;

    private DetailsHintsFragment.HintInterface mDelegate;

    private static final int INFO = 0;
    private static final int HINTS = 1;
    private static final int PHOTOS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cassette_detail);

        mUser = ((HBApplication) getApplication()).user;

        cassetteImage = (ImageView) findViewById(R.id.cassette_detail_cassette_image);

        loadingLayout = (FrameLayout) findViewById(R.id.cassette_detail_loading_layout);

        flagHintLayout = (LinearLayout) findViewById(R.id.cassette_detail_hint_flag_confirm);
        flagHintButton = (HBButton) findViewById(R.id.cassette_detail_flag_hint_button);

        infoButton = (HBButton) findViewById(R.id.cassette_detail_info_button);
        infoButton.setOnClickListener(this);
        hintsButton = (HBButton) findViewById(R.id.cassette_detail_hints_button);
        hintsButton.setOnClickListener(this);
        photosButton = (HBButton) findViewById(R.id.cassette_detail_photos_button);
        photosButton.setOnClickListener(this);

        viewPager = (ViewPager) findViewById(R.id.cassette_detail_viewpager);
        viewPager.setAdapter(new CassetteDetailActivity.SectionPagerAdapter(
                        getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                mSelectedButton.setBackgroundResource(R.color.hbCharcoal60);
                switch (position) {
                    case 0:
                        mSelectedButton = infoButton;
                        break;
                    case 1:
                        mSelectedButton = hintsButton;
                        break;
                    case 2:
                        mSelectedButton = photosButton;
                        break;
                    default:
                        break;
                }
                mSelectedButton.setBackgroundResource(R.color.hbBlue);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mJourney = getIntent().getParcelableExtra(getString(R.string.journey_extra));
        mCassette = getIntent().getParcelableExtra(getString(R.string.cassette_extra));

        mSelectedButton = infoButton;

        mInfoData = new HashMap<>();
        mPhotoUrls = new ArrayList<>();

        mInfoFragment = DetailsInfoFragment.newInstance();
        mHintsFragment = DetailsHintsFragment.newInstance();
        mPhotosFragment = DetailsPhotosFragment.newInstance();

        mDbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference geoRef = mDbRef.child(getString(R.string.geo)).child(getString(R.string.journeys));
        mGeoFire = new GeoFire(geoRef);

        mDelegate = new DetailsHintsFragment.HintInterface() {
            @Override
            public void hintFlagged(final int position) {
                flagHintLayout.setVisibility(View.VISIBLE);
                flagHintButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                            Toast.makeText(CassetteDetailActivity.this, "You must be signed in to flag a hint", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Hint currentHint = mCurrentHints.get(position);
                        currentHint.setFlag(mUser.getKey());
                        currentHint.save();

                        mPhotoUrls = new ArrayList<>();
                        for (Hint hint : mCurrentHints) {
                            if ((hint.getHintImageURL() != null) && (hint.flagged() == false)) {
                                mPhotoUrls.add(hint.getHintImageURL());
                            }
                        }

                        mPhotoUrls.add(mCassette.getCassetteModel().getCassetteArtURL());
                        mPhotoUrls.add(mCassette.getCassetteModel().getCoverArtURL());

                        mHintsFragment.setData(mCurrentHints, mJourney, mCassette, mUser.getKey(), mDelegate);
                        mPhotosFragment.setData(mPhotoUrls, mCurrentHints, mCassette);

                        flagHintLayout.setVisibility(View.GONE);
                    }
                });
            }
        };

        mCassette.setCassetteModel(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                cassetteModelSet();
            }

            @Override
            public void onFail(String error) {
                Log.e(getString(R.string.hb_log_tag), error);
                Toast.makeText(
                        CassetteDetailActivity.this, "Error getting cassette.", Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        });

        if (mJourney == null) {
            DatabaseReference ref = mDbRef.child(getString(R.string.journeys)).child(mCassette.getKey());
            ref.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        DataSnapshot childSnapshot = dataSnapshot.getChildren().iterator().next();
                        mJourney = new Journey(childSnapshot);
                        journeySet();

                    } catch (RequiredValueMissing e) {
                        Log.e(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(getString(R.string.hb_log_tag), databaseError.getMessage());
                }
            });
        } else {
            journeySet();
        }
    }

    @Override
    public void onClick(View v) {
        mSelectedButton.setBackgroundResource(R.color.hbCharcoal60);

        mSelectedButton = (HBButton) v;
        mSelectedButton.setBackgroundResource(R.color.hbBlue);

        viewPager.setCurrentItem(Integer.parseInt(v.getTag().toString()));
    }

    private void cassetteModelSet() {
        mInfoData.put(getString(R.string.name),
                "#" + mCassette.getNumber() + " " + mCassette.getCassetteModel().getName());
        mInfoData.put(getString(R.string.description),
                mCassette.getCassetteModel().getDescription());
        mInfoFragment.setData(mInfoData);

        mCassette.getCassetteModel().setCassetteArt(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                try {
                    Glide.with(CassetteDetailActivity.this)
                            .load(mCassette.getCassetteModel().getCassetteArtURL())
                            .into(cassetteImage);

                    mPhotoUrls.add(mCassette.getCassetteModel().getCassetteArtURL());

                    mPhotosFragment.setData(mPhotoUrls, mPhotosFragment.getHints(), mCassette);
                } catch (Exception e) {
                    Log.w(CassetteDetailActivity.this.getString(R.string.hb_log_tag), e);
                }
            }

            @Override
            public void onFail(String error) {
                Log.w(getString(R.string.hb_log_tag), error);
            }
        });

        mCassette.getCassetteModel().setCoverArt(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                mPhotoUrls.add(mCassette.getCassetteModel().getCoverArtURL());
                mPhotosFragment.setData(mPhotoUrls, mPhotosFragment.getHints(), mCassette);
            }

            @Override
            public void onFail(String error) {
                Log.w(getString(R.string.hb_log_tag), error);
            }
        });
    }

    private void journeySet() {
        mInfoData.put(getString(R.string.date_hidden), mJourney.dateFormattedComplete());
        mInfoData.put(getString(R.string.location), mJourney.getAddress());

        setHidByUser();
        setJourneyLocation();
        setJourneyHints();
    }

    private void setHidByUser() {
        mDbRef.child(getString(R.string.users)).child(mJourney.getUserRef())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() == false) {
                            mInfoData.put(getString(R.string.username), "Hipster Bait");
                            mInfoData.put(getString(R.string.rank), "Head of Distribution");
                            loadingLayout.setVisibility(View.GONE);
                            return;
                        }

                        @SuppressWarnings("unchecked")
                        Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
                        String username = (String) value.get(getString(R.string.username));
                        String rank = (String) value.get(getString(R.string.rank));
                        mInfoData.put(getString(R.string.username), username);
                        mInfoData.put(getString(R.string.rank), rank);
                        mInfoFragment.setData(mInfoData);

                        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                                .child(getString(R.string.avatars))
                                .child(mJourney.getUserRef())
                                .child(getString(R.string.thumbnail));

                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mInfoData.put(getString(R.string.image), uri.toString());
                                mInfoFragment.setData(mInfoData);
                                loadingLayout.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                loadingLayout.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
                    }
                });
    }

    private void setJourneyLocation() {
        mGeoFire.getLocation(mJourney.getKey(), new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                double lat = Math.round(1000 * location.latitude) / 1000;
                double lon = Math.round(1000 * location.longitude) / 1000;
                String locationString =
                        "Latitude: " + String.valueOf(lat) +
                        " Longitude: " + String.valueOf(lon);
                mInfoData.put(getString(R.string.gps), locationString);
                mInfoFragment.setData(mInfoData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
            }
        });
    }

    private void setJourneyHints() {
        mDbRef.child(getString(R.string.hints)).child(mJourney.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Hint> hints = new ArrayList<Hint>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    try {
                        final Hint hint = new Hint(childSnapshot);
                        hints.add(hint);
                        mCurrentHints = hints;

                        hint.setHintImage(new ModelPropertySetCallback() {
                            @Override
                            public void onSuccess() {
                                if (hint.flagged() == false) {
                                    mPhotoUrls.add(0, hint.getHintImageURL());
                                }
                                mPhotosFragment.setData(mPhotoUrls, mCurrentHints, mCassette);
                                mHintsFragment.setData(mCurrentHints, mJourney, mCassette, mUser.getKey(), mDelegate);
                            }

                            @Override
                            public void onFail(String error) {
                                Log.w(getString(R.string.hb_log_tag), error);
                                mHintsFragment.setData(mCurrentHints, mJourney, mCassette, mUser.getKey(), mDelegate);
                            }
                        });
                    } catch (RequiredValueMissing e) {
                        Log.w(getString(R.string.hb_log_tag), e);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(getString(R.string.hb_log_tag), databaseError.getMessage());
            }
        });
    }

    public void closeFlagTapped(View v) {
        flagHintLayout.setVisibility(View.GONE);
    }

    private class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case INFO:
                    if (mInfoFragment == null) {
                        mInfoFragment = DetailsInfoFragment.newInstance();
                    }
                    return mInfoFragment;
                case HINTS:
                    if (mHintsFragment == null) {
                        mHintsFragment = DetailsHintsFragment.newInstance();
                    }
                    return mHintsFragment;
                case PHOTOS:
                default:
                    if (mPhotosFragment == null) {
                        mPhotosFragment = DetailsPhotosFragment.newInstance();
                    }
                    return mPhotosFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case INFO:
                    return getString(R.string.info);
                case HINTS:
                    return getString(R.string.hints_display);
                case PHOTOS:
                default:
                    return getString(R.string.photos_display);
            }
        }
    }
}
