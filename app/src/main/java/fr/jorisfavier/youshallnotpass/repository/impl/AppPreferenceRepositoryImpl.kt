package fr.jorisfavier.youshallnotpass.repository.impl

import androidx.appcompat.app.AppCompatDelegate
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.repository.AppPreferenceRepository
import fr.jorisfavier.youshallnotpass.utils.extensions.suspendRunCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppPreferenceRepositoryImpl(
    appScope: CoroutineScope,
    private val appPreferenceDataSource: AppPreferenceDataSource,
) : AppPreferenceRepository {

    override val theme: Flow<Int?> = appPreferenceDataSource.theme
    override val shouldHideItems: Flow<Boolean> = appPreferenceDataSource.shouldHideItems

    init {
        appScope.launch {
            appPreferenceDataSource.theme.collectLatest { themeValue ->
                println("Zbra - Theme value - $themeValue")
                AppCompatDelegate.setDefaultNightMode(
                    themeValue ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                )
            }
        }
    }

    override suspend fun setTheme(theme: Int): Result<Unit> {
        return suspendRunCatching(
            errorMessage = "An error occurred while setting the theme preference",
        ) {
            appPreferenceDataSource.setTheme(theme)
        }
    }

    override suspend fun setShouldHideItems(hide: Boolean): Result<Unit> {
        return suspendRunCatching(
            errorMessage = "An error occurred while setting the shouldHide preference",
        ) {
            appPreferenceDataSource.setShouldHideItems(hide)
        }
    }
}
