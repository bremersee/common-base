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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * The json path jwt converter test.
 *
 * @author Christian Bremer
 */
class JsonPathJwtConverterTest {

  /**
   * Convert jwt.
   */
  @Test
  void convert() {
    Map<String, Object> headers = new HashMap<>();
    headers.put("alg", "RS256");
    headers.put("kid", "-V5Nzr9xllRiyURYK-t6pB7C-5E8IKm8-eAPFLVvCt4");
    headers.put("typ", "JWT");

    Date exp = new Date(System.currentTimeMillis() + 1000L * 60L * 15L);
    Date iat = new Date();
    String sub = UUID.randomUUID().toString();
    JWTClaimsSet.Builder builder = new Builder();
    builder.audience("account");
    builder.expirationTime(exp);
    builder.issuer("https://openid.dev.bremersee.org/auth/realms/omnia");
    builder.issueTime(iat);
    builder.jwtID("48502385-7f37-47da-abc9-a223b8972795");
    builder.notBeforeTime(iat);
    builder.subject(sub);

    builder.claim("sub", "livia");
    builder.claim("scope", "email profile");
    builder.claim("email_verified", false);
    builder.claim("name", "Anna Livia Plurabelle");
    builder.claim("preferred_username", "anna");
    builder.claim("given_name", "Anna Livia");
    builder.claim("family_name", "Plurabelle");
    builder.claim("email", "anna@example.org");

    Map<String, Object> roles = new LinkedHashMap<>();
    roles.put("roles", Collections.singletonList("LOCAL_USER"));
    builder.claim("realm_access", roles);

    JWTClaimsSet claimsSet = builder.build();
    Jwt jwt = new Jwt(
        "an-access-token",
        iat.toInstant(),
        exp.toInstant(),
        headers,
        claimsSet.getClaims());

    JsonPathJwtConverter converter = new JsonPathJwtConverter();
    converter.setRolesValueList(true);
    converter.setRolesJsonPath("$.realm_access.roles");
    converter.setNameJsonPath("$.preferred_username");
    converter.setRolePrefix("ROLE_");
    JwtAuthenticationToken auth = converter.convert(jwt);
    assertNotNull(auth);
    assertEquals("anna", auth.getName());
    assertTrue(auth.getAuthorities().stream()
        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority()
            .equalsIgnoreCase("ROLE_LOCAL_USER")));

    converter.setRolesValueList(false);
    converter.setRolesJsonPath("$.scope");
    converter.setNameJsonPath("$.sub");
    converter.setRolePrefix("SCOPE_");
    converter.setRolesValueSeparator(" ");
    auth = converter.convert(jwt);
    assertNotNull(auth);
    assertEquals("livia", auth.getName());
    assertTrue(auth.getAuthorities().stream()
        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority()
            .equalsIgnoreCase("SCOPE_EMAIL")));
    assertTrue(auth.getAuthorities().stream()
        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority()
            .equalsIgnoreCase("SCOPE_PROFILE")));
  }

  /**
   * Test convert roles.
   */
  @Test
  void testConvertRoles() {

    final Map<String, Object> roles = new LinkedHashMap<>();
    roles.put("roles", Arrays.asList("ADMIN", "ROLE_USER"));
    final Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("realm_access", roles);

    Jwt jwt = mock(Jwt.class);
    when(jwt.getClaims()).thenReturn(claims);

    JsonPathJwtConverter converter = new JsonPathJwtConverter();
    converter.setRolesValueList(true);
    converter.setRolesJsonPath("$.realm_access.roles");
    converter.setRolePrefix("ROLE_");
    final JwtAuthenticationToken authToken = converter.convert(jwt);
    assertNotNull(authToken);
    assertNotNull(authToken.getAuthorities());
    assertTrue(
        authToken
            .getAuthorities()
            .contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    assertTrue(
        authToken
            .getAuthorities()
            .contains(new SimpleGrantedAuthority("ROLE_USER")));
  }

}