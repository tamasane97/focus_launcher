package com.example.easyapps.focusmode.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easyapps.focusmode.launcher.utils.Utils

class LauncherAdapter(val set: MutableSet<AppDrawerInfo>) :
    RecyclerView.Adapter<LauncherAdapter.ViewHolder>() {

    private val appInfoSet: MutableSet<AppDrawerInfo> = linkedSetOf()

    init {
        appInfoSet.addAll(set)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.launcher_item,
                parent,
                false
            )
        );
    }

    override fun getItemCount(): Int {
        return appInfoSet.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appInfoSet.elementAt(position))
    }

    fun update(set: MutableSet<AppDrawerInfo>) {
        appInfoSet.clear();
        appInfoSet.addAll(set)
        notifyDataSetChanged()
    }

    fun getAppCurrentSelectedSet(): List<AppDrawerInfo> {
        val set = mutableSetOf<AppDrawerInfo>()
        set.addAll(appInfoSet)
        return set.mapNotNull {
            if (it.selected) {
                it
            } else {
                null
            }
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val label: TextView = itemView.findViewById(R.id.label);

        fun bind(appInfo: AppDrawerInfo) {
            icon.setImageDrawable(appInfo.appInfo.icon)
            label.text = appInfo.appInfo.label
            itemView.setOnClickListener {
                Utils.launchApp(it.context, appInfo)
            }
        }
    }
}