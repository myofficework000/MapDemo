package com.example.googlemapdemo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.googlemapdemo.R
import com.example.googlemapdemo.databinding.ActivityFindMyCurrentLocationBinding
import com.example.googlemapdemo.util.hasPermission
import com.example.googlemapdemo.util.requirePermissionWithRationale
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar


class FindMyCurrentLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var locationRequest: LocationRequest? = null
    private lateinit var binding: ActivityFindMyCurrentLocationBinding
    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }
    private var marker: Marker? = null
    private var currentUpdatedLocation = LatLng(0.0, 0.0)
    private var cancellationTokenSource = CancellationTokenSource()
    private lateinit var locationCallback: LocationCallback
    private val snackBar by lazy {
        Snackbar.make(binding.container, getString(R.string.get_location), Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.ok)) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQ_CODE
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindMyCurrentLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        requestForPermission()
        registerForCallback()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        locationRequest?.let {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest as LocationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun registerForCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    currentUpdatedLocation = LatLng(location.latitude, location.longitude)
                    Toast.makeText(
                        this@FindMyCurrentLocationActivity,
                        "Updated ${currentUpdatedLocation.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (this@FindMyCurrentLocationActivity::map.isInitialized) {
                        marker?.position = currentUpdatedLocation
                        map.addMarker(
                            MarkerOptions().position(currentUpdatedLocation)
                                .title("Marker in current location")
                                .icon(bitmapFromVector(R.drawable.baseline_add_location_24))
                        )
                        val cameraPosition = CameraPosition(currentUpdatedLocation, 15.5f, 0f, 0f)
                        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
                        map.animateCamera(cameraUpdate)
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.apply {
            mapType = GoogleMap.MAP_TYPE_TERRAIN
            marker = addMarker(
                MarkerOptions().position(currentUpdatedLocation).title("Marker in current location")
                    .icon(bitmapFromVector(R.drawable.baseline_add_location_24))
            )
            marker?.position = currentUpdatedLocation
            moveCamera(CameraUpdateFactory.newLatLngZoom(currentUpdatedLocation, 15.5f))
        }
    }

    fun createLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun requestForPermission() {
        val permission = applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission) {
            requestForCurrentLocation()
        } else {
            requirePermissionWithRationale(
                Manifest.permission.ACCESS_FINE_LOCATION,
                REQ_CODE,
                snackBar
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestForCurrentLocation() {
        if (applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val currentTask: Task<Location> = fusedLocationProviderClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )

            currentTask.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful) {
                    task.result?.let {
                        currentUpdatedLocation = LatLng(it.latitude, it.longitude)
                        Toast.makeText(
                            this@FindMyCurrentLocationActivity,
                            "Updated ${currentUpdatedLocation.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (this::map.isInitialized) {
                            marker?.position = currentUpdatedLocation
                            map.addMarker(
                                MarkerOptions().position(currentUpdatedLocation)
                                    .title("Marker in current location")
                                    .icon(bitmapFromVector(R.drawable.baseline_add_location_24))
                            )
                            val cameraPosition =
                                CameraPosition(currentUpdatedLocation, 15.5f, 0f, 0f)
                            val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
                            map.animateCamera(cameraUpdate)
                        }
                    }
                } else {
                    Log.i("tag", "Bad fetch")
                }
            }
        }
    }

    private fun updateLocationOnUI(location: Location) = with(location) {
        val intent = Intent(this@FindMyCurrentLocationActivity, MapsActivity::class.java)
        intent.putExtra("lat", latitude)
        intent.putExtra("lng", longitude)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQ_CODE) {
            when {
                grantResults.isEmpty() -> Log.i("tag", "user cancelled")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    Snackbar.make(
                        binding.container,
                        getString(R.string.approved),
                        Snackbar.LENGTH_LONG
                    ).show()
            }
        } else {
            Snackbar.make(
                binding.container,
                getString(R.string.pemission_denied),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.settings)) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", "com.example.googlemapdemo", null)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }.show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
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

    companion object {
        private const val REQ_CODE = 100
    }


}