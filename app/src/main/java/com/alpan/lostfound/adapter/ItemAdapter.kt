package com.alpan.lostfound.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alpan.lostfound.R
import com.alpan.lostfound.databinding.ItemRowBinding
import com.alpan.lostfound.model.Item
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class ItemAdapter(
    private val list: List<Item>,
    private val isMyItemsPage: Boolean,
    private val onItemClick: (Item) -> Unit,
    private val onDeleteClick: ((Item) -> Unit)? = null
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvNameRow.text = item.name
        holder.binding.tvLocationRow.text = item.locationName

        item.createdAt?.let {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            holder.binding.tvDateRow.text = sdf.format(it)
        }

        if (item.imageUrl.isNotEmpty()) {
            Picasso.get().load(item.imageUrl).placeholder(R.drawable.ic_image).into(holder.binding.imgItemRow)
        } else {
            holder.binding.imgItemRow.setImageResource(R.drawable.ic_image)
        }

        if (isMyItemsPage) {
            holder.binding.btnDeleteItem.visibility = View.VISIBLE
            holder.binding.btnDeleteItem.setOnClickListener { onDeleteClick?.invoke(item) }
        } else {
            holder.binding.btnDeleteItem.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}
