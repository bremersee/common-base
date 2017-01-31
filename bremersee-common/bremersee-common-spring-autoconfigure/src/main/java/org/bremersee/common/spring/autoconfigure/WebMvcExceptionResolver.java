/*
 * Copyright 2017 the original author or authors.
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

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.apache.commons.lang3.Validate;
import org.bremersee.common.exception.StatusCodeAwareException;
import org.bremersee.common.exception.ThrowableToDtoMapper;
import org.bremersee.common.exception.ThrowableToThrowableDtoMapper;
import org.bremersee.common.model.ThrowableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MarshallingView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Christian Bremer
 */
public class WebMvcExceptionResolver implements HandlerExceptionResolver {

    private static final String DTO_KEY = "error";

    final Logger log = LoggerFactory.getLogger(getClass());

    Marshaller marshaller;

    ObjectMapper objectMapper;

    ThrowableToDtoMapper defaultExceptionMapper;

    private final Map<String, ThrowableToDtoMapper> exceptionMapperHandlerMap = new HashMap<>();

    public WebMvcExceptionResolver() {
        defaultExceptionMapper = new ThrowableToThrowableDtoMapper();
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setContextPath(ThrowableDto.class.getPackage().getName());
        marshaller = m;
        objectMapper = new ObjectMapper();
        // see http://wiki.fasterxml.com/JacksonJAXBAnnotations
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        AnnotationIntrospectorPair pair = new AnnotationIntrospectorPair(primary, secondary);
        objectMapper.setAnnotationIntrospector(pair);
    }

    public void setMarshaller(Marshaller marshaller) {
        if (marshaller != null) {
            this.marshaller = marshaller;
        }
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        if (objectMapper != null) {
            this.objectMapper = objectMapper;
        }
    }

    public Map<String, ThrowableToDtoMapper> getExceptionMapperHandlerMap() {
        return exceptionMapperHandlerMap;
    }

    public void setDefaultExceptionMapper(ThrowableToDtoMapper defaultExceptionMapper) {
        this.defaultExceptionMapper = defaultExceptionMapper;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {

        if (ex == null || !isRestController(handler)) {
            return null;
        }

        final Object dto = getExceptionMapper(handler).toDto(ex);
        if (dto == null) {
            return null;
        }

        ModelAndView mav;

        String acceptStr = request.getHeader("Accept") == null ? "" : request.getHeader("Accept").toLowerCase();
        if (log.isDebugEnabled()) {
            log.debug("Handler [" + handler + "] got accept header [" + acceptStr + "] from client.");
        }
        if (acceptStr.contains("application/xml") || acceptStr.contains("text/xml")) {

            MarshallingView mv = new MarshallingView(marshaller);
            mv.setModelKey(DTO_KEY);
            mav = new ModelAndView(mv, DTO_KEY, dto);
            response.setContentType(MediaType.APPLICATION_XML_VALUE);

        } else {

            final MappingJackson2JsonView mjv = new MappingJackson2JsonView(objectMapper);
            mav = new ModelAndView(mjv);
            final Map<String, Object> map = toJsonMap(dto);
            mav.addAllObjects(map);
            if (acceptStr.contains("text/plain")) {
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            } else {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            }
        }

        Integer statusCode = determineStatusCode(ex);
        applyStatusCodeIfPossible(request, response, statusCode);

        return mav;
    }

    void applyStatusCodeIfPossible(HttpServletRequest request, HttpServletResponse response, int statusCode) {
        if (!WebUtils.isIncludeRequest(request)) {
            if (log.isDebugEnabled()) {
                log.debug("Applying HTTP status code " + statusCode);
            }
            response.setStatus(statusCode);
            request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, statusCode);
        }
    }

    Class<?> getHandlerClass(Object handler) {
        Validate.notNull(handler, "Handler must not be null.");
        if (handler instanceof HandlerMethod) {
            return ((HandlerMethod) handler).getBean().getClass();
        } else {
            return handler.getClass();
        }
    }

    boolean isRestController(Object handler) {
        if (handler == null) {
            return false;
        }
        final Class<?> cls = getHandlerClass(handler);
        final boolean result = AnnotationUtils.findAnnotation(cls, RestController.class) != null;
        if (log.isDebugEnabled()) {
            log.debug("Is handler [" + handler + "] a rest controller? " + result);
        }
        return result;
    }

    ThrowableToDtoMapper getExceptionMapper(Object handler) {
        ThrowableToDtoMapper mapper = findExceptionMapper(handler);
        if (mapper == null) {
            mapper = defaultExceptionMapper;
        }
        return mapper;
    }

    private ThrowableToDtoMapper findExceptionMapper(Object handler) {

        ThrowableToDtoMapper mapper;

        mapper = exceptionMapperHandlerMap.get(getHandlerClass(handler).getName());
        if (mapper != null) {
            return mapper;
        }

        mapper = exceptionMapperHandlerMap.get(getHandlerClass(handler).getSimpleName());
        if (mapper != null) {
            return mapper;
        }

        final String className = getHandlerClass(handler).getName().toLowerCase();
        for (Map.Entry<String, ThrowableToDtoMapper> entry : exceptionMapperHandlerMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                String entryName = entry.getKey().toLowerCase();
                if (className.contains(entryName)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    int determineStatusCode(Exception ex) {

        if (ex == null) {

            return HttpStatus.OK.value();

        } else if (ex instanceof StatusCodeAwareException) {

            return ((StatusCodeAwareException) ex).getHttpStatusCode();

        } else if (ex instanceof IllegalArgumentException) {

            return HttpStatus.BAD_REQUEST.value();

        } else if (instanceOf(ex.getClass(), "org.springframework.security.access.AccessDeniedException")) {

            return HttpStatus.FORBIDDEN.value();

        } else if (instanceOf(ex.getClass(), "javax.persistence.EntityNotFoundException")) {

            return HttpStatus.NOT_FOUND.value();

        } else {

            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }

    private boolean instanceOf(Class<?> clazz, String otherClassName) {
        return clazz != null && (clazz.getName().equals(otherClassName) // NOSONAR
                || instanceOf(clazz.getSuperclass(), otherClassName));
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> toJsonMap(Object dto) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(dto);
            return (Map<String, Object>) objectMapper.readValue(jsonBytes, Map.class);

        } catch (IOException e) {
            log.error("Creating a JSON map from DTO failed. Returning a new map with DTO and key '" + DTO_KEY + "'.", e);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(DTO_KEY, dto);
            return map;
        }
    }

}
