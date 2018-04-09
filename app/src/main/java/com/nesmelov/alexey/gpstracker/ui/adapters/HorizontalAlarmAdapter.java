package com.nesmelov.alexey.gpstracker.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nesmelov.alexey.gpstracker.R;
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
public class HorizontalAlarmAdapter extends RecyclerView.Adapter<HorizontalAlarmAdapter.AlarmViewHolder> {

    private final List<Alarm> mAlarms = new ArrayList<>();

    private Observable<Alarm> mAlarmClickedObservable;
    private List<ObservableEmitter<Alarm>> mAlarmClickedEmitters = new CopyOnWriteArrayList<>();

    /**
     * Constructs horizontal alarms adapter.
     */
    public HorizontalAlarmAdapter() {
        mAlarmClickedObservable = Observable.create(mAlarmClickedEmitters::add);
    }

    /**
     * Returns observable for clicked alarm.
     *
     * @return observable for clicked alarm.
     */
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

    /**
     * Returns alarms date.
     *
     * @return alarms date.
     */
    public List<Alarm> getData() {
        return mAlarms;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_horizontal_alarm, parent, false);
        final AlarmViewHolder viewHolder = new AlarmViewHolder(view);
        viewHolder.mCard.setOnClickListener(v -> {
            final int pos =  viewHolder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                for (final ObservableEmitter<Alarm> emitter : mAlarmClickedEmitters) {
                    if (!emitter.isDisposed()) {
                        emitter.onNext(mAlarms.get(pos));
                    }
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AlarmViewHolder holder, final int position) {
        final int holderPos = holder.getAdapterPosition();
        if (holderPos != RecyclerView.NO_POSITION) {
            final Alarm holderAlarm = mAlarms.get(holderPos);
            holder.mCard.setCardBackgroundColor(holderAlarm.color);
        }
    }

    @Override
    public int getItemCount() {
        return mAlarms.size();
    }

    /**
     * View holder class for horizontal alarms list.
     */
    static class AlarmViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.cardView) CardView mCard;

        /**
         * View holder constructor.
         *
         * @param itemView view of view holder.
         */
        AlarmViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
