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

/*** Το κύριο Activity της εφαρμογής, χειρίζεται την αναζήτηση καιρού (όνομα μέρους
    ή τρέχουσα τοποθεσία), δείχνει τα αποτελέσματα στην οθόνη, και εκκινεί το
    WeatherService που τρέχει στο παρασκήνιο ***/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etCity;    // To searchbar για το μέρος
    Button btnSrch;     // Το κουμπί για την αναήτηση μέρους
    Button btnLoc;      // Το κουμπί για αποστολή coords
    ProgressBar pbLoad; // Το progress bar (κύκλος φόρτωσης)
    TextView tvCityName;    // Πεδίο επίδειξης μέρους
    TextView tvTemp;    // Πεδίο επίδειξης θερμοκρασασίας
    TextView tvFeels;   // Πεδίο επίδειξης αίσθησης θερμοκρασασίας
    TextView tvDescr;   // Πεδίο επίδειξης καιρού
    TextView tvWind;    // Πεδίο επίδειξης ταχύτητας ανέμου
    LocationManager LocMan; // Χειριστής για την τοποθεσία
    ImageView ivBackground; // Εικόνα background

    weatherData data;

    //  Κλήση μεθόδου για εκκίνηση της εφαρμογής
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

    // Διαχείριση κλικ κουμμπιών btnSrch, btnLoc)
    @Override
    public void onClick(View v) {
        if (v == btnSrch) {
            String city = etCity.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                return;
            }

            pbLoad.setVisibility(View.VISIBLE);

            CityThread cityThread = new CityThread(this,city);
            cityThread.start();
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

            LocationThread locThread = new LocationThread(this);
            locThread.start();
        }
    }


    // Αναζήτηση με όνομα μέρους
    void SearchByCity(final String city) {
        // Για το WeatherService κρατάει το τελευταίο μέρος που έψαξες
        SaveLastCityQuery(city);
        Client client = new Client();
        data = client.getWeatherByCity(city,"metric");
    }

    // Αναζήτηση με όνομα με τοποθεσία (συντεταγμενες)
    void SearchByLocation() {

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


    // Μέθοδος για την CheckWeatherAlerts, περνάει το τελευταίο εισαχθέο μέρος
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

    // Μέθοδοςγια την CheckWeatherAlerts, περνάει την τελευταία τοποθεσία
    private void SaveLastLocationQuery(String lat, String lon) {
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_lat", lat);
        editor.putString("last_lon", lon);
        editor.remove("last_city");
        editor.apply();
    }

    // Μέθοδος που ζητάει από τον χρήστη άδεια για ειδοποιήσεις
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

        if (hour >= 6 && hour < 21) {
            ivBackground.setImageResource(R.drawable.sky_day);
        } else {
            ivBackground.setImageResource(R.drawable.sky_night);
        }
    }
}

// Thread που εκτελεί την μέθοδο λήψης δεδομένων βάση της πόλης και ενημερώνει τα πλαίσια της οθόνης με τα δεδομένα
class CityThread extends Thread{
    MainActivity mA;
    String city;

    CityThread(MainActivity mA, String city){
        this.mA = mA;
        this.city = city;
    }

    public void run(){
        mA.SearchByCity(city);

        // Εκτελεί την ενημέρωση πίσω στο main thread, καθώς ένα background thread δεν μπορεί να επηρεάσει τα πλαίσια
        mA.runOnUiThread(() -> {
            mA.pbLoad.setVisibility(View.GONE);
            if (mA.data != null) {
                mA.tvCityName.setText("City:\n" + mA.data.getCity());
                mA.tvTemp.setText("Temperature:\n" + mA.data.getTemperature());
                mA.tvFeels.setText("Feels Like:\n" + mA.data.getFeelsLike());
                mA.tvDescr.setText("Weather:\n" + mA.data.getDescription());
                mA.tvWind.setText("Wind Speed:\n" + mA.data.getWindSpeed());
            } else {
                Toast.makeText(mA, "Error fetching data!", Toast.LENGTH_SHORT).show();
            }
            mA.data = null;
        });
    }
}

class LocationThread extends Thread{
    MainActivity mA;

    LocationThread(MainActivity mA){
        this.mA = mA;
    }

    @Override
    public void run(){
        mA.SearchByLocation();

        // Εκτελεί την ενημέρωση πίσω στο main thread, καθώς ένα background thread δεν μπορεί να επηρεάσει τα πλαίσια
        mA.runOnUiThread(() -> {
            mA.pbLoad.setVisibility(View.GONE);
            if (mA.data != null) {
                mA.tvCityName.setText("City:\n" + mA.data.getCity());
                mA.tvTemp.setText("Temperature:\n" + mA.data.getTemperature());
                mA.tvFeels.setText("Feels Like:\n" + mA.data.getFeelsLike());
                mA.tvDescr.setText("Weather:\n" + mA.data.getDescription());
                mA.tvWind.setText("Wind Speed:\n" + mA.data.getWindSpeed());
            } else {
                Toast.makeText(mA, "Error fetching data!", Toast.LENGTH_SHORT).show();
            }
            mA.data = null;
        });
    }
}