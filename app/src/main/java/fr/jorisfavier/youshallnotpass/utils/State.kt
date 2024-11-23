package fr.jorisfavier.youshallnotpass.utils

sealed class State<out T : Any> {
    data object Loading : State<Nothing>()
    class Success<out T : Any>(val value: T) : State<T>()
    data object Error : State<Nothing>()
}
