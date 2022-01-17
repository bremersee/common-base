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

package org.bremersee.exception;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ClassUtils;

/**
 * The rest api exception parser autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass({Jackson2ObjectMapperBuilder.class})
@Configuration
@Slf4j
public class RestApiExceptionParserAutoConfiguration {

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
   * Rest api exception parser rest api exception parser.
   *
   * @param objectMapperBuilderProvider the object mapper builder provider
   * @return the rest api exception parser
   */
  @Bean
  public RestApiExceptionParser restApiExceptionParser(
      ObjectProvider<Jackson2ObjectMapperBuilder> objectMapperBuilderProvider) {
    Jackson2ObjectMapperBuilder objectMapperBuilder = objectMapperBuilderProvider.getIfAvailable();
    return Optional.ofNullable(objectMapperBuilder)
        .map(RestApiExceptionParserImpl::new)
        .orElseGet(RestApiExceptionParserImpl::new);
  }

}
