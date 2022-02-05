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

package org.bremersee.xml;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The jaxb context builder autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass(JaxbContextBuilder.class)
@Configuration
@Slf4j
public class JaxbContextBuilderAutoConfiguration {

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
   * Creates jaxb context builder bean.
   *
   * @param configurers the configurers
   * @return the jaxb context builder
   */
  @ConditionalOnMissingBean(JaxbContextBuilder.class)
  @Bean
  public JaxbContextBuilder jaxbContextBuilder(List<JaxbContextBuilderConfigurer> configurers) {
    log.info("Creating bean {} with configurers {}",
        JaxbContextBuilder.class.getSimpleName(), configurers);
    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder.newInstance()
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));
    Optional.ofNullable(configurers)
        .ifPresent(configurerList -> configurerList
            .forEach(configurer -> configurer.configure(jaxbContextBuilder)));
    return jaxbContextBuilder;
  }

}
