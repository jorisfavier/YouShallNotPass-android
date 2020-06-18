package fr.jorisfavier.youshallnotpass.utils

import java.security.SecureRandom

enum class PasswordOptions(val value: Int) {
    UPPERCASE(1),
    NUMBER(2),
    SYMBOL(4)
}

class PasswordUtil {

    companion object {

        private const val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val lowercase = "abcdefghijklmnopqrstuvwxyz"
        private const val numbers = "0123456789"
        private const val symbols = "!#$%&()*+,-./:;<=>?@^[\\]_{|}~"

        const val defaultSize = 12

        /**
         * Generates a secure password String based on the given options
         * @param options combines the values from the PasswordOptions enum
         * @param passwordSize indicates the password length
         */
        fun getSecurePassword(options: Int, passwordSize: Int = defaultSize): String {
            val builder = StringBuilder()
            val random = SecureRandom()
            var bound: Int
            val maxBoundForOptions = (passwordSize * 0.33).toInt()

            if (options and PasswordOptions.UPPERCASE.value != 0) {
                bound = random.nextInt(maxBoundForOptions)
                for (i in 0..bound) {
                    builder.append(uppercase[random.nextInt(uppercase.length)])
                }
            }

            if (options and PasswordOptions.NUMBER.value != 0) {
                bound = random.nextInt(maxBoundForOptions)
                for (i in 0..bound) {
                    builder.append(numbers[random.nextInt(numbers.length)])
                }
            }

            if (options and PasswordOptions.SYMBOL.value != 0) {
                bound = random.nextInt(maxBoundForOptions)
                for (i in 0..bound) {
                    builder.append(symbols[random.nextInt(symbols.length)])
                }
            }
            for (i in builder.length until passwordSize) {
                builder.append(lowercase[random.nextInt(lowercase.length)])
            }
            val res = builder.toList() as MutableList<Char>
            res.shuffle()
            return res.joinToString("")
        }
    }


}