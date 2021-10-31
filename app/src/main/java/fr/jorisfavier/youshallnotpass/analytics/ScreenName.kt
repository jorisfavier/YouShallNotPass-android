package fr.jorisfavier.youshallnotpass.analytics

sealed class ScreenName(open val event: String) {
    object Home : ScreenName("home")
}