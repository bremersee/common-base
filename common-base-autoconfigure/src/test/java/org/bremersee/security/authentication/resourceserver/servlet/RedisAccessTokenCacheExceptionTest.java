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

package org.bremersee.security.authentication.resourceserver.servlet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Date;
import org.bremersee.security.authentication.RedisAccessTokenCache;
import org.bremersee.security.authentication.resourceserver.servlet.withredis.WithRedisTestConfiguration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * The jwt test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = WithRedisTestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=resourceserver-jwt",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwk",
        "bremersee.redis.embedded=false",
        "bremersee.auth.resource-server=auto",
        "bremersee.auth.any-access-mode=deny_all",
        "bremersee.exception-mapping.api-paths=/**"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class RedisAccessTokenCacheExceptionTest {

  /**
   * The cache.
   */
  @Autowired(required = false)
  RedisAccessTokenCache cache;

  /**
   * Not null.
   */
  @Test
  @Order(1)
  void notNull() {
    assertNotNull(cache);
  }

  /**
   * Returns empty because of an exception.
   */
  @Test
  @Order(2)
  void returnEmptyBecauseOfException() {
    assertTrue(cache.findAccessToken("a.key").isEmpty());
  }

  /**
   * Does not throw the exception.
   */
  @Test
  @Order(3)
  void doesNotThrowAnException() {
    final Date exp = new Date(System.currentTimeMillis() + Duration.ofDays(300).toMillis());
    cache.setFindExpirationTimeFn(accessToken -> exp);
    assertDoesNotThrow(() -> cache.putAccessToken("a-key", "access-token"));
  }

}
