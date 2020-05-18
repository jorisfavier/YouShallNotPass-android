package fr.jorisfavier.youshallnotpass

import dagger.Component
import fr.jorisfavier.youshallnotpass.features.auth.AuthActivity
import fr.jorisfavier.youshallnotpass.features.search.SearchFragment
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(auth: AuthActivity)
    fun inject(search: SearchFragment)
    fun inject(itemManager: IItemManager)
}
