package com.hipsterbait.android.other;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.RankNotFound;
import com.hipsterbait.android.Resources.Exceptions.RequiredValueMissing;
import com.hipsterbait.android.activities.BadgeNotificationActivity;
import com.hipsterbait.android.models.NotificationItem;
import com.hipsterbait.android.models.Points;
import com.hipsterbait.android.models.Rank;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserPoints;
import com.hipsterbait.android.models.UserRank;
import com.twitter.sdk.android.core.Twitter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class HBApplication extends Application {

    private static HBApplication instance;

    public User user = null;
    private boolean isLoggedIn = false;
    public boolean loaded = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Twitter.initialize(this);

        RanksStore.getInstance();
        BadgesStore.getInstance();
        PointsStore.getInstance();

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.w("APP", "connected");
                } else {
                    Log.w("APP", "not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("APP", "cancelled");
            }
        });

        FirebaseAuth.getInstance()
                .addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                        if (firebaseUser != null) {

                            if (isLoggedIn) {
                                return;
                            }

                            isLoggedIn = true;

                            FirebaseDatabase.getInstance().getReference()
                                    .child(getApplicationContext().getString(R.string.users))
                                    .child(firebaseUser.getUid())
//                                    .child("WkEo8QUGooMjTUo8clbk6kzmRV52")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.exists()) {
                                                Log.e("TEST", "snapshot exists");
                                                try {
                                                    user = new User(dataSnapshot);
                                                    user.save();
                                                    Log.e("TEST", "user set");
                                                } catch (RequiredValueMissing e) {
                                                    Log.w(getString(R.string.hb_log_tag),
                                                            e.getLocalizedMessage());
                                                }

                                            } else {
                                                Log.e("TEST", "snapshot doesn't exists");
                                                String firstname = null;
                                                String lastname = null;

                                                String name = firebaseUser.getDisplayName();

                                                if (name != null) {
                                                    String[] parts = name.split(" ");

                                                    firstname = parts[0];
                                                    if (parts.length > 1) {
                                                        lastname = parts[1];
                                                    }
                                                }

                                                String email = firebaseUser.getEmail();

                                                if (email == null) {
                                                    email = "";
                                                }

                                                if (name == null) {
                                                    String[] parts = email.split("@");
                                                    name = parts[0];
                                                }

                                                Uri photoUrl = firebaseUser.getPhotoUrl();
                                                String photoUrlString = null;

                                                if (photoUrl != null) {
                                                    photoUrlString = photoUrl.toString();
                                                }

                                                user = new User(email, name, firstname, lastname, photoUrlString, new HashMap<String, String>(), getString(R.string.roadie), null, true, 0, 0, firebaseUser.getUid());

                                                user.save();
                                                Log.e("TEST", "user set");

                                                if (photoUrl != null) {
                                                    new UploadAvatarTask(HBApplication.this, user).execute(photoUrlString, user.getKey());
                                                }

                                                if (firebaseUser.isEmailVerified() == false) {
                                                    firebaseUser.sendEmailVerification();
                                                }

                                                if (user.getRanks().size() < 1) {
                                                    // User always gets the Roadie rank
                                                    try {
                                                        Rank roadieRank = RanksStore.getInstance().getRank(1);
                                                        UserRank userRank = new UserRank(user.getKey(), roadieRank.getKey(), null);
                                                        userRank.save();

                                                        user.addRank(roadieRank, userRank);
                                                        user.setNotification(roadieRank.getKey());
                                                        user.save();

                                                        ArrayList<NotificationItem> items = new ArrayList<>();
                                                        items.add(new NotificationItem(roadieRank.getKey(), true));

                                                        Intent intent = new Intent(HBApplication.this, BadgeNotificationActivity.class);
                                                        intent.putParcelableArrayListExtra(HBApplication.this.getString(R.string.arraylist_extra), items);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        HBApplication.this.startActivity(intent);

                                                    } catch (RankNotFound e) {
                                                        Log.w(getString(R.string.hb_log_tag), "Couldn't find rank Roadie.");
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.w(getString(R.string.hb_log_tag),
                                                    databaseError.getMessage());
                                        }
                                    });

                        } else {
                            isLoggedIn = false;

                            if (user != null) {
                                user.logout();
                            }
                        }
                    }
                });
    }

    public HBApplication() {
        instance = this;
    }

    public static HBApplication getInstance() {
        return instance;
    }
}

class UploadAvatarTask extends AsyncTask<String, Void, Void> {

    Context mContext;
    User mUser;

    public UploadAvatarTask(Context context, User user) {
        mContext = context.getApplicationContext();
        mUser = user;
    }

    protected Void doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while (input.available() > 0) {
                outputStream.write(input.read());
            }
            byte[] result = outputStream.toByteArray();
            input.close();
            outputStream.close();

            if (result.length > 0) {
                StorageReference reference = FirebaseStorage.getInstance().getReference()
                        .child("avatars")
                        .child(strings[1])
                        .child("thumbnail");
                reference.putBytes(result);

                CacheManager.getInstance().cacheImageData(result, reference);

                Points points = PointsStore.getInstance().getPoints("avatarSet");
                UserPoints userPoints = new UserPoints(strings[1], points.getKey(), points.getValue(), null, null, null);
                userPoints.save();
            }

        } catch (Exception e) {
            Log.w(mContext.getString(R.string.hb_log_tag), e);
        }

        return null;
    }

    protected void onPostExecute() {
        mUser.setImage();
    }
}