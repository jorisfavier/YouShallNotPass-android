package fr.jorisfavier.youshallnotpass

import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(home: MainActivity)
}