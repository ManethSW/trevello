package com.example.trevello

import android.app.Activity
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ImageCarouselAdapter(private val images: MutableList<Bitmap>) : RecyclerView.Adapter<ImageCarouselAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val deleteButton: LinearLayout = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.carousel_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageBitmap(images[position])
        holder.deleteButton.setOnClickListener {
            images.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, images.size)
            // Update the visibility of the "No images selected" text
            val tvNoImages: TextView = (holder.itemView.context as Activity).findViewById(R.id.tvNoImages)
            if (images.isEmpty()) {
                tvNoImages.visibility = View.VISIBLE
            } else {
                tvNoImages.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = images.size
}