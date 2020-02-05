package com.bendenen.visionai.example.screens.main

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.screens.artisticstyletransfer.ArtisticStyleTransferActivity
import com.bendenen.visionai.example.screens.bodysegmentation.BodySegmentationActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!allPermissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                getRequiredPermissions(),
                PERMISSIONS_REQUEST_CODE
            )
        }
        request_style_transfer_button.setOnClickListener {
            startActivity(ArtisticStyleTransferActivity.getStartIntent(this))
        }
        request_body_segmentation_button.setOnClickListener {
            startActivity(BodySegmentationActivity.getStartIntent(this))
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun getRequiredPermissions(): Array<String> {
        return try {
            val info = packageManager?.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val ps = info!!.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                emptyArray()
            }
        } catch (e: Exception) {
            emptyArray()
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1
    }
}
