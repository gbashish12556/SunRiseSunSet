package com.test.ashish.sunrisesunset;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class LocationRepository {

    private LocationDao locationDao;
    private LiveData<List<Location>> allLocation;

    public LocationRepository(Application application){
        LocationDatabase  locationDatabase = LocationDatabase.getInstance(application);
        locationDao = locationDatabase.locationDao();
        allLocation = locationDao.getAllLocation();
    }

    public void insert(Location location){
        new InserAsyncTask(locationDao).execute(location);
    }

    public LiveData<List<Location>> getAllLocation(){
        return allLocation;
    }

    public static class InserAsyncTask extends AsyncTask<Location, Void,Void> {

        private LocationDao locationDao;

        public InserAsyncTask(LocationDao locationDao){
            this.locationDao = locationDao;
        }

        @Override
        protected Void doInBackground(Location... notes) {
            locationDao.insert(notes[0]);
            return null;
        }
    }
}
