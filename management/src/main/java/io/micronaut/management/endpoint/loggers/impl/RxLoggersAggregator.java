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

import io.micronaut.context.annotation.Requires;
import io.micronaut.management.endpoint.loggers.*;
import io.reactivex.Single;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.util.*;

/**
 * <p>Default implementation of {@link LoggersAggregator}.</p>
 *
 * @author Matthew Moss
 * @since 1.0
 */
@Singleton
@Requires(beans = LoggersEndpoint.class)
public class RxLoggersAggregator implements LoggersAggregator {

    private LoggingSystem loggingSystem;

    RxLoggersAggregator(LoggingSystem loggingSystem) {
        this.loggingSystem = loggingSystem;
    }

    @Override
    public Publisher<Map<String, Object>> loggers() {
        Map<String, Object> result = new HashMap<>();

        LogLevel[] logLevels = LogLevel.values();
        List<String> levels = new ArrayList<>(logLevels.length);
        for (LogLevel level : logLevels) {
            levels.add(level.name());
        }
        result.put("levels", levels);

        Collection<LoggerConfiguration> configs = loggingSystem.getLoggers();
        Map<String, Object> loggers = new HashMap<>(configs.size());
        for (LoggerConfiguration config : configs) {
            loggers.put(config.getName(), buildLogInfo(config));
        }
        result.put("loggers", loggers);

        return Single.just(result).toFlowable();
    }

    @Override
    public Single getLogger(String name) {
        LoggerConfiguration config = loggingSystem.getLogger(name);
        return Single.just(buildLogInfo(config));
    }

    @Override
    public void setLogLevel(String name, String level) {
        // TODO
    }

    private Map<String, String> buildLogInfo(LoggerConfiguration config) {
        Map<String, String> info = new HashMap<>();
        info.put("configuredLevel", config.getConfiguredLevel().name());
        info.put("effectiveLevel", config.getEffectiveLevel().name());
        return info;
    }

}
