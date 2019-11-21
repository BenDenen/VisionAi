package com.bendenen.visionai.example.screens.artisticstyletransfer.adapters

import android.graphics.BlendMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bendenen.visionai.example.R
import kotlinx.android.synthetic.main.item_blend_mode.view.*

class BlendModeAdapter(
    private val callback: BlendModeAdapterCallback
) : ListAdapter<BlendModeItem, BlendModeAdapter.BlendModeViewHolder>(
    BlendModeDiffCallback()
) {

    var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlendModeViewHolder =
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_blend_mode,
            parent,
            false
        ).run {
            BlendModeViewHolder(this)
        }

    override fun onBindViewHolder(holder: BlendModeViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class BlendModeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val item = getItem(position)



            itemView.blend_title.text = item.name
            itemView.setOnClickListener {
                if (selectedPosition != position) {
                    callback.onBlendModeClick(item.blendMode)
                    val previousSelected = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(previousSelected)
                    notifyItemChanged(position)
                }
            }
            if (position == selectedPosition) {
                itemView.blend_title.setBackgroundColor(
                    itemView.context.resources.getColor(android.R.color.holo_blue_light)
                )
            } else {
                itemView.blend_title.setBackgroundColor(
                    itemView.context.resources.getColor(android.R.color.darker_gray)
                )
            }
        }
    }
}

data class BlendModeItem(
    val blendMode: BlendMode?,
    val name: String
)

class BlendModeDiffCallback : DiffUtil.ItemCallback<BlendModeItem>() {

    override fun areItemsTheSame(oldItem: BlendModeItem, newItem: BlendModeItem): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: BlendModeItem, newItem: BlendModeItem): Boolean =
        oldItem == newItem
}

interface BlendModeAdapterCallback {
    fun onBlendModeClick(blendMode: BlendMode?)
}