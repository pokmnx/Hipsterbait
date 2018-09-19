package com.hipsterbait.android.other;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.StorageReference;
import com.hipsterbait.android.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CacheManager {

    private static CacheManager singleton = null;
    private File mImagesStorage;
    private File mMusicStorage;

    private CacheManager(final Context context) {
        mImagesStorage = new File(context.getFilesDir(), context.getString(R.string.images));
        mMusicStorage = new File(context.getFilesDir(), context.getString(R.string.music));
        mImagesStorage.mkdir();
        mMusicStorage.mkdir();
    }

    public static CacheManager getInstance() {
        if (singleton == null) {
            singleton = new CacheManager(HBApplication.getInstance());
        }
        return singleton;
    }

    public byte[] getImageData(StorageReference reference) throws FileNotFoundException, IOException {

        String[] parts = reference.toString().split("/");

        String pathResult = "";

        for (int i = 0; i < parts.length - 1; i++) {
            if (i < 2) {
                continue;
            }

            pathResult += File.separator + parts[i];
        }

        File dir = new File(mImagesStorage + pathResult);
        File file = new File(dir, parts[parts.length - 1]);

        if (file.exists()) {

            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while (inputStream.available() > 0) {
                outputStream.write(inputStream.read());
            }
            byte[] result = outputStream.toByteArray();
            inputStream.close();
            outputStream.close();

            return result;

        } else {
            throw new FileNotFoundException(file.getAbsolutePath() + " not found");
        }
    }

    public void cacheImageData(byte[] data, StorageReference reference) {

        String[] parts = reference.toString().split("/");

        String pathResult = "";

        for (int i = 0; i < parts.length - 1; i++) {
            if (i < 2) {
                continue;
            }

            pathResult += File.separator + parts[i];
        }

        File dir = new File(mImagesStorage + pathResult);
        dir.mkdirs();

        try {
            File file = new File(dir, parts[parts.length - 1]);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();

        } catch (Exception e) {
            Log.w("HB", "Failed to cache image: " + e.getLocalizedMessage());
        }
    }

    public Uri getSongData(StorageReference reference) throws FileNotFoundException, IOException {
        String[] parts = reference.toString().split("/");

        String pathResult = "";

        for (int i = 0; i < parts.length - 1; i++) {
            if (i < 2) {
                continue;
            }

            pathResult += File.separator + parts[i];
        }

        File dir = new File(mMusicStorage + pathResult);
        File file = new File(dir, parts[parts.length - 1]);

        if (file.exists()) {

            return Uri.parse(file.toURI().toString());

        } else {
            throw new FileNotFoundException(file.getAbsolutePath() + " not found");
        }
    }

    public Uri cacheSongData(byte[] data, StorageReference reference) {

        String[] parts = reference.toString().split("/");

        String pathResult = "";

        for (int i = 0; i < parts.length - 1; i++) {
            if (i < 2) {
                continue;
            }

            pathResult += File.separator + parts[i];
        }

        File dir = new File(mMusicStorage + pathResult);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Log.w("TEST", "dir created");
            } else {
                Log.w("TEST", "dir not created");
            }
        }

        try {
            File file = new File(dir, parts[parts.length - 1]);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();

            return Uri.parse(file.toURI().toString());

        } catch (Exception e) {
            Log.w("HB", "Failed to cache song: " + e.getLocalizedMessage());
            return null;
        }
    }
}