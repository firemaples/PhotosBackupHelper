package com.firemaples.photosbackuphelper

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.net.InetAddress

class ServiceFinder(
    private val context: Context,
    private val serviceName: String,
    private val serviceType: String,
    private val port: Int
) {
    private val logger by lazy { logger() }

    /**
     * https://developer.android.com/training/connect-devices-wirelessly/nsd.html#discover
     */
    private val nsdManager by lazy { context.getSystemService(Context.NSD_SERVICE) as NsdManager }

    var listener: ((NsdServiceInfo) -> Unit)? = null
    private var mServiceName: String? = null
    private var serviceInfo: NsdServiceInfo? = null

    companion object {

        /**
         * https://kodi.wiki/view/Avahi_Zeroconf
         */
        fun createCIFSFinder(context: Context): ServiceFinder =
            ServiceFinder(context, "CIFSFinder", "_smb._tcp.", 139)
    }

    fun start() {
        registerService()
        discoverService()
    }

    fun stop() {
        nsdManager.apply {
            unregisterService(registrationListener)
            stopServiceDiscovery(discoveryListener)
        }
    }

    private fun registerService() {
        val info = NsdServiceInfo().apply {
            serviceName = this@ServiceFinder.serviceName
            serviceType = this@ServiceFinder.serviceType
            port = this@ServiceFinder.port
        }

        nsdManager.apply {
            logger.debug("Register service: $info")
            registerService(info, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }
    }

    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            mServiceName = NsdServiceInfo.serviceName
            logger.debug("onServiceRegistered(), NsdServiceInfo.serviceName: $mServiceName")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Registration failed! Put debugging code here to determine why.
            logger.warn("onRegistrationFailed(), errorCode: $errorCode")
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
            logger.debug("onServiceUnregistered()")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
            logger.warn("onUnregistrationFailed(), errorCode: $errorCode")
        }
    }

    private fun discoverService() {
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            logger.debug("Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            logger.debug("Service discovery success, service: {$service}")
            when {
                service.serviceType != serviceType -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    logger.debug("Unknown Service Type: ${service.serviceType}")
                service.serviceName == mServiceName -> // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    logger.debug("Same machine: $mServiceName")
//                service.serviceName.contains(SERVICE_NAME) -> nsdManager.resolveService(
//                    service,
//                    resolveListener
//                )
                else -> {
                    logger.debug("Resolve service: $service")
                    nsdManager.resolveService(service, resolveListener)
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            logger.error("service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            logger.info("Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            logger.error("Discovery failed: Error code: $errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            logger.error("Discovery failed: Error code: $errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            logger.error("onResolveFailed(), errorCode $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            logger.info("onServiceResolved(), serivceInfo: $serviceInfo")

            if (serviceInfo.serviceName == mServiceName) {
                logger.debug("Same IP.")
                return
            }
            this@ServiceFinder.serviceInfo = serviceInfo
            val port: Int = serviceInfo.port
            val host: InetAddress = serviceInfo.host

            logger.debug("Found service: $host:$port")

            listener?.invoke(serviceInfo)
        }
    }
}