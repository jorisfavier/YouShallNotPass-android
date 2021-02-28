package fr.jorisfavier.youshallnotpass.utils.extensions

/**
 * Returns the first non null value of nullableElement's list.
 * If all of them are null the defaultValue is returned
 * @param defaultValue the non null fallback value
 * @param nullableElement a list of nullable elements
 * @return The first non null element otherwise defaultValue
 */
fun <T> firstNotNull(defaultValue: T, vararg nullableElement: T?): T {
    nullableElement.forEach {
        if (it != null) return it
    }
    return defaultValue
}