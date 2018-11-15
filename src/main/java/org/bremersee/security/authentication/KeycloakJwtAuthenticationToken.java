/*
 * Copyright 2018 the original author or authors.
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

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * The {@link JwtAuthenticationToken} of a keycloak identity provider.
 *
 * @author Christian Bremer
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class KeycloakJwtAuthenticationToken extends JwtAuthenticationToken {

  /**
   * The constant KEYCLOAK_PREFERRED_USERNAME.
   */
  public static final String KEYCLOAK_PREFERRED_USERNAME = "preferred_username";

  /**
   * Instantiates a new keycloak jwt authentication token.
   *
   * @param jwt the jwt
   */
  public KeycloakJwtAuthenticationToken(Jwt jwt) {
    super(jwt);
  }

  /**
   * Instantiates a new keycloak jwt authentication token.
   *
   * @param jwt         the jwt
   * @param authorities the authorities
   */
  public KeycloakJwtAuthenticationToken(Jwt jwt,
      Collection<? extends GrantedAuthority> authorities) {
    super(jwt, authorities);
  }

  /**
   * Gets preferred name.
   *
   * @return the preferred name
   */
  public String getPreferredName() {
    if (getToken().containsClaim(KEYCLOAK_PREFERRED_USERNAME)) {
      return getToken().getClaimAsString(KEYCLOAK_PREFERRED_USERNAME);
    }
    return super.getName(); // return sub
  }

}
