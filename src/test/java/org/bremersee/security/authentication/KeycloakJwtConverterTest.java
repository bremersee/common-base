/*
 * Copyright 2017 the original author or authors.
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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * @author Christian Bremer
 */
public class KeycloakJwtConverterTest {

  @Test
  public void testConvertRoles() {

    final Map<String, Object> roles = new LinkedHashMap<>();
    roles.put("roles", Arrays.asList("ADMIN", "ROLE_USER"));
    final Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("realm_access", roles);

    Jwt jwt = Mockito.mock(Jwt.class);
    Mockito.when(jwt.getClaims()).thenReturn(claims);

    final KeycloakJwtConverter converter = new KeycloakJwtConverter();
    final JwtAuthenticationToken authToken = converter.convert(jwt);
    Assert.assertNotNull(authToken);
    Assert.assertNotNull(authToken.getAuthorities());
    Assert.assertTrue(
        authToken
            .getAuthorities()
            .contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    Assert.assertTrue(
        authToken
            .getAuthorities()
            .contains(new SimpleGrantedAuthority("ROLE_USER")));
  }

}
