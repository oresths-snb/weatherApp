package gr.uniwa.weatherapp;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.net.*;
import java.util.Properties;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;

public class Client {
    private static final String apiKey = BuildConfig.API_KEY;
    public Client() {
    }

    public weatherData getWeatherByCity(String city, String unit){
        if (city.contains(" ")){
            String[] parts = city.split(" ");
            StringBuilder cityBuilder = new StringBuilder();

            for (String part : parts) {
                cityBuilder.append(part).append("+");
            }
            city = cityBuilder.toString();
            city = city.substring(0, city.length() - 1);
        }

        String apiURI = "https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+apiKey+"&units="+unit;
        Log.d(TAG, "getWeatherByCity: "+apiKey);
        return fetchAPIData(apiURI);
    }

    public weatherData getWeatherByCoords(String lat, String lon, String unit) {
        String apiURI = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid="+apiKey+"&units="+unit;
        return fetchAPIData(apiURI);
    }

    weatherData fetchAPIData(String API_URI){
        try {
            URI uri = new URI(API_URI);
            HttpURLConnection urlConnection = (HttpURLConnection) uri.toURL().openConnection();
            InputStream inp = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inp));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            inp.close();
            reader.close();
            urlConnection.disconnect();

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            double temperature = json.getAsJsonObject("main").get("temp").getAsDouble();
            String description = json.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString();
            String cityJson = json.getAsJsonObject().get("name").getAsString();
            double feelsLike = json.getAsJsonObject("main").get("feels_like").getAsDouble();
            double windSpeed = json.getAsJsonObject("wind").get("speed").getAsDouble();

            Log.d(TAG, "fetchAPIData:"+cityJson+description+feelsLike+description+windSpeed);

            return new weatherData(cityJson, temperature, description, windSpeed, feelsLike);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public alertData getAlerts(String city,String lon,String lat){
        alertData alert = new alertData();
        weatherData data = null;
        if (city != null && !city.isEmpty()){
            data = getWeatherByCity(city, "metric");
        } else if (lon != null && lat != null && !lon.isEmpty() && !lat.isEmpty()){
            data = getWeatherByCoords(lat, lon, "metric");
        } else {
            System.out.println("Error fetching weather data");
        }
        if (data != null) {
            double temp = data.getTemperature();
            double windSpeed = data.getWindSpeed();
            String description = data.getDescription();
            if (temp > 30) {
                alert.setAlert(true);
                alert.setHighTemp(true);
            } else if (temp < 10) {
                alert.setAlert(true);
                alert.setLowTemp(true);
            }

            if (windSpeed > 20) {
                alert.setAlert(true);
                alert.setHighWind(true);
            }

            if (description.contains("rain")) {
                alert.setAlert(true);
                alert.setRain(true);
            }

            if (description.contains("snow")) {
                alert.setAlert(true);
                alert.setSnow(true);
            }

            if (description.contains("thunderstorm")) {
                alert.setAlert(true);
                alert.setThunderstorm(true);
            }

        }
        return alert;
    }

}
