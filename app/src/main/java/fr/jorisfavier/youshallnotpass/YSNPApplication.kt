package fr.jorisfavier.youshallnotpass

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import fr.jorisfavier.youshallnotpass.utils.ReleaseLoggingTree
import timber.log.Timber
import timber.log.Timber.DebugTree


@HiltAndroidApp
class YSNPApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(ReleaseLoggingTree())
        }
    }
}