package com.nesmelov.alexey.gpstracker.ui.activities;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.nesmelov.alexey.gpstracker.R;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;
import com.nesmelov.alexey.gpstracker.ui.adapters.AddressListAdapter;
import com.nesmelov.alexey.gpstracker.ui.adapters.ViewPagerAdapter;
import com.nesmelov.alexey.gpstracker.ui.fragments.AddressesFragment;
import com.nesmelov.alexey.gpstracker.ui.utils.AddressesDiffUtilsCallback;
import com.nesmelov.alexey.gpstracker.viewmodels.SearchActivityViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.tabLayout)TabLayout mTabLayout;
    @BindView(R.id.viewPager)ViewPager mViewPager;
    @BindView(R.id.geocoderList) RecyclerView mFoundAddressListView;
    @BindView(R.id.my_toolbar) Toolbar mToolbar;

    private CompositeDisposable mCompositeDisposable;
    private Observable<Place> mPlaceSelectedObservable;
    private Disposable mPlaceSelectedDisposable;

    private AddressListAdapter mRecentsAddressesListAdapter;
    private AddressListAdapter mFavouriteAddressesListAdapter;

    private SearchActivityViewModel mViewModel;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search);
        ButterKnife.bind(this);
        initActionBar();
        mViewModel = ViewModelProviders.of(this).get(SearchActivityViewModel.class);
        mViewModel.getRecentAddresses().observe(this, this::showRecentAddresses);
        mViewModel.getFavouriteAddresses().observe(this, this::showFavouriteAddresses);

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        mPlaceSelectedObservable = Observable.create((ObservableOnSubscribe<Place>) emitter -> {
            emitter.setCancellable(() -> {
                if (autocompleteFragment != null) {
                    autocompleteFragment.setOnPlaceSelectedListener(null);
                }
            });
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(final Place place) {
                    emitter.onNext(place);
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                }
            });
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        mFavouriteAddressesListAdapter = new AddressListAdapter(false);
        mRecentsAddressesListAdapter = new AddressListAdapter(true);

        setupViewPager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPlaceSelectedDisposable = mPlaceSelectedObservable.subscribe(place -> {
            final Address address = Address.fromGeoAddress(place);
            mViewModel.addAddress(address);
            completeWithAddress(address);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(mFavouriteAddressesListAdapter.getClickedAddressObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::completeWithAddress));
        mCompositeDisposable.add(mRecentsAddressesListAdapter.getClickedAddressObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::completeWithAddress));
        mCompositeDisposable.add(mRecentsAddressesListAdapter.getFavouriteChangedAddressObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mViewModel::updateAddressFavourite));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCompositeDisposable.dispose();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlaceSelectedDisposable.dispose();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    /**
     * Initiates view pager.
     */
    private void setupViewPager() {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(AddressesFragment.getInstance(mRecentsAddressesListAdapter, false), getString(R.string.recent));
        final AddressesFragment favouriteFragment = AddressesFragment.getInstance(mFavouriteAddressesListAdapter, true);
        favouriteFragment.getAddressSwipedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(address -> {
                    address.favourite = false;
                    mViewModel.updateAddressFavourite(address);
                    Snackbar.make(mViewPager, R.string.removed_favourite, Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.cancel), view -> {
                                address.favourite = true;
                                mViewModel.addAddress(address);
                            }).show();
                });
        adapter.addFragment(favouriteFragment, getString(R.string.favourite));

        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Edit text view focus changed event.
     *
     * @param focus <tt>true</tt> if edit text is focused.
     */
    private void onFocusChanged(final boolean focus) {
        mTabLayout.setVisibility(focus ? View.GONE : View.VISIBLE);
        mViewPager.setVisibility(focus ? View.GONE : View.VISIBLE);
        mFoundAddressListView.setVisibility(focus ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows recent addresses.
     *
     * @param addresses addresses to show.
     */
    private void showRecentAddresses(final List<Address> addresses) {
        showAddressesByDiff(addresses, mRecentsAddressesListAdapter);
    }

    /**
     * Shows favourite addresses.
     *
     * @param addresses addresses to show.
     */
    private void showFavouriteAddresses(final List<Address> addresses) {
        showAddressesByDiff(addresses, mFavouriteAddressesListAdapter);
    }

    /**
     * Completes search by the certain address.
     *
     * @param address address to complete with.
     */
    private void completeWithAddress(final Address address) {
        final Intent resultIntent = new Intent();
        resultIntent.putExtra("lat", address.lat);
        resultIntent.putExtra("lon", address.lon);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Refreshes adapter by new addresses.
     *
     * @param addresses new addresses to update adapter.
     * @param adapter adapter to refresh.
     */
    private void showAddressesByDiff(final List<Address> addresses, final AddressListAdapter adapter) {
        final AddressesDiffUtilsCallback diffUtilCallback = new AddressesDiffUtilsCallback(
                adapter.getData(), addresses);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilCallback);
        adapter.setData(addresses);
        diffResult.dispatchUpdatesTo(adapter);
    }

    /**
     * Initiates action bar.
     */
    private void initActionBar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
