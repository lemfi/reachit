package fr.lemfi.reachit.server.configuration

import fr.lemfi.reachit.server.service.MessageService
import fr.lemfi.reachit.server.websocket.Handler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
open class WebSocketConfiguration(val messageService: MessageService): WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(Handler(messageService), "/socket")
                .setAllowedOrigins("*")
    }
}