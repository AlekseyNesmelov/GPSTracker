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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.nesmelov.alexey.gpstracker.R;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;
import com.nesmelov.alexey.gpstracker.ui.adapters.AddressListAdapter;
import com.nesmelov.alexey.gpstracker.ui.adapters.ViewPagerAdapter;
import com.nesmelov.alexey.gpstracker.ui.fragments.AddressesFragment;
import com.nesmelov.alexey.gpstracker.ui.utils.AddressesDiffUtilsCallback;
import com.nesmelov.alexey.gpstracker.viewmodels.SearchActivityViewModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SearchActivity extends AppCompatActivity {

    private static final int SEARCH_TIMEOUT = 500;

    @BindView(R.id.searchTextView)EditText mSearchView;
    @BindView(R.id.tabLayout)TabLayout mTabLayout;
    @BindView(R.id.viewPager)ViewPager mViewPager;
    @BindView(R.id.geocoderList) RecyclerView mFoundAddressListView;

    private CompositeDisposable mCompositeDisposable;
    private Observable<Boolean> mSearchFocusObservable;
    private Flowable<String> mSearchTextChangedObservable;

    private TextWatcher mTextWatcher;

    private AddressListAdapter mFoundAddressListAdapter;
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
        mViewModel.getFoundAddresses().observe(this, this::showFoundAddresses);

        mFavouriteAddressesListAdapter = new AddressListAdapter(false);
        mRecentsAddressesListAdapter = new AddressListAdapter(true);
        mFoundAddressListAdapter = new AddressListAdapter(true);

        mFoundAddressListView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        mFoundAddressListView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mFoundAddressListView.setAdapter(mFoundAddressListAdapter);

        mSearchFocusObservable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            emitter.setCancellable(() -> mSearchView.setOnFocusChangeListener(null));
            mSearchView.setOnFocusChangeListener((v, hasFocus) -> emitter.onNext(hasFocus));
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        mSearchTextChangedObservable = Flowable.create((FlowableOnSubscribe<String>) emitter -> {
            emitter.setCancellable(() -> {
                if (mTextWatcher != null) {
                    mSearchView.removeTextChangedListener(mTextWatcher);
                    mTextWatcher = null;
                }
            });
            mTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    emitter.onNext(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };
            mSearchView.addTextChangedListener(mTextWatcher);
        }, BackpressureStrategy.MISSING).debounce(SEARCH_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        setupViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(mSearchFocusObservable.subscribe(this::onFocusChanged));
        mCompositeDisposable.add(mSearchTextChangedObservable.subscribe(mViewModel::requestPlace));
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
        mCompositeDisposable.add(mFoundAddressListAdapter.getClickedAddressObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(address -> {
                    mViewModel.addAddress(address);
                    completeWithAddress(address);
                }));
        mCompositeDisposable.add(mFoundAddressListAdapter.getFavouriteChangedAddressObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mViewModel::addAddress));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCompositeDisposable.dispose();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.hasFocus()) {
            mSearchView.clearFocus();
            mSearchView.setText("");
        } else {
            finish();
        }
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
     * Shows found addresses.
     *
     * @param addresses addresses to show.
     */
    private void showFoundAddresses(final List<Address> addresses) {
        showAddressesByDiff(addresses, mFoundAddressListAdapter);
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.search);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
