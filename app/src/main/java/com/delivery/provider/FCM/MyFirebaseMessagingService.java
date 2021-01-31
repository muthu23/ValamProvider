package com.delivery.provider.FCM;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.delivery.provider.Activity.MainActivity;
import com.delivery.provider.Activity.SplashScreen;
import com.delivery.provider.Helper.SharedHelper;
import com.delivery.provider.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    public static final String INTENT_FILTER = "INTENT_FILTER";
    Uri sound = Settings.System.DEFAULT_NOTIFICATION_URI;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("DEVICE_ID: ", deviceId);
        Log.d("FCM_TOKEN", s);
        SharedHelper.putKey(this, "device_token", s);
        SharedHelper.putKey(this, "device_id", deviceId);
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message: " + remoteMessage.toString());
        String message = remoteMessage.getData().get("message");

        if (!isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Log.d( TAG, "foreground");
        } else {
            wakeUpScreen();
            Log.d(TAG, "background");
            if (message.equalsIgnoreCase("New Incoming Ride")) {
                KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("IN");
                kl.disableKeyguard();
                Intent mainIntent = new Intent(this, SplashScreen.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(mainIntent);
            }
        }
        handleNotification(message);
        /*if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            sendNotification(remoteMessage.getData().get("message"));
            Intent intent = new Intent(INTENT_FILTER);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(INTENT_FILTER);
            sendBroadcast(intent);
        }*/

    }


    private void handleNotification(String message) {
        if (isAppIsInBackground(getApplicationContext())) {
            wakeUpScreen();
            Log.d(TAG, "background");
            if (message.equalsIgnoreCase("New Incoming Ride")) {
                KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("IN");
                kl.disableKeyguard();

                Intent mainIntent = new Intent(this, SplashScreen.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
            }
        }

        sendNotification(message);
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "PUSH");
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(messageBody);
        long when = System.currentTimeMillis();         // notification time
        // Sets an ID for the notification, so it can be updated.
        int notifyID = 1;
        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "Channel human readable title4";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        mBuilder.setSmallIcon(R.mipmap.ic_launcher).setTicker(getString(R.string.app_name))
                .setContentTitle(getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setSound(sound)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setWhen(when)
                .setSmallIcon(getNotificationIcon(mBuilder))
                .setContentText(messageBody)
                .setChannelId(CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_VIBRATE
                        | Notification.DEFAULT_LIGHTS
                );
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel mChannel = new android.app.NotificationChannel(CHANNEL_ID, name, importance);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mChannel.setSound(null, null);
            try {
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), sound);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Create a notification and set the notification channel.
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(0, mBuilder.build());
    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            return R.drawable.ic_push;
        } else {
            return R.mipmap.ic_launcher;
        }
    }

    private void wakeUpScreen() {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on......", "" + isScreenOn);
        if (!isScreenOn) {
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
            wl_cpu.acquire(10000);
        }
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }


}