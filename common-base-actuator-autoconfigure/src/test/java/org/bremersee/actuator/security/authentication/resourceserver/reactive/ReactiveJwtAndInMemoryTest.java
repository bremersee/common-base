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

package org.bremersee.actuator.security.authentication.resourceserver.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.bremersee.actuator.security.authentication.resourceserver.reactive.app.TestConfiguration;
import org.bremersee.test.security.authentication.WithJwtAuthenticationToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

/**
 * The reactive jwt and in memory test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=reactive",
        "spring.application.name=resourceserver-jwt-in-memory",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwk",
        "management.endpoints.web.exposure.include=*",
        "bremersee.actuator.auth.enable=auto",
        "bremersee.auth.resource-server=auto",
        "bremersee.auth.resource-server=auto",
        "bremersee.auth.any-access-mode=deny_all",
        "bremersee.auth.path-matchers[0].ant-pattern=/public/**",
        "bremersee.auth.path-matchers[0].access-mode=permit_all",
        "bremersee.auth.path-matchers[1].ant-pattern=/protected/**",
        "bremersee.auth.path-matchers[1].http-method=POST",
        "bremersee.auth.path-matchers[1].roles=ROLE_ADMIN",
        "bremersee.auth.path-matchers[2].ant-pattern=/protected/**",
        "bremersee.auth.path-matchers[2].roles=ROLE_USER",
        "bremersee.auth.in-memory-users[0].name=user",
        "bremersee.auth.in-memory-users[0].password=user",
        "bremersee.auth.in-memory-users[0].authorities=ROLE_USER",
        "bremersee.auth.in-memory-users[1].name=admin",
        "bremersee.auth.in-memory-users[1].password=admin",
        "bremersee.auth.in-memory-users[1].authorities=ROLE_ADMIN,ROLE_ACTUATOR_ADMIN",
        "bremersee.auth.in-memory-users[2].name=someone",
        "bremersee.auth.in-memory-users[2].password=someone",
        "bremersee.auth.in-memory-users[2].authorities=ROLE_SOMETHING",
        "bremersee.auth.in-memory-users[3].name=actuator",
        "bremersee.auth.in-memory-users[3].password=actuator",
        "bremersee.auth.in-memory-users[3].authorities=ROLE_ACTUATOR",
        "bremersee.exception-mapping.api-paths=/**"
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class ReactiveJwtAndInMemoryTest {

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

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
   * Base url of the local server.
   *
   * @return the base url of the local server
   */
  String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Creates a new web client, that uses the real security configuration.
   *
   * @return the web client
   */
  WebClient newWebClient() {
    return WebClient.builder()
        .baseUrl(baseUrl())
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

  /**
   * Gets health.
   */
  @Test
  void getHealth() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/actuator/health")
        .retrieve()
        .bodyToMono(String.class))
        .assertNext(body -> assertTrue(body.contains("\"UP\"")))
        .verifyComplete();
  }

  /**
   * Gets metrics.
   */
  @Test
  void getMetrics() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/actuator/metrics")
        .headers(httpHeaders -> httpHeaders
            .setBasicAuth("actuator", "actuator", StandardCharsets.UTF_8))
        .exchange())
        .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
        .verifyComplete();
  }

  /**
   * Gets metrics and expect forbidden.
   */
  @Test
  void getMetricsAndExpectForbidden() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/actuator/metrics")
        .headers(httpHeaders -> httpHeaders
            .setBasicAuth("someone", "someone", StandardCharsets.UTF_8))
        .exchange())
        .assertNext(clientResponse -> assertEquals(
            HttpStatus.FORBIDDEN,
            clientResponse.statusCode()))
        .verifyComplete();
  }

}
