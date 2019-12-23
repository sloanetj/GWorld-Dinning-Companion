package com.example.maptest


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.jetbrains.anko.doAsync


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var confirm: Button
    private var markerID: String? = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences: SharedPreferences = getSharedPreferences("SAVED_INFO", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        confirm = findViewById(R.id.confirm)
        confirm.isEnabled = false

        if (preferences.getBoolean("FIRST_LAUNCH",true)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Instructions")
            builder.setMessage("Long press on the map to see the restaurants around that area. Select a marker to view that restaurant's reviews. The blue markers indicate the restaurants that accept GWolrd ")
            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, whichButton ->
                val editor = preferences.edit()
                editor.putBoolean("FIRST_LAUNCH", false)
                editor.apply()
            })
            builder.show()
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in GW and move the camera
        val gwuCoords = LatLng(38.898365, -77.046753)
        val zoom = 10.0f
        val circleRadius = intent.getStringExtra("SEARCH_AREA")

        // get yelp rating of GWU
        doAsync {
            val networkManager = NetworkManager()
            val gwMarker = networkManager.retrieveGWRating(this@MapsActivity)

            runOnUiThread{
                mMap.addMarker(MarkerOptions()
                    .snippet(gwMarker.id)
                    .position(gwuCoords)
                    .title("GWU:\tYelp rating: (${gwMarker.rating})"))
                    .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            }
        }


        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(gwuCoords, zoom)
        )

        mMap.setOnMapLongClickListener { coords ->
            //Log.e("MapsActivity", "Long Press Detected")

            mMap.clear()
            placeMarkers(coords)

            // make circle
            val circle = mMap.addCircle(CircleOptions().center(coords).radius(circleRadius.toDouble()).strokeColor(Color.RED))

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 12.0f))
        }

        mMap.setOnMarkerClickListener{marker ->

            markerID = marker.snippet
            //marker.snippet = ""
            val name = marker.title

            updateConfirmButton("See reviews for $name")

            false

        }


        confirm.setOnClickListener {

            val intent = Intent(this, YelpActivity::class.java)
            intent.putExtra("ID", markerID)
            startActivity(intent)
        }
    }

    fun placeMarkers(latLng: LatLng) {
        doAsync {

            // get the food categories and radius from the intent
            val burgers = intent.getBooleanExtra("BURGERS", false)
            val pizza = intent.getBooleanExtra("PIZZA", false)
            val comfort = intent.getBooleanExtra("CHINESE", false)
            val sushi = intent.getBooleanExtra("SUSHI", false)
            val fast = intent.getBooleanExtra("INDIAN", false)
            val salad = intent.getBooleanExtra("SALAD", false)
            val foodCategories = booleanArrayOf(burgers, pizza, comfort, sushi, fast, salad)
            val radius = intent.getStringExtra("SEARCH_AREA")

            // have the network manager get all the markers' coordinates & info
            val networkManager = NetworkManager()
            val yelpMarkers = networkManager.retrieveYelpMarkers(this@MapsActivity, latLng, radius, foodCategories)

            // display markers on map
            runOnUiThread{

                // handle no-results error
                if (yelpMarkers.isEmpty()){
                    //Update button text
                    updateConfirmButton("No Results")

                }
                // handle network error
                else if (yelpMarkers.get(0).id.equals("0")){
                    //Update button text
                    updateConfirmButton("Network error")
                }
                else {

                    updateConfirmButton("Choose Location")
                    for (i in 0 until yelpMarkers.size) {
                        val coords = yelpMarkers.get(i).latLng
                        val name = yelpMarkers.get(i).name
                        val rating = yelpMarkers.get(i).rating
                        val yelpID = yelpMarkers.get(i).id

                        if(yelpMarkers.get(i).isGWorld) {
                            mMap.addMarker(MarkerOptions()
                                .snippet(yelpID)
                                .position(coords)
                                .title("$name: $rating Yelp rating"))
                                .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                        }else {
                            mMap.addMarker(MarkerOptions()
                                .snippet(yelpID)
                                .position(coords)
                                .title("$name: $rating Yelp rating"))
                        }
                    }
                }

                //mMap.addMarker(MarkerOptions().position(coords).title("My Marker"))
            }
        }
    }

    private fun updateConfirmButton(str: String) {

        // update if error
        if (str.equals("No Results") || str.equals("Network Error") || str.equals("Choose Location")){
            val redColor = ContextCompat.getColor(
                this, R.color.buttonRed
            )
            confirm.setBackgroundColor(redColor)

            val xIcon = ContextCompat.getDrawable(
                this,R.drawable.ic_clear_white
            )
            confirm.setCompoundDrawablesWithIntrinsicBounds(xIcon, null, null, null)

            confirm.text = str
            confirm.isEnabled = false
        }
        else {
            // Update the button color -- need to load the color from resources first
            val greenColor = ContextCompat.getColor(
                this, R.color.buttonGreen
            )
            val checkIcon = ContextCompat.getDrawable(
                this, R.drawable.ic_check_white
            )
            confirm.setBackgroundColor(greenColor)

            // Update the left-aligned icon
            confirm.setCompoundDrawablesWithIntrinsicBounds(checkIcon, null, null, null)

            //Update button text
            confirm.text = str
            confirm.isEnabled = true
        }
    }
}
