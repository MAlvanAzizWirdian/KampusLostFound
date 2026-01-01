package com.alpan.lostfound.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alpan.lostfound.R
import com.alpan.lostfound.databinding.ActivityDetailBinding
import com.alpan.lostfound.model.Item
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentItem: Item? = null
    private var isMyItem = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getStringExtra("ID") ?: return

        db.collection("items").document(id).get().addOnSuccessListener { snapshot ->
            snapshot?.toObject(Item::class.java)?.let { item ->
                currentItem = item
                val myDeviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                isMyItem = item.deviceId == myDeviceId
                invalidateOptionsMenu() // This will redraw the menu

                title = item.name
                binding.tvDescDetail.text = item.description
                Picasso.get().load(item.imageUrl).into(binding.imgDetail)

                setupMiniMap(item.latitude, item.longitude)

                if (isMyItem) {
                    binding.btnWhatsapp.visibility = View.GONE
                } else {
                    binding.btnWhatsapp.setOnClickListener {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("https://wa.me/6281234567890?text=Saya%20tertarik%20dengan%20barang%20Anda:%20${item.name}")
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isMyItem) {
            menuInflater.inflate(R.menu.detail_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_delete -> {
                showDeleteConfirmation()
                return true
            }
            // TODO: Handle Edit
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Laporan")
            .setMessage("Apakah Anda yakin ingin menghapus laporan ini?")
            .setPositiveButton("Hapus") { _, _ -> deleteItem() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteItem() {
        currentItem?.id?.let {
            db.collection("items").document(it).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menghapus laporan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupMiniMap(lat: Double, lng: Double) {
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(binding.miniMapContainer.id, mapFragment)
            .commit()

        mapFragment.getMapAsync { googleMap ->
            val position = LatLng(lat, lng)
            googleMap.addMarker(MarkerOptions().position(position))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
            // Enable Zoom Controls but disable other gestures
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isScrollGesturesEnabled = false
        }
    }
}
