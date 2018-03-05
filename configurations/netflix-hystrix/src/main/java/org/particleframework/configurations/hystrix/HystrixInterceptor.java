/*
 * Copyright 2018 original authors
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
package org.particleframework.configurations.hystrix;

import com.netflix.hystrix.*;
import org.particleframework.aop.InterceptPhase;
import org.particleframework.aop.MethodInterceptor;
import org.particleframework.aop.MethodInvocationContext;
import org.particleframework.context.annotation.Property;
import org.particleframework.context.exceptions.ConfigurationException;
import org.particleframework.core.async.publisher.Publishers;
import org.particleframework.core.convert.ConversionService;
import org.particleframework.core.naming.NameUtils;
import org.particleframework.core.reflect.ReflectionUtils;
import org.particleframework.core.type.ReturnType;
import org.particleframework.core.util.ArrayUtils;
import org.particleframework.core.util.StringUtils;
import rx.Observable;
import rx.SingleSubscriber;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * A {@link MethodInterceptor} that adds support for decorating methods for Hystrix
 *
 * @author graemerocher
 * @since 1.0
 */
@Singleton
public class HystrixInterceptor implements MethodInterceptor<Object, Object> {

    public static final int POSITION = InterceptPhase.RETRY.getPosition() + 10;

    private final Map<Method, HystrixCommand.Setter> setterMap = new ConcurrentHashMap<>();
    private final Map<Method, HystrixObservableCommand.Setter> observableSetterMap = new ConcurrentHashMap<>();
    @Override
    public int getOrder() {
        return POSITION;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Class<org.particleframework.configurations.hystrix.annotation.HystrixCommand> annotationType = org.particleframework.configurations.hystrix.annotation.HystrixCommand.class;
        org.particleframework.configurations.hystrix.annotation.Hystrix settings = context.getAnnotation(org.particleframework.configurations.hystrix.annotation.Hystrix.class);
        org.particleframework.configurations.hystrix.annotation.HystrixCommand cmd = context.getAnnotation(annotationType);
        if(cmd == null) {
            return context.proceed();
        }
        else {

            String hystrixGroup = resolveHystrixGroup(context, settings);
            String commandName = cmd.value();
            if(StringUtils.isEmpty(commandName)) {
                commandName = context.getMethodName();
            }
            boolean hasSettings = settings != null;
            String threadPool = hasSettings ? settings.threadPool() : null;
            boolean wrapExceptions = hasSettings && settings.wrapExceptions();

            ReturnType<Object> returnType = context.getReturnType();
            Class<Object> javaReturnType = returnType.getType();

            boolean isFuture = CompletableFuture.class.isAssignableFrom(javaReturnType);
            if(Publishers.isPublisher(javaReturnType) || isFuture) {
                String finalCommandName = commandName;
                HystrixObservableCommand.Setter setter = observableSetterMap.computeIfAbsent(context.getTargetMethod(), method ->
                        buildObservableSetter(hystrixGroup, finalCommandName,settings)
                );

                HystrixObservableCommand<Object> command = new HystrixObservableCommand<Object>(setter) {
                    @SuppressWarnings("unchecked")
                    @Override
                    protected Observable<Object> construct() {
                        Object result = context.proceed();
                        return ConversionService.SHARED.convert(result, Observable.class)
                                .orElseThrow(()->new IllegalStateException("Unsupported Reactive type: " + javaReturnType)) ;
                    }

                    @Override
                    protected boolean isFallbackUserDefined() {
                        return super.isFallbackUserDefined();
                    }

                    @Override
                    protected Observable<Object> resumeWithFallback() {
                        return super.resumeWithFallback();
                    }

                    @Override
                    protected boolean shouldNotBeWrapped(Throwable underlying) {
                        return !wrapExceptions || super.shouldNotBeWrapped(underlying);
                    }
                };
                if(isFuture) {
                    CompletableFuture future = new CompletableFuture();
                    command.toObservable().toSingle().subscribe(new SingleSubscriber<Object>() {
                        @Override
                        public void onSuccess(Object value) {
                            future.complete(value);
                        }

                        @Override
                        public void onError(Throwable error) {
                            future.completeExceptionally(error);
                        }
                    });
                    return future;
                }
                else {
                    return ConversionService.SHARED.convert(command.toObservable(), returnType.asArgument())
                            .orElseThrow(()-> new IllegalStateException("Unsupported Reactive type: " + javaReturnType));
                }
            }
            else {
                String finalCommandName = commandName;
                HystrixCommand.Setter setter = setterMap.computeIfAbsent(context.getTargetMethod(), method ->
                        buildSetter(hystrixGroup, finalCommandName, threadPool, settings)
                );

                HystrixCommand<Object> hystrixCommand = new HystrixCommand<Object>(setter) {
                    @Override
                    protected Object run() throws Exception {
                        return context.proceed();
                    }

                    @Override
                    protected boolean isFallbackUserDefined() {
                        return super.isFallbackUserDefined();
                    }

                    @Override
                    protected Object getFallback() {
                        return super.getFallback();
                    }

                    @Override
                    protected boolean shouldNotBeWrapped(Throwable underlying) {
                        return !wrapExceptions || super.shouldNotBeWrapped(underlying);
                    }
                };
                try {
                    return hystrixCommand.execute();
                } catch (Exception e) {

                    if(!wrapExceptions) {
                        // unpack the original exception
                        //noinspection ConstantConditions
                        if(e instanceof ExecutionException) {
                            Throwable cause = e.getCause();
                            if(cause instanceof RuntimeException) {
                                throw (RuntimeException)cause;
                            }
                        }
                    }
                    throw e;
                }
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private HystrixCommand.Setter buildSetter(String hystrixGroup, String commandName, String threadPool, org.particleframework.configurations.hystrix.annotation.Hystrix settings) {
        HystrixCommand.Setter setter = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(hystrixGroup));
        HystrixCommandKey commandKey = HystrixCommandKey.Factory.asKey(commandName);
        setter.andCommandKey(commandKey);
        if(StringUtils.isNotEmpty(threadPool)) {
            setter.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPool));
        }
        if(settings != null) {
            Property[] properties = settings.commandProperties();
            if (ArrayUtils.isNotEmpty(properties)) {

                HystrixCommandProperties.Setter instance = buildCommandProperties(properties);
                setter.andCommandPropertiesDefaults(
                        instance
                );
            }
            Property[] threadPoolProps = settings.threadPoolProperties();
            if (ArrayUtils.isNotEmpty(threadPoolProps)) {

                HystrixThreadPoolProperties.Setter threadPoolPropsInstance = buildThreadPoolProperties(properties);
                setter.andThreadPoolPropertiesDefaults(
                        threadPoolPropsInstance
                );

            }
        }
        return setter;
    }

