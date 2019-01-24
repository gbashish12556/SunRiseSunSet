package com.test.ashish.sunrisesunset;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{


    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_PICKUP = 1;
    private List<com.test.ashish.sunrisesunset.Location> locations;
    private LocationViewModel locationViewModel;
    private Location mLastLocation;
    private String mAddressOutput, mAreaOutput, mStreetOutput, mCityOutput, mPostaleCode;

    private LinearLayout pickup_container;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private Activity mContext;
    private TextView pickup_location, dateTextView, sunRiseTextView, sunSetTextView, moonRiseTextView, moonsetTextView;

    private LatLng mCenterLatLong;
    private double current_lat = 22.58, current_lng = 88.34;
    private ImageView nextDayButton, previousDayButton, currentDayButton;

    private AddressResultReceiver mResultReceiver;
    private FusedLocationProviderClient client;
    private Calendar cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intialising context
        mContext = this;

        //Taking reference of all the views
        mLastLocation = new Location(LocationManager.GPS_PROVIDER);
        pickup_container = (LinearLayout) findViewById(R.id.pickup_point_container);
        pickup_location = (TextView) findViewById(R.id.pickup_location);
        dateTextView = findViewById(R.id.today_date);

        sunRiseTextView = findViewById(R.id.sunrise_time);
        sunSetTextView = findViewById(R.id.sunset_time);
        moonRiseTextView = findViewById(R.id.moonrise_time);
        moonsetTextView = findViewById(R.id.moonset_time);


        //Intialising calender object
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        TimeZone tz = TimeZone.getTimeZone("GMT+5:30");
        cal.setTime(new Date());
        cal.setTimeZone(tz);

        //Defining actions on all the buttons
        nextDayButton= findViewById(R.id.next_date);
        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.setTime(new Date((cal.getTimeInMillis()+86400000)));
                changeDate();
            }
        });

        previousDayButton= findViewById(R.id.previous_date);
        previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.setTime(new Date((cal.getTimeInMillis()-86400000)));
                changeDate();
            }
        });

        currentDayButton= findViewById(R.id.current_date);
        currentDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.setTime(new Date());
                changeDate();
            }
        });

        //Intialising mapFragment
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mMapFragment);
        mMapFragment.getMapAsync(this);

        //Intialising location viewmodel
        locationViewModel = (LocationViewModel) ViewModelProviders.of(this).get(LocationViewModel.class);

        mResultReceiver = new AddressResultReceiver(new Handler());
        final AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        pickup_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(typeFilter)
                            .build(mContext);
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });
        changeDate();
    }

    public void createNotification(){

        //Build notification dialog for showing golden hour

        String title = "GOLDEN HOUR";
        String message = "this is golden hour";
        Intent i = new Intent(this, MainActivity.class);
        int notificationId = 1;

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(getApplicationContext());
        taskStackBuilder.addNextIntentWithParentStack(i);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), title)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        //Check version of sdk
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }
        else {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    public void changeDate(){

        //Change date on datetextview
        SimpleDateFormat format1 = new SimpleDateFormat("EEE, MMM d, yyyy");
        String formatted = format1.format(cal.getTime());
        dateTextView.setText(formatted);
        calculateTime();
    }

    public void calculateTime(){


        //Calculate sun rise/set and moon rise/set time
        SunMoonHelper sunMoonHelper = new SunMoonHelper(mLastLocation.getLongitude(), mLastLocation.getLatitude());
        sunMoonHelper.setTime(cal.getTimeInMillis());

        long currentTimeiInMillis = cal.getTimeInMillis();

        //Intialise calender
        Calendar calender = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("GMT+5:30");
        calender.setTimeZone(tz);
        SimpleDateFormat format1 = new SimpleDateFormat("hh:mm aaa");

        //Calculating sunrise time and setting to text view
        long sunrRiseTimeMillis = sunMoonHelper.getSunRiseSet(true);
        calender.setTimeInMillis(sunrRiseTimeMillis);
        String sunRiseTime = format1.format(calender.getTime());

        sunRiseTextView.setText(sunRiseTime);

        //Calculating sunset time and setting to text view
        long sunSetTimeMillis =  sunMoonHelper.getSunRiseSet(false);
        calender.setTimeInMillis(sunSetTimeMillis);
        String sunSetTime = format1.format(calender.getTime());

        sunSetTextView.setText(sunSetTime);

        //Calculating moon time and setting to text view
        long moonRiseTimeMillis =  sunMoonHelper.getMoonRiseSet(true);
        calender.setTimeInMillis(moonRiseTimeMillis);
        String moonRiseTime = format1.format(calender.getTime());

        moonRiseTextView.setText(moonRiseTime);

        //Calculating moonset time and setting to text view
        long moonSetTimeMillis =  sunMoonHelper.getMoonRiseSet(false);
        calender.setTimeInMillis(moonSetTimeMillis);
        String moonSetTime = format1.format(calender.getTime());

        moonsetTextView.setText(moonSetTime);

        //Checking if current time is within golden hour range
        if((sunSetTimeMillis-currentTimeiInMillis)>0 && (sunSetTimeMillis-currentTimeiInMillis) < 60*60*1000){
            createNotification();
        }

    }



    public void drawLine(double latitude, double longitude) {

        PolylineOptions rectOptions = new PolylineOptions();

        ArrayList<LatLng> pathPoint = new ArrayList<LatLng>();

        pathPoint.add(new LatLng(latitude, longitude));
        pathPoint.add(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        if(mMap != null) {
            rectOptions.addAll(pathPoint);
            rectOptions.width(5);
            rectOptions.color(Color.BLUE);
            mMap.addPolyline(rectOptions);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        //Crating tool bar action menu for bookmark and pin button
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Checking if bookmark or pin has been clicked
        // and handling the respective event
        switch (item.getItemId()) {
            case R.id.save_location:

                //saving current location in database
                saveLocation();
                return true;
            case R.id.bookmarked_location:

                //fetching all the saved location in database
                bookmarkedLocation(this.findViewById(R.id.bookmarked_location));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveLocation() {
        //saving current location in database
        com.test.ashish.sunrisesunset.Location location = new com.test.ashish.sunrisesunset.Location(mLastLocation.getLatitude(), mLastLocation.getLongitude(), mAddressOutput, new Date());
        locationViewModel.insert(location);
    }

    public void bookmarkedLocation(View view) {

        //fetching all the saved location in database
        final PopupMenu popupMenu = new PopupMenu(this, view);
        locationViewModel = (LocationViewModel) ViewModelProviders.of(this).get(LocationViewModel.class);
        locationViewModel.getAllLocation().observe(this,new Observer<List<com.test.ashish.sunrisesunset.Location>>() {
            @Override
            public void onChanged(@Nullable List<com.test.ashish.sunrisesunset.Location> notes) {
                for(int i=0;i<notes.size();i++) {
                    popupMenu.getMenu().add(Menu.NONE, notes.get(i).getId(), notes.get(i).getId(), notes.get(i).getAddress()).setEnabled(true);
                }
            }
        });

        popupMenu.show();

        //handling action on click of popup menu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                final int id  = item.getItemId();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final com.test.ashish.sunrisesunset.Location location = locationViewModel.getLocation(id);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Location temp = new Location(LocationManager.GPS_PROVIDER);
                                temp.setLatitude(location.getLat());
                                temp.setLongitude(location.getLng());
                                mLastLocation = temp;
                                changeMap(mLastLocation);
                            }//public void run() {
                        });
                    }
                }).start();
                return true;
            }
        });

    }

    public void LoadAddress() {

        //Checking GPS permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        client = LocationServices.getFusedLocationProviderClient(this);

        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                //If GPS is working fine load map
                if (location != null) {
                    mLastLocation = location;
                    changeMap(mLastLocation);
                }
            }
        });

    }

    private void changeMap(Location location) {
        calculateTime();
        if (this != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            // check if map is created successfully or not
            if (mMap == null) {
                mMapFragment.getMapAsync(this);
            }
            if (mMap != null) {

                //Configuring map settings and setting the marker to current location
                mMap.getUiSettings().setZoomControlsEnabled(false);
                LatLng latLong = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLong).zoom(10f).build();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                //Starting FetchIntentService for fetching current location
                startIntentService(location);

            } else {
                Toast.makeText(this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    public void startIntentService(Location mLocation) {
        if (this != null) {
            // Create an intent for passing to the intent service responsible for fetching the address.
            Intent intent = new Intent(MainActivity.this, FetchAddressIntentService.class);
            // Pass the result receiver as an extra to the service.
            intent.putExtra(AppUtils.LocationConstants.RECEIVER, mResultReceiver);
            // Pass the location data as an extra to the service.
            intent.putExtra(AppUtils.LocationConstants.LOCATION_DATA_EXTRA, mLocation);
            // Start the service. If the service isn't already running, it is instantiated and started
            // (creating a process for it if needed); if it is running then it remains running. The
            // service kills itself automatically once all intents are processed.
            this.startService(intent);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mCenterLatLong = cameraPosition.target;
                current_lat = mCenterLatLong.latitude;
                current_lng = mCenterLatLong.longitude;
                mMap.clear();
                try {
                    Location mLocation = new Location("");
                    mLocation.setLatitude(current_lat);
                    mLocation.setLongitude(current_lng);
                    mLastLocation = mLocation;
                    startIntentService(mLastLocation);
                    calculateTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        //TODO: UI updates.
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        LoadAddress();
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(AppUtils.LocationConstants.RESULT_DATA_KEY);
            mAreaOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_AREA);
            mCityOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_CITY);
            mPostaleCode  = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_EXTRA);
            mStreetOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_STREET);
            mAddressOutput = mStreetOutput;
            displayAddressOutput();
            // Show a toast message if an address was found.
            if (resultCode == AppUtils.LocationConstants.SUCCESS_RESULT) {
                //  showToast(getString(R.string.address_found));

            }
        }
    }
    /**
     * Updates the address in the UI.
     */
    public void displayAddressOutput() {
        //Display the address of current location in textview
        try {
            if (mAreaOutput != null)
                pickup_location.setText(mAddressOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Called after the autocomplete activity has finished to return its result.
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(this != null) {
            // Check that the result was from the autocomplete widget.
            if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_PICKUP) {
                if (resultCode == this.RESULT_OK) {
                    // Get the user's selected place from the Intent.
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    // TODO call location based filter
                    LatLng latLong;
                    latLong = place.getLatLng();
                    mLastLocation.setLatitude(latLong.latitude);
                    mLastLocation.setLongitude(latLong.longitude);
                    pickup_location.setText(place.getName().toString().replaceAll("[\r\n]+", " ") + "");
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(latLong).zoom(17).build();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    changeDate();
                }
            }
        }
    }
}

