package com.bendenen.visionai.example.selectstyle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bendenen.visionai.example.R
import com.bendenen.visionai.tflite.styletransfer.step.Gallery
import com.bendenen.visionai.tflite.styletransfer.step.Style
import kotlinx.android.synthetic.main.activity_select_style.*

class SelectStyleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_style)

        style_list.layoutManager = GridLayoutManager(this, 3)
        style_list.adapter =
            StyleListAdapter(Gallery.values().asList()) { position ->

                setResult(Activity.RESULT_OK, Intent().also {
                    it.putExtra(
                        SELECTED_STYLE_POSITION_ARG,
                        position
                    )
                })
                finish()
            }

        get_custom_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent,
                PICK_IMAGE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val selectedUri = data.data
                setResult(Activity.RESULT_OK, Intent().also {
                    it.putExtra(NEW_STYLE_URI_ARG, selectedUri)
                })
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        private const val PICK_IMAGE_REQUEST_CODE = 1

        private const val SELECTED_STYLE_POSITION_ARG = "arg.selected.style.position"
        private const val NEW_STYLE_URI_ARG = "arg.new.style.uri"

        fun getStartIntent(context: Context): Intent =
            Intent(context, SelectStyleActivity::class.java)

        fun getStyleFromData(intent: Intent): Style? {
            if (intent.hasExtra(SELECTED_STYLE_POSITION_ARG)) {
                val position = intent.getIntExtra(SELECTED_STYLE_POSITION_ARG, 0)
                return Gallery.values().asList()[position].style
            }
            if (intent.hasExtra(NEW_STYLE_URI_ARG)) {
                val selectedUri = intent.getParcelableExtra<Uri>(NEW_STYLE_URI_ARG)
                selectedUri?.let {
                    return Style.PhotoUriStyle(selectedUri)
                }
            }
            return null
        }
    }
}