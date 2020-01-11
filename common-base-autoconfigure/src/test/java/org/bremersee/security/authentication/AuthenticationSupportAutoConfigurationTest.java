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

package org.bremersee.security.authentication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestTemplate;

/**
 * The authentication support auto configuration test.
 *
 * @author Christian Bremer
 */
class AuthenticationSupportAutoConfigurationTest {

  private static AuthenticationSupportAutoConfiguration configuration;

  /**
   * Init.
   */
  @BeforeAll
  static void init() {
    AuthenticationProperties properties = new AuthenticationProperties();
    configuration = new AuthenticationSupportAutoConfiguration(properties);
    configuration.init();
  }

  /**
   * Json path jwt converter.
   */
  @Test
  void jsonPathJwtConverter() {
    assertNotNull(configuration.jsonPathJwtConverter());
  }

  /**
   * Rest template access token retriever.
   */
  @Test
  void restTemplateAccessTokenRetriever() {
    assertNotNull(configuration.restTemplateAccessTokenRetriever(restTemplateBuilder()));
  }

  /**
   * Password flow authentication manager.
   */
  @Test
  void passwordFlowAuthenticationManager() {
    assertNotNull(configuration.passwordFlowAuthenticationManager(
        jwtDecoder(),
        new JsonPathJwtConverter(),
        new RestTemplateAccessTokenRetriever(new RestTemplate())));
  }

  private static ObjectProvider<RestTemplateBuilder> restTemplateBuilder() {
    RestTemplateBuilder value = new RestTemplateBuilder();
    //noinspection unchecked
    ObjectProvider<RestTemplateBuilder> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }

  private static ObjectProvider<JwtDecoder> jwtDecoder() {
    JwtDecoder value = mock(JwtDecoder.class);
    //noinspection unchecked
    ObjectProvider<JwtDecoder> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }
}