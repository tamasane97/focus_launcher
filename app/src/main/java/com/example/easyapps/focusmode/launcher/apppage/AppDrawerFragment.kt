package com.example.easyapps.focusmode.launcher.apppage

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyapps.focusmode.launcher.LauncherAdapter
import com.example.easyapps.focusmode.launcher.R
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoVMFactory
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AppDrawerFragment : Fragment() {

    val sharedVM: AppInfoViewModel by activityViewModels {
        AppInfoVMFactory(activity!!.application)
    }

    companion object {
        fun createAppDrawer(): Fragment{
            return AppDrawerFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.app_drawer_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.root)
            .setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        initView(view)
    }


    private fun initView(view: View) {
        val appList = view.findViewById<RecyclerView>(R.id.list_view)
        var adapter: LauncherAdapter? = null
        sharedVM.appInfoList.observe(this, Observer {
            if (it.count() > 0) {
                hideProgress(view)
            }
            if (adapter == null) {
                adapter = LauncherAdapter(it)
                appList.adapter = adapter
            } else {
                adapter!!.update(it)
            }
        })
        appList.layoutManager = GridLayoutManager(view.context, 4) as RecyclerView.LayoutManager?

        // cancel and ticks

        view.findViewById<ImageView>(R.id.done).visibility = View.GONE
        view.findViewById<ImageView>(R.id.discard).visibility = View.GONE
    }

    private fun hideProgress(view: View) {
        val progress = view.findViewById<ProgressBar>(R.id.progress)
        if (progress.visibility == View.VISIBLE) {
            progress.visibility = View.GONE
        }
    }

}