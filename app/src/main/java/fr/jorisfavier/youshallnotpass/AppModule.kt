package fr.jorisfavier.youshallnotpass

import dagger.Module
import dagger.Provides
import fr.jorisfavier.youshallnotpass.managers.support.FingerPrintAuthManager
import fr.jorisfavier.youshallnotpass.managers.IFingerPrintAuthManager

@Module
class AppModule {
    @Provides
    fun fingerPrintManagerProvider(): IFingerPrintAuthManager {
        return FingerPrintAuthManager()
    }
}