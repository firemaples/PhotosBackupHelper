package com.firemaples.photosbackuphelper

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

class ServiceSniffer(context: Context) {
    private val logger by lazy { logger() }

    private val appContext: Application = context.applicationContext as Application

    private val connectivityManager by lazy {
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val wifiManager by lazy {
        appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun sniff() {
        launch {
            val activeNetwork = connectivityManager.activeNetworkInfo
            val connectionInfo = wifiManager.connectionInfo
//            val ipAddress = Formatter.formatIpAddress(connectionInfo.ipAddress)
            val ipAddress =
                InetAddress.getByAddress(
                    connectionInfo.ipAddress.toBigInteger().toByteArray().reversedArray()
                ).hostAddress

            logger.debug("ActiveNetwork: $activeNetwork")
            logger.debug("Ip address: $ipAddress")

            val prefix = ipAddress.substringBeforeLast(".").plus(".")
            logger.debug("Ip prefix: $prefix")

            var totalChecked = AtomicInteger(0)
            (0..255).forEach { sub ->
                launch {
                    val testIp = prefix.plus(sub)

//                    logger.debug("Start to test: $testIp")

                    val address = InetAddress.getByName(testIp)
                    val reachable = address.isReachable(1000)
                    val hostName = address.canonicalHostName

                    logger.debug("Checked: (${totalChecked.incrementAndGet()}/256)")
                    if (reachable) logger.info("Found host: $hostName, ip: $testIp")
                }
            }
        }
    }
}