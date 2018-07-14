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

import io.reactivex.Single;
import org.reactivestreams.Publisher;

import javax.validation.constraints.NotBlank;

/**
 * <p>Aggregates all loggers into a single response.</p>
 *
 * @author Matthew Moss
 * @since 1.0
 */
public interface LoggersAggregator<T> {

    /**
     * Methods to collect Logger information.
     *
     */
    Publisher<T> loggers();

    Single<T> getLogger(@NotBlank String name);

    void setLogLevel(@NotBlank String name, String level);
}
