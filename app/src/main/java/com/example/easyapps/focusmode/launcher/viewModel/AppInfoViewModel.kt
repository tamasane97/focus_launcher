package com.example.easyapps.focusmode.launcher.viewModel

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import com.example.easyapps.focusmode.launcher.AppDrawerInfo
import com.example.easyapps.focusmode.launcher.utils.Utils

class AppInfoViewModel(val application: Application) : ViewModel() {

    val appInfoList: MutableLiveData<MutableSet<AppDrawerInfo>> by lazy {
        MutableLiveData<MutableSet<AppDrawerInfo>>().also {
            val runnable = Runnable {
                it.postValue(
                    Utils.getAllAppsForDrawer(
                        application.applicationContext,
                        selectedAppSet.value
                    )
                )
            }
            Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    val selectedAppSet: MutableLiveData<MutableSet<AppDrawerInfo>> by lazy {
        MutableLiveData<MutableSet<AppDrawerInfo>>().also {
            it.value = Utils.getSelectedApps(application.applicationContext)
        }
    }

    val currentProgress: MutableLiveData<Double> = MutableLiveData();
    private var timer: CountDownTimer? = null

    fun startTicking() {
        val context = application.applicationContext
        if (!Utils.isFocusHourSet(context)) {
            currentProgress.value = 100.0
            return
        }
        updateCurrentProgress(context)
        timer = object : CountDownTimer(Utils.getTimeToEndFocus(context), 60 * 1000) {

            override fun onTick(millisUntilFinished: Long) {
                currentProgress.postValue(Utils.getFocusProgress(context))
                if (100.0 == currentProgress.value) {
                    cancel()
                }
            }

            override fun onFinish() {
                currentProgress.value = 100.0
            }
        }.start()
    }

    fun updateCurrentProgress(context: Context){
        currentProgress.value = Utils.getFocusProgress(context)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }

    fun saveSelectedApps(context: Context, selectedApps: List<AppDrawerInfo>) {
        val appSelection = mutableSetOf<AppDrawerInfo>()
        appSelection.addAll(selectedApps)
        appInfoList.value!!.forEach {
            it.selected = selectedApps.contains(AppDrawerInfo(it.appInfo, true))
        }
        selectedAppSet.value = appSelection
        Utils.saveSelectedApps(context, appSelection)
    }

}

class AppInfoVMFactory(private val application: Application) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(application::class.java).newInstance(application)
    }

}