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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@ConditionalOnClass(name = {"org.springframework.http.converter.xml.MarshallingHttpMessageConverter"})
@ConditionalOnWebApplication
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Jaxb2Marshaller jaxbMarshaller;

    @Autowired(required = false)
    @Qualifier("jaxbMarshaller")
    public void setJaxbMarshaller(Jaxb2Marshaller jaxbMarshaller) {
        this.jaxbMarshaller = jaxbMarshaller;
    }

    @PostConstruct
    public void init() {
        // @formatter:off
        String msg;
        if (jaxbMarshaller == null) {
            jaxbMarshaller = JaxbAutoConfiguration.createJaxbMarshaller();
            //    "**********************************************************************\n"
            msg = "*   WARNING: A bean with name 'jaxbMarshaller' was not found!        *\n"
                + "*            Using default 'Jaxb2Marshaller'.                        *\n";
        } else {
            msg = "";
        }
        log.info("\n"
                + "**********************************************************************\n"
                + "*  Common WebMVC Configuration                                       *\n"
                + "**********************************************************************\n"
                + "* - Provides common 'MarshallingHttpMessageConverter'.               *\n"
                + msg
                + "**********************************************************************");
        // @formatter:on
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(marshallingHttpMessageConverter());
    }
    
    @Bean
    @Primary
    public MarshallingHttpMessageConverter marshallingHttpMessageConverter() {
        return new MarshallingHttpMessageConverter(jaxbMarshaller, jaxbMarshaller);
    }

}
