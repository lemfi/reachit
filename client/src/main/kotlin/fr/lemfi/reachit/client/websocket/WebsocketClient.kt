package fr.lemfi.reachit.client.websocket

import fr.lemfi.reachit.client.configuration.ServerProperties
import fr.lemfi.reachit.client.service.MessageService
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI
import javax.websocket.*


@Component
@ClientEndpoint
class WebsocketClient(
        val messageService: MessageService,
        val properties: ServerProperties
) {

    init {
        println("""Connecting developer "${properties.developer}" to server ...""")
        while (true)
            try {
                StandardWebSocketClient(
                        ContainerProvider.getWebSocketContainer().apply {
                            connectToServer(this@WebsocketClient, URI(properties.socket + properties.developer))
                        }
                )
                break
            } catch (e: Throwable) { }
    }

    @OnOpen
    fun onOpen(session: Session?, config: EndpointConfig?) {
        println("Connexion ok!")
    }


    @OnClose
    fun onClose(session: Session?, closeReason: CloseReason?) {
        println("Connexion closed! Reconnecting...")
        while (true)
            try {
                StandardWebSocketClient(
                        ContainerProvider.getWebSocketContainer().apply {
                            connectToServer(this@WebsocketClient, URI(properties.socket + properties.developer))
                        }
                )
                break
            } catch (e: Throwable) { }
    }

    @OnMessage
    fun onMessage(message: String?) {
        println("receiving message $message")
        message?.let {
            messageService.onMessage(message)
        }
    }


}