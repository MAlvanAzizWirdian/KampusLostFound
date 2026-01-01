package com.alpan.lostfound.ui.report

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.alpan.lostfound.R
import com.alpan.lostfound.databinding.ActivityReportBinding
import com.alpan.lostfound.model.Item
import com.alpan.lostfound.ui.picker.PickLocationActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private var imageUri: Uri? = null
    private var latestTmpUri: Uri? = null
    private var lat: Double = 0.0
    private var lng: Double = 0.0

    private val pickImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleImageSelection(it) }
    }

    private val takeImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri -> handleImageSelection(uri) }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImageSelection(uri: Uri) {
        imageUri = uri
        binding.imgPreview.setImageURI(uri)
        binding.imgPreview.visibility = View.VISIBLE
        binding.placeholderPickImage.visibility = View.GONE
    }

    private val pickLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            lat = data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            lng = data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            if (lat != 0.0 || lng != 0.0) {
                showMiniMap(lat, lng)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent.getStringExtra("TYPE") ?: "hilang"

        if (type.equals("hilang", ignoreCase = true)) {
            title = "Lapor Kehilangan"
            binding.locationSection.visibility = View.GONE
        } else {
            title = "Lapor Temuan"
            binding.locationSection.visibility = View.VISIBLE
        }

        binding.cardPickImage.setOnClickListener { showImageSourceDialog() }

        binding.cardPickLocation.setOnClickListener {
            val intent = Intent(this, PickLocationActivity::class.java).apply {
                putExtra("current_latitude", -7.8331118224412215)
                putExtra("current_longitude", 110.38309706321732)
            }
            pickLocationLauncher.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(this, "Harap pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (type.equals("temuan", ignoreCase = true) && (lat == 0.0 && lng == 0.0)) {
                Toast.makeText(this, "Harap pilih lokasi untuk barang temuan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadImage(type)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Buka Kamera", "Pilih dari Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> pickImageFromGallery.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val imageFile = createImageFile()
        latestTmpUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", imageFile)
        latestTmpUri?.let { uri ->
            takeImage.launch(uri)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun showMiniMap(latitude: Double, longitude: Double) {
        binding.placeholderPickLocation.visibility = View.GONE

        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(binding.miniMapContainerReport.id, mapFragment)
            .commit()

        mapFragment.getMapAsync { googleMap ->
            val position = LatLng(latitude, longitude)
            googleMap.addMarker(MarkerOptions().position(position))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isScrollGesturesEnabled = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun uploadImage(type: String) {
        imageUri?.let { uri ->
            binding.btnSubmit.isEnabled = false
            binding.btnSubmit.text = "Mengunggah..."
            MediaManager.get().upload(uri)
                .unsigned("lostfound_preset")
                .callback(object : UploadCallback {
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"].toString()
                        saveData(type, imageUrl)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        binding.btnSubmit.isEnabled = true
                        binding.btnSubmit.text = "Kirim"
                        Toast.makeText(baseContext, "Upload Gagal: ${error.description}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
        }
    }

    private fun saveData(type: String, imageUrl: String) {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        val item = Item(
            name = binding.etName.text.toString(),
            description = binding.etDesc.text.toString(),
            type = type,
            imageUrl = imageUrl,
            latitude = lat,
            longitude = lng,
            locationName = if (type.equals("temuan", ignoreCase = true)) "Lokasi di Peta" else "Lokasi tidak spesifik",
            deviceId = deviceId
        )

        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("items")
            .add(item)
            .addOnSuccessListener { 
                Toast.makeText(this, "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show()
                finish() 
            }
            .addOnFailureListener { 
                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.text = "Kirim"
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
    }
}
