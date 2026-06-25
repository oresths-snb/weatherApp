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

    public weatherData getWeatherByCity(String city, String unit) {
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

    /*public void getAlerts(String msg){
        String response = null;
        try {
             if (msg.startsWith("CITY:")) {
                String city = msg.substring(5);
                response = getWeatherByCity(city, "metric",true);
            } else if (msg.startsWith("COORDINATES:")){
                String[] coords = msg.substring(12).split(",");
                response = getWeatherByCoords(coords[0], coords[1], "metric",true);
            }
            if (response != null) {
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                double temp = json.getAsJsonObject("main").get("temp").getAsDouble();
                String description = json.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString();
                double windSpeed = json.getAsJsonObject("wind").get("speed").getAsDouble();

                if (temp > 30) {
                    out.println("ALERT: High temperature!");
                } else if (temp < 10) {
                    out.println("ALERT: Low temperature!");
                }

                if (description.toLowerCase().contains("thunderstorm")) {
                    out.println("ALERT: Thunderstorm!");
                } else if (description.toLowerCase().contains("rain")) {
                    out.println("ALERT: Rain!");
                }

                if (windSpeed > 20) {
                    out.println("ALERT: High wind speed!");
                }

                if (temp <= 30 && temp >= 10 && !description.toLowerCase().contains("thunderstorm") && !description.toLowerCase().contains("rain") && windSpeed <= 20)
                    out.println("No alerts");

                out.println("");
                out.flush();
            } else{
                out.println("No weather data available");
                out.println("");
                out.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

}
