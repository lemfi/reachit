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
                    while (reader.readLine().also { line = it }.let { it != null && it != "" } ) {
                        val currentLine = line!!
                        if (requestLine == null) {
                            requestLine = currentLine
                        }
                        if (currentLine.startsWith("Content-Type") && currentLine.contains("multipart/form-data")) {
                            boundary = currentLine.substringAfter("boundary=").substringBefore("\n").substringBefore(";").trim()
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
                } catch (e: SocketException) {}
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

    if (method == "POST" && path == "/hello") handleSayHello(jacksonObjectMapper().readValue(body!!, Map::class.java)["who"] as String)
    else if (method == "GET" && path == "/hello") handleListHello()

    else if (method == "DELETE" && path.startsWith("/hello")) handleSayGoodbye(URLDecoder.decode(path.substringAfter("who="), Charsets.UTF_8))

    else if (method == "POST" && path == "/send-letter") handleLetter(boundary, body!!)
    else if (method == "GET" && path == "/sent-letters") handleSentLetters()

    else PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 405 OK
                    Content-Type: application/json

                    {"message": "method not allowed", "code": 1, "description": "method $method is not allowed for path $path"}"""
            .trimIndent())
    }
}

private fun OutputStream.handleSayHello(who: String) {

    helloPeople.add(who)

    PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 201 OK
                    Content-Type: text/plain

                    Hello $who!"""
            .trimIndent())
    }
}

private fun OutputStream.handleSayGoodbye(who: String) {

    helloPeople.remove(who)

    PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 201 OK
                    Content-Type: text/plain

                    Goodbye $who!"""
            .trimIndent())
    }
}

private fun OutputStream.handleListHello() {

    PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 200 OK
                    Content-Type: application/json

                    [${helloPeople.map { """"$it"""" }.joinToString(", ")}]
                """.trimIndent())
    }
}

private fun OutputStream.handleSentLetters() {

    PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 200 OK
                    Content-Type: application/json

                    [${letters.map { """{"to": "${it.key}", "letters": [${it.value.map { """"$it"""" }.joinToString(", ")}]}""" }.joinToString(", ")}]
                """.trimIndent())
    }
}

private fun OutputStream.handleLetter(boundary: String?, content: String) {

    if (boundary == null) {
        PrintWriter(this, true).apply {
            println("""
                    HTTP/1.1 415 OK
                    Content-Type: application/json

                    {"message": "Unsupported Media Type", "code": 2, "description": "letter is expected as multipart/form-data"}"""
                .trimIndent())
        }
    } else {


        val data = content.split("--$boundary").dropLast(1).drop(1)

        var letter: String? = null
        var to: String? = null
        data.forEach {
            val contentDisposition = it.substringAfter("Content-Disposition:").substringBefore("\n").trim()
            val name = contentDisposition.substringAfter("name=\"").substringBefore("\"").trim()
            val filename = contentDisposition.substringAfter("filename=\"").substringBefore("\"").trim().ifEmpty { null }
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
                println("""
                    HTTP/1.1 200 OK
                    Content-Type: text/plain

                    Your letter was successfully sent to $to
                """.trimIndent())
            }
        } else {
            PrintWriter(this, true).apply {
                println("""
                    HTTP/1.1 422 OK
                    Content-Type: application/json

                    {"message": "invalid letter", "code": 2, "description": "you should provide letter and to parameters"}"""
                    .trimIndent())
            }
        }


    }
}

fun main() {
    startSampleApi()
    while (true) {}
}