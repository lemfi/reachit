package fr.lemfi.reachit.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.lemfi.reachit.server.business.Payload
import fr.lemfi.reachit.server.service.MiddlewareService
import okhttp3.internal.closeQuietly
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/req")
internal class RequestController(val middlewareService: MiddlewareService) {

    @RequestMapping(
            "/{developer}/**",
            method = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE]
    )
    fun get(@PathVariable developer: String, request: HttpServletRequest, responses: HttpServletResponse, @RequestBody body: Any?) {

        println("Receiving message for $developer, ... forwarding ...")

        val path = request.servletPath.substringAfter("/req/$developer") + "?" +
                request.parameterMap.flatMap { entry ->
                    entry.value.map { "${entry.key}=${ URLEncoder.encode(it, Charsets.UTF_8)}" }
                }.joinToString("&")

        middlewareService.notify(developer, Payload(method = request.method, path = path, body = body?.let {jacksonObjectMapper().writeValueAsString(it)}))
                .let { response ->

                    val outputStream = responses.outputStream

                    response.headers.filterNot { it.key.equals("Transfer-Encoding", ignoreCase = true) || it.key.equals("Accept-Encoding", ignoreCase = true) }.forEach {
                        responses.addHeader(it.key, it.value as String?)
                    }
                    response.body?.also { outputStream.write(it).apply {
                        outputStream.closeQuietly()
                    } }
                }
    }
}

