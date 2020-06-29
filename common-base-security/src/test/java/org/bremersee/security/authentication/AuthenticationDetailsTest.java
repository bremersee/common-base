/*
 * Copyright 2020 the original author or authors.
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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * The authentication details interface test.
 *
 * @author Christian Bremer
 */
class AuthenticationDetailsTest {

  /**
   * Gets preferred language and time zone null safe.
   */
  @Test
  void getPreferredLanguageAndTimeZoneNullSafe() {
    final Locale expectedLocale = Locale.CANADA_FRENCH;
    final TimeZone expectedTimeZone = TimeZone.getTimeZone("Asia/Dubai");
    AuthenticationDetails details = new AuthenticationDetails() {
      @Override
      public Optional<Locale> getPreferredLanguage(Authentication authentication) {
        return Optional.of(expectedLocale);
      }

      @Override
      public Optional<TimeZone> getPreferredTimeZone(Authentication authentication) {
        return Optional.of(expectedTimeZone);
      }
    };
    assertEquals(
        expectedLocale,
        details.getPreferredLanguageNullSafe(createJwtAuthenticationToken()));
    assertEquals(
        expectedTimeZone,
        details.getPreferredTimeZoneNullSafe(createJwtAuthenticationToken()));
  }

  /**
   * Gets preferred language and time zone null safe and expect defaults.
   */
  @Test
  void getPreferredLanguageAndTimeZoneNullSafeAndExpectDefaults() {
    AuthenticationDetails details = new AuthenticationDetails() {
      @Override
      public Optional<Locale> getPreferredLanguage(Authentication authentication) {
        return Optional.empty();
      }

      @Override
      public Optional<TimeZone> getPreferredTimeZone(Authentication authentication) {
        return Optional.empty();
      }
    };
    assertEquals(
        details.getDefaultLocale(),
        details.getPreferredLanguageNullSafe(null));
    assertEquals(
        details.getDefaultLocale(),
        details.getPreferredLanguageNullSafe(createJwtAuthenticationToken()));
    assertEquals(
        details.getDefaultTimeZone(),
        details.getPreferredTimeZoneNullSafe(null));
    assertEquals(
        details.getDefaultTimeZone(),
        details.getPreferredTimeZoneNullSafe(createJwtAuthenticationToken()));
  }

  private JwtAuthenticationToken createJwtAuthenticationToken() {
    return new JwtAuthenticationToken(createJwt());
  }

  private static Jwt createJwt() {
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
    return new Jwt(
        "an-access-token",
        iat.toInstant(),
        exp.toInstant(),
        headers,
        claimsSet.getClaims());
  }
}