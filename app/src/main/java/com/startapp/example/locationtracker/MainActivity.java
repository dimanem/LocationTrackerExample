package com.startapp.example.locationtracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final int REQUEST_CODE_PERMISSIONS = 1;

    private LocationStrategy selectedStrategy;

    private TextView tvCurrentStrategy;

    private EditText etDistance;
    private EditText etInterval;
    private TextView tvStatus;
    private TextView tvGoogleStatus;

    private TextView tvResult;

    private boolean requestingPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurrentStrategy = (TextView) findViewById(R.id.tvCurrStrategy);
        tvCurrentStrategy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeStrategyClick();
            }
        });

        etDistance = (EditText) findViewById(R.id.etDistance);
        etInterval = (EditText) findViewById(R.id.etInterval);

        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvGoogleStatus = (TextView) findViewById(R.id.tvGoogleClientStatus);
        tvResult = (TextView) findViewById(R.id.tvRes);

        Configuration configuration = Configuration.load(this);

        etDistance.setText(String.valueOf(configuration.getDistance()));
        etInterval.setText(String.valueOf(configuration.getUpdatesIntervalSeconds()));

        etDistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Distance changed, restarting service");
                restartLocationService();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "Distance changed, restarting service");
                restartLocationService();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Interval changed, restarting service");
                restartLocationService();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        requestPermissions();

        onNewStrategy(configuration.getLocationStrategy());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
        LocalBroadcastManager.getInstance(this).registerReceiver(LocationReceiver, new IntentFilter(Constants.BR_ACTION_LOCATION_UPDATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(LocationReceiver, new IntentFilter(Constants.BR_ACTION_SERVICE_STATUS));
        LocalBroadcastManager.getInstance(this).registerReceiver(LocationReceiver, new IntentFilter(Constants.BR_ACTION_GOOGLE_STATUS));
        if (!isLocationEnabled()) {
            tryEnableLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(LocationReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            Log.d(TAG, "onRequestPermissionsResult");
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Not all permissions were granted!");
                    Toast.makeText(this, "Not all permissions were granted!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Log.d(TAG, "Success granting permissions!");
            requestingPermissions = false;
            restartLocationService();
        }
    }

    private void onChangeStrategyClick() {
        final ArrayList<LocationStrategy> locationStrategies = new ArrayList<>(EnumSet.allOf(LocationStrategy.class));
        List<String> locationStrategiesStrs = new ArrayList<>();
        int selectedPosition = 0;
        for (int i = 0; i < locationStrategies.size(); i++) {
            LocationStrategy locationStrategy = locationStrategies.get(i);
            if (locationStrategy.equals(selectedStrategy)) {
                selectedPosition = i;
            }
            locationStrategiesStrs.add(locationStrategy.toString());
        }
        String[] locationStrategiesArr = new String[locationStrategiesStrs.size()];
        locationStrategiesArr = locationStrategiesStrs.toArray(locationStrategiesArr);

        new AlertDialog.Builder(this)
                .setSingleChoiceItems(locationStrategiesArr, selectedPosition, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        onNewStrategy(locationStrategies.get(selectedPosition));
                    }
                })
                .show();
    }

    private void onNewStrategy(LocationStrategy locationStrategy) {
        Log.d(TAG, "New Strategy selected: " + locationStrategy);
        selectedStrategy = locationStrategy;
        tvCurrentStrategy.setText("Location Strategy: " + locationStrategy.toString());
        if (!requestingPermissions) {
            restartLocationService();
        }
    }

    private BroadcastReceiver LocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BR_ACTION_LOCATION_UPDATE)) { // location update
                Toast.makeText(context, "Received location update", Toast.LENGTH_SHORT).show();
                onLocationUpdate((MyLocation) intent.getExtras().get(Constants.BR_EXTRA_LOCATION_UPDATE));
            } else if (intent.getAction().equals(Constants.BR_ACTION_SERVICE_STATUS)) { // service status
                updateServiceStatus(intent.getExtras().getBoolean(Constants.BR_EXTRA_SERVICE_STATUS, false));
            } else if (intent.getAction().equals(Constants.BR_ACTION_GOOGLE_STATUS)) { // google status
                updateGoogleStatus(intent.getExtras().getString(Constants.BR_EXTRA_GOOGLE_STATUS));
            }
        }
    };

    private void onLocationUpdate(MyLocation location) {
        if (location != null) {
            tvResult.setText("Last Update: \n" + location.toString());
        }
    }

    private void requestPermissions() {
        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestingPermissions = true;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS);
            return;
        }
    }

    private void restartLocationService() {
        Log.v(TAG, "restartLocationService");
        stopLocationService();
        startLocationService();
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return gps_enabled || network_enabled;
    }

    private void tryEnableLocation() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Location services not available");
        dialog.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private void updateStatus() {
        updateServiceStatus(isLocationServiceRunning());
        updateGoogleStatus(getGoogleAPIClientStatus());
    }

    private String getGoogleAPIClientStatus() {
        return getApplicationContext().getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE).getString(Constants.SP_KEY_GOOGLE_STATUS, "Disconnected");
    }

    private void updateServiceStatus(boolean isRunning) {
        if (isRunning) {
            tvStatus.setText("Service Status: Running");
        } else {
            tvStatus.setText("Service Status: Stopped");
        }
    }

    private void updateGoogleStatus(String status) {
        if (status.equals(Constants.GOOGLE_CONNECTED_VALUE_OK)) {
            tvGoogleStatus.setText("Google status: OK");
        } else {
            tvGoogleStatus.setText("Google status: " + status);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.startService:
                updateConfiguration();
                startLocationService();
                return true;
            case R.id.stopService:
                stopLocationService();
                return true;
            case R.id.showHistory:
                showHistory();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startLocationService() {
        startService(new Intent(this, LocationService.class));
    }

    private void stopLocationService() {
        stopService(new Intent(this, LocationService.class));
    }

    private void updateConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setLocationStrategy(selectedStrategy);
        configuration.setIntervalSeconds(Integer.valueOf(etInterval.getText().toString()));
        configuration.setDistanceMeters(Integer.valueOf(etDistance.getText().toString()));
        configuration.save(this);
    }

    private void showHistory() {
        String locationsListHeader = MyLocation.HEADER;
        final StringBuilder shareBuilder = new StringBuilder(locationsListHeader);

        List<MyLocation> receivedLocations = LocationStorage.getLocations(getApplicationContext());

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle("Locations History");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        final ListIterator<MyLocation> myLocationListIterator = receivedLocations.listIterator(receivedLocations.size());
        while (myLocationListIterator.hasPrevious()) {
            MyLocation previous = myLocationListIterator.previous();
            arrayAdapter.add(previous.toString());
            shareBuilder.append(previous.toString()).append("\n");
        }

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String toShare = shareBuilder.toString();
                File file = saveToFile(toShare);
                if (file != null) {
                    share(file);
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to generate location history file!", Toast.LENGTH_LONG).show();
                }
            }
        });

        builderSingle.setNeutralButton("Google Maps", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Location");
                alertDialog.setMessage(arrayAdapter.getItem(which));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
        AlertDialog alertDialog = builderSingle.create();
        alertDialog.getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.colorPrimary))); // set color
        alertDialog.getListView().setDividerHeight(1);
        alertDialog.show();
    }

    private void share(File fileToShare) {
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", fileToShare);
        Intent sendIntent = new Intent();
        sendIntent.setDataAndType(uri, getContentResolver().getType(uri));
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"yaniv.avraham@startapp.com"});
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Location History " + Utils.timestampToDate(System.currentTimeMillis()));
        startActivity(sendIntent);
    }

    private File saveToFile(String content) {
        File directoryFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Locations");
        if (!directoryFile.mkdirs()) {
        }

        File outFile = new File(directoryFile, "location_history.csv");
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outFile, false);
            outputStream.write(content.getBytes());
            outputStream.close();
            return outFile;
        } catch (Exception e) {
            Log.e(TAG, "Failed to save file with ex: " + e.getLocalizedMessage());
            return null;
        }
    }

    private boolean isLocationServiceRunning() {
        return getApplicationContext().getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE).getBoolean(Constants.SP_KEY_SERVICE_RUNNING, false)
                && Utils.isServiceRunning(getApplicationContext(), LocationService.class);
    }
}
