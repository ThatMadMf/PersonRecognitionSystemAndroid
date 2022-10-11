package com.example.person_recognition_system.services

import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SocketClient : WebSocketClient {
    constructor(serverUri: URI?, draft: Draft?) : super(serverUri, draft) {}
    constructor(serverURI: URI?) : super(serverURI) {}
    constructor(serverUri: URI?, httpHeaders: Map<String?, String?>?) : super(
        serverUri,
        httpHeaders
    ) {
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        println("opened connection")
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