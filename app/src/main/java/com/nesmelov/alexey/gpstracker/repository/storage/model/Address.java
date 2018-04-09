package com.nesmelov.alexey.gpstracker.repository.storage.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.sql.Date;

@Entity
public class Address {

    @NonNull
    @PrimaryKey
    public String name = "";
    public String details = "";
    public Boolean favourite;
    public double lat;
    public double lon;
    public Date date;

    /**
     * Creates instance by google place.
     *
     * @param place google place to create entity.
     * @return address instance.
     */
    public static Address fromGeoAddress(final Place place) {
        final Address result = new Address();
        result.name = place.getName().toString();
        result.details = place.getAddress().toString();
        result.favourite = false;
        result.date = new Date(System.currentTimeMillis());
        final LatLng latLng = place.getLatLng();
        result.lat = latLng.latitude;
        result.lon = latLng.longitude;

        return result;
    }
}
