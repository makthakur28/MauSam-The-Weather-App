package com.example.mausam;

import static android.view.WindowManager.LayoutParams.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.Permission;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements LocationListener {

    String url = "https://api.openweathermap.org/data/2.5/weather?q=";
    String API = "dde213bff216f4827cbe3f164e426680";

    EditText loc_input;
    TextView location1, daydate, desc, temp, humidity, wind_speed, feels_like, pressure;
    Button button;
    RequestQueue requestQueue;
    LocationManager locationManager;
    private String provider;
    LinearLayout layout;
    Location location;

    Random r = new Random();

    int[] array = new int[]{R.drawable.bg1, R.drawable.bg2, R.drawable.bg3, R.drawable.bg4, R.drawable.bg5};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        request();


        layout = (LinearLayout) findViewById(R.id.layout);
        location1 = findViewById(R.id.location);
        button = findViewById(R.id.button);
        temp = findViewById(R.id.temperature3);
        desc = findViewById(R.id.weather_desc3);
        pressure = findViewById(R.id.pressure);
        wind_speed = findViewById(R.id.wind_speed);
        humidity = findViewById(R.id.humidity);
        daydate = findViewById(R.id.daydate);
        feels_like = findViewById(R.id.feels_like);
        loc_input = findViewById(R.id.loc_input);

        int i = r.nextInt(array.length);
        layout.setBackgroundResource(array[i]);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);



        //currLoc();
//        if (location != null) {
//            System.out.println("Provider " + provider + " has been selected.");
//            onLocationChanged(location);
//        } else {
//
//            location1.setText("Location not available");
//        }

        // set date and day
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("EEEE dd-MMMM-yyyy h:mm a", Locale.getDefault());
        String d = dateFormat.format(date);
        daydate.setText(d);

        //format for Temprature
        DecimalFormat df = new DecimalFormat("#.#");

        //JSON prasing
        requestQueue = Volley.newRequestQueue(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loc = loc_input.getText().toString();
                if (loc.equals("")) {
                    currLoc();
                } else {
                    location1.setText(loc);
                    getApiDetails(loc);
                }
            }
        });
    }

    private void request() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                provider = locationManager.getBestProvider(criteria, false);
                currLoc();
            } else {
                Toast.makeText(MainActivity.this, "cant fetch", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "cant fetch", Toast.LENGTH_SHORT).show();
        }
    }

    private void currLoc() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        } else {
            location1.setText("Location not available");
        }

    }

    private void getApiDetails(String loc) {

        String complete_url = url + loc + "&appid=" + API;
        location1.setText(loc);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, complete_url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = response.getJSONObject("main");
                    double kelTemp = Double.parseDouble(obj.getString("temp")) - 273.15;
                    String degTemp = Double.toString(kelTemp);
                    temp.setText(String.format("%.2f", kelTemp) + "°c");
                    kelTemp = Double.parseDouble(obj.getString("feels_like")) - 273.15;
                    degTemp = Double.toString(kelTemp);
                    feels_like.setText(String.format("%.2f", kelTemp) + "°c");
                    pressure.setText(obj.getString("pressure") + " atm");
                    humidity.setText(obj.getString("humidity") + "%");
                    JSONObject obj2 = response.getJSONObject("wind");
                    wind_speed.setText(obj2.getString("speed") + " kph");
                    JSONArray obj3 = response.getJSONArray("weather");
                    JSONObject ob31 = obj3.getJSONObject(0);
                    desc.setText(ob31.getString("description"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Something went Wrong..!!", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        MainActivity.this.getWindow().setBackgroundDrawableResource(array[i]);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 4000, 1000, this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> address = geoCoder.getFromLocation(lat, lng, 1);

            String city = address.get(0).getLocality();

            getApiDetails(city);

        } catch (IOException e) {
            // Handle IOException
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }
}