package com.bendenen.tfliteexample.selectstyle

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bendenen.tfliteexample.R
import com.bendenen.visionai.tflite.styletransfer.Gallery
import com.bendenen.visionai.tflite.styletransfer.Style
import kotlinx.android.synthetic.main.item_style.view.*

class StyleListAdapter(
    private val styleList:List<Gallery>,
    private val callback: (position: Int) -> Unit
) : RecyclerView.Adapter<StyleListAdapter.StyleViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): StyleViewHolder = LayoutInflater.from(parent.context).inflate(
        R.layout.item_style,
        parent,
        false
    ).run {
        StyleViewHolder(this)
    }

    override fun getItemCount(): Int = styleList.size

    override fun onBindViewHolder(
        holder: StyleViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    inner class StyleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val galleryItem = styleList[position]
            when (val style = galleryItem.style) {
                is Style.AssetStyle -> {
                    val assetManager = itemView.context.assets
                    val bitmap = BitmapFactory.decodeStream(assetManager.open(style.styleFileName))
                    itemView.style_image.setImageBitmap(bitmap)
                    itemView.style_name.text = galleryItem.styleName
                    itemView.setOnClickListener {
                        callback.invoke(position)
                    }
                }
            }
        }
    }
}
