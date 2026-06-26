package gr.uniwa.weatherapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class WeatherService extends Service {

    private Timer timer = null;
    private TimerTask timerTask = null;
    //private static final int INTERVAL_MS = 30000; // 30s for test
    private static final int INTERVAL_MS = 900000; // tosa ms == 15min
    private static final String CHANNEL_ID = "weather_alerts_channel";
    private static final int TOAST_ID = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void StartInterval() {
        if (timer != null)
            return;
        timerTask = new TimerTask() {
            public void run() {
                CheckWeatherAlerts();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, INTERVAL_MS);
    }

    public void StopInterval() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Override
    public void onCreate () {
        StartInterval();
        super.onCreate();
        CreateNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startID) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StopInterval();
    }

    private void CheckWeatherAlerts() {
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        String lastCity = prefs.getString("last_city", null);
        String lastLat = prefs.getString("last_lat", null);
        String lastLon = prefs.getString("last_lon", null);

        boolean hasCity = lastCity != null && !lastCity.isEmpty();
        boolean hasCoords = lastLat != null && lastLon != null;

        if (!hasCity && !hasCoords)
            return;

        Client client = new Client ();
        // getAlerts prwta Lon meta Lat
        alertData alert = client.getAlerts(lastCity, lastLon, lastLat);

        if (alert.getAlert())
            ShowToast(alert);
    }

    private void ShowToast(alertData alert) {
        // Kalytero apo concat, mono 1 object
        StringBuilder message = new StringBuilder();
        if (alert.getHighTemp()) message.append("High temperature!\n");
        if (alert.getLowTemp()) message.append("Low temperature!\n");
        if (alert.getHighWind()) message.append("High wind speed!\n");
        if (alert.getRain()) message.append("Rain!\n");
        if (alert.getSnow()) message.append("Snow!\n");
        if (alert.getThunderstorm()) message.append("Thunderstorm!\n");

        Intent contentIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Weather Alert")
                .setContentText(message.toString().trim())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString().trim()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(TOAST_ID, builder.build());
    }

    /*** https://developer.android.com/develop/ui/compose/notifications/channels ***/
    private void CreateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Weather Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for severe weather conditions");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

}
