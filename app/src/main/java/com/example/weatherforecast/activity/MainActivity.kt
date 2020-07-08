package com.example.weatherforecast.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.weatherforecast.R
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.sort_radio_button.*
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import kotlinx.android.synthetic.main.sort_radio_button.view.celsius

class MainActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    lateinit var tabs: TabLayout
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var latitude: String
    lateinit var longitude: String
    lateinit var sharedPreferences: SharedPreferences
    lateinit var radioButtonView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewPager = findViewById(R.id.viewPager)
        tabs = findViewById(R.id.tabs);
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_location), Context.MODE_PRIVATE)


        val fragmentAdapter =
            com.example.weatherforecast.adapter.PagerAdapter(supportFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabs.setupWithViewPager(viewPager)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()
    }

    @SuppressLint("MissingPermission", "CommitPrefEdits")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        sharedPreferences.edit().putString("latitude", location.latitude.toString())
                            .apply()
                        sharedPreferences.edit()
                            .putString("longitude", location.longitude.toString()).apply()
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        @SuppressLint("CommitPrefEdits")
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            sharedPreferences.edit().putString("latitude", mLastLocation.latitude.toString())
            sharedPreferences.edit().putString("longitude", mLastLocation.longitude.toString())
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        when (id) {

            R.id.sort -> {
                radioButtonView = View.inflate(
                    this,
                    R.layout.sort_radio_button,
                    null
                )//radiobutton view for sorting display
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Hi ${sharedPreferences.getString("UserName", "User")}, Select Unit")
                    .setView(radioButtonView)
                    .setPositiveButton("OK") { text, listener ->
                        if (radioButtonView.celsius.isChecked) {
                            sharedPreferences.edit().putString("Unit", "metric").apply()
                            radioButtonView.celsius.isChecked
                            refreshApp()
                        }
                        if (radioButtonView.farenthite.isChecked) {
                            sharedPreferences.edit().putString("Unit", "imperial").apply()
                            radioButtonView.farenthite.isChecked
                            refreshApp()
                        }
                    }
                    .setNegativeButton("CANCEL") { text, listener ->

                    }
                    .create()
                    .show()
            }
            R.id.logOut -> {
                sharedPreferences.edit().putBoolean("isLogged", false).apply()
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun refreshApp() {
        val intent = Intent(this@MainActivity, MainActivity::class.java)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}
