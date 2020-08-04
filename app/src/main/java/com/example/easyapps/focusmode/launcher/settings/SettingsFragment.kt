package com.example.easyapps.focusmode.launcher.settings

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
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
    private val REQ_CODE = 100;
    private lateinit var remindFocus:Switch
    private lateinit var dozeMode:Switch

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
            interaction.openAppDrawerForSelection()
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

        remindFocus = view.findViewById<Switch>(R.id.remind_focus_lost)

        dozeMode = view.findViewById<Switch>(R.id.dnd)

        remindFocus.isChecked = Utils.getRemindMeOption(context!!)

        remindFocus.setOnCheckedChangeListener { compoundButton: CompoundButton, checked: Boolean ->
            if (checked && !Utils.isUsagePermissionGranted(view.context)) {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            Utils.saveRemindMeOption(view.context, checked)
        }

        dozeMode.isChecked = Utils.getDndOption(context!!)

        dozeMode.setOnCheckedChangeListener{ compoundButton: CompoundButton, b: Boolean ->
            if (b && !Utils.isNotificationListenerPermissionGranted(view.context.applicationContext)) {
                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                startActivity(intent)
            }
            Utils.saveDndOption(view.context, b)
        }

        val daypicker = view.findViewById<MaterialDayPicker>(R.id.day_picker);
        daypicker.setDaySelectionChangedListener {
            Utils.saveSelectedDays(view.context, daypicker.selectedDays)
            sharedVm.updateCurrentProgress(view.context)
        }

    }

    override fun onStart() {
        super.onStart()
        remindFocus.isChecked = remindFocus.isChecked && Utils.isUsagePermissionGranted(context!!.applicationContext)
        dozeMode.isChecked = dozeMode.isChecked && Utils.isNotificationListenerPermissionGranted(context!!.applicationContext)
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
        fun openAppDrawerForSelection();
    }

}