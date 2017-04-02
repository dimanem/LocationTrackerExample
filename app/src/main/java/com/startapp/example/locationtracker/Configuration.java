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

    private static final LocationStrategy DEFAULT_STRATEGY = LocationStrategy.FUSED_PRIORITY_NO_POWER;
    private static final int DEFAULT_INTERVAL_SECONDS = 300;
    private static final int DEFAULT_DISTANCE_METERS= 0;

    private LocationStrategy locationStrategy = DEFAULT_STRATEGY; // default strategy
    private int intervalSeconds = DEFAULT_INTERVAL_SECONDS; // 5 minutes
    private int distanceMeters = DEFAULT_DISTANCE_METERS;

    public static Configuration load(Context context) {
        Configuration configuration = new Configuration();
        SharedPreferences sharedPref = context.getSharedPreferences("sp", Context.MODE_PRIVATE);
        configuration.setLocationStrategy(LocationStrategy.valueOf(sharedPref.getString(SP_STRATEGY, DEFAULT_STRATEGY.toString())));
        configuration.setIntervalSeconds(sharedPref.getInt(SP_INTERVAL, DEFAULT_INTERVAL_SECONDS));
        configuration.setDistanceMeters(sharedPref.getInt(SP_DISTANCE, DEFAULT_DISTANCE_METERS));
        return configuration;
    }

    public void save(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("sp", Context.MODE_PRIVATE);
        sharedPref.edit().putString(SP_STRATEGY, locationStrategy.toString())
                .putInt(SP_INTERVAL, intervalSeconds)
                .putInt(SP_DISTANCE, distanceMeters)
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
}
