package fr.jorisfavier.youshallnotpass.utils

import java.security.SecureRandom

enum class PasswordOptions(val value: Int) {
    UPPERCASE(1),
    NUMBER(2),
    SYMBOL(4)
}

object PasswordUtil {

    const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    const val NUMBERS = "0123456789"
    const val SYMBOLS = "!#$%&()*+,-./:;<=>?@^[\\]_{|}~"

    const val MINIMUM_SECURE_SIZE = 12

    /**
     * Generates a secure password String based on the given options
     * @param options combines the values from the PasswordOptions enum
     * @param passwordSize indicates the password length
     */
    fun getSecurePassword(options: Int, passwordSize: Int = MINIMUM_SECURE_SIZE): String {
        val builder = StringBuilder()
        val random = SecureRandom()
        var bound: Int
        val maxBoundForOptions = (passwordSize * 0.33).toInt()

        if (options and PasswordOptions.UPPERCASE.value != 0) {
            bound = random.nextInt(maxBoundForOptions)
            for (i in 0..bound) {
                builder.append(UPPERCASE[random.nextInt(UPPERCASE.length)])
            }
        }

        if (options and PasswordOptions.NUMBER.value != 0) {
            bound = random.nextInt(maxBoundForOptions)
            for (i in 0..bound) {
                builder.append(NUMBERS[random.nextInt(NUMBERS.length)])
            }
        }

        if (options and PasswordOptions.SYMBOL.value != 0) {
            bound = random.nextInt(maxBoundForOptions)
            for (i in 0..bound) {
                builder.append(SYMBOLS[random.nextInt(SYMBOLS.length)])
            }
        }
        for (i in builder.length until passwordSize) {
            builder.append(LOWERCASE[random.nextInt(LOWERCASE.length)])
        }
        val res = builder.toList() as MutableList<Char>
        res.shuffle()
        return res.joinToString("")
    }
}