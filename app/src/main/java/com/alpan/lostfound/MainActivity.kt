package com.alpan.lostfound

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.alpan.lostfound.databinding.ActivityMainBinding
import com.alpan.lostfound.model.Item
import com.alpan.lostfound.ui.about.AboutActivity
import com.alpan.lostfound.ui.list.ItemListActivity
import com.alpan.lostfound.ui.report.ReportActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects

class MainActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnLaporKehilangan.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java).apply {
                putExtra("TYPE", "hilang")
            })
        }

        binding.btnLaporTemuan.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java).apply {
                putExtra("TYPE", "temuan")
            })
        }

        // Modern way to handle the back button press
        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                // Disable this callback and perform the default back press action
                isEnabled = false
                onBackPressed()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        loadMarkers()
    }

    private fun loadMarkers() {
        db.collection("items").addSnapshotListener { snapshot, _ ->
            mMap.clear() // Clear old markers
            snapshot?.let {
                val items = it.toObjects<Item>()
                for (item in items) {
                    val pos = LatLng(item.latitude, item.longitude)
                    // FIX: Use case-insensitive comparison
                    val color = if (item.type.equals("hilang", ignoreCase = true)) {
                        BitmapDescriptorFactory.HUE_RED
                    } else {
                        BitmapDescriptorFactory.HUE_GREEN
                    }
                    mMap.addMarker(
                        MarkerOptions().position(pos).title(item.name).icon(BitmapDescriptorFactory.defaultMarker(color))
                    )
                }
                if (items.isNotEmpty()) {
                    val firstItemPos = LatLng(items.first().latitude, items.first().longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstItemPos, 12f))
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_beranda -> {
                // Already on home screen
            }
            R.id.nav_laporan_saya -> {
                startActivity(Intent(this, ItemListActivity::class.java).apply {
                    putExtra("FILTER", "laporan_saya")
                })
            }
            R.id.nav_barang_hilang -> {
                startActivity(Intent(this, ItemListActivity::class.java).apply {
                    putExtra("FILTER", "hilang")
                })
            }
            R.id.nav_barang_temuan -> {
                startActivity(Intent(this, ItemListActivity::class.java).apply {
                    putExtra("FILTER", "temuan")
                })
            }
            R.id.nav_tentang -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
