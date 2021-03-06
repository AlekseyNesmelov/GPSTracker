package com.nesmelov.alexey.gpstracker.repository;

import android.location.Geocoder;

import com.nesmelov.alexey.gpstracker.repository.storage.AppDatabase;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Implementation of addresses repository.
 */
public class AddressesRepository implements IAddressesRepository {

    private AppDatabase mDatabase;

    @Inject
    public AddressesRepository(final AppDatabase database) {
        mDatabase = database;
    }

    @Override
    public Flowable<List<Address>> getRecentAddresses() {
        return mDatabase.addressDao().getRecents();
    }

    @Override
    public Flowable<List<Address>> getFavouriteAddresses() {
        return mDatabase.addressDao().getFavourites();
    }

    @Override
    public Completable addAddress(final Address address) {
        return Completable.create(emitter -> mDatabase.addressDao().insert(address))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable updateAddressFavourite(final Address address) {
        return Completable.create(emitter -> mDatabase.addressDao()
                .updateAddressFavourite(address.name, address.favourite))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Flowable<List<Alarm>> getAlarms() {
        return mDatabase.alarmDao().getAlarms();
    }

    @Override
    public Flowable<List<Alarm>> getTurnedOnAlarms() {
        return mDatabase.alarmDao().getTurnedOnAlarms();
    }

    @Override
    public Completable addAlarm(Alarm alarm) {
        return Completable.create(emitter -> mDatabase.alarmDao().insert(alarm))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
