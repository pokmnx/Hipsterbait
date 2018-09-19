package com.hipsterbait.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Hint;

import java.util.ArrayList;
import java.util.Date;

public class DetailsPhotosFragment extends Fragment {

    private ArrayList<String> mData;
    private ArrayList<Hint> mHints;
    private PhotosAdapter mAdapter;
    private Cassette mCassette;

    public DetailsPhotosFragment() {}

    public static DetailsPhotosFragment newInstance() {
        DetailsPhotosFragment fragment = new DetailsPhotosFragment();

        fragment.mData = new ArrayList<>();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details_photos, container, false);

        GridView gridView = (GridView) view.findViewById(R.id.details_photos_gridview);
        mAdapter = new PhotosAdapter();
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: Present fullscreen image activity
            }
        });

        return view;
    }

    public ArrayList<Hint> getHints() { return mHints; }

    public void setData(ArrayList<String> data, ArrayList<Hint> hints, Cassette cassette) {
        mData = data;
        mHints = hints;
        mCassette = cassette;
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    private class PhotosAdapter extends BaseAdapter {
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = new View(getActivity());

                gridView = inflater.inflate(R.layout.hint_photo_griditem, null);

            } else {
                gridView = convertView;
            }

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            final int finalPosition = position;
            ImageView imageView = (ImageView) gridView.findViewById(R.id.hint_griditem_photo);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Hint> hints = new ArrayList<Hint>();
                    if (mHints != null) {
                        for (Hint hint : mHints) {
                            hints.add(hint);
                        }
                    }
                    Hint cassetteHint = new Hint("", "", "", "", "Hipster Bait", "Cassette", new Date().getTime(), null);
                    cassetteHint.setDummyRef();
                    cassetteHint.setHintImageURL(mCassette.getCassetteModel().getCassetteArtURL());
                    hints.add(cassetteHint);
                    Hint coverHint = new Hint("", "", "", "", "Hipster Bait", "Cover Art", new Date().getTime(), null);
                    coverHint.setDummyRef();
                    coverHint.setHintImageURL(mCassette.getCassetteModel().getCoverArtURL());
                    hints.add(coverHint);

                    Intent intent = new Intent(getActivity(), PhotoReelActivity.class);
                    intent.putParcelableArrayListExtra(getString(R.string.hints_extra), hints);
                    intent.putExtra(getString(R.string.index_extra), finalPosition);
                    startActivity(intent);
                }
            });
            String imageUrl = mData.get(position);
            if (imageUrl != null) {
                try {
                    Glide.with(getActivity())
                        .load(imageUrl)
                        .into(imageView);
                } catch (Exception e) {
                    Log.w(getString(R.string.hb_log_tag), e);
                }
            }

            return gridView;
        }

        @Override
        public int getCount() {
            if (mData == null) {
                mData = new ArrayList<>();
            }

            return mData.size();
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
}
