package com.example.easyapps.focusmode.launcher

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.easyapps.focusmode.launcher.apppage.AppFragment
import com.example.easyapps.focusmode.launcher.settings.OnBoardingFragment
import com.example.easyapps.focusmode.launcher.settings.SettingsFragment
import com.example.easyapps.focusmode.launcher.utils.Utils
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoVMFactory
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoViewModel

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class LauncherActivity : AppCompatActivity(), HomeFragment.Interaction,
    SettingsFragment.Interaction, OnBoardingFragment.Interaction {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        if (Utils.isFirstRun(this)) {
            openOnBoardingFragment()
        } else {
            val appInfoVm: AppInfoViewModel by viewModels {
                    AppInfoVMFactory(application)
            }
            appInfoVm.startTicking()
            addLauncherFragment()
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

    override fun openLauncherWithDrawer() {
        addLauncherFragment()
        openAppDrawer()
    }


    /*override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
    }*/

    override fun openAppDrawer() {
        AppFragment.show(supportFragmentManager, "App_Drawer")
    }


}
