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

    static final ERROR = 'ERROR'
    static final WARN = 'WARN'
    static final INFO = 'INFO'
    static final DEBUG = 'DEBUG'
    static final TRACE = 'TRACE'
    static final NOT_SPECIFIED = 'NOT_SPECIFIED'

    void "test that the loggers endpoint is not available when disabled via config"() {
        given:
        ApplicationContext context = ApplicationContext.run(['endpoints.loggers.enabled': false])

        expect:
        !context.containsBean(LoggersEndpoint)

        cleanup:
        context.close()
    }

    void "test that the loggers endpoint is available when enabled via config"() {
        given:
        ApplicationContext context = ApplicationContext.run(['endpoints.loggers.enabled': true])

        expect:
        context.containsBean(LoggersEndpoint)

        cleanup:
        context.close()
    }

    void "test that the loggers configured in logback-test.xml are returned from the endpoint"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.GET("/loggers"), Map).blockingFirst()
        def result = response.body()

        then:
        response.status == HttpStatus.OK
        result.containsKey 'loggers'

        and:
        result.loggers == [
                ROOT: [configuredLevel: INFO, effectiveLevel: INFO],
                foobar: [configuredLevel: ERROR, effectiveLevel: ERROR],
                debugging: [configuredLevel: DEBUG, effectiveLevel: DEBUG],
                'inherit-parent': [configuredLevel: NOT_SPECIFIED, effectiveLevel: INFO],
        ]

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

    void "test that log levels of a known logger can be retrieved via the loggers endpoint"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.GET("/loggers/zzz"), Integer).blockingFirst()

        then:
        response.status == HttpStatus.OK

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

    void "test that can configure level of a known logger via the loggers endpoint"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.POST("/loggers/zzz", [configuredLevel: 'OFF']), String).blockingFirst()

        then:
        response.status == HttpStatus.NO_CONTENT

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

}
