package fr.lemfi.reachit.server.controller

import fr.lemfi.reachit.server.business.Payload
import fr.lemfi.reachit.server.service.MiddlewareService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/resp")
internal class ResponseController(val middlewareService: MiddlewareService) {

    @RequestMapping(
            "/{id}",
            method = [RequestMethod.POST],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    fun get(@PathVariable id: String, @RequestBody body: Any): ResponseEntity<Any> {

        middlewareService.response(id, body)

        return ResponseEntity.noContent().build()
    }
}

