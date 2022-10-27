package com.example.person_recognition_system.services

import com.example.person_recognition_system.dtos.SocketEventDto
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SocketClient(serverURI: URI?, private var deviceId: String) : WebSocketClient(serverURI) {

    override fun onOpen(handshakedata: ServerHandshake) {
        this.send(Gson().toJson(SocketEventDto("authorize-device", this.deviceId)))
    }

    override fun onMessage(message: String) {
        println("received: $message")
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("Connection closed")
    }

    override fun onError(ex: Exception) {
        ex.printStackTrace()
    }
}