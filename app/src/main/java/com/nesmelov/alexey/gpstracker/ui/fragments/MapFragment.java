package com.nesmelov.alexey.gpstracker.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
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
import com.nesmelov.alexey.gpstracker.R;
import com.nesmelov.alexey.gpstracker.application.utils.Position;
import com.nesmelov.alexey.gpstracker.databinding.LayoutMapBinding;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;
import com.nesmelov.alexey.gpstracker.ui.activities.SearchActivity;
import com.nesmelov.alexey.gpstracker.ui.adapters.HorizontalAlarmAdapter;
import com.nesmelov.alexey.gpstracker.ui.utils.AlarmDiffUtilsCallback;
import com.nesmelov.alexey.gpstracker.viewmodels.MapFragmentViewModel;

import java.util.List;

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

    private MapFragmentViewModel mViewModel;

    private CompositeDisposable mCompositeDisposable;
    private Observable<View> mSearchCardClickObservable;
    private Observable<View> mAddAlarmBtnClickObservable;
    private Observable<Double> mRadiusChangedObservable;
    private Observable<Integer> mBottomSheetStateChangedObservable;

    private HorizontalAlarmAdapter mHorizontalAlarmAdapter;

    /**
     * Returns an instance of the map fragment.
     *
     * @return an instance of the map fragment.
     */
    public static MapFragment getInstance() {
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container, final @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(MapFragmentViewModel.class);
        mViewModel.getFollowMeState().observe(this, this::updateFollowMeState);
        mViewModel.getAlarms().observe(this, this::showAlarms);
        mViewModel.getMode().observe(this, this::modeChanged);

        final LayoutMapBinding binding = DataBindingUtil.inflate(inflater, R.layout.layout_map, container, false);
        binding.setLifecycleOwner(this);
        binding.setViewModel(mViewModel);
        final View view = binding.getRoot();
        ButterKnife.bind(this, view);
        mMapView.onCreate(savedInstanceState);
        return view;
    }

    @Override
    public void onDestroyView() {
        mViewModel = null;
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        bringViewsToFront();
        initHorizontalAlarmAdapter();
        initClickObservables();

        mBottomSheetStateChangedObservable = Observable.create(e -> {
            final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
            e.setCancellable(() -> bottomSheetBehavior.setBottomSheetCallback(null));
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
                    e.onNext(newState);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        });

        mRadiusChangedObservable = Observable.create(e -> {
            e.setCancellable(() -> mRadiusSeekBar.setOnSeekBarChangeListener(null));
            mRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                    e.onNext((double)(progress + 10));
                }

                @Override
                public void onStartTrackingTouch(final SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(final SeekBar seekBar) {
                }
            });
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
        mViewModel.initMap(getActivity(), googleMap);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_SEARCH_PLACE
                && resultCode == Activity.RESULT_OK && data != null) {
            final double lat = data.getDoubleExtra(Position.LAT, Position.BAD_POS);
            final double lon = data.getDoubleExtra(Position.LON, Position.BAD_POS);
            if (lat > Position.BAD_POS && lon > Position.BAD_POS) {
                mViewModel.setCameraPos(lat, lon);
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
            mViewModel.animateCameraToMyPos();
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
        /*if (mAlarmRadius != null) {
            mAlarmRadius.setVisible(false);
        }*/
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
        /*if (mAlarmRadius != null) {
            mAlarmRadius.setVisible(false);
        }*/
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
        /*if (mAlarmRadius != null) {
            mAlarmRadius.setVisible(false);
        }*/
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
        /*if (mMap != null) {
            mAlarmRadius = mMap.addCircle(new CircleOptions()
                    .center(mMap.getCameraPosition().target)
                    .visible(true)
                    .fillColor(Color.argb(128, 255, 152, 0))
                    .strokeColor(Color.argb(128, 230, 81, 0))
                    .strokeWidth(7)
                    .radius(1000)
            );
            mMapUtils.animateCameraTo(mMap, mAlarmRadius);
        }*/

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

    private void radiusChanged(final double radius) {
        /*if (mAlarmRadius != null) {
            mAlarmRadius.setRadius(radius);
            mMapUtils.animateCameraTo(mMap, mAlarmRadius);
        }*/
    }

    private void bottomSheetStateChangred(final int newState) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            mViewModel.menuClosed();
        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            mViewModel.menuOpened();
        }
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
     * Initiates click observables.
     */
    private void initClickObservables() {
        mSearchCardClickObservable = createClickObservable(mSearchCard);
        mAddAlarmBtnClickObservable = createClickObservable(mAddAlarmBtn);
    }

    /**
     * Subscribes to click observables.
     */
    private void subscribeClickObservables() {
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(mAddAlarmBtnClickObservable.subscribe(v -> mViewModel.clickAddAlarm()));
        mCompositeDisposable.add(mSearchCardClickObservable.subscribe(v -> startActivityForResult(
                new Intent(getContext(), SearchActivity.class), REQUEST_CODE_SEARCH_PLACE)));
        mCompositeDisposable.add(mBottomSheetStateChangedObservable.subscribe(this::bottomSheetStateChangred));
        mCompositeDisposable.add(mRadiusChangedObservable.subscribe(this::radiusChanged));
    }

    /**
     * Unsubscribes from click observables.
     */
    private void unsubscribeClickObservables() {
        mCompositeDisposable.dispose();
    }
}
