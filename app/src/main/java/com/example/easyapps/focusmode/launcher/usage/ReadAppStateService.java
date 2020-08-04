package com.example.easyapps.focusmode.launcher.usage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.DateUtils;

import androidx.core.app.NotificationCompat;

import com.example.easyapps.focusmode.launcher.AppDrawerInfo;
import com.example.easyapps.focusmode.launcher.LauncherActivity;
import com.example.easyapps.focusmode.launcher.R;
import com.example.easyapps.focusmode.launcher.utils.FocusUtil;
import com.example.easyapps.focusmode.launcher.utils.Utils;

public class ReadAppStateService extends Service {


    private boolean isServiceForeground;
    private Handler localHandler = new Handler();

    public ReadAppStateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra("Notification", false)) {
            FocusUtil.INSTANCE.clearReminderNotification(this);
            Utils.Companion.addExceptionPackage(
                    this,
                    intent.getStringExtra(Utils.PACKAGE_NAME));
        }
        startMonitoring();
        return START_NOT_STICKY;
    }

    private void startMonitoring() {
        if (Utils.Companion.getFocusProgress(this) < 100.0) {
            localHandler.postDelayed(runnable, DateUtils.SECOND_IN_MILLIS);
        } else {
            FocusUtil.INSTANCE.setAlarmForFocusHours(this);
            stopSelf();
        }
    }

    private void makeServiceForeground() {
        Intent notificationIntent = new Intent(this, LauncherActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createSilentNotificationChannel("channel_Fg", "Checker");
        }
        notification = new NotificationCompat.Builder(this, "channel_Fg")
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(android.R.drawable.ic_notification_clear_all)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(FocusUtil.INSTANCE.getREMIND_FOCUS_NOT_ID(), notification);
        isServiceForeground = true;
    }

    private void createSilentNotificationChannel(String channelid, String channelName) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channelName;
            String description = "checks for usage of Non-focused apps";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(channelid, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (Utils.Companion.getFocusProgress(ReadAppStateService.this) < 100.0) {
                stopSelf();
                return;
            }
            String openedAppPackage = FocusUtil.INSTANCE.getRecentOpenedApp(ReadAppStateService.this);
            long time = 5 * DateUtils.SECOND_IN_MILLIS;
            if (!isServiceForeground) {
                makeServiceForeground();
            }
            if (openedAppPackage == null || !Utils.Companion.hasLauncherIntent(ReadAppStateService.this, openedAppPackage)) {
                localHandler.postDelayed(this, time);
                return;
            }
            AppDrawerInfo appDrawerInfo = Utils.Companion.getAppInfoPkgName(ReadAppStateService.this, openedAppPackage);
            if (appDrawerInfo != null && !Utils.Companion.getExhaustiveSelectedApps(ReadAppStateService.this).contains(appDrawerInfo)) {
                FocusUtil.INSTANCE.updateNotification(ReadAppStateService.this, appDrawerInfo.getAppInfo().getPackageName());
                time = DateUtils.MINUTE_IN_MILLIS;
            }
            localHandler.postDelayed(this,time);
        }
    };

}
