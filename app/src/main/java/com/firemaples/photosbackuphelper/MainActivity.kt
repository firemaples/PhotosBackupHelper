package com.firemaples.photosbackuphelper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val logger by lazy { logger() }

    private val cifsFinder: ServiceFinder by lazy { ServiceFinder.createCIFSFinder(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cifsFinder.listener = {
            logger.debug("Find CIFS service: $it")
        }
        cifsFinder.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        cifsFinder.stop()
    }
}
