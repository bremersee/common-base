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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.bremersee.security.authentication.AuthenticationProperties.PasswordFlow;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The password flow authentication manager test.
 *
 * @author Christian Bremer
 */
public class PasswordFlowReactiveAuthenticationManagerTest {

  /**
   * Tests authenticate.
   */
  @Test
  public void authenticate() {
    Map<String, Object> headers = new HashMap<>();
    headers.put("test-key", "test-value");
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "an_username");
    Jwt jwt = jwt(headers, claims);

    PasswordFlowReactiveAuthenticationManager manager = workingManager(jwt);

    Authentication loginAuthentication = mock(Authentication.class);
    when(loginAuthentication.getName()).thenReturn("an_username");
    when(loginAuthentication.getCredentials()).thenReturn("a_password");

    StepVerifier.create(manager.authenticate(loginAuthentication))
        .assertNext(authentication -> {
          assertTrue(authentication instanceof JwtAuthenticationToken);
          JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;
          Jwt actualJwt = authenticationToken.getToken();
          assertNotNull(actualJwt);
          assertEquals(jwt.getClaims(), actualJwt.getClaims());
          assertEquals(jwt.getHeaders(), actualJwt.getHeaders());
        })
        .expectNextCount(0)
        .verifyComplete();
  }

  /**
   * Tests authenticate fails.
   */
  @Test
  public void authenticateFails() {
    PasswordFlowReactiveAuthenticationManager manager = notWorkingManager();

    Authentication loginAuthentication = mock(Authentication.class);
    when(loginAuthentication.getName()).thenReturn("an_username");
    when(loginAuthentication.getCredentials()).thenReturn("a_password");

    StepVerifier.create(manager.authenticate(loginAuthentication))
        .expectErrorMatches(throwable -> throwable instanceof OAuth2AuthenticationException)
        .verify();

  }

  private static AuthenticationProperties authenticationProperties() {
    PasswordFlow passwordFlow = new PasswordFlow();
    passwordFlow.setClientId("abc");
    passwordFlow.setClientSecret("xyz");
    passwordFlow.setSystemPassword("XYZ");
    passwordFlow.setSystemUsername("ABC");
    passwordFlow.setTokenEndpoint("http://localhost/token");
    AuthenticationProperties properties = new AuthenticationProperties();
    properties.setPasswordFlow(passwordFlow);
    return properties;
  }

  private static AccessTokenRetriever<Mono<String>> retriever() {
    //noinspection unchecked
    AccessTokenRetriever<Mono<String>> accessTokenRetriever = mock(
        AccessTokenRetriever.class);
    when(accessTokenRetriever.retrieveAccessToken(any(PasswordFlowProperties.class)))
        .thenReturn(Mono.just("an_access_token"));
    return accessTokenRetriever;
  }

  private static Jwt jwt(Map<String, Object> headers, Map<String, Object> claims) {
    String tokenValue = "an_access_token";
    Instant issuedAt = Instant.now();
    Instant expiresAt = Instant.now().plus(1L, ChronoUnit.HOURS);
    return new Jwt(tokenValue, issuedAt, expiresAt, headers, claims);
  }

  private static ReactiveJwtDecoder workingJwtDecoder(Jwt jwt) {
    ReactiveJwtDecoder jwtDecoder = mock(ReactiveJwtDecoder.class);
    when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));
    return jwtDecoder;
  }

  private static PasswordFlowReactiveAuthenticationManager workingManager(Jwt jwt) {
    return new PasswordFlowReactiveAuthenticationManager(
        authenticationProperties(),
        workingJwtDecoder(jwt),
        null,
        retriever());
  }

  private static ReactiveJwtDecoder notWorkingJwtDecoder() {
    ReactiveJwtDecoder jwtDecoder = mock(ReactiveJwtDecoder.class);
    when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("Expected test error"));
    return jwtDecoder;
  }

  private static PasswordFlowReactiveAuthenticationManager notWorkingManager() {
    return new PasswordFlowReactiveAuthenticationManager(
        authenticationProperties(),
        notWorkingJwtDecoder(),
        null,
        retriever());
  }

}