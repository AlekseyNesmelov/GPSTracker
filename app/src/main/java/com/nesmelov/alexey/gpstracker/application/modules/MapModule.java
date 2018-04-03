package com.nesmelov.alexey.gpstracker.application.modules;

import com.nesmelov.alexey.gpstracker.application.utils.LocationUtils;
import com.nesmelov.alexey.gpstracker.application.utils.MapUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MapModule {

    @Provides
    @Singleton
    MapUtils provideMapUtils(final LocationUtils locationUtils) {
        return new MapUtils(locationUtils);
    }
}