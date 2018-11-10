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

package org.bremersee.common.web.client;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import lombok.Getter;
import org.bremersee.common.exception.ExceptionRegistry;
import org.bremersee.common.exception.HttpErrorException;
import org.bremersee.common.model.ThrowableMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.ResponseErrorHandler;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * <p>
 * A response error handler that tries to parse the response to a {@link ThrowableMessageDto}
 * or a {@link org.bremersee.common.model.ThrowableDto} and throws an appropriate exception.
 * If the response don't contains such a DTO, an exception appropriate to the status code will be thrown.
 * </p>
 *
 * @author Christian Bremer
 */
public class ResponseErrorHandlerImpl implements ResponseErrorHandler {

    /**
     * The logger.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The XML unmarshaller.
     */
    @Getter
    private Unmarshaller unmarshaller;

    /**
     * The JSON object mapper.
     */
    @Getter
    private ObjectMapper objectMapper;

    /**
     * Default constructor.
     */
    public ResponseErrorHandlerImpl() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setContextPath(ThrowableMessageDto.class.getPackage().getName());
        unmarshaller = m;
        objectMapper = new ObjectMapper();
        // see http://wiki.fasterxml.com/JacksonJAXBAnnotations
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        AnnotationIntrospectorPair pair = new AnnotationIntrospectorPair(primary, secondary);
        objectMapper.setAnnotationIntrospector(pair);
    }

    /**
     * Set the XML unmarshaller.
     *
     * @param unmarshaller the XML unmarshaller
     */
    public void setUnmarshaller(Unmarshaller unmarshaller) {
        if (unmarshaller != null) {
            this.unmarshaller = unmarshaller;
        }
    }

    /**
     * Set the JSON object mapper.
     *
     * @param objectMapper the JSON object mapper
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        if (objectMapper != null) {
            this.objectMapper = objectMapper;
        }
    }

    @Override
    public boolean hasError(final ClientHttpResponse response) throws IOException {
        boolean hasError;
        try {
            HttpStatus statusCode = response.getStatusCode();
            hasError = statusCode.series() == HttpStatus.Series.CLIENT_ERROR ||
                    statusCode.series() == HttpStatus.Series.SERVER_ERROR;

        } catch (Throwable t) { // NOSONAR
            hasError = response.getRawStatusCode() >= 400;
        }
        if (log.isTraceEnabled()) {
            log.trace("Response is error? " + hasError);
        }
        return hasError;
    }

    @Override
    public void handleError(final ClientHttpResponse response) throws IOException {

        final Throwable t = findThrowable(response);

        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            HttpErrorException e = new HttpErrorException(t.getMessage(), t.getCause());
            e.setHttpStatusCode(response.getRawStatusCode());
            throw e;
        }
    }

    /**
     * Find a throwable instance of the HTTP response.
     *
     * @param response the HTTP response
     * @return the throwable
     * @throws IOException if the response cannot be parsed
     */
    protected Throwable findThrowable(final ClientHttpResponse response) throws IOException {
        final MediaType contentType = response.getHeaders().getContentType();
        try {
            if (contentType.includes(MediaType.APPLICATION_XML)) {
                return findThrowableFromXml(response);
            } else {
                return findThrowableFromJson(response);
            }

        } catch (RuntimeException re) { // NOSONAR

            return findThrowableByStatusCode(response);
        }
    }

    /**
     * Find a throwable instance by parsing the response to a {@link ThrowableMessageDto}.
     *
     * @param response the HTTP response as XML
     * @return the throwable
     * @throws IOException if the response cannot be parsed
     */
    protected Throwable findThrowableFromXml(final ClientHttpResponse response) throws IOException {
        final ThrowableMessageDto dto = (ThrowableMessageDto) unmarshaller.unmarshal(new StreamSource(response.getBody()));
        if (log.isDebugEnabled()) {
            log.debug("Got throwable from response xml body: " + dto);
        }
        return ExceptionRegistry.getExceptionByDto(dto);
    }

    /**
     * Find a throwable instance by parsing the response to a {@link ThrowableMessageDto}.
     *
     * @param response the HTTP response as JSON
     * @return the throwable
     * @throws IOException if the response cannot be parsed
     */
    protected Throwable findThrowableFromJson(final ClientHttpResponse response) throws IOException {
        final ThrowableMessageDto dto = objectMapper.readValue(response.getBody(), ThrowableMessageDto.class);
        if (log.isDebugEnabled()) {
            log.debug("Got throwable from response json body: " + dto);
        }
        return ExceptionRegistry.getExceptionByDto(dto);
    }

    /**
     * Find a throwable instance by calling the {@link ExceptionRegistry}.
     *
     * @param response the HTTP response as JSON
     * @return the throwable
     * @throws IOException if the response cannot be parsed
     */
    protected RuntimeException findThrowableByStatusCode(final ClientHttpResponse response) throws IOException {
        return ExceptionRegistry.getExceptionByHttpStatusCode(response.getRawStatusCode(), response.getStatusText());
    }

}
