package com.example.positiontrace;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private GoogleApiClient googleApiClient;
    private com.google.android.gms.location.LocationListener listener;
    private Context context = this;
    private float allDistance = 0;
    private Location lastLocation = null;
    private AddressResultReceiver resultReceiver;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String address = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);

            if(resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                TextView text = (TextView) findViewById(R.id.address);
                text.setText(address);
                Toast.makeText(getApplicationContext(), "Address found", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void onClick(View view) {
        startIntentService();
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, resultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, lastLocation);
        startService(intent);
    }

    public void startNavigate(View view) {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
        } else {
            Toast.makeText(this, "Permission danied", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public void stopNavigate(View view) {
        Intent intent = new Intent(this, LocationService.class);
        allDistance = 0;
        stopService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyMessage messageReceiver = new MyMessage();
        registerReceiver(messageReceiver, new IntentFilter("NEW_LOCATION"));

        Log.i("AC", "activity created");

        resultReceiver = new AddressResultReceiver(new Handler());
    }

    class MyMessage extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase("NEW_LOCATION")) {
                Bundle extra = intent.getExtras();
                Location location = extra.getParcelable("Location");

                Log.i("MM", "New location received");
                TextView text = (TextView) findViewById(R.id.text2);

                if(lastLocation != null) {
                    float distance = location.distanceTo(lastLocation);
                    allDistance += distance;

                }

                if(location != null) {
                    lastLocation = location;
                }

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Latitude: " + String.valueOf(location.getLatitude()) + "\n");
                stringBuilder.append("Longitude: " + String.valueOf(location.getLongitude()) + "\n");
                stringBuilder.append("Altitude: " + String.valueOf(location.getAltitude()) + "\n");
                stringBuilder.append("Accuracy: " + String.valueOf(location.getAccuracy()) + "\n");
                stringBuilder.append("Bearing: " + String.valueOf(location.getBearing()) + "\n");
                stringBuilder.append("Speed: " + String.valueOf(location.getSpeed()) + "\n");
                stringBuilder.append("Actual distance: " + String.valueOf(allDistance) + "\n");
                text.setText(stringBuilder.toString());

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                TextView time = (TextView) findViewById(R.id.time2);
                time.setText("Time: " + sdf.format(new Date()));

            }
        }
    }

    @Override
    protected void onStart() {
        Log.i("AC", "activity started");
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case 1:
                if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, LocationService.class);
                    startService(intent);
                } else {
                    Toast.makeText(this, "Location is null", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }
}
