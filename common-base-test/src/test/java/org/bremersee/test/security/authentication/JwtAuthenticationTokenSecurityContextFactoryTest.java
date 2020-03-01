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

package org.bremersee.test.security.authentication;

import static org.bremersee.security.core.AuthorityConstants.LOCAL_USER_ROLE_NAME;
import static org.bremersee.security.core.AuthorityConstants.USER_ROLE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * The jwt authentication token security context factory test.
 *
 * @author Christian Bremer
 */
@SpringJUnitConfig
class JwtAuthenticationTokenSecurityContextFactoryTest {

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
  void createSecurityContext() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertTrue(authentication instanceof JwtAuthenticationToken);
    JwtAuthenticationToken authToken = (JwtAuthenticationToken) authentication;
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
        authToken.getName());
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