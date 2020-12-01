package fr.lemfi.reachit.test

import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.`true`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.executor.http.cli.`given http call`
import com.github.lemfi.kest.executor.http.model.HttpResponse
import com.github.lemfi.kest.executor.http.model.filePart
import com.github.lemfi.kest.executor.http.model.multipartBody
import com.github.lemfi.kest.executor.http.model.parameterPart
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.junit5.runner.`run scenarios`
import fr.lemfi.reachit.test.sampleapi.startSampleApi
import org.junit.jupiter.api.TestFactory
import java.io.File

class Go {

    @TestFactory
    fun go() = `run scenarios`(

            scenario {

                name = "say hello"

                lateinit var expected: HttpResponse<String>

                `given http call`<String> {

                    url = "http://localhost:8001/hello"
                    method = "POST"
                    body = """{"who": "Darth Vader"}"""
                    contentType = "application/json"

                    withResult { expected = this }
                }

                `given http call`<String> {

                    url = "http://localhost:8080/req/flemontreer/hello"
                    method = "POST"
                    body = """{"who": "Han Solo"}"""
                    contentType = "application/json"

                } `assert that` {

                    eq(expected.status, it.status)
                    eq("Hello Han Solo!", it.body)
                }
            },

            scenario {

                name = "get greeted list"

                lateinit var expected: HttpResponse<List<String>>

                `given http call`<List<String>> {

                    url = "http://localhost:8001/hello"

                    withResult { expected = this }
                }

                `given http call`<List<String>> {

                    url = "http://localhost:8080/req/flemontreer/hello"
                } `assert that` { step ->

                    eq(expected.status, step.status)
                    eq(expected.body, step.body)
                    expected.headers.forEach {
                        `true`(step.headers.containsKey(it.key))
                        eq(it.value, step.headers[it.key])
                    }
                }
            },

            scenario {

                name = "status code is correctly forwarded"

                lateinit var expected: HttpResponse<JsonMap>

                `given http call`<JsonMap> {

                    url = "http://localhost:8001/whatever"

                    withResult { expected = this }
                }

                `given http call`<JsonMap> {

                    url = "http://localhost:8080/req/flemontreer/whatever"
                } `assert that` { step ->

                    eq(expected.status, step.status)
                    eq(expected.body, step.body)
                    expected.headers.forEach {
                        `true`(step.headers.containsKey(it.key))
                        eq(it.value, step.headers[it.key])
                    }
                }
            },

            scenario {

                name = "multipart/form-data is correctly handled"

                lateinit var expected: HttpResponse<String>

                `given http call`<String> {

                    url = "http://localhost:8001/send-letter"
                    method = "POST"
                    body = multipartBody(
                            filePart {
                                file = File(this.javaClass.getResource("/santa_claus.txt").toURI())
                                name = "letter"
                                filename = "my_letter.txt"
                                contentType = "text/plain"
                            },
                            parameterPart {
                                name = "to"
                                value = "santa claus"
                            }
                    )

                    withResult { expected = this }
                }

                `given http call`<JsonMap> {

                    url = "http://localhost:8080/req/flemontreer/send-letter"
                    method = "POST"
                    body = multipartBody(
                            filePart {
                                file = File(this.javaClass.getResource("/santa_claus.txt").toURI())
                                name = "letter"
                                filename = "my_letter.txt"
                                contentType = "text/plain"
                            },
                            parameterPart {
                                name = "to"
                                value = "santa claus"
                            }
                    )

                } `assert that` { step ->

                    eq(expected.status, step.status)
                    eq(expected.body, step.body)
                    expected.headers.forEach {
                        `true`(step.headers.containsKey(it.key))
                        eq(it.value, step.headers[it.key])
                    }
                }
            },

            beforeEach = {
                startSampleApi()
            }
    )
}