package com.startapp.example.locationtracker;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitri Nemets on 23/02/2017.
 */

public class LocationStorage {
    public static List<MyLocation> getLocations(Context context) {
        List<MyLocation> locations = new ArrayList<>();
        try {
            String locationsSet = context.getSharedPreferences("sp", Context.MODE_PRIVATE).getString("locations", null);
            if (locationsSet != null) {
                locations = new Gson().fromJson(locationsSet, new TypeToken<List<MyLocation>>() {
                }.getType());
            }
        } catch (Exception e) {
        }

        return locations;
    }

    public static void addLocation(Context context, MyLocation location) {
        List<MyLocation> locations = getLocations(context);
        locations.add(location);
        context.getSharedPreferences("sp", Context.MODE_PRIVATE).edit().putString("locations", new Gson().toJson(locations)).apply();
    }
}
