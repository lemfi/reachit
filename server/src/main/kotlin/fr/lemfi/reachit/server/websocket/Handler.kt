package fr.lemfi.reachit.server.websocket

import fr.lemfi.reachit.server.service.MessageService
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class Handler(val messageService: MessageService): TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        messageService.acceptSession(session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        messageService.removeSession(session)
    }
}