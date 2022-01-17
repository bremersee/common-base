/*
 * Copyright 2019-2022 the original author or authors.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import org.bremersee.converter.BaseCommonConverters;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

/**
 * The base common converters autoconfiguration test.
 *
 * @author Christian Bremer
 */
class BaseCommonConvertersAutoConfigurationTest {

  /**
   * Add formatters.
   */
  @Test
  void addFormatters() {
    BaseCommonConvertersAutoConfiguration configuration
        = new BaseCommonConvertersAutoConfiguration();
    configuration.init();
    FormatterRegistry registry = Mockito.mock(FormatterRegistry.class);
    configuration.addFormatters(registry);
    verify(registry, atLeast(BaseCommonConverters.CONVERTERS.length))
        .addConverter(any(Converter.class));
  }
}