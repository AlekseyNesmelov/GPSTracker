package com.nesmelov.alexey.gpstracker.application.modules;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.nesmelov.alexey.gpstracker.repository.storage.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppDatabaseModule {

    private static final String DATABASE_NAME = "database";

    @Provides
    @Singleton
    AppDatabase provideDatabase(final Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
    }
}
