package com.nesmelov.alexey.gpstracker.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.nesmelov.alexey.gpstracker.application.TrackerApplication;
import com.nesmelov.alexey.gpstracker.application.utils.LocationUtils;

import javax.inject.Inject;

/**
 * View model for map fragment.
 */
public class MapFragmentViewModel extends ViewModel {

    private MutableLiveData<Boolean> mIsMenuOpened;
    private MutableLiveData<Boolean> mIsFollowMe;
    private MutableLiveData<LatLng> mCameraPos;

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
