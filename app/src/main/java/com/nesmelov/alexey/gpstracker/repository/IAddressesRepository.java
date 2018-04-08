package com.nesmelov.alexey.gpstracker.repository;

import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * GPS tracker repository interface.
 */
public interface IAddressesRepository {
    /**
     * Gets recent addresses.
     *
     * @return flowable of recent addresses.
     */
    Flowable<List<Address>> getRecentAddresses();

    /**
     * Gets favourite addresses.
     *
     * @return flowable of favourite addresses.
     */
    Flowable<List<Address>> getFavouriteAddresses();

    /**
     * Adds a new address to the repository.
     *
     * @param address address to add.
     * @return completable of the action.
     */
    Completable addAddress(final Address address);

    /**
     * Updates address favourite flag in the repository.
     *
     * @param address address to update.
     * @return completable of the action.
     */
    Completable updateAddressFavourite(final Address address);

    /**
     * Gets alarms.
     *
     * @return flowable of alarms.
     */
    Flowable<List<Alarm>> getAlarms();

    /**
     * Gets turned on alarms.
     *
     * @return flowable of turned on alarms.
     */
    Flowable<List<Alarm>> getTurnedOnAlarms();

    /**
     * Adds a new alarm to the repository.
     *
     * @param alarm alarm to add.
     * @return completable of the action.
     */
    Completable addAlarm(final Alarm alarm);
}
