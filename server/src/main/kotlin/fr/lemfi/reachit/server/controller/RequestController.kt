package fr.lemfi.reachit.server.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.lemfi.reachit.server.business.MultipartPayload
import fr.lemfi.reachit.server.business.NoMultipartPayload
import fr.lemfi.reachit.server.business.Part
import fr.lemfi.reachit.server.business.Payload
import fr.lemfi.reachit.server.service.MiddlewareService
import okhttp3.internal.closeQuietly
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
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

        notify(responses, developer,
                NoMultipartPayload(
                        method = request.method,
                        headers = request.headerNames.toList().map { it to request.getHeader(it) }.let {
                            mutableMapOf<String, String>().apply {
                                it.forEach { put(it.first, it.second) }
                            }
                        },
                        contentType = request.contentType,
                        path = path(request, developer),
                        body = body?.let { jacksonObjectMapper().writeValueAsBytes(it) }
                ))
    }

    @RequestMapping(
            "/{developer}/**",
            method = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE],
            consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun multipart(@PathVariable developer: String, request: HttpServletRequest, responses: HttpServletResponse) {

        println("Receiving message for $developer, ... forwarding ...")

        val formFields = ServletFileUpload().getItemIterator(request).let { items ->
            mutableMapOf<String, Boolean>().also { parts ->
                while (items.hasNext()) {
                    items.next().let {
                        parts.put(it.name, it.isFormField)
                    }
                }
            }
        }

        val parts = request.parts.map {
            Part(
                    data = it.inputStream.readAllBytes(),
                    contentType = it?.contentType ?: "text/plain",
                    name = it.name,
                    file = formFields[it.name] ?: true,
                    filename = it.submittedFileName
            )
        }

        notify(responses, developer,
                MultipartPayload(
                        method = request.method,
                        path = path(request, developer),
                        headers = request.headerNames.toList().map { it to request.getHeader(it) }.let {
                            mutableMapOf<String, String>().
                            apply {
                                it.forEach { put(it.first, it.second) }
                            }
                        },
                        contentType = MediaType.MULTIPART_FORM_DATA_VALUE,
                        parts = parts)
        )
    }

    private fun notify(responses: HttpServletResponse, developer: String, body: Payload) {

        middlewareService.notify(developer, body)
                .let { response ->

                    val outputStream = responses.outputStream

                    response.headers.filterNot { it.key.equals("Transfer-Encoding", ignoreCase = true) || it.key.equals("Accept-Encoding", ignoreCase = true) }.forEach {
                        responses.addHeader(it.key, it.value as String?)
                    }
                    responses.status = response.status
                    response.body?.also {
                        outputStream.write(it).apply {
                            outputStream.closeQuietly()
                        }
                    }
                }
    }

    private fun path(request: HttpServletRequest, developer: String) =
            request.servletPath.substringAfter("/req/$developer") + "?" +
                    request.parameterMap.flatMap { entry ->
                        entry.value.map { "${entry.key}=${URLEncoder.encode(it, Charsets.UTF_8)}" }
                    }.joinToString("&")
}