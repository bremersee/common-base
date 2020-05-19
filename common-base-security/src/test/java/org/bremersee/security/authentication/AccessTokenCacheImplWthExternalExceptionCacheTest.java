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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.bremersee.exception.ServiceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

/**
 * The access token cache impl test with external cache.
 *
 * @author Christian Bremer
 */
class AccessTokenCacheImplWthExternalExceptionCacheTest {

  private static final String value = UUID.randomUUID().toString();

  private static AccessTokenCacheImpl cache;

  /**
   * Configure.
   */
  @BeforeAll
  static void configure() {
    Cache external = mock(Cache.class);
    doThrow(ServiceException.badRequest()).when(external).put(any(), any());
    //noinspection unchecked
    when(external.get(any(), any(Class.class))).thenThrow(ServiceException.internalServerError());
    cache = new AccessTokenCacheImpl(external, Duration.ofSeconds(10L), null);
    cache.setExpiredBiFn((token, duration) -> false);
  }

  /**
   * Put and find access token.
   */
  @Test
  void putAndFindAccessToken() {
    String key = UUID.randomUUID().toString();
    cache.putAccessToken(key, value);
    Optional<String> result = cache.findAccessToken(key);
    assertTrue(result.isEmpty());
  }

  /**
   * Destroy.
   */
  @AfterAll
  static void destroy() {
    cache.destroy();
  }

}