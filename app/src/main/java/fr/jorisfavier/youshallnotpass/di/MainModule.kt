package fr.jorisfavier.youshallnotpass.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import fr.jorisfavier.youshallnotpass.ui.item.ItemFragment
import fr.jorisfavier.youshallnotpass.ui.search.SearchFragment

@Module
abstract class MainModule {

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeItemFragment(): ItemFragment

}