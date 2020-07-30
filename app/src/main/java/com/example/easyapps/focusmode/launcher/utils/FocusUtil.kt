package com.example.easyapps.focusmode.launcher.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.easyapps.focusmode.launcher.LauncherActivity
import com.example.easyapps.focusmode.launcher.R
import com.example.easyapps.focusmode.launcher.usage.ReadAppStateService

object FocusUtil {
    @SuppressLint("WrongConstant")
    public fun getRecentOpenedApp(context: Context): String? {
        val endTime = System.currentTimeMillis()
        var lastTimeUsed: Long = -1
        val startTime = System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS
        var recentPackageName: String? = null
        val usageStatsManager =
            context.applicationContext.getSystemService("usagestats") as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        for (usageStats in queryUsageStats) {
            if (usageStats == null || usageStats.packageName.isNullOrEmpty()) {
                continue
            }
            if (usageStats.firstTimeStamp < startTime) {
                continue
            }
            if (lastTimeUsed < usageStats.lastTimeUsed) {
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

    private val REMIND_FOCUS_NOT_ID = 11

    /**
     * @param usageStatsManager
     * @param diff
     * @return
     *//*
    fun crossCheckForeGroundApp(usageStatsManager: UsageStatsManager, diff: Long): String? {
        var diff = diff
        val time = System.currentTimeMillis()
        var packageName: String? = null
        diff -= 1 * DateUtils.SECOND_IN_MILLIS
        val usageEvents = usageStatsManager.queryEvents(diff, time)
        val event = UsageEvents.Event()
        // get last foreground event so that we can get last app in foreground
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || event.eventType == UsageEvents.Event.USER_INTERACTION) {
                packageName = event.packageName
            }
        }
        return packageName
    }*/

    fun showNotification(context: Context) {
        val notificationIntent = Intent(context, LauncherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        val dismissIntent = PendingIntent.getService(
            context,
            0,
            Intent(context, ReadAppStateService::class.java).putExtra("Notification", true),
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
            channel.enableLights(true)
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