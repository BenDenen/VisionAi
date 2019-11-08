package com.bendenen.tfliteexample.selectstyle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bendenen.tfliteexample.R
import com.bendenen.visionai.tflite.styletransfer.Gallery
import com.bendenen.visionai.tflite.styletransfer.Style
import kotlinx.android.synthetic.main.activity_select_style.*

class SelectStyleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_style)

        style_list.layoutManager = GridLayoutManager(this, 3)
        style_list.adapter = StyleListAdapter (Gallery.values().asList()) { position ->

            setResult(Activity.RESULT_OK, Intent().also {
                it.putExtra(SELECTED_STYLE_POSITION_ARG, position)
            })
            finish()
        }
    }

    companion object {

        const val SELECTED_STYLE_POSITION_ARG = "arg.selected.style.position"
        const val NEW_STYLE_PATH_ARG = "arg.new.style.path"

        fun getStartIntent(context: Context): Intent =
            Intent(context, SelectStyleActivity::class.java)

        fun getStyleFromData(intent: Intent): Style? {
            if(intent.hasExtra(SELECTED_STYLE_POSITION_ARG)) {
                val position = intent.getIntExtra(SELECTED_STYLE_POSITION_ARG,0)
                return Gallery.values().asList()[position].style
            }
            return null
        }
    }
}