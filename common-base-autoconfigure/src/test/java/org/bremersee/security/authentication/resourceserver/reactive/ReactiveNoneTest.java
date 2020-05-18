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

import org.bremersee.security.authentication.resourceserver.reactive.withoutredis.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

/**
 * The reactive none test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=reactive",
        "spring.application.name=resourceserver-none",
        "bremersee.auth.resource-server=none",
        "bremersee.exception-mapping.api-paths=/**"
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class ReactiveNoneTest {

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
        .retrieve()
        .bodyToMono(String.class))
        .assertNext(body -> assertEquals("protected", body))
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
        .body(BodyInserters.fromValue("hello"))
        .retrieve()
        .bodyToMono(String.class))
        .assertNext(body -> assertEquals("hello", body))
        .verifyComplete();
  }

}
