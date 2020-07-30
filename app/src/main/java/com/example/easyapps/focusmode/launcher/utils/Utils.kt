package com.example.easyapps.focusmode.launcher.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
import android.provider.Settings
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.example.easyapps.focusmode.launcher.AppDrawerInfo
import com.example.easyapps.focusmode.launcher.AppInfo
import java.util.*
import kotlin.collections.LinkedHashSet


class Utils {

    companion object {

        private const val SHARED_PREF = "LAUNCHER_DEFAULT_SP"
        private const val SELECTED_APPS = "Selectedapps"
        private const val EXCEPTION_APPS = "ExceptionApp"
        private const val START_HOUR = "starthour"
        private const val START_MIN = "startmin"
        private const val END_HOUR = "endhour"
        private const val END_MIN = "endmin"
        private const val SELECTED_DAYS = "selecteddays"
        private const val RUN_COUNT = "run"
        private const val REMIND_ME = "remind_me"
        const val PACKAGE_NAME = "package_name"


        fun getAllAppsForDrawer(
            context: Context,
            selectedApps: MutableSet<AppDrawerInfo>?
        ): MutableSet<AppDrawerInfo> {
            val pm = context.packageManager
            val appsList = mutableSetOf<AppDrawerInfo>()

            val i = Intent(Intent.ACTION_MAIN, null)
            i.addCategory(Intent.CATEGORY_LAUNCHER)

            val allApps = pm.queryIntentActivities(i, 0)
            for (ri in allApps) {
                val app = AppInfo(
                    ri.activityInfo.applicationInfo.loadIcon(pm),
                    ri.activityInfo.applicationInfo.loadLabel(pm) as String,
                    ri.activityInfo.applicationInfo.packageName
                )
                val selected = selectedApps?.contains(
                    AppDrawerInfo(
                        app,
                        true
                    )
                ) ?: false

                appsList.add(AppDrawerInfo(app, selected));
            }
            return appsList
        }

        fun saveSelectedApps(context: Context, selectedApps: Set<AppDrawerInfo>) {
            context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit {
                val packageNameList = selectedApps.map { it.appInfo.packageName }
                putStringSet(SELECTED_APPS, LinkedHashSet(packageNameList))
            }
        }

        fun getSelectedApps(context: Context): LinkedHashSet<AppDrawerInfo> {
            val packageset =
                context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).getStringSet(
                    SELECTED_APPS, null
                ) ?: return linkedSetOf()

            packageset.remove(
                getDialer(
                    context
                )
            )
            packageset.remove(getSettings(context))
            packageset.remove(context.packageName)
            val list = packageset.mapNotNull {
                getAppInfoPkgName(
                    context,
                    it
                )
            }
            return LinkedHashSet(list)
        }

        fun getExhaustiveSelectedApps(context: Context): LinkedHashSet<AppDrawerInfo> {
            val packageset =
                context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).getStringSet(
                    SELECTED_APPS, null
                ) ?: linkedSetOf()

            packageset.addAll(getExceptionList(context))

