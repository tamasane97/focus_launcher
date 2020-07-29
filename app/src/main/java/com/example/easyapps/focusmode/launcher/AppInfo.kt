package com.example.easyapps.focusmode.launcher

import android.graphics.drawable.Drawable

data class AppInfo(val icon: Drawable, val label: String, val packageName: String){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppInfo

        if (packageName != other.packageName) return false

        return true
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }

}

data class AppDrawerInfo(val appInfo: AppInfo, var selected: Boolean)
