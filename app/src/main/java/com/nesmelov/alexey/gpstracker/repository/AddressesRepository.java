package com.nesmelov.alexey.gpstracker.repository;

import android.location.Geocoder;

import com.nesmelov.alexey.gpstracker.repository.storage.AppDatabase;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Implementation of addresses repository.
 */
public class AddressesRepository implements IAddressesRepository {

    private static final int MAX_SEARCH_RESULTS = 10;

    private Geocoder mGeocoder;
    private AppDatabase mDatabase;

    @Inject
    public AddressesRepository(final Geocoder geocoder, final AppDatabase database) {
        mGeocoder = geocoder;
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
    public Observable<List<Address>> getAddressesByPlace(final String place) {
        return Observable.fromCallable(() -> {
            final List<android.location.Address> addresses = mGeocoder.getFromLocationName(place, MAX_SEARCH_RESULTS);
            if (addresses == null) {
                return new ArrayList<android.location.Address>();
            }
            return addresses;
        }).map(addresses -> {
            final List<Address> result = new ArrayList<>();
            for (final android.location.Address a : addresses) {
                result.add(Address.fromGeoAddress(a));
            }
            return result;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
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
