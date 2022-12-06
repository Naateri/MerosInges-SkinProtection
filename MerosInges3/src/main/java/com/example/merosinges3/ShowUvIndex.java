package com.example.merosinges3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ShowUvIndex extends AppCompatActivity implements LocationListener{

    private static final int REQUEST_LOCATION = 1;
    private int CUR_UV; // 1 -> myCity, 0 -> other City
    private TextView latit, longitude, UVIndex, curUV, time_st1, time_st2, time_st3;
    private EditText city;
    private Button getUV_myCity, getUV_City;
    LocationManager locationManager;
    double doubleLat, doubleLong, doubleMaxUV;

    public String currentUV, timeph1, timeph2, timeph3;

    boolean found_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_uv_index);

        latit = findViewById(R.id.latt);
        longitude = findViewById(R.id.longg);
        UVIndex = findViewById(R.id.UV_Index);
        curUV = findViewById(R.id.cur_uv);
        time_st1 = findViewById(R.id.time_st1);
        time_st2 = findViewById(R.id.time_st2);
        time_st3 = findViewById(R.id.time_st3);

        city = findViewById(R.id.editTextCity);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        getUV_myCity = findViewById(R.id.btn_getUV);
        getUV_City = findViewById(R.id.btn_getUV2);

        getUV_City.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // create object of MyAsyncTasks class and execute it
                CUR_UV = 0;
                MyAsyncTasks myAsyncTasks = new MyAsyncTasks();
                myAsyncTasks.execute();
            }
        });

        getUV_myCity.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // create object of MyAsyncTasks class and execute it
                CUR_UV = 1;
                MyAsyncTasks myAsyncTasks = new MyAsyncTasks();
                myAsyncTasks.execute();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("MyNotification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        doubleLat = 0.0;
        doubleLong = 0.0;
        doubleMaxUV = 0;
        currentUV = "";
        timeph1 = "";
        timeph2 = "";
        timeph3 = "";
    }

    public void showUVCity(View view){
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(city.getText().toString(), 1);

            if (addressList != null){
                doubleLat = addressList.get(0).getLatitude();
                doubleLong = addressList.get(0).getLongitude();

                latit.setText((String.valueOf(doubleLat)).substring(0, 7));
                longitude.setText((String.valueOf(doubleLong)).substring(0,7));
                found_location = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showUVMyCity(View view){
        if (ActivityCompat.checkSelfPermission(
                ShowUvIndex.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                ShowUvIndex.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Log.d("RadUV", "Location permission (FINE AND COARSE) OK");
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (locationGPS != null) {
                Log.d("RadUV","OnLocationChanged called.");
                onLocationChanged(locationGPS);
                found_location = true;
            } else {
                // Toast.makeText(this, "No se puede encontrar ubicación", Toast.LENGTH_SHORT).show();
                found_location = false;
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        doubleLat = location.getLatitude();
        doubleLong = location.getLongitude();

        latit.setText((String.valueOf(doubleLat)).substring(0, 7));
        longitude.setText((String.valueOf(doubleLong)).substring(0,7));
    }

    private class MyAsyncTasks extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ShowUvIndex.this);
            progressDialog.setMessage("processing results");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params){
            // Fetch data from the API in the background.

            // https://developer.android.com/reference/android/util/JsonReader

            if (CUR_UV == 0){
                showUVCity(findViewById(R.id.btn_getUV2));
                Log.d("UVRad", "Finding UV Rad NOT in my city");
            }
            else if (CUR_UV == 1) {
                Log.d("UVRad", "Finding UV Rad in my city");
                showUVMyCity(findViewById(R.id.btn_getUV));
            }

            if ((doubleLat == 0.0 && doubleLong == 0.0) || !found_location){
                return "No se encontró ubicación";
            }

            String text = "https://api.openuv.io/api/v1/uv?";
            String lat_text = String.valueOf(doubleLat);
            String long_text = String.valueOf(doubleLong);
            String call_to_api = text + "lat=" + lat_text + "&lng=" + long_text;

            String to_return = "";

            try {
                URL openUVApi = new URL(call_to_api);
                HttpsURLConnection myConnection = (HttpsURLConnection) openUVApi.openConnection();

                myConnection.setRequestProperty("x-access-token", "USE YOUR OWN API-KEY BUDDY :)");

                if (myConnection.getResponseCode() == 200) {
                    // Success
                    // Further processing here
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);

                    Log.d("UVRad", "Starting to read JSON");

                    boolean night_time = false;

                    jsonReader.beginObject();
                    while(jsonReader.hasNext()){
                        if (night_time) break;
                        String name = jsonReader.nextName();
                        Log.d("UVRad", "Current name: " + name);
                        if (name.equals("result")){
                            Log.d("UVRad", "We are in result");
                            jsonReader.beginObject(); // Start processing the JSON object

                            while (jsonReader.hasNext()) { // Loop through all keys
                                if (night_time) break;
                                String key = jsonReader.nextName(); // Fetch the next key
                                Log.d("UVRad", "Cur_key: " + key);

                                if (key.equals("uv_max")) { // Check if desired key
                                    Log.d("UVRad", "Key = uv_max");
                                    Log.d("UVRad", "Cur Value: ");
                                    // Fetch the value as a String
                                    String value = jsonReader.nextString();
                                    Log.d("UVRad", value);
                                    // Do something with the value
                                    to_return = "UV Máximo: " + value;
                                    //break; // Break out of the loop

                                    doubleMaxUV = Double.parseDouble(value);
                                } else if (key.equals("uv")){

                                    Log.d("UVRad", "Key = uv");
                                    String value = jsonReader.nextString();
                                    currentUV = "UV Actual: " + value;
                                    // jsonReader.skipValue(); // Skip values of other keys

                                } else if (key.equals("safe_exposure_time")) {

                                    if (night_time) break;

                                    Log.d("UVRad", "Key = safe_exposure_time");
                                    jsonReader.beginObject();

                                    while(jsonReader.hasNext()){
                                        if (night_time) break;
                                        String key_exp = jsonReader.nextName();
                                        Log.d("UVRad", "Key = " + key_exp);
                                        if (key_exp.equals("st2")){
                                            Log.d("UVRad", "Key = st2 HARDCODEEE");
                                            String value;
                                            try{
                                                value = jsonReader.nextString();
                                                timeph1 = "I, II, III: " + value;
                                            } catch(Exception e){
                                                night_time = true;
                                                timeph1 = "Ya";
                                                timeph2 = "es la";
                                                timeph3 = "noche :)";
                                            }
                                        } else if(key_exp.equals("st4")){
                                            Log.d("UVRad", "Key = st4");
                                            String value;
                                            try{
                                                value = jsonReader.nextString();
                                                timeph2 = "III, IV: " + value;
                                            } catch(Exception e){
                                                timeph2 = "es la";
                                            }


                                        } else if(key_exp.equals("st5")){
                                            Log.d("UVRad", "Key = st5");
                                            String value;
                                            try{
                                                value = jsonReader.nextString();
                                                timeph3 = "V, VI: " + value;
                                            } catch(Exception e){
                                                timeph3 = "noche :)";
                                            }

                                        } else {
                                            jsonReader.skipValue();
                                        }
                                    }

                                } else {
                                    jsonReader.skipValue();
                                }
                            }
                            // jsonReader.endObject();
                            break;
                        } else if (name.equals("error")){ // API Limit reached

                            to_return = "Error: se llegó al límite de la API :(";
                            jsonReader.close();
                            myConnection.disconnect();
                            return to_return;

                        }
                        jsonReader.skipValue();
                    }
                    // jsonReader.endObject();
                    jsonReader.close();
                    myConnection.disconnect();
                } else{
                    ;// Error handling code goes here
                }
            } catch(IOException e){
                ;
            }
            Log.d("UVRad", "Value to return: ");
            Log.d("UVRad", to_return);
            return to_return;
        }

        @Override
        protected void onPostExecute (String s){
            // dismiss the progress dialog after receiving data from API
            Log.d("UVRad", "Value: ");
            Log.d("UVRad", s);
            progressDialog.dismiss();
            UVIndex.setText(s);

            curUV.setText(currentUV);
            time_st1.setText(timeph1);
            time_st2.setText(timeph2);
            time_st3.setText(timeph3);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(ShowUvIndex.this, "MyNotification");

            // Basado en recomendaciones de la EPA Estadounidense
            if (doubleMaxUV >= 0.0) {
                if (doubleMaxUV <= 2) {
                    builder.setContentTitle("Condiciones tranquilas!");
                    builder.setContentText("Bien ahí por preocuparte! La recomendación que...");
                    builder.setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Bien ahí por preocuparte! La recomendación que te damos es que salgas sin mucho miedo, la exposición al sol es segura! :)"));
                } else if (doubleMaxUV <= 7) {
                    builder.setContentTitle("Un poco de cuidado");
                    builder.setContentText("Bien ahí por preocuparte! La recomendación que...");
                    builder.setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Bien ahí por preocuparte! La recomendación que te damos es que busques sombra y te apliques bloqueador con un FPS de al menos 15 :)"));
                } else if (doubleMaxUV <= 10) {
                    builder.setContentTitle("Cuidado!");
                    builder.setContentText("Bien ahí por preocuparte! La recomendación que...");
                    builder.setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Bien ahí por preocuparte! La recomendación que te damos es que busques sombra, uses gorro, lentes de sol y te apliques bloqueador con un FPS de al menos 15 :)"));
                } else { // 10+ DANGER ZONE
                    builder.setContentTitle("MUCHO CUIDADO!");
                    builder.setContentText("Bien ahí por preocuparte! La recomendación que...");
                    builder.setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Bien ahí por preocuparte! La recomendación que te damos es que evites la exposición al sol, busques sombra, uses gorro, lentes de sol y te apliques bloqueador con un FPS de al menos 30 :) Recuerda que cualquier valor por encima de 10 es EXTREMADAMENTE PELIGROSO."));
                }

                builder.setSmallIcon(R.drawable.meros_inges);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(ShowUvIndex.this);
                managerCompat.notify(420, builder.build());
            }

        }

    }
}
