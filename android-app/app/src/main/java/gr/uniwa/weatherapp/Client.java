/*

    ΜΕΛΗ ΟΜΆΔΑΣ:
    ΟΡΕΣΤΗΣ ΚΟΥΤΣΙΑΣ (ΑΜ: 21390106)
    ΝΕΒΕΝΑ ΑΝΔΡΙΤΣΟΥ (ΑΜ : 19390016)

 */

package gr.uniwa.weatherapp;

import java.net.*;
import java.io.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


// Κλάση Client διαχειρίζεται όλη την επικοινωνία με το API και τη λήψη/διαχείριση δεδομένων απο αυτό
// Πιο σωστό θα ήταν ο client να συνδέεται σε server που εκτελεί τις κλήσεις του API σε αυτή τη περίπτωση το κάνει ο ίδιος ο client
public class Client {
    private static final String apiKey = "f94f27c56e46f6b756ab77b2a3f72d41"; // κανονικά αυτό δεν επιτρέπεται
    public Client() {
    }

    // Κλήση API με όνομα πόλης/περιοχής ως παράμετρο αναζήτησης
    public weatherData getWeatherByCity(String city, String unit){
        // Σε περίπτωση που η πόλη/περιοχή έχει κενά την φορμάρει για σωστή χρήση του URI (πχ. New York City -> New+York+City
        if (city.contains(" ")){
            String[] parts = city.split(" ");
            StringBuilder cityBuilder = new StringBuilder();

            for (String part : parts) {
                cityBuilder.append(part).append("+");
            }
            city = cityBuilder.toString();
            city = city.substring(0, city.length() - 1);
        }

        // Χτίζει το URI για την κλήση του API και το καλεί επιστρέφοντας τα αποτελέσματα
        String apiURI = "https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+apiKey+"&units="+unit;
        return fetchAPIData(apiURI);
    }

    // Κλήση API με συντεταγμένες απο GPS κινητού ως παράμετρο αναζήτησης
    public weatherData getWeatherByCoords(String lat, String lon, String unit) {
        // Χτίζει το URI για την κλήση του API και το καλεί επιστρέφοντας τα αποτελέσματα
        String apiURI = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid="+apiKey+"&units="+unit;
        return fetchAPIData(apiURI);
    }

    weatherData fetchAPIData(String API_URI){
        try {
            // Λαμβάνει το URI και εκτελεί HTTP request στο API endpoint του OpenWeather
            URI uri = new URI(API_URI);
            HttpURLConnection urlConnection = (HttpURLConnection) uri.toURL().openConnection();

            // Λαμβάνει την απάντηση του HTTP request από το OpenWeather
            InputStream inp = urlConnection.getInputStream();

            // Διαβάζει την απάντηση γραμμή/γραμμή (σε string) που είναι σε μορφή json
            BufferedReader reader = new BufferedReader(new InputStreamReader(inp));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            inp.close();
            reader.close();
            urlConnection.disconnect();

            // Διαχείριση json για εξαγωγή των δεδομένων που μας ενδιαφέρουν σε μορφή μεταβλητών
            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();

            double temperature = json.getAsJsonObject("main").get("temp").getAsDouble();
            String description = json.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString();
            String cityJson = json.getAsJsonObject().get("name").getAsString();
            double feelsLike = json.getAsJsonObject("main").get("feels_like").getAsDouble();
            double windSpeed = json.getAsJsonObject("wind").get("speed").getAsDouble();

            // Επιστρέφει κλάση weatherData που λειτουργεί σαν struct με όλα τα δεδομένα που εξάχθηκαν σαν μεταβλητές της
            return new weatherData(cityJson, temperature, description, windSpeed, feelsLike);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    // Εκτελεί μία απο τις παραπάνω μεθόδους παραλαβής δεδομένων και ελέγχει τα δεδομένα της για ακραίες καιρικές συνθήκες
    public alertData getAlerts(String city,String lon,String lat){
        // Struct με boolean τιμές που λαμβάνουν το αποτέλεσμα των ελέγχων για κάθε καιρική συνθήκη
        alertData alert = new alertData();
        weatherData data = null;

        // Αν έχει ληφθεί όνομα πόλης (προϋποθέτει να μην έχουν ληφθεί συντεταγμένες) εκτελεί getWeatherByCity
        // Αν το αντίστροφο εκτελεί getWeatherByCoords
        if (city != null && !city.isEmpty()){
            data = getWeatherByCity(city, "metric");
        } else if (lon != null && lat != null && !lon.isEmpty() && !lat.isEmpty()){
            data = getWeatherByCoords(lat, lon, "metric");
        } else {
            System.out.println("Error fetching weather data");
        }

        // Αν λήφθηκε ορθό αποτέλεσμα εκτελεί τους ελέγχους
        if (data != null) {
            double temp = data.getTemperature();
            double windSpeed = data.getWindSpeed();
            String description = data.getDescription();

            // Υψηλή/χαμηλή θερμοκρασία
            if (temp > 30) {
                alert.setAlert(true);
                alert.setHighTemp(true);
            } else if (temp < 10) {
                alert.setAlert(true);
                alert.setLowTemp(true);
            }

            // Υψηλοί άνεμοι
            if (windSpeed > 20) {
                alert.setAlert(true);
                alert.setHighWind(true);
            }

            // Βροχή
            if (description.contains("rain")) {
                alert.setAlert(true);
                alert.setRain(true);
            }

            // Χιόνι
            if (description.contains("snow")) {
                alert.setAlert(true);
                alert.setSnow(true);
            }

            // Καταιγίδα
            if (description.contains("thunderstorm")) {
                alert.setAlert(true);
                alert.setThunderstorm(true);
            }

        }
        return alert;
    }

}
