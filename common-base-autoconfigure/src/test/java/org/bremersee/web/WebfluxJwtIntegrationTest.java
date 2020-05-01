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

package org.bremersee.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bremersee.test.security.authentication.WithJwtAuthenticationToken;
import org.bremersee.web.app.reactive.WebfluxTestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * The webflux jwt integration test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = WebfluxTestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=reactive",
        "spring.application.name=reactive-test",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwk",
        "bremersee.security.authentication.resource-server-auto-configuration=true",
        "bremersee.security.authentication.enable-jwt-support=true",
        "bremersee.security.authentication.any-access-mode=deny_all",
        "bremersee.security.authentication.path-matchers[0].ant-pattern=/public/**",
        "bremersee.security.authentication.path-matchers[0].access-mode=permit_all",
        "bremersee.security.authentication.path-matchers[1].ant-pattern=/protected/**",
        "bremersee.security.authentication.path-matchers[1].roles=ROLE_USER",
        "bremersee.security.authentication.path-matchers[2].ant-pattern=/protected/**",
        "bremersee.security.authentication.path-matchers[2].http-method=POST",
        "bremersee.security.authentication.path-matchers[2].roles=ROLE_ADMIN",
        "bremersee.exception-mapping.api-paths=/**",
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class WebfluxJwtIntegrationTest {

  /**
   * The application context.
   */
  @Autowired
  ApplicationContext context;

  /**
   * The test web client.
   */
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  WebTestClient webClient;

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
  @WithJwtAuthenticationToken(roles = "ROLE_USER")
  @Test
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
   * Gets protected anf expect forbidden.
   */
  @WithJwtAuthenticationToken(roles = "ROLE_SOMETHING")
  @Test
  void getProtectedAnfExpectForbidden() {
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
  @WithJwtAuthenticationToken(roles = "ROLE_ADMIN")
  @Test
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
