package com.nesmelov.alexey.gpstracker.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.nesmelov.alexey.gpstracker.R;
import com.nesmelov.alexey.gpstracker.application.TrackerApplication;
import com.nesmelov.alexey.gpstracker.application.utils.LocationUtils;
import com.nesmelov.alexey.gpstracker.application.utils.MapUtils;
import com.nesmelov.alexey.gpstracker.application.utils.Position;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;
import com.nesmelov.alexey.gpstracker.ui.activities.SearchActivity;
import com.nesmelov.alexey.gpstracker.ui.adapters.HorizontalAlarmAdapter;
import com.nesmelov.alexey.gpstracker.ui.utils.AlarmDiffUtilsCallback;
import com.nesmelov.alexey.gpstracker.viewmodels.MapFragmentViewModel;

import java.util.List;

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
    @BindView(R.id.zoomInBtn) FloatingActionButton mZoomInBtn;
    @BindView(R.id.zoomOutBtn) FloatingActionButton mZoomOutBtn;
    @BindView(R.id.bottom_sheet) ConstraintLayout mBottomSheet;
    @BindView(R.id.searchCard) CardView mSearchCard;
    @BindView(R.id.alarmsHorizontalView) RecyclerView mAlarmsView;
    @BindView(R.id.add_alarm_btn) FloatingActionButton mAddAlarmBtn;
    @BindView(R.id.okBtn) FloatingActionButton mOkBtn;
    @BindView(R.id.alarm_target) ImageView mAlarmTarget;
    @BindView(R.id.radiusSeekBar) SeekBar mRadiusSeekBar;

    private GoogleMap mMap;
    private Circle mAlarmRadius;

    private MapFragmentViewModel mViewModel;

    private CompositeDisposable mCompositeDisposable;
    private Observable<View> mFollowMeClickObservable;
    private Observable<View> mMenuClickObservable;
    private Observable<View> mZoomInClickObservable;
    private Observable<View> mZoomOutClickObservable;
    private Observable<View> mSearchCardClickObservable;
    private Observable<View> mAddAlarmBtnClickObservable;
    private Observable<View> mOkBtnClickObservable;

    private HorizontalAlarmAdapter mHorizontalAlarmAdapter;

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
        bringViewsToFront();
        initHorizontalAlarmAdapter();
        initViewModel();
        initClickObservables();

        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mViewModel.menuClosed();
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    mViewModel.menuOpened();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        mRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                final float radius = progress + 10;
                if (mAlarmRadius != null) {
                    mAlarmRadius.setRadius(radius);
                }
                if (mAlarmRadius != null) {
                    mMapUtils.animateCameraTo(mMap, mAlarmRadius);
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mMapView.getMapAsync(this);

        subscribeClickObservables();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            mViewModel.getCameraPos().setValue(mMap.getCameraPosition().target);
        }
        mMapView.onPause();
        unsubscribeClickObservables();
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
     * Handle mode changed event. The modes are following:
     * 0 - menu is closed,
     * 1 - menu is opened,
     * 2 - alarm position selecting,
     * 3 - alarm radius selecting.
     *
     * @param mode mode to select.
     */
    private void modeChanged(final int mode) {
        switch (mode) {
            case MapFragmentViewModel.MODE_MENU_CLOSED:
                setMenuClosedMode();
                break;
            case MapFragmentViewModel.MODE_MENU_OPENED:
                setMenuOpenedMode();
                break;
            case MapFragmentViewModel.MODE_ALARM_POS:
                setAlarmPosMode();
                break;
            case MapFragmentViewModel.MODE_ALARM_RADIUS:
                setAlarmRadiusMode();
                break;
            default:
                break;
        }
    }

    /**
     * Sets menu closed mode.
     */
    private void setMenuClosedMode() {
        if (mAlarmRadius != null) {
            mAlarmRadius.setVisible(false);
        }
        mAlarmTarget.setVisibility(View.GONE);
        mRadiusSeekBar.setVisibility(View.GONE);
        if (mOkBtn.getVisibility() == View.VISIBLE) {
            mOkBtn.hide();
        }
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (getActivity() != null) {
            mMenuFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_reorder_black_24dp));
        }
    }

    /**
     * Sets menu opened mode.
     */
    private void setMenuOpenedMode() {
        if (mAlarmRadius != null) {
            mAlarmRadius.setVisible(false);
        }
        mAlarmTarget.setVisibility(View.GONE);
        mRadiusSeekBar.setVisibility(View.GONE);
        if (mOkBtn.getVisibility() == View.VISIBLE) {
            mOkBtn.hide();
        }
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        if (getActivity() != null) {
            mMenuFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_close_black_24dp));
        }
    }

    /**
     * Sets alarm position mode.
     */
    private void setAlarmPosMode() {
        if (mAlarmRadius != null) {
            mAlarmRadius.setVisible(false);
        }
        mRadiusSeekBar.setVisibility(View.GONE);
        mAlarmTarget.setVisibility(View.VISIBLE);
        if (mOkBtn.getVisibility() != View.VISIBLE) {
            mOkBtn.show();
        }
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (getActivity() != null) {
            mMenuFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_close_black_24dp));
        }
    }

    /**
     * Sets alarm radius mode.
     */
    private void setAlarmRadiusMode() {
        if (mMap != null) {
            mAlarmRadius = mMap.addCircle(new CircleOptions()
                    .center(mMap.getCameraPosition().target)
                    .visible(true)
                    .fillColor(Color.argb(128, 255, 152, 0))
                    .strokeColor(Color.argb(128, 230, 81, 0))
                    .strokeWidth(7)
                    .radius(1000)
            );
            mMapUtils.animateCameraTo(mMap, mAlarmRadius);
        }

        mRadiusSeekBar.setVisibility(View.VISIBLE);
        mAlarmTarget.setVisibility(View.GONE);
        if (mOkBtn.getVisibility() != View.VISIBLE) {
            mOkBtn.show();
        }
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (getActivity() != null) {
            mMenuFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_close_black_24dp));
        }
    }

    /**
     * Show list of alarms.
     *
     * @param alarms alarms to show.
     */
    private void showAlarms(final List<Alarm> alarms) {
        showAlarmsByDiff(alarms, mHorizontalAlarmAdapter);
    }

    private void showAlarmsByDiff(final List<Alarm> alarms, final HorizontalAlarmAdapter adapter) {
        final AlarmDiffUtilsCallback diffUtilCallback = new AlarmDiffUtilsCallback(
                adapter.getData(), alarms);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilCallback);
        adapter.setData(alarms);
        diffResult.dispatchUpdatesTo(adapter);
    }

    /**
     * Brings views to the front.
     */
    private void bringViewsToFront() {
        mAlarmTarget.bringToFront();
        mRadiusSeekBar.bringToFront();
        mOkBtn.bringToFront();
        mBottomSheet.bringToFront();
    }

    /**
     * Initiates horizontal alarms adapter.
     */
    private void initHorizontalAlarmAdapter() {
        mHorizontalAlarmAdapter = new HorizontalAlarmAdapter();
        mAlarmsView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        mAlarmsView.setAdapter(mHorizontalAlarmAdapter);
    }

    /**
     * Initiates view model.
     */
    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(MapFragmentViewModel.class);
        mViewModel.getFollowMeState().observe(this, this::updateFollowMeState);
        mViewModel.getAlarms().observe(this, this::showAlarms);
        mViewModel.getMode().observe(this, this::modeChanged);
    }

    /**
     * Initiates click observables.
     */
    private void initClickObservables() {
        mFollowMeClickObservable = createClickObservable(mFollowMeBtn);
        mMenuClickObservable = createClickObservable(mMenuFab);
        mZoomInClickObservable = createClickObservable(mZoomInBtn);
        mZoomOutClickObservable = createClickObservable(mZoomOutBtn);
        mSearchCardClickObservable = createClickObservable(mSearchCard);
        mAddAlarmBtnClickObservable = createClickObservable(mAddAlarmBtn);
        mOkBtnClickObservable = createClickObservable(mOkBtn);
    }

    /**
     * Subscribes to click observables.
     */
    private void subscribeClickObservables() {
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(mOkBtnClickObservable.subscribe(v -> mViewModel.clickOk()));
        mCompositeDisposable.add(mMenuClickObservable.subscribe(v-> mViewModel.clickMenu()));
        mCompositeDisposable.add(mFollowMeClickObservable.subscribe(v -> mViewModel.clickFollowMe()));
        mCompositeDisposable.add(mAddAlarmBtnClickObservable.subscribe(v -> mViewModel.clickAddAlarm()));
        mCompositeDisposable.add(mZoomInClickObservable.subscribe(v -> mMapUtils.zoomIn(mMap)));
        mCompositeDisposable.add(mZoomOutClickObservable.subscribe(v -> mMapUtils.zoomOut(mMap)));
        mCompositeDisposable.add(mSearchCardClickObservable.subscribe(v -> startActivityForResult(
                new Intent(getContext(), SearchActivity.class), REQUEST_CODE_SEARCH_PLACE)));
    }

    /**
     * Unsubscribes from click observables.
     */
    private void unsubscribeClickObservables() {
        mCompositeDisposable.dispose();
    }
}
