package fr.lemfi.reachit.server.controller

import fr.lemfi.reachit.server.business.Response
import fr.lemfi.reachit.server.service.MiddlewareService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/resp")
internal class ResponseController(val middlewareService: MiddlewareService) {

    @RequestMapping(
            "/{id}",
            method = [RequestMethod.POST],
            consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    fun get(@PathVariable id: String, @RequestBody body: Any?, request: HttpServletRequest): ResponseEntity<Any> {

        middlewareService.response(id,

                Response(
                        status = request.getIntHeader("X-Reachit-Status"),
                        headers = request.headerNames.toList().filterNot {
                            it.toUpperCase().startsWith("X-REACHIT-")
                                    || it.toUpperCase().equals("CONNECTION")
                                    || it.toUpperCase().equals("CONTENT-LENGTH")
                        }.map {
                            it to request.getHeader(it)
                        }.toMap(),
                        body = body
                ))

        return ResponseEntity.noContent().build()
    }
}

