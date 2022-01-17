/*
 * Copyright 2020-2022 the original author or authors.
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
import org.bremersee.web.UploadProperties;
import org.bremersee.web.reactive.multipart.MultipartFileBuilder;
import org.bremersee.web.reactive.multipart.MultipartFileBuilderImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The multipart file builder autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass(MultipartFileBuilder.class)
@EnableConfigurationProperties(UploadProperties.class)
@Configuration
@Slf4j
public class MultipartFileBuilderAutoConfiguration {

  private final UploadProperties properties;

  /**
   * Instantiates a new multipart file builder autoconfiguration.
   *
   * @param properties the properties
   */
  public MultipartFileBuilderAutoConfiguration(UploadProperties properties) {
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

  /**
   * Multipart file builder multipart file builder.
   *
   * @return the multipart file builder
   */
  @ConditionalOnMissingBean
  @Bean
  public MultipartFileBuilder multipartFileBuilder() {
    return new MultipartFileBuilderImpl(properties.getTmpDir());
  }

}
