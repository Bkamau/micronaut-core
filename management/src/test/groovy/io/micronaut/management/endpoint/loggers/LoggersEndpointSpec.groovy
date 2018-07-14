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
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
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
    private configuredLoggers = [
            ROOT: [configuredLevel: INFO, effectiveLevel: INFO],
            errors: [configuredLevel: ERROR, effectiveLevel: ERROR],
            'no-appenders': [configuredLevel: WARN, effectiveLevel: WARN],
            'no-level': [configuredLevel: NOT_SPECIFIED, effectiveLevel: INFO],
            'no-config': [configuredLevel: NOT_SPECIFIED, effectiveLevel: INFO],
    ]

    // Some known loggers internal to micronaut
    private expectedBuiltinLoggers = ['io.micronaut', 'io.netty']

    private expectedLogLevels = [ALL, TRACE, DEBUG, INFO, WARN, ERROR, FATAL,
                                 OFF, NOT_SPECIFIED]

    void "test that the configured loggers are returned from the endpoint"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.GET("/loggers"), Map).blockingFirst()
        def result = response.body()

        then: 'the request was successful'
        response.status == HttpStatus.OK
        result.containsKey 'loggers'

        and: 'we have all loggers and expected levels from configuration'
        configuredLoggers.every { log, levels ->
            result.loggers.containsKey log
            result.loggers[log] == levels
        }

        and: 'we have some expected builtin loggers'
        expectedBuiltinLoggers.every {
            result.loggers.containsKey it
        }

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

    void "test that the expected log levels are returned from the endpoint"() {
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.GET("/loggers"), Map).blockingFirst()
        def result = response.body()

        then: 'the request was successful'
        response.status == HttpStatus.OK
        result.containsKey 'levels'

        and: 'we have all the expected log levels'
        result.levels == expectedLogLevels
    }

    void "test that log levels can be retrieved via the loggers endpoint"() {
        given:
        def url = '/loggers/errors'
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.GET(url), Map).blockingFirst()

        then:
        response.status == HttpStatus.OK

        when:
        def result = response.body()

        then:
        response.body() == [configuredLevel: ERROR, effectiveLevel: ERROR]

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

    void "test that log levels can be configured via the loggers endpoint"() {
        given:
        def url = '/loggers/errors'
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.POST(url, [configuredLevel: 'OFF']), String).blockingFirst()

        then:
        response.status == HttpStatus.NO_CONTENT

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

}
