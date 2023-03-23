package com.company.openweatherapplication.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.company.openweatherapplication.Adapter.Adapter
import com.company.openweatherapplication.R
import com.company.openweatherapplication.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var viewmodel: MainViewModel
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    private var locationManager: LocationManager? = null
    private var finalCurrentLocation: String? = null
    private var cnt: Int = 0
    private var latitude: String? = null
    private var longitude: String? = null
    private val locationArray = arrayListOf("London","Singapore","New York","Mumbai","Delhi","Sydney")
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isLocationPermissionGranted()
        supportActionBar?.hide();

        GET = getSharedPreferences(packageName, MODE_PRIVATE)
        SET = GET.edit()

        viewmodel = ViewModelProvider(this)[MainViewModel::class.java]
        val currentLocation = getCurrentLocation()
        
        if (currentLocation != null) {
            for (element in currentLocation) {
                if (cnt > 10) {
                    finalCurrentLocation += element
                }
                cnt += 1
            }
        }
        if (finalCurrentLocation != null) {
            Log.d("vedant", finalCurrentLocation!!)
        }
        val cName = GET.getString("cityName", finalCurrentLocation)?.toLowerCase(Locale.ROOT)
        edt_city_name.setText(cName)
        viewmodel.refreshData(cName!!)

        getLiveData()
        
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = Adapter(this, locationArray, this,this)
        rv.adapter = adapter


        swipe_refresh_layout.setOnRefreshListener {
            ll_data.visibility = View.GONE
            tv_error.visibility = View.GONE
            pb_loading.visibility = View.GONE
            adapter.notifyDataSetChanged()
            val cityName = GET.getString("cityName", cName)?.toLowerCase(Locale.ROOT)
            edt_city_name.setText(cityName)
            viewmodel.refreshData(cityName!!)
            swipe_refresh_layout.isRefreshing = false
        }

        img_search_city.setOnClickListener {
            val cityName = edt_city_name.text.toString()
            SET.putString("cityName", cityName)
            SET.apply()
            viewmodel.refreshData(cityName)
            getLiveData()
            Log.i(TAG, "onCreate: " + cityName)
        }

    }

    private fun getCurrentLocation(): String? {
        var cityName: String? = null
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS()
        } else {
            val myLocation = getLastKnownLocation()
            if (myLocation != null) {
                val lat = myLocation.latitude
                val longi = myLocation.longitude
                latitude = lat.toString()
                longitude = longi.toString()
                val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                var addresses: List<Address>? = null
                addresses = try {
                    geocoder.getFromLocation(lat, longi, 1)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
                cityName = addresses?.get(0)?.getAddressLine(0).toString()
                val stateName = addresses?.get(0)?.getAddressLine(1)
                val countryName = addresses?.get(0)?.getAddressLine(2)
//              Log.d("vedant",latitude.toString())

            }
        }
        return cityName
    }

    private fun getLiveData() {

        viewmodel.weather_data.observe(this, Observer { data ->
            data?.let {
                ll_data.visibility = View.VISIBLE

                tv_city_code.text = data.sys.country
                tv_city_name.text = data.name

                Glide.with(this)
                    .load("https://openweathermap.org/img/wn/" + data.weather[0].icon + "@2x.png")
                    .into(img_weather_pictures)

                tv_degree.text = data.main.temp.toString() + "°C"

                tv_humidity.text = data.main.humidity.toString() + "%"
                tv_wind_speed.text = data.wind.speed.toString() + "m/s"
                tv_lat.text = data.coord.lat.toString()
                tv_lon.text = data.coord.lon.toString()

            }
        })

        viewmodel.weather_error.observe(this, Observer { error ->
            error?.let {
                if (error) {
                    tv_error.visibility = View.VISIBLE
                    pb_loading.visibility = View.GONE
                    ll_data.visibility = View.GONE
                } else {
                    tv_error.visibility = View.GONE
                }
            }
        })

        viewmodel.weather_loading.observe(this, Observer { loading ->
            loading?.let {
                if (loading) {
                    pb_loading.visibility = View.VISIBLE
                    tv_error.visibility = View.GONE
                    ll_data.visibility = View.GONE
                } else {
                    pb_loading.visibility = View.GONE
                }
            }
        })

    }

    private fun OnGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton(
            "Yes"
        ) { _: DialogInterface?, _: Int ->
            startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            )
        }.setNegativeButton(
            "No"
        ) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
            false
        } else {
            true
        }
    }

    private fun getLastKnownLocation(): Location? {
        locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationManager!!.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

            }
            val l: Location =
                locationManager!!.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        return bestLocation
    }

}