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

package io.micronaut.management.endpoint.loggers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.management.endpoint.Endpoint;
import io.micronaut.management.endpoint.EndpointConfiguration;
import io.micronaut.management.endpoint.Read;
import io.micronaut.management.endpoint.Write;
import io.reactivex.Single;

import javax.annotation.Nullable;

/**
 * <p>Exposes an {@link Endpoint} to manage loggers.</p>
 *
 * @author Matthew Moss
 * @since 1.0
 */
@Endpoint(id = LoggersEndpoint.NAME,
        defaultEnabled = LoggersEndpoint.DEFAULT_ENABLED,
        defaultSensitive = LoggersEndpoint.DEFAULT_SENSITIVE)
public class LoggersEndpoint {

    /**
     * Endpoint name.
     */
    public static final String NAME = "loggers";

    /**
     * Endpoint configuration prefix.
     */
    public static final String PREFIX = EndpointConfiguration.PREFIX + "." + NAME;

    /**
     * Endpoint default enabled.
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Endpoint default sensitivity.
     */
    public static final boolean DEFAULT_SENSITIVE = false;

    private LoggersAggregator loggersAggregator;

    /**
     * @param loggersAggregator The {@link LoggersAggregator}
     */
    public LoggersEndpoint(LoggersAggregator loggersAggregator) {
        this.loggersAggregator = loggersAggregator;
    }

    /**
     * Returns the configured loggers and their configured and effective levels.
     * Also returns the list of available levels.
     *
     * @return map of available levels and configured loggers with their levels
     */
    @Read
    public Single loggers() {
        return Single.fromPublisher(loggersAggregator.loggers());
    }

    /**
     * Returns the configured log levels for the named logger. If the named
     * logger is not found, one will be created with default log levels.
     *
     * @param name the logger to find
     * @return map of configured and effective log levels for the named logger
     */
    @Read
    public Single loggerLevels(@QueryValue String name) {
            return loggersAggregator.getLogger(name);
    }

    /**
     * Sets the log level for the named logger. If the named logger is not found,
     * one will be created and have its log level set to the provided value.
     *
     * @param name the logger to find and configure
     * @param configuredLevel the log level to set on the named logger
     * @return status level 204: successful, no content
     */
    @Write
    public HttpResponse configureLogLevel(@QueryValue String name, @Nullable String configuredLevel) {
        loggersAggregator.setLogLevel(name, configuredLevel);
        return HttpResponse.noContent();
    }

}
