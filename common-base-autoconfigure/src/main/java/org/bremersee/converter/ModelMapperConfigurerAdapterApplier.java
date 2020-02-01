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

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The model mapper configurer adapter applier.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass(ModelMapper.class)
@AutoConfigureAfter(ModelMapperAutoConfiguration.class)
@Configuration
@Slf4j
public class ModelMapperConfigurerAdapterApplier {

  private ModelMapper modelMapper;

  private List<ModelMapperConfigurerAdapter> adapters;

  /**
   * Instantiates a new model mapper configurer adapter applier.
   *
   * @param modelMapper the model mapper
   * @param adapters the adapters
   */
  public ModelMapperConfigurerAdapterApplier(
      ObjectProvider<ModelMapper> modelMapper,
      ObjectProvider<List<ModelMapperConfigurerAdapter>> adapters) {
    this.modelMapper = modelMapper.getIfAvailable();
    this.adapters = adapters.getIfAvailable();
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
            + "* modelMapper = {}\n"
            + "* adapters = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        modelMapper,
        adapters == null ? null : adapters.stream()
            .map(adapter -> ClassUtils.getUserClass(adapter.getClass()).getSimpleName())
            .collect(Collectors.toList()));

    if (modelMapper != null && adapters != null) {
      adapters.forEach(adapter -> adapter.configure(modelMapper));
    }
  }

}
