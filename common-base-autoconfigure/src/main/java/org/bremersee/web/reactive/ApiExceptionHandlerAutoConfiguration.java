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

package org.bremersee.web.reactive;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.util.Assert;

/**
 * The api exception handler auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnBean({
    ErrorAttributes.class,
    ResourceProperties.class,
    ServerCodecConfigurer.class,
    RestApiExceptionMapper.class
})
@AutoConfigureAfter({
    RestApiExceptionMapperAutoConfiguration.class
})
@Configuration
@Slf4j
public class ApiExceptionHandlerAutoConfiguration {

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

  /**
   * Builds api exception handler bean.
   *
   * @param errorAttributes the error attributes
   * @param resourceProperties the resource properties
   * @param applicationContext the application context
   * @param serverCodecConfigurer the server codec configurer
   * @param restApiExceptionMapper the rest api exception mapper
   * @return the api exception handler bean
   */
  @Bean
  @Order(-2)
  public ApiExceptionHandler apiExceptionHandler(
      ObjectProvider<ErrorAttributes> errorAttributes,
      ObjectProvider<ResourceProperties> resourceProperties,
      ApplicationContext applicationContext,
      ObjectProvider<ServerCodecConfigurer> serverCodecConfigurer,
      ObjectProvider<RestApiExceptionMapper> restApiExceptionMapper) {

    Assert.notNull(
        errorAttributes.getIfAvailable(),
        "Error attributes must be present.");
    Assert.notNull(
        resourceProperties.getIfAvailable(),
        "Resource properties must be present.");
    Assert.notNull(
        serverCodecConfigurer.getIfAvailable(),
        "Server codec configurer must be present.");
    Assert.notNull(
        restApiExceptionMapper.getIfAvailable(),
        "Rest api exception mapper must be present.");
    log.info("Creating api exception handler [{}].",
        restApiExceptionMapper.getIfAvailable().getClass().getSimpleName());

    return new ApiExceptionHandler(
        errorAttributes.getIfAvailable(),
        resourceProperties.getIfAvailable(),
        applicationContext,
        serverCodecConfigurer.getIfAvailable(),
        restApiExceptionMapper.getIfAvailable());
  }

}
