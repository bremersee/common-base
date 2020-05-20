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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

/**
 * The access token cache test.
 *
 * @author Christian Bremer
 */
class AccessTokenCacheTest {

  private static final Date exp = new Date(
      System.currentTimeMillis() + Duration.ofDays(300).toMillis());

  private static final String signedAccessTokenWithoutExp = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
      + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"
      + ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

  private static final String plainAccessTokenWithExp = new PlainJWT(new JWTClaimsSet.Builder()
      .subject("subject")
      .expirationTime(exp)
      .jwtID(UUID.randomUUID().toString())
      .build())
      .serialize();

  /**
   * Is expired.
   */
  @Test
  void isExpired() {
    assertTrue(AccessTokenCache.isExpired(signedAccessTokenWithoutExp, Duration.ofSeconds(1L)));
    assertFalse(AccessTokenCache.isExpired(plainAccessTokenWithExp, null));
  }

  /**
   * Gets expiration time.
   */
  @Test
  void getExpirationTime() {
    assertNull(AccessTokenCache.getExpirationTime(signedAccessTokenWithoutExp));
    Date actual = AccessTokenCache.getExpirationTime(plainAccessTokenWithExp);
    assertNotNull(actual);
    assertEquals(exp.getTime() / 1000L, actual.getTime() / 1000L);
  }

  /**
   * Parse.
   */
  @Test
  void parse() {
    assertNotNull(AccessTokenCache.parse(signedAccessTokenWithoutExp));
    assertNotNull(AccessTokenCache.parse(plainAccessTokenWithExp));
  }

  /**
   * Builder.
   */
  @Test
  void builder() {
    assertNotNull(AccessTokenCache.builder());
    assertNotNull(AccessTokenCache.builder().build());
    assertNotNull(AccessTokenCache.builder().withExternalCache(mock(Cache.class)).build());
    assertNotNull(AccessTokenCache.builder().withKeyPrefix("jwt_").build());
    assertNotNull(
        AccessTokenCache.builder().withExpirationTimeThreshold(Duration.ofSeconds(30L)).build());
  }

}