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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bremersee.security.authentication.resourceserver.servlet.withoutredis.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * The in memory test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=resourceserver-in-memory",
        "bremersee.cors.enable=true",
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
        "bremersee.auth.in-memory-users[1].authorities=ROLE_ADMIN",
        "bremersee.auth.in-memory-users[2].name=someone",
        "bremersee.auth.in-memory-users[2].password=someone",
        "bremersee.auth.in-memory-users[2].authorities=ROLE_SOMETHING",
        "bremersee.exception-mapping.api-paths=/**"
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class InMemoryTest {

  /**
   * The Test rest template.
   */
  @Autowired
  TestRestTemplate testRestTemplate;

  /**
   * The Rest template builder.
   */
  @Autowired
  RestTemplateBuilder restTemplateBuilder;

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
   * Rest template rest template.
   *
   * @return the rest template
   */
  RestTemplate restTemplate() {
    return restTemplateBuilder
        .rootUri(baseUrl())
        .build();
  }

  /**
   * Rest template rest template.
   *
   * @param user the user
   * @param password the password
   * @return the rest template
   */
  RestTemplate restTemplate(String user, String password) {
    return restTemplateBuilder
        .rootUri(baseUrl())
        .basicAuthentication(user, password)
        .build();
  }

  /**
   * Gets public.
   */
  @Test
  void getPublic() {
    assertEquals(
        "public",
        restTemplate()
            .getForEntity("/public", String.class)
            .getBody());
  }

  /**
   * Gets protected.
   */
  @Test
  void getProtected() {
    ResponseEntity<String> response = restTemplate("user", "user")
        .getForEntity("/protected", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("protected", response.getBody());
  }

  /**
   * Gets protected and expect forbidden.
   */
  @Test
  void getProtectedAndExpectForbidden() {
    // We use the test rest template here, because the real rest template will throw an exception
    ResponseEntity<String> response = testRestTemplate
        .withBasicAuth("someone", "someone")
        .getForEntity("/protected", String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  /**
   * Post protected.
   */
  @Test
  void postProtected() {
    ResponseEntity<String> response = restTemplate("admin", "admin")
        .postForEntity("/protected", "hello", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("hello", response.getBody());
  }

}
