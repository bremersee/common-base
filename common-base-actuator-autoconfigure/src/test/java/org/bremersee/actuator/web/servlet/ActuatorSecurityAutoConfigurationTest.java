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

package org.bremersee.actuator.web.servlet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.actuator.security.ActuatorSecurityProperties;
import org.bremersee.security.SecurityProperties;
import org.bremersee.security.authentication.JsonPathJwtConverter;
import org.bremersee.security.authentication.RestTemplateAccessTokenRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * The actuator security auto configuration test.
 *
 * @author Christian Bremer
 */
class ActuatorSecurityAutoConfigurationTest {

  /**
   * Init.
   */
  @Test
  void init() {
    SecurityProperties securityProperties = new SecurityProperties();
    ActuatorSecurityProperties actuatorSecurityProperties = new ActuatorSecurityProperties();
    ActuatorSecurityAutoConfiguration as = new ActuatorSecurityAutoConfiguration(
        securityProperties,
        actuatorSecurityProperties,
        mockJwtDecoderProvider(),
        mockJwtConverterProvider(),
        mockTokenRetrieverProvider());
    as.init();
  }

  private ObjectProvider<JwtDecoder> mockJwtDecoderProvider() {
    JwtDecoder value = mock(JwtDecoder.class);
    //noinspection unchecked
    ObjectProvider<JwtDecoder> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

  private ObjectProvider<JsonPathJwtConverter> mockJwtConverterProvider() {
    JsonPathJwtConverter value = mock(JsonPathJwtConverter.class);
    //noinspection unchecked
    ObjectProvider<JsonPathJwtConverter> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

  private ObjectProvider<RestTemplateAccessTokenRetriever> mockTokenRetrieverProvider() {
    RestTemplateAccessTokenRetriever value = mock(RestTemplateAccessTokenRetriever.class);
    //noinspection unchecked
    ObjectProvider<RestTemplateAccessTokenRetriever> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

}