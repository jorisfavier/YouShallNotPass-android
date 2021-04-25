package fr.jorisfavier.youshallnotpass.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fr.jorisfavier.youshallnotpass.ui.auth.AuthViewModel
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillSearchViewModel
import fr.jorisfavier.youshallnotpass.ui.desktop.DesktopConnectionViewModel
import fr.jorisfavier.youshallnotpass.ui.home.HomeViewModel
import fr.jorisfavier.youshallnotpass.ui.item.ItemEditViewModel
import fr.jorisfavier.youshallnotpass.ui.search.SearchViewModel
import fr.jorisfavier.youshallnotpass.ui.settings.SettingsViewModel
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel

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
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ImportItemViewModel::class)
    abstract fun bindImportItemViewModel(importItemViewModel: ImportItemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DesktopConnectionViewModel::class)
    abstract fun bindDesktopConnectionViewModel(desktopConnectionViewModel: DesktopConnectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AutofillSearchViewModel::class)
    abstract fun bindAutofillViewModel(autofillSearchViewModel: AutofillSearchViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: MainViewModelFactory): ViewModelProvider.Factory
}