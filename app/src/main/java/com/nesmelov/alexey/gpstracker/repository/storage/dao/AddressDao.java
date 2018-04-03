package com.nesmelov.alexey.gpstracker.repository.storage.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface AddressDao {

    @Query("SELECT * FROM Address ORDER BY date DESC")
    Flowable<List<Address>> getRecents();

    @Query("SELECT * FROM Address WHERE favourite = 1 ORDER BY date DESC")
    Flowable<List<Address>> getFavourites();

    @Query("SELECT * FROM Address WHERE name = :name")
    Single<Address> getByName(final String name);

    @Query("UPDATE Address SET favourite = :favourite  WHERE name = :name")
    void updateAddressFavourite(final String name, final boolean favourite);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final Address address);

    @Delete
    void delete(final Address address);
}
