package com.test.ashish.sunrisesunset;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class MyConverters {

    @TypeConverter
    public static Long dateToLong(Date date){
        return date == null?null: date.getTime();
    }

    @TypeConverter
    public static Date longToDate(Long longTime){
        return longTime == null ? null:new Date(longTime);
    }
}
