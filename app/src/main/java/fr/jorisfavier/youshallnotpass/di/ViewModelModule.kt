package fr.jorisfavier.youshallnotpass.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fr.jorisfavier.youshallnotpass.ui.item.ItemEditViewModel
import fr.jorisfavier.youshallnotpass.ui.search.SearchViewModel

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(searchViewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ItemEditViewModel::class)
    abstract fun bindItemEditViewModel(itemEditViewModel: ItemEditViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: MainViewModelFactory): ViewModelProvider.Factory
}