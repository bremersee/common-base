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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bremersee.common.converter.ObjectMapperUtils;
import org.bremersee.common.exception.ThrowableToDtoMapper;
import org.bremersee.common.exception.ThrowableToThrowableDtoMapper;
import org.bremersee.common.exception.ThrowableToThrowableMessageDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bremer
 */
@Configuration
@ConditionalOnClass(name = {"org.springframework.http.converter.xml.MarshallingHttpMessageConverter"})
@EnableConfigurationProperties(WebMvcProperties.class)
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private WebMvcProperties properties = new WebMvcProperties();

    private Jaxb2Marshaller jaxbMarshaller;

    private ObjectMapper objectMapper;

    private final WebMvcExceptionResolver exceptionResolver = new WebMvcExceptionResolver();

    private final ThrowableToDtoMapper full = new ThrowableToThrowableDtoMapper();

    private final ThrowableToDtoMapper light = new ThrowableToThrowableMessageDtoMapper();

    @Autowired(required = false)
    public void setProperties(WebMvcProperties properties) {
        this.properties = properties;
    }

    @Autowired(required = false)
    @Qualifier("jaxbMarshaller")
    public void setJaxbMarshaller(Jaxb2Marshaller jaxbMarshaller) {
        this.jaxbMarshaller = jaxbMarshaller;
    }

    @Autowired(required = false)
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        // @formatter:off
        StringBuilder msg = new StringBuilder();
        if (jaxbMarshaller == null) {
            jaxbMarshaller = JaxbAutoConfiguration.createJaxbMarshaller();
            //         "**********************************************************************\n"
            msg.append("* WARNING: A bean with name 'jaxbMarshaller' was not found!          *\n");
            msg.append("*          Using default 'Jaxb2Marshaller'.                          *\n");
        }
        if (objectMapper == null) {
            objectMapper = ObjectMapperUtils.createDefaultObjectMapper();
            //         "**********************************************************************\n"
            msg.append("* WARNING: A bean of type JSON 'ObjectMapper' was not found!         *\n");
            msg.append("*          Using default JSON ObjectMapper.                         *\n");
        }
        log.info("\n"// NOSONAR
                + "**********************************************************************\n"
                + "*  Common WebMVC Configuration                                       *\n"
                + "**********************************************************************\n"
                + msg.toString()
                + "* - properties = " + properties + "\n"
                + "**********************************************************************");
        // @formatter:on
        exceptionResolver.setMarshaller(jaxbMarshaller);
        exceptionResolver.setObjectMapper(objectMapper);
        exceptionResolver.setDefaultExceptionMapper(getThrowableToDtoMapper(properties.getErrorMessageType()));
        for (Map.Entry<String, WebMvcProperties.ErrorMessageDetails> entry :
                properties.getErrorMessageDetailsHandlerMap().entrySet()) {

            exceptionResolver.getExceptionMapperHandlerMap().put(
                    entry.getKey(), getThrowableToDtoMapper(entry.getValue()));
        }
    }

    private ThrowableToDtoMapper getThrowableToDtoMapper(WebMvcProperties.ErrorMessageDetails type) {
        if (type == WebMvcProperties.ErrorMessageDetails.LIGHTWEIGHT) {
            return light;
        } else {
            return full;
        }
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(marshallingHttpMessageConverter());
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        synchronized (exceptionResolver) {
            if (!exceptionResolvers.contains(exceptionResolver)) {
                exceptionResolvers.add(0, exceptionResolver);
            }
        }
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        synchronized (exceptionResolver) {
            if (!exceptionResolvers.contains(exceptionResolver)) {
                exceptionResolvers.add(0, exceptionResolver);
            }
        }
    }

    @Bean
    @Primary
    public MarshallingHttpMessageConverter marshallingHttpMessageConverter() {
        return new MarshallingHttpMessageConverter(jaxbMarshaller, jaxbMarshaller);
    }

}
