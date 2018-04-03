package com.nesmelov.alexey.gpstracker.ui.fragments;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nesmelov.alexey.gpstracker.R;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;
import com.nesmelov.alexey.gpstracker.ui.adapters.AddressListAdapter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;

/**
 * Fragment that shows addresses list.
 */
public class AddressesFragment extends Fragment {

    @BindView(R.id.emptyListLabel) TextView mEmptyListLabel;
    @BindView(R.id.addressesList) RecyclerView mAddressesList;

    private AddressListAdapter mAddressListAdapter;
    private boolean mAllowSwipe;

    private Observable<Address> mAddressSwipedObservable;
    private List<ObservableEmitter<Address>> mAddressSwipedEmitters = new CopyOnWriteArrayList<>();

    /**
     * Returns an instance of the addresses fragment.
     *
     * @param addressListAdapter list adapter to use.
     * @param allowSwipe <tt>true</tt> if item swipe is allowed.
     * @return an instance of the addresses fragment.
     */
    public static AddressesFragment getInstance(final AddressListAdapter addressListAdapter,
                                                final boolean allowSwipe) {
        final AddressesFragment fragment = new AddressesFragment();
        fragment.mAddressListAdapter = addressListAdapter;
        fragment.mAllowSwipe = allowSwipe;
        fragment.mAddressSwipedObservable = Observable.create(fragment.mAddressSwipedEmitters::add);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_addresses, container, false);
        ButterKnife.bind(this, view);

        if (getActivity() != null) {
            mAddressesList.setLayoutManager(new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false));
        }
        mAddressesList.setAdapter(mAddressListAdapter);

        if (mAllowSwipe) {
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
                    return  makeMovementFlags(0, LEFT | RIGHT);
                }

                @Override
                public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder,
                                      final RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
                    final int pos = viewHolder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        for (final ObservableEmitter<Address> emitter : mAddressSwipedEmitters) {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(mAddressListAdapter.getData().get(pos));
                            }
                        }
                    }
                }

                @Override
                public void onChildDraw(final Canvas c, final RecyclerView recyclerView,
                                        final RecyclerView.ViewHolder viewHolder, final float dX, final float dY,
                                        final int actionState, final boolean isCurrentlyActive) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        final float alpha = 1 - Math.abs(dX) / viewHolder.itemView.getWidth();
                        viewHolder.itemView.setAlpha(alpha);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            });
            itemTouchHelper.attachToRecyclerView(mAddressesList);
        }
        return view;
    }

    /**
     * Gets observable for swiped item.
     *
     * @return observable for swiped item.
     */
    public Observable<Address> getAddressSwipedObservable() {
        return mAddressSwipedObservable;
    }
}
