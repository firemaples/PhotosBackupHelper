package com.firemaples.photosbackuphelper

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://www.reddit.com/r/Kotlin/comments/8gbiul/slf4j_loggers_in_3_ways/
 */
inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}