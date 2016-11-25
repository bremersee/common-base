/*
 * Copyright 2015 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.bremersee.common.exception.StatusCodeAwareException;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass(name = { "org.glassfish.jersey.server.spring.SpringComponentProvider",
        "javax.servlet.ServletRegistration" })
@ConditionalOnBean(type = "org.glassfish.jersey.server.ResourceConfig")
@ConditionalOnWebApplication
@AutoConfigureBefore(org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration.class)
public class JerseyAutoConfiguration {
    
    @Autowired(required = false)
    @Qualifier("jaxbMarshaller")
    protected Jaxb2Marshaller jaxb2Marshaller;
    
    @Autowired(required = false)
    private List<JerseyComponentsProvider> componentProviders = new ArrayList<JerseyComponentsProvider>();

    @Bean
    public ResourceConfigCustomizer jaxbContextResourceConfig() {
        return new ResourceConfigCustomizer() {
            
            @Override
            public void customize(ResourceConfig config) {
                config.register(new JAXBContextProvider(jaxb2Marshaller));
            }
        };
    }

    @Bean
    public ResourceConfigCustomizer componentsProviderResourceConfig() {
        return new ResourceConfigCustomizer() {
            
            @Override
            public void customize(ResourceConfig config) {
                final Map<Class<? extends RuntimeException>, Integer> exceptionStatusCodeMap = new HashMap<>();
                final Set<Object> componentsSet = new LinkedHashSet<Object>();
                if (componentProviders != null) {
                    for (JerseyComponentsProvider provider : componentProviders) {
                        Object[] components = provider.getJerseyComponents();
                        if (components != null) {
                            for (Object component : components) {
                                if (component != null) {
                                    componentsSet.add(component);
                                }
                            }
                        }
                        Map<Class<? extends RuntimeException>, Integer> map = provider.getExceptionStatusCodeMap();
                        if (map != null) {
                            exceptionStatusCodeMap.putAll(map);
                        }
                    }
                    for (Object component : componentsSet) {
                        if (component != null) {
                            if (component instanceof String) {
                                try {
                                    config.register(Class.forName(component.toString()));
                                } catch (Exception e) {
                                    // log?
                                }
                            } else if (component instanceof Class<?>) {
                                config.register((Class<?>)component);
                            } else {
                                config.register(component);
                            }
                        }
                    }
                }
                
                config.register(new DefaultExceptionMapper(exceptionStatusCodeMap));
            }
        };
    }

    @Provider
    @Produces({ MediaType.APPLICATION_XML, "text/xml", MediaType.WILDCARD })
    @Consumes({ MediaType.APPLICATION_XML, "text/xml" })
    public static class JAXBContextProvider implements ContextResolver<JAXBContext> {
        
        protected final Jaxb2Marshaller jaxb2Marshaller;
        
        public JAXBContextProvider(Jaxb2Marshaller jaxb2Marshaller) {
            this.jaxb2Marshaller = jaxb2Marshaller;
        }

        @Override
        public JAXBContext getContext(Class<?> type) {
            if (jaxb2Marshaller != null && type != null && jaxb2Marshaller.supports(type)) {
                return jaxb2Marshaller.getJaxbContext();
            }
            return null;
        }
        
    }
    
    @Provider
    public static class DefaultExceptionMapper implements org.glassfish.jersey.spi.ExtendedExceptionMapper<RuntimeException> {
        
        protected final Logger log = LoggerFactory.getLogger(getClass());
        
        private final Map<Class<? extends RuntimeException>, Integer> exceptionStatusCodeMap;
        
        public DefaultExceptionMapper() {
            this(null);
        }
        
        public DefaultExceptionMapper(Map<Class<? extends RuntimeException>, Integer> exceptionStatusCodeMap) {
            this.exceptionStatusCodeMap = exceptionStatusCodeMap == null ? new HashMap<Class<? extends RuntimeException>, Integer>() : exceptionStatusCodeMap;
            initExceptionStatusCodeMap();
        }
        
        protected void initExceptionStatusCodeMap() {
            addRuntimeExceptionClassToMap(RuntimeException.class, Status.INTERNAL_SERVER_ERROR.getStatusCode());
            addRuntimeExceptionClassToMap(IllegalArgumentException.class, Status.BAD_REQUEST.getStatusCode());
            addRuntimeExceptionClassToMap("org.springframework.security.access.AccessDeniedException", Status.FORBIDDEN.getStatusCode());
            addRuntimeExceptionClassToMap("javax.persistence.EntityNotFoundException", Status.NOT_FOUND.getStatusCode());
        }
        
        protected void addRuntimeExceptionClassToMap(Class<? extends RuntimeException> cls, int statusCode) {
            if (cls != null) {
                if (exceptionStatusCodeMap.get(cls) == null) {
                    exceptionStatusCodeMap.put(cls, statusCode);
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        protected void addRuntimeExceptionClassToMap(String clsName, int statusCode) {
            if (!StringUtils.isEmpty(clsName)) {
                try {
                    Class<? extends RuntimeException> cls = (Class<? extends RuntimeException>) Class.forName(clsName);
                    addRuntimeExceptionClassToMap(cls, statusCode);
                } catch (Throwable t) {
                    log.info("RuntimeException [" + clsName + "] was not found. Status code mapping for this exception is not available.");
                }
            }
        }
        
        @Override
        public Response toResponse(RuntimeException exception) {
            if (exception != null) {
                Integer statusCode = exceptionStatusCodeMap.get(exception);
                if (statusCode == null && exception instanceof StatusCodeAwareException) {
                    statusCode = ((StatusCodeAwareException)exception).getStatusCode();
                }
                if (statusCode == null) {
                    statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();
                }
                final String msg;
                if (StringUtils.isEmpty(exception.getMessage())) {
                    Status status = Status.fromStatusCode(statusCode);
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
            if (exception != null) {
                if (exception instanceof StatusCodeAwareException) {
                    return true;
                }
                return exceptionStatusCodeMap.get(exception) != null;
            }
            return false;
        }
        
    }

}