package com.nesmelov.alexey.gpstracker.ui.utils;

import android.support.v7.util.DiffUtil;

import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;

import java.util.List;

/**
 * Callback to merge address lists and provide changes to an adapter.
 */
public class AddressesDiffUtilsCallback extends DiffUtil.Callback {

    private final List<Address> mOldList;
    private final List<Address> mNewList;

    /**
     * Constructs the callback.
     *
     * @param oldList old addresses list.
     * @param newList new addresses list.
     */
    public AddressesDiffUtilsCallback(final List<Address> oldList, final List<Address> newList) {
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
        final Address oldAddress = mOldList.get(oldItemPosition);
        final Address newAddress = mNewList.get(newItemPosition);
        return oldAddress.name.equals(newAddress.name);
    }

    @Override
    public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
        final Address oldAddress = mOldList.get(oldItemPosition);
        final Address newAddress = mNewList.get(newItemPosition);
        return oldAddress.name.equals(newAddress.name)
                && oldAddress.date.equals(newAddress.date)
                && oldAddress.favourite == newAddress.favourite
                && oldAddress.lat == newAddress.lat
                && oldAddress.lon == newAddress.lon;
    }
}