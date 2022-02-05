/*
 * Copyright 2022 the original author or authors.
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

package org.bremersee.web.servlet;

import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.http.converter.Jaxb2HttpMessageConverter;
import org.bremersee.http.converter.JsonStringHttpMessageConverter;
import org.bremersee.xml.JaxbContextBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.web.servlet.server.Encoding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.ClassUtils;

/**
 * The http message converter configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@Configuration
@Slf4j
public class HttpMessageConverterAutoConfiguration {

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName());
  }

  /**
   * Creates string http message converter bean.
   *
   * @param environment the environment
   * @return the string http message converter
   */
  @Bean
  public StringHttpMessageConverter stringHttpMessageConverter(Environment environment) {
    log.info("Creating bean {}", JsonStringHttpMessageConverter.class.getSimpleName());
    Charset charset = Binder
        .get(environment)
        .bindOrCreate("server.servlet.encoding", Encoding.class)
        .getCharset();
    StringHttpMessageConverter converter = new JsonStringHttpMessageConverter(charset);
    converter.setWriteAcceptCharset(false);
    return converter;
  }

  /**
   * Creates jaxb http message converter bean.
   *
   * @param jaxbContextBuilder the jaxb context builder
   * @return the jaxb http message converter
   */
  @ConditionalOnMissingBean(Jaxb2HttpMessageConverter.class)
  @ConditionalOnBean(JaxbContextBuilder.class)
  @Bean
  public Jaxb2HttpMessageConverter jaxb2HttpMessageConverter(
      JaxbContextBuilder jaxbContextBuilder) {
    log.info("Creating bean {}", Jaxb2HttpMessageConverter.class.getSimpleName());
    return new Jaxb2HttpMessageConverter(jaxbContextBuilder);
  }

}
