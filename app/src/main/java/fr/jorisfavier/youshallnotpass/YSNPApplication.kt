package fr.jorisfavier.youshallnotpass

import android.app.Application

class YSNPApplication: Application() {
    companion object {
        var currentInstance: YSNPApplication? = null
    }

    var appComponent: AppComponent? = null

    override fun onCreate() {
        super.onCreate()
        currentInstance = this
        appComponent = DaggerAppComponent
                            .builder()
                            .appModule(AppModule(this))
                            .build()
    }
}