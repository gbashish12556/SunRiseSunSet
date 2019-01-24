package com.test.ashish.sunrisesunset;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface LocationDao {

    @Insert(onConflict = REPLACE)
    void insert(Location location);

    @Query("SELECT*FROM location_table ORDER BY id")
    LiveData<List<Location>> getAllLocation();

    @Query("SELECT*FROM location_table WHERE id= :id")
    Location getLocation(int id);

}
