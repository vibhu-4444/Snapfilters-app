package com.snapfilters.core.common.logging

interface AppLogger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

class ConsoleAppLogger : AppLogger {
    override fun d(tag: String, message: String) {
        println("DEBUG: [$tag] $message")
    }

    override fun i(tag: String, message: String) {
        println("INFO:  [$tag] $message")
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        println("WARN:  [$tag] $message")
        throwable?.printStackTrace()
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        System.err.println("ERROR: [$tag] $message")
        throwable?.printStackTrace()
    }
}

