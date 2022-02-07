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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperImpl;
import org.bremersee.exception.RestApiExceptionMapperProperties;
import org.bremersee.test.beans.MockObjectProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * The api exception resolver autoconfiguration test.
 *
 * @author Christian Bremer
 */
class ApiExceptionResolverAutoConfigurationTest {

  /**
   * Extend handler exception resolvers.
   */
  @Test
  void extendHandlerExceptionResolvers() {
    ApiExceptionResolverAutoConfiguration configuration = new ApiExceptionResolverAutoConfiguration(
        restApiExceptionMapper(),
        new MockObjectProvider<>(new Jackson2ObjectMapperBuilder()));
    configuration.init();
    List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();
    configuration.extendHandlerExceptionResolvers(exceptionResolvers);
  }

  private static ObjectProvider<RestApiExceptionMapper> restApiExceptionMapper() {
    RestApiExceptionMapper value = new RestApiExceptionMapperImpl(
        new RestApiExceptionMapperProperties(), "testapp");
    //noinspection unchecked
    ObjectProvider<RestApiExceptionMapper> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

}