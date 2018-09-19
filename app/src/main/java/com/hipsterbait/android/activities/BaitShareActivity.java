package com.hipsterbait.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookDialog;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Hint;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserPoints;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.RotatingTexts;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BaitShareActivity extends ImmersiveActivity {

    private static int TWEET_REQUEST_CODE = 213;
    public ImageButton fbButton, twButton, igButton;
    private boolean fbShare = false, twShare = false, igShare = false;
    private Cassette mCassette;
    private Journey mJourney;
    private User mUser;
    private Hint mHint;
    private Uri mPhotoUri;
    private DatabaseReference mDbRef;
    private CallbackManager callbackManager;
    private TwitterBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bait_share);

        fbButton = findViewById(R.id.bait_share_fb);
        twButton = findViewById(R.id.bait_share_twit);
        igButton = findViewById(R.id.bait_share_insta);

        mUser = ((HBApplication) getApplication()).user;
        mDbRef = FirebaseDatabase.getInstance().getReference();

        String cassetteKey = getIntent().getStringExtra(getString(R.string.cassette_extra_key));
        mCassette = mUser.getCassetteByKey(cassetteKey);
        mJourney = getIntent().getParcelableExtra(getString(R.string.journey_extra));
        mHint = getIntent().getParcelableExtra(getString(R.string.hint_extra));
        String photoExtra = getIntent().getStringExtra(getString(R.string.image_extra));
        if (photoExtra != null) {
            mPhotoUri = Uri.parse(photoExtra);
        }
        ArrayList<UserPoints> result = getIntent().getParcelableArrayListExtra(getString(R.string.userpoints_arraylist_arg));
        int points = getIntent().getIntExtra(getString(R.string.old_points), 0);

        int newPoints = 0;
        for (UserPoints userPoints : result) {
            newPoints += userPoints.getValue();
        }

        Bundle args = new Bundle();
        args.putString(getString(R.string.title_string_arg),
                RotatingTexts.getString(RotatingTexts.HINT_EARNINGS));
        args.putParcelableArrayList(getString(R.string.userpoints_arraylist_arg), result);
        args.putInt(getString(R.string.old_points), points);
        args.putInt(getString(R.string.new_points), newPoints);
        BottomSheetDialogFragment pointsFrag = new PointsReceiptFragment();
        pointsFrag.setArguments(args);
        pointsFrag.show(getSupportFragmentManager(), pointsFrag.getTag());
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.twitter.sdk.android.tweetcomposer.UPLOAD_SUCCESS");
        filter.addAction("com.twitter.sdk.android.tweetcomposer.UPLOAD_FAILURE");

        receiver = new TwitterBroadcastReceiver();
        this.registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        this.unregisterReceiver(receiver);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TWEET_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                twShare = true;
                twButton.setEnabled(false);
                twButton.setImageAlpha(0x3F);
            } else {
                Log.w("TEST", "RESULT " + String.valueOf(resultCode));
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void facebookTapped(View v) {
        callbackManager = CallbackManager.Factory.create();

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.no_hint);

        if (mPhotoUri != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            image = BitmapFactory.decodeFile(mPhotoUri.getPath(), options);
        }

        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .setUserGenerated(true)
                .setCaption(mHint.getDescription())
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .setShareHashtag(new ShareHashtag.Builder().setHashtag("#HipsterBait").build())
                .build();

        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                fbShare = true;
                fbButton.setEnabled(false);
                fbButton.setImageAlpha(0x3F);
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {
                Log.w(getString(R.string.hb_log_tag), error.getLocalizedMessage());
            }
        });

        if (shareDialog.canShow(content, ShareDialog.Mode.AUTOMATIC)) {
            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
        } else {
            Log.e("HB", "Can't show Facebook ShareDialog");
        }
    }

    public void twitterTapped(View v) {

        PackageManager pkManager = getPackageManager();
        try {
            PackageInfo pkgInfo = pkManager.getPackageInfo("com.twitter.android", 0);
            String getPkgInfo = pkgInfo.toString();

            if (getPkgInfo.equals("com.twitter.android"))   {
                Toast.makeText(this, "Install Twitter to share this hint.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Install Twitter to share this hint.", Toast.LENGTH_SHORT).show();
            return;
        }

        FileOutputStream out = null;
        File file = new File(Environment.getExternalStorageDirectory().toString(), "cropped" + mHint.getKey() + ".jpg");
        String path = "";
        Uri uri = null;

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.no_hint);

        if (mPhotoUri != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            image = BitmapFactory.decodeFile(mPhotoUri.getPath(), options);
        }

        try {
            out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, out);

            path = MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            uri = Uri.parse(path);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (uri != null) {
            Intent intent = new TweetComposer.Builder(this)
                    .text(mHint.getDescription() + " #HipsterBait #BaitHint")
                    .image(uri)
                    .createIntent();
            startActivityForResult(intent, TWEET_REQUEST_CODE);
        } else {
            Log.e("HB", "Null uri for photo share");
        }
    }

    public void instagramTapped(View v) {

        if (verifyInstagram() == false) {
            Toast.makeText(this, "Install Instagram to share this hint.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");


        FileOutputStream out = null;
        File file = new File(Environment.getExternalStorageDirectory().toString(), "cropped" + mHint.getKey() + ".jpg");
        String path = "";
        Uri uri = null;

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.no_hint);

        if (mPhotoUri != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            image = BitmapFactory.decodeFile(mPhotoUri.getPath(), options);
        }

        try {
            out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, out);

            path = MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            uri = Uri.parse(path);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setPackage("com.instagram.android");

        startActivity(Intent.createChooser(share, "Share to"));

        igShare = true;
        igButton.setEnabled(false);
        igButton.setImageAlpha(0x3F);
    }

    private boolean verifyInstagram(){
        boolean installed = false;

        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.instagram.android", 0);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public void closeButtonTapped(View v) {

        if (fbShare || twShare || igShare) {
            Intent intent = new Intent(BaitShareActivity.this, BaitDoneActivity.class);
            intent.putExtra(getString(R.string.fb_share_extra), fbShare);
            intent.putExtra(getString(R.string.tw_share_extra), twShare);
            intent.putExtra(getString(R.string.ig_share_extra), igShare);
            intent.putExtra(getString(R.string.cassette_extra_key), mCassette.getKey());
            intent.putExtra(getString(R.string.journey_extra), mJourney);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    public class TwitterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction())) {
                // success
                final Long tweetId = intent.getExtras().getLong(TweetUploadService.EXTRA_TWEET_ID);
            } else {
                // failure
                final Intent retryIntent = intent.getExtras().getParcelable(TweetUploadService.EXTRA_RETRY_INTENT);
            }
        }
    }
}
