package fr.jorisfavier.youshallnotpass.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.jorisfavier.youshallnotpass.service.YsnpAutofillService
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillActivity
import fr.jorisfavier.youshallnotpass.ui.desktop.DesktopConnectionActivity
import fr.jorisfavier.youshallnotpass.ui.home.HomeActivity
import fr.jorisfavier.youshallnotpass.ui.item.ItemFragment
import fr.jorisfavier.youshallnotpass.ui.search.SearchFragment
import fr.jorisfavier.youshallnotpass.ui.settings.SettingsFragment
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemActivity
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportResultFragment
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportSelectFileFragment
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ProvideImportPasswordFragment
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.review.ReviewImportedItemsFragment

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

    @ContributesAndroidInjector
    abstract fun contributeImportItemActivity(): ImportItemActivity

    @ContributesAndroidInjector
    abstract fun contributeImportSelectFileFragment(): ImportSelectFileFragment

    @ContributesAndroidInjector
    abstract fun contributeProvideImportPasswordFragment(): ProvideImportPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeReviewImportedItemsFragment(): ReviewImportedItemsFragment

    @ContributesAndroidInjector
    abstract fun contributeImportResultFragment(): ImportResultFragment

    @ContributesAndroidInjector
    abstract fun contributeDesktopConnectionActivity(): DesktopConnectionActivity

    @ContributesAndroidInjector
    abstract fun contributeAutofillService(): YsnpAutofillService

    @ContributesAndroidInjector
    abstract fun contributeAutofillActivity(): AutofillActivity
}