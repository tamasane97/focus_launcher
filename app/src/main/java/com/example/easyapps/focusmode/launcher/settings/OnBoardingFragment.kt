package com.example.easyapps.focusmode.launcher.settings

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.example.easyapps.focusmode.launcher.R
import com.example.easyapps.focusmode.launcher.utils.Utils
import java.lang.IllegalStateException

class OnBoardingFragment : Fragment() {

    private lateinit var interaction : Interaction

    companion object {
        fun createOnBoardingFragment(): OnBoardingFragment {
            return OnBoardingFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Interaction){
            interaction = context
        }else {
            throw IllegalStateException("override Interaction in onBoarding")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.onboarding_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view);
    }

    private fun initViews(view: View) {
        view.findViewById<TextView>(R.id.start_time).setOnClickListener {
            openTimePicker(true)
        }

        view.findViewById<TextView>(R.id.end_time).setOnClickListener {
            openTimePicker(false)
        }
        view.findViewById<TextView>(R.id.done).setOnClickListener{
            Utils.saveSelectedDays(view.context,view.findViewById<MaterialDayPicker>(R.id.day_picker).selectedDays)
            interaction.openLauncherWithDrawer()
        }
        setFocusTime(view)
    }

    private fun openTimePicker(start: Boolean) {
        val picker = TimePickerDialog(
            activity,
            TimePickerDialog.OnTimeSetListener { tp, sHour, sMinute ->
                if (start) {
                    Utils.saveStartTime(tp.context, sHour, sMinute)
                } else {
                    Utils.saveEndTime(tp.context, sHour, sMinute)
                }
                view?.let { setFocusTime(it) }
            },
            0,
            0,
            true
        )
        picker.show()
    }

    private fun setFocusTime(view : View) {
        view.findViewById<TextView>(R.id.start_time).text = Utils.getStartTime(view.context)
        view.findViewById<TextView>(R.id.end_time).text = Utils.getEndTime(view.context)
    }

    interface Interaction {
        fun openLauncherWithDrawer()
    }

}