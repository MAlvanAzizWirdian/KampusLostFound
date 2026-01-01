package com.alpan.lostfound.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alpan.lostfound.databinding.ItemReportBinding
import com.alpan.lostfound.model.Report
import com.squareup.picasso.Picasso

class ReportAdapter(
    private val list: List<Report>,
    private val onMapClick: (String) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.txtTitle.text = item.title
        holder.binding.txtLocation.text = item.location

        Picasso.get()
            .load(item.imageUrl)
            .into(holder.binding.imgItem)

        holder.binding.btnMap.setOnClickListener {
            onMapClick(item.location)
        }
    }

    override fun getItemCount() = list.size
}
