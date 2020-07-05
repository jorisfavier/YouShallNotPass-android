package fr.jorisfavier.youshallnotpass

import android.app.Application
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import fr.jorisfavier.youshallnotpass.di.DaggerAppComponent
import javax.inject.Inject

class YSNPApplication : Application(), HasAndroidInjector {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent
                .builder()
                .application(this)
                .build().inject(this)
    }

    override fun androidInjector() = activityInjector
}