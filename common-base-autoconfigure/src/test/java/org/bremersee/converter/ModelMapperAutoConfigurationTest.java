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

package org.bremersee.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The model mapper autoconfiguration test.
 *
 * @author Christian Bremer
 */
class ModelMapperAutoConfigurationTest {

  /**
   * Tests creation of model mapper.
   */
  @Test
  void modelMapper() {
    ModelMapperAutoConfiguration configuration = new ModelMapperAutoConfiguration();
    ModelMapperConfigurerAdapter adapter = mock(ModelMapperConfigurerAdapter.class);
    assertNotNull(configuration.modelMapper(objectProvider(Collections.singletonList(adapter))));
    verify(adapter).configure(any(ModelMapper.class));
  }

  private static <T> ObjectProvider<T> objectProvider(T provides) {
    //noinspection unchecked
    ObjectProvider<T> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(provides);
    when(provider.getIfAvailable(any())).thenReturn(provides);
    return provider;
  }
}