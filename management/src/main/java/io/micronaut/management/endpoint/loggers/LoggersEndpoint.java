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

import io.micronaut.management.endpoint.Endpoint;
import io.micronaut.management.endpoint.EndpointConfiguration;
import io.micronaut.management.endpoint.Read;
import io.reactivex.Single;

/**
 * <p>Exposes an {@link Endpoint} to manage loggers.</p>
 *
 * @author Matthew Moss
 * @since 1.0
 */
@Endpoint(id = LoggersEndpoint.NAME, defaultSensitive = LoggersEndpoint.DEFAULT_SENSITIVE)
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

    @Read
    public Single loggers() {
        return Single.fromPublisher(loggersAggregator.aggregate());
    }
}
