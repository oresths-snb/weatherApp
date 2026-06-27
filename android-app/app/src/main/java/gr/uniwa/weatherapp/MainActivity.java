/*

    ΜΕΛΗ ΟΜΆΔΑΣ:
    ΟΡΕΣΤΗΣ ΚΟΥΤΣΙΑΣ (ΑΜ: 21390106)
    ΝΕΒΕΝΑ ΑΝΔΡΙΤΣΟΥ (ΑΜ : 19390016)

 */

package gr.uniwa.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar; // Βιβλιοθήκη που χρειάζεται για να αλλάζει το ackground αναλόγως της ώρας


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etCity;  // To searchbar για το μέρος
    Button btnSrch;  // Το κουμπί για την αναήτηση μέρους
    Button btnLoc;  //
    ProgressBar pbLoad;
    TextView tvCityName;
    TextView tvTemp;
    TextView tvFeels;
    TextView tvDescr;
    TextView tvWind;
    LocationManager LocMan;
    ImageView ivBackground;

    weatherData data;


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
        LocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ivBackground = findViewById(R.id.ivBackground);

        SetBackgroundByTimeOfDay();

        RequestNotificationPermissionIfNeeded();
        // κλήση για να ξεκινήσει το WeatherService
        ContextCompat.startForegroundService(this, new Intent(this, WeatherService.class));

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

            // Oταν πατιέται το btnLoc ζητάει permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }

            // Όταν έχει ήδη αναζητηθεί κάποιο μέρος και πατηθεί να στείλεις location το search bar κάνει clear
            etCity.setText("");
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
        // Για το WeatherService κρατάει το τελευταίο μέρος που έψαξες
        SaveLastCityQuery(city);
        Client client = new Client();
        data = client.getWeatherByCity(city,"metric");
    }

    private void SearchByLocation() {

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        String provider = LocMan.getBestProvider(criteria,true);

        if (provider != null){

            Location location = LocMan.getLastKnownLocation(provider);

            if (location != null){
                String lat = location.getLatitude() + "";
                String lon = location.getLongitude() + "";
                // Για το WeatherService κρατάει το τελευταίο μέρος που ήσουν
                SaveLastLocationQuery(lat, lon);

                Client client = new Client();
                data = client.getWeatherByCoords(lat,lon,"metric");
            }
        }
    }


    // Μέθοδος
    private void SaveLastCityQuery(String city) {
        // Μέθοδος του context για nα δημιουργεί ένα αρχείο
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        // Κλάση του SharedPreferences, δημιουργούμε το αντικείμενο object για χειρισμό του αρχείου
        SharedPreferences.Editor editor = prefs.edit();
        // Βάζει την τιμή που αναζητήηκε στο κλειδί "last_city" (είναι ζεύγος)
        editor.putString("last_city", city);
        // Σβήνει τα coords
        editor.remove("last_lat");
        editor.remove("last_lon");
        // Αποθηκεύει τις αλλαγές στο αρχείο
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

    //
    private void RequestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
            }
        }
    }


    // Ανάλογα με την ώρα του κινητού αλλάζει background εικόνα
    private void SetBackgroundByTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour < 20) {
            ivBackground.setImageResource(R.drawable.sky_day);
        } else {
            ivBackground.setImageResource(R.drawable.sky_night);
        }
    }
}