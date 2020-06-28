package fr.lemfi.reachit.server.business

import org.springframework.web.socket.WebSocketSession

data class Room(
        val name: String,
        var developer: WebSocketSession
)