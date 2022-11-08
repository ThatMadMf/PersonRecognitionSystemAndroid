package com.example.person_recognition_system.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.person_recognition_system.dtos.DeviceAuthorizationEvent
import com.example.person_recognition_system.dtos.EventResponse
import com.example.person_recognition_system.dtos.PhotoSocketEvent
import com.example.person_recognition_system.dtos.SocketEvent
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*


class WebsocketClient(
    private val serverURI: URI?,
    private var deviceId: String,
    private var authToken: String,
) : Service() {
    private val tag = "Socket client"

    private val cc = object: WebSocketClient(serverURI) {
        override fun onOpen(handshakedata: ServerHandshake) {
            this@WebsocketClient.sendAuthorizeDevice(
                DeviceAuthorizationEvent(
                    this@WebsocketClient.deviceId,
                    this@WebsocketClient.authToken,
                )
            )
        }

        override fun onMessage(message: String) {
            val event = Gson().fromJson(message, EventResponse::class.java)

            when (event.command) {
                authorizeDeviceEvent ->
                    if (event.result == "success") {
                        serviceCallbacks!!.authorizationSuccess()
                    } else {
                        serviceCallbacks!!.authorizationFailure()
                    }
            }
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            Log.w(tag, "Connection closed $reason")
        }

        override fun onError(ex: Exception) {
            Log.w(tag, "Socket error: " + ex.message)
        }
    }

    private val authorizeDeviceEvent = "authorize-device"

    // Binder given to clients
    private val binder: IBinder = LocalBinder()

    // Registered callbacks
    private var serviceCallbacks: SocketClientCallbacks? = null

    // Class used for the client Binder.
    inner class LocalBinder : Binder() {
        fun getService(): WebsocketClient {
            // Return this instance of MyService so clients can call public methods
            return this@WebsocketClient
        }
    }

    fun setCallbacks(callbacks: SocketClientCallbacks?) {
        serviceCallbacks = callbacks
    }

    fun connect() {
       cc.connect()
    }

    fun sendFaceCaptureFrame(eventData: PhotoSocketEvent) {
        Log.i(tag, "Sending face-capture-frame event")

        this.sendEvent("face-capture-frame", eventData)
    }

    private fun sendAuthorizeDevice(eventData: DeviceAuthorizationEvent) {
        Log.i(tag, "Sending $authorizeDeviceEvent event")

        this.sendEvent(authorizeDeviceEvent, eventData)
    }

    private fun sendEvent(command: String, data: Any) {
        if (!cc.isOpen) {
            Log.i(tag, "Could not send event: connection is not open")
        }

        try {
            cc.send(Gson().toJson(SocketEvent(command, data, UUID.randomUUID())))
        } catch (e: Exception) {
            Log.i(tag, "Could not send event: " + e.message)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}