            val list = packageset.mapNotNull {
                getAppInfoPkgName(
                    context,
                    it
                )
            }
            return LinkedHashSet(list)
        }

        fun addExceptionPackage(context: Context, packageName: String) {
            val packageSet =
                context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).getStringSet(
                    EXCEPTION_APPS, null
                ) ?: linkedSetOf()

            packageSet.add(packageName)

            context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit {
                putStringSet(EXCEPTION_APPS, packageSet)
            }

        }

        fun getExceptionList(context: Context): Set<String> {
            val packageSet =
                context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).getStringSet(
                    EXCEPTION_APPS, null
                ) ?: linkedSetOf()

            packageSet.add(
                getDialer(
                    context
                )
            )
            packageSet.add(
                getSettings(
                    context
                )
            )
            packageSet.add(context.packageName)
            packageSet.add(
                getPackageForIntent(
                    context,
                    Intent(ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                )
            )
            packageSet.addAll(getDefaultOrAllPackagesForIntent(context, Intent(Intent.ACTION_CALL)))
            return packageSet
        }


        fun getDialer(context: Context): String {
            val dialingIntent = Intent(Intent.ACTION_DIAL).addCategory(Intent.CATEGORY_DEFAULT)
            val resolveInfoList = context.packageManager.queryIntentActivities(dialingIntent, 0)
            if (resolveInfoList.size == 1) {
                return resolveInfoList[0].activityInfo.packageName
            }
            resolveInfoList.forEach {
                if (it.isDefault) {
                    return it.activityInfo.packageName
                }
            }
            return resolveInfoList[0].activityInfo.packageName
        }

        fun getSettings(context: Context): String {
            val dialingIntent =
                Intent(Settings.ACTION_SETTINGS).addCategory(Intent.CATEGORY_DEFAULT)
            val resolveInfoList = context.packageManager.queryIntentActivities(dialingIntent, 0)
            if (resolveInfoList.size == 1) {
                return resolveInfoList[0].activityInfo.applicationInfo.packageName
            }
            resolveInfoList.forEach {
                if (it.isDefault) {
                    return it.activityInfo.applicationInfo.packageName
                }
            }
            return resolveInfoList[0].activityInfo.applicationInfo.packageName
        }

        fun getPackageForIntent(context: Context, intent: Intent): String {
            val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
            if (resolveInfoList.size == 1) {
                return resolveInfoList[0].activityInfo.applicationInfo.packageName
            }
            resolveInfoList.forEach {
                if (it.isDefault) {
                    return it.activityInfo.applicationInfo.packageName
                }
            }
            return resolveInfoList[0].activityInfo.applicationInfo.packageName
        }

        fun getDefaultOrAllPackagesForIntent(context: Context, intent: Intent): List<String> {
            val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
            val defaultApp = resolveInfoList.filter {
                it.isDefault
            }.map {
                it.activityInfo.applicationInfo.packageName
            }
            if (defaultApp.isEmpty()) {
                return resolveInfoList.map {
                    it.activityInfo.applicationInfo.packageName
                }
            } else {
                return defaultApp
            }
        }


        fun getDialerAppInfo(context: Context): AppDrawerInfo? {
            return getAppInfoPkgName(
                context,
                getDialer(context)
            );
        }

        fun getAppInfoPkgName(context: Context, packageName: String): AppDrawerInfo? {
            try {
                val packageManager = context.packageManager
                val info =
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                val appInfo = AppInfo(
                    info.loadIcon(packageManager),
                    info.loadLabel(packageManager) as String,
                    packageName
                )
                return AppDrawerInfo(appInfo, true)
            } catch (e: PackageManager.NameNotFoundException) {
            }
            return null

        }

        fun launchApp(context: Context, appDrawerInfo: AppDrawerInfo) {
            var intent =
                context.packageManager.getLaunchIntentForPackage(appDrawerInfo.appInfo.packageName)
            context.startActivity(intent)
        }

        fun openSettingsPageForlauncherDefault(context: Context) {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Intent(ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            } else {
                TODO("VERSION.SDK_INT < N")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent);

        }

        fun openSettingsApp(context: Context) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent);
        }

        fun saveStartTime(context: Context, hrs: Int, min: Int) {
            context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit {
                putInt(START_HOUR, hrs)
                putInt(START_MIN, min)
            }
        }

        fun getStartTime(context: Context): String {
            return getStringValueForInt(
                getSPInt(
                    context,
                    START_HOUR
                )
            ) + ":" +
                    getStringValueForInt(
                        getSPInt(
                            context,
                            START_MIN
                        )
                    )
        }

        fun getEndTime(context: Context): String {
            return getStringValueForInt(
                getSPInt(
                    context,
                    END_HOUR
                )
            ) + ":" +
                    getStringValueForInt(
                        getSPInt(
                            context,
                            END_MIN
                        )
                    )
        }

        fun saveEndTime(context: Context, hrs: Int, min: Int) {
            context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit {
                putInt(END_HOUR, hrs)
                putInt(END_MIN, min)
            }
        }

        fun saveSelectedDays(context: Context, days: List<MaterialDayPicker.Weekday>) {
            val dayIndex = days.map {
                it.ordinal
            }
            context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit {
                putString(SELECTED_DAYS, GsonUtils.serializeToJson(dayIndex, List::class.java))
            }
        }

        fun getSelectedDays(context: Context): List<Int>? {
            val json = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).getString(
                SELECTED_DAYS, null
            ) ?: return null
            val list: List<Double> = GsonUtils.deserializeJSON(
                json,
                List::class.java
            ) as List<Double>
            return list?.map {
                it.toInt()
            }
        }

        fun getSPInt(context: Context, key: String): Int {
            return context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).getInt(key, 0)
        }

        fun getStringValueForInt(key: Int): String {
            if (key < 10) {
                return "0$key";
            } else {
                return "$key"
            }
        }

        fun isFirstRun(context: Context): Boolean {
            val run = getSPInt(
                context,
                RUN_COUNT
            )
            context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit {
                putInt(RUN_COUNT, run + 1)
            }
            return run == 0;
        }

        fun getFocusProgress(context: Context): Double {
            if (!isDaySelected(context)) {
                return 100.0
            }
            val currentTime = Calendar.getInstance().time
            val startFocus = Calendar.getInstance()
            startFocus.set(
                Calendar.HOUR_OF_DAY,
                getSPInt(
                    context,
                    START_HOUR
                )
            )
            startFocus.set(
                Calendar.MINUTE,
                getSPInt(
                    context,
                    START_MIN
                )
            )

            val endFocus = Calendar.getInstance()
            endFocus.set(
                Calendar.HOUR_OF_DAY,
                getSPInt(
                    context,
                    END_HOUR
                )
            )
            endFocus.set(
                Calendar.MINUTE,
                getSPInt(
                    context,
                    END_MIN
                )
            )

            if (currentTime.before(startFocus.time)) {
                return 100.0
            }
            if (currentTime.after(endFocus.time)) {
                return 100.0
            }
            val progress: Double =
                ((currentTime.time.toDouble() - startFocus.time.time) / (endFocus.time.time - startFocus.time.time))
            return progress * 100
        }

        fun getTimeToEndFocus(context: Context): Long {
            val currentTime = Calendar.getInstance().time
            val endFocus = Calendar.getInstance()
            endFocus.set(
                Calendar.HOUR_OF_DAY,
                getSPInt(
                    context,
                    END_HOUR
                )
            )
            endFocus.set(
                Calendar.MINUTE,
                getSPInt(
                    context,
                    END_MIN
                )
            )
            return endFocus.time.time - currentTime.time
        }

        fun isFocusHourSet(context: Context): Boolean {
            if (!isDaySelected(context)) {
                return false
            }
            val sp = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
            return sp.contains(START_HOUR) && sp.contains(
                END_HOUR
            )
        }

        private fun isDaySelected(context: Context): Boolean {
            val days =
                getSelectedDays(
                    context
                ) ?: return false
            val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (days.count() == 0 || !days.contains(day - 1)) {
                return false
            }
            return true
        }

        fun isUsagePermissionGranted(context: Context): Boolean {
            val appOps =
                context.applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        }

        fun saveRemindMeOption(context: Context, remind: Boolean) {
            context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit {
                putBoolean(REMIND_ME, remind)
            }
        }

        fun getRemindMeOption(context: Context): Boolean {
            return context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                .getBoolean(REMIND_ME, false)
        }
    }

}