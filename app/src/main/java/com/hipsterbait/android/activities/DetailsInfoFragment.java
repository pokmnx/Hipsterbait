package com.hipsterbait.android.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailsInfoFragment extends Fragment {

    public GridView gridView;

    private Map<String, Object> mData;
    private InfoCellAdapter mAdapter;

    public DetailsInfoFragment() {}

    public static DetailsInfoFragment newInstance() {
        DetailsInfoFragment fragment = new DetailsInfoFragment();

        fragment.mData = new HashMap<>();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details_info, container, false);

        gridView = (GridView) view.findViewById(R.id.details_info_gridview);
        mAdapter = new InfoCellAdapter();
        gridView.setAdapter(mAdapter);

        return view;
    }

    public void setData(Map<String, Object> data) {
        mData = data;

        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    private class InfoCellAdapter extends BaseAdapter {

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = new View(getActivity());

                if (position == 4) {
                    gridView = inflater.inflate(R.layout.journey_detail_avatar_grid_item, null);
                } else {
                    gridView = inflater.inflate(R.layout.journey_detail_grid_item, null);
                }

            } else {
                gridView = convertView;
            }

            HBTextView name = new HBTextView(getActivity());
            HBTextView detail = new HBTextView(getActivity());

            if (position != 4) {
                name = (HBTextView) gridView
                        .findViewById(R.id.journey_griditem_name);
                detail = (HBTextView) gridView
                        .findViewById(R.id.journey_griditem_detail);
            }

            switch (position) {
                case 0:
                default:
                    name.setText(R.string.name_capitalized);
                    String nameData = (String) mData.get(getString(R.string.name));
                    if (nameData != null) {
                        detail.setText(nameData);
                    }
                    break;
                case 1:
                    name.setText(R.string.date_hidden_capitalized);
                    String dateHiddenData = (String) mData.get(getString(R.string.date_hidden));
                    if (dateHiddenData != null) {
                        detail.setText(dateHiddenData);
                    }
                    break;
                case 2:
                    name.setText(R.string.location_capitalized);
                    name.setTextColor(ContextCompat.getColor(getActivity(), R.color.hbBlue));
                    String locationData = (String) mData.get(getString(R.string.location));
                    if (locationData != null) {
                        detail.setText(locationData);
                    }
                    break;
                case 3:
                    name.setText(getString(R.string.gps));
                    name.setTextColor(ContextCompat.getColor(getActivity(), R.color.hbBlue));
                    String gpsData = (String) mData.get(getString(R.string.gps));
                    if (gpsData != null) {
                        detail.setText(gpsData);
                    }
                    break;
                case 4:
                    CircleImageView image = (CircleImageView) gridView
                            .findViewById(R.id.journey_avatar_griditem_image);
                    String imageUrl = (String) mData.get(getString(R.string.image));
                    if (imageUrl != null) {
                        Glide.with(getActivity())
                                .load(imageUrl)
                                .into(image);
                    } else {
                        image.setImageDrawable(
                                ContextCompat.getDrawable(getActivity(), R.drawable.avatar_generic));
                    }

                    HBTextView username = (HBTextView) gridView
                            .findViewById(R.id.journey_avatar_griditem_name);
                    String usernameData = (String) mData.get(getString(R.string.username));
                    if (usernameData != null) {
                        username.setText(usernameData);
                    }
                    HBTextView rank = (HBTextView) gridView
                            .findViewById(R.id.journey_avatar_griditem_rank);

                    String rankData = (String) mData.get(getString(R.string.rank));
                    if (rankData != null) {
                        rank.setText(rankData);
                    }
                    break;
                case 5:
                    name.setText(R.string.some_words_about_this_cassette);
                    String descriptionData = (String) mData.get(getString(R.string.description));
                    if (descriptionData != null) {
                        detail.setText(descriptionData);
                        detail.requestLayout();
                    }
                    break;
            }

            gridView.requestLayout();

            return gridView;
        }

        @Override
        public int getCount() {
            return 6;
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
