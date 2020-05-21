package fr.jorisfavier.youshallnotpass

import dagger.Component
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import fr.jorisfavier.youshallnotpass.ui.search.SearchFragment
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(auth: AuthActivity)
    fun inject(search: SearchFragment)
    fun inject(itemRepository: IItemRepository)
}
