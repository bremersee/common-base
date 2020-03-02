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

package org.bremersee.converter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

/**
 * The model mapper auto configuration.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass(ModelMapper.class)
@Configuration
@Slf4j
public class ModelMapperAutoConfiguration {

  /**
   * Creates the model mapper bean.
   *
   * @param adapters the configuration adapters
   * @return the model mapper
   */
  @Bean
  public ModelMapper modelMapper(ObjectProvider<List<ModelMapperConfigurerAdapter>> adapters) {
    final List<ModelMapperConfigurerAdapter> adapterList = adapters
        .getIfAvailable(Collections::emptyList);
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* adapters = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        adapterList.stream()
            .map(adapter -> ClassUtils.getUserClass(adapter.getClass()).getSimpleName())
            .collect(Collectors.toList()));
    final ModelMapper modelMapper = new ModelMapper();
    adapterList.forEach(adapter -> adapter.configure(modelMapper));
    return modelMapper;
  }

}
