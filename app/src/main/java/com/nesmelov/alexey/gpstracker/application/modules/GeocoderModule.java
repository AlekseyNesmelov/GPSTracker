package com.nesmelov.alexey.gpstracker.application.modules;

import android.content.Context;
import android.location.Geocoder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class GeocoderModule {

    @Provides
    @Singleton
    Geocoder provideGeocoder(final Context context) {
        return new Geocoder(context);
    }
}
