package com.example.easyapps.focusmode.launcher.settings

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.example.easyapps.focusmode.launcher.R
import com.example.easyapps.focusmode.launcher.utils.Utils
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoVMFactory
import com.example.easyapps.focusmode.launcher.viewModel.AppInfoViewModel


class SettingsFragment : Fragment() {


    private lateinit var interaction: Interaction
    private val sharedVm: AppInfoViewModel by activityViewModels {
        AppInfoVMFactory(activity!!.application)
    }

    companion object {
        fun createSettingsPage(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Interaction) {
            interaction = context
        } else {
            throw IllegalStateException("override interaction")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.settings_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    private fun initViews(view: View) {
        view.findViewById<TextView>(R.id.app_drawer).setOnClickListener {
            interaction.openAppDrawer()
        }

        view.findViewById<TextView>(R.id.make_default).setOnClickListener {
            Utils.openSettingsPageForlauncherDefault(it.context)
        }

        view.findViewById<TextView>(R.id.system_settings).setOnClickListener {
            Utils.openSettingsApp(it.context)
        }

        view.findViewById<TextView>(R.id.setup_focus_time).setOnClickListener {
            view.findViewById<View>(R.id.setup_focus).visibility = View.VISIBLE
            setFocusTime(view)
        }

        view.findViewById<TextView>(R.id.start_time).setOnClickListener {
            openTimePicker(true)
        }

        view.findViewById<TextView>(R.id.end_time).setOnClickListener {
            openTimePicker(false)
        }
        val daypicker = view.findViewById<MaterialDayPicker>(R.id.day_picker);
        daypicker.setDaySelectionChangedListener {
            Utils.saveSelectedDays(view.context, daypicker.selectedDays)
            sharedVm.updateCurrentProgress(view.context)
        }

    }

    private fun setFocusTime(view: View) {
        view.findViewById<TextView>(R.id.start_time).text = Utils.getStartTime(view.context)
        view.findViewById<TextView>(R.id.end_time).text = Utils.getEndTime(view.context)
        val selectedDays = Utils.getSelectedDays(view.context)?.map {
            MaterialDayPicker.Weekday[it]
        }
        if (selectedDays != null) {
            view.findViewById<MaterialDayPicker>(R.id.day_picker)
                .setSelectedDays(selectedDays)
        }
    }


    fun openTimePicker(start: Boolean) {
        val picker = TimePickerDialog(
            activity,
            TimePickerDialog.OnTimeSetListener { tp, sHour, sMinute ->
                if (start) {
                    Utils.saveStartTime(tp.context, sHour, sMinute)
                } else {
                    Utils.saveEndTime(tp.context, sHour, sMinute)
                }

                view?.let {
                    setFocusTime(it)
                    sharedVm.updateCurrentProgress(it.context)
                }
            },
            0,
            0,
            true
        )
        picker.show()
    }

    interface Interaction {
        fun openAppDrawer();
    }

}