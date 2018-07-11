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

        then: 'the request was successful'
        response.status == HttpStatus.OK
        result.containsKey 'loggers'

        and: 'we have some expected builtin loggers'
        ['io.micronaut', 'io.netty'].every {
            result.loggers.containsKey it
        }

        and: 'we have all loggers and expected levels from logback-test.xml'
        [ROOT: [configuredLevel: INFO, effectiveLevel: INFO],
         errors: [configuredLevel: ERROR, effectiveLevel: ERROR],
         'no-appenders': [configuredLevel: WARN, effectiveLevel: WARN],
         'no-level': [configuredLevel: NOT_SPECIFIED, effectiveLevel: INFO],
         'no-config': [configuredLevel: NOT_SPECIFIED, effectiveLevel: INFO],
        ].every { log, levels ->
            result.loggers.containsKey log
            result.loggers[log] == levels
        }

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

    void "test that a request for log levels of a non-existent log returns NOT_FOUND"() {
        given:
        def url = '/loggers/.xyz.abc.'     // log '.xyz.abc.' does not exist
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.GET(url), Integer).blockingFirst()

        then:
        response.status == HttpStatus.NOT_FOUND

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

    void "test that log levels of an existing log can be retrieved via the loggers endpoint"() {
        given:
        def url = '/loggers/errors'     // log 'errors' exists in logback-test.xml
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.GET(url), Integer).blockingFirst()

        then:
        response.status == HttpStatus.OK

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

    void "test that an attempt to configure a non-existent log returns NOT_FOUND"() {
        given:
        def url = '/loggers/.xyz.abc.'     // log '.xyz.abc.' does not exist
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)

        when:
        def response = rxClient.exchange(HttpRequest.POST(url, [configuredLevel: 'OFF']), String).blockingFirst()

        then:
        response.status == HttpStatus.NOT_FOUND

        cleanup:
        rxClient.close()
        embeddedServer.close()
    }

    void "test that can configure level of an existing log via the loggers endpoint"() {
        given:
        def url = '/loggers/errors'     // log 'errors' exists in logback-test.xml
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
