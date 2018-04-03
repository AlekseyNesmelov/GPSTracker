package com.nesmelov.alexey.gpstracker.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.nesmelov.alexey.gpstracker.application.TrackerApplication;
import com.nesmelov.alexey.gpstracker.repository.AddressesRepository;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * View model for search activity.
 */
public class SearchActivityViewModel extends ViewModel {

    @Inject AddressesRepository mAddressesRepository;

    private Disposable mGeocoderDisposable;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    private MutableLiveData<List<Address>> mRecentAddresses;
    private MutableLiveData<List<Address>> mFavouriteAddresses;
    private MutableLiveData<List<Address>> mFoundAddresses;

    /**
     * Constructs view model for search activity.
     */
    public SearchActivityViewModel() {
        TrackerApplication.getAppUtilsComp().inject(this);
    }

    /**
     * Gets live data for recent addresses.
     *
     * @return live data for recent addresses.
     */
    public MutableLiveData<List<Address>> getRecentAddresses() {
        if (mRecentAddresses == null) {
            mRecentAddresses = new MutableLiveData<>();
            mAddressesRepository.getRecentAddresses()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mRecentAddresses::setValue);
        }
        return mRecentAddresses;
    }

    /**
     * Gets live data for favourite addresses.
     *
     * @return live data for favourite addresses.
     */
    public MutableLiveData<List<Address>> getFavouriteAddresses() {
        if (mFavouriteAddresses == null) {
            mFavouriteAddresses = new MutableLiveData<>();
            mAddressesRepository.getFavouriteAddresses()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mFavouriteAddresses::setValue);
        }
        return mFavouriteAddresses;
    }

    /**
     * Gets live data for found addresses.
     *
     * @return live data for found addresses.
     */
    public MutableLiveData<List<Address>> getFoundAddresses() {
        if (mFoundAddresses == null) {
            mFoundAddresses = new MutableLiveData<>();
        }
        return mFoundAddresses;
    }

    /**
     * Request found addresses to update by input place.
     *
     * @param place place to find addresses.
     */
    public void requestPlace(final String place) {
        if (mGeocoderDisposable != null) {
            mGeocoderDisposable.dispose();
        }
        if (place.isEmpty()) {
            mFoundAddresses.setValue(new ArrayList<>());
        } else {
            mGeocoderDisposable = mAddressesRepository.getAddressesByPlace(place)
                    .subscribe(mFoundAddresses::setValue);
        }
    }

    /**
     * Adds a new address to the repository.
     *
     * @param address address to add.
     */
    public void addAddress(final Address address) {
        mAddressesRepository.addAddress(address).subscribe();
    }

    /**
     * Updates address favourite flag.
     *
     * @param address address to update.
     */
    public void updateAddressFavourite(final Address address) {
        mAddressesRepository.updateAddressFavourite(address).subscribe();
    }

    @Override
    protected void onCleared() {
        if (mGeocoderDisposable != null) {
            mGeocoderDisposable.dispose();
        }
        mDisposables.dispose();
    }
}
