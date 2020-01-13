/*
 * Copyright 2019 the original author or authors.
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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The api exception resolver auto configuration.
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({
    ApiExceptionResolver.class
})
@ConditionalOnBean({
    RestApiExceptionMapper.class,
    Jackson2ObjectMapperBuilder.class
})
@AutoConfigureAfter({
    RestApiExceptionMapperAutoConfiguration.class
})
@Configuration
@Slf4j
public class ApiExceptionResolverAutoConfiguration implements WebMvcConfigurer {

  private final ApiExceptionResolver apiExceptionResolver;

  /**
   * Instantiates a new api exception resolver auto configuration.
   *
   * @param apiExceptionMapper the api exception mapper
   * @param objectMapperBuilder the object mapper builder
   */
  public ApiExceptionResolverAutoConfiguration(
      ObjectProvider<RestApiExceptionMapper> apiExceptionMapper,
      ObjectProvider<Jackson2ObjectMapperBuilder> objectMapperBuilder) {

    Assert.notNull(
        apiExceptionMapper.getIfAvailable(),
        "Api exception resolver must be present.");
    Assert.notNull(
        objectMapperBuilder.getIfAvailable(),
        "Object mapper builder must be present.");
    apiExceptionResolver = new ApiExceptionResolver(
        apiExceptionMapper.getIfAvailable(),
        objectMapperBuilder.getIfAvailable());
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        getClass().getSimpleName());
  }

  @Override
  public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    log.info("Adding exception resolver [{}] to registry.",
        apiExceptionResolver.getClass().getSimpleName());
    exceptionResolvers.add(0, apiExceptionResolver);
  }

}
