package com.alpan.lostfound.ui.picker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.alpan.lostfound.R
import com.alpan.lostfound.databinding.ActivityPickLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class PickLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityPickLocationBinding
    private lateinit var mMap: GoogleMap
    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarPicker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_picker_container) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSelectLocation.setOnClickListener {
            selectedLatLng?.let {
                val resultIntent = Intent()
                resultIntent.putExtra("latitude", it.latitude)
                resultIntent.putExtra("longitude", it.longitude)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Enable Zoom UI
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        // ** THE FIX **
        // Add padding to the bottom of the map. This pushes the Google logo and zoom controls up.
        // The value is in pixels, so we calculate it from dp.
        val density = resources.displayMetrics.density
        val paddingInPx = (80 * density).toInt() // 80dp padding from the bottom
        mMap.setPadding(0, 0, 0, paddingInPx)

        val currentLat = intent.getDoubleExtra("current_latitude", -6.593) // Default IPB
        val currentLng = intent.getDoubleExtra("current_longitude", 106.789)

        val startLocation = LatLng(currentLat, currentLng)
        selectedLatLng = startLocation

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 16f))

        mMap.setOnCameraIdleListener {
            selectedLatLng = mMap.cameraPosition.target
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
