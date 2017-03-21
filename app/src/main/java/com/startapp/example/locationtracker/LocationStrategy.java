package com.startapp.example.locationtracker;

import android.location.LocationManager;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by Dmitri Nemets on 23/02/2017.
 */

public enum LocationStrategy {
    FUSED_PRIORITY_NO_POWER,
    FUSED_PRIORITY_LOW_POWER,
    FUSED_PRIORITY_HIGH_ACCURACY,
    FUSED_PRIORITY_BALANCED_POWER_ACCURACY,
    PASSIVE,
    NETWORK,
    GPS;

    public String getProvider() {
        switch (this) {
            case PASSIVE:
                return LocationManager.PASSIVE_PROVIDER;
            case NETWORK:
                return LocationManager.NETWORK_PROVIDER;
            case GPS:
                return LocationManager.GPS_PROVIDER;
        }
        return null;
    }

    public int getFusedPriority() {
        switch (this) {
            case FUSED_PRIORITY_NO_POWER:
                return LocationRequest.PRIORITY_NO_POWER;
            case FUSED_PRIORITY_LOW_POWER:
                return LocationRequest.PRIORITY_LOW_POWER;
            case FUSED_PRIORITY_BALANCED_POWER_ACCURACY:
                return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
            case FUSED_PRIORITY_HIGH_ACCURACY:
                return LocationRequest.PRIORITY_HIGH_ACCURACY;
        }
        return -1;
    }

    public String toPrintableString() {
        switch (this) {
            case PASSIVE:
                return "p";
            case NETWORK:
                return "n";
            case GPS:
                return "g";
            case FUSED_PRIORITY_NO_POWER:
                return "fnp";
            case FUSED_PRIORITY_LOW_POWER:
                return "flp";
            case FUSED_PRIORITY_HIGH_ACCURACY:
                return "fha";
            case FUSED_PRIORITY_BALANCED_POWER_ACCURACY:
                return "fbpa";
            default:
                return "";
        }
    }
}
