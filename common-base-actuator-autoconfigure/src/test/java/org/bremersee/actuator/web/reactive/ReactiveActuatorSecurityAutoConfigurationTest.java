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

package org.bremersee.actuator.web.reactive;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.actuator.security.ActuatorSecurityProperties;
import org.bremersee.security.SecurityProperties;
import org.bremersee.security.authentication.JsonPathReactiveJwtConverter;
import org.bremersee.security.authentication.WebClientAccessTokenRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

/**
 * The reactive actuator security auto configuration test.
 *
 * @author Christian Bremer
 */
class ReactiveActuatorSecurityAutoConfigurationTest {

  /**
   * Init.
   */
  @Test
  void init() {
    SecurityProperties securityProperties = new SecurityProperties();
    ActuatorSecurityProperties actuatorSecurityProperties = new ActuatorSecurityProperties();
    ReactiveActuatorSecurityAutoConfiguration as = new ReactiveActuatorSecurityAutoConfiguration(
        securityProperties,
        actuatorSecurityProperties,
        mockJwtDecoderProvider(),
        mockJwtConverterProvider(),
        mockTokenRetrieverProvider());
    as.init();
  }

  private ObjectProvider<ReactiveJwtDecoder> mockJwtDecoderProvider() {
    ReactiveJwtDecoder value = mock(ReactiveJwtDecoder.class);
    //noinspection unchecked
    ObjectProvider<ReactiveJwtDecoder> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

  private ObjectProvider<JsonPathReactiveJwtConverter> mockJwtConverterProvider() {
    JsonPathReactiveJwtConverter value = mock(JsonPathReactiveJwtConverter.class);
    //noinspection unchecked
    ObjectProvider<JsonPathReactiveJwtConverter> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

  private ObjectProvider<WebClientAccessTokenRetriever> mockTokenRetrieverProvider() {
    WebClientAccessTokenRetriever value = mock(WebClientAccessTokenRetriever.class);
    //noinspection unchecked
    ObjectProvider<WebClientAccessTokenRetriever> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

}