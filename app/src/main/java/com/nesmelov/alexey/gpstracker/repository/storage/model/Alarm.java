package com.nesmelov.alexey.gpstracker.repository.storage.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.sql.Date;

@Entity
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String name = "";
    public double lat;
    public double lon;
    public double radius;
    public boolean turnedOn;
    public int color;
    public Date date;
}
