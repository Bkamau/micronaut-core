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
package io.micronaut.management.endpoint.loggers.impl;

//import ch.qos.logback.classic.Level;
//import ch.qos.logback.classic.Logger;
//import ch.qos.logback.classic.LoggerContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.management.endpoint.loggers.LogLevel;
import io.micronaut.management.endpoint.loggers.LoggerConfiguration;
import io.micronaut.management.endpoint.loggers.LoggersEndpoint;
import io.micronaut.management.endpoint.loggers.LoggingSystem;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Singleton
@Requires(beans = LoggersEndpoint.class)
public class LogbackLoggingSystem implements LoggingSystem {

    /**
     * Collect list of all logger configurations.
     *
     * @return list of all logger configurations
     */
    @Override
    public Collection<LoggerConfiguration> getLoggers() {
        //LoggerContext context = getLoggerContext();

//        return context.getLoggerList()
//                .stream()
//                .map(LogbackLoggingSystem::buildLoggerConfiguration)
//                .collect(Collectors.toList());
        return new ArrayList<>();
    }

    /**
     * Find the specified logger and return its configuration.
     *
     * @param name the logger to find
     * @return the logger configuration of the found logger
     */
    @Override
    public LoggerConfiguration getLogger(String name) {
//        LoggerContext context = getLoggerContext();
//        Logger logger = context.getLogger(name);
//
//        return buildLoggerConfiguration(logger);
        return new LoggerConfiguration("foo", LogLevel.OFF, LogLevel.OFF);
    }

    /**
     * Configure the log level of the specified logger.
     *
     * @param name the logger to find
     * @param level the log level to configure for the found logger
     */
    // TODO Implement.
    @Override
    public void setLogLevel(String name, LogLevel logLevel) {
//        LoggerContext context = getLoggerContext();
//        Logger logger = context.getLogger(name);
//
//        logger.setLevel(toLevel(logLevel));
    }

//    private static LoggerContext getLoggerContext() {
//        return (LoggerContext) LoggerFactory.getILoggerFactory();
//    }
//
//    private static LoggerConfiguration buildLoggerConfiguration(Logger logger) {
//        LogLevel configuredLevel = toLogLevel(logger.getLevel());
//        LogLevel effectiveLevel = toLogLevel(logger.getEffectiveLevel());
//
//        return new LoggerConfiguration(logger.getName(), configuredLevel,
//                effectiveLevel);
//    }
//
//    private static LogLevel toLogLevel(Level level) {
//        if (level == null) {
//            return LogLevel.NOT_SPECIFIED;
//        }
//        else {
//            return LogLevel.valueOf(level.toString());
//        }
//    }
//
//    private static Level toLevel(LogLevel logLevel) {
//        if (logLevel == LogLevel.NOT_SPECIFIED) {
//            return null;
//        }
//        else {
//            return Level.valueOf(logLevel.name());
//        }
//    }

}
