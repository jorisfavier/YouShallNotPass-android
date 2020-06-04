package fr.jorisfavier.youshallnotpass.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.jorisfavier.youshallnotpass.ui.search.SearchFragment

@Module
abstract class MainModule {

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

}