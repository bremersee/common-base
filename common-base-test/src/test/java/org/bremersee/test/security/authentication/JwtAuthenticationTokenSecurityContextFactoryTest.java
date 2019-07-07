package org.bremersee.test.security.authentication;

import static org.bremersee.security.core.AuthorityConstants.LOCAL_USER_ROLE_NAME;
import static org.bremersee.security.core.AuthorityConstants.USER_ROLE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.bremersee.security.authentication.KeycloakJwtAuthenticationToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The jwt authentication token security context factory test.
 *
 * @author Christian Bremer
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JwtAuthenticationTokenSecurityContextFactoryTest {

  /**
   * Create security context.
   */
  @WithJwtAuthenticationToken(
      audience = "account",
      issuer = "https://openid.dev.bremersee.org/auth/realms/omnia",
      scope = {"email", "profile"},
      jwtId = "080836fb-7e74-4a56-92ba-08aeaf9a3852",
      subject = "1918e152-294b-4701-a2c8-b9090bb5aa06",
      email = "plurabelle@example.net",
      roles = {USER_ROLE_NAME, LOCAL_USER_ROLE_NAME})
  @Test
  public void createSecurityContext() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertTrue(authentication instanceof KeycloakJwtAuthenticationToken);
    KeycloakJwtAuthenticationToken authToken = (KeycloakJwtAuthenticationToken) authentication;
    assertEquals("account", authToken.getToken().getClaimAsString("aud"));
    assertEquals(
        "https://openid.dev.bremersee.org/auth/realms/omnia",
        authToken.getToken().getClaimAsString("iss"));
    assertEquals(
        "080836fb-7e74-4a56-92ba-08aeaf9a3852",
        authToken.getToken().getClaimAsString("jti"));
    assertEquals(
        "1918e152-294b-4701-a2c8-b9090bb5aa06",
        authToken.getToken().getClaimAsString("sub"));
    assertEquals(
        "Anna Livia Plurabelle",
        authToken.getToken().getClaimAsString("name"));
    assertEquals(
        "anna",
        authToken.getPreferredName());
    assertEquals(
        "Anna Livia",
        authToken.getToken().getClaimAsString("given_name"));
    assertEquals(
        "Plurabelle",
        authToken.getToken().getClaimAsString("family_name"));
    assertEquals(
        "plurabelle@example.net",
        authToken.getToken().getClaimAsString("email"));
    List<String> scopes = authToken.getToken().getClaimAsStringList("scope");
    assertNotNull(scopes);
    assertTrue(scopes.contains("email"));
    assertTrue(scopes.contains("profile"));
    assertTrue(authToken.getAuthorities().contains(new SimpleGrantedAuthority(USER_ROLE_NAME)));
    assertTrue(
        authToken.getAuthorities().contains(new SimpleGrantedAuthority(LOCAL_USER_ROLE_NAME)));
  }

}