package gr.uniwa.weatherapp;
public class alertData{
    boolean alert;
    boolean highTemp;
    boolean lowTemp;
    boolean highWind;
    boolean rain;
    boolean snow;
    boolean thunderstorm;

    public alertData() {
        this.alert = false;
        this.highTemp = false;
        this.lowTemp = false;
        this.highWind = false;
        this.rain = false;
        this.snow = false;
        this.thunderstorm = false;
    }

    public boolean getAlert() {
        return alert;
    }
    public boolean getHighTemp() {
        return highTemp;
    }
    public boolean getLowTemp() {
        return lowTemp;
    }
    public boolean getHighWind() {
        return highWind;
    }
    public boolean getRain() {
        return rain;
    }
    public boolean getSnow() {
        return snow;
    }
    public boolean getThunderstorm() {
        return thunderstorm;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }
    public void setHighTemp(boolean highTemp) {
        this.highTemp = highTemp;
    }
    public void setLowTemp(boolean lowTemp) {
        this.lowTemp = lowTemp;
    }
    public void setHighWind(boolean highWind) {
        this.highWind = highWind;
    }
    public void setRain(boolean rain) {
        this.rain = rain;
    }
    public void setSnow(boolean snow) {
        this.snow = snow;
    }
    public void setThunderstorm(boolean thunderstorm) {
        this.thunderstorm = thunderstorm;
    }
}