package fr.lemfi.reachit.server.controller

import fr.lemfi.reachit.server.business.Payload
import fr.lemfi.reachit.server.service.MessageService
import fr.lemfi.reachit.server.service.MiddlewareService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/req")
internal class RequestController(val middlewareService: MiddlewareService) {

    @RequestMapping(
            "/{developer}/**",
            method = [RequestMethod.GET]
    )
    fun get(@PathVariable developer: String, request: HttpServletRequest): ResponseEntity<Any> {

        println("Receiving message for $developer, ... forwarding ...")

        val path = request.servletPath.substringAfter("/req/$developer") + "?" +
                request.parameterMap.flatMap { entry ->
                    entry.value.map { "${entry.key}=$it" }
                }.joinToString("&")

        return ResponseEntity.ok(middlewareService.notify(developer, Payload("GET", path)))
    }
}

