package com.hipsterbait.android.other;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.PointsNotFound;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.Hint;
import com.hipsterbait.android.models.Journey;
import com.hipsterbait.android.models.Points;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserPoints;

import java.util.ArrayList;

public class PointsEarningsManager {

    public static ArrayList<UserPoints> awardPointsForFinding(Context context, User user, Cassette cassette, Journey newJourney, Journey lastJourney, boolean rated) {
        ArrayList<UserPoints> result = new ArrayList<>();

        if (user.getFoundCount() < 2) {
            try {
                Points points = PointsStore.getInstance().getPoints(context.getString(R.string.first_find_points));
                UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), newJourney.getKey(), null);
                userPoints.save();

                result.add(userPoints);
            } catch (PointsNotFound e) {
                Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
            }
        }

        try {
            Points points = PointsStore.getInstance().getPoints(context.getString(R.string.found_announced));
            UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), newJourney.getKey(), null);
            userPoints.save();

            result.add(userPoints);
        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        if (rated) {
            try {
                Points points = PointsStore.getInstance().getPoints(context.getString(R.string.rate_a_song));
                UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), newJourney.getKey(), null);
                userPoints.save();

                result.add(userPoints);
            } catch (PointsNotFound e) {
                Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
            }
        }

        return result;
    }

    public static ArrayList<UserPoints> awardPointsForHiding(final Context context, User user, Cassette cassette, final Journey newJourney, final Journey lastJourney) {

        ArrayList<UserPoints> result = new ArrayList<>();

        try {
            Points points = PointsStore.getInstance().getPoints(context.getString(R.string.hide_cassette));
            UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), newJourney.getKey(), null);

            if (user.getHiddenCount() == 0) {
                userPoints.setValue(userPoints.getValue() + points.getIncrement());
            }

            userPoints.save();

            result.add(userPoints);

        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        try {
            Points points = PointsStore.getInstance().getPoints(context.getString(R.string.quick_hide));
            UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), newJourney.getKey(), null);

            long hiddenAt = newJourney.getTimestamp();
            long foundAt = lastJourney.getTimestamp();
            long interval = hiddenAt - foundAt;
            if (interval > 24 * 60 * 60 * 1000) {
                userPoints.setValue(userPoints.getValue() - points.getIncrement());
            }
            if (interval > 48 * 60 * 60 * 1000) {
                userPoints.setValue(userPoints.getValue() - points.getIncrement());
            }

            if (userPoints.getValue() > 0) {
                userPoints.save();
                result.add(userPoints);
            }

        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        try {
            Points points = PointsStore.getInstance().getPoints(context.getString(R.string.hide_distance));
            UserPoints userPoints = null;

            Location hiddenLoc = newJourney.getLocation();
            Location foundLoc = newJourney.getLocation();

            float distance = hiddenLoc.distanceTo(foundLoc);
            if (distance > 1 * 25 * 1000) {
                userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), newJourney.getKey(), null);
            }

            int increments = (int) Math.floor(distance / 1000 * 25);

            if (increments > 8) {
                increments = 8;
            }

            for (int index = 1; index < 8; index += 1) {
                if (userPoints != null)
                    userPoints.setValue(userPoints.getValue() + points.getIncrement());
            }

            if (userPoints != null) {
                userPoints.save();
                result.add(userPoints);
            }

        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        try {
            if (newJourney.getCountry() != null && lastJourney.getCountry() != null) {
                if (newJourney.getCountry().equals(lastJourney.getCountry()) == false) {
                    Points points = PointsStore.getInstance().getPoints(context.getString(R.string.hide_distance));
                    UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), newJourney.getKey(), null);
                    userPoints.save();
                    result.add(userPoints);
                }
            }

        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        return result;
    }

    public static ArrayList<UserPoints> awardPointsForLeavingHint(final Context context, Hint hint, User user, Cassette cassette, final Journey journey) {

        ArrayList<UserPoints> result = new ArrayList<>();

        try {
            if (hint.getDescription() != null && hint.getDescription().equals("No Comment.") == false && hint.getDescription().equals("") == false) {
                Points points = PointsStore.getInstance().getPoints(context.getString(R.string.text_hint));
                UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), journey.getKey(), null);
                userPoints.save();
                result.add(userPoints);
            }

        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        try {
            if (hint.getImageRef() != null) {
                Points points = PointsStore.getInstance().getPoints(context.getString(R.string.photo_hint));
                UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), journey.getKey(), null);
                userPoints.save();
                result.add(userPoints);
            }

        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        return result;
    }

    public static ArrayList<UserPoints> awardPointsForLeavingHint(final Context context, User user, Cassette cassette, final Journey journey, boolean fbShare, boolean twShare, boolean igShare) {

        ArrayList<UserPoints> result = new ArrayList<>();

        try {
            if (fbShare) {
                Points points = PointsStore.getInstance().getPoints(context.getString(R.string.share_hint_facebook));
                UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), journey.getKey(), null);
                userPoints.save();
                result.add(userPoints);
            }

        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        try {
            if (twShare) {
                Points points = PointsStore.getInstance().getPoints(context.getString(R.string.share_hint_twitter));
                UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), journey.getKey(), null);
                userPoints.save();
                result.add(userPoints);
            }

        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        try {
            if (igShare) {
                Points points = PointsStore.getInstance().getPoints(context.getString(R.string.share_hint_instagram));
                UserPoints userPoints = new UserPoints(user.getKey(), points.getKey(), points.getValue(), cassette.getKey(), journey.getKey(), null);
                userPoints.save();
                result.add(userPoints);
            }

        } catch (PointsNotFound e) {
            Log.w(context.getString(R.string.hb_log_tag), e.getLocalizedMessage());
        }

        return result;
    }

}
