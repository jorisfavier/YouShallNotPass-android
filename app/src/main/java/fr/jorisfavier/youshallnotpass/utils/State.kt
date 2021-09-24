package fr.jorisfavier.youshallnotpass.utils

sealed class State<out T : Any> {
    object Loading : State<Nothing>()
    class Success<out T : Any>(val value: T) : State<T>()
    object Error : State<Nothing>()
}