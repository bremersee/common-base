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

package org.bremersee.web.reactive;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperImpl;
import org.bremersee.exception.RestApiExceptionMapperProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;

/**
 * The type Api exception handler auto configuration test.
 *
 * @author Christian Bremer
 */
class ApiExceptionHandlerAutoConfigurationTest {

  /**
   * Api exception handler.
   */
  @Test
  void apiExceptionHandler() {
    ApiExceptionHandlerAutoConfiguration configuration = new ApiExceptionHandlerAutoConfiguration();
    configuration.init();
    assertNotNull(configuration.apiExceptionHandler(
        errorAttributes(),
        resourceProperties(),
        applicationContext(),
        codecConfigurer(),
        restApiExceptionMapper()));
  }

  private static ObjectProvider<ErrorAttributes> errorAttributes() {
    ErrorAttributes value = mock(ErrorAttributes.class);
    //noinspection unchecked
    ObjectProvider<ErrorAttributes> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

  private static ObjectProvider<ResourceProperties> resourceProperties() {
    ResourceProperties value = mock(ResourceProperties.class);
    //noinspection unchecked
    ObjectProvider<ResourceProperties> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

  private static ObjectProvider<ServerCodecConfigurer> codecConfigurer() {
    DefaultServerCodecConfigurer value = new DefaultServerCodecConfigurer();
    //noinspection unchecked
    ObjectProvider<ServerCodecConfigurer> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

  private static ObjectProvider<RestApiExceptionMapper> restApiExceptionMapper() {
    RestApiExceptionMapper value = new RestApiExceptionMapperImpl(
        new RestApiExceptionMapperProperties(), "testapp");
    //noinspection unchecked
    ObjectProvider<RestApiExceptionMapper> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

  private static ApplicationContext applicationContext() {
    ApplicationContext value = mock(ApplicationContext.class);
    when(value.getClassLoader())
        .thenReturn(ApplicationContext.class.getClassLoader());
    return value;
  }

}