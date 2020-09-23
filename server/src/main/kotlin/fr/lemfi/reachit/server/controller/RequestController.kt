package fr.lemfi.reachit.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.lemfi.reachit.server.business.Payload
import fr.lemfi.reachit.server.service.MiddlewareService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/req")
internal class RequestController(val middlewareService: MiddlewareService) {

    @RequestMapping(
            "/{developer}/**",
            method = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE]
    )
    fun get(@PathVariable developer: String, request: HttpServletRequest, @RequestBody body: Any?): ResponseEntity<Any?> {

        println("Receiving message for $developer, ... forwarding ...")

        val path = request.servletPath.substringAfter("/req/$developer") + "?" +
                request.parameterMap.flatMap { entry ->
                    entry.value.map { "${entry.key}=${ URLEncoder.encode(it, Charsets.UTF_8)}" }
                }.joinToString("&")

        return middlewareService.notify(developer, Payload(method = request.method, path = path, body = body?.let {jacksonObjectMapper().writeValueAsString(it)})).let { response ->
            ResponseEntity.status(response.status)
                    .headers {
                        headers ->
                        response.headers.forEach {
                            headers.set(it.key, it.value as String?)
                        }
                    }
                    .body(response.body)
        }
    }
}

