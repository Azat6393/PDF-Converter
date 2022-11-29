package woynapp.wsann.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import woynapp.wsann.R;
import woynapp.wsann.activity.AuthActivity;
import woynapp.wsann.activity.NewMainActivity;
import woynapp.wsann.activity.SplashActivity;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManager notificationManager;
    private NotificationChannel notificationChannel;
    private Notification.Builder builder;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = message.getData().get("data_title");
        String text = message.getData().get("data_text");

        String CHANNEL_ID = "W_SCANN_NOTIFICATION";
        Intent intent = new Intent(this, SplashActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        notificationManager = (NotificationManager) (getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "W Scann notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);

            builder = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logoo)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent);
        }else {
            builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.logoo)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent);
        }
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
}
