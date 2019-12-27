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

package org.bremersee.converter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

/**
 * The base common converters test.
 *
 * @author Christian Bremer
 */
public class BaseCommonConvertersTest {

  /**
   * Register all.
   */
  @Test
  public void registerAll() {
    FormatterRegistry registry = mock(FormatterRegistry.class);
    BaseCommonConverters.registerAll(registry);
    verify(registry, times(BaseCommonConverters.CONVERTERS.length))
        .addConverter(any(Converter.class));
  }
}