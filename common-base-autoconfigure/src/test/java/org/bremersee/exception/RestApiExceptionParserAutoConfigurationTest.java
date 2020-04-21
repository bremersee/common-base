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

package org.bremersee.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * The rest api exception parser auto configuration test.
 *
 * @author Christian Bremer
 */
class RestApiExceptionParserAutoConfigurationTest {

  /**
   * Rest api exception parser.
   */
  @Test
  void restApiExceptionParser() {
    RestApiExceptionParserAutoConfiguration configuration
        = new RestApiExceptionParserAutoConfiguration();
    configuration.init();
    assertNotNull(configuration.restApiExceptionParser(objectProvider(null)));

    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    assertNotNull(configuration.restApiExceptionParser(objectProvider(builder)));
  }

  private static <T> ObjectProvider<T> objectProvider(T provides) {
    //noinspection unchecked
    ObjectProvider<T> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(provides);
    return provider;
  }
}