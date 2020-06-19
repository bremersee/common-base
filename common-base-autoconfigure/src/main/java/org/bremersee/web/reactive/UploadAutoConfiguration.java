/*
 * Copyright 2020 the original author or authors.
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
 * The upload auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass(UploadedItemBuilder.class)
@EnableConfigurationProperties(UploadProperties.class)
@Configuration
@Slf4j
public class UploadAutoConfiguration {

  private final UploadProperties properties;

  /**
   * Instantiates a new upload auto configuration.
   *
   * @param properties the properties
   */
  public UploadAutoConfiguration(UploadProperties properties) {
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
   * Creates reactive upload item builder.
   *
   * @return the reactive upload item builder
   */
  @ConditionalOnMissingBean
  @Bean
  public UploadedItemBuilder uploadedItemBuilder() {
    log.info("Creating {} ...", UploadedItemBuilder.class.getSimpleName());
    return new UploadedItemBuilderImpl(properties.getTmpDir());
  }

}
