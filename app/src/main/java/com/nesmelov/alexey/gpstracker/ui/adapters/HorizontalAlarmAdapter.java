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
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Horizontal list of alarms adapter.
 */
public class HorizontalAlarmAdapter extends RecyclerView.Adapter<HorizontalAlarmAdapter.AddressViewHolder> {

    private final List<Alarm> mAlarms = new ArrayList<>();

    private Observable<Alarm> mAlarmClickedObservable;
    private List<ObservableEmitter<Alarm>> mAlarmClickedEmitters = new CopyOnWriteArrayList<>();

    /*private Observable<Address> mAddressFavouriteChangedObservable;
    private List<ObservableEmitter<Address>> mAddressFavouriteChangedEmitters = new CopyOnWriteArrayList<>();*/

    public HorizontalAlarmAdapter() {
        mAlarmClickedObservable = Observable.create(mAlarmClickedEmitters::add);
    }

    public Observable<Alarm> getClickedAlarmObservable() {
        return mAlarmClickedObservable;
    }

    /**
     * Sets data to the adapter.
     *
     * @param alarms data to set.
     */
    public void setData(final List<Alarm> alarms) {
        mAlarms.clear();
        mAlarms.addAll(alarms);
    }

    public List<Alarm> getData() {
        return mAlarms;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_horizontal_alarm, parent, false);
        final AddressViewHolder viewHolder = new AddressViewHolder(view);
        /*viewHolder.mFavouriteBtn.setVisibility(mShowFavouriteIcon ? View.VISIBLE : View.GONE);
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
        });*/
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AddressViewHolder holder, final int position) {
        final int holderPos = holder.getAdapterPosition();
        if (holderPos != RecyclerView.NO_POSITION) {
            /*final Address holderAddress = mAddresses.get(holderPos);
            holder.mNameView.setText(holderAddress.name);
            holder.mFavouriteBtn.setChecked(holderAddress.favourite);*/
        }
    }

    @Override
    public int getItemCount() {
        return mAlarms.size();
    }

    /**
     * View holder class for addresses list.
     */
    static class AddressViewHolder extends RecyclerView.ViewHolder{
        /*@BindView(R.id.name) TextView mNameView;
        @BindView(R.id.favourite) ToggleButton mFavouriteBtn;
        @BindView(R.id.main) ConstraintLayout mLayout;*/

        /**
         * View holder constructor.
         *
         * @param itemView view of view holder.
         */
        AddressViewHolder(final View itemView) {
            super(itemView);
            // ButterKnife.bind(this, itemView);
        }
    }
}
