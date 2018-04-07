package com.nesmelov.alexey.gpstracker.repository.storage.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface AlarmDao {

    @Query("SELECT * FROM Alarm ORDER BY date DESC")
    Flowable<List<Alarm>> getAlarms();

    @Query("SELECT * FROM Alarm WHERE turnedOn = 1 ORDER BY date DESC")
    Flowable<List<Alarm>> getTurnedOnAlarms();

    @Query("SELECT * FROM Alarm WHERE id = :id")
    Single<Alarm> getById(final int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final Alarm alarm);

    @Delete
    void delete(final Alarm alarm);
}
