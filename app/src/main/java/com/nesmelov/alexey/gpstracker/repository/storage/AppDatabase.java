package com.nesmelov.alexey.gpstracker.repository.storage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.nesmelov.alexey.gpstracker.repository.storage.converters.Converters;
import com.nesmelov.alexey.gpstracker.repository.storage.dao.AddressDao;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;

@Database(entities = {Address.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract AddressDao addressDao();
}
