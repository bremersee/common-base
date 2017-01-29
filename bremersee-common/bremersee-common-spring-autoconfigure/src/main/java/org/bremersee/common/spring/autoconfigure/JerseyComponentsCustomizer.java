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

package org.bremersee.common.spring.autoconfigure;

import org.bremersee.common.exception.StatusCodeAwareException;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.util.StringUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.*;

/**
 * @author Christian Bremer
 */
public class JerseyComponentsCustomizer implements ResourceConfigCustomizer {

    final Logger log = LoggerFactory.getLogger(getClass());

    final List<JerseyComponentsProvider> componentProviders;

    JerseyComponentsCustomizer(List<JerseyComponentsProvider> componentProviders) {
        this.componentProviders = componentProviders;
    }

    @Override
    public void customize(ResourceConfig config) {

        final Set<Object> componentSet = createComponentSet();
        for (final Object component : componentSet) {
            if (component instanceof String) {
                try {
                    config.register(Class.forName(component.toString()));
                } catch (ClassNotFoundException e) {
                    log.error("Registering component [" + component + "] failed.", e);
                }
            } else if (component instanceof Class<?>) {
                config.register((Class<?>) component);
            } else {
                config.register(component);
            }
        }

        final Map<Class<? extends RuntimeException>, Integer> exceptionStatusCodeMap = createExceptionStatusCodeMap();
        config.register(new DefaultExceptionMapper(exceptionStatusCodeMap));
    }

    private Set<Object> createComponentSet() {
        final Set<Object> componentSet = new LinkedHashSet<>();
        for (JerseyComponentsProvider provider : componentProviders) {
            final Object[] components = provider.getJerseyComponents() == null ? new Object[0] : provider.getJerseyComponents();
            for (final Object component : components) {
                if (component != null) {
                    componentSet.add(component);
                }
            }
        }
        return componentSet;
    }

    private Map<Class<? extends RuntimeException>, Integer> createExceptionStatusCodeMap() {
        final Map<Class<? extends RuntimeException>, Integer> exceptionStatusCodeMap = new HashMap<>();
        for (JerseyComponentsProvider provider : componentProviders) {
            final Map<Class<? extends RuntimeException>, Integer> map = provider.getExceptionStatusCodeMap();
            if (map != null) {
                exceptionStatusCodeMap.putAll(map);
            }
        }
        return exceptionStatusCodeMap;
    }

    @Provider
    public static class DefaultExceptionMapper implements org.glassfish.jersey.spi.ExtendedExceptionMapper<RuntimeException> {

        protected final Logger log = LoggerFactory.getLogger(getClass());

        private final Map<Class<? extends RuntimeException>, Integer> exceptionStatusCodeMap;

        public DefaultExceptionMapper() {
            this(null);
        }

        public DefaultExceptionMapper(Map<Class<? extends RuntimeException>, Integer> exceptionStatusCodeMap) {
            this.exceptionStatusCodeMap = exceptionStatusCodeMap == null ? new HashMap<>() : exceptionStatusCodeMap;
            initExceptionStatusCodeMap();
        }

        protected void initExceptionStatusCodeMap() {
            addRuntimeExceptionClassToMap(RuntimeException.class, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            addRuntimeExceptionClassToMap(IllegalArgumentException.class, Response.Status.BAD_REQUEST.getStatusCode());
            addRuntimeExceptionClassToMap("org.springframework.security.access.AccessDeniedException", Response.Status.FORBIDDEN.getStatusCode());
            addRuntimeExceptionClassToMap("javax.persistence.EntityNotFoundException", Response.Status.NOT_FOUND.getStatusCode());
        }

        protected void addRuntimeExceptionClassToMap(Class<? extends RuntimeException> cls, int statusCode) {
            if (cls != null && exceptionStatusCodeMap.get(cls) == null) {
                exceptionStatusCodeMap.put(cls, statusCode);
            }
        }

        @SuppressWarnings("unchecked")
        protected void addRuntimeExceptionClassToMap(String clsName, int statusCode) {
            if (!StringUtils.isEmpty(clsName)) {
                try {
                    Class<? extends RuntimeException> cls = (Class<? extends RuntimeException>) Class.forName(clsName);
                    addRuntimeExceptionClassToMap(cls, statusCode);
                } catch (Exception e) {
                    log.info("RuntimeException [" + clsName + "] was not found. Status code mapping for this exception is not available.", e);
                }
            }
        }

        @Override
        public Response toResponse(RuntimeException exception) {
            if (exception != null) {
                Integer statusCode = exceptionStatusCodeMap.get(exception.getClass());
                if (statusCode == null && exception instanceof StatusCodeAwareException) {
                    statusCode = ((StatusCodeAwareException)exception).getHttpStatusCode();
                }
                if (statusCode == null) {
                    statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
                }
                final String msg;
                if (StringUtils.isEmpty(exception.getMessage())) {
                    Response.Status status = Response.Status.fromStatusCode(statusCode);
                    msg = status != null ? status.getReasonPhrase() : "No reason";
                } else {
                    msg = exception.getMessage();
                }
                return Response
                        .status(statusCode)
                        .entity(msg)
                        .type("text/plain").build();
            } else {
                return Response.serverError().build();
            }
        }

        @Override
        public boolean isMappable(RuntimeException exception) {
            return exception != null
                    && (exception instanceof StatusCodeAwareException
                    || exceptionStatusCodeMap.get(exception.getClass()) != null);
        }

    }

}
