package com.example.weatherforecast.fragment


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.weatherforecast.R
import com.example.weatherforecast.util.ConnectionManager
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class CurrentFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var latitude: String
    lateinit var longitude: String
    val API_KEY = "c5fe1b1dd89f6ca1c1b822bd2cf5b6e8"
    lateinit var loader: ProgressBar
    lateinit var errorText: TextView
    lateinit var mainContainer: RelativeLayout
    lateinit var unit: String
    lateinit var tempUnit: String
    lateinit var txtUser: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_current, container, false)


        sharedPreferences =
            activity!!.getSharedPreferences(
                getString(R.string.preference_location),
                Context.MODE_PRIVATE
            )
        txtUser = view.findViewById(R.id.txtUser)
        txtUser.text = "Hi, ${sharedPreferences.getString("UserName", "User")}"

        latitude = sharedPreferences.getString("latitude", "0").toString()
        longitude = sharedPreferences.getString("longitude", "0").toString()
        unit = sharedPreferences.getString("Unit", "metric").toString()
        if (unit == "metric") {
            tempUnit = "°C"
        } else {
            tempUnit = "°F"
        }

        errorText = view.findViewById(R.id.errorText)
        loader = view.findViewById(R.id.loader)
        loader.visibility = View.VISIBLE
        mainContainer = view.findViewById(R.id.mainContainer)
        mainContainer.visibility = View.GONE


        val queue = Volley.newRequestQueue(activity as Context)
        val url =
            "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=$unit&appid=$API_KEY"

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            loader.visibility = View.GONE
            val jsonObjectRequest =
                object : JsonObjectRequest(Method.GET, url, null, Response.Listener {
                    try {

                        val weather = it.getJSONArray("weather").getJSONObject(0)
                        val main = it.getJSONObject("main")
                        val sys = it.getJSONObject("sys")
                        val wind = it.getJSONObject("wind")

                        val updatedAt: Long = it.getLong("dt")
                        val updatedAtText = "Updated at: " + SimpleDateFormat(
                            "dd/MM/yyyy hh:mm a",
                            Locale.ENGLISH
                        ).format(Date(updatedAt * 1000))
                        val temp = main.getString("temp") + tempUnit
                        val tempMin = "Min Temp: " + main.getString("temp_min") + tempUnit
                        val tempMax = "Max Temp: " + main.getString("temp_max") + tempUnit
                        val pressure = main.getString("pressure")
                        val humidity = main.getString("humidity")
                        val windSpeed = wind.getString("speed")
                        val weatherDescription = weather.getString("description")
                        val address = it.getString("name") + ", " + sys.getString("country")
                        val id = it.getString("id")
                        sharedPreferences.edit().putString("CityId", id).apply()

                        view.findViewById<TextView>(R.id.address).text = address
                        view.findViewById<TextView>(R.id.updatedAt).text = updatedAtText
                        view.findViewById<TextView>(R.id.status).text =
                            weatherDescription.capitalize()
                        view.findViewById<TextView>(R.id.temp).text = temp
                        view.findViewById<TextView>(R.id.temp_min).text = tempMin
                        view.findViewById<TextView>(R.id.temp_max).text = tempMax
                        view.findViewById<TextView>(R.id.wind).text = windSpeed
                        view.findViewById<TextView>(R.id.pressure).text = pressure
                        view.findViewById<TextView>(R.id.humidity).text = humidity

                        errorText.visibility = View.GONE
                        mainContainer.visibility = View.VISIBLE

                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some error occured:$e",
                            Toast.LENGTH_LONG
                        ).show()
                        mainContainer.visibility = View.GONE
                        errorText.visibility = View.VISIBLE
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
            mainContainer.visibility = View.GONE
            loader.visibility = View.GONE
            errorText.visibility = View.VISIBLE
        }



        return view
    }
}

