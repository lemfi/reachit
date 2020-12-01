package fr.lemfi.reachit.server.controller

import fr.lemfi.reachit.server.business.Response
import fr.lemfi.reachit.server.service.MiddlewareService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/resp")
internal class ResponseController(val middlewareService: MiddlewareService) {

    @RequestMapping(
            "/{id}",
            method = [RequestMethod.POST]
    )
    fun get(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Any> {

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
                        body = request.inputStream.readAllBytes()
                ))

        return ResponseEntity.noContent().build()
    }
}

