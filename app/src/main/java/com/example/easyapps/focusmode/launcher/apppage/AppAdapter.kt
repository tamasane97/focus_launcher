package com.example.easyapps.focusmode.launcher.apppage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.easyapps.focusmode.launcher.AppDrawerInfo
import com.example.easyapps.focusmode.launcher.R

class AppAdapter(val set: MutableSet<AppDrawerInfo>) :
    RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    private val appInfoSet: MutableSet<AppDrawerInfo> = linkedSetOf()
    private val selectedAppList: MutableList<AppDrawerInfo> = mutableListOf()

    init {
        appInfoSet.addAll(set)
        selectedAppList.addAll(appInfoSet.filter {
            it.selected
        }.map {
            AppDrawerInfo(it.appInfo, true)
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.app_item,
                parent,
                false
            )
        );
    }

    override fun getItemCount(): Int {
        return appInfoSet.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = appInfoSet.elementAt(position)
        holder.bind(appInfo)
        val isSelected = selectedAppList.contains(appInfo)
        holder.updateCheckBoxVisibility(isSelected)
        holder.itemView.setOnClickListener {
            if (!holder.checkBox.isChecked && selectedAppList.count() >= 6) {
                Toast.makeText(
                    holder.itemView.context,
                    "Max Number of Apps selected",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //toggle
                val wasSelected = holder.checkBox.isChecked
                assert(selectedAppList.contains(appInfo) == wasSelected)
                holder.updateCheckBoxVisibility(!wasSelected)
                if (wasSelected) {
                    selectedAppList.remove(AppDrawerInfo(appInfo.appInfo, true))
                } else {
                    selectedAppList.add(AppDrawerInfo(appInfo.appInfo, true))
                }
            }
        }
    }

    fun update(set: MutableSet<AppDrawerInfo>) {
        updateList(set)
        notifyDataSetChanged()
    }

    private fun updateList(set: MutableSet<AppDrawerInfo>) {
        appInfoSet.clear();
        appInfoSet.addAll(set)
        selectedAppList.clear()
        selectedAppList.addAll(appInfoSet.filter {
            it.selected
        }.map {
            AppDrawerInfo(it.appInfo, true)
        })
    }

    fun getAppCurrentSelectedSet(): List<AppDrawerInfo> {
        return selectedAppList
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label);
        val checkBox: AppCompatCheckBox = itemView.findViewById(R.id.check);

        fun bind(appInfo: AppDrawerInfo) {
            icon.setImageDrawable(appInfo.appInfo.icon)
            label.text = appInfo.appInfo.label
            checkBox.isClickable = false
        }

        fun updateCheckBoxVisibility(checked: Boolean) {
            if (checked) {
                checkBox.isChecked = true
                checkBox.visibility = View.VISIBLE
            } else {
                checkBox.isChecked = false
                checkBox.visibility = View.GONE
            }
        }
    }
}