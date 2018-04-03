package com.nesmelov.alexey.gpstracker.repository.storage.converters;

import android.arch.persistence.room.TypeConverter;

import java.sql.Date;

/**
 * Database converters.
 */
public class Converters {

    @TypeConverter
    public static Date fromTimestamp(final Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(final Date date) {
        return date == null ? null : date.getTime();
    }
}