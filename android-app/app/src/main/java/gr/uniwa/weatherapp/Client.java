package gr.uniwa.weatherapp;

import java.net.*;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;

public class Client {
    private static String apiKey;

    public Client() {
        // Load API key
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config")) {
            properties.load(input);
            apiKey = properties.getProperty("api.key");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        String resp = client.getWeatherByCity("London", "metric", false);
        System.out.println(resp);
        resp = client.getWeatherByCoords("51.5074", "-0.1278", "metric", false);
        System.out.println("\n\n" + resp);
    }

    public String getWeatherByCity(String city, String unit, Boolean isAlert) {
        try {
            String apiURI = "https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+apiKey+"&units="+unit;
            URI uri = new URI(apiURI);
            HttpURLConnection urlConnection = (HttpURLConnection) uri.toURL().openConnection();
            InputStream inp = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inp));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            urlConnection.disconnect();

            return response.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getWeatherByCoords(String lat, String lon, String unit, Boolean isAlert) {
        try {
            String apiURI = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid="+apiKey+"&units="+unit;
            URI uri = new URI(apiURI);
            HttpURLConnection urlConnection = (HttpURLConnection) uri.toURL().openConnection();
            InputStream inp = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inp));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            urlConnection.disconnect();

            return response.toString();
        } catch (Exception ex) {
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
