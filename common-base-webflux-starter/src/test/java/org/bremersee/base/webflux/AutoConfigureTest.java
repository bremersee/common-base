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

package org.bremersee.base.webflux;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.base.webflux.app.TestConfiguration;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.security.core.AuthorityConstants;
import org.bremersee.test.security.authentication.WithJwtAuthenticationToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * The auto configure test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.application.name=common-base-webflux-test",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwk",
        "bremersee.exception-mapping.api-paths=/api/**",
        "bremersee.security.authentication.enable-jwt-support=true"
    })
@Slf4j
public class AutoConfigureTest {

  /**
   * The Web client.
   */
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  WebTestClient webClient;

  /**
   * Current user name.
   */
  @WithJwtAuthenticationToken(preferredUsername = "leopold")
  @Test
  void currentUserName() {
    webClient
        .get()
        .uri("/api/name")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .value(actual -> assertEquals("leopold", actual));
  }

  /**
   * Current admin name.
   */
  @WithJwtAuthenticationToken(
      preferredUsername = "leopold",
      roles = {AuthorityConstants.ADMIN_ROLE_NAME})
  @Test
  void currentAdminName() {
    webClient
        .get()
        .uri("/api/admin/name")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .value(actual -> assertEquals("leopold", actual));
  }

  /**
   * Current admin name and expect forbidden.
   */
  @Test
  void currentAdminNameAndExpectForbidden() {
    webClient
        .get()
        .uri("/api/admin/name")
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(String.class)
        .value(Assertions::assertNull);
  }

  /**
   * Exception.
   */
  @WithJwtAuthenticationToken(preferredUsername = "leopold")
  @Test
  void exception() {
    webClient
        .get()
        .uri("/api/exception")
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(RestApiException.class)
        .value(actual -> assertEquals("TEST:4711", actual.getErrorCode()));
  }

  /**
   * Health.
   */
  @Test
  void health() {
    webClient
        .get()
        .uri("/actuator/health")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .value(actual -> assertEquals("{\"status\":\"UP\"}", actual));
  }

  /**
   * Info.
   */
  @WithMockUser(username = "actuator", authorities = {AuthorityConstants.ACTUATOR_ROLE_NAME})
  @Test
  void info() {
    webClient
        .get()
        .uri("/actuator/info")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .value(Assertions::assertNotNull);
  }

  /**
   * Info and expect client error.
   */
  @Test
  void infoAndExpectClientError() {
    webClient
        .get()
        .uri("/actuator/info")
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(String.class)
        .value(Assertions::assertNull);
  }

}
