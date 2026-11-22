package com.snapfilters.core.common.id

import java.security.SecureRandom
import java.util.UUID

interface IdGenerator {
    fun generateUuid(): String
    fun generateSecureRandomBytes(byteCount: Int = 32): ByteArray
}

class DefaultIdGenerator : IdGenerator {
    private val secureRandom = SecureRandom()

    override fun generateUuid(): String = UUID.randomUUID().toString()

    override fun generateSecureRandomBytes(byteCount: Int): ByteArray {
        val bytes = ByteArray(byteCount)
        secureRandom.nextBytes(bytes)
        return bytes
    }
}

