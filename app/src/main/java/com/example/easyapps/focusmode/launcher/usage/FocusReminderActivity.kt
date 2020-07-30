package com.example.easyapps.focusmode.launcher.usage

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.easyapps.focusmode.launcher.LauncherActivity
import com.example.easyapps.focusmode.launcher.R
import com.example.easyapps.focusmode.launcher.utils.Utils



class FocusReminderActivity : AppCompatActivity() {

    private lateinit var appInfo: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.focus_remind)
        if (intent.extras != null) {
            appInfo = intent.getStringExtra(Utils.PACKAGE_NAME)
        }
        initView()
    }

    private fun startLauncher() {
        val intent = Intent(this, LauncherActivity::class.java)
        startActivity(intent)
    }

    private fun initView() {
        findViewById<View>(R.id.take_back).setOnClickListener { startLauncher() }
        findViewById<View>(R.id.snooze).setOnClickListener { finish() }
        findViewById<View>(R.id.exception).setOnClickListener {
            Utils.addExceptionPackage(
                this,
                appInfo
            )
            finish()
        }
     //   animateOpen()
    }

    private fun animateOpen() {
        val view = findViewById<View>(R.id.focus_reminder)
        val transAnimation = ObjectAnimator.ofFloat(view, "translationY", 300f, 0f)
        transAnimation.duration = 300
        transAnimation.start()
    }


}