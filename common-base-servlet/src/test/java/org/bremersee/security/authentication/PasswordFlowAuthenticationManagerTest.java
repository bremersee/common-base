package org.bremersee.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.MultiValueMap;

/**
 * The password flow authentication manager test.
 *
 * @author Christian Bremer
 */
public class PasswordFlowAuthenticationManagerTest {

  private static OAuth2Properties oauth2Properties() {
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

  private static AccessTokenRetriever<MultiValueMap<String, String>, String> tokenRetriever() {
    //noinspection unchecked
    AccessTokenRetriever<MultiValueMap<String, String>, String> accessTokenRetriever = mock(
        AccessTokenRetriever.class);
    //noinspection unchecked
    when(accessTokenRetriever.retrieveAccessToken(any(MultiValueMap.class)))
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
        oauth2Properties(),
        workingJwtDecoder(jwt),
        tokenRetriever());
  }

  private static JwtDecoder notWorkingJwtDecoder() {
    JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("Test error"));
    return jwtDecoder;
  }

  private static PasswordFlowAuthenticationManager notWorkingManager() {
    return new PasswordFlowAuthenticationManager(
        oauth2Properties(),
        notWorkingJwtDecoder(),
        tokenRetriever());
  }

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
  @Test(expected = OAuth2AuthenticationException.class)
  public void authenticateFails() {
    PasswordFlowAuthenticationManager manager = notWorkingManager();

    Authentication loginAuthentication = mock(Authentication.class);
    when(loginAuthentication.getName()).thenReturn("an_username");
    when(loginAuthentication.getCredentials()).thenReturn("a_password");

    manager.authenticate(loginAuthentication);
  }

  /**
   * Tests supports.
   */
  @Test
  public void supports() {
    Map<String, Object> headers = new HashMap<>();
    headers.put("test-key", "test-value");
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "an_username");
    Jwt jwt = jwt(headers, claims);
    PasswordFlowAuthenticationManager manager = workingManager(jwt);
    assertTrue(manager.supports(UsernamePasswordAuthenticationToken.class));
    assertFalse(manager.supports(OAuth2Properties.class));
  }
}