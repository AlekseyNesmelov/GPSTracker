package com.nesmelov.alexey.gpstracker.application.modules;

import android.content.Context;

import com.nesmelov.alexey.gpstracker.application.utils.LocationUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class LocationModule {

    @Provides
    @Singleton
    LocationUtils provideLocationUtils(final Context context) {
        return new LocationUtils(context);
    }
}
