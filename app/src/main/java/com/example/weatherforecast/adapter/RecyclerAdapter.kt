package com.example.weatherforecast.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.R
import com.example.weatherforecast.model.Weather

class RecyclerAdapter(val context: Context, val itemList: List<Weather>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val date: TextView = view.findViewById(R.id.date)
        val description: TextView = view.findViewById(R.id.description)
        val max_min: TextView = view.findViewById(R.id.max_min)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_row_recycler, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return itemList.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val temp = itemList[position]

        holder.date.text = temp.dt
        holder.description.text = temp.description
        holder.max_min.text = temp.temp_max + "/" + temp.temp_min
    }
}