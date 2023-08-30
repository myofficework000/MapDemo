package com.example.googlemapdemo.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.googlemapdemo.R
import com.example.googlemapdemo.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Add a marker in Sydney and move the camera
        val myLocation = LatLng(
            intent.getDoubleExtra("lat", 0.0), intent.getDoubleExtra("lng", 0.0)
        )
        val sydney = LatLng(-34.0, 151.0)
        val london = LatLng(51.5, -0.12)
        val home = LatLng(51.515968, -0.237460)
        val office = LatLng(51.525990, -0.087710)
        val covidAffected = LatLng(51.460930, -0.116020)
        val place1 = LatLng(51.550331, -0.292560)
        val place2 = LatLng(51.566080, -0.220110)
        val place3 = LatLng(51.513350, -0.304210)


        map.apply {
            mapType = GoogleMap.MAP_TYPE_TERRAIN
            addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            addMarker(MarkerOptions().position(london).title("Marker in London"))
            addMarker(MarkerOptions().position(place1).title("Marker in Wembley"))
            addMarker(MarkerOptions().position(place2).title("Marker in Cricklewood"))
            addMarker(MarkerOptions().position(place3).title("Marker in ealing"))
            addMarker(
                MarkerOptions().position(home).title("Home")
                    .icon(bitmapFromVector(R.drawable.baseline_home_24))
            )
            addMarker(
                MarkerOptions().position(myLocation).title("Marker in current location")
                    .icon(bitmapFromVector(R.drawable.baseline_add_location_24))
            )
            addMarker(
                MarkerOptions().position(office).title("Office")
                    .icon(bitmapFromVector(R.drawable.baseline_business_center_24))
            )
            addPolyline(
                PolylineOptions().add(home).add(office).color(Color.CYAN).geodesic(true)
                    .visible(true).width(10.0f)
            )
            addCircle(
                CircleOptions().radius(1000.00).center(covidAffected).fillColor(Color.RED)
                    .strokeWidth(2.0f).strokeColor(Color.BLACK)
            )
            addPolyline(
                PolylineOptions().add(place1).add(place2).add(place3).add(place1)
                    .color(Color.MAGENTA).geodesic(true)
                    .visible(true).width(15.0f)
            )
            moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15.0f))
        }
    }

    private fun bitmapFromVector(vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)!!.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}