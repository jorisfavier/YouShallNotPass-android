package fr.jorisfavier.youshallnotpass.model

import java.time.LocalDateTime

enum class Frequency {
    DAILY;

    /**
     * Indicates if compared to now, the given date passed the current frequency.
     * @param date
     * @return true if the date is passed otherwise false
     */
    fun isOutdated(date: LocalDateTime): Boolean {
        return when (this) {
            DAILY -> LocalDateTime.now().minusDays(1).isAfter(date)
        }
    }
}