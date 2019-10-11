package com.firemaples.photosbackuphelper

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.ref.WeakReference

/**
 * https://www.reddit.com/r/Kotlin/comments/8gbiul/slf4j_loggers_in_3_ways/
 */
inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

fun <T> T.getWeakReference(): WeakReference<T> = WeakReference(this)