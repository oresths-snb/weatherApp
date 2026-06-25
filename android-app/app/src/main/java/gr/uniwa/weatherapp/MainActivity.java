package gr.uniwa.weatherapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
    TextView tvAlerts;
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
        tvAlerts = findViewById(R.id.tvAlerts);
        LocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ivBackground = findViewById(R.id.ivBackground);
		
		SetBackgroundByTimeOfDay();

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
                        tvWind.setText("Wind Speed:\n"+data.getWindSpeed());
                    } else{
                        Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }

        if (v == btnLoc)
            UseCurrentLocation();
    }

    private void SearchByCity(final String city) {
        // TODO: call WeatherApiClient + ServerConnection here
        Client client = new Client();
        data = client.getWeatherByCity(city,"metric");
    }

    private void UseCurrentLocation() {
       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        pbLoad.setVisibility(View.VISIBLE);
        LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 2.5f, this); */
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