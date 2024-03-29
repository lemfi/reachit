package fr.lemfi.reachit.test.sampleapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.SocketException
import java.net.URLDecoder

private var server: ServerSocket? = null

private val helloPeople = mutableListOf<String>()
private val letters = mutableMapOf<String, MutableList<String>>()

@Suppress("BlockingMethodInNonBlockingContext")
fun startSampleApi() {

    if (server == null) {

        server = ServerSocket(8001)

        GlobalScope.launch {

            while (server != null) {
                try {

                    val sampleApi = server!!.accept()

                    var contentLength = -1
                    var boundary: String? = null

                    val reader = sampleApi.getInputStream().bufferedReader(Charsets.UTF_8)
                    var line: String?
                    var requestLine: String? = null
                    var body: String? = null
                    while (reader.readLine().also { line = it }.let { it != null && it != "" }) {
                        val currentLine = line!!
                        if (requestLine == null) {
                            requestLine = currentLine
                        }
                        if (currentLine.startsWith("Content-Type") && currentLine.contains("multipart/form-data")) {
                            boundary =
                                currentLine.substringAfter("boundary=").substringBefore("\n").substringBefore(";")
                                    .trim()
                        }
                        if (currentLine.startsWith("Content-Length")) {
                            val headerValue = currentLine.substringAfter(":").trim { it <= ' ' }
                            contentLength = headerValue.toInt()
                        }
                    }
                    if (contentLength > 0) {
                        body = CharArray(contentLength).let {
                            reader.read(it)
                            it.joinToString("")
                        }
                    }

                    val output = sampleApi.getOutputStream()
                    requestLine?.let {
                        output.handleRequest(it, boundary, body)
                    }
                    output.flush()
                    output.close()
                } catch (e: SocketException) {
                }
            }
        }
    }
}

fun stopSampleApi() {
    server?.close()
    helloPeople.clear()
    server = null
}


private fun OutputStream.handleRequest(request: String, boundary: String?, body: String?) {

    val (method, path) = request.split(" ").let { it[0] to it[1].substringBefore("?") }


    if (method == "POST" && path == "/hello") handleSayHello(
        jacksonObjectMapper().readValue(
            body!!,
            Map::class.java
        )["who"] as String
    )
    else if (method == "GET" && path == "/hello") handleListHello()
    else if (method == "DELETE" && path.startsWith("/hello")) handleSayGoodbye(
        URLDecoder.decode(
            path.substringAfter("who="),
            Charsets.UTF_8
        )
    )
    else if (method == "POST" && path == "/send-letter") handleLetter(boundary, body!!)
    else if (method == "GET" && path == "/sent-letters") handleSentLetters()
    else if (method == "GET" && path == "/lorem-ipsum") handleLoremIpsum()

    else PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 405 OK
                    Content-Type: application/json
                    Content-Length: ${"""{"message": "method not allowed", "code": 1, "description": "method $method is not allowed for path $path"}""".length}

                    {"message": "method not allowed", "code": 1, "description": "method $method is not allowed for path $path"}"""
                .trimIndent()
        )
    }
}

private fun OutputStream.handleSayHello(who: String) {

    helloPeople.add(who)

    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 201 OK
                    Content-Type: text/plain
                    Content-Length: ${"Hello $who!".length}

                    Hello $who!"""
                .trimIndent()
        )
    }
}

private fun OutputStream.handleSayGoodbye(who: String) {

    helloPeople.remove(who)

    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 201 OK
                    Content-Type: text/plain
                    Content-Length: ${"Goodbye $who!".length}

                    Goodbye $who!"""
                .trimIndent()
        )
    }
}

private fun OutputStream.handleListHello() {

    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 200 OK
                    Content-Type: application/json
                    Content-Length: ${"[${helloPeople.map { """"$it"""" }.joinToString(", ")}]".length}

                    [${helloPeople.map { """"$it"""" }.joinToString(", ")}]
                """.trimIndent()
        )
    }
}

