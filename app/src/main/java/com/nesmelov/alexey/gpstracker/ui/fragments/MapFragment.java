package com.nesmelov.alexey.gpstracker.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.nesmelov.alexey.gpstracker.R;
import com.nesmelov.alexey.gpstracker.application.TrackerApplication;
import com.nesmelov.alexey.gpstracker.application.utils.LocationUtils;
import com.nesmelov.alexey.gpstracker.application.utils.MapUtils;
import com.nesmelov.alexey.gpstracker.application.utils.Position;
import com.nesmelov.alexey.gpstracker.ui.activities.SearchActivity;
import com.nesmelov.alexey.gpstracker.viewmodels.MapFragmentViewModel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment that contains map.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int REQUEST_CODE_SEARCH_PLACE = 0;

    @Inject MapUtils mMapUtils;
    @Inject LocationUtils mLocationUtils;

    @BindView(R.id.mapView) MapView mMapView;
    @BindView(R.id.menuBtn) FloatingActionButton mMenuFab;
    @BindView(R.id.followMeBtn) FloatingActionButton mFollowMeBtn;
    @BindView(R.id.searchPlaceBtn) FloatingActionButton mSearchPlaceBtn;
    @BindView(R.id.zoomInBtn) FloatingActionButton mZoomInBtn;
    @BindView(R.id.zoomOutBtn) FloatingActionButton mZoomOutBtn;

    private GoogleMap mMap;

    private MapFragmentViewModel mViewModel;

    private CompositeDisposable mCompositeDisposable;
    private Observable<View> mSearchBtnClickObservable;
    private Observable<View> mFollowMeClickObservable;
    private Observable<View> mMenuClickObservable;
    private Observable<View> mZoomInClickObservable;
    private Observable<View> mZoomOutClickObservable;

    private Animation mShowSearchAnim;
    private Animation mHideSearchAnim;

    /**
     * Returns an instance of the map fragment.
     *
     * @return an instance of the map fragment.
     */
    public static MapFragment getInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TrackerApplication.getMapUtilsComp().inject(this);
        TrackerApplication.getLocationUtilsComp().inject(this);
        mShowSearchAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_search_show);
        mHideSearchAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_search_hide);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container, final @Nullable Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.layout_map, container, false);
        ButterKnife.bind(this, view);
        mMapView.onCreate(savedInstanceState);
        return view;
    }

    @Override
    public void onDestroyView() {
        mViewModel = null;
        mMap = null;
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(MapFragmentViewModel.class);
        mViewModel.getFollowMeState().observe(this, this::updateFollowMeState);
        mViewModel.getMenuOpenedState().observe(this, this::updateMenuOpenedState);

        mSearchBtnClickObservable = createClickObservable(mSearchPlaceBtn);
        mFollowMeClickObservable = createClickObservable(mFollowMeBtn);
        mMenuClickObservable = createClickObservable(mMenuFab);
        mZoomInClickObservable = createClickObservable(mZoomInBtn);
        mZoomOutClickObservable = createClickObservable(mZoomOutBtn);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mMapView.getMapAsync(this);

        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(mMenuClickObservable.subscribe(v-> mViewModel.clickMenu()));
        mCompositeDisposable.add(mFollowMeClickObservable.subscribe(v -> mViewModel.clickFollowMe()));
        mCompositeDisposable.add(mZoomInClickObservable.subscribe(v -> mMapUtils.zoomIn(mMap)));
        mCompositeDisposable.add(mZoomOutClickObservable.subscribe(v -> mMapUtils.zoomOut(mMap)));
        mCompositeDisposable.add(mSearchBtnClickObservable.subscribe(v -> startActivityForResult(
                new Intent(getContext(), SearchActivity.class), REQUEST_CODE_SEARCH_PLACE)));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            mViewModel.getCameraPos().setValue(mMap.getCameraPosition().target);
        }

        mMapView.onPause();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (getActivity() != null && ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            final LatLng location = mViewModel.getCameraPos().getValue();
            if (location == null) {
                mMapUtils.initMap(mMap);
            } else {
                mMapUtils.animateCameraTo(mMap, location.latitude, location.longitude);
            }
        }
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                mViewModel.getFollowMeState().setValue(false);
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_SEARCH_PLACE
                && resultCode == Activity.RESULT_OK && data != null) {
            final double lat = data.getDoubleExtra(Position.LAT, Position.BAD_POS);
            final double lon = data.getDoubleExtra(Position.LON, Position.BAD_POS);
            if (lat > Position.BAD_POS && lon > Position.BAD_POS) {
                mViewModel.getCameraPos().setValue(new LatLng(lat, lon));
            }
        }
    }

    /**
     * Creates view clicked observable.
     *
     * @param view view to create observable.
     * @return view clicked observable.
     */
    private Observable<View> createClickObservable(final View view) {
        return Observable.create((ObservableOnSubscribe<View>) emitter -> {
            emitter.setCancellable(() -> view.setOnClickListener(null));
            view.setOnClickListener(emitter::onNext);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Updates ui by follow me state.
     *
     * @param followMe <tt>true</tt> if follow me state is turned on.
     */
    private void updateFollowMeState(final boolean followMe) {
        if (followMe) {
            final Location myPos = mLocationUtils.getLastKnownLocation();
            if (myPos != null) {
                mMapUtils.animateCameraTo(mMap, myPos.getLatitude(), myPos.getLongitude());
            }
        }
        if (getActivity() != null) {
            mFollowMeBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                    followMe ? R.drawable.ic_near_me_black_24dp : R.drawable.ic_my_location_off_black_48dp));
        }
    }

    /**
     * Updates ui by menu opened state.
     *
     * @param isMenuOpened <tt>true</tt> if menu is opened.
     */
    private void updateMenuOpenedState(final boolean isMenuOpened) {
        if (getActivity() != null) {
            mMenuFab.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                    isMenuOpened ? R.drawable.ic_close_black_24dp : R.drawable.ic_reorder_black_24dp));
        }
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSearchPlaceBtn.getLayoutParams();
        int param = isMenuOpened ? 1 : -1;
        layoutParams.rightMargin += (int) (param * mSearchPlaceBtn.getWidth() * 1.7);
        layoutParams.bottomMargin += (int) (param * mSearchPlaceBtn.getHeight() * 0.25);
        mSearchPlaceBtn.setLayoutParams(layoutParams);
        mSearchPlaceBtn.startAnimation(isMenuOpened ? mShowSearchAnim : mHideSearchAnim);
    }
}
