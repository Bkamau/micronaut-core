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

/**
 * <p>Represents the configuration of a {@link LoggingSystem} logger.</p>
 *
 * @author Matthew Moss
 * @since 1.0
 */
public class LoggerConfiguration {

    private final String name;
    private final LogLevel configuredLevel;
    private final LogLevel effectiveLevel;

    /**
     * Create an immutable {@link LoggerConfiguration} instance.
     * @param name The name of the logger
     * @param configuredLevel The configured log level for the logger
     * @param effectiveLevel The effective log level for the logger
     */
    public LoggerConfiguration(String name, LogLevel configuredLevel,
                               LogLevel effectiveLevel) {
        this.name = name;
        this.configuredLevel = configuredLevel;
        this.effectiveLevel = effectiveLevel;
    }

    public String getName() {
        return name;
    }

    public LogLevel getConfiguredLevel() {
        return configuredLevel;
    }

    public LogLevel getEffectiveLevel() {
        return effectiveLevel;
    }

}
