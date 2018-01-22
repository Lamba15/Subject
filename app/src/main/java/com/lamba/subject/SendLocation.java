package com.lamba.subject;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SendLocation extends Service {

    Handler handler = new Handler();
    Runnable runnable;
    FusedLocationProviderClient location;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class Update extends AsyncTask<String, Void, String> {

        String a7mos = "";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(SendLocation.this, s, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("http://192.168.1.3/getlocation.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                OutputStream out = urlConnection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
                BufferedWriter bufferedWriter = new BufferedWriter(writer);

                String tempdata = URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8") + "&" +
                        URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8");

                bufferedWriter.write(tempdata);
                bufferedWriter.flush();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;

                    a7mos += current;

                    data = reader.read();
                }


            } catch (Exception e) {
                a7mos = "eroor";
            }

            return a7mos;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        location = LocationServices.getFusedLocationProviderClient(this);

        runnable = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(SendLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SendLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                location.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        new Update().execute(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
                    }
                });
                handler.postDelayed(runnable, 10000);
            }
        };
        handler.postDelayed(runnable, 10000);

        return START_STICKY;
    }
}
