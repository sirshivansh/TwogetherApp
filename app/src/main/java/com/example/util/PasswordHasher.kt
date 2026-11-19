package com.example.util

import java.security.MessageDigest

object PasswordHasher {
    fun sha256(raw: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(raw.trim().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(raw: String, expectedHash: String): Boolean {
        if (raw.isBlank() || expectedHash.isBlank()) return false
        return sha256(raw) == expectedHash
    }
}
