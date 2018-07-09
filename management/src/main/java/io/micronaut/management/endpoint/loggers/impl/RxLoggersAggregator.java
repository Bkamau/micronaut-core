package io.micronaut.management.endpoint.loggers.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.util.StatusPrinter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.management.endpoint.loggers.LoggersAggregator;
import io.micronaut.management.endpoint.loggers.LoggersEndpoint;
import io.reactivex.Single;
import org.reactivestreams.Publisher;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;

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

    // TODO A lot of this is specific to Logback... to be abstracted/moved elsewhere.

    public static final String NOT_SPECIFIED = "NOT_SPECIFIED";

    @Override
    public Publisher<Map<String, Object>> aggregate() {

        // Assumes SLF4J is bound to logback.
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(loggerContext);

        Map<String, Object> loggers = new HashMap<>();

        // TODO Reimplement async.
        loggerContext.getLoggerList()
                .stream()
                .forEach((log) -> {
                    Map<String, String> levels = new HashMap<>();

                    Level configuredLevel = log.getLevel();
                    Level effectiveLevel = log.getEffectiveLevel();

                    levels.put("configuredLevel",
                            configuredLevel != null ? configuredLevel.toString() : NOT_SPECIFIED);
                    levels.put("effectiveLevel",
                            effectiveLevel != null ? effectiveLevel.toString() : NOT_SPECIFIED);

                    loggers.put(log.getName(), levels);
                });

        Map<String, Object> loggersInfo = new HashMap<>();
        loggersInfo.put("loggers", loggers);

        return Single.just(loggersInfo).toFlowable();
    }

    private boolean hasAppenders(ch.qos.logback.classic.Logger log) {
        Iterator<Appender<ILoggingEvent>> it = log.iteratorForAppenders();
        return it.hasNext();
    }

}
