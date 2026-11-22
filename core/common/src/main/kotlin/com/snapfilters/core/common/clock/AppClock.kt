package com.snapfilters.core.common.clock

interface AppClock {
    fun nowEpochMillis(): Long
    fun nowEpochSeconds(): Long = nowEpochMillis() / 1000
}

class SystemAppClock : AppClock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}
