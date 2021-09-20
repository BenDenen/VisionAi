package com.bendenen.visionai.example.screens.main

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bendenen.visionai.example.screens.artisticstyletransfer.ArtisticStyleTransferActivity
import com.bendenen.visionai.example.screens.bodysegmentation.BodySegmentationActivity
import com.bendenen.visionai.example.screens.main.ui.MainScreenLayout
import com.bendenen.visionai.example.screens.main.viewmodel.MainViewModel
import com.bendenen.visionai.example.ui.AppTheme
import org.koin.androidx.scope.currentScope

class MainActivity : AppCompatActivity() {

    private val viewModel by currentScope.inject<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainScreenLayout(
                    { viewModel.requestStyleTransfer() },
                    { viewModel.requestSegmentation() }
                )
            }

        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (!allPermissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                getRequiredPermissions(),
                PERMISSIONS_REQUEST_CODE
            )
        }

        viewModel.requestStyleTransferEvent.observe(this, Observer {
            startActivity(ArtisticStyleTransferActivity.getStartIntent(this))
        })

        viewModel.requestSegmentationEvent.observe(this, Observer {
            startActivity(BodySegmentationActivity.getStartIntent(this))
        })
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
