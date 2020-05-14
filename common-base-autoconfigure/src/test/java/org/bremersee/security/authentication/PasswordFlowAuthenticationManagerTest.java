/*
 * Copyright 2019-2020 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.bremersee.security.authentication.AuthProperties.PasswordFlow;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * The password flow authentication manager test.
 *
 * @author Christian Bremer
 */
class PasswordFlowAuthenticationManagerTest {

  private static PasswordFlow properties() {
    PasswordFlow passwordFlow = new PasswordFlow();
    passwordFlow.setClientId("abc");
    passwordFlow.setClientSecret("xyz");
    passwordFlow.setPassword("XYZ");
    passwordFlow.setUsername("ABC");
    passwordFlow.setTokenEndpoint("http://localhost/token");
    return passwordFlow;
  }

  private static AccessTokenRetriever<String> tokenRetriever() {
    //noinspection unchecked
    AccessTokenRetriever<String> accessTokenRetriever = mock(AccessTokenRetriever.class);
    when(accessTokenRetriever.retrieveAccessToken(any(PasswordFlowProperties.class)))
        .thenReturn("an_access_token");
    return accessTokenRetriever;
  }

  private static Jwt jwt(Map<String, Object> headers, Map<String, Object> claims) {
    String tokenValue = "an_access_token";
    Instant issuedAt = Instant.now();
    Instant expiresAt = Instant.now().plus(1L, ChronoUnit.HOURS);
    return new Jwt(tokenValue, issuedAt, expiresAt, headers, claims);
  }

  private static JwtDecoder workingJwtDecoder(Jwt jwt) {
    JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    return jwtDecoder;
  }

  private static PasswordFlowAuthenticationManager workingManager(Jwt jwt) {
    return new PasswordFlowAuthenticationManager(
        properties(),
        workingJwtDecoder(jwt),
        null,
        tokenRetriever());
  }

  private static JwtDecoder notWorkingJwtDecoder() {
    JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("Test error"));
    return jwtDecoder;
  }

  private static PasswordFlowAuthenticationManager notWorkingManager() {
    return new PasswordFlowAuthenticationManager(
        properties(),
        notWorkingJwtDecoder(),
        null,
        tokenRetriever());
  }

  /**
   * Tests authenticate.
   */
  @Test
  void authenticate() {
    Map<String, Object> headers = new HashMap<>();
    headers.put("test-key", "test-value");
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "an_username");
    Jwt jwt = jwt(headers, claims);

    PasswordFlowAuthenticationManager manager = workingManager(jwt);

    Authentication loginAuthentication = mock(Authentication.class);
    when(loginAuthentication.getName()).thenReturn("an_username");
    when(loginAuthentication.getCredentials()).thenReturn("a_password");

    Authentication authentication = manager.authenticate(loginAuthentication);
    assertNotNull(authentication);
    assertTrue(authentication instanceof JwtAuthenticationToken);
    JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;
    Jwt actualJwt = authenticationToken.getToken();
    assertNotNull(actualJwt);
    assertEquals(jwt.getClaims(), actualJwt.getClaims());
    assertEquals(jwt.getHeaders(), actualJwt.getHeaders());
  }

  /**
   * Tests authenticate fails.
   */
  @Test
  void authenticateFails() {
    PasswordFlowAuthenticationManager manager = notWorkingManager();

    Authentication loginAuthentication = mock(Authentication.class);
    when(loginAuthentication.getName()).thenReturn("an_username");
    when(loginAuthentication.getCredentials()).thenReturn("a_password");

    assertThrows(
        OAuth2AuthenticationException.class,
        () -> manager.authenticate(loginAuthentication));
  }

  /**
   * Tests supports.
   */
  @Test
  void supports() {
    Map<String, Object> headers = new HashMap<>();
    headers.put("test-key", "test-value");
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "an_username");
    Jwt jwt = jwt(headers, claims);
    PasswordFlowAuthenticationManager manager = workingManager(jwt);
    assertTrue(manager.supports(UsernamePasswordAuthenticationToken.class));
    assertFalse(manager.supports(AuthProperties.class));
  }
}