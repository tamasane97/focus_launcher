package com.example.easyapps.focusmode.launcher

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

class AppDrawerBehaviour : BottomSheetBehavior<View>() {


    override fun setPeekHeight(peekHeight: Int) {
        super.setPeekHeight(80,true)
    }

    override fun setExpandedOffset(offset: Int) {
        super.setExpandedOffset(0)
    }
}
