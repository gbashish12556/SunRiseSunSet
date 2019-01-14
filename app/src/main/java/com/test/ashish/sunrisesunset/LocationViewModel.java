package com.test.ashish.sunrisesunset;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class LocationViewModel extends AndroidViewModel {

    private LocationRepository locationRepository;
    private LiveData<List<Location>> allLocation;

    public LocationViewModel(@NonNull Application application) {
        super(application);
        locationRepository = new LocationRepository(application);
        allLocation = locationRepository.getAllLocation();
    }

    public void insert(Location note){
        locationRepository.insert(note);
    }

    public LiveData<List<Location>> getAllLocation(){
        return locationRepository.getAllLocation();
    }


}
