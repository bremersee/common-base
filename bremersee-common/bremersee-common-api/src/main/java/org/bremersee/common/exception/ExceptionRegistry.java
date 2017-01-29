/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bremersee.common.model.ThrowableDto;
import org.bremersee.common.model.ThrowableMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * <p>
 * A registry of runtime exceptions that can be used for (restful) client server communication.
 * The server can register it's exceptions and the client can get them and rethrow them when an
 * appropriate status code was returned.
 * </p>
 *
 * @author Christian Bremer
 */
public abstract class ExceptionRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionRegistry.class);

    /**
     * Registered exceptions accessible by their class.
     */
    private static final Map<Class<? extends RuntimeException>, ExceptionDescription> CLASS_MAP = new HashMap<>();

    /**
     * Registered exceptions accessible by their custom status code.
     */
    private static final Map<Integer, ExceptionDescription> CUSTOM_CODE_MAP = new HashMap<>();

    /**
     * Registered exceptions accessible by their HTTP status code.
     */
    private static final Map<Integer, List<ExceptionDescription>> HTTP_CODE_MAP = new HashMap<>();

    /**
     * Init flag.
     */
    private static boolean init = false;

    static {
        // Register all exceptions that are listed in {@link Exceptions}.
        synchronized (CLASS_MAP) {
            if (!init) {
                init = true;
                for (Exceptions e : Exceptions.values()) {
                    register(e.getRuntimeExceptionClass(), e.getDefaultMessage(), e.getCustomStatusCode(),
                            e.getHttpStatusCode(), false);
                }
            }
        }
    }

    /**
     * Never construct this utility class.
     */
    private ExceptionRegistry() {
        super();
    }

    /**
     * Register a runtime exception by it's instance.
     *
     * @param exception         the runtime exception
     * @param replaceDuplicates if it's {@code false}, no duplicate exceptions (classes and custom status codes)
     *                          will be accepted
     * @throws IllegalArgumentException if the parameter {@code replaceDuplicates} is {@code false} and a duplicate
     *                                  exception should be registered
     */
    public static void register(RuntimeException exception, final boolean replaceDuplicates) {
        Validate.notNull(exception, "Exception must not be null.");
        final Integer customStatusCode;
        final Integer httpStatusCode;
        if (exception instanceof StatusCodeAwareException) {
            StatusCodeAwareException statusCodeAwareException = (StatusCodeAwareException) exception;
            customStatusCode = statusCodeAwareException.getCustomStatusCode();
            httpStatusCode = statusCodeAwareException.getHttpStatusCode();
        } else {
            customStatusCode = null;
            httpStatusCode = null;
        }
        register(exception.getClass(), exception.getMessage(), customStatusCode, httpStatusCode, replaceDuplicates);
    }

    /**
     * Register a runtime exception by it's class name, the default message and it's status codes.
     *
     * @param runtimeExceptionClassName the class name of the runtime exception
     * @param defaultMessage            the default message (optional - but should be set)
     * @param customStatusCode          the unique status code (optional)
     * @param httpStatusCode            the HTTP status code (optional)
     * @param replaceDuplicates         if it's {@code false}, no duplicate exceptions (classes and custom status codes)
     *                                  will be accepted
     * @throws IllegalArgumentException if the parameter {@code runtimeExceptionClassName} is null or blank;
     *                                  if the runtime exception class could not be found;
     *                                  if the parameter {@code replaceDuplicates} is {@code false} and a duplicate
     *                                  exception should be registered
     */
    public static void register(final String runtimeExceptionClassName, final String defaultMessage,
                                final Integer customStatusCode, final Integer httpStatusCode,
                                final boolean replaceDuplicates) {

        Validate.notBlank(runtimeExceptionClassName, "Runtime exception class name must not be null or blank.");
        try {
            //noinspection unchecked
            register((Class<? extends RuntimeException>) Class.forName(runtimeExceptionClassName), defaultMessage,
                    customStatusCode, httpStatusCode, replaceDuplicates);

        } catch (ClassNotFoundException e) {
            IllegalArgumentException iae = new IllegalArgumentException(e);
            LOG.error("Registering exception [" + runtimeExceptionClassName + "] failed.", iae); // NOSONAR
            throw iae;
        }
    }

    /**
     * Register a runtime exception by it's class, the default message and it's status codes.
     *
     * @param clazz             the class of the runtime exception
     * @param defaultMessage    the default message (optional - but should be set)
     * @param customStatusCode  the unique status code (optional)
     * @param httpStatusCode    the HTTP status code (optional)
     * @param replaceDuplicates if it's {@code false}, no duplicate exceptions (classes and custom status codes)
     *                          will be accepted
     * @throws IllegalArgumentException if the parameter {@code clazz} is null;
     *                                  if the parameter {@code replaceDuplicates} is {@code false} and a duplicate
     *                                  exception should be registered
     */
    public static void register(final Class<? extends RuntimeException> clazz,
                                final String defaultMessage, final Integer customStatusCode,
                                final Integer httpStatusCode,
                                final boolean replaceDuplicates) {

        synchronized (CLASS_MAP) {
            Validate.notNull(clazz, "Exception class must not be null.");
            ExceptionDescription description = new ExceptionDescription(clazz, defaultMessage, customStatusCode,
                    httpStatusCode);

            if (!replaceDuplicates) {
                Validate.isTrue(!existsByRuntimeExceptionClass(clazz), "Runtime exception ["
                        + clazz.getName() + "] is already registered."); // NOSONAR
                Validate.isTrue(!existsByCustomStatusCode(customStatusCode),
                        "Runtime exception with custom status code[" + customStatusCode
                                + "] is already registered.");
            }

            ExceptionDescription oldDescription = CLASS_MAP.put(clazz, description);
            if (oldDescription != null && oldDescription.getCustomStatusCode() != null) {
                CUSTOM_CODE_MAP.remove(oldDescription.getCustomStatusCode());
            }
            if (oldDescription != null && oldDescription.getHttpStatusCode() != null) {
                List<ExceptionDescription> list = HTTP_CODE_MAP.get(oldDescription.getHttpStatusCode());
                if (list != null) {
                    list.remove(oldDescription);
                }
            }
            if (description.getCustomStatusCode() != null) {
                CUSTOM_CODE_MAP.put(description.getCustomStatusCode(), description);
            }
            if (description.getHttpStatusCode() != null) {
                HTTP_CODE_MAP.computeIfAbsent(description.getHttpStatusCode(), k -> new LinkedList<>());
                List<ExceptionDescription> list = HTTP_CODE_MAP.get(description.getHttpStatusCode());
                list.add(description);
                Collections.sort(list);
            }
        }
    }

    /**
     * Check whether a runtime exception is registered or not.
     *
     * @param clazz the class of the runtime exception
     * @return {@code true} if the class is registered otherwise {@code false}
     */
    public static boolean existsByRuntimeExceptionClass(final Class<? extends RuntimeException> clazz) {
        return clazz != null && CLASS_MAP.get(clazz) != null;
    }

    /**
     * Check whether a runtime exception with the custom status code is registered or not.
     *
     * @param customStatusCode the custom status code
     * @return {@code true} if the class is registered otherwise {@code false}
     */
    public static boolean existsByCustomStatusCode(final Integer customStatusCode) {
        return customStatusCode != null && CUSTOM_CODE_MAP.get(customStatusCode) != null;
    }

    /**
     * Check whether a runtime exception for the specified HTTP status code is registered or not.
     *
     * @param httpStatusCode the HTTP status code
     * @return {@code true} if the class is registered otherwise {@code false}
     */
    public static boolean existsByHttpStatusCode(final Integer httpStatusCode) {
        return httpStatusCode != null && HTTP_CODE_MAP.get(httpStatusCode) != null
                && !HTTP_CODE_MAP.get(httpStatusCode).isEmpty();
    }

    /**
     * Get a registered runtime exception by the specified class name.<br/>
     * If there's no registered exception with the specified class name, a plain runtime exception will be returned.
     *
     * @param exceptionClassName the class name of the runtime exception
     * @return a runtime exception
     */
    public static RuntimeException getExceptionByClassName(final String exceptionClassName) {
        return getExceptionByClassName(exceptionClassName, null);
    }

    /**
     * Get a registered runtime exception by the specified class name.<br/>
     * If there's no registered exception with the specified class name, a plain runtime exception will be returned.
     *
     * @param exceptionClassName the class name of the runtime exception
     * @param customMessage      an optional message that overwrites the default one
     * @return a runtime exception
     */
    @SuppressWarnings("SameParameterValue")
    public static RuntimeException getExceptionByClassName(final String exceptionClassName,
                                                           final String customMessage) {

        Validate.notBlank(exceptionClassName, "Exception class must not be null or blank.");
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends RuntimeException> exceptionClass = (Class<? extends RuntimeException>) Class
                    .forName(exceptionClassName);
            return getExceptionByClass(exceptionClass, customMessage);

        } catch (ClassNotFoundException e) {
            LOG.error("Getting exception by class name [" + exceptionClassName
                    + "] failed - returning plain runtime exception.", e);
            return StringUtils.isBlank(customMessage) ? new RuntimeException() : new RuntimeException(customMessage);
        }
    }

    /**
     * Get a registered runtime exception by the specified class.<br/>
     * If there's no registered exception with the specified class, a plain runtime exception will be returned.
     *
     * @param exceptionClass the class of the runtime exception
     * @return a runtime exception
     */
    public static RuntimeException getExceptionByClass(final Class<? extends RuntimeException> exceptionClass) {
        return getExceptionByClass(exceptionClass, null);
    }

    /**
     * Get a registered runtime exception by the specified class.<br/>
     * If there's no registered exception with the specified class, a plain runtime exception will be returned.
     *
     * @param exceptionClass the class of the runtime exception
     * @param customMessage  an optional message that overwrites the default one
     * @return a runtime exception
     */
    public static RuntimeException getExceptionByClass(final Class<? extends RuntimeException> exceptionClass,
                                                       final String customMessage) {

        Validate.notNull(exceptionClass, "Exception class msut not be null.");
        final ExceptionDescription description = CLASS_MAP.get(exceptionClass);
        if (description == null) {
            LOG.warn("Exception of class [" + exceptionClass.getName()
                    + "] was not found - returning plain runtime exception.");
            return StringUtils.isBlank(customMessage) ? new RuntimeException() : new RuntimeException(customMessage);
        }
        final String message = StringUtils.isNotBlank(customMessage) ? customMessage : description.getDefaultMessage();
        return (RuntimeException) findThrowable(description.getClazz(), message, null);
    }

    /**
     * Get a registered runtime exception by it's custom status code.<br/>
     * If there's no registered exception with the specified status code, a plain {@link HttpErrorException} will be
     * returned.
     *
     * @param customStatusCode the custom status code of the runtime exception
     * @return a runtime exception
     */
    public static RuntimeException getExceptionByCustomStatusCode(final int customStatusCode) {
        return getExceptionByCustomStatusCode(customStatusCode, null);
    }

    /**
     * Get a registered runtime exception by it's custom status code.<br/>
     * If there's no registered exception with the specified status code, a plain {@link HttpErrorException} will be
     * returned.
     *
     * @param customStatusCode the custom status code of the runtime exception
     * @param customMessage    an optional message that overwrites the default one
     * @return a runtime exception
     */
    @SuppressWarnings("SameParameterValue")
    public static RuntimeException getExceptionByCustomStatusCode(final int customStatusCode,
                                                                  final String customMessage) {

        final ExceptionDescription description = CUSTOM_CODE_MAP.get(customStatusCode);
        if (description == null) {
            LOG.warn("Exception with custom status code [" + customStatusCode
                    + "] was not found - returning plain http error exception.");
            HttpErrorException e;
            if (StringUtils.isBlank(customMessage)) {
                e = new HttpErrorException();
            } else {
                e = new HttpErrorException(customMessage);
            }
            e.setCustomStatusCode(customStatusCode);
            return e;
        }
        final String message = StringUtils.isNotBlank(customMessage) ? customMessage : description.getDefaultMessage();
        return (RuntimeException) findThrowable(description.getClazz(), message, null);
    }

    /**
     * Get a registered runtime exception by it's HTTP status code.<br/>
     * If there's more than one exception with the specified status code, the one with the lowest custom status code
     * will be returned.<br/>
     * If there's no registered exception with the specified status code, a plain {@link HttpErrorException} will be
     * returned.
     *
     * @param httpStatusCode the HTTP status code of the runtime exception
     * @return a runtime exception
     */
    public static RuntimeException getExceptionByHttpStatusCode(final int httpStatusCode) {
        return getExceptionByHttpStatusCode(httpStatusCode, null);
    }

    /**
     * Get a registered runtime exception by it's HTTP status code.<br/>
     * If there's more than one exception with the specified status code, the one with the lowest custom status code
     * will be returned.<br/>
     * If there's no registered exception with the specified status code, a plain {@link HttpErrorException} will be
     * returned.
     *
     * @param httpStatusCode the HTTP status code of the runtime exception
     * @param customMessage  an optional message that overwrites the default one
     * @return a runtime exception
     */
    @SuppressWarnings("SameParameterValue")
    public static RuntimeException getExceptionByHttpStatusCode(final int httpStatusCode, final String customMessage) {

        final List<ExceptionDescription> list = HTTP_CODE_MAP.get(httpStatusCode);
        if (list == null || list.isEmpty()) {
            LOG.warn("Exception with HTTP status code [" + httpStatusCode
                    + "] was not found - returning plain http error exception.");
            HttpErrorException e;
            if (StringUtils.isBlank(customMessage)) {
                e = new HttpErrorException();
            } else {
                e = new HttpErrorException(customMessage);
            }
            e.setHttpStatusCode(httpStatusCode);
            return e;
        }
        final ExceptionDescription description = list.get(0);
        final String message = StringUtils.isNotBlank(customMessage) ? customMessage : description.getDefaultMessage();
        return (RuntimeException) findThrowable(description.getClazz(), message, null);
    }

    /**
     * Get a runtime exception from the specified {@link ThrowableMessageDto} or {@link ThrowableDto}.<br/>
     * The {@link ThrowableMessageDto} or {@link ThrowableDto} can be returned by a (rest) server.
     * Use this method to obtain the original runtime exception.
     *
     * @param throwableMessage the exception as DTO
     * @return the runtime exception
     */
    public static RuntimeException getExceptionByDto(final ThrowableMessageDto throwableMessage) {

        Throwable t = getThrowableByDto(throwableMessage);
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        LOG.warn(t.getClass().getName() + " is not a RuntimeException - wrap it");
        return new RuntimeException(t);
    }

    /**
     * Get a throwable from the specified {@link ThrowableMessageDto} or {@link ThrowableDto}.<br/>
     * The {@link ThrowableMessageDto} or {@link ThrowableDto} can be returned by a (rest) server.
     * Use this method to obtain the original throwable.
     *
     * @param throwableMessage the throwable as DTO
     * @return the runtime exception
     */
    public static Throwable getThrowableByDto(final ThrowableMessageDto throwableMessage) {

        Validate.notNull(throwableMessage, "Throwable message must not be null.");
        if (StringUtils.isBlank(throwableMessage.getClassName())) {
            throwableMessage.setClassName(HttpErrorException.class.getName());
        }

        LOG.info("Finding throwable with class [" + throwableMessage.getClassName() + "] defined by throwable DTO ...");

        Throwable cause;
        if (throwableMessage instanceof ThrowableDto) {
            ThrowableDto throwable = (ThrowableDto) throwableMessage;
            if (throwable.getCause() == null) {
                cause = null;
            } else {
                cause = getThrowableByDto(throwable.getCause());
            }
        } else {
            cause = null;
        }
        String message = throwableMessage.getMessage();
        Class<? extends Throwable> clazz = findThrowableClass(throwableMessage.getClassName());
        Throwable t = findThrowable(clazz, message, cause);
        LOG.info("Finding throwable with class [" + throwableMessage.getClassName()
                + "] defined by throwable DTO ... DONE (resulting throwable class is " + t.getClass().getName() + ")!");
        return t;
    }

    /**
     * Find an available throwable class.
     *
     * @param throwableClassName the class name defined in the DTO
     * @return the available throwable class
     */
    private static Class<? extends Throwable> findThrowableClass(final String throwableClassName) { // NOSONAR
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding class of throwable by name [" + throwableClassName + "] ..."); // NOSONAR
        }
        try {
            Class<?> cls = Class.forName(throwableClassName);
            if (Throwable.class.isAssignableFrom(cls)) {
                //noinspection unchecked
                return (Class<? extends Throwable>) cls;
            } else {
                LOG.warn("Finding class of throwable by name: '" + throwableClassName
                        + "' is not a throwable class - returning plain throwable class.");
                return Throwable.class;
            }
        } catch (ClassNotFoundException e) { // NOSONAR
            LOG.warn("Finding class of throwable by name: '" + throwableClassName
                    + "' was not found - returning plain throwable class.");
            return Throwable.class;
        } catch (RuntimeException e) {
            LOG.warn("Finding class of throwable by name failed - returning plain throwable class.", e);
            return Throwable.class;
        }
    }

    /**
     * Find an instance of the given throwable class.
     *
     * @param clazz   the throwable class
     * @param message an optional message
     * @param cause   an optional cause
     * @return the throwable instance
     */
    private static Throwable findThrowable(final Class<? extends Throwable> clazz, final String message,
                                           final Throwable cause) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding throwable of class [" + clazz + "] ...");
        }
        Class<? extends Throwable> cls = clazz;
        while (Modifier.isAbstract(cls.getModifiers())) {
            //noinspection unchecked
            cls = (Class<? extends Throwable>) cls.getSuperclass();
        }
        Throwable t = findThrowableByMessageAndCause(cls, message, cause);
        if (t == null) {
            t = findThrowableByMessage(cls, message, cause);
        }
        if (t == null) {
            t = findThrowableByCause(cls, message, cause);
        }
        if (t == null) {
            t = findThrowableByDefaultConstructor(cls, message, cause);
        }
        if (t == null) {
            Class<?> superCls = cls.getSuperclass();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finding throwable of class [" + cls + "] failed. Trying super class [" + superCls + "] ...");
            }
            if (Throwable.class.isAssignableFrom(superCls)) {
                //noinspection unchecked
                return findThrowable((Class<? extends Throwable>) superCls, message, cause);
            } else {
                InternalServerError ise = new InternalServerError();
                LOG.error("Finding throwable for class [" + clazz.getName() + "] failed.", ise); // NOSONAR
                throw ise;
            }
        }
        return t;
    }

    /*
     * Helper method.
     */
    private static Throwable findThrowableByMessageAndCause(final Class<? extends Throwable> clazz,
                                                            final String message, final Throwable cause) {
        try {
            Constructor<? extends Throwable> constructor = clazz.getDeclaredConstructor(String.class, Throwable.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance(message, cause);

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException // NOSONAR
                | InvocationTargetException e) { // NOSONAR
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finding throwable by message and cause failed. Class [" + clazz.getName() // NOSONAR
                        + "] has no suitable constructor: " + e.getMessage() // NOSONAR
                        + " (" + e.getClass().getName() + ")."); // NOSONAR
            }
            return null;
        }
    }

    /*
     * Helper method.
     */
    private static Throwable findThrowableByMessage(final Class<? extends Throwable> clazz, final String message,
                                                    final Throwable cause) {
        try {
            Constructor<? extends Throwable> constructor = clazz.getDeclaredConstructor(String.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            Throwable t = constructor.newInstance(message);
            if (cause != null) {
                t.initCause(cause);
            }
            return t;

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException // NOSONAR
                | InvocationTargetException e) { // NOSONAR
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finding throwable by message failed. Class [" + clazz.getName()
                        + "] has no suitable constructor: " + e.getMessage() + " (" + e.getClass().getName() + ").");
            }
            return null;
        }
    }

    /*
     * Helper method.
     */
    private static Throwable findThrowableByCause(final Class<? extends Throwable> clazz, final String message,
                                                  final Throwable cause) {
        try {
            Constructor<? extends Throwable> constructor = clazz.getDeclaredConstructor(Throwable.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            Throwable t = constructor.newInstance(cause);
            putMessage(t, message);
            return t;

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException // NOSONAR
                | InvocationTargetException e) { // NOSONAR
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finding throwable by cause failed. Class [" + clazz.getName()
                        + "] has no suitable constructor: " + e.getMessage() + " (" + e.getClass().getName() + ").");
            }
            return null;
        }
    }

    /*
     * Helper method.
     */
    private static Throwable findThrowableByDefaultConstructor(final Class<? extends Throwable> clazz,
                                                               final String message, final Throwable cause) {
        try {
            Constructor<? extends Throwable> constructor = clazz.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            Throwable t = constructor.newInstance();
            putMessage(t, message);
            if (cause != null) {
                t.initCause(cause); // NOSONAR
            }
            return t;

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException // NOSONAR
                | InvocationTargetException e) { // NOSONAR
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finding throwable by default constructor failed. Class [" + clazz.getName()
                        + "] has no such constructor: " + e.getMessage() + " (" + e.getClass().getName() + ").");
            }
            return null;
        }
    }

    /**
     * Sets a message on the specified throwable.
     *
     * @param throwable the throwable
     * @param message   the message
     */
    private static void putMessage(Throwable throwable, String message) {
        if (throwable == null || StringUtils.isBlank(message)) {
            return;
        }
        Validate.notNull(throwable, "Throwable must not be null.");
        try {
            Field messageField = findMessageField(throwable);
            if (messageField != null) {
                if (!messageField.isAccessible()) {
                    messageField.setAccessible(true);
                }
                messageField.set(throwable, message);
            } else {
                LOG.warn("Putting message [" + message + "] to throwable [" + throwable.getClass().getName() // NOSONAR
                        + "] failed: There's no field with name ''detailMessage");
            }

        } catch (RuntimeException re) {
            LOG.error("Putting message [" + message + "] to throwable [" + throwable.getClass().getName()
                    + "] failed.", re);
            throw re;
        } catch (IllegalAccessException iae) {
            LOG.error("Putting message [" + message + "] to throwable [" + throwable.getClass().getName()
                    + "] failed.", iae);
        }
    }

    /*
     * Helper method.
     */
    private static Field findMessageField(Throwable throwable) {
        Class<?> cls = throwable.getClass();
        boolean interrupted = false;
        Field messageField = null;
        while (!interrupted) {
            try {
                messageField = cls.getDeclaredField("detailMessage");
                interrupted = messageField != null;

            } catch (NoSuchFieldException e) { // NOSONAR
                cls = cls.getSuperclass();
                interrupted = !Throwable.class.isAssignableFrom(cls);
            }
        }
        return messageField;
    }

    /**
     * Description of a registered runtime exception.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExceptionDescription implements Comparable<ExceptionDescription> {

        private Class<? extends RuntimeException> clazz;

        private String defaultMessage;

        private Integer customStatusCode;

        private Integer httpStatusCode;

        @SuppressWarnings("NullableProblems")
        @Override
        public int compareTo(final ExceptionDescription o) {
            int i0 = getCustomStatusCode() != null ? getCustomStatusCode() : 0;
            int i1 = o != null && o.getCustomStatusCode() != null ? o.getCustomStatusCode() : 0;
            return i0 - i1;
        }
    }

}
