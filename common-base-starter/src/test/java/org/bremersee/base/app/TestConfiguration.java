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

package org.bremersee.base.app;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.converter.ModelMapperConfigurerAdapter;
import org.bremersee.security.access.AclFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * The test configuration.
 *
 * @author Christian Bremer
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {TestConfiguration.class})
@Slf4j
public class TestConfiguration {

  @Bean
  public AclFactory<AclEntity> aclFactory() {
    log.info("Creating bean 'aclFactory' ...");
    return AclEntity::new;
  }

  @Bean
  public ModelMapperConfigurerAdapter customModelMapperConfig() {
    log.info("Creating custom model mapper config ...");
    return modelMapper -> {
      modelMapper
          .typeMap(Date.class, Instant.class)
          .setConverter(context -> Optional.ofNullable(context.getSource())
              .map(Date::toInstant)
              .orElse(null));
      modelMapper
          .typeMap(Instant.class, Date.class)
          .setConverter(context -> Optional.ofNullable(context.getSource())
              .map(Date::from)
              .orElse(null));
    };
  }

}
