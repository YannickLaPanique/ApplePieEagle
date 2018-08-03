package com.vieweet.app.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

@Database(entities = {Album.class, Picture.class, Hotspot.class}, version = 1)
@TypeConverters(Converters.class)
public abstract class MyDatabase extends RoomDatabase{
    public abstract MyDao myDao();
}

