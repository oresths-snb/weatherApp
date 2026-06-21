package gr.uniwa.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etCity;
    Button btnSrch;
    Button btnLoc;
    ProgressBar pbLoad;
    TextView tvCityName;
    TextView tvTemp;
    TextView tvDescr;
    TextView tvAlerts;

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
        tvDescr = findViewById(R.id.tvDescr);
        tvAlerts = findViewById(R.id.tvAlerts);

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
            SearchByCity(city);
        }

        if (v == btnLoc)
            UseCurrentLocation();
    }

    private void SearchByCity(String city) {
        // TODO: call WeatherApiClient + ServerConnection here
        pbLoad.setVisibility(View.VISIBLE);
    }

    private void UseCurrentLocation() {
        // TODO: call LocationHelper here
        pbLoad.setVisibility(View.VISIBLE);
    }
}