package com.example.easyapps.focusmode.launcher

import android.content.Context
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.example.easyapps.focusmode.launcher.utils.Utils
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoVMFactory
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoViewModel

class HomeFragment : Fragment() {


    private lateinit var interaction: Interaction
    val sharedVM: AppInfoViewModel by activityViewModels {
        AppInfoVMFactory(activity!!.application)
    }

    companion object {
        fun creteHomeFragment(): HomeFragment {
            val fragment = HomeFragment();
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Interaction) {
            interaction = context
        } else {
            throw IllegalStateException("implement interaction Homefragment")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        sharedVM.updateCurrentProgress(view.context)
        setupClock(view)
    }

    private fun setupClock(view: View) {
        val progress = view.findViewById<CircularProgressIndicator>(R.id.circular_progress)
        progress.maxProgress = 100.0
        progress.isAnimationEnabled = true
        sharedVM.currentProgress.observe(this, Observer {
            progress.setCurrentProgress(it)
        })
    }

    private fun initView(view: View) {
        val appList = view.findViewById<RecyclerView>(R.id.list_view)
        var adapter: LauncherAdapter? = null
        sharedVM.selectedAppSet.observe(this, Observer {
            if (adapter == null) {
                adapter = LauncherAdapter(it)
                appList.adapter = adapter
            } else {
                adapter!!.update(it)
            }
        })
        val layoutManager = GridLayoutManager(view.context, 3)
        appList.layoutManager = layoutManager
        initBottomBar(view)
    }

    private fun initBottomBar(view: View) {
        val dialer = view.findViewById<View>(R.id.dialer)
        val settings = view.findViewById<View>(R.id.settings)
        val drawer = view.findViewById<View>(R.id.drawer)

        setDialer(dialer, Utils.getDialerAppInfo(view.context))
        setSettingsApps(settings)
        setDrawerIcon(drawer)
    }

    private fun setDrawerIcon(drawer: View?) {
        sharedVM.currentProgress.observe(this, Observer {
            if (it >= 100) {
                drawer!!.visibility = View.VISIBLE
                drawer.findViewById<ImageView>(R.id.icon)
                    .setImageResource(R.drawable.ic_apps_black_40dp)
                drawer.setOnClickListener {
                    interaction.openAppDrawer()
                }
            } else {
                drawer!!.visibility = View.GONE
            }
        })
    }

    private fun setDialer(view: View, appInfo: AppDrawerInfo?) {
        if (appInfo == null) {
            return
        }
        view.findViewById<ImageView>(R.id.icon).setImageDrawable(appInfo.appInfo.icon)
        view.findViewById<TextView>(R.id.label).text = appInfo.appInfo.label
        view.setOnClickListener {
            Utils.launchApp(it.context, appInfo)
        }
    }

    private fun setSettingsApps(view: View) {
        view.setOnClickListener {
            interaction.openSettingsPage()
        }
        view.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.ic_settings)
        view.findViewById<TextView>(R.id.label).text = getString(R.string.settings)
    }

    interface Interaction {

        fun openSettingsPage();

        fun openAppDrawer()
    }

}