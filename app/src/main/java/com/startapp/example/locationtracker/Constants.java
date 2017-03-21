package com.startapp.example.locationtracker;

/**
 * Created by dmitrinemets on 02/03/2017.
 */

public class Constants {
    // Shared prefs
    public static final String SP_FILE_NAME = "sp";
    public static final String SP_KEY_SERVICE_RUNNING = "isRunning";
    public static final String SP_KEY_GOOGLE_STATUS = "isGoogleConnected";

    public static final String GOOGLE_CONNECTED_VALUE_OK = "OK";

    // Broadcast
    public static final String BR_ACTION_SERVICE_STATUS = "service_status";
    public static final String BR_ACTION_GOOGLE_STATUS = "google_status";
    public static final String BR_ACTION_LOCATION_UPDATE = "location_update";
    public static final String BR_EXTRA_SERVICE_STATUS = "service_status";
    public static final String BR_EXTRA_GOOGLE_STATUS = "google_api_client_status";
    public static final String BR_EXTRA_LOCATION_UPDATE = "location_update_extra";
}
