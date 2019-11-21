package com.bendenen.visionai.example

import android.app.Application
import com.bendenen.visionai.example.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class VisionAiSampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Koin Android logger
            androidLogger()
            //inject Android context
            androidContext(this@VisionAiSampleApp)
            // use modules
            modules(appModule)
        }
    }
}