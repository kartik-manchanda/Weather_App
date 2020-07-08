package com.example.weatherforecast.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.example.weatherforecast.R
import com.example.weatherforecast.adapter.RecyclerAdapter
import com.example.weatherforecast.model.Weather
import com.example.weatherforecast.util.ConnectionManager
import org.json.JSONException
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ReportFragment : Fragment() {

    lateinit var recycler: RecyclerView
    lateinit var recyclerAdapter: RecyclerAdapter
    lateinit var layoutManager: LinearLayoutManager
    var tempList = ArrayList<Weather>()
    val API_KEY = "c5fe1b1dd89f6ca1c1b822bd2cf5b6e8"
    lateinit var loader: RelativeLayout
    lateinit var sharedPreferences: SharedPreferences
    lateinit var tempUnit: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)

        recycler = view.findViewById(R.id.recycler)
        layoutManager = LinearLayoutManager(activity as Context)
        loader = view.findViewById(R.id.loader)
        loader.visibility = View.VISIBLE
        sharedPreferences =
            activity!!.getSharedPreferences(
                getString(R.string.preference_location),
                Context.MODE_PRIVATE
            )

        val id = sharedPreferences.getString("CityId", "00000")
        val unit = sharedPreferences.getString("Unit", "metric")
        if (unit == "metric") {
            tempUnit = "°C"
        } else {
            tempUnit = "°F"
        }

        val queue = Volley.newRequestQueue(activity as Context)
        val url =
            "https://api.openweathermap.org/data/2.5/forecast?id=$id&units=$unit&appid=$API_KEY&cnt=30"

        if (ConnectionManager().checkConnectivity(activity as Context)) {

            val jsonObjectRequest =
                object : JsonObjectRequest(Method.GET, url, null, Response.Listener {
                    try {

                        loader.visibility = View.GONE

                        val list = it.getJSONArray("list")
                        for (i in 0 until list.length()) {
                            val jsonObject = list.getJSONObject(i)
                            val date = jsonObject.getLong("dt")
                            val dt = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                                Date(date * 1000)
                            )
                            val main = jsonObject.getJSONObject("main")
                            val temp_min =
                                (round(main.getString("temp_min").toDouble())).toString() + tempUnit
                            val temp_max =
                                (round(main.getString("temp_max").toDouble())).toString() + tempUnit
                            val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
                            val description = weather.getString("description")

                            val obj = Weather(
                                dt,
                                description,
                                temp_max,
                                temp_min
                            )
                            tempList.add(obj)

                            recyclerAdapter = RecyclerAdapter(
                                activity as Context,
                                tempList
                            )
                            recycler.adapter = recyclerAdapter
                            recycler.layoutManager = layoutManager
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some error occured:$e",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }, Response.ErrorListener {
                    Toast.makeText(
                        activity as Context,
                        "Volley error occured:$it",
                        Toast.LENGTH_LONG
                    ).show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        return headers
                    }
                }
            queue.add(jsonObjectRequest)
        } else {
            Toast.makeText(activity as Context, "No Internet Connection", Toast.LENGTH_LONG).show()
        }

        return view
    }


}
