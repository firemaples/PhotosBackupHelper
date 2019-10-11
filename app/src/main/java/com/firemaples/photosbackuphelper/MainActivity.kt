package com.firemaples.photosbackuphelper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val logger by lazy { logger() }

    private val finder: ServiceFinder by lazy { ServiceFinder.createSMBFinder(this, 3003) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        finder.listener = {
            logger.debug("Find SMB/CIFS service: $it")
        }
        finder.start()

//        ServiceSniffer(this).sniff()
    }

    override fun onDestroy() {
        super.onDestroy()

        finder.stop()
    }
}
