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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;

/**
 * The json path jwt authentication details test.
 *
 * @author Christian Bremer
 */
class JsonPathJwtAuthenticationDetailsTest {

  private static final String LOCALE_CLAIM = "preferred_language";

  private static final String TIME_ZONE_CLAIM = "preferred_time_zone";

  private static final Locale DEFAULT_LOCALE = Locale.FRANCE;

  private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Australia/Eucla");

  private static final JsonPathJwtAuthenticationDetails details = new JsonPathJwtAuthenticationDetails(
      DEFAULT_LOCALE,
      DEFAULT_TIME_ZONE,
      "$." + LOCALE_CLAIM,
      "$." + TIME_ZONE_CLAIM
  );

  /**
   * Gets default locale.
   */
  @Test
  void getDefaultLocale() {
    assertEquals(DEFAULT_LOCALE, details.getDefaultLocale());
  }

  /**
   * Gets default time zone.
   */
  @Test
  void getDefaultTimeZone() {
    assertEquals(DEFAULT_TIME_ZONE, details.getDefaultTimeZone());
  }

  /**
   * Gets preferred language.
   */
  @Test
  void getPreferredLanguage() {
    Locale expected = Locale.ITALY;
    Optional<Locale> actual = details.getPreferredLanguage(
        createJwtAuthenticationToken(expected.toString(), null));
    assertTrue(actual.isPresent());
    assertEquals(expected, actual.get());

    actual = details.getPreferredLanguage(
        createJwtAuthenticationToken(null, null));
    assertFalse(actual.isPresent());

    actual = details.getPreferredLanguage(null);
    assertFalse(actual.isPresent());
  }

  /**
   * Gets preferred time zone.
   */
  @Test
  void getPreferredTimeZone() {
    TimeZone expected = TimeZone.getTimeZone("GMT");
    Optional<TimeZone> actual = details.getPreferredTimeZone(
        createJwtAuthenticationToken(null, expected.getID()));
    assertTrue(actual.isPresent());
    assertEquals(expected, actual.get());

    actual = details.getPreferredTimeZone(
        createJwtAuthenticationToken(null, null));
    assertFalse(actual.isPresent());

    actual = details.getPreferredTimeZone(null);
    assertFalse(actual.isPresent());
  }

  private JwtAuthenticationToken createJwtAuthenticationToken(String locale, String timeZone) {
    return new JwtAuthenticationToken(createJwt(locale, timeZone));
  }

  private static Jwt createJwt(String locale, String timeZone) {
    Map<String, Object> headers = new HashMap<>();
    headers.put("alg", "RS256");
    headers.put("kid", "-V5Nzr9xllRiyURYK-t6pB7C-5E8IKm8-eAPFLVvCt4");
    headers.put("typ", "JWT");

    Map<String, Object> roles = new LinkedHashMap<>();
    roles.put("roles", Collections.singletonList("LOCAL_USER"));

    Date exp = new Date(System.currentTimeMillis() + 1000L * 60L * 15L);
    Date iat = new Date();
    String sub = UUID.randomUUID().toString();
    JWTClaimsSet.Builder builder = new Builder()
        .audience("account")
        .expirationTime(exp)
        .issuer("https://openid.dev.bremersee.org/auth/realms/omnia")
        .issueTime(iat)
        .jwtID("48502385-7f37-47da-abc9-a223b8972795")
        .notBeforeTime(iat)
        .subject(sub)

        .claim("realm_access", roles)
        .claim("sub", "livia")
        .claim("scope", "email profile")
        .claim("email_verified", false)
        .claim("name", "Anna Livia Plurabelle")
        .claim("preferred_username", "anna")
        .claim("given_name", "Anna Livia")
        .claim("family_name", "Plurabelle")
        .claim("email", "anna@example.org");

    if (StringUtils.hasText(locale)) {
      builder = builder.claim(LOCALE_CLAIM, locale);
    }
    if (StringUtils.hasText(timeZone)) {
      builder = builder.claim(TIME_ZONE_CLAIM, timeZone);
    }

    JWTClaimsSet claimsSet = builder.build();
    return new Jwt(
        "an-access-token",
        iat.toInstant(),
        exp.toInstant(),
        headers,
        claimsSet.getClaims());
  }
}