package fr.jorisfavier.youshallnotpass.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import fr.jorisfavier.youshallnotpass.ui.home.HomeActivity
import fr.jorisfavier.youshallnotpass.ui.item.ItemFragment
import fr.jorisfavier.youshallnotpass.ui.search.SearchFragment
import fr.jorisfavier.youshallnotpass.ui.settings.SettingsFragment

@Module
abstract class MainModule {

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeItemFragment(): ItemFragment

    @ContributesAndroidInjector
    abstract fun contributeAuthActivity(): AuthActivity

    @ContributesAndroidInjector
    abstract fun contributeHomeActivity(): HomeActivity

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment

}