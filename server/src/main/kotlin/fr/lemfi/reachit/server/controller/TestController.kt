package fr.lemfi.reachit.server.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/test/**")
internal class TestController() {

    @RequestMapping(
            method = [RequestMethod.GET],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    fun get(request: HttpServletRequest): ResponseEntity<Any> {

        val path = request.servletPath + "?" +
                request.parameterMap.flatMap { entry ->
                    entry.value.map { "${entry.key}=$it" }
                }.joinToString("&")

//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .headers { headers ->
//                    headers.put("X-TEST", listOf("X-VALUE"))
//                }
//                .body("""
//            {
//                "hey": "$path"
//            }
//        """.trimIndent())

        return ResponseEntity.status(HttpStatus.FOUND).location(URI("https://petstore.swagger.io/v2/swagger.json")).build()
    }
}

