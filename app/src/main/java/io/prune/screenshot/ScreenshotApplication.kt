package io.prune.screenshot

import android.app.Application
import io.prune.screenshot.di.AppContainer

class ScreenshotApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

