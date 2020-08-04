package com.example.easyapps.focusmode.launcher.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.easyapps.focusmode.launcher.LauncherActivity
import com.example.easyapps.focusmode.launcher.R
import com.example.easyapps.focusmode.launcher.usage.ReadAppStateService

private val PACKAGE_UNCHANGED = "NO_CHANGE"

object FocusUtil {
    @SuppressLint("WrongConstant")
    public fun getRecentOpenedApp(context: Context): String? {
        val endTime = System.currentTimeMillis()
        val diff = endTime - 2*DateUtils.MINUTE_IN_MILLIS
        var lastTimeUsed: Long = -1
        val startTime = System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS
        var recentPackageName: String? = null
        val usageStatsManager =
            context.applicationContext.getSystemService("usagestats") as UsageStatsManager
        val usageEvents = usageStatsManager.queryEvents(diff, endTime)
        val movedToforeground = getMovedToForegroundApps(usageEvents)
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        /*if(!usageEvents.hasNextEvent()){
            return PACKAGE_UNCHANGED
        }*/
        for (usageStats in queryUsageStats) {
            if (usageStats == null || usageStats.packageName.isNullOrEmpty()) {
                continue
            }
            if (usageStats.firstTimeStamp < startTime) {
                continue
            }
            if (lastTimeUsed < usageStats.lastTimeUsed && movedToforeground.contains(usageStats.packageName)) {
                lastTimeUsed = usageStats.lastTimeUsed
                recentPackageName = usageStats.packageName
            }
        }

        /*if (!recentPackageName.equals(crossCheckForeGroundApp(usageStatsManager, 10))) {
           recentPackageName = null
        }*/
        return recentPackageName;
    }

    fun setAlarmForFocusHours(context: Context) {
        //TODO set Alarm for entering focus mode
    }

    private val REMIND_FOCUS_CHANNEL_ID = "channel_remind"

    val REMIND_FOCUS_NOT_ID = 11

    /**
     * @param usageStatsManager
     * @param diff
     * @return
     */
    fun getMovedToForegroundApps(usageEvents: UsageEvents): List<String> {
        val event = UsageEvents.Event()
        val list = arrayListOf<String>()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                list.add(event.packageName)
            }
        }
        return list
    }

    fun updateNotification(context: Context, packageName: String) {
        val notificationIntent = Intent(context, LauncherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        val dismissIntent = PendingIntent.getService(
            context,
            0,
            Intent(context, ReadAppStateService::class.java)
                .putExtra("Notification", true)
                .putExtra(Utils.PACKAGE_NAME, packageName),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createHeadsupNotificationChannel(context, REMIND_FOCUS_CHANNEL_ID, "remind Focus")
        }
        val notification = NotificationCompat.Builder(context, REMIND_FOCUS_CHANNEL_ID)
            .setContentTitle(context.getText(R.string.notification_title))
            .setContentText(context.getText(R.string.focus_message))
            .setSmallIcon(R.drawable.ic_focus)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getText(R.string.focus_message)))
            .addAction(
                android.R.drawable.ic_notification_clear_all,
                context.getString(R.string.take_me_back),
                pendingIntent
            )
            .addAction(
                android.R.drawable.ic_notification_clear_all,
                context.getString(R.string.always_allow_this_app),
                dismissIntent
            )
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(REMIND_FOCUS_NOT_ID, notification!!)

    }

    private fun createHeadsupNotificationChannel(
        context: Context,
        channelid: String,
        channelName: String
    ) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description = "checks for usage of Non-focused apps"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelid, channelName, importance)
            channel.description = description
            channel.enableVibration(true)
            val v = longArrayOf(1000, 1000, 1000, 1000, 1000)
            channel.vibrationPattern = v
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager =
                context.getSystemService<NotificationManager>(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun clearReminderNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(REMIND_FOCUS_NOT_ID)
    }

}