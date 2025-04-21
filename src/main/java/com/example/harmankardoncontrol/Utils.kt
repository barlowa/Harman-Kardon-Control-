package com.example.harmankardoncontrol

import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress

object Utils {
    private const val NTP_SERVER = "pool.ntp.org"

    fun getNetworkTime(): Long? {
        return try {
            val client = NTPUDPClient()
            client.defaultTimeout = 3000
            val inetAddress = InetAddress.getByName(NTP_SERVER)
            val timeInfo = client.getTime(inetAddress)
            timeInfo.message.transmitTimeStamp.time
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
