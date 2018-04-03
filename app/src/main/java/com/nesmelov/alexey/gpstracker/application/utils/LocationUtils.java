package com.nesmelov.alexey.gpstracker.application.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Provides location utils.
 */
public class LocationUtils {

    private Context mContext;

    /**
     * Constructs location utils.
     *
     * @param context context to use.
     */
    public LocationUtils(final Context context) {
        mContext = context;
    }

    /**
     * Returns last known location.
     *
     * @return last known location.
     */
    public Location getLastKnownLocation() {
        Location bestLocation = null;
        if (ActivityCompat.checkSelfPermission(
                mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                final List<String> providers = locationManager.getProviders(true);
                for (final String provider : providers) {
                    final Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null && (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy())) {
                        bestLocation = location;
                    }
                }
            }
        }
        return bestLocation;
    }
}
