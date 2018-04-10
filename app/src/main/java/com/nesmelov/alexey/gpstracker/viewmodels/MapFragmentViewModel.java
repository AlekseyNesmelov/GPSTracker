package com.nesmelov.alexey.gpstracker.viewmodels;

import android.Manifest;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.nesmelov.alexey.gpstracker.application.TrackerApplication;
import com.nesmelov.alexey.gpstracker.application.utils.LocationUtils;
import com.nesmelov.alexey.gpstracker.application.utils.MapUtils;
import com.nesmelov.alexey.gpstracker.repository.AddressesRepository;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * View model for map fragment.
 */
public class MapFragmentViewModel extends ViewModel {

    public static final int MODE_MENU_CLOSED = 0;
    public static final int MODE_MENU_OPENED = 1;
    public static final int MODE_ALARM_POS = 2;
    public static final int MODE_ALARM_RADIUS = 3;

    @Inject AddressesRepository mAddressesRepository;

    private MutableLiveData<Boolean> mIsFollowMe;
    private MutableLiveData<Integer> mMode;
    private MutableLiveData<List<Alarm>> mAlarms;

    private GoogleMap mMap;
    private Circle mAlarmRadius;
    @Inject MapUtils mMapUtils;
    @Inject LocationUtils mLocationUtils;

    /**
     * Constructs view model for map fragment.
     */
    public MapFragmentViewModel() {
        TrackerApplication.getAppUtilsComp().inject(this);
        TrackerApplication.getMapUtilsComp().inject(this);
        TrackerApplication.getLocationUtilsComp().inject(this);
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

    public void initMap(final Context context, final GoogleMap googleMap) {
        final LatLng prevLocation = mMap == null ? null : mMap.getCameraPosition().target;
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (context != null && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            if (prevLocation == null) {
                mMapUtils.initMap(mMap);
            } else {
                mMapUtils.animateCameraTo(mMap, prevLocation.latitude, prevLocation.longitude);
            }
        }
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                getFollowMeState().setValue(false);
            }
        });
    }

    public void setCameraPos(final double lat, final double lon) {
        if (mMap != null) {
            mMapUtils.moveCameraTo(mMap, lat, lon);
        }
    }

    public void animateCameraToMyPos() {
        final Location myPos = mLocationUtils.getLastKnownLocation();
        if (myPos != null) {
            mMapUtils.animateCameraTo(mMap, myPos.getLatitude(), myPos.getLongitude());
        }
    }

    /**
     * Returns live data for follow me state.
     *
     * @return live data for follow me state.
     */
    public MutableLiveData<Boolean> getFollowMeState() {
        if (mIsFollowMe == null) {
            mIsFollowMe = new MutableLiveData<>();
            mIsFollowMe.setValue(false);
        }
        return mIsFollowMe;
    }

    public MutableLiveData<Integer> getMode() {
        if (mMode == null) {
            mMode = new MutableLiveData<>();
            mMode.setValue(MODE_MENU_CLOSED);
        }
        return mMode;
    }

    public void menuOpened() {
        final Integer mode = getMode().getValue();
        if (mode != null && mode == MODE_MENU_CLOSED) {
            getMode().setValue(MODE_MENU_OPENED);
        }
    }

    public void menuClosed() {
        final Integer mode = getMode().getValue();
        if (mode != null && mode == MODE_MENU_OPENED) {
            getMode().setValue(MODE_MENU_CLOSED);
        }
    }

    /**
     * Click menu event.
     */
    public void clickMenu(final View view) {
        final Integer mode = getMode().getValue();
        if (mode != null) {
            switch (mode) {
                case MODE_MENU_CLOSED:
                    getMode().setValue(MODE_MENU_OPENED);
                    break;
                case MODE_MENU_OPENED:
                    getMode().setValue(MODE_MENU_CLOSED);
                    break;
                case MODE_ALARM_RADIUS:
                    getMode().setValue(MODE_MENU_CLOSED);
                    break;
                default:
                    getMode().setValue(MODE_MENU_CLOSED);
                    break;
            }
        }
    }

    public void clickOk(final View view) {
        final Integer mode = getMode().getValue();
        if (mode != null) {
            switch (mode) {
                case MODE_ALARM_POS:
                    getMode().setValue(MODE_ALARM_RADIUS);
                    break;
                case MODE_ALARM_RADIUS:
                    // TODO: add alarm
                    mAddressesRepository.addAlarm(new Alarm()).subscribe();
                    getMode().setValue(MODE_MENU_CLOSED);
                    break;
                default:
                    break;
            }
        }
    }

    public void clickZoomIn(final View view) {
        if (mMap != null) {
            mMapUtils.zoomIn(mMap);
        }
    }

    public void clickZoomOut(final View view) {
        if (mMap != null) {
            mMapUtils.zoomOut(mMap);
        }
    }

    /**
     * Click follow me event.
     */
    public void clickFollowMe(final View view) {
        final Boolean prevState = mIsFollowMe.getValue();
        mIsFollowMe.setValue(prevState == null || !prevState);
    }

    public void clickAddAlarm() {
        getMode().setValue(MODE_ALARM_POS);
    }
}
