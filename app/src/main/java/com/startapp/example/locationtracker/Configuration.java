package com.startapp.example.locationtracker;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Dmitri Nemets on 13/02/2017.
 */

public class Configuration {

    private static final String SP_STRATEGY = "location_strategy";
    private static final String SP_INTERVAL = "interval";
    private static final String SP_DISTANCE = "distance";
    private static final String SP_MANUAL_INTERVAL = "manualInterval";

    private LocationStrategy locationStrategy = LocationStrategy.PASSIVE;
    private int intervalSeconds = 0;
    private int distanceMeters = 0;
    private int manualInterval = 30; // seconds

    public static Configuration load(Context context) {
        Configuration configuration = new Configuration();
        SharedPreferences sharedPref = context.getSharedPreferences("sp", Context.MODE_PRIVATE);
        configuration.setLocationStrategy(LocationStrategy.valueOf(sharedPref.getString(SP_STRATEGY, LocationStrategy.FUSED_PRIORITY_LOW_POWER.toString())));
        configuration.setIntervalSeconds(sharedPref.getInt(SP_INTERVAL, 0));
        configuration.setDistanceMeters(sharedPref.getInt(SP_DISTANCE, 0));
        configuration.setManualInterval(sharedPref.getInt(SP_MANUAL_INTERVAL, 30));
        return configuration;
    }

    public void save(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("sp", Context.MODE_PRIVATE);
        sharedPref.edit().putString(SP_STRATEGY, locationStrategy.toString())
                .putInt(SP_INTERVAL, intervalSeconds)
                .putInt(SP_DISTANCE, distanceMeters)
                .putInt(SP_MANUAL_INTERVAL, manualInterval)
                .apply();
    }

    public LocationStrategy getLocationStrategy() {
        return locationStrategy;
    }

    public void setLocationStrategy(LocationStrategy locationStrategy) {
        this.locationStrategy = locationStrategy;
    }

    public int getDistance() {
        return distanceMeters;
    }

    public int getUpdatesIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int interval) {
        this.intervalSeconds = interval;
    }

    public void setDistanceMeters(int distance) {
        this.distanceMeters = distance;
    }

    public int getManualInterval() {
        return manualInterval;
    }

    public void setManualInterval(int manualInterval) {
        this.manualInterval = manualInterval;
    }
}
