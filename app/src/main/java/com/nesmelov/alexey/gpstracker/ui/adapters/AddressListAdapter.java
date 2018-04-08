package com.nesmelov.alexey.gpstracker.ui.adapters;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nesmelov.alexey.gpstracker.R;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Addresses list adapter.
 */
public class AddressListAdapter extends RecyclerView.Adapter<AddressListAdapter.AddressViewHolder> {

    private final boolean mShowFavouriteIcon;
    private final List<Address> mAddresses = new ArrayList<>();

    private Observable<Address> mAddressClickedObservable;
    private List<ObservableEmitter<Address>> mAddressClickedEmitters = new CopyOnWriteArrayList<>();

    private Observable<Address> mAddressFavouriteChangedObservable;
    private List<ObservableEmitter<Address>> mAddressFavouriteChangedEmitters = new CopyOnWriteArrayList<>();

    /**
     * Constructs addresses list adapter.
     *
     * @param showFavouriteIcon <tt>true</tt> if it needs to show favourite icon.
     */
    public AddressListAdapter(final boolean showFavouriteIcon) {
        mShowFavouriteIcon = showFavouriteIcon;
        mAddressClickedObservable = Observable.create(mAddressClickedEmitters::add);
        mAddressFavouriteChangedObservable = Observable.create(mAddressFavouriteChangedEmitters::add);
    }

    /**
     * Returns observable for clicked addresses.
     *
     * @return observable for clicked addresses.
     */
    public Observable<Address> getClickedAddressObservable() {
        return mAddressClickedObservable;
    }

    /**
     * Returns observable for favourite changed addresses.
     *
     * @return observable for favourite changed addresses.
     */
    public Observable<Address> getFavouriteChangedAddressObservable() {
        return mAddressFavouriteChangedObservable;
    }

    /**
     * Gets adapter's data.
     *
     * @return adapter's data.
     */
    public List<Address> getData() {
        return mAddresses;
    }

    /**
     * Sets data to the adapter.
     *
     * @param addresses data to set.
     */
    public void setData(final List<Address> addresses) {
        mAddresses.clear();
        mAddresses.addAll(addresses);
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_address, parent, false);
        final AddressViewHolder viewHolder = new AddressViewHolder(view);
        viewHolder.mFavouriteBtn.setVisibility(mShowFavouriteIcon ? View.VISIBLE : View.GONE);
        viewHolder.mLayout.setOnClickListener(v -> {
            final int pos =  viewHolder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                for (final ObservableEmitter<Address> emitter : mAddressClickedEmitters) {
                    if (!emitter.isDisposed()) {
                        emitter.onNext(mAddresses.get(pos));
                    }
                }
            }
        });
        viewHolder.mFavouriteBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final int pos = viewHolder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                for (final ObservableEmitter<Address> emitter : mAddressFavouriteChangedEmitters) {
                    if (!emitter.isDisposed()){
                        mAddresses.get(pos).favourite = isChecked;
                        emitter.onNext(mAddresses.get(pos));
                    }
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AddressViewHolder holder, final int position) {
        final int holderPos = holder.getAdapterPosition();
        if (holderPos != RecyclerView.NO_POSITION) {
            final Address holderAddress = mAddresses.get(holderPos);
            holder.mNameView.setText(holderAddress.name);
            holder.mDetailsView.setText(holderAddress.details);
            holder.mFavouriteBtn.setChecked(holderAddress.favourite);
        }
    }

    @Override
    public int getItemCount() {
        return mAddresses.size();
    }

    /**
     * View holder class for addresses list.
     */
    static class AddressViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.name) TextView mNameView;
        @BindView(R.id.details) TextView mDetailsView;
        @BindView(R.id.favourite) ToggleButton mFavouriteBtn;
        @BindView(R.id.main) ConstraintLayout mLayout;

        /**
         * View holder constructor.
         *
         * @param itemView view of view holder.
         */
        AddressViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