private fun OutputStream.handleLoremIpsum() {

    val speak = """Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
        |Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 
        |Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. 
        |Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.""".trimMargin()

    val chunks = speak.length / 100 + if (speak.length % 100 > 0) 1 else 0

    PrintWriter(this, false).apply {
        println(
            """HTTP/1.1 200 OK
            |Content-Type: text/plain
            |Transfer-Encoding: chunked
            |Connection: keep-alive
            |Keep-Alive: timeout=60
            |
            |${100.toString(16)}
            |${speak.subSequence(0, 100)}"""
                .trimMargin()
        )
    }.apply { flush() }

    (1 until chunks).forEach {
        val chunk = speak.subSequence(it * 100, minOf(it * 100 + 100, speak.length))
        PrintWriter(this, false).apply {
            println(
                """${chunk.length.toString(16)}
                    |$chunk"""
                    .trimMargin()
            )
        }.apply { flush() }
    }
    PrintWriter(this, false).apply {
        println("""
            |0
            |""".trimMargin())
    }.apply { flush() }
}

private fun OutputStream.handleSentLetters() {

    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 200 OK
                    Content-Type: application/json
                    Content-Length: ${
                "[${
                    letters.map {
                        """{"to": "${it.key}", "letters": [${
                            it.value.map { """"$it"""" }.joinToString(", ")
                        }]}"""
                    }.joinToString(", ")
                }]".length
            }

                    [${
                letters.map {
                    """{"to": "${it.key}", "letters": [${
                        it.value.map { """"$it"""" }.joinToString(", ")
                    }]}"""
                }.joinToString(", ")
            }]
                """.trimIndent()
        )
    }
}

private fun OutputStream.handleLetter(boundary: String?, content: String) {

    if (boundary == null) {
        PrintWriter(this, true).apply {
            println(
                """
                    HTTP/1.1 415 OK
                    Content-Type: application/json
                    Content-Length: ${"""{"message": "Unsupported Media Type", "code": 2, "description": "letter is expected as multipart/form-data"}""".length}

                    {"message": "Unsupported Media Type", "code": 2, "description": "letter is expected as multipart/form-data"}"""
                    .trimIndent()
            )
        }
    } else {


        val data = content.split("--$boundary").dropLast(1).drop(1)

        var letter: String? = null
        var to: String? = null
        data.forEach {
            val contentDisposition = it.substringAfter("Content-Disposition:").substringBefore("\n").trim()
            val name = contentDisposition.substringAfter("name=\"").substringBefore("\"").trim()
            val contentType = it.substringAfter("Content-Type:").substringBefore("\n").trim().ifEmpty { null }

            if (name == "letter") {
                letter = it.trim().substringAfterLast("\n")
            }

            if (name == "to" && contentType == null) {
                to = it.trim().substringAfterLast("\n")
            }
        }

        if (to != null && letter != null) {

            if (letters.containsKey(to!!)) letters[to!!]!!.add(letter!!)
            else letters.put(to!!, mutableListOf(letter!!))

            PrintWriter(this, true).apply {
                println(
                    """
                    HTTP/1.1 200 OK
                    Content-Type: text/plain
                    Content-Length: ${"Your letter was successfully sent to $to".length}

                    Your letter was successfully sent to $to
                """.trimIndent()
                )
            }
        } else {
            PrintWriter(this, true).apply {
                println(
                    """
                    HTTP/1.1 422 OK
                    Content-Type: application/json
                    Content-Length: ${"""{"message": "invalid letter", "code": 2, "description": "you should provide letter and to parameters"}""".length}

                    {"message": "invalid letter", "code": 2, "description": "you should provide letter and to parameters"}"""
                        .trimIndent()
                )
            }
        }


    }
}

fun main() {
    startSampleApi()
    while (true) {
    }
}