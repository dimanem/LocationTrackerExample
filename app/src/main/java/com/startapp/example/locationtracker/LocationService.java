package com.startapp.example.locationtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

@SuppressWarnings({"ResourceType"})
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = "LocationService";
    private static final String TAG_PS = TAG + "_PS";

    // Framework
    private LocationManager locationManager = null;

    // Google Play Services
    protected GoogleApiClient googleApiClient = null;
    private LocationRequest locationRequest = null;

    // Configuration
    private Configuration configuration;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        configuration = Configuration.load(getApplicationContext());
        LocationStrategy locationStrategy = configuration.getLocationStrategy();
        int updatesInterval = configuration.getUpdatesIntervalSeconds();
        int requestsDistance = configuration.getDistance();

        Log.d(TAG, "Starting service: Strategy: " + locationStrategy + ", Updates Interval: " + updatesInterval);

        switch (locationStrategy) {
            // Framework
            case PASSIVE:
            case NETWORK:
            case GPS:
                locationManager.requestLocationUpdates(locationStrategy.getProvider(), TimeUnit.SECONDS.toMillis(updatesInterval), requestsDistance, LocationListener);
                break;
            // Google Play Services
            case FUSED_PRIORITY_NO_POWER:
            case FUSED_PRIORITY_LOW_POWER:
            case FUSED_PRIORITY_BALANCED_POWER_ACCURACY:
            case FUSED_PRIORITY_HIGH_ACCURACY:
                buildGoogleAPIClient();
                buildLocationRequest();
                break;
        }
        onServiceStarted();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        locationManager.removeUpdates(LocationListener);
        googleApiClient.disconnect();
        onServiceStopped();
        onGoogleClientDisconnected("Service died");
        super.onDestroy();
    }

    private void onServiceStarted() {
        // Notify user
        Toast.makeText(getApplicationContext(), "Service started!", Toast.LENGTH_SHORT).show();
        // Update Shared Prefs
        getSharedPrefsEditor().putBoolean(Constants.SP_KEY_SERVICE_RUNNING, true).apply();
        // Send broadcast
        getLocalBroadcastManager().sendBroadcast(new Intent(Constants.BR_ACTION_SERVICE_STATUS).putExtra(Constants.BR_EXTRA_SERVICE_STATUS, true));
    }

    private void onServiceStopped() {
        // Notify user
        Toast.makeText(getApplicationContext(), "Service stopped!", Toast.LENGTH_SHORT).show();
        // Update shared prefs
        getSharedPrefsEditor().putBoolean(Constants.SP_KEY_SERVICE_RUNNING, false).apply();
        // Send broadcast
        getLocalBroadcastManager().sendBroadcast(new Intent(Constants.BR_ACTION_SERVICE_STATUS).putExtra(Constants.BR_EXTRA_SERVICE_STATUS, false));
    }

    // Define a listener that responds to location updates
    private LocationListener LocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            onReceivedLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    /****************************************************************************************************************************/
    /*********************************************** Google Play Services Start **************************************************/
    /****************************************************************************************************************************/
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG_PS, "onLocationChanged: " + location.toString());
        onReceivedLocation(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG_PS, "Google API client connected");
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            onReceivedLocation(lastLocation);
        }
        startLocationUpdates();
        onGoogleAPIClientConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        onGoogleClientDisconnected("Connection suspended due to: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        onGoogleClientDisconnected("Connection failed due to: " + connectionResult.getErrorMessage());
    }

    private void buildGoogleAPIClient() {
        Log.i(TAG_PS, "Building GoogleApiClient");
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    private void buildLocationRequest() {
        Log.v(TAG_PS, "buildLocationRequest");

        locationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(configuration.getUpdatesIntervalSeconds()));

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(30));

        locationRequest.setPriority(configuration.getLocationStrategy().getFusedPriority());
    }

    private void startLocationUpdates() {
        Log.v(TAG_PS, "startLocationUpdates");
        try {
            Log.d(TAG_PS, "Requesting location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } catch (Exception e) {
            Log.e(TAG_PS, "Failed to request location updates");
        }
    }

    private void onGoogleAPIClientConnected() {
        getSharedPrefsEditor()
                .putString(Constants.SP_KEY_GOOGLE_STATUS, Constants.GOOGLE_CONNECTED_VALUE_OK).apply();

        getLocalBroadcastManager()
                .sendBroadcast(new Intent(Constants.BR_ACTION_GOOGLE_STATUS)
                .putExtra(Constants.BR_EXTRA_GOOGLE_STATUS, Constants.GOOGLE_CONNECTED_VALUE_OK));
    }

    private void onGoogleClientDisconnected(String reason) {
        getSharedPrefsEditor().putString(Constants.SP_KEY_GOOGLE_STATUS, reason).apply();

        getLocalBroadcastManager()
                .sendBroadcast(new Intent(Constants.BR_ACTION_GOOGLE_STATUS)
                .putExtra(Constants.BR_EXTRA_GOOGLE_STATUS, reason));
    }

    /****************************************************************************************************************************/
    /*********************************************** Google Play Services End **************************************************/
    /****************************************************************************************************************************/

    private void onReceivedLocation(Location location) {
        MyLocation locationUpdate = new MyLocation(location, configuration.getLocationStrategy());
        LocationStorage.addLocation(getApplicationContext(), locationUpdate);
        Intent lastKnownLocationIntent = new Intent(Constants.BR_ACTION_LOCATION_UPDATE);
        lastKnownLocationIntent.putExtra(Constants.BR_EXTRA_LOCATION_UPDATE, locationUpdate);
        getLocalBroadcastManager().sendBroadcast(lastKnownLocationIntent);
    }

    private LocalBroadcastManager getLocalBroadcastManager() {
        return LocalBroadcastManager.getInstance(getApplicationContext());
    }

    private SharedPreferences.Editor getSharedPrefsEditor() {
        return getApplicationContext().getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE).edit();
    }
}
