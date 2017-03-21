package com.startapp.example.locationtracker;

import android.location.Location;

import java.io.Serializable;

/**
 * Created by Dmitri Nemets on 14/02/2017.
 */

public class MyLocation implements Serializable {

    public static final String HEADER = "Timestamp;Latitude;Longitude;Accuracy;Provider;Mode";

    private String provider;
    private double lat;
    private double lon;
    private float accuracy;
    private String timestamp;
    private LocationStrategy locationStrategy;

    public MyLocation(Location location, LocationStrategy strategy) {
        this.provider = location.getProvider();
        this.lat = location.getLatitude();
        this.lon = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.timestamp = Utils.timestampToDate(location.getTime());
        this.locationStrategy = strategy;
    }

    public String getProvider() {
        return provider;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public LocationStrategy getLocationStrategy() {
        return locationStrategy;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(timestamp)
                .append(";")
                .append(lat)
                .append(";")
                .append(lon)
                .append(";")
                .append(accuracy)
                .append(";")
                .append(provider)
                .append(";")
                .append(locationStrategy.toPrintableString())
                .toString();
    }
}
