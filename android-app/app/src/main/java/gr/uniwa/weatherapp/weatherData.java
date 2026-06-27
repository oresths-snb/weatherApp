/*

    ΜΕΛΗ ΟΜΆΔΑΣ:
    ΟΡΕΣΤΗΣ ΚΟΥΤΣΙΑΣ (ΑΜ: 21390106)
    ΝΕΒΕΝΑ ΑΝΔΡΙΤΣΟΥ (ΑΜ : 19390016)

 */

// Βοηθητική κλάση για την διαχείριση και μεταφορά δεδομένων μεταξύ της κλάσης client και το βασικό πρόγραμμα
// Περιέχει μεταβλητές με τα δεδομένα που μας ενδιαφέρουν απο την απάντηση του OpenWeather API
package gr.uniwa.weatherapp;

public class weatherData {
    String city;
    double temperature;
    String description;
    double feelsLike;
    double windSpeed;

    public weatherData(String city, double temperature, String description, double windSpeed, double feelsLike) {
        this.city = city;
        this.temperature = temperature;
        this.description = description;
        this.windSpeed = windSpeed;
        this.feelsLike = feelsLike;
    }

    public String getCity() {
        return city;
    }
    public double getTemperature() { return temperature; }
    public String getDescription() { return description;
    }
    public double getFeelsLike() { return feelsLike; }
    public double getWindSpeed() { return windSpeed; }

}
