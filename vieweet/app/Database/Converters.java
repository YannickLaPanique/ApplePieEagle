package com.vieweet.app.Database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Status toStatus(int value) {
        if (value == 0) return Status.LOCAL;
        else if (value == 2) return Status.ONLINE;
        else return Status.PROCESSED;
    }

    @TypeConverter
    public static int toStatusInt(Status status) {
        if (status == Status.ONLINE) return 2;
        else if (status == Status.LOCAL) return 0;
        else return 5;
    }
}
