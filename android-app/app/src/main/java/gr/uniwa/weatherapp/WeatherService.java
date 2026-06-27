package gr.uniwa.weatherapp;

import android.app.Notification;
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

/*** Service που τρέχει στο παρασκήνιο και ελέγχει τον καιρό αναλόγως με το μέρος
    που έχει σταλθεί τελευταίο στο MainActivity για να βγει notification αν υπάρχει alert ***/
public class WeatherService extends Service {

    private Timer timer = null;
    private TimerTask timerTask = null;
    // private static final int INTERVAL_MS = 10000; // 10s για test
    private static final int INTERVAL_MS = 900000; // Τόσα ms == 15min
    private static final String CHANNEL_ID = "weather_alerts_channel";  // ID για το κανάλι alerts
    private static final int TOAST_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 42;
    private static final String STATUS_CHANNEL_ID = "weather_status_channel"; // ID για το κανάλι persistent notifications

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Ξεκινάει τον έλεγχο στο παρασκήνιο (αν δεν έχει ήδη αρχίσει)
    public void StartInterval() {
        if (timer != null)
            return;
        timerTask = new TimerTask() {
            public void run() {
                CheckWeatherAlerts();  // Καλεί την μέθοδο για έλεγχο για αλερτ
            }
        };
        timer = new Timer();
        // Ξεκινάει κατευθείαν (0 delay) κάθε 15 λεπτά (σταθερά ορισμένη πάνω)
        timer.schedule(timerTask, 0, INTERVAL_MS);
    }

    // Σταματάει τον έλεγχο και επαναφέρει το timer σε null
    public void StopInterval() {
        if (timer != null) {
            timer.cancel(); // Σταματάει
            timer.purge();  // Καθαρίζει τον timerTask
            timer = null;  // Επαναφορά τιμής
        }
    }

    @Override
    public void onCreate () {
        super.onCreate();
        CreateNotificationChannel();    // Δημιουργεί τα κανάλια
        StartForegroundNotification();  // Εκκινεί να τρέχει στο παρασκήνιο το service
        StartInterval();    // Εκκινεί την μέθοδο περιόδου 15 λεπτών
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        return START_STICKY; // Για να ξανακάνει restart service όταν γίνει kill service
    }

    // Κλείσιμο κύκλου ζωής του service
    @Override
    public void onDestroy() {
        super.onDestroy();
        StopInterval();
    }

    // Έλεγχος ακραίων καιρικών συνθηκών
    private void CheckWeatherAlerts() {
        // Παίρνει πρόσβαση στο αρχείο WeatherPrefs (βλπ Save μεθόδους στο MainActivity)
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        // Παίρνει τις αποθηκευμένες τιμές από το αρχείο, αλλιώς null αν δεν υπάρχουν
        String lastCity = prefs.getString("last_city", null);
        String lastLat = prefs.getString("last_lat", null);
        String lastLon = prefs.getString("last_lon", null);

        boolean hasCity = lastCity != null && !lastCity.isEmpty();
        boolean hasCoords = lastLat != null && lastLon != null;

        if (!hasCity && !hasCoords)
            return;

        Client client = new Client ();
        // getAlerts πρώτα Lon, μετά Lat
        alertData alert = client.getAlerts(lastCity, lastLon, lastLat);

        if (alert.getAlert())
            ShowToast(alert);
    }

    // Βγάζει ειδοποίηση αναλόγως του ακραίου καιρικού φαινομένου
    private void ShowToast(alertData alert) {
        // Καλύτερο από concat, μόνο 1 object
        StringBuilder message = new StringBuilder();
        if (alert.getHighTemp()) message.append("High temperature!\n");
        if (alert.getLowTemp()) message.append("Low temperature!\n");
        if (alert.getHighWind()) message.append("High wind speed!\n");
        if (alert.getRain()) message.append("Rain!\n");
        if (alert.getSnow()) message.append("Snow!\n");
        if (alert.getThunderstorm()) message.append("Thunderstorm!\n");

        // Ορισμός για το ποίο activity θα ανοίξει το intent
        Intent contentIntent = new Intent(this, MainActivity.class);
        // To Intent ενθυλακώνεται από το PendingIntent θα περιμένει να εκτελεστεί μέχρι να το πατήσει ο χρήστης
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE);

        // Χτίσιμο του notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Weather Alert")
                .setContentText(message.toString().trim())
                // Δυνατότητα expand του notification αν είναι πάνω από μία γραμμή
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString().trim()))
                // Στον χρήστη έρχεται ειδοποίηση, δονείται το κινητό και βγάζει και ήχο
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Όταν πατήσει το notification ο χρήστης, ανοίγει το MainActivity
                .setContentIntent(pendingIntent)
                // Όταν πατήσει το notification ο χρήστης, κάνει dismiss
                .setAutoCancel(true);

        // Ζητάει από το σύστημα την υπηρεσία διαχείρισης ειδοποιήσεων
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Μετατροπή ρυθμίσεων builder σε notification object και εμφάνιση ειδοποίησης στον χρήστη
        notificationManager.notify(TOAST_ID, builder.build());
    }

    /*** https://developer.android.com/develop/ui/compose/notifications/channels ***/
    // Δημιουργία καναλιού για τις ειδοποιήσεις
    private void CreateNotificationChannel() {
        // Για Android από Οreo (ΑΡΙ 26) και μετά
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Κανάλι για τα alerts
            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Weather Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            alertChannel.setDescription("Notifications for severe weather conditions");

            // Κανάλι για τα service
            NotificationChannel statusChannel = new NotificationChannel(
                    STATUS_CHANNEL_ID, "Service Status", NotificationManager.IMPORTANCE_LOW);
            statusChannel.setDescription("Indicates the weather monitoring service is active");

            NotificationManager manager = getSystemService(NotificationManager.class);
            // Καταχώριση καναλιών στο σύστημα
            manager.createNotificationChannel(alertChannel);
            manager.createNotificationChannel(statusChannel);
        }
    }

    /*** https://developer.android.com/develop/background-work/services/fgs/launch ***/
    // Δημιουργία notification ενημέρωσης ότι το service ρέχει στο παρασκήνιο
    private void StartForegroundNotification() {
        // Δημιουργία notification αντικειμένου
        Notification notification = new NotificationCompat.Builder(this, STATUS_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Weather Alerts Active")
                .setContentText("Monitoring weather conditions in the background")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        // Διαφορετικό ID για αυτό το notification
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

}
