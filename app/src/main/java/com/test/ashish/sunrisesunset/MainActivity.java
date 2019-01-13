package com.test.ashish.sunrisesunset;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public String mAddressOutput;
    public String mAreaOutput;
    public String mCityOutput;
    public String mStreetOutput;
    public String mPostaleCode;
    private TextView mLocationText;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_PICKUP = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_DROPOFF = 2;
    private String pickup_address = "", dropoff_address = "";

    LinearLayout pickup_container;
    SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    Location location;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String TAG = "MAP LOCATION";
    private Activity mContext;
    private TextView mLocationMarkerText;
    TextView pickup_location, dropoff_location;
    int journey_type = 0;
    private LatLng mCenterLatLong;
    double current_lat = 22.58, current_lng = 88.34;
    LinearLayout marker_container;
    ImageView imageMarker;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static GoogleApiClient mGoogleApiClient;
    private AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        pickup_container = (LinearLayout) findViewById(R.id.pickup_point_container);
        pickup_location = (TextView) findViewById(R.id.pickup_location);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mMapFragment);

        marker_container = (LinearLayout) findViewById(R.id.marker_container);
        imageMarker = (ImageView) findViewById(R.id.imageMarker);
        mResultReceiver = new AddressResultReceiver(new Handler());
        final AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        pickup_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    LatLng southwest = new LatLng(180, 180);
                    LatLng northeast = new LatLng(180, 180);
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setBoundsBias(new LatLngBounds(southwest, northeast))
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
        buildGoogleApiClient();
    }

    public synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        LoadAddress();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_location:
                saveLocation();
                return true;
            case R.id.bookmarked_location:
                bookmarkedLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveLocation(){

    }

    public void bookmarkedLocation(){

    }
    public void LoadAddress(){
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
        if(mGoogleApiClient.isConnected()) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                changeMap(mLastLocation);
            } else
                try {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            try {
                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(5000);
                mLocationRequest.setFastestInterval(1000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(this != null) {
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
            if (mGoogleApiClient.isConnected()) {
                try {
                    if (location != null)
                        changeMap(location);
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void changeMap(Location location) {
        if(this != null) {
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
                mMap = mMapFragment.getMap();
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
                            marker_container.setVisibility(View.VISIBLE);

                            startIntentService(mLocation);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            if (mMap != null) {
                mMap.getUiSettings().setZoomControlsEnabled(false);
                LatLng latLong;
                latLong = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLong).zoom(19f).tilt(70).build();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
        if(this!= null) {
            // Create an intent for passing to the intent service responsible for fetching the address.
            Intent intent = new Intent(this, FetchAddressIntentService.class);
            // Pass the result receiver as an extra to the service.
            intent.putExtra(AppUtils.LocationConstants.RECEIVER, mResultReceiver);
            // Pass the location data as an extra to the service.
            intent.putExtra(AppUtils.LocationConstants.LOCATION_DATA_EXTRA, mLocation);
            // Start the service. If the service isn't already running, it is instantiated and started
            // (creating a process for it if needed); if it is running then it remains running. The
            // service kills itself automatically once all intents are processed.
            startService(intent);
        }
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
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}

