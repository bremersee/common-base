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
import org.bremersee.security.OAuth2Properties;
import org.bremersee.security.OAuth2Properties.PasswordFlowProperties;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.MultiValueMap;
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

  private static OAuth2Properties oAuth2Properties() {
    PasswordFlowProperties passwordFlowProperties = new PasswordFlowProperties();
    passwordFlowProperties.setClientId("abc");
    passwordFlowProperties.setClientSecret("xyz");
    passwordFlowProperties.setExpirationTimeRemainsMillis(10000L);
    passwordFlowProperties.setSystemPassword("XYZ");
    passwordFlowProperties.setSystemUsername("ABC");
    passwordFlowProperties.setTokenEndpoint("http://localhost/token");
    OAuth2Properties properties = new OAuth2Properties();
    properties.setPasswordFlow(passwordFlowProperties);
    return properties;
  }

  private static AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> retriever() {
    //noinspection unchecked
    AccessTokenRetriever<MultiValueMap<String, String>, Mono<String>> accessTokenRetriever = mock(
        AccessTokenRetriever.class);
    //noinspection unchecked
    when(accessTokenRetriever.retrieveAccessToken(any(MultiValueMap.class)))
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
        oAuth2Properties(),
        workingJwtDecoder(jwt),
        retriever());
  }

  private static ReactiveJwtDecoder notWorkingJwtDecoder() {
    ReactiveJwtDecoder jwtDecoder = mock(ReactiveJwtDecoder.class);
    when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("Test error"));
    return jwtDecoder;
  }

  private static PasswordFlowReactiveAuthenticationManager notWorkingManager() {
    return new PasswordFlowReactiveAuthenticationManager(
        oAuth2Properties(),
        notWorkingJwtDecoder(),
        retriever());
  }

}