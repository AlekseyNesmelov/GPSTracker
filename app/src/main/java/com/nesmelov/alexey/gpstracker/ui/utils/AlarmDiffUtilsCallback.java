package com.nesmelov.alexey.gpstracker.ui.utils;

import android.support.v7.util.DiffUtil;

import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;

import java.util.List;

/**
 * Callback to merge address lists and provide changes to an adapter.
 */
public class AlarmDiffUtilsCallback extends DiffUtil.Callback {

    private final List<Alarm> mOldList;
    private final List<Alarm> mNewList;

    /**
     * Constructs the callback.
     *
     * @param oldList old addresses list.
     * @param newList new addresses list.
     */
    public AlarmDiffUtilsCallback(final List<Alarm> oldList, final List<Alarm> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
        final Alarm oldAddress = mOldList.get(oldItemPosition);
        final Alarm newAddress = mNewList.get(newItemPosition);
        return oldAddress.id == newAddress.id;
    }

    @Override
    public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
        final Alarm oldAddress = mOldList.get(oldItemPosition);
        final Alarm newAddress = mNewList.get(newItemPosition);
        return oldAddress.name.equals(newAddress.name)
                && oldAddress.date.equals(newAddress.date)
                && oldAddress.id == newAddress.id
                && oldAddress.lat == newAddress.lat
                && oldAddress.lon == newAddress.lon;
    }
}