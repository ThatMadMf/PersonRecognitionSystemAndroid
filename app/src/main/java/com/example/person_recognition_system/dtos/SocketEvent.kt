package com.example.person_recognition_system.dtos

import java.util.UUID

data class SocketEvent(val command: String, val data: Any?, val uuid: UUID)
