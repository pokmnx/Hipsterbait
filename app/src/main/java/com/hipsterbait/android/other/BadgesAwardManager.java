package com.hipsterbait.android.other;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Hint;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserBadges;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BadgesAwardManager {

    private static final ArrayList<LatLng> westCoast = new ArrayList<LatLng>() {
        {
            add(new LatLng(69.90011, -140.71289));
            add(new LatLng(71.10254, -163.91601));
            add(new LatLng(55.72711, -169.71679));
            add(new LatLng(55.77657, -137.98828));
            add(new LatLng(30.9776, -120.67382));
            add(new LatLng(13.36824, -99.53613));
            add(new LatLng(16.25686, -94.79003));
            add(new LatLng(32.84267, -113.81835));
            add(new LatLng(69.90011, -140.71289));
        }
    };
    private static final ArrayList<LatLng> eastCoast = new ArrayList<LatLng>() {
        {
            add(new LatLng(31.23613, -86.86844));
            add(new LatLng(29.91685, -97.7124));
            add(new LatLng(24.58709, -98.833));
            add(new LatLng(22.08563, -98.65722));
            add(new LatLng(19.08288, -96.63574));
            add(new LatLng(17.41354, -94.21875));
            add(new LatLng(18.91667, -84.44091));
            add(new LatLng(10.42298, -58.29345));
            add(new LatLng(18.43792, -57.06298));
            add(new LatLng(24.115, -80.15157));
            add(new LatLng(47.12667, -51.04499));
            add(new LatLng(52.22832, -55.30982));
            add(new LatLng(61.54364, -60.09521));
            add(new LatLng(60.877, -67.63183));
            add(new LatLng(50.72254, -59.28222));
            add(new LatLng(31.23613, -86.86844));
        }
    };

    public static ArrayList<UserBadges> awardBadgesForFind(final Context context, final User user, final Cassette cassette, final Journey lastJourney, final Journey newJourney) {

        ArrayList<UserBadges> result = new ArrayList<>();

        if (lastJourney == null || newJourney == null) {
            Log.e(context.getString(R.string.hb_log_tag), "ERROR: Null Journey");
            return result;
        }

        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        float distance = newJourney.getLocation().distanceTo(lastJourney.getLocation());

        // MILE HIGH
        if ((newJourney.getElevation() - lastJourney.getElevation()) > 150) {
            if (user.getBadges().containsKey(context.getString(R.string.mile_high)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.mile_high), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // UNDERGROUND
        if ((newJourney.getElevation() - lastJourney.getElevation()) < -20) {
            if (user.getBadges().containsKey(context.getString(R.string.underground)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.underground), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        if (cassette.isPhysical()) {
            // ANALOG
            if (user.getBadges().containsKey(context.getString(R.string.analog)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.analog), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }

        } else {
            // DIGITAL
            if (user.getBadges().containsKey(context.getString(R.string.digital)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.digital), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // DAVID LEE
        if (newJourney.getRegional() != null && lastJourney.getRegional() != null) {
            if (newJourney.getRegional().equals(lastJourney.getRegional()) && distance > 10000) {
                if (user.getBadges().containsKey(context.getString(R.string.david_lee)) == false) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.david_lee), cassette.getKey(), newJourney.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }
            }
        }

        // 360
        if (user.getCassettesArrayList().contains(cassette) || user.getArchivedCassettesArrayList().contains(cassette)) {
            if (cassette.getJourneys().size() > 2) {
                if (newJourney.getTimestamp() - cassette.getJourneys().get(2).getTimestamp() > 2 * 7 * 24 * 60 * 60 * 1000) {
                    if (user.getBadges().containsKey(context.getString(R.string.three_sixty)) == false) {
                        UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.three_sixty), cassette.getKey(), newJourney.getKey(), null);
                        userBadges.save();
                        user.setBadge(userBadges);
                        user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                        result.add(userBadges);
                    }
                }
            }

            // BOOMERANG
            if (lastJourney.getUserRef().equals(user.getKey())) {
                if (user.getBadges().containsKey(context.getString(R.string.boomerang)) == false && (distance > 20)) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.boomerang), cassette.getKey(), newJourney.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }
            }
        }

        // Jimi
        Location empMuseum = new Location(context.getString(R.string.location_provider));
        empMuseum.setLatitude(47.6214917);
        empMuseum.setLongitude(-122.3492515);

        Location jimiMemorial = new Location(context.getString(R.string.location_provider));
        jimiMemorial.setLatitude(47.4865127);
        jimiMemorial.setLongitude(-122.1762102);

        if (newJourney.getLocation().distanceTo(empMuseum) < 25 || newJourney.getLocation().distanceTo(jimiMemorial) < 25) {
            if (user.getBadges().containsKey(context.getString(R.string.jimi)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.jimi), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // LEMMY
        Location rainbowBar = new Location(context.getString(R.string.location_provider));
        rainbowBar.setLatitude(34.0906296);
        rainbowBar.setLongitude(-118.3882489);

        if (newJourney.getLocation().distanceTo(rainbowBar) < 25) {
            if (user.getBadges().containsKey(context.getString(R.string.lemmy)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.lemmy), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // HARPOS
        Location harpos = new Location(context.getString(R.string.location_provider));
        harpos.setLatitude(48.425739);
        harpos.setLongitude(-123.369515);

        if (newJourney.getLocation().distanceTo(harpos) < 25) {
            if (user.getBadges().containsKey("harpos") == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), "harpos", cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // RAT'S NEST
        Location ratsNest = new Location(context.getString(R.string.location_provider));
        ratsNest.setLatitude(48.444539);
        ratsNest.setLongitude(-123.377192);

        if (newJourney.getLocation().distanceTo(ratsNest) < 25) {
            if (user.getBadges().containsKey("the-rats-nest") == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), "the-rats-nest", cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // LYLE'S
        Location lyles = new Location(context.getString(R.string.location_provider));
        lyles.setLatitude(48.426225);
        lyles.setLongitude(-123.362817);

        if (newJourney.getLocation().distanceTo(lyles) < 25) {
            if (user.getBadges().containsKey("lyles") == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), "lyles", cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // STEAMERS
        Location steamers = new Location(context.getString(R.string.location_provider));
        steamers.setLatitude(48.426734);
        steamers.setLongitude(-123.368213);

        if (newJourney.getLocation().distanceTo(steamers) < 25) {
            if (user.getBadges().containsKey("steamers") == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), "steamers", cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // LAST CALL
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 4 && hour >= 2) {
            if (user.getBadges().containsKey(context.getString(R.string.last_call)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.last_call), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // First Find
        int finds = 0;

        for (Journey journey : cassette.getJourneys()) {
            if (journey.getAction().equals(context.getString(R.string.found))) {
                finds += 1;
            }
        }

        if (finds < 2) {
            if (user.getBadges().containsKey(context.getString(R.string.first_find)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.first_find), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // Goes to 11
        if (user.getFoundCount() > 10) {
            if (user.getBadges().containsKey(context.getString(R.string.goes_to_11)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.goes_to_11), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // Heard them first
        if (cassette.getCassetteModel().getHeardThemFirst() == null) {
            if (user.getBadges().containsKey(context.getString(R.string.heard_them_first)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.heard_them_first), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);

                cassette.getCassetteModel().setHeardThemFirst(user.getKey());
                cassette.getCassetteModel().save();
            }
        }

        ArrayList<Journey> journeys = cassette.getJourneys();
        Collections.reverse(journeys);
        int index = 0;
        int foundCount = 0;
        String autotuner1 = null;
        String autotuner2 = null;
        Boolean cher = true;
        Map<String, String> foundBy = new HashMap<>();
        for (Journey journey : journeys) {

            if (journey.getAction().equals(context.getString(R.string.found))) {
                foundCount += 1;
                foundBy.put(journey.getUserRef(), journey.getKey());
            }

            if (index == 0) {
                autotuner1 = journey.getUserRef();
            }

            if (index == 1) {
                autotuner2 = journey.getUserRef();
            }

            if (index == 2) {
                if (journey.getUserRef().equals(autotuner2) == false) {
                    cher = false;
                }

                if ((newJourney.getCountry() != null) && (journey.getCountry() != null)) {
                    if (newJourney.getCountry().equals(journey.getCountry()) == false) {
                        if (user.getBadges().containsKey(context.getString(R.string.import_)) == false) {
                            UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.import_), cassette.getKey(), newJourney.getKey(), null);
                            userBadges.save();
                            user.setBadge(userBadges);
                            user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                            result.add(userBadges);
                        }
                    }
                }
            }

            if (index == 3) {
                if (journey.getUserRef().equals(autotuner1) == false) {
                    cher = false;
                }
            }

            if (index == 4) {
                if (journey.getUserRef().equals(autotuner1) == false) {
                    cher = false;
                }
            }

            if (index == 5) {
                if (journey.getUserRef().equals(autotuner2) == false) {
                    cher = false;
                }
            }

            if (index == 6) {
                if (journey.getUserRef().equals(autotuner2) == false) {
                    cher = false;
                }
            }

            if (index == 7) {
                if (journey.getUserRef().equals(autotuner1) == false) {
                    cher = false;
                }

                final String finalAutotuner1 = autotuner1;
                final String finalAutotuner2 = autotuner2;

                if (cher) {
                    dbRef.child(context.getString(R.string.users))
                            .child(finalAutotuner1)
                            .child(context.getString(R.string.badges))
                            .child(context.getString(R.string.auto_tune))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        return;
                                    }

                                    UserBadges userBadges = new UserBadges(finalAutotuner1, context.getString(R.string.auto_tune), cassette.getKey(), newJourney.getKey(), null);
                                    userBadges.save();
                                    dataSnapshot.getRef().setValue(userBadges.getKey());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(context.getString(R.string.hb_log_tag), databaseError.getMessage());
                                }
                            });

                    dbRef.child(context.getString(R.string.users))
                            .child(finalAutotuner2)
                            .child(context.getString(R.string.badges))
                            .child(context.getString(R.string.auto_tune))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        return;
                                    }

                                    UserBadges userBadges = new UserBadges(finalAutotuner2, context.getString(R.string.auto_tune), cassette.getKey(), newJourney.getKey(), null);
                                    userBadges.save();
                                    dataSnapshot.getRef().setValue(userBadges.getKey());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(context.getString(R.string.hb_log_tag), databaseError.getMessage());
                                }
                            });
                }
            }

            if (index == 13) {
                if (journey.getUserRef().equals(user.getKey())) {
                    if (foundBy.size() > 6) {
                        if (user.getBadges().containsKey(context.getString(R.string.crowd_surf)) == false) {
                            UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.crowd_surf), cassette.getKey(), newJourney.getKey(), null);
                            userBadges.save();
                            user.setBadge(userBadges);
                            user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                            result.add(userBadges);
                        }
                    }
                }
            }

            index += 1;
        }

        // FALSE ENDING
        if (lastJourney.getUserRef().equals(user.getKey())) {
            long diff = newJourney.getTimestamp() - lastJourney.getTimestamp();
            if (diff < 1000 * 60 * 60 * 4 && diff > 1000 * 60 * 60 * 2) {
                if (user.getBadges().containsKey(context.getString(R.string.false_ending)) == false) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.false_ending), cassette.getKey(), newJourney.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }
            }
        }

        // EP
        if (AudioPlayerManager.getInstance().getPlaylist().size() > 4) {
            if (user.getBadges().containsKey(context.getString(R.string.ep)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.ep), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // LP
        if (AudioPlayerManager.getInstance().getPlaylist().size() > 11) {
            if (user.getBadges().containsKey(context.getString(R.string.lp)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.lp), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        Map<String, Number> shadow = user.getShadow();
        if (!lastJourney.getUserRef().equals(user.getKey())) {
            if (shadow.size() < 1) {
                // Not shadowing anyone
                shadow.put(lastJourney.getUserRef(), 1);
            } else {
                if (shadow.containsKey(lastJourney.getUserRef())) {
                    long newValue = (shadow.get(lastJourney.getUserRef())).longValue() + 1;
                    shadow.put(lastJourney.getUserRef(), newValue);

                    // SHADOW
                    if (newValue > 4) {
                        dbRef.child(context.getString(R.string.users))
                                .child(lastJourney.getUserRef())
                                .child(context.getString(R.string.badges))
                                .child(context.getString(R.string.shadow))
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            return;
                                        }

                                        UserBadges userBadges = new UserBadges(lastJourney.getUserRef(), context.getString(R.string.shadow), cassette.getKey(), newJourney.getKey(), null);
                                        userBadges.save();
                                        dataSnapshot.getRef().setValue(userBadges.getKey());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(context.getString(R.string.hb_log_tag), databaseError.getMessage());
                                    }
                                });
                    }
                } else {
                    shadow = new HashMap<>();
                    shadow.put(lastJourney.getUserRef(), 1);
                }
            }
        }

        user.setShadow(shadow);
        user.save();

        // Coast to Coast
        boolean westContainsNewLoc = PolyUtil.containsLocation(new LatLng(newJourney.getLocation().getLatitude(), newJourney.getLocation().getLongitude()), westCoast, true);
        boolean eastContainsNewLoc = PolyUtil.containsLocation(new LatLng(newJourney.getLocation().getLatitude(), newJourney.getLocation().getLongitude()), eastCoast, true);
        boolean westContainsOldLoc = PolyUtil.containsLocation(new LatLng(lastJourney.getLocation().getLatitude(), lastJourney.getLocation().getLongitude()), westCoast, true);
        boolean eastContainsOldLoc = PolyUtil.containsLocation(new LatLng(lastJourney.getLocation().getLatitude(), lastJourney.getLocation().getLongitude()), eastCoast, true);
        boolean coastToCoast = false;
        if (westContainsNewLoc && eastContainsOldLoc) {
            coastToCoast = true;
        }
        if (eastContainsNewLoc && westContainsOldLoc) {
            coastToCoast = true;
        }

        if (coastToCoast) {
            if (user.getBadges().containsKey(context.getString(R.string.coast_to_coast)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.coast_to_coast), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // Increment baitsFound count for users who hid this cassette
        for (final Journey journey : cassette.getJourneys()) {
            if (journey.getAction().equals(context.getString(R.string.hidden))) {
                // Hidden Journey
                dbRef.child(context.getString(R.string.users))
                        .child(journey.getUserRef())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists() == false)
                                    return;

                                Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
                                long baitsFoundCount = value.containsKey(context.getString(R.string.baits_found_count)) ?
                                        (long) value.get(context.getString(R.string.baits_found_count)) : 0;
                                baitsFoundCount += 1;
                                dataSnapshot.getRef().child("baitsFoundCount").setValue(baitsFoundCount);

                                // Master Bait
                                if (baitsFoundCount > 1) {
                                    Map<String, String> badges = (value.get(context.getString(R.string.badges)) == null) ?
                                            new HashMap<String, String>() : (HashMap<String, String>) value.get(context.getString(R.string.badges));
                                    if (badges.containsKey(context.getString(R.string.master_bait)) == false)  {
                                        UserBadges userBadges = new UserBadges(dataSnapshot.getKey(), context.getString(R.string.master_bait), cassette.getKey(), newJourney.getKey(), null);
                                        userBadges.save();
                                        dbRef.child(context.getString(R.string.users))
                                                .child(dataSnapshot.getKey())
                                                .child(context.getString(R.string.badges))
                                                .child(userBadges.getBadge()).setValue(userBadges.getKey());
                                    }
                                }

                                // GOLD
                                if (baitsFoundCount > 49) {
                                    Map<String, String> badges = (value.get(context.getString(R.string.badges)) == null) ?
                                            new HashMap<String, String>() : (HashMap<String, String>) value.get(context.getString(R.string.badges));
                                    if (badges.containsKey(context.getString(R.string.gold)) == false)  {
                                        UserBadges userBadges = new UserBadges(dataSnapshot.getKey(), context.getString(R.string.gold), cassette.getKey(), newJourney.getKey(), null);
                                        userBadges.save();
                                        dbRef.child(context.getString(R.string.users))
                                                .child(dataSnapshot.getKey())
                                                .child(context.getString(R.string.badges))
                                                .child(userBadges.getBadge()).setValue(userBadges.getKey());
                                    }
                                }

                                // PLATINUM
                                if (baitsFoundCount > 99) {
                                    Map<String, String> badges = (value.get(context.getString(R.string.badges)) == null) ?
                                            new HashMap<String, String>() : (HashMap<String, String>) value.get(context.getString(R.string.badges));
                                    if (badges.containsKey(context.getString(R.string.platinum)) == false)  {
                                        UserBadges userBadges = new UserBadges(dataSnapshot.getKey(), context.getString(R.string.platinum), cassette.getKey(), newJourney.getKey(), null);
                                        userBadges.save();
                                        dbRef.child(context.getString(R.string.users))
                                                .child(dataSnapshot.getKey())
                                                .child(context.getString(R.string.badges))
                                                .child(userBadges.getBadge()).setValue(userBadges.getKey());
                                    }
                                }

                                // MULTI-PLATINUM
                                if (baitsFoundCount > 199) {
                                    Map<String, String> badges = (value.get(context.getString(R.string.badges)) == null) ?
                                            new HashMap<String, String>() : (HashMap<String, String>) value.get(context.getString(R.string.badges));
                                    if (badges.containsKey(context.getString(R.string.multi_platinum)) == false)  {
                                        UserBadges userBadges = new UserBadges(dataSnapshot.getKey(), context.getString(R.string.multi_platinum), cassette.getKey(), newJourney.getKey(), null);
                                        userBadges.save();
                                        dbRef.child(context.getString(R.string.users))
                                                .child(dataSnapshot.getKey())
                                                .child(context.getString(R.string.badges))
                                                .child(userBadges.getBadge()).setValue(userBadges.getKey());
                                    }
                                }

                                // DIAMOND
                                if (baitsFoundCount > 399) {
                                    Map<String, String> badges = (value.get(context.getString(R.string.badges)) == null) ?
                                            new HashMap<String, String>() : (HashMap<String, String>) value.get(context.getString(R.string.badges));
                                    if (badges.containsKey(context.getString(R.string.diamond)) == false)  {
                                        UserBadges userBadges = new UserBadges(dataSnapshot.getKey(), context.getString(R.string.diamond), cassette.getKey(), newJourney.getKey(), null);
                                        userBadges.save();
                                        dbRef.child(context.getString(R.string.users))
                                                .child(dataSnapshot.getKey())
                                                .child(context.getString(R.string.badges))
                                                .child(userBadges.getBadge()).setValue(userBadges.getKey());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(context.getString(R.string.hb_log_tag), databaseError.getMessage());
                            }
                        });
            } else {
                // Found Journey
                if (journey.getUserRef().equals(user.getKey()) == false) {
                    if (Math.abs(journey.getTimestamp() - newJourney.getTimestamp()) < 24 * 60 * 60 * 1000) {
                        // Cassette was found less than 24 hours ago
                        journey.pullLocation(new ModelPropertySetCallback() {
                            @Override
                            public void onSuccess() {
                                if (journey.getLocation().distanceTo(newJourney.getLocation()) < 1000) {
                                    // Cassette was found less than 1km away

                                    // Double header

                                    // Each user that his this cassette should get the double-header badge
                                    for (final Journey journey : cassette.getJourneys()) {
                                        if (journey.getUserRef().equals(user.getKey())) {
                                            continue;
                                        }

                                        if (journey.getAction().equals(context.getString(R.string.hidden))) {
                                            dbRef.child(context.getString(R.string.users))
                                                    .child(journey.getUserRef())
                                                    .child(context.getString(R.string.badges))
                                                    .child(context.getString(R.string.double_header))
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            UserBadges userBadges = new UserBadges(journey.getUserRef(), context.getString(R.string.double_header), cassette.getKey(), journey.getKey(), null);
                                                            userBadges.save();
                                                            dataSnapshot.getRef().setValue(userBadges.getKey());
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                            Log.w(context.getString(R.string.hb_log_tag), databaseError.getMessage());
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFail(String error) {
                                Log.w(context.getString(R.string.hb_log_tag), error);
                            }
                        });
                    }
                }
            }
        }

        return result;
    }

    public static ArrayList<UserBadges> awardBadgesForHide(final Context context, final User user, final Cassette cassette, final Journey lastJourney, final Journey newJourney) {

        ArrayList<UserBadges> result = new ArrayList<>();

        if (lastJourney == null || newJourney == null) {
            Log.e(context.getString(R.string.hb_log_tag), "ERROR: Null Journey");
            return result;
        }

        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        // MILE HIGH
        if ((newJourney.getElevation() - lastJourney.getElevation()) > 150) {
            if (user.getBadges().containsKey(context.getString(R.string.mile_high)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.mile_high), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // UNDERGROUND
        if ((newJourney.getElevation() - lastJourney.getElevation()) < -20) {
            if (user.getBadges().containsKey(context.getString(R.string.underground)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.underground), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // WALKMAN
        if (newJourney.getCity() == null) {
            if (user.getBadges().containsKey("walkman") == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), "walkman", cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // TOP 10
        if (user.getCassettesArrayList().size() > 9 && user.getHiddenCount() > 9) {
            if (user.getBadges().containsKey(context.getString(R.string.top10)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.top10), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // TOP 20
        if (user.getCassettesArrayList().size() > 19 && user.getHiddenCount() > 19) {
            if (user.getBadges().containsKey(context.getString(R.string.top20)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.top20), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // TOP 30
        if (user.getCassettesArrayList().size() > 29 && user.getHiddenCount() > 29) {
            if (user.getBadges().containsKey(context.getString(R.string.top30)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.top30), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // TOP 40
        if (user.getCassettesArrayList().size() > 39 && user.getHiddenCount() > 39) {
            if (user.getBadges().containsKey(context.getString(R.string.top40)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.top40), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.save();
                result.add(userBadges);
            }
        }

        // TOP 50
        if (user.getCassettesArrayList().size() > 49 && user.getHiddenCount() > 49) {
            if (user.getBadges().containsKey(context.getString(R.string.top50)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.top50), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // TOP 60
        if (user.getCassettesArrayList().size() > 59 && user.getHiddenCount() > 59) {
            if (user.getBadges().containsKey(context.getString(R.string.top60)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.top60), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // TOP 70
        if (user.getCassettesArrayList().size() > 69 && user.getHiddenCount() > 69) {
            if (user.getBadges().containsKey(context.getString(R.string.top70)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.top70), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // TOP 80
        if (user.getCassettesArrayList().size() > 79 && user.getHiddenCount() > 79) {
            if (user.getBadges().containsKey(context.getString(R.string.top80)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.top80), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // TOP 90
        if (user.getCassettesArrayList().size() > 89 && user.getHiddenCount() > 89) {
            if (user.getBadges().containsKey(context.getString(R.string.top90)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.top90), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // HOT 100
        if (user.getCassettesArrayList().size() > 99 && user.getHiddenCount() > 99) {
            if (user.getBadges().containsKey(context.getString(R.string.hot100)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.hot100), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // JIMI
        Location empMuseum = new Location("gps");
        empMuseum.setLatitude(47.6214917);
        empMuseum.setLongitude(-122.3492515);
        Location jimiMemorial = new Location("gps");
        jimiMemorial.setLatitude(47.4865127);
        jimiMemorial.setLongitude(-122.1762102);

        if (newJourney.getLocation().distanceTo(empMuseum) < 25 ||
                newJourney.getLocation().distanceTo(jimiMemorial) < 25) {
            if (user.getBadges().containsKey(context.getString(R.string.jimi)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.jimi), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // LEMMY
        Location rainbowBar = new Location(context.getString(R.string.location_provider));
        rainbowBar.setLatitude(34.0906296);
        rainbowBar.setLongitude(-118.3882489);

        if (newJourney.getLocation().distanceTo(rainbowBar) < 25) {
            if (user.getBadges().containsKey(context.getString(R.string.lemmy)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.lemmy), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // HARPOS
        Location harpos = new Location(context.getString(R.string.location_provider));
        harpos.setLatitude(48.425739);
        harpos.setLongitude(-123.369515);

        if (newJourney.getLocation().distanceTo(harpos) < 25) {
            if (user.getBadges().containsKey("harpos") == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), "harpos", cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // RAT'S NEST
        Location ratsNest = new Location(context.getString(R.string.location_provider));
        ratsNest.setLatitude(48.444539);
        ratsNest.setLongitude(-123.377192);

        if (newJourney.getLocation().distanceTo(ratsNest) < 25) {
            if (user.getBadges().containsKey("the-rats-nest") == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), "the-rats-nest", cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // LYLE'S
        Location lyles = new Location(context.getString(R.string.location_provider));
        lyles.setLatitude(48.426225);
        lyles.setLongitude(-123.362817);

        if (newJourney.getLocation().distanceTo(lyles) < 25) {
            if (user.getBadges().containsKey("lyles") == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), "lyles", cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // STEAMERS
        Location steamers = new Location(context.getString(R.string.location_provider));
        steamers.setLatitude(48.426734);
        steamers.setLongitude(-123.368213);

        if (newJourney.getLocation().distanceTo(steamers) < 25) {
            if (user.getBadges().containsKey("steamers") == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), "steamers", cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // LAST CALL
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 4 && hour >= 2) {
            if (user.getBadges().containsKey(context.getString(R.string.last_call)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.last_call), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // FAT ELVIS
        float distance = newJourney.getLocation().distanceTo(lastJourney.getLocation());

        if (distance < 20) {
            if (user.getBadges().containsKey(context.getString(R.string.fat_elvis)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.fat_elvis), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // MILE HIGH
        if (newJourney.getLocation().hasAltitude()) {
            if (newJourney.getLocation().getAltitude() > 1609) {
                if (user.getBadges().containsKey(context.getString(R.string.mile_high)) == false) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.mile_high), cassette.getKey(), newJourney.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }
            }
        }

        // WALKMAN

        // DAVID LEE
        if (newJourney.getRegional() != null && lastJourney.getRegional() != null) {
            if (newJourney.getRegional().equals(lastJourney.getRegional()) && distance > 10000) {
                if (user.getBadges().containsKey(context.getString(R.string.david_lee)) == false) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.david_lee), cassette.getKey(), newJourney.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }
            }
        }

        // GET IN THE VAN
        if (newJourney.getCity() != null && lastJourney.getCity() != null) {
            if (newJourney.getCity().equals(lastJourney.getCity()) == false) {
                if (user.getBadges().containsKey(context.getString(R.string.get_in_the_van)) == false) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.get_in_the_van), cassette.getKey(), newJourney.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }
            }
        }

        // WORLD TOUR
        if (newJourney.getCountry() != null && lastJourney.getCountry() != null) {
            if (newJourney.getCountry().equals(lastJourney.getCountry()) == false) {
                if (user.getBadges().containsKey(context.getString(R.string.world_tour)) == false) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.world_tour), cassette.getKey(), newJourney.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }

                // EXPORT
                dbRef.child(context.getString(R.string.users))
                        .child(lastJourney.getUserRef())
                        .child(context.getString(R.string.badges))
                        .child(context.getString(R.string.export))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    return;
                                }

                                UserBadges userBadges = new UserBadges(lastJourney.getUserRef(), context.getString(R.string.export), cassette.getKey(), newJourney.getKey(), null);
                                userBadges.save();
                                dataSnapshot.getRef().setValue(userBadges.getKey());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(context.getString(R.string.hb_log_tag), databaseError.getMessage());
                            }
                        });
            }
        }

        // HANGOVER
        if (newJourney.getTimestamp() - lastJourney.getTimestamp() > 1000 * 60 * 60 * 24 * 2) {
            if (user.getBadges().containsKey(context.getString(R.string.hangover)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.hangover), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // FIRST BAIT
        if (user.getBadges().containsKey(context.getString(R.string.first_bait)) == false) {
            UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.first_bait), cassette.getKey(), newJourney.getKey(), null);
            userBadges.save();
            user.setBadge(userBadges);
            user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
            result.add(userBadges);
        }

        // BOMBER
        if (user.getHiddenCount() > 4) {
            if (user.getBadges().containsKey(context.getString(R.string.bomber)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.bomber), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // TRIPLE THREAT
        if (user.getHiddenCount() > 2) {
            if (user.getBadges().containsKey(context.getString(R.string.triple_threat)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.triple_threat), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // BINGE AND PURGE
        if (newJourney.getTimestamp() - lastJourney.getTimestamp() < 1000 * 60 * 30 && (distance > 20)) {
            if (user.getBadges().containsKey(context.getString(R.string.binge_and_purge)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.binge_and_purge), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // JAILBREAK
        if (newJourney.getTimestamp() - lastJourney.getTimestamp() > 7 * 24 * 60 * 60 * 1000) {
            if (user.getBadges().containsKey(context.getString(R.string.jailbreak)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.jailbreak), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // DRUM ROLL
        if (newJourney.getStreet() != null) {
            Map<String, ArrayList> drumRoll = user.getDrumRoll();

            if (drumRoll.containsKey(newJourney.getStreet())) {
                ArrayList<String> value = drumRoll.get(newJourney.getStreet());
                if (value.contains(cassette.getKey()) == false) {
                    value.add(cassette.getKey());
                }
                drumRoll.put(newJourney.getStreet(), value);
                if (value.size() > 3) {
                    if (user.getBadges().containsKey(context.getString(R.string.drum_roll)) == false) {
                        UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.drum_roll), cassette.getKey(), newJourney.getKey(), null);
                        userBadges.save();
                        user.setBadge(userBadges);
                        user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                        result.add(userBadges);
                    }
                }
            } else {
                ArrayList<String> list = new ArrayList<>();
                list.add(cassette.getKey());
                drumRoll.put(newJourney.getStreet(), list);
            }

            user.setDrumRoll(drumRoll);
            user.save();
        }

        // DROP D
        if (newJourney.getTimestamp() - user.getLastHideTimestamp() < 1000 * 60) {
            if (user.getBadges().containsKey(context.getString(R.string.drop_d)) == false && (distance > 20)) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.drop_d), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // WALK THE LINE
        if (newJourney.getState() != null && lastJourney.getState() != null) {
            if (newJourney.getState().equals(lastJourney.getState()) == false) {
                if (user.getBadges().containsKey(context.getString(R.string.walk_the_line)) == false) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.walk_the_line), cassette.getKey(), newJourney.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }
            }
        }

        // MOSH PIT
        GeoFire geoFire = new GeoFire(
                dbRef.child(context.getString(R.string.geo)).child(context.getString(R.string.cassettes)));
        final Map<String, Long> moshPit = new HashMap<>();
        GeoQuery query = geoFire.queryAtLocation(
                new GeoLocation(
                        newJourney.getLocation().getLatitude(),
                        newJourney.getLocation().getLongitude()),
                0.050);
        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (key != null && location != null) {
                    dbRef.child(context.getString(R.string.journeys)).child(key)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> value = (Map<String, Object>) child.getValue();
                                        Long timestamp = ((Number) value.get(context.getString(R.string.timestamp))).longValue();
                                        String userRef = (String) value.get(context.getString(R.string.user_ref));

                                        if (user.getLastHideTimestamp() - timestamp < 60 * 10) {
                                            Log.w("TEST", "Added to the Mosh Pit " + userRef);
                                            moshPit.put(userRef, timestamp);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(context.getString(R.string.hb_log_tag), databaseError.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onKeyExited(String key) {}

            @Override
            public void onKeyMoved(String key, GeoLocation location) {}

            @Override
            public void onGeoQueryReady() {
                if (moshPit.size() > 4) {
                    for (String key : moshPit.keySet()) {
                        dbRef.child(context.getString(R.string.users))
                                .child(key)
                                .child(context.getString(R.string.badges))
                                .child(context.getString(R.string.mosh_pit))
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            return;
                                        }

                                        UserBadges userBadges = new UserBadges(lastJourney.getUserRef(), context.getString(R.string.mosh_pit), cassette.getKey(), newJourney.getKey(), null);
                                        userBadges.save();
                                        dataSnapshot.getRef().setValue(userBadges.getKey());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(context.getString(R.string.hb_log_tag), databaseError.getMessage());
                                    }
                                });
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.w(context.getString(R.string.hb_log_tag), error.getMessage());
            }
        });

        // FLYING V
        int index = 0;
        boolean flyingV = true;

        for (final Journey journey : cassette.getJourneys()) {
            switch (index) {
                case 2:
                    if (journey.getUserRef().equals(user.getKey())) {
                        flyingV = false;
                    }
                    break;
                case 4:
                    if (journey.getUserRef().equals(user.getKey()) == false) {
                        flyingV = false;
                    }
                    break;
                default:
                    break;
            }

            index += 1;
        }

        if (flyingV && index > 3) {
            if (user.getBadges().containsKey(context.getString(R.string.flying_v)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.flying_v), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        // COAST TO COAST
        boolean westContainsNewLoc = PolyUtil.containsLocation(new LatLng(newJourney.getLocation().getLatitude(), newJourney.getLocation().getLongitude()), westCoast, true);
        boolean eastContainsNewLoc = PolyUtil.containsLocation(new LatLng(newJourney.getLocation().getLatitude(), newJourney.getLocation().getLongitude()), eastCoast, true);
        boolean westContainsOldLoc = PolyUtil.containsLocation(new LatLng(lastJourney.getLocation().getLatitude(), lastJourney.getLocation().getLongitude()), westCoast, true);
        boolean eastContainsOldLoc = PolyUtil.containsLocation(new LatLng(lastJourney.getLocation().getLatitude(), lastJourney.getLocation().getLongitude()), eastCoast, true);
        boolean coastToCoast = false;
        if (westContainsNewLoc && eastContainsOldLoc) {
            coastToCoast = true;
        }
        if (eastContainsNewLoc && westContainsOldLoc) {
            coastToCoast = true;
        }

        if (coastToCoast) {
            if (user.getBadges().containsKey(context.getString(R.string.coast_to_coast)) == false) {
                UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.coast_to_coast), cassette.getKey(), newJourney.getKey(), null);
                userBadges.save();
                user.setBadge(userBadges);
                user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                result.add(userBadges);
            }
        }

        return result;
    }

    public static ArrayList<UserBadges> awardBadgesForLeavingHint(final Context context, final Hint hint, final User user, final Cassette cassette, final Journey journey) {

        ArrayList<UserBadges> result = new ArrayList<>();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        if (hint.getImageRef() != null) {
            if (user.getBadges().containsKey(context.getString(R.string.paparazzi)) == false) {
                user.incrementPaparazziCount();
                if (user.getPaparazziCount() > 2) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.paparazzi), cassette.getKey(), journey.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }
            }
        }

        if (hint.getDescription() != null && hint.getDescription().equals("No Comment.") == false && hint.getDescription().equals("") == false) {
            if (user.getBadges().containsKey(context.getString(R.string.letter_pounder)) == false) {
                user.incrementLetterPounderCount();
                if (user.getLetterPounderCount() > 4) {
                    UserBadges userBadges = new UserBadges(user.getKey(), context.getString(R.string.letter_pounder), cassette.getKey(), journey.getKey(), null);
                    userBadges.save();
                    user.setBadge(userBadges);
                    user.getRef().child("badges").child(userBadges.getBadge()).child(userBadges.getKey());
                    result.add(userBadges);
                }
            }
        }

        return result;
    }
}