    private HystrixCommandProperties.Setter buildCommandProperties(Property[] properties) {
        Class<HystrixCommandProperties.Setter> setterClass = HystrixCommandProperties.Setter.class;
        HystrixCommandProperties.Setter instance = HystrixCommandProperties.defaultSetter();
        return buildPropertiesDynamic(properties, setterClass, instance);
    }

    private HystrixThreadPoolProperties.Setter buildThreadPoolProperties(Property[] properties) {
        return buildPropertiesDynamic(
                properties,
                HystrixThreadPoolProperties.Setter.class,
                HystrixThreadPoolProperties.defaultSetter());
    }

    private <T> T buildPropertiesDynamic(Property[] properties, Class<T> setterClass, T instance) {
        for (Property property : properties) {
            String name = property.name();
            if(StringUtils.isNotEmpty(name)) {
                String value = property.value();
                if(StringUtils.isNotEmpty(value)) {
                    String methodName = "with" + NameUtils.capitalize(name);
                    Optional<Method> method = ReflectionUtils.findMethodsByName(setterClass, methodName)
                            .findFirst();
                    if(method.isPresent()) {
                        Method m = method.get();
                        Class<?>[] parameterTypes = m.getParameterTypes();
                        if(parameterTypes.length == 1) {
                            Optional<?> converted = ConversionService.SHARED.convert(value, parameterTypes[0]);
                            if(converted.isPresent()) {

                                try {
                                    Object v = converted.get();
                                    ReflectionUtils.invokeMethod(instance, m, v);
                                } catch (Exception e) {
                                    throw new ConfigurationException("Invalid Hystrix Property: " + name);
                                }
                            }
                        }
                    }
                }

            }
        }
        return instance;
    }

    @SuppressWarnings("Duplicates")
    private HystrixObservableCommand.Setter buildObservableSetter(String hystrixGroup, String commandName, org.particleframework.configurations.hystrix.annotation.Hystrix settings) {
        HystrixObservableCommand.Setter setter = HystrixObservableCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(hystrixGroup));
        HystrixCommandKey commandKey = HystrixCommandKey.Factory.asKey(commandName);
        setter.andCommandKey(commandKey);

        if(settings != null) {
            Property[] properties = settings.commandProperties();
            if(ArrayUtils.isNotEmpty(properties)) {

                HystrixCommandProperties.Setter instance = buildCommandProperties(properties);
                setter.andCommandPropertiesDefaults(
                        instance
                );
            }

        }
        return setter;
    }

    private String resolveHystrixGroup(MethodInvocationContext<Object, Object> context,
                                       org.particleframework.configurations.hystrix.annotation.Hystrix ann) {
        String group = ann != null ? ann.group() : null;
        if(StringUtils.isEmpty(group)) {
            return context.getValue("org.particleframework.http.client.Client", "id", String.class).orElse(context.getDeclaringType().getSimpleName());
        }
        return group;
    }
}