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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.exception.AccessTokenRetrieverAuthenticationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * The password flow access token retriever test.
 *
 * @author Christian Bremer
 */
class RestTemplateAccessTokenRetrieverTest {

  /**
   * Retrieve access token.
   */
  @Test
  void retrieveAccessToken() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    //noinspection unchecked
    when(restTemplate.exchange(
        anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
        .thenReturn(ResponseEntity.ok("{\"access_token\":\"junit_access_token_value\"}"));
    RestTemplateAccessTokenRetriever tokenRetriever = new RestTemplateAccessTokenRetriever(
        restTemplate);

    PasswordFlowProperties properties = PasswordFlowProperties.builder()
        .tokenEndpoint("http://localhost/token")
        .clientId("123")
        .clientSecret("456")
        .username("789")
        .password("012")
        .build();
    String token = tokenRetriever.retrieveAccessToken(properties);
    assertNotNull(token);
    assertEquals("junit_access_token_value", token);
  }

  /**
   * Retrieve access token fails.
   */
  @Test
  void retrieveAccessTokenFails() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    //noinspection unchecked
    when(restTemplate.exchange(
        anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
        .thenReturn(ResponseEntity.ok("{\"illegal_token\":\"junit_access_token_value\"}"));
    RestTemplateAccessTokenRetriever tokenRetriever = new RestTemplateAccessTokenRetriever(
        restTemplate);

    PasswordFlowProperties properties = PasswordFlowProperties.builder()
        .tokenEndpoint("http://localhost/token")
        .clientId("123")
        .clientSecret("456")
        .username("789")
        .password("012")
        .build();

    assertThrows(
        AccessTokenRetrieverAuthenticationException.class,
        () -> tokenRetriever.retrieveAccessToken(properties));
  }

}