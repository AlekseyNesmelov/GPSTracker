package com.nesmelov.alexey.gpstracker.repository.storage.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.sql.Date;

@Entity
public class Address {

    @NonNull
    @PrimaryKey
    public String name = "";
    public Boolean favourite;
    public double lat;
    public double lon;
    public Date date;

    /**
     * Creates instance by google address.
     *
     * @param geoAddress google address to create entity.
     * @return address instance.
     */
    public static Address fromGeoAddress(final android.location.Address geoAddress) {
        final Address result = new Address();
        final StringBuilder sb = new StringBuilder();
        if (geoAddress.getMaxAddressLineIndex() != -1) {
            for (int i = 0; i < geoAddress.getMaxAddressLineIndex(); i++) {
                sb.append(geoAddress.getAddressLine(i));
            }
            sb.append(geoAddress.getAddressLine(geoAddress.getMaxAddressLineIndex()));
        }
        result.name = sb.toString();
        result.favourite = false;
        result.date = new Date(System.currentTimeMillis());
        result.lat = geoAddress.getLatitude();
        result.lon = geoAddress.getLongitude();

        return result;
    }
}
