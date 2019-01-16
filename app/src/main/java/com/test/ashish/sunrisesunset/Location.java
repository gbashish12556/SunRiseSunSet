package com.test.ashish.sunrisesunset;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "location_table" ,indices = {@Index(value = "address", unique = true)})
public class Location {


    @PrimaryKey(autoGenerate = true)
    private int id;

    private double Lat;
    private double Lng;

    private String address;

    public Location(double Lat, double Lng, String address){
        this.Lat = Lat;
        this.Lng = Lng;
        this.address = address;
    }


    public void setId(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public double getLat() {
        return Lat;
    }

    public double getLng() {
        return Lng;
    }

    public String getAddress() {
        return address;
    }

}