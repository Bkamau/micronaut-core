/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.management.endpoint.loggers

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Matthew Moss
 * @since 1.0
 */
class LoggersEndpointSpec extends Specification {

    static final ALL = 'ALL'
    static final ERROR = 'ERROR'
    static final WARN = 'WARN'
    static final INFO = 'INFO'
    static final DEBUG = 'DEBUG'
    static final TRACE = 'TRACE'
    static final FATAL = 'FATAL'
    static final OFF = 'OFF'
    static final NOT_SPECIFIED = 'NOT_SPECIFIED'

    // Loggers configured in logback-test.xml
    static final configuredLoggers = [
            ROOT: [configuredLevel: INFO, effectiveLevel: INFO],
            errors: [configuredLevel: ERROR, effectiveLevel: ERROR],
            'no-appenders': [configuredLevel: WARN, effectiveLevel: WARN],
            'no-level': [configuredLevel: NOT_SPECIFIED, effectiveLevel: INFO],
            'no-config': [configuredLevel: NOT_SPECIFIED, effectiveLevel: INFO],
    ]

    // Some known loggers internal to micronaut
    static final expectedBuiltinLoggers = ['io.micronaut', 'io.netty']

    static final expectedLogLevels = [
            ALL, TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF, NOT_SPECIFIED]


    @Shared EmbeddedServer server
    @Shared RxHttpClient client

    void setup() {
        server = ApplicationContext.run(EmbeddedServer)
        client = server.applicationContext.createBean(RxHttpClient, server.URL)
    }

    void cleanup() {
        client.close()
        server.close()
    }

    void "test that the configured loggers are returned from the endpoint"() {
        when:
        def response = client.exchange(HttpRequest.GET("/loggers"), Map).blockingFirst()

        then:
        response.status == HttpStatus.OK

        when:
        def result = response.body()

        then:
        result.containsKey 'loggers'

        and: 'we have all loggers expected from configuration'
        configuredLoggers.every { log, levels ->
            result.loggers.containsKey log
            result.loggers[log] == levels
        }

        and: 'we have some expected builtin loggers'
        expectedBuiltinLoggers.every {
            result.loggers.containsKey it
        }
    }

    void "test that the expected log levels are returned from the endpoint"() {
        when:
        def response = client.exchange(HttpRequest.GET("/loggers"), Map)
                .blockingFirst()

        then:
        response.status == HttpStatus.OK

        when:
        def result = response.body()

        then:
        result.levels == expectedLogLevels
    }

    void "test that log levels can be retrieved via the loggers endpoint"() {
        given:
        def url = '/loggers/errors'

        when:
        def response = client.exchange(HttpRequest.GET(url), Map)
                .blockingFirst()

        then:
        response.status == HttpStatus.OK
        response.body() == [configuredLevel: ERROR, effectiveLevel: ERROR]
    }

    void "test that log levels can be configured via the loggers endpoint"() {
        given:
        def url = '/loggers/errors'
        def data = [configuredLevel: OFF]

        when:
        def response = client.exchange(HttpRequest.POST(url, data))
                .blockingFirst()

        then:
        response.status == HttpStatus.NO_CONTENT
    }

}
