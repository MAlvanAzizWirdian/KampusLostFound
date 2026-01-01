package com.alpan.lostfound.ui.list

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpan.lostfound.adapter.ItemAdapter
import com.alpan.lostfound.databinding.ActivityItemListBinding
import com.alpan.lostfound.model.Item
import com.alpan.lostfound.ui.detail.DetailActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects

class ItemListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemListBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.layoutManager = LinearLayoutManager(this)

        val filter = intent.getStringExtra("FILTER") ?: ""
        val isMyItemsPage = filter == "laporan_saya"

        setupToolbarAndQuery(filter)
        setupAdapter(isMyItemsPage)
        fetchData()
    }

    private fun setupToolbarAndQuery(filter: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = when (filter) {
            "laporan_saya" -> "Laporan Saya"
            "hilang" -> "Daftar Barang Hilang"
            "temuan" -> "Daftar Barang Temuan"
            else -> "Daftar Barang"
        }
    }

    private fun setupAdapter(isMyItemsPage: Boolean) {
        itemAdapter = ItemAdapter(itemList, isMyItemsPage, onItemClick = { item ->
            startActivity(
                Intent(this, DetailActivity::class.java).putExtra("ID", item.id)
            )
        }, onDeleteClick = { item ->
            showDeleteConfirmation(item)
        })
        binding.recycler.adapter = itemAdapter
    }

    private fun fetchData() {
        val filter = intent.getStringExtra("FILTER") ?: ""
        var query: Query = db.collection("items").orderBy("createdAt", Query.Direction.DESCENDING)

        when (filter) {
            "laporan_saya" -> {
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                query = query.whereEqualTo("deviceId", deviceId)
            }
            "hilang" -> {
                query = query.whereEqualTo("type", "hilang")
            }
            "temuan" -> {
                query = query.whereEqualTo("type", "temuan")
            }
        }

        query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // This is the crucial block that was missing
                Log.w("ItemListActivity", "Listen failed.", exception)
                Toast.makeText(this, "Gagal memuat data. Lihat Logcat untuk membuat indeks.", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val newItems = snapshot.toObjects<Item>()
                itemList.clear()
                itemList.addAll(newItems)
                itemAdapter.notifyDataSetChanged()

                if (itemList.isEmpty()) {
                    Toast.makeText(this, "Tidak ada data laporan.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmation(item: Item) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Laporan")
            .setMessage("Apakah Anda yakin ingin menghapus laporan '${item.name}'?")
            .setPositiveButton("Hapus") { _, _ -> deleteItem(item) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteItem(item: Item) {
        db.collection("items").document(item.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
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
