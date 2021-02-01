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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The reactive in memory test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=reactive",
        "spring.application.name=resourceserver-in-memory",
        "management.endpoints.web.exposure.include=*",
        "bremersee.actuator.auth.enable=auto",
        "bremersee.actuator.auth.enable-cors=true",
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
public class ReactiveInMemoryTest {

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

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
    StepVerifier.create(newWebClient()
        .get()
        .uri("/public")
        .retrieve()
        .bodyToMono(String.class))
        .assertNext(body -> assertEquals("public", body))
        .verifyComplete();
  }

  /**
   * Gets protected.
   */
  @Test
  void getProtected() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/protected")
        .headers(httpHeaders -> httpHeaders
            .setBasicAuth("user", "user", StandardCharsets.UTF_8))
        .retrieve()
        .bodyToMono(String.class))
        .assertNext(body -> assertEquals("protected", body))
        .verifyComplete();
  }

  /**
   * Gets protected and expect forbidden.
   */
  @Test
  void getProtectedAndExpectForbidden() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/protected")
        .headers(httpHeaders -> httpHeaders
            .setBasicAuth("someone", "someone", StandardCharsets.UTF_8))
        .exchangeToMono(clientResponse -> Mono.just(clientResponse.statusCode())))
        .assertNext(status -> assertEquals(HttpStatus.FORBIDDEN, status))
        .verifyComplete();
  }

  /**
   * Post protected.
   */
  @Test
  void postProtected() {
    StepVerifier.create(newWebClient()
        .post()
        .uri("/protected")
        .contentType(MediaType.TEXT_PLAIN)
        .accept(MediaType.TEXT_PLAIN)
        .headers(httpHeaders -> httpHeaders
            .setBasicAuth("admin", "admin", StandardCharsets.UTF_8))
        .body(BodyInserters.fromValue("hello"))
        .retrieve()
        .bodyToMono(String.class))
        .assertNext(body -> assertEquals("hello", body))
        .verifyComplete();
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
        .exchangeToMono(clientResponse -> Mono.just(clientResponse.statusCode())))
        .assertNext(status -> assertEquals(HttpStatus.OK, status))
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
        .exchangeToMono(clientResponse -> Mono.just(clientResponse.statusCode())))
        .assertNext(status -> assertEquals(HttpStatus.FORBIDDEN, status))
        .verifyComplete();
  }

}
