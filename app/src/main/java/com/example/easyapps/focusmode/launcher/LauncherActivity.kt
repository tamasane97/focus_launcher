package com.example.easyapps.focusmode.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.easyapps.focusmode.launcher.apppage.AppDrawerFragment
import com.example.easyapps.focusmode.launcher.apppage.AppFragment
import com.example.easyapps.focusmode.launcher.settings.OnBoardingFragment
import com.example.easyapps.focusmode.launcher.settings.SettingsFragment
import com.example.easyapps.focusmode.launcher.usage.ReadAppStateService
import com.example.easyapps.focusmode.launcher.utils.FocusUtil
import com.example.easyapps.focusmode.launcher.utils.Utils
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoVMFactory
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoViewModel
import android.os.BatteryManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class LauncherActivity : AppCompatActivity(), HomeFragment.Interaction,
    SettingsFragment.Interaction, OnBoardingFragment.Interaction {

    private val appInfoViewModel: AppInfoViewModel by viewModels {
        AppInfoVMFactory(application)
    }
    private lateinit var battery: TextView
    private val batteryDrawable = arrayOf(
        R.drawable.ic_battery_20_black_24dp,
        R.drawable.ic_battery_50_black_24dp,
        R.drawable.ic_battery_60_black_24dp,
        R.drawable.ic_battery_80_black_24dp,
        R.drawable.ic_battery_90_black_24dp,
        R.drawable.ic_battery_full_black_24dp
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)

        if (Utils.isFirstRun(this)) {
            openOnBoardingFragment()
        } else {
            appInfoViewModel.startTicking()
            addLauncherFragment()
        }
        battery = findViewById(R.id.status_bar);
        registerReceiver(this.mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        registerWindowInset();
    }

    private fun registerWindowInset() {
        val view = findViewById<View>(R.id.content_holder)
        ViewCompat.setOnApplyWindowInsetsListener(view)
        { v, insets ->
            v.updatePadding(bottom = insets.systemWindowInsets.bottom)
            // Return the insets so that they keep going down the view hierarchy
            insets
        }
    }

    private val mBatInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            battery.text = "$level%"
            battery.setCompoundDrawablesWithIntrinsicBounds(batteryDrawable[level/20],0,0,0)
        }
    }

    private fun addLauncherFragment() {
        val fragment = HomeFragment.creteHomeFragment()
        supportFragmentManager.commit(allowStateLoss = true) {
            replace(R.id.content_holder, fragment)
        }
    }

    override fun openSettingsPage() {
        supportFragmentManager.commit(allowStateLoss = true) {
            val fragment = SettingsFragment.createSettingsPage();
            replace(R.id.content_holder, fragment)
            addToBackStack(null)
        }
    }

    private fun openOnBoardingFragment() {
        supportFragmentManager.commit(allowStateLoss = true) {
            val fragment = OnBoardingFragment.createOnBoardingFragment();
            replace(R.id.content_holder, fragment)
        }
    }

    override fun onOnBoardingFinished() {
        addLauncherFragment()
        openAppDrawerForSelection()
    }

    override fun onStart() {
        super.onStart()
        onLauncherStarted()
    }

    override fun onStop() {
        super.onStop()
        startService(Intent(this, ReadAppStateService::class.java))
    }

    override fun onRestart() {
        super.onRestart()
        onLauncherStarted()
    }

    private fun onLauncherStarted() {
        stopService(Intent(this, ReadAppStateService::class.java))
        FocusUtil.clearReminderNotification(this)
    }


    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
    }

    override fun openAppDrawerForSelection() {
        AppFragment.show(supportFragmentManager, "App_Drawer_selection")
    }

    override fun openAppDrawer() {
        supportFragmentManager.commit(allowStateLoss = true) {
            val fragment = AppDrawerFragment.createAppDrawer();
            replace(R.id.content_holder, fragment)
            addToBackStack(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBatInfoReceiver)
    }

}
