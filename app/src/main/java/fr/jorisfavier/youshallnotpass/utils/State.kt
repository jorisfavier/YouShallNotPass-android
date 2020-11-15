package fr.jorisfavier.youshallnotpass.utils

sealed class State {
    object Loading : State()
    object Success : State()
    object Error : State()
}