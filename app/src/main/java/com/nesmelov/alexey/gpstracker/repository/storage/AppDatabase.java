package com.nesmelov.alexey.gpstracker.repository.storage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.nesmelov.alexey.gpstracker.repository.storage.converters.Converters;
import com.nesmelov.alexey.gpstracker.repository.storage.dao.AddressDao;
import com.nesmelov.alexey.gpstracker.repository.storage.dao.AlarmDao;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Address;
import com.nesmelov.alexey.gpstracker.repository.storage.model.Alarm;

@Database(entities = {Address.class, Alarm.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract AddressDao addressDao();
    public abstract AlarmDao alarmDao();
}
