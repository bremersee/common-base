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

import static org.assertj.core.api.InstanceOfAssertFactories.collection;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.bremersee.security.core.AuthorityConstants.LOCAL_USER_ROLE_NAME;
import static org.bremersee.security.core.AuthorityConstants.USER_ROLE_NAME;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * The jwt authentication token security context factory test.
 *
 * @author Christian Bremer
 */
@SpringJUnitConfig
@ExtendWith(SoftAssertionsExtension.class)
class JwtAuthenticationTokenSecurityContextFactoryTest {

  /**
   * Create security context.
   *
   * @param softly the softly
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
  void createSecurityContext(SoftAssertions softly) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    softly.assertThat(authentication)
        .isInstanceOf(JwtAuthenticationToken.class);
    if (!(authentication instanceof JwtAuthenticationToken)) {
      return;
    }

    JwtAuthenticationToken authToken = (JwtAuthenticationToken) authentication;
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getToken, type(ClaimAccessor.class))
        .extracting(jwt -> jwt.getClaimAsString("aud"))
        .isEqualTo("account");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getToken, type(ClaimAccessor.class))
        .extracting(jwt -> jwt.getClaimAsString("iss"))
        .isEqualTo("https://openid.dev.bremersee.org/auth/realms/omnia");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getToken, type(ClaimAccessor.class))
        .extracting(jwt -> jwt.getClaimAsString("jti"))
        .isEqualTo("080836fb-7e74-4a56-92ba-08aeaf9a3852");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getToken, type(ClaimAccessor.class))
        .extracting(jwt -> jwt.getClaimAsString("sub"))
        .isEqualTo("1918e152-294b-4701-a2c8-b9090bb5aa06");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getToken, type(ClaimAccessor.class))
        .extracting(jwt -> jwt.getClaimAsString("name"))
        .isEqualTo("Anna Livia Plurabelle");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getToken, type(ClaimAccessor.class))
        .extracting(jwt -> jwt.getClaimAsString("name"))
        .isEqualTo("Anna Livia Plurabelle");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getToken, type(ClaimAccessor.class))
        .extracting(jwt -> jwt.getClaimAsString("given_name"))
        .isEqualTo("Anna Livia");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getToken, type(ClaimAccessor.class))
        .extracting(jwt -> jwt.getClaimAsString("family_name"))
        .isEqualTo("Plurabelle");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getToken, type(ClaimAccessor.class))
        .extracting(jwt -> jwt.getClaimAsString("email"))
        .isEqualTo("plurabelle@example.net");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getName)
        .isEqualTo("anna");
    softly.assertThat(authToken)
        .extracting(JwtAuthenticationToken::getAuthorities, collection(GrantedAuthority.class))
        .contains(
            new SimpleGrantedAuthority(USER_ROLE_NAME),
            new SimpleGrantedAuthority(LOCAL_USER_ROLE_NAME));

    List<String> scopes = authToken.getToken().getClaimAsStringList("scope");
    softly.assertThat(scopes)
        .contains("email", "profile");
  }

}