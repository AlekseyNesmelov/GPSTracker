package com.nesmelov.alexey.gpstracker.application.modules;

import android.location.Geocoder;

import com.nesmelov.alexey.gpstracker.repository.storage.AppDatabase;
import com.nesmelov.alexey.gpstracker.repository.AddressesRepository;
import com.nesmelov.alexey.gpstracker.repository.IAddressesRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AddressRepositoryModule {

    @Provides
    @Singleton
    IAddressesRepository provideAddressRepository(final Geocoder geocoder, final AppDatabase database) {
        return new AddressesRepository(geocoder, database);
    }
}
