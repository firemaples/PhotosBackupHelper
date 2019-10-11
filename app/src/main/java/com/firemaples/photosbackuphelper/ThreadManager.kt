package com.firemaples.photosbackuphelper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun launch(block: suspend CoroutineScope.() -> Unit) {
    GlobalScope.launch(block = block)
}