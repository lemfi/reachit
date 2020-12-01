package fr.lemfi.reachit.server.controller

import fr.lemfi.reachit.server.service.MessageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/payloads")
internal class PayloadsController(val messageService: MessageService) {

    @RequestMapping(method = [RequestMethod.GET])
    fun get(responses: HttpServletResponse, @RequestParam uuid: String): ResponseEntity<Any> {

        return ResponseEntity.ok(messageService.getPayload(uuid))
    }
}

