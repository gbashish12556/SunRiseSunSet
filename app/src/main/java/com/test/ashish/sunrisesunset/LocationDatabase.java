package com.test.ashish.sunrisesunset;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

@Database(entities = Location.class,version = 1, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase {

    private static LocationDatabase instance;

    public abstract LocationDao locationDao();

    public static synchronized LocationDatabase getInstance(Context context){
        if(instance ==null){
            instance = Room.databaseBuilder(context.getApplicationContext(), LocationDatabase.class, "note_database")
                    .addCallback(mCallBack)
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    private static RoomDatabase.Callback mCallBack = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {

        private LocationDao noteDao;

        public PopulateDbAsyncTask(LocationDatabase db){
            noteDao = db.locationDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
//            noteDao.insert(new Location("title1", "description1", "1"));
            return null;
        }
    }
}
