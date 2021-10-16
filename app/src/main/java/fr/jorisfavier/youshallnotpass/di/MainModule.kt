package fr.jorisfavier.youshallnotpass.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.jorisfavier.youshallnotpass.manager.AuthManager
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.manager.impl.AuthManagerImpl
import fr.jorisfavier.youshallnotpass.manager.impl.CryptoManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MainModule {

    @Singleton
    @Binds
    abstract fun bindCryptoManager(impl: CryptoManagerImpl): CryptoManager

    @Singleton
    @Binds
    abstract fun bindAuthManager(impl: AuthManagerImpl): AuthManager
}