/*

    ΜΕΛΗ ΟΜΆΔΑΣ:
    ΟΡΕΣΤΗΣ ΚΟΥΤΣΙΑΣ (ΑΜ: 21390106)
    ΝΕΒΕΝΑ ΑΝΔΡΙΤΣΟΥ (ΑΜ : 19390016)

 */

package gr.uniwa.weatherapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

// vgale to location listener
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etCity;
    Button btnSrch;
    Button btnLoc;
    ProgressBar pbLoad;
    TextView tvCityName;
    TextView tvTemp;
    TextView tvFeels;
    TextView tvDescr;
    TextView tvWind;
    // TextView tvAlerts; instead of this toast
    LocationManager LocMan;
    ImageView ivBackground;

    weatherData data;


    /*
    //Grant permission for UseLocation()
    // https://developer.android.com/develop/sensors-and-location/location/permissions/runtime#java
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    new ActivityResultCallback<Boolean>() {
                        @Override
                        public void onActivityResult(Boolean isGranted) {
                            if (isGranted) {
                                UseCurrentLocation();
                            } else {
                                Toast.makeText(MainActivity.this, "Location permission is required", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCity = findViewById(R.id.etCity);
        btnSrch = findViewById(R.id.btnSrch);
        btnLoc = findViewById(R.id.btnLoc);
        pbLoad = findViewById(R.id.pbLoad);
        tvCityName = findViewById(R.id.tvCityName);
        tvTemp = findViewById(R.id.tvTemp);
        tvFeels = findViewById(R.id.tvFeels);
        tvDescr = findViewById(R.id.tvDescr);
        tvWind = findViewById(R.id.tvWind);
        // tvAlerts = findViewById(R.id.tvAlerts); instead of this toasts
        LocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ivBackground = findViewById(R.id.ivBackground);

        SetBackgroundByTimeOfDay();

        RequestNotificationPermissionIfNeeded();
        startService(new Intent(this, WeatherService.class));

        btnSrch.setOnClickListener(this);
        btnLoc.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnSrch) {
            String city = etCity.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                return;
            }

            pbLoad.setVisibility(View.VISIBLE);

            new Thread(() -> {
                SearchByCity(city);

                runOnUiThread(() -> {
                    pbLoad.setVisibility(View.GONE);
                    if (data != null){
                        tvCityName.setText("City:\n"+data.getCity());
                        tvTemp.setText("Temperature:\n"+data.getTemperature());
                        tvFeels.setText("Feels Like:\n"+data.getFeelsLike());
                        tvDescr.setText("Weather:\n" + data.getDescription());
                        tvWind.setText("Wind Speed:\n"+data.getWindSpeed());
                    } else{
                        Toast.makeText(this, "Error fetching data!", Toast.LENGTH_SHORT).show();
                    }
                    data = null;
                });
            }).start();
        }

        if (v == btnLoc) {

            // Gia na trexei sto main thread kai oxi sto background, otan patietai to btnLoc zhtaei permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }

            pbLoad.setVisibility(View.VISIBLE);

            new Thread(() -> {
                SearchByLocation();

                runOnUiThread(() -> {
                    pbLoad.setVisibility(View.GONE);
                    if (data != null) {
                        tvCityName.setText("City:\n" + data.getCity());
                        tvTemp.setText("Temperature:\n" + data.getTemperature());
                        tvFeels.setText("Feels Like:\n" + data.getFeelsLike());
                        tvDescr.setText("Weather:\n" + data.getDescription());
                        tvWind.setText("Wind Speed:\n" + data.getWindSpeed());
                    } else {
                        Toast.makeText(this, "Error fetching data!", Toast.LENGTH_SHORT).show();
                    }
                    data = null;
                });
            }).start();
        }
    }

    private void SearchByCity(final String city) {
        SaveLastCityQuery(city); // Gia to WeatherService krataei to teleutaio meros pou epsakses
        Client client = new Client();
        data = client.getWeatherByCity(city,"metric");
    }

    private void SearchByLocation() {

        /*** Initialized on create
         LocationManager LocMan = (LocationManager) getSystemService (Context.LOCATION_SERVICE); ***/
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        String provider = LocMan.getBestProvider(criteria,true);

        if (provider != null){
            /*** Auto pou to evgale to IDE gia to LocMan, an exoume xrono to vlepoume
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
             ***/
            Location location = LocMan.getLastKnownLocation(provider);

            if (location != null){
                String lat = location.getLatitude() + "";
                String lon = location.getLongitude() + "";
                SaveLastLocationQuery(lat, lon); // Gia to WeatherService krataei to teleutaio meros pou hsoun

                Client client = new Client();
                data = client.getWeatherByCoords(lat,lon,"metric");
            }
        }
    }


    /*** https://developer.android.com/training/data-storage/shared-preferences#java ***/
    private void SaveLastCityQuery(String city) {
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_city", city);
        editor.remove("last_lat");
        editor.remove("last_lon");
        editor.apply();
    }

    private void SaveLastLocationQuery(String lat, String lon) {
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_lat", lat);
        editor.putString("last_lon", lon);
        editor.remove("last_city");
        editor.apply();
    }

    /*** https://developer.android.com/develop/ui/compose/notifications/notification-permission ***/
    private void RequestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
            }
        }
    }

    private void SetBackgroundByTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour < 20) {
            ivBackground.setImageResource(R.drawable.sky_day);
        } else {
            ivBackground.setImageResource(R.drawable.sky_night);
        }
    }

    /*
    // For LocationListener based on IDE suggestions + Locations gmele, need to double check/alter
    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    } */
}