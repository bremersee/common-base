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

import java.util.UUID;
import org.bremersee.security.authentication.resourceserver.reactive.withredis.WithRedisTestConfiguration;
import org.bremersee.test.security.authentication.WithJwtAuthenticationToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
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
        "bremersee.redis.embedded=true",
        "bremersee.auth.resource-server=auto",
        "bremersee.auth.any-access-mode=deny_all",
        "bremersee.auth.path-matchers[0].ant-pattern=/public/**",
        "bremersee.auth.path-matchers[0].access-mode=permit_all",
        "bremersee.auth.path-matchers[1].ant-pattern=/protected/**",
        "bremersee.auth.path-matchers[1].http-method=POST",
        "bremersee.auth.path-matchers[1].roles=ROLE_ADMIN",
        "bremersee.auth.path-matchers[2].ant-pattern=/protected/**",
        "bremersee.auth.path-matchers[2].roles=ROLE_USER",
        "bremersee.exception-mapping.api-paths=/**"
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class ReactiveJwtWithRedisTest {

  /**
   * The application context.
   */
  @Autowired
  ApplicationContext context;

  /**
   * The test web client (security configuration is by-passed).
   */
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  WebTestClient webClient;

  @Autowired
  ReactiveRedisTemplate<String, String> redisTemplate;

  /**
   * Setup tests.
   */
  @BeforeAll
  void setUp() {
    // https://docs.spring.io/spring-security/site/docs/current/reference/html/test-webflux.html
    WebTestClient
        .bindToApplicationContext(this.context)
        .configureClient()
        .build();
  }

  @Test
  void writeAndReadRedisValue() {
    String key = UUID.randomUUID().toString();
    String value = UUID.randomUUID().toString();
    StepVerifier
        .create(redisTemplate.opsForValue().set(key, value))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();
    StepVerifier
        .create(redisTemplate.opsForValue().get(key))
        .assertNext(readValue -> assertEquals(value, readValue))
        .verifyComplete();
  }

  /**
   * Gets public.
   */
  @Test
  void getPublic() {
    webClient
        .get()
        .uri("/public")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(response -> assertEquals("public", response));
  }

  /**
   * Gets protected.
   */
  @Test
  @WithJwtAuthenticationToken(roles = "ROLE_USER")
  void getProtected() {
    webClient
        .get()
        .uri("/protected")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(response -> assertEquals("protected", response));
  }

  /**
   * Gets protected and expect forbidden.
   */
  @Test
  @WithJwtAuthenticationToken(roles = "ROLE_SOMETHING")
  void getProtectedAndExpectForbidden() {
    webClient
        .get()
        .uri("/protected")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  /**
   * Post protected.
   */
  @Test
  @WithJwtAuthenticationToken(roles = "ROLE_ADMIN")
  void postProtected() {
    webClient
        .post()
        .uri("/protected")
        .contentType(MediaType.TEXT_PLAIN)
        .accept(MediaType.TEXT_PLAIN)
        .body(BodyInserters.fromValue("hello"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(response -> assertEquals("hello", response));
  }

}
