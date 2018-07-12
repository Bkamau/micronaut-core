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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

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

    // TODO Temporary?
    public LoggerConfiguration(Logger logger) {
        this.name = logger.getName();
        this.configuredLevel = toLogLevel(logger.getLevel());
        this.effectiveLevel = toLogLevel(logger.getEffectiveLevel());
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

    private static LogLevel toLogLevel(Level level) {
        if (level == null) {
            return LogLevel.NOT_SPECIFIED;
        }
        switch (level.toInt()) {
            case Level.ALL_INT:
                return LogLevel.ALL;
            case Level.TRACE_INT:
                return LogLevel.TRACE;
            case Level.DEBUG_INT:
                return LogLevel.DEBUG;
            case Level.INFO_INT:
                return LogLevel.INFO;
            case Level.WARN_INT:
                return LogLevel.WARN;
            case Level.ERROR_INT:
                return LogLevel.ERROR;
            case Level.OFF_INT:
                return LogLevel.OFF;
            default:
                throw new IllegalStateException("Unknown log level " + level.toString() + ", " + level.toInt());
        }
    }

}
