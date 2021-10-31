package fr.jorisfavier.youshallnotpass.utils

import android.util.Log
import io.sentry.Sentry
import timber.log.Timber
import java.io.IOException

class ReleaseLoggingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority == Log.ERROR) {
            if (throwable != null && throwable !is IOException) {
                Sentry.captureMessage(message)
                Sentry.captureException(throwable)
            }
        } else {
            return
        }
    }
}