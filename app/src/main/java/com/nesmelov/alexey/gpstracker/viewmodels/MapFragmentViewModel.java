package com.nesmelov.alexey.gpstracker.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.nesmelov.alexey.gpstracker.application.TrackerApplication;
import com.nesmelov.alexey.gpstracker.application.utils.LocationUtils;
import com.nesmelov.alexey.gpstracker.repository.AddressesRepository;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * View model for map fragment.
 */
public class MapFragmentViewModel extends ViewModel {

    @Inject
    AddressesRepository mAddressesRepository;

    private MutableLiveData<Boolean> mIsMenuOpened;
    private MutableLiveData<Boolean> mIsFollowMe;
    private MutableLiveData<LatLng> mCameraPos;

    private MutableLiveData<List<Alarm>> mAlarms;

    public MapFragmentViewModel() {
        TrackerApplication.getAppUtilsComp().inject(this);
    }

    public MutableLiveData<List<Alarm>> getAlarms() {
        if (mAlarms == null) {
            mAlarms = new MutableLiveData<>();
            mAddressesRepository.getAlarms()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mAlarms::setValue);
        }
        return mAlarms;
    }

    /**
     * Returns live data for camera position.
     *
     * @return live data for camera position.
     */
    public MutableLiveData<LatLng> getCameraPos() {
        if (mCameraPos == null) {
            mCameraPos = new MutableLiveData<>();
        }
        return mCameraPos;
    }

    /**
     * Returns live data for menu state.
     *
     * @return live data for menu state.
     */
    public MutableLiveData<Boolean> getMenuOpenedState() {
        if (mIsMenuOpened == null) {
            mIsMenuOpened = new MutableLiveData<>();
        }
        return mIsMenuOpened;
    }

    /**
     * Returns live data for follow me state.
     *
     * @return live data for follow me state.
     */
    public MutableLiveData<Boolean> getFollowMeState() {
        if (mIsFollowMe == null) {
            mIsFollowMe = new MutableLiveData<>();
        }
        return mIsFollowMe;
    }

    /**
     * Click menu event.
     */
    public void clickMenu() {
        final Boolean prevState = getMenuOpenedState().getValue();
        getMenuOpenedState().setValue(prevState == null || !prevState);
    }

    /**
     * Click follow me event.
     */
    public void clickFollowMe() {
        final Boolean prevState = getFollowMeState().getValue();
        getFollowMeState().setValue(prevState == null || !prevState);
    }
}
