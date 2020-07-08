package com.example.weatherforecast.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.weatherforecast.R
import com.example.weatherforecast.util.ConnectionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException


class DateFragment : Fragment(), OnMapReadyCallback {

    lateinit var spinner: Spinner
    lateinit var cities: Array<String>

    lateinit var status: TextView
    lateinit var temp: TextView
    lateinit var temp_min: TextView
    lateinit var temp_max: TextView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var cityName: String
    val API_KEY = "c5fe1b1dd89f6ca1c1b822bd2cf5b6e8"
    lateinit var tempUnit: String
    lateinit var unit: String
    lateinit var llContent: RelativeLayout
    private var googleMap: GoogleMap? = null
    lateinit var citySelection: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_date, container, false)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        sharedPreferences =
            activity!!.getSharedPreferences(
                getString(R.string.preference_location),
                Context.MODE_PRIVATE
            )
        sharedPreferences.edit().putString("CityName", "New Delhi").apply()

        citySelection = view.findViewById(R.id.citySelection)

        citySelection.text =
            "${sharedPreferences.getString("UserName", "User")}, Please select city:"


        spinner = view.findViewById(R.id.spinner)
        cities = arrayOf("New Delhi", "Mumbai", "Noida")

        val adapter = ArrayAdapter(activity as Context, R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // either one will work as well
                // val item = parent.getItemAtPosition(position) as String
                val item = adapter.getItem(position) as String
                sharedPreferences.edit().putString("CityName", item).apply()
                fetchData()
            }
        }

        return view
    }


    fun fetchData() {
        sharedPreferences =
            activity!!.getSharedPreferences(
                getString(R.string.preference_location),
                Context.MODE_PRIVATE
            )

        unit = sharedPreferences.getString("Unit", "metric").toString()
        if (unit == "metric") {
            tempUnit = "°C"
        } else {
            tempUnit = "°F"
        }

        cityName = sharedPreferences.getString("CityName", "New Delhi").toString()
        status = view!!.findViewById(R.id.status)
        temp = view!!.findViewById(R.id.temp)
        temp_min = view!!.findViewById(R.id.temp_min)
        temp_max = view!!.findViewById(R.id.temp_max)
        llContent = view!!.findViewById(R.id.llContent)
        llContent.visibility = View.INVISIBLE


        val queue = Volley.newRequestQueue(activity as Context)
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$cityName&unit=$unit&appid=$API_KEY"

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = @SuppressLint("SetTextI18n")
            object : JsonObjectRequest(Method.GET, url, null, Response.Listener {
                try {

                    llContent.visibility = View.VISIBLE
                    val coord = it.getJSONObject("coord")
                    sharedPreferences.edit()
                        .putFloat("newLatitude", coord.getDouble("lat").toFloat()).apply()
                    sharedPreferences.edit()
                        .putFloat("newLongitude", coord.getDouble("lon").toFloat()).apply()
                    onMapReady(googleMap)
                    val weather = it.getJSONArray("weather").getJSONObject(0)
                    val main = it.getJSONObject("main")
                    val sys = it.getJSONObject("sys")

                    val temp = main.getString("temp")
                    val tempMin = main.getString("temp_min")
                    val tempMax = main.getString("temp_max")
                    val weatherDescription = weather.getString("description")
                    val address = it.getString("name") + ", " + sys.getString("country")
                    val id = it.getString("id")
                    sharedPreferences.edit().putString("CityId", id).apply()

                    view!!.findViewById<TextView>(R.id.address).text = address
                    view!!.findViewById<TextView>(R.id.status).text =
                        weatherDescription.capitalize()
                    if (unit == "metric") {
                        view!!.findViewById<TextView>(R.id.temp).text =
                            kelvinToCelsius(temp.toDouble()) + tempUnit
                        view!!.findViewById<TextView>(R.id.temp_min).text =
                            "Min Temp: " + "\n ${kelvinToCelsius(tempMin.toDouble())}$tempUnit"
                        view!!.findViewById<TextView>(R.id.temp_max).text =
                            "Max Temp: " + "\n ${kelvinToCelsius(tempMax.toDouble())}$tempUnit"
                    } else {
                        view!!.findViewById<TextView>(R.id.temp).text =
                            kelvinToFahrenheit(temp.toDouble()).toString() + tempUnit
                        view!!.findViewById<TextView>(R.id.temp_min).text =
                            "Min Temp: " + "\n ${kelvinToCelsius(tempMin.toDouble())}$tempUnit"
                        view!!.findViewById<TextView>(R.id.temp_max).text =
                            "Max Temp: " + "\n ${kelvinToCelsius(tempMax.toDouble())}$tempUnit"
                    }


                } catch (e: JSONException) {
                    Toast.makeText(activity as Context, "Some error occured:$e", Toast.LENGTH_LONG)
                        .show()
                }
            }, Response.ErrorListener {

                Toast.makeText(activity as Context, "Volley error occured:$it", Toast.LENGTH_LONG)
                    .show()

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
    }

    fun kelvinToCelsius(kelvin: Double): String {
        val celsius: Double = kelvin - 273.15
        return "%.1f".format(celsius)
    }

    fun kelvinToFahrenheit(kelvin: Double): String {
        val fahrenheit = (kelvin - 273.15) * (1.8) + 32
        return "%.1f".format(fahrenheit)
    }

    override fun onMapReady(p0: GoogleMap?) {

        sharedPreferences =
            activity!!.getSharedPreferences(
                getString(R.string.preference_location),
                Context.MODE_PRIVATE
            )

        val lat: Double =
            "%.2f".format(sharedPreferences.getFloat("newLatitude", 28.61F).toDouble()).toDouble()
        val lon =
            "%.2f".format(sharedPreferences.getFloat("newLongitude", 77.23F).toDouble()).toDouble()
        val cityName = sharedPreferences.getString("CityName", "New Delhi").toString()

        googleMap = p0

        //Adding markers to map

        val latLng = LatLng(lat, lon)
        val markerOptions: MarkerOptions = MarkerOptions().position(latLng).title(cityName)

        // moving camera and zoom map

        val zoomLevel = 12.0f //This goes up to 21


        googleMap.let {
            it!!.addMarker(markerOptions)
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        }
    }

}
