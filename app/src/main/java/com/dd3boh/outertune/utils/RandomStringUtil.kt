package com.dd3boh.outertune.utils

import java.security.SecureRandom

object RandomStringUtil {
    private val secureRandom = SecureRandom()

    /**
     * Generates a random string of the specified length using the specified character types.
     * 
     * @param length The length of the string to generate.
     * @param includeLetters Whether to include alphabetic characters (A-Z, a-z).
     * @param includeNumbers Whether to include numeric characters (0-9).
     * @return A random string based on the specified parameters.
     */
    fun random(length: Int, includeLetters: Boolean, includeNumbers: Boolean): String {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val charPool = StringBuilder()

        if (includeLetters) {
            charPool.append(letters)
        }
        if (includeNumbers) {
            charPool.append(numbers)
        }

        // Ensure at least one character type is included
        if (charPool.isEmpty()) {
            throw IllegalArgumentException("At least one character type must be included.")
        }

        val builder = StringBuilder(length)
        repeat(length) {
            builder.append(charPool[secureRandom.nextInt(charPool.length)])
        }
        return builder.toString()
    }
}