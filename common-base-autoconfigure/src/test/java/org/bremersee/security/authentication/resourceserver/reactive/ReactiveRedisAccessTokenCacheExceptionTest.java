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

package org.bremersee.security.authentication.resourceserver.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import org.bremersee.security.authentication.ReactiveRedisAccessTokenCache;
import org.bremersee.security.authentication.resourceserver.reactive.withredis.WithRedisTestConfiguration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import reactor.test.StepVerifier;

/**
 * The reactive jwt test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = WithRedisTestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=reactive",
        "spring.application.name=resourceserver-jwt",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwk",
        "bremersee.redis.embedded=false",
        "bremersee.auth.resource-server=auto",
        "bremersee.auth.any-access-mode=deny_all",
        "bremersee.exception-mapping.api-paths=/**"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class ReactiveRedisAccessTokenCacheExceptionTest {

  /**
   * The Cache.
   */
  @Autowired(required = false)
  ReactiveRedisAccessTokenCache cache;

  /**
   * Not null.
   */
  @Test
  @Order(1)
  void notNull() {
    assertNotNull(cache);
  }

  /**
   * Return empty because of an exception.
   */
  @Test
  @Order(2)
  void returnEmptyBecauseOfException() {
    StepVerifier.create(cache.findAccessToken("a-key"))
        .verifyComplete();
  }

  /**
   * Return access token besides of an exception.
   */
  @Test
  @Order(3)
  void returnAccessTokenBesidesOfAnException() {
    final Date exp = new Date(System.currentTimeMillis() + Duration.ofDays(300).toMillis());
    cache.setFindExpirationTimeFn(accessToken -> exp);
    final String accessToken = UUID.randomUUID().toString();
    StepVerifier.create(cache.putAccessToken("a.key", accessToken))
        .assertNext(value -> assertEquals(accessToken, value))
        .verifyComplete();
  }

}
