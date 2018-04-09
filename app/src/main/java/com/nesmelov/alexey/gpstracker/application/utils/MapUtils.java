package com.nesmelov.alexey.gpstracker.application.utils;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import javax.inject.Inject;

/**
 * Module to manage map.
 */
public class MapUtils {

    private static final float MAX_ZOOM = 17;
    private static final float MIN_ZOOM = 0;
    private static final float START_ZOOM = 16f;
    private static final int CAMERA_SPEED = 100;

    private final LocationUtils mLocationUtils;

    /**
     *  Constructs location utils.
     *
     * @param locationUtils location utils component.
     */
    @Inject
    public MapUtils(final LocationUtils locationUtils) {
        mLocationUtils = locationUtils;
    }

    /**
     * Initiates map properties.
     *
     * @param map map to initiate.
     */
    public void initMap(final GoogleMap map) {
        final Location location = mLocationUtils.getLastKnownLocation();
        if (location != null) {
            moveCameraTo(map, START_ZOOM, location.getLatitude(), location.getLongitude());
        }
    }

    /**
     * Animates camera to the certain place.
     *
     * @param map google map.
     * @param zoom zoom to animate camera.
     * @param lat latitude of the place.
     * @param lon longitude of the place.
     */
    public void animateCameraTo(final GoogleMap map, final float zoom, final double lat, final double lon) {
        if (map != null) {
            final LatLng latLng = new LatLng(lat, lon);
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate, CAMERA_SPEED, null);
        }
    }

    /**
     * Moves camera to the certain place.
     *
     * @param map google map.
     * @param zoom zoom to animate camera.
     * @param lat latitude of the place.
     * @param lon longitude of the place.
     */
    public void moveCameraTo(final GoogleMap map, final float zoom, final double lat, final double lon) {
        if (map != null) {
            final LatLng latLng = new LatLng(lat, lon);
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.moveCamera(cameraUpdate);
        }
    }

    /**
     * Updates zoom level.
     *
     * @param map google map.
     * @param zoom zoom level to animate to.
     */
    public void updateZoom(final GoogleMap map, final float zoom) {
        if (map != null) {
            final LatLng cameraTarget = map.getCameraPosition().target;
            animateCameraTo(map, zoom, cameraTarget.latitude, cameraTarget.longitude);
        }
    }

    /**
     * Animates camera to the certain place.
     *
     * @param map google map.
     * @param lat latitude of the place.
     * @param lon longitude of the place.
     */
    public void animateCameraTo(final GoogleMap map, final double lat, final double lon) {
        if (map != null) {
            final LatLng latLng = new LatLng(lat, lon);
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            map.animateCamera(cameraUpdate);
        }
    }

    /**
     * Animates camera to the circle bounding box.
     *
     * @param map google map.
     * @param circle circle to animate camera to.
     */
    public void animateCameraTo(final GoogleMap map, final Circle circle) {
        final LatLng targetNorthEast = SphericalUtil.computeOffset(circle.getCenter(), circle.getRadius() * Math.sqrt(2), 45);
        final LatLng targetSouthWest = SphericalUtil.computeOffset(circle.getCenter(), circle.getRadius() * Math.sqrt(2), 225);
        if (map != null) {
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(new LatLngBounds(targetSouthWest, targetNorthEast), 0);
            map.animateCamera(cameraUpdate);
        }
    }

    /**
     * Zooming in.
     *
     * @param map google map.
     */
    public void zoomIn(final GoogleMap map) {
        if (map !=  null) {
            final float cameraZoom = map.getCameraPosition().zoom;
            if (cameraZoom < MAX_ZOOM) {
                updateZoom(map, cameraZoom + 1);
            }
        }
    }

    /**
     * Zooming out.
     *
     * @param map google map.
     */
    public void zoomOut(final GoogleMap map) {
        if (map !=  null) {
            final float cameraZoom = map.getCameraPosition().zoom;
            if (cameraZoom > MIN_ZOOM) {
                updateZoom(map, cameraZoom - 1);
            }
        }
    }
}
