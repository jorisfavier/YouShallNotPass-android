package fr.jorisfavier.youshallnotpass

import dagger.Component
import fr.jorisfavier.youshallnotpass.features.search.SearchActivity
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(home: MainActivity)
    fun inject(search: SearchActivity)
    fun inject(itemManager: IItemManager)
}