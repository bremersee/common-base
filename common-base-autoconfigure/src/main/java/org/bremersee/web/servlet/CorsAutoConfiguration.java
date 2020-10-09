/*
 * Copyright 2019-2020 the original author or authors.
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

import lombok.extern.slf4j.Slf4j;
import org.bremersee.web.CorsProperties;
import org.bremersee.web.CorsProperties.CorsConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The cors auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(CorsProperties.class)
@Configuration
@Slf4j
public class CorsAutoConfiguration implements WebMvcConfigurer {

  private final CorsProperties properties;

  /**
   * Instantiates a new cors auto configuration.
   *
   * @param properties the cors properties
   */
  public CorsAutoConfiguration(CorsProperties properties) {
    this.properties = properties;
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* properties = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(), properties);
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public void addCorsMappings(@NonNull CorsRegistry corsRegistry) {
    if (!properties.isEnable()) {
      return;
    }
    for (CorsConfiguration config : properties.getConfigs()) {
      corsRegistry.addMapping(config.getPathPattern())
          .allowedOrigins(config.getAllowedOrigins().toArray(new String[0]))
          .allowedMethods(config.getAllowedMethods().toArray(new String[0]))
          .allowedHeaders(config.getAllowedHeaders().toArray(new String[0]))
          .exposedHeaders(config.getExposedHeaders().toArray(new String[0]))
          .maxAge(config.getMaxAge())
          .allowCredentials(config.isAllowCredentials());
    }
  }

}
