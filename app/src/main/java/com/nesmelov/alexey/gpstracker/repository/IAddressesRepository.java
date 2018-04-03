package com.nesmelov.alexey.gpstracker.repository;

import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;

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
     * @return live data of recent addresses.
     */
    Flowable<List<Address>> getRecentAddresses();

    /**
     * Gets favourite addresses.
     *
     * @return live data of favourite addresses.
     */
    Flowable<List<Address>> getFavouriteAddresses();

    /**
     * Gets live data of addresses by place.
     *
     * @return live data of addresses by place.
     */
    Observable<List<Address>> getAddressesByPlace(final String place);

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
}
