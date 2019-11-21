package com.bendenen.visionai.example.screens.artisticstyletransfer.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bendenen.visionai.example.R
import com.bendenen.visionai.tflite.styletransfer.step.Style
import kotlinx.android.synthetic.main.item_style.view.*

class StyleListAdapter(
    private val callback: StyleAdapterCallback
) : ListAdapter<Style, StyleListAdapter.StyleAdapterViewHolder>(
    StyleDiffCallback()
) {

    var selectedPosition: Int = -1

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_TYPE_ADD_STYLE
        }

        return VIEW_TYPE_STYLE
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): StyleAdapterViewHolder = when (viewType) {
        VIEW_TYPE_STYLE -> createStyleViewHolder(parent)
        VIEW_TYPE_ADD_STYLE -> createAddStyleViewHolder(parent)
        else -> createStyleViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: StyleAdapterViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    private fun createStyleViewHolder(parent: ViewGroup): StyleViewHolder =
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_style,
            parent,
            false
        ).run {
            StyleViewHolder(this)
        }

    private fun createAddStyleViewHolder(parent: ViewGroup): AddStyleViewHolder =
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_add_style,
            parent,
            false
        ).run {
            AddStyleViewHolder(this)
        }

    abstract inner class StyleAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(position: Int)
    }

    inner class StyleViewHolder(view: View) : StyleAdapterViewHolder(view) {

        override fun bind(position: Int) {
            when (val item = getItem(position)) {
                is Style.AssetStyle -> {
                    val assetManager = itemView.context.assets
                    val bitmap = BitmapFactory.decodeStream(assetManager.open(item.styleFileName))
                    itemView.style_image.setImageBitmap(bitmap)
                    itemView.style_name.text = item.name
                    itemView.setOnClickListener {
                        if (selectedPosition != position) {
                            callback.onStyleClick(item)
                            val previousSelected = selectedPosition
                            selectedPosition = position
                            notifyItemChanged(previousSelected)
                            notifyItemChanged(position)
                        }

                    }
                    if (position == selectedPosition) {
                        itemView.container.setBackgroundColor(
                            itemView.context.resources.getColor(android.R.color.holo_blue_light)
                        )
                    } else {
                        itemView.container.setBackgroundColor(
                            itemView.context.resources.getColor(android.R.color.transparent)
                        )
                    }
                }
            }
        }
    }

    inner class AddStyleViewHolder(view: View) : StyleAdapterViewHolder(view) {

        override fun bind(position: Int) {
            itemView.setOnClickListener {
                callback.onAddStyleClick()
            }
        }
    }

    companion object {
        const val VIEW_TYPE_STYLE = 0
        const val VIEW_TYPE_ADD_STYLE = 1
    }
}

class StyleDiffCallback : DiffUtil.ItemCallback<Style?>() {

    override fun areItemsTheSame(oldItem: Style, newItem: Style): Boolean {

        if (oldItem.javaClass.name != newItem.javaClass.name) return false

        return when (oldItem) {
            is Style.AssetStyle -> {
                (oldItem.styleFileName == (newItem as Style.AssetStyle).styleFileName)
                    && (oldItem.name == newItem.name)
            }
            is Style.PhotoUriStyle -> {
                (oldItem.styleFileUri == (newItem as Style.PhotoUriStyle).styleFileUri)
                    && (oldItem.name == newItem.name)
            }
        }
    }

    override fun areContentsTheSame(oldItem: Style, newItem: Style): Boolean {

        if (oldItem.javaClass.name != newItem.javaClass.name) return false

        return when (oldItem) {
            is Style.AssetStyle -> {
                (oldItem.styleFileName == (newItem as Style.AssetStyle).styleFileName)
                    && (oldItem.name == newItem.name)
            }
            is Style.PhotoUriStyle -> {
                (oldItem.styleFileUri == (newItem as Style.PhotoUriStyle).styleFileUri)
                    && (oldItem.name == newItem.name)
            }
        }
    }
}

interface StyleAdapterCallback {

    fun onStyleClick(style: Style)

    fun onAddStyleClick()
}