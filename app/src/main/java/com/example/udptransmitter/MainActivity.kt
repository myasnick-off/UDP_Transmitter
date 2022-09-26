package com.example.udptransmitter

import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.IOException
import java.net.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var isOpened = false
    private var udpSocket: DatagramSocket? = null
    private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        launchUdpBroadCast()
    }

    override fun onStop() {
        super.onStop()
        isOpened = false
    }

    private fun launchUdpBroadCast() {
        isOpened = true
        if (openSocket()) {
            Thread {
                while (isOpened) {
                    Thread.sleep(1000)
                    val datetime = dateFormatter.format(Date())
                    val locale = Locale.getDefault().isO3Language
                    val audioMgr = getSystemService(AUDIO_SERVICE) as AudioManager
                    val volume = audioMgr.getStreamVolume(AudioManager.STREAM_SYSTEM)
                    val byteData = convertDataToByteArray(datetime, locale, volume)
                    sendPacket(byteData)
                }
                closeSocket()
            }.start()
        }
    }

    private fun openSocket(): Boolean {
        try {
            udpSocket = DatagramSocket(SEND_PORT)
            udpSocket?.apply {
                broadcast = true
                reuseAddress = true
            }
            return true
        } catch (ex: SocketException) {
            Log.e(LOG_TAG, ex.message ?: DEFAULT_ERROR_MESSAGE)
        }
        return false
    }

    private fun closeSocket(): Boolean {
        udpSocket?.let { socket ->
            if (!socket.isClosed) {
                socket.close()
                return true
            }
        }
        return false
    }

    private fun sendPacket(buffer: ByteArray): Boolean {
        try {
            val packet = DatagramPacket(buffer, buffer.size)
            udpSocket?.send(packet)
            return true
        } catch (ex: UnknownHostException) {
            Log.e(LOG_TAG, ex.message ?: DEFAULT_ERROR_MESSAGE)
        } catch (ex: IOException) {
            Log.e(LOG_TAG, ex.message ?: DEFAULT_ERROR_MESSAGE)
        }
        return false
    }

    private fun convertDataToByteArray(date : String, locale: String, volume: Int): ByteArray {
        val data = JSONObject()
        data.put("date", date)
        data.put("locale", locale)
        data.put("volume", volume)
        return data.toString().toByteArray()
    }

    companion object {
        private const val SEND_PORT = 5555
        private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"
        private const val LOG_TAG = "udp_log"
        private const val DEFAULT_ERROR_MESSAGE = "Unknown error!"
    }
}