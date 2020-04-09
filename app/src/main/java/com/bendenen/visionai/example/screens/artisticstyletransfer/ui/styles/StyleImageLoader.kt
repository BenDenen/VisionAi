package com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.bendenen.visionai.tflite.styletransfer.step.Style
import java.io.FileNotFoundException
import java.io.IOException

interface StyleImageLoader {

    fun loadStyleImage(style: Style): Bitmap?

    class Impl(val context: Context) : StyleImageLoader {
        override fun loadStyleImage(style: Style): Bitmap? = when (style) {
            is Style.AssetStyle -> BitmapFactory.decodeStream(context.assets.open(style.styleFileName))
            is Style.PhotoUriStyle -> getThumbnail(style.styleFileUri)
        }

        @Throws(FileNotFoundException::class, IOException::class)
        fun getThumbnail(uri: Uri?): Bitmap? {
            var input = uri?.let { context.contentResolver.openInputStream(it) }
            val onlyBoundsOptions = BitmapFactory.Options()
            onlyBoundsOptions.inJustDecodeBounds = true
            onlyBoundsOptions.inDither = true //optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
            input?.close()
            if (onlyBoundsOptions.outWidth == -1 || onlyBoundsOptions.outHeight == -1) {
                return null
            }
            val originalSize =
                if (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) onlyBoundsOptions.outHeight else onlyBoundsOptions.outWidth
            val ratio = if (originalSize > THUMBNAIL_SIZE) {
                originalSize.toDouble() / THUMBNAIL_SIZE
            } else {
                1.0
            }
            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio)
            bitmapOptions.inDither = true //optional
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //
            input = uri?.let { context.contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
            input?.close()
            return bitmap
        }

        private fun getPowerOfTwoForSampleRatio(ratio: Double): Int {
            val k = Integer.highestOneBit(Math.floor(ratio).toInt())
            return if (k == 0) 1 else k
        }

        companion object {
            const val THUMBNAIL_SIZE = 140
        }
    }
}