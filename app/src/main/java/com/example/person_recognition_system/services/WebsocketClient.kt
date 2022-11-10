package com.example.person_recognition_system.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.person_recognition_system.MainActivity.Companion.authToken
import com.example.person_recognition_system.MainActivity.Companion.deviceId
import com.example.person_recognition_system.MainActivity.Companion.serverURI
import com.example.person_recognition_system.MainActivity.Companion.token
import com.example.person_recognition_system.dtos.DeviceAuthorizationEvent
import com.example.person_recognition_system.dtos.EventResponse
import com.example.person_recognition_system.dtos.PhotoSocketEvent
import com.example.person_recognition_system.dtos.SocketEvent
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.util.*


class WebsocketClient : Service {
    constructor() : super() {
        this.cc = object : WebSocketClient(serverURI) {
            override fun onOpen(handshakedata: ServerHandshake) {
                this@WebsocketClient.sendAuthorizeDevice(
                    DeviceAuthorizationEvent(
                        deviceId!!,
                        authToken,
                    )
                )
            }

            override fun onMessage(message: String) {
                val event = Gson().fromJson(message, EventResponse::class.java)

                if (serviceCallbacks == null) {
                    Log.i(tag, "Callbacks are null")

                    return
                }

                when (event.command) {
                    authorizeDeviceEvent ->
                        if (event.result == "success") {
                            serviceCallbacks!!.deviceAuthorizationSuccess()
                        } else {
                            serviceCallbacks!!.deviceAuthorizationFailure()
                        }
                    authorizationResultEvent ->
                        if (event.result == "success") {
                            token = event.data["token"]
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
    }

    private val tag = "Socket client"

    private var cc: WebSocketClient? = null

    private val authorizeDeviceEvent = "authorize-device"
    private val authorizationResultEvent = "authorization-result"

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

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun setCallbacks(callbacks: SocketClientCallbacks?) {
        serviceCallbacks = callbacks
    }

    fun connect() {
        cc!!.connect()
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
        if (!cc!!.isOpen) {
            Log.i(tag, "Could not send event: connection is not open")
        }

        try {
            cc!!.send(Gson().toJson(SocketEvent(command, data, UUID.randomUUID())))
        } catch (e: Exception) {
            Log.i(tag, "Could not send event: " + e.message)
        }
    }
}