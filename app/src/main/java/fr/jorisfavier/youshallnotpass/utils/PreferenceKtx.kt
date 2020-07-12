package fr.jorisfavier.youshallnotpass.utils

import androidx.preference.ListPreference

fun ListPreference.getEntryforValue(value: Any): String? {
    return if (value is String) {
        val index = findIndexOfValue(value)
        if (index > 0 && index < entries.size) {
            entries[index].toString()
        } else {
            null
        }
    } else {
        null
    }
}