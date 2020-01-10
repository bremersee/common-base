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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.exception.AccessTokenRetrieverAuthenticationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The web client access token retriever test.
 *
 * @author Christian Bremer
 */
class WebClientAccessTokenRetrieverTest {

  /**
   * Retrieve access token.
   */
  @Test
  void retrieveAccessToken() {
    final String jwt = "{\"access_token\": \"test-token\"}";

    ResponseSpec responseSpec = mock(ResponseSpec.class);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    //noinspection unchecked
    when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(jwt));

    //noinspection rawtypes
    RequestHeadersSpec requestHeadersSpec = mock(RequestHeadersSpec.class);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    RequestBodySpec requestBodySpec = mock(RequestBodySpec.class);
    when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
    //noinspection unchecked
    when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);

    RequestBodyUriSpec requestBodyUriSpec = mock(RequestBodyUriSpec.class);
    when(requestBodyUriSpec.uri(Mockito.anyString())).thenReturn(requestBodySpec);

    WebClient webClient = mock(WebClient.class);
    when(webClient.method(any(HttpMethod.class))).thenReturn(requestBodyUriSpec);

    AccessTokenRetrieverProperties properties = new AccessTokenRetrieverProperties() {
      @Override
      public String getTokenEndpoint() {
        return "http://localhost/token";
      }

      @Override
      public MultiValueMap<String, String> createBody() {
        return new LinkedMultiValueMap<>();
      }
    };

    WebClientAccessTokenRetriever retriever = new WebClientAccessTokenRetriever(webClient);
    StepVerifier.create(retriever.retrieveAccessToken(properties))
        .assertNext(token -> assertEquals("test-token", token))
        .verifyComplete();
  }

  /**
   * Build exception.
   */
  @Test
  void buildException() {
    ClientResponse clientResponse = mock(ClientResponse.class);
    when(clientResponse.statusCode()).thenReturn(HttpStatus.FORBIDDEN);

    WebClientAccessTokenRetriever retriever = new WebClientAccessTokenRetriever();
    AuthenticationException exception = retriever.buildException(clientResponse, "Sorry");
    assertNotNull(exception);
    assertTrue(exception instanceof AccessTokenRetrieverAuthenticationException);
    assertEquals(
        HttpStatus.FORBIDDEN.value(),
        ((AccessTokenRetrieverAuthenticationException) exception).status());
  }
